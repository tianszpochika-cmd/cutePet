<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { RECORD_KINDS, recordFields, validateRecord } from '../../../web/src/domain/pet.ts';

interface Pet { id: number; name: string; species: string; state: string }
interface SavedRecord { id: number }

const route = useRoute();
const router = useRouter();
const authorized = ref(Boolean(getAccessToken()));
const pets = ref<Pet[]>([]);
const petsLoading = ref(authorized.value);
const petsError = ref('');
const selectedPetId = ref('');
const initialKind = typeof route.query.kind === 'string' && (RECORD_KINDS as readonly string[]).includes(route.query.kind)
  ? route.query.kind : '疫苗';
const kind = ref(initialKind);
const eventDate = ref('');
const values = reactive<Record<string, string>>({});
const attempted = ref(false);
const saving = ref(false);
const saveError = ref('');
const needsReview = ref(false);
const savedRecord = ref<SavedRecord | null>(null);

const selectedPet = computed(() => pets.value.find((pet) => String(pet.id) === selectedPetId.value) ?? null);
const fields = computed(() => recordFields(kind.value).filter((field) =>
  field.key !== 'eventDate' && field.key !== 'photo' && field.key !== 'measuredAt'));
const weightUnavailable = computed(() => kind.value === '体重');
const effectiveEventDate = computed(() => kind.value === '用药' ? values.validFrom || '' : eventDate.value);
const eventDateLabel = computed(() => {
  if (kind.value === '疫苗') return '接种日期';
  if (kind.value === '驱虫') return '驱虫日期';
  if (kind.value === '体检') return '体检日期';
  if (kind.value === '就医') return '就诊日期';
  return '实际发生日期';
});
const now = new Date();
const today = [
  now.getFullYear(),
  String(now.getMonth() + 1).padStart(2, '0'),
  String(now.getDate()).padStart(2, '0'),
].join('-');

function validPet(value: unknown): value is Pet {
  if (!value || typeof value !== 'object') return false;
  const row = value as Record<string, unknown>;
  return typeof row.id === 'number' && Number.isSafeInteger(row.id) && row.id > 0 &&
    typeof row.name === 'string' && typeof row.species === 'string' && row.state === 'ACTIVE';
}
function fieldLabel(key: string): string {
  if (key === 'eventDate') return eventDateLabel.value;
  return recordFields(kind.value).find((field) => field.key === key)?.label ?? key;
}
function errorCopy(code: string): string {
  if (code.startsWith('REQUIRED:')) return '请填写' + fieldLabel(code.slice(9)) + '。';
  if (code === 'EVENT_FUTURE_NOT_ALLOWED') return '实际发生日期不能晚于今天。';
  if (code === 'MED_COURSE_INVALID') return '结束日期不能早于开始日期。';
  return code;
}
function validationError(): string | null {
  if (!selectedPet.value) return '请先选择一只在照护的宠物。';
  if (weightUnavailable.value) return '体重写入接口尚未接入本端，请从宠物档案查看已有体重。';
  if (!effectiveEventDate.value) return '请填写' + (kind.value === '用药' ? '开始日期' : eventDateLabel.value) + '。';
  if (effectiveEventDate.value > today) return '实际发生日期不能晚于今天。';
  if (values.validFrom && values.validFrom > today) return '用药开始日期不能晚于今天。';
  if (values.nextReminderDate && values.nextReminderDate <= effectiveEventDate.value) {
    return '下次建议日期须晚于实际发生日期。';
  }
  const error = validateRecord(kind.value, { ...values, eventDate: effectiveEventDate.value });
  return error ? errorCopy(error) : null;
}
const visibleValidation = computed(() => attempted.value ? validationError() : null);

async function loadPets() {
  authorized.value = Boolean(getAccessToken());
  if (!authorized.value) return;
  petsLoading.value = true;
  petsError.value = '';
  try {
    const result: unknown = await api.petsList();
    if (!Array.isArray(result) || !result.every(validPet)) throw new Error('宠物列表格式暂无法确认');
    pets.value = result;
    const requestedId = typeof route.query.petId === 'string' ? route.query.petId : '';
    selectedPetId.value = requestedId
      ? result.some((pet) => String(pet.id) === requestedId) ? requestedId : ''
      : String(result[0]?.id ?? '');
  } catch (cause) {
    pets.value = [];
    selectedPetId.value = '';
    petsError.value = cause instanceof Error ? cause.message : '宠物列表读取失败';
  } finally { petsLoading.value = false; }
}
function selectKind(next: string) {
  if (!(RECORD_KINDS as readonly string[]).includes(next) || saving.value) return;
  kind.value = next;
  attempted.value = false;
  saveError.value = '';
  for (const key of Object.keys(values)) delete values[key];
}
async function save() {
  attempted.value = true;
  saveError.value = '';
  const error = validationError();
  if (error || !authorized.value || saving.value || needsReview.value) return;
  const pet = selectedPet.value;
  if (!pet) return;
  saving.value = true;
  let submitted = false;
  try {
    const current: unknown = await api.petGet({ path: { id: pet.id } });
    if (!validPet(current) || current.id !== pet.id) throw new Error('所选宠物档案状态未通过核对');
    const body = {
      kind: kind.value,
      eventDate: effectiveEventDate.value,
      validFrom: values.validFrom || undefined,
      validTo: values.validTo || undefined,
      org: values.org?.trim() || undefined,
      doctor: values.doctor?.trim() || undefined,
      note: [values.kindName?.trim(), values.note?.trim()].filter(Boolean).join(' · '),
      nextReminderDate: values.nextReminderDate || undefined,
    };
    submitted = true;
    const result: unknown = await api.recordCreate({ path: { id: pet.id }, body });
    const recordId = result && typeof result === 'object' ? (result as { id?: unknown }).id : null;
    if (typeof recordId !== 'number' || !Number.isSafeInteger(recordId) || recordId <= 0) {
      throw new Error('平台未返回可核对的记录编号');
    }
    savedRecord.value = { id: recordId };
  } catch (cause) {
    needsReview.value = submitted;
    saveError.value = submitted
      ? '保存结果尚未确认：' + (cause instanceof Error ? cause.message : '请求失败') + '。请先查看宠物时间线，避免重复提交。'
      : '尚未提交记录：' + (cause instanceof Error ? cause.message : '档案核对失败') + '。请核对宠物后重试。';
  } finally { saving.value = false; }
}
function again() {
  savedRecord.value = null;
  attempted.value = false;
  needsReview.value = false;
  saveError.value = '';
  eventDate.value = '';
  for (const key of Object.keys(values)) delete values[key];
}
watch(() => route.query.petId, (next) => {
  if (typeof next === 'string' && pets.value.some((pet) => String(pet.id) === next)) selectedPetId.value = next;
});
onMounted(loadPets);
</script>

<template>
  <div class="quick-record">
    <header class="top">
      <button type="button" class="back" @click="router.push('/pets')">← 宠物列表</button>
      <span class="eyebrow">ONE STEP CARE</span>
      <h1>记录实际发生的事</h1>
      <p>先选宠物，再填一件事。提交成功后会出现平台记录编号。</p>
    </header>

    <section v-if="!authorized" class="state" role="status">
      <h2>登录后记录照护</h2>
      <p>健康记录只属于你有权照护的宠物。</p>
      <button type="button" class="primary" @click="router.push({ path: '/login', query: { return: route.fullPath } })">前往登录</button>
    </section>
    <section v-else-if="petsLoading" class="state" role="status">正在读取宠物档案…</section>
    <section v-else-if="petsError" class="state error-state" role="alert">
      <h2>宠物列表暂时不可用</h2><p>{{ petsError }}</p>
      <button type="button" class="secondary" @click="loadPets">重新读取</button>
    </section>
    <section v-else-if="!pets.length" class="state">
      <h2>先建立宠物档案</h2>
      <p>当前没有可用于记录的在照护宠物。</p>
      <button type="button" class="primary" @click="router.push('/pets/new')">建立档案</button>
    </section>
    <section v-else-if="savedRecord" class="success card" data-testid="success" role="status">
      <span class="success-mark" aria-hidden="true">✓</span>
      <h2>记录已保存</h2>
      <p>{{ selectedPet?.name }}的{{ kind }}记录编号 #{{ savedRecord.id }} 已由平台返回。</p>
      <p v-if="kind === '疫苗' || kind === '驱虫'" class="hint">下次日期仅是提醒建议；提醒计划需要单独创建，当前没有自动排期。</p>
      <button type="button" class="primary" @click="router.push('/pets/' + selectedPetId + '?tab=records')">查看宠物时间线</button>
      <button type="button" class="secondary" @click="again">再记一条</button>
    </section>

    <template v-else>
      <section class="card form-section">
        <div class="section-head"><span class="step">01</span><div><h2>给谁记录？</h2><p>只显示平台返回的在照护宠物</p></div></div>
        <label class="field">选择宠物
          <select v-model="selectedPetId" :disabled="saving || needsReview" data-testid="pet">
            <option value="" disabled>请选择宠物</option>
            <option v-for="pet in pets" :key="pet.id" :value="String(pet.id)">{{ pet.name }} · {{ pet.species }}</option>
          </select>
        </label>
      </section>
      <section class="card form-section">
        <div class="section-head"><span class="step">02</span><div><h2>发生了什么？</h2><p>选择一类记录后填写实际信息</p></div></div>
        <div class="kind-grid" role="group" aria-label="记录类型">
          <button v-for="item in RECORD_KINDS" :key="item" type="button" :class="{ picked: kind === item }" :aria-pressed="kind === item" :disabled="saving || needsReview" @click="selectKind(item)">{{ item }}</button>
        </div>
        <p v-if="weightUnavailable" class="unavailable" role="status">体重写入接口尚未接入本端。可以在宠物档案查看已有体重，当前不会创建体重记录。</p>
        <template v-else>
          <label v-if="kind !== '用药'" class="field">{{ eventDateLabel }} <span aria-hidden="true">*</span>
            <input v-model="eventDate" type="date" :max="today" data-testid="eventDate" :disabled="saving || needsReview" />
          </label>
          <p v-else class="med-note">用药开始日期同时作为这条记录的发生日期。</p>
          <label v-for="field in fields" :key="field.key" class="field">
            {{ field.key === 'nextReminderDate' ? '下次日期建议（不会自动创建提醒）' : field.label }}
            <span v-if="field.required" aria-hidden="true">*</span>
            <textarea v-if="field.type === 'textarea'" v-model="values[field.key]" rows="3" :data-testid="field.key" :disabled="saving || needsReview" />
            <input v-else v-model="values[field.key]" :type="field.type === 'date' ? 'date' : 'text'" :max="field.key === 'validFrom' ? today : undefined" :data-testid="field.key" :disabled="saving || needsReview" />
          </label>
        </template>
      </section>
      <p v-if="visibleValidation" class="error" role="alert" data-testid="error">{{ visibleValidation }}</p>
      <p v-if="saveError" class="error" role="alert">{{ saveError }}</p>
      <div v-if="needsReview" class="state review-state">
        <p>若平台已接收这次请求，重复提交会生成第二条记录。</p>
        <button type="button" class="secondary" @click="router.push('/pets/' + selectedPetId + '?tab=records')">先核对时间线</button>
      </div>
      <button type="button" class="primary save" data-testid="save" :disabled="saving || needsReview || weightUnavailable" @click="save">{{ saving ? '正在保存…' : needsReview ? '请先核对结果' : '确认保存记录' }}</button>
      <p class="footnote">保存记录不会完成待办，也不会自动建立提醒计划；照片上传尚未接入。</p>
    </template>
  </div>
</template>

<style scoped>
.quick-record{display:grid;gap:14px;padding:18px 16px calc(34px + env(safe-area-inset-bottom));color:#30241d}.top{display:grid;justify-items:start;gap:6px}.back{min-height:44px;padding:0;border:0;background:transparent;color:#93400e;font-size:13px;font-weight:750}.eyebrow{color:#a6490e;font-size:11px;font-weight:800;letter-spacing:.12em}.top h1{margin:0;font-size:27px;line-height:1.2}.top p,.section-head p,.footnote{margin:0;color:#706155;font-size:13px;line-height:1.65}.card,.state{padding:18px;border:1px solid #e8dbce;border-radius:18px;background:#fff}.state{display:grid;justify-items:start;gap:12px}.state h2,.card h2{margin:0;font-size:18px}.state p{margin:0;color:#706155;font-size:13px;line-height:1.7}.error-state{background:#fff4f0}.form-section{display:grid;gap:17px}.section-head{display:flex;gap:11px;align-items:start}.step{display:grid;place-items:center;flex:none;width:33px;height:33px;border-radius:10px;background:#fff0df;color:#98440f;font-size:12px;font-weight:850}.section-head h2{margin:1px 0 3px}.field{display:grid;gap:8px;color:#47382d;font-size:13px;font-weight:750}.field>span{display:inline;color:#a33b22}.field input,.field select,.field textarea{width:100%;min-height:48px;padding:10px 12px;border:1px solid #d9c9ba;border-radius:11px;background:#fff;color:#30241d;font-family:inherit;font-size:16px;font-weight:400;line-height:1.4}.field textarea{resize:vertical}.kind-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:7px}.kind-grid button{min-height:46px;padding:8px 3px;border:1px solid #e0d2c5;border-radius:10px;background:#fffaf5;color:#624b3b;font-size:13px;font-weight:750}.kind-grid button.picked{border-color:#b85111;background:#b85111;color:#fff}.unavailable{margin:0;padding:13px;border-radius:11px;background:#fff5e8;color:#67441f;font-size:13px;line-height:1.6}.med-note{margin:0;color:#706155;font-size:12px;line-height:1.6}.primary,.secondary{min-height:48px;padding:10px 14px;border-radius:12px;font-size:14px;font-weight:750}.primary{border:0;background:#b85111;color:#fff}.secondary{border:1px solid #d0aa88;background:#fff;color:#8e3c0b}.save{width:100%;min-height:52px;font-size:16px}.error{margin:0;padding:10px 12px;border-radius:10px;background:#fff1ec;color:#a12e28;font-size:13px;line-height:1.6}.review-state{background:#fff7eb}.footnote{text-align:center;font-size:12px}.success{display:grid;justify-items:start;gap:14px}.success-mark{display:grid;place-items:center;width:48px;height:48px;border-radius:50%;background:#e2f3e8;color:#17784d;font-size:28px;font-weight:800}.success p{margin:0;color:#66594f;font-size:14px;line-height:1.65}.success .hint{font-size:12px}.success button{width:100%}button:disabled{opacity:.55}button:focus-visible,input:focus-visible,select:focus-visible,textarea:focus-visible{outline:3px solid #783307;outline-offset:2px}@media(max-width:350px){.card,.state{padding:15px}.kind-grid{grid-template-columns:repeat(3,minmax(0,1fr))}.top h1{font-size:24px}}
</style>

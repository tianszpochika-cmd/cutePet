<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';

interface PetCard { id: number; name: string; species: string; breed: string; state: string }
interface PendingReminder { id: number; petId: number; petName: string; type: string; nextDue: string | null }

const router = useRouter();
const authorized = ref(Boolean(getAccessToken()));
const pets = ref<PetCard[]>([]);
const reminders = ref<PendingReminder[]>([]);
const petsLoading = ref(authorized.value);
const remindersLoading = ref(authorized.value);
const petsError = ref('');
const remindersError = ref('');

function validPet(value: unknown): value is PetCard {
  if (!value || typeof value !== 'object') return false;
  const row = value as Record<string, unknown>;
  return typeof row.id === 'number' && Number.isSafeInteger(row.id) &&
    typeof row.name === 'string' && typeof row.species === 'string' &&
    typeof row.breed === 'string' && row.state === 'ACTIVE';
}
function validReminder(value: unknown): value is PendingReminder {
  if (!value || typeof value !== 'object') return false;
  const row = value as Record<string, unknown>;
  return typeof row.id === 'number' && typeof row.petId === 'number' &&
    typeof row.petName === 'string' && typeof row.type === 'string' &&
    (row.nextDue === null || typeof row.nextDue === 'string');
}
async function loadPets() {
  if (!authorized.value) return;
  petsLoading.value = true;
  petsError.value = '';
  try {
    const result: unknown = await api.petsList();
    if (!Array.isArray(result) || !result.every(validPet)) throw new Error('宠物列表格式暂无法确认');
    pets.value = result;
  } catch (cause) {
    pets.value = [];
    petsError.value = cause instanceof Error ? cause.message : '宠物列表读取失败';
  } finally { petsLoading.value = false; }
}
async function loadReminders() {
  if (!authorized.value) return;
  remindersLoading.value = true;
  remindersError.value = '';
  try {
    const result: unknown = await api.remindersPending();
    if (!Array.isArray(result) || !result.every(validReminder)) throw new Error('提醒列表格式暂无法确认');
    reminders.value = [...result].sort((a, b) => (a.nextDue ?? '').localeCompare(b.nextDue ?? ''));
  } catch (cause) {
    reminders.value = [];
    remindersError.value = cause instanceof Error ? cause.message : '提醒列表读取失败';
  } finally { remindersLoading.value = false; }
}
function refresh() {
  authorized.value = Boolean(getAccessToken());
  if (!authorized.value) return;
  void loadPets();
  void loadReminders();
}
function goPrivate(path: string) {
  void router.push(authorized.value ? path : { path: '/login', query: { return: path } });
}
onMounted(refresh);
</script>

<template>
  <div class="care-home">
    <header class="intro">
      <span class="eyebrow">TODAY'S CARE</span>
      <h1>把今天的照护，接着做好。</h1>
      <p>先看平台返回的提醒，再进入宠物档案记录实际发生的事。</p>
    </header>

    <section v-if="!authorized" class="guest card" role="status">
      <span class="mark" aria-hidden="true">♡</span>
      <div><h2>照护资料需要登录</h2><p>公开资讯和探索可以直接浏览；登录后才能读取自己的宠物和提醒。</p></div>
      <button type="button" class="primary" @click="goPrivate('/pets')">前往登录</button>
    </section>

    <template v-else>
      <section class="card reminders" aria-labelledby="reminders-title">
        <div class="section-head"><div><span class="eyebrow">01 · CARE FIRST</span><h2 id="reminders-title">接下来要照顾的事</h2></div><span class="section-tag">平台提醒</span></div>
        <p class="section-copy">打开提醒只查看详情，不会直接算完成；健康待办须与有效记录一起确认。</p>
        <p v-if="remindersLoading" class="state" role="status">正在读取提醒…</p>
        <div v-else-if="remindersError" class="state error" role="alert"><p>{{ remindersError }}</p><button type="button" class="text-button" @click="loadReminders">重新读取</button></div>
        <div v-else-if="!reminders.length" class="state empty" data-testid="reminder-empty"><strong>当前没有平台提醒</strong><p>记录中的下次日期可能只是建议；只有已创建的计划才会出现在这里。</p></div>
        <div v-else class="reminder-list">
          <button v-for="item in reminders.slice(0, 3)" :key="item.id" type="button" class="reminder-row" data-testid="reminder-bar" @click="goPrivate('/pets/' + item.petId + '?tab=reminders')">
            <span class="due" aria-hidden="true">◷</span><span class="row-main"><strong>{{ item.petName }} · {{ item.type }}</strong><small>计划日期 {{ item.nextDue ?? '待核对' }}</small></span><span class="arrow" aria-hidden="true">→</span>
          </button>
          <p v-if="reminders.length > 3" class="more-note">另有 {{ reminders.length - 3 }} 项提醒；可进入对应宠物查看。</p>
        </div>
      </section>

      <section class="card pets" aria-labelledby="pets-title">
        <div class="section-head"><div><span class="eyebrow">02 · YOUR PETS</span><h2 id="pets-title">当前宠物</h2></div><button type="button" class="text-button" @click="goPrivate('/pets')">全部档案 →</button></div>
        <p v-if="petsLoading" class="state" role="status">正在读取宠物档案…</p>
        <div v-else-if="petsError" class="state error" role="alert"><p>{{ petsError }}</p><button type="button" class="text-button" @click="loadPets">重新读取</button></div>
        <div v-else-if="!pets.length" class="state empty" data-testid="pet-empty"><strong>还没有可见的在照护档案</strong><p>可以从第一只宠物开始建档。归档或回收站里的宠物会单独显示。</p><button type="button" class="secondary" @click="goPrivate('/pets/new')">建立宠物档案</button></div>
        <div v-else class="pet-list">
          <button v-for="pet in pets.slice(0, 2)" :key="pet.id" type="button" class="pet-row" @click="goPrivate('/pets/' + pet.id)">
            <span class="pet-mark" aria-hidden="true">✿</span><span><strong>{{ pet.name }}</strong><small>{{ pet.species }}{{ pet.breed ? ' · ' + pet.breed : '' }}</small></span><span class="arrow" aria-hidden="true">→</span>
          </button>
        </div>
      </section>

      <button type="button" class="primary quick" data-testid="quick-record" @click="goPrivate('/quick-record')"><span aria-hidden="true">＋</span> 一步记录</button>
      <p class="quick-note">选择宠物与事项后填写实际信息；只有平台返回记录编号才显示已保存。</p>
    </template>

    <section class="discover" aria-labelledby="discover-title">
      <span class="eyebrow">03 · DISCOVER WHEN NEEDED</span><h2 id="discover-title">需要时，再去发现</h2>
      <div class="discover-grid">
        <button type="button" @click="router.push('/news')"><span aria-hidden="true">▤</span><strong>照护资讯</strong><small>阅读公开内容 →</small></button>
        <button type="button" @click="router.push('/explore')"><span aria-hidden="true">⌖</span><strong>附近探索</strong><small>查找场所与活动 →</small></button>
        <button type="button" @click="router.push('/goods')"><span aria-hidden="true">◈</span><strong>宠物用品</strong><small>查看内容导购 →</small></button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.care-home{display:grid;gap:15px;padding:24px 16px calc(30px + env(safe-area-inset-bottom));color:#30241d}.intro{padding:3px 2px 4px}.eyebrow{display:block;color:#a94c11;font-size:11px;font-weight:800;letter-spacing:.13em}.intro h1{max-width:13ch;margin:9px 0;font-size:clamp(28px,8vw,37px);line-height:1.18}.intro p,.section-copy,.quick-note{margin:0;color:#6f6054;font-size:13px;line-height:1.7}.card{padding:18px;border:1px solid #e9ddd1;border-radius:18px;background:#fff}.section-head{display:flex;justify-content:space-between;align-items:center;gap:8px}.section-head h2,.discover h2{margin:6px 0 0;font-size:20px}.section-tag{flex:none;padding:5px 9px;border-radius:999px;background:#fff0e2;color:#8e4311;font-size:11px;font-weight:750}.section-copy{margin-top:9px}.state{margin-top:14px}.state{padding:15px;border-radius:12px;background:#f9f6f1;color:#65574b;font-size:13px;line-height:1.65}.state p{margin:4px 0 0}.state strong{color:#30241d}.state.error{background:#fff1ef;color:#a1302c}.state.empty{display:grid;justify-items:start;gap:8px}.reminder-list,.pet-list{display:grid;gap:8px;margin-top:15px}.reminder-row,.pet-row{width:100%;display:flex;align-items:center;gap:11px;min-height:64px;padding:11px 12px;border:1px solid #eee1d5;border-radius:13px;background:#fffbf7;color:#30241d;text-align:left}.due,.pet-mark{width:38px;height:38px;flex:none;display:grid;place-items:center;border-radius:12px;background:#fce6d3;color:#a7470d;font-size:21px}.row-main,.pet-row>span:nth-child(2){flex:1;min-width:0}.reminder-row strong,.pet-row strong,.reminder-row small,.pet-row small{display:block}.reminder-row strong,.pet-row strong{font-size:14px}.reminder-row small,.pet-row small{margin-top:4px;color:#716258;font-size:12px}.arrow{flex:none;color:#a94c11;font-size:21px}.more-note{margin:2px 0;color:#716258;font-size:12px}.text-button{min-height:44px;padding:0;border:0;background:transparent;color:#9c430f;font-size:13px;font-weight:750}.primary,.secondary{min-height:48px;padding:10px 18px;border-radius:13px;font-size:15px;font-weight:750}.primary{border:0;background:#b85111;color:#fff}.secondary{border:1px solid #c88456;background:#fff;color:#8c3d0e}.quick{width:100%;min-height:55px;display:flex;align-items:center;justify-content:center;gap:9px;font-size:17px}.quick-note{margin-top:-8px;padding:0 3px;font-size:12px}.discover{padding:16px 0 5px}.discover-grid{display:grid;grid-template-columns:1fr 1fr;gap:9px;margin-top:13px}.discover-grid button{min-height:102px;display:grid;justify-items:start;align-content:center;gap:4px;padding:15px;border:1px solid #e9ddd1;border-radius:16px;background:#fff;color:#30241d;text-align:left}.discover-grid button:last-child{grid-column:1/-1;min-height:78px}.discover-grid button>span{color:#9c430f;font-size:24px}.discover-grid strong{font-size:14px}.discover-grid small{color:#716258;font-size:12px}.guest{display:grid;grid-template-columns:40px 1fr;gap:11px;align-items:start}.guest .mark{display:grid;place-items:center;width:40px;height:40px;border-radius:13px;background:#fff0e2;color:#a7470d;font-size:27px}.guest h2{margin:0;font-size:17px}.guest p{margin:6px 0 0;color:#6f6054;font-size:13px;line-height:1.6}.guest .primary{grid-column:1/-1;width:100%}button:focus-visible{outline:3px solid #783307;outline-offset:2px}@media(max-width:350px){.card{padding:15px}.section-head h2{font-size:18px}.discover-grid button{padding:12px}}
</style>

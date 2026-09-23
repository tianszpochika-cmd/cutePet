<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface Pet { id: number | string; name: string; species: string; breed?: string; birthDate?: string | null; state?: string }
interface TimelineEntry { type: string; date: string; note?: string; weightKg?: number }
interface HealthRecord { id: number | string; kind: string; eventDate: string; note?: string }
interface Reminder { id: number | string; title: string; type: string; state: string; nextDue?: string | null }
type Tab = 'timeline' | 'records' | 'reminders' | 'profile';

const route = useRoute();
const router = useRouter();
const petId = computed(() => String(route.params.id));
const pet = ref<Pet | null>(null);
const loading = ref(true);
const error = ref('');
const tab = ref<Tab>('timeline');
const panelLoading = ref(false);
const panelError = ref('');
const timeline = ref<TimelineEntry[]>([]);
const records = ref<HealthRecord[]>([]);
const reminders = ref<Reminder[]>([]);
const tabs: { id: Tab; label: string }[] = [
  { id: 'timeline', label: '时间线' },
  { id: 'records', label: '健康记录' },
  { id: 'reminders', label: '提醒计划' },
  { id: 'profile', label: '档案资料' },
];
let requestVersion = 0;
let panelRequestVersion = 0;

watch(() => route.params.id, () => { void loadPet(); }, { immediate: true });

async function loadPet() {
  const version = ++requestVersion;
  panelRequestVersion += 1;
  pet.value = null;
  error.value = '';
  loading.value = true;
  try {
    const result = (await api.petGet({ path: { id: petId.value } })) as unknown;
    if (!result || typeof result !== 'object' || !('id' in result) || String(result.id) !== petId.value ||
        !('name' in result) || typeof result.name !== 'string' ||
        !('species' in result) || typeof result.species !== 'string') {
      throw new Error('宠物详情格式异常');
    }
    if (version !== requestVersion) return;
    pet.value = result as Pet;
    void loadPanel();
  } catch (e) {
    if (version !== requestVersion) return;
    error.value = e instanceof Error ? e.message : '无法读取宠物档案';
  } finally {
    if (version === requestVersion) loading.value = false;
  }
}

function selectTab(next: Tab) {
  tab.value = next;
  void loadPanel();
}

async function loadPanel() {
  if (!pet.value || tab.value === 'profile') return;
  const version = requestVersion;
  const panelVersion = ++panelRequestVersion;
  const activeTab = tab.value;
  panelLoading.value = true;
  panelError.value = '';
  try {
    const response = activeTab === 'timeline'
      ? await api.petTimeline({ path: { id: petId.value } })
      : activeTab === 'records'
        ? await api.petRecords({ path: { id: petId.value } })
        : await api.reminderList({ path: { id: petId.value } });
    if (!Array.isArray(response)) throw new Error('内容格式异常');
    if (version !== requestVersion || panelVersion !== panelRequestVersion || activeTab !== tab.value) return;
    if (activeTab === 'timeline') timeline.value = response as TimelineEntry[];
    else if (activeTab === 'records') records.value = response as HealthRecord[];
    else reminders.value = response as Reminder[];
  } catch (e) {
    if (version !== requestVersion || panelVersion !== panelRequestVersion || activeTab !== tab.value) return;
    panelError.value = e instanceof Error ? e.message : '内容读取失败';
  } finally {
    if (version === requestVersion && panelVersion === panelRequestVersion && activeTab === tab.value) panelLoading.value = false;
  }
}
</script>

<template>
  <main class="pet-detail">
    <button type="button" class="back" @click="router.push('/pets')">‹ 我的宠物</button>
    <p v-if="loading" class="state" role="status">正在读取宠物档案…</p>
    <div v-else-if="error || !pet" class="state error-state" role="alert">
      <h1>无法查看这只宠物</h1>
      <p>{{ error || '档案不可用或授权已变更。' }}</p>
      <div class="state-actions">
        <button type="button" class="ghost" @click="loadPet">重新加载</button>
        <button type="button" class="ghost" @click="router.push('/pets')">返回宠物列表</button>
      </div>
    </div>

    <template v-else>
      <header class="hero">
        <div class="avatar" aria-hidden="true">{{ pet.name.slice(0, 1) }}</div>
        <div class="hero-copy">
          <span class="eyebrow">宠物档案</span>
          <h1>{{ pet.name }}</h1>
          <p>{{ pet.species }}{{ pet.breed ? ` · ${pet.breed}` : '' }}</p>
        </div>
        <div v-if="pet.state === 'ACTIVE'" class="actions">
          <button type="button" class="primary" data-testid="go-record" @click="router.push(`/pets/${petId}/record`)">＋ 添加记录</button>
          <button type="button" class="ghost" @click="router.push(`/pets/${petId}/weights`)">体重曲线</button>
        </div>
      </header>
      <p v-if="pet.state && pet.state !== 'ACTIVE'" class="archive-note">该档案当前未启用。请到归档与回收站核对状态后再进行照护操作。</p>

      <nav class="tabs" aria-label="宠物详情分类">
        <button v-for="item in tabs" :key="item.id" type="button" :class="{ on: tab === item.id }" :aria-current="tab === item.id ? 'page' : undefined" @click="selectTab(item.id)">
          {{ item.label }}
        </button>
      </nav>

      <section class="panel">
        <p v-if="panelLoading && tab !== 'profile'" class="muted" role="status">正在读取{{ tabs.find((item) => item.id === tab)?.label }}…</p>
        <div v-else-if="panelError && tab !== 'profile'" class="panel-state" role="alert">
          <p>{{ panelError }}</p><button type="button" class="ghost" @click="loadPanel">重试</button>
        </div>
        <template v-else-if="tab === 'timeline'">
          <div v-if="timeline.length === 0" class="panel-state" data-testid="panel-timeline">
            <h2>还没有照护时间线</h2><p>保存第一条健康或体重记录后，平台确认的事项会出现在这里。</p>
            <button v-if="pet.state === 'ACTIVE'" type="button" class="ghost" @click="router.push(`/pets/${petId}/record`)">添加健康记录</button>
          </div>
          <ol v-else class="entry-list" data-testid="panel-timeline">
            <li v-for="(item, index) in timeline" :key="index"><time>{{ item.date }}</time><strong>{{ item.type }}</strong><span>{{ item.weightKg == null ? item.note || '已记录' : `${item.weightKg} kg` }}</span></li>
          </ol>
        </template>
        <template v-else-if="tab === 'records'">
          <div v-if="records.length === 0" class="panel-state" data-testid="panel-records">
            <h2>还没有健康记录</h2><p>疫苗、驱虫、体检等记录会在保存并收到平台确认后显示。</p>
            <button v-if="pet.state === 'ACTIVE'" type="button" class="ghost" @click="router.push(`/pets/${petId}/record`)">记录一件事</button>
          </div>
          <ul v-else class="entry-list" data-testid="panel-records">
            <li v-for="item in records" :key="item.id"><time>{{ item.eventDate }}</time><strong>{{ item.kind }}</strong><span>{{ item.note || '已记录' }}</span></li>
          </ul>
        </template>
        <template v-else-if="tab === 'reminders'">
          <div v-if="reminders.length === 0" class="panel-state" data-testid="panel-reminders">
            <h2>还没有提醒计划</h2><p>记录中的“下次日期”只会生成建议，计划仍需单独创建。</p>
          </div>
          <ul v-else class="entry-list" data-testid="panel-reminders">
            <li v-for="item in reminders" :key="item.id"><time>{{ item.nextDue || '待排期' }}</time><strong>{{ item.title || item.type }}</strong><span>{{ item.state }}</span></li>
          </ul>
          <button type="button" class="text-link" @click="router.push(`/pets/${petId}/reminders`)">查看提醒计划说明 →</button>
        </template>
        <dl v-else class="profile" data-testid="panel-profile">
          <div><dt>昵称</dt><dd>{{ pet.name }}</dd></div>
          <div><dt>类型</dt><dd>{{ pet.species }}</dd></div>
          <div><dt>品种</dt><dd>{{ pet.breed || '尚未填写' }}</dd></div>
          <div><dt>生日</dt><dd>{{ pet.birthDate || '尚未填写' }}</dd></div>
          <div><dt>状态</dt><dd>{{ pet.state === 'ACTIVE' ? '照护中' : pet.state || '待核对' }}</dd></div>
        </dl>
      </section>
    </template>
  </main>
</template>

<style scoped>
.pet-detail { max-width: 1000px; margin: 0 auto; padding: 24px 16px 56px; color: #2b2118; }
.back { min-height: 44px; padding: 0; border: 0; background: transparent; color: #a8470c; cursor: pointer; }
.hero { display: flex; gap: 18px; align-items: center; flex-wrap: wrap; background: #fff; border: 1px solid #f0e6dc; border-radius: 22px; padding: 24px; margin: 10px 0 22px; }
.avatar { flex: none; width: 68px; height: 68px; display: grid; place-items: center; border-radius: 20px; background: #fff1e8; color: #a8470c; font-size: 30px; font-weight: 700; }
.hero-copy { min-width: 180px; }
.eyebrow { color: #a8470c; font-size: 13px; font-weight: 700; }
.hero h1 { margin: 2px 0; font-size: clamp(24px, 4vw, 32px); }
.hero p { margin: 0; color: #706255; }
.archive-note { padding: 12px 16px; border-radius: 12px; border: 1px solid #ecd9c8; background: #fff7ee; color: #754011; line-height: 1.6; }
.actions { margin-left: auto; display: flex; gap: 8px; flex-wrap: wrap; }
.actions button, .ghost { min-height: 44px; padding: 0 16px; border-radius: 999px; cursor: pointer; font-weight: 600; }
.primary { background: #b85111; color: #fff; border: 0; }
.ghost { background: #fff; border: 1px solid #dacabc; color: #684b39; }
.tabs { display: flex; gap: 4px; overflow-x: auto; border-bottom: 1px solid #e6d8cb; margin-bottom: 16px; }
.tabs button { min-height: 46px; min-width: max-content; padding: 0 16px; background: transparent; border: 0; border-bottom: 3px solid transparent; color: #706255; cursor: pointer; }
.tabs button.on { color: #a8470c; border-bottom-color: #b85111; font-weight: 700; }
.panel { min-height: 210px; background: #fff; border: 1px solid #f0e6dc; border-radius: 18px; padding: 20px; }
.panel-state, .state { padding: 20px; color: #604b3b; }
.state { background: #fff; border: 1px solid #f0e6dc; border-radius: 18px; }
.error-state { color: #962c24; }
.state h1, .panel-state h2 { margin: 0 0 8px; }
.panel-state p, .state p { margin: 0 0 12px; line-height: 1.6; }
.state-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.entry-list { list-style: none; padding: 0; margin: 0; display: grid; }
.entry-list li { display: grid; grid-template-columns: 110px minmax(90px, 1fr) 2fr; gap: 12px; padding: 14px 4px; border-bottom: 1px solid #f0e6dc; font-size: 14px; }
.entry-list li:last-child { border-bottom: 0; }
.entry-list time, .entry-list span, .muted { color: #706255; }
.profile { margin: 0; }
.profile div { display: grid; grid-template-columns: 110px 1fr; gap: 14px; padding: 12px 0; border-bottom: 1px solid #f0e6dc; }
.profile div:last-child { border-bottom: 0; }
.profile dt { color: #706255; }
.profile dd { margin: 0; }
.text-link { min-height: 44px; border: 0; background: transparent; color: #a8470c; cursor: pointer; font-weight: 600; }
button:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
@media (max-width: 600px) { .hero { padding: 18px; } .actions { width: 100%; margin-left: 0; } .actions button { flex: 1; } .entry-list li { grid-template-columns: 1fr 1fr; } .entry-list li span { grid-column: 1 / -1; } }
</style>

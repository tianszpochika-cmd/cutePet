<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';

interface PetRow { id: number; name: string; species: string; breed: string; state: string }
const router = useRouter();
const authorized = ref(Boolean(getAccessToken()));
const loading = ref(authorized.value);
const error = ref('');
const pets = ref<PetRow[]>([]);

function isPet(value: unknown): value is PetRow {
  if (!value || typeof value !== 'object') return false;
  const row = value as Record<string, unknown>;
  return typeof row.id === 'number' && Number.isSafeInteger(row.id) && row.id > 0 &&
    typeof row.name === 'string' && typeof row.species === 'string' &&
    typeof row.breed === 'string' && row.state === 'ACTIVE';
}
async function load() {
  authorized.value = Boolean(getAccessToken());
  if (!authorized.value) { pets.value = []; return; }
  loading.value = true;
  error.value = '';
  try {
    const result: unknown = await api.petsList();
    if (!Array.isArray(result) || !result.every(isPet)) throw new Error('宠物列表格式暂无法确认');
    pets.value = result;
  } catch (cause) {
    pets.value = [];
    error.value = cause instanceof Error ? cause.message : '宠物档案读取失败';
  } finally { loading.value = false; }
}
function go(path: string) {
  void router.push(authorized.value ? path : { path: '/login', query: { return: path } });
}
onMounted(() => { void load(); });
</script>

<template>
  <div class="pets-page">
    <header><span class="eyebrow">CARE PROFILE</span><h1>我的宠物</h1><p>每只宠物的记录、体重和提醒分别查看，平台返回什么就显示什么。</p></header>
    <section v-if="!authorized" class="state" role="status"><span class="state-icon" aria-hidden="true">♡</span><h2>登录后查看档案</h2><p>宠物资料只对有权的账号显示。可以先浏览公开内容，准备好后再登录。</p><button type="button" class="primary" @click="go('/pets')">前往登录</button></section>
    <template v-else>
      <p v-if="loading" class="state compact" role="status">正在读取宠物档案…</p>
      <section v-else-if="error" class="state error" role="alert"><h2>暂时无法读取档案</h2><p>{{ error }}</p><button type="button" class="secondary" @click="load">重试</button></section>
      <section v-else-if="!pets.length" class="state"><span class="state-icon" aria-hidden="true">✿</span><h2>还没有在照护的宠物</h2><p>从昵称、类型和品种开始建档。照片与体重可在相应能力接入后再补充。</p><button type="button" class="primary" @click="go('/pets/new')">建立第一份档案</button><button type="button" class="text-button" @click="go('/pets/recycle')">查找归档或回收站 →</button></section>
      <template v-else>
        <div class="count"><strong>{{ pets.length }}</strong><span>只在照护的宠物</span></div>
        <ul class="list"><li v-for="pet in pets" :key="pet.id" :data-testid="'pet-' + pet.id"><button type="button" class="pet-card" @click="go('/pets/' + pet.id)"><span class="avatar" aria-hidden="true">✿</span><span class="identity"><strong>{{ pet.name }}</strong><small>{{ pet.species }}{{ pet.breed ? ' · ' + pet.breed : '' }}</small></span><span class="arrow" aria-hidden="true">→</span></button></li></ul>
        <button type="button" class="add" @click="go('/pets/new')">＋ 新建宠物档案</button>
        <div class="links"><button type="button" @click="go('/pets/recycle')">归档与回收站 →</button><button type="button" @click="go('/quick-record')">一步记录 →</button></div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.pets-page{display:grid;gap:15px;padding:24px 16px calc(28px + env(safe-area-inset-bottom));color:#30241d}.eyebrow{color:#9c430f;font-size:11px;font-weight:800;letter-spacing:.13em}header h1{margin:7px 0 8px;font-size:31px}header p{max-width:33ch;margin:0;color:#6f6054;font-size:13px;line-height:1.7}.state{display:grid;justify-items:start;gap:11px;padding:24px;border:1px solid #eaded2;border-radius:19px;background:#fff}.state.compact{display:block;color:#6f6054;font-size:13px}.state.error{background:#fff5f3;color:#a1302c}.state-icon{display:grid;place-items:center;width:48px;height:48px;border-radius:15px;background:#fff0e1;color:#ad4f13;font-size:28px}.state h2{margin:0;font-size:20px}.state p{margin:0;color:#6f6054;font-size:13px;line-height:1.7}.count{display:flex;align-items:baseline;gap:8px;padding:3px 2px}.count strong{font-size:25px;color:#9c430f}.count span{color:#6f6054;font-size:12px}.list{display:grid;gap:10px;list-style:none;margin:0;padding:0}.pet-card{width:100%;min-height:77px;display:flex;align-items:center;gap:12px;padding:13px;border:1px solid #eaded2;border-radius:17px;background:#fff;color:#30241d;text-align:left}.avatar{display:grid;place-items:center;width:47px;height:47px;flex:none;border-radius:15px;background:#fff0e2;color:#b85111;font-size:25px}.identity{flex:1;min-width:0}.identity strong,.identity small{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.identity strong{font-size:16px}.identity small{margin-top:5px;color:#6f6054;font-size:12px}.arrow{color:#a74b12;font-size:22px}.primary,.secondary,.add{min-height:49px;padding:10px 17px;border-radius:13px;font-size:15px;font-weight:750}.primary{border:0;background:#b85111;color:#fff}.secondary{border:1px solid #d3ae8d;background:#fff;color:#8d410f}.add{width:100%;border:1px dashed #cf9a72;background:#fff9f3;color:#9b420c}.links{display:grid;grid-template-columns:1fr 1fr;gap:10px}.links button,.text-button{min-height:44px;border:0;background:transparent;color:#9c430f;font-size:13px;font-weight:750;text-align:left}.links button:last-child{text-align:right}button:focus-visible{outline:3px solid #783307;outline-offset:2px}@media(max-width:350px){.state{padding:19px}.links{gap:5px}.links button{font-size:12px}}
</style>

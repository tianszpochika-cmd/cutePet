<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api, ApiClientError } from '@cutepet/api-client';
import { JOIN_SCOPE_COPY } from '../../domain/family';

interface FamilySummary {
  familyId: number;
  name: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER';
  members: number;
  shares: number;
}

const router = useRouter();
const family = ref<FamilySummary | null>(null);
const loading = ref(true);
const noFamily = ref(false);
const error = ref('');
const familyName = ref('');
const creating = ref(false);

function familySummary(value: unknown): FamilySummary {
  if (typeof value !== 'object' || value === null) throw new Error('家庭信息暂时无法读取');
  const data = value as Record<string, unknown>;
  if (typeof data.familyId !== 'number' || typeof data.name !== 'string' ||
      !['OWNER', 'ADMIN', 'MEMBER'].includes(String(data.role)) ||
      typeof data.members !== 'number' || typeof data.shares !== 'number') {
    throw new Error('家庭信息暂时无法读取');
  }
  return data as unknown as FamilySummary;
}

async function loadFamily() {
  loading.value = true;
  error.value = '';
  noFamily.value = false;
  try {
    family.value = familySummary(await api.familyMine());
  } catch (cause) {
    family.value = null;
    if (cause instanceof ApiClientError && cause.status === 404) noFamily.value = true;
    else error.value = cause instanceof Error ? cause.message : '家庭信息加载失败';
  } finally {
    loading.value = false;
  }
}

async function createFamily() {
  const name = familyName.value.trim();
  if (!name) { error.value = '请填写家庭名称'; return; }
  if (name.length > 30) { error.value = '家庭名称请控制在 30 字以内'; return; }
  creating.value = true;
  error.value = '';
  try {
    await api.familyCreate({ body: { name } });
    await loadFamily();
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '创建失败，请保留填写内容后重试';
  } finally {
    creating.value = false;
  }
}

onMounted(loadFamily);
</script>

<template>
  <div class="family-page">
    <header class="page-head"><div><p class="eyebrow">一起照顾，也各有边界</p><h1>我的家庭</h1><p>先看清家庭关系，再决定要共享哪只宠物。</p></div><button v-if="family && family.role !== 'MEMBER'" type="button" class="primary" @click="router.push('/family/invite')">邀请家人</button></header>

    <p v-if="loading" class="state" role="status">正在读取家庭信息…</p>
    <section v-else-if="noFamily" class="empty">
      <span class="empty-mark" aria-hidden="true">♡</span><h2>你还没有加入家庭</h2>
      <p>可以创建自己的家庭，或使用家人发来的邀请码加入。加入后，你自己的宠物仍保持私有。</p>
      <form class="create" @submit.prevent="createFamily"><label for="family-name">创建家庭</label><div><input id="family-name" v-model="familyName" maxlength="30" placeholder="给家庭取个名字" /><button class="primary" type="submit" :disabled="creating">{{ creating ? '创建中…' : '创建家庭' }}</button></div></form>
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <router-link class="text-link" to="/family/join">已有邀请码？前往加入 →</router-link>
    </section>
    <section v-else-if="error" class="state error" role="alert"><h2>家庭信息暂时无法显示</h2><p>{{ error }}</p><button type="button" class="secondary" @click="loadFamily">重新加载</button></section>
    <template v-else-if="family">
      <section class="overview"><div><p class="eyebrow">当前家庭</p><h2>{{ family.name }}</h2><p>{{ family.role === 'OWNER' ? '你是家庭创建者' : family.role === 'ADMIN' ? '你是家庭管理员' : '你是家庭成员' }}</p></div><div class="counts"><div><strong>{{ family.members }}</strong><span>家庭成员</span></div><div><strong>{{ family.shares }}</strong><span>已共享宠物</span></div></div></section>
      <p class="privacy">{{ JOIN_SCOPE_COPY }}</p>
      <div class="section-grid"><section class="card"><h2>照护从宠物档案开始</h2><p>每只宠物的记录和待办分别保存。只有宠物所有者能决定共享范围，家庭管理员也不会自动看到他人的私有档案。</p><router-link class="text-link" to="/pets">查看我的宠物 →</router-link></section><section class="card"><h2>家人如何接力</h2><p>新成员对已共享宠物默认只读。需要共同记录或处理提醒时，由该宠物所有者单独授予可管理权限。</p><router-link class="text-link" to="/family/invite" v-if="family.role !== 'MEMBER'">查看邀请方式 →</router-link></section></div>
      <section class="detail-state"><h2>成员与逐宠权限</h2><p>目前只显示家庭成员及共享宠物的总数，尚无法在这里核对每位成员对每只宠物的授权。请勿依据家庭角色推断宠物资料权限。</p></section>
      <nav class="next-links" aria-label="家庭相关操作"><router-link v-if="family.role === 'OWNER'" to="/family/transfer-admin">了解管理员转交 <span aria-hidden="true">→</span></router-link><router-link to="/pets">从宠物档案查看所有权相关操作 <span aria-hidden="true">→</span></router-link></nav>
    </template>
  </div>
</template>

<style scoped>
.family-page{width:min(980px,calc(100% - 32px));margin:0 auto;padding:38px 0 90px;display:grid;gap:20px}.page-head{display:flex;justify-content:space-between;align-items:end;gap:20px}.eyebrow{margin:0 0 7px;color:#73539a;font-size:12px;font-weight:800;letter-spacing:.08em}.page-head h1{margin:0;font-size:clamp(28px,4vw,42px)}.page-head p:last-child{margin:6px 0 0;color:#6d6259}.primary,.secondary{min-height:46px;padding:10px 20px;border:0;border-radius:999px;font-weight:700;cursor:pointer}.primary{background:#5c3c8c;color:#fff}.primary:disabled{opacity:.55}.secondary{background:#fff;color:#5c3c8c;box-shadow:inset 0 0 0 1px #d9cce5}.state,.empty,.overview,.card,.detail-state{padding:26px;border:1px solid #ece2db;border-radius:20px;background:#fff}.state{color:#6d6259}.state h2,.empty h2,.card h2,.detail-state h2{margin:0 0 9px;font-size:19px}.state p,.empty p,.card p,.detail-state p{margin:0;color:#6d6259;font-size:14px;line-height:1.8}.form-error{color:#a43636!important}.error{border-color:#e9c5c5}.error .secondary{margin-top:20px}.empty{display:grid;justify-items:start;gap:12px}.empty-mark{display:grid;place-items:center;width:52px;height:52px;border-radius:17px;background:#f3eafb;color:#5c3c8c;font-size:31px}.create{display:grid;gap:9px;width:min(100%,570px);margin-top:11px}.create label{font-size:14px;font-weight:700}.create>div{display:flex;gap:10px}.create input{flex:1;min-width:0;min-height:46px;padding:0 14px;border:1px solid #ddd0e7;border-radius:12px;font-size:16px}.text-link{display:inline-flex;align-items:center;min-height:44px;color:#5c3c8c;font-weight:700;text-decoration:none}.overview{display:flex;justify-content:space-between;align-items:center;gap:24px;background:linear-gradient(120deg,#f7f0fb,#fff)}.overview h2{margin:0;font-size:28px}.overview p:last-child{margin:8px 0 0;color:#6d6259;font-size:14px}.counts{display:flex;gap:13px}.counts>div{display:grid;min-width:125px;padding:16px 20px;border-radius:15px;background:#fff;text-align:center}.counts strong{font-size:27px;color:#5c3c8c}.counts span{font-size:12px;color:#6d6259}.privacy{margin:0;padding:17px 20px;border-radius:16px;background:#f1eafb;color:#583f77;font-size:14px;line-height:1.75}.section-grid{display:grid;grid-template-columns:1fr 1fr;gap:17px}.card{display:flex;flex-direction:column}.card .text-link{margin-top:auto;padding-top:13px}.detail-state{background:#faf8f6}.next-links{display:flex;flex-wrap:wrap;gap:10px}.next-links a{display:inline-flex;align-items:center;min-height:46px;padding:10px 16px;border:1px solid #e7dbd0;border-radius:13px;background:#fff;color:#45382e;text-decoration:none;font-size:14px;font-weight:650}@media(max-width:650px){.family-page{padding-top:24px}.page-head,.overview{align-items:stretch;flex-direction:column}.page-head .primary{align-self:start}.counts>div{flex:1}.section-grid{grid-template-columns:1fr}.create>div{flex-direction:column}.next-links{display:grid}}
</style>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { DEFAULT_NEW_MEMBER_SHARE, JOIN_SCOPE_COPY } from '../../../web/src/domain/family.ts';

interface FamilySummary { familyId: number; name: string; ownerUserId: number; role: 'OWNER' | 'ADMIN' | 'MEMBER'; members: number; shares: number }
interface InviteReceipt { code: string; expiresAt: string; ttlDays?: number }
const route = useRoute();
const router = useRouter();
const mode = computed(() => route.path.endsWith('/invite') ? 'invite' : route.path.endsWith('/join') ? 'join' : 'home');
const state = ref<'guest' | 'loading' | 'none' | 'ready' | 'error'>(getAccessToken() ? 'loading' : 'guest');
const family = ref<FamilySummary | null>(null);
const invite = ref<InviteReceipt | null>(null);
const familyName = ref('');
const joinCode = ref('');
const joinAcknowledged = ref(false);
const busy = ref(false);
const error = ref('');
const feedback = ref('');
const canInvite = computed(() => family.value?.role === 'OWNER' || family.value?.role === 'ADMIN');

async function loadFamily() {
  if (!getAccessToken()) { state.value = 'guest'; family.value = null; return; }
  state.value = 'loading'; error.value = '';
  try {
    const result = await api.familyMine() as FamilySummary;
    if (!result || typeof result.familyId !== 'number' || typeof result.name !== 'string' || !['OWNER', 'ADMIN', 'MEMBER'].includes(result.role) || typeof result.members !== 'number' || typeof result.shares !== 'number') {
      throw new Error('家庭资料格式不完整');
    }
    family.value = result;
    state.value = 'ready';
  } catch (cause) {
    family.value = null;
    if (cause && typeof cause === 'object' && 'status' in cause && cause.status === 404 && cause instanceof Error && cause.message.includes('未加入任何家庭')) {
      state.value = 'none';
      return;
    }
    state.value = 'error';
    error.value = cause instanceof Error ? cause.message : '家庭资料暂时无法读取';
  }
}
function resetFeedback() { error.value = ''; feedback.value = ''; }
async function createFamily() {
  resetFeedback();
  const name = familyName.value.trim();
  if (!name || name.length > 40) { error.value = '请输入 1–40 字的家庭名称。'; return; }
  if (state.value !== 'none' || busy.value) return;
  busy.value = true;
  try {
    const result = await api.familyCreate({ body: { name } }) as { familyId?: number };
    if (typeof result?.familyId !== 'number') throw new Error('创建回执格式不完整，请核对家庭状态');
    await loadFamily();
    feedback.value = family.value?.familyId === result.familyId
      ? '平台已确认新家庭，个人宠物仍保持私有。'
      : '创建请求已返回，但当前家庭编号尚未与回执对上，请重新读取家庭状态。';
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '创建家庭失败'; }
  finally { busy.value = false; }
}
async function issueInvite() {
  resetFeedback(); invite.value = null;
  if (!canInvite.value || busy.value) return;
  busy.value = true;
  try {
    const result = await api.familyInviteCreate() as InviteReceipt;
    if (!result || typeof result.code !== 'string' || !result.code || typeof result.expiresAt !== 'string') {
      throw new Error('邀请回执格式不完整，无法展示邀请码');
    }
    invite.value = result;
    feedback.value = '邀请码已由平台生成。请只发给你信任的家人；加入不自动共享个人宠物。';
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '邀请码暂时无法生成'; }
  finally { busy.value = false; }
}
async function joinFamily() {
  resetFeedback();
  if (state.value !== 'none' || busy.value) return;
  const code = joinCode.value.trim();
  if (!code) { error.value = '请输入邀请码。'; return; }
  if (!joinAcknowledged.value) { error.value = '请先确认加入后的资料范围。'; return; }
  busy.value = true;
  try {
    const result = await api.familyJoin({ body: { code } }) as { familyId?: number; ownPetsShared?: boolean };
    if (typeof result?.familyId !== 'number') throw new Error('加入回执格式不完整，请核对家庭状态');
    joinCode.value = '';
    await loadFamily();
    feedback.value = family.value?.familyId === result.familyId && result.ownPetsShared === false
      ? '平台已确认加入家庭。你的个人宠物没有因此自动共享。'
      : '加入请求已返回，家庭编号或个人宠物的共享状态尚未全部复核，请刷新后确认。';
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '加入家庭失败'; }
  finally { busy.value = false; }
}
watch(mode, () => { invite.value = null; joinCode.value = ''; joinAcknowledged.value = false; resetFeedback(); });
onMounted(() => void loadFamily());
</script>

<template>
  <div class="family-page">
    <header class="page-head">
      <button type="button" class="back" @click="router.push(mode === 'home' ? '/me' : '/family')" aria-label="返回">←</button>
      <div><p class="eyebrow">CARE TOGETHER</p><h1>{{ mode === 'invite' ? '邀请家人' : mode === 'join' ? '加入家庭' : '我的家庭' }}</h1></div>
    </header>
    <div class="scope"><span aria-hidden="true">♡</span><p>{{ JOIN_SCOPE_COPY }}</p></div>
    <div v-if="state === 'guest'" class="state-card" role="status"><h2>先核验账号</h2><p>家庭关系与宠物共享属于私人资料。登录后才能读取本人家庭或使用邀请码。</p><router-link :to="{ path: '/login', query: { return: route.fullPath } }">前往账号入口 →</router-link></div>
    <div v-else-if="state === 'loading'" class="state-card" role="status"><h2>正在读取家庭资料…</h2><p>读取完成前不会展示成员或共享信息。</p></div>
    <div v-else-if="state === 'error'" class="state-card" role="alert"><h2>家庭状态暂不可用</h2><p>{{ error }}</p><button type="button" class="outline" @click="loadFamily">重试读取</button></div>

    <template v-else>
      <section v-if="state === 'ready' && family" class="family-card" aria-label="本人家庭概况">
        <div class="family-icon" aria-hidden="true">⌂</div><div><span class="eyebrow">已核验的家庭</span><h2>{{ family.name }}</h2><p>家庭 #{{ family.familyId }} · {{ family.role === 'OWNER' ? '创建者' : family.role === 'ADMIN' ? '管理员' : '成员' }}</p></div>
        <div class="stats"><span><strong>{{ family.members }}</strong> 家庭成员</span><span><strong>{{ family.shares }}</strong> 共享关系</span></div>
      </section>

      <section v-if="mode === 'home'" class="panel">
        <template v-if="state === 'none'"><h2>还没有加入家庭</h2><p>可以创建家庭，也可以用家人提供的邀请码加入。每个人同时只能属于一个家庭。</p><label class="field">家庭名称<input v-model="familyName" type="text" maxlength="40" placeholder="给共同照护取个名字" /></label><button type="button" class="primary" :disabled="busy" @click="createFamily">{{ busy ? '正在创建…' : '创建家庭' }}</button><router-link class="text-link" to="/family/join">已有邀请码？加入家庭 →</router-link></template>
        <template v-else><h2>共同照护，按宠物授权</h2><p>当前接口只提供成员数与共享关系数，不能列出成员姓名或逐宠档位。家庭管理员也不会自动取得私人健康记录权限。</p><router-link v-if="canInvite" class="action-link" to="/family/invite">邀请家人 <span aria-hidden="true">→</span></router-link><p v-else class="muted">生成邀请仅对创建者和管理员开放。</p></template>
      </section>

      <section v-else-if="mode === 'invite'" class="panel">
        <template v-if="state === 'ready' && canInvite"><h2>发给你信任的人</h2><p>邀请码有有效期。生成后由平台返回真实邀请码；请在分享前确认对方身份。</p><button type="button" class="primary" :disabled="busy" @click="issueInvite">{{ busy ? '生成中…' : '生成邀请码' }}</button><div v-if="invite" class="receipt" role="status"><span>平台邀请码</span><strong>{{ invite.code }}</strong><small>有效期至 {{ invite.expiresAt.replace('T', ' ') }}</small></div></template>
        <template v-else><h2>当前不能生成邀请</h2><p>{{ state === 'none' ? '请先创建或加入家庭。' : '只有家庭创建者或管理员可以生成邀请；当前账号没有该权限。' }}</p><router-link class="text-link" to="/family">返回家庭概况 →</router-link></template>
      </section>

      <section v-else class="panel">
        <template v-if="state === 'none'"><h2>确认加入范围</h2><p>邀请码仅用于加入家庭，不会自动共享你的宠物、收藏或投稿。新成员默认只读；后续逐宠共享须所有者明确设置。</p><label class="field">邀请码<input v-model.trim="joinCode" type="text" autocomplete="off" spellcheck="false" placeholder="输入家人提供的邀请码" /></label><label class="ack"><input v-model="joinAcknowledged" type="checkbox" /><span>我理解加入后个人宠物仍保持私有</span></label><button type="button" class="primary" :disabled="busy" @click="joinFamily">{{ busy ? '正在核对…' : '确认加入家庭' }}</button></template>
        <template v-else><h2>当前已属于一个家庭</h2><p>一人同时只能加入一个家庭。这里不会自动退出当前家庭或覆盖现有共享关系。</p><router-link class="text-link" to="/family">查看家庭概况 →</router-link></template>
      </section>

      <p v-if="error" class="feedback error" role="alert">{{ error }}</p>
      <p v-if="feedback" class="feedback" role="status">{{ feedback }}</p>
      <p class="footnote">逐宠档位、成员移除与所有权转移需要读取对象级权限、当前版本和真实处理回执，当前入口尚未提供这些操作。默认共享档位：{{ DEFAULT_NEW_MEMBER_SHARE === 'READONLY' ? '只读' : '可管理' }}。</p>
    </template>
  </div>
</template>

<style scoped>
.family-page{width:min(100%,620px);margin:auto;padding:18px 16px calc(35px + env(safe-area-inset-bottom,0px));display:grid;gap:15px;color:#2b2118}.page-head{display:flex;align-items:center;gap:12px}.back{flex:none;width:44px;height:44px;border:1px solid #eadfd4;border-radius:13px;background:#fff;color:#b85111;font-size:20px}.eyebrow{margin:0 0 4px;color:#ad4a10;font-size:10px;font-weight:850;letter-spacing:.13em}.page-head h1{margin:0;font-size:23px}.scope{display:flex;gap:11px;align-items:start;padding:14px;border-radius:14px;background:#fff1e6;color:#764627}.scope>span{font-size:23px;line-height:1}.scope p{margin:0;font-size:12px;line-height:1.75}.state-card,.panel,.family-card{padding:21px;border:1px solid #eadfd4;border-radius:19px;background:#fff}.state-card h2,.panel h2,.family-card h2{margin:0;font-size:19px}.state-card p,.panel p,.family-card p{color:#695d53;font-size:13px;line-height:1.75}.state-card a,.text-link{display:inline-flex;align-items:center;min-height:44px;color:#a0440e;font-weight:750;font-size:13px;text-decoration:none}.family-card{display:grid;grid-template-columns:50px 1fr;gap:13px}.family-icon{width:48px;height:48px;display:grid;place-items:center;border-radius:14px;background:#fff0e2;color:#b85111;font-size:30px}.family-card p{margin:6px 0 0}.stats{grid-column:1/-1;display:grid;grid-template-columns:1fr 1fr;gap:9px}.stats span{padding:12px;border-radius:10px;background:#faf7f3;color:#706257;font-size:12px}.stats strong{color:#2b2118;font-size:18px}.panel{display:grid;gap:12px}.panel p{margin:0}.field{display:grid;gap:7px;font-size:13px;font-weight:750}.field input{width:100%;min-height:48px;padding:0 13px;border:1px solid #d9cbbf;border-radius:11px;background:#fff;font-size:16px}.primary,.outline,.action-link{min-height:48px;display:flex;align-items:center;justify-content:center;padding:0 16px;border-radius:11px;font-size:14px;font-weight:750}.primary,.action-link{border:0;background:#b85111;color:#fff}.primary:disabled{opacity:.55}.outline{border:1px solid #b85111;background:#fff;color:#a0440e}.action-link{text-decoration:none;justify-content:space-between}.ack{min-height:44px;display:flex;align-items:center;gap:8px;color:#5e5147;font-size:12px}.ack input{width:18px;height:18px;accent-color:#b85111}.receipt{display:grid;gap:5px;padding:14px;border:1px solid #e5c6a7;border-radius:11px;background:#fff8ee}.receipt span,.receipt small{color:#796557;font-size:11px}.receipt strong{font-size:18px;overflow-wrap:anywhere;user-select:all}.feedback{margin:0;padding:11px 13px;border-radius:11px;background:#f0f7ee;color:#356342;font-size:12px;line-height:1.6}.feedback.error{background:#fff0ed;color:#9e3028}.muted,.footnote{margin:0;color:#786a5e;font-size:11px;line-height:1.7}
</style>

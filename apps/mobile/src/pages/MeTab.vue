<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api, getAccessToken, setAccessToken } from '@cutepet/api-client';
import { ME_GALLERY } from '../../../web/src/domain/family.ts';

interface Profile { id: number; nickname?: string | null; phone?: string | null; status?: string }
const router = useRouter();
const profile = ref<Profile | null>(null);
const state = ref<'guest' | 'loading' | 'ready' | 'error'>(getAccessToken() ? 'loading' : 'guest');
const error = ref('');
const notice = ref('');
const iconPaths: Record<(typeof ME_GALLERY)[number]['id'], string[]> = {
  pets: ['M8.2 13.5c-1.9 1.7-1 5 1.3 5 1.1 0 1.6-.6 2.5-.6s1.4.6 2.5.6c2.3 0 3.2-3.3 1.3-5-1.1-1-2.3-1.7-3.8-1.7s-2.7.7-3.8 1.7Z', 'M5 8.8c.8-.2 1.6.6 1.8 1.7.2 1.1-.2 2.1-1 2.3-.9.2-1.7-.5-1.9-1.7-.2-1.1.2-2.1 1.1-2.3ZM9 5.5c.9-.2 1.7.6 1.9 1.7.2 1.2-.3 2.3-1.2 2.5-.9.1-1.7-.7-1.8-1.9-.2-1.1.3-2.1 1.1-2.3ZM15 5.5c.8.2 1.3 1.2 1.1 2.3-.1 1.2-.9 2-1.8 1.9-.9-.2-1.4-1.3-1.2-2.5.2-1.1 1-1.9 1.9-1.7ZM19 8.8c.9.2 1.3 1.2 1.1 2.3-.2 1.2-1 1.9-1.9 1.7-.8-.2-1.2-1.2-1-2.3.2-1.1 1-1.9 1.8-1.7Z'],
  family: ['M3 10.5 12 3l9 7.5V21H3V10.5Z', 'M9 21v-7h6v7'],
  creation: ['m4 17 11-11 3 3L7 20H4v-3Z', 'm13.5 7.5 3 3', 'M12 20h8'],
  favorites: ['M20.5 8.5c0 4.2-5.5 8.5-8.5 11-3-2.5-8.5-6.8-8.5-11a4.5 4.5 0 0 1 8.5-2 4.5 4.5 0 0 1 8.5 2Z'],
  messages: ['M3 5h18v14H3V5Z', 'm3 7 9 7 9-7'],
  activity: ['M4 6h16v15H4V6Z', 'M8 3v6M16 3v6M4 11h16', 'm12 14 .8 1.8 2 .2-1.5 1.4.4 2-1.7-1-1.7 1 .4-2-1.5-1.4 2-.2L12 14Z'],
};
const gallery = ME_GALLERY.map((item) => ({ ...item, to: item.id === 'creation' ? '/write' : item.to }));

function phoneLabel(phone?: string | null): string {
  return phone && /^1[3-9][0-9]{9}$/.test(phone) ? `${phone.slice(0, 3)}****${phone.slice(-4)}` : '手机号未展示';
}
async function loadProfile() {
  if (!getAccessToken()) { state.value = 'guest'; profile.value = null; return; }
  state.value = 'loading'; error.value = '';
  try {
    const result = await api.meGet() as Profile;
    if (!result || typeof result.id !== 'number') throw new Error('个人资料格式不完整');
    profile.value = result;
    state.value = 'ready';
  } catch (cause) {
    profile.value = null;
    if (cause && typeof cause === 'object' && 'status' in cause && (cause.status === 401 || cause.status === 403)) {
      setAccessToken(null);
      state.value = 'guest';
      return;
    }
    state.value = 'error';
    error.value = cause instanceof Error ? cause.message : '账号状态暂时无法核验';
  }
}
function open(to: string, requiresAccount = true) {
  notice.value = '';
  if (!requiresAccount || state.value === 'ready') { void router.push(to); return; }
  if (state.value === 'error' || state.value === 'loading') {
    notice.value = '请先重试账号核验，再打开个人资料。';
    return;
  }
  void router.push({ path: '/login', query: { return: to } });
}
onMounted(() => void loadProfile());
</script>

<template>
  <div class="me-page">
    <header class="heading"><p class="eyebrow">MY CUTE PET</p><h1>我的</h1></header>
    <section class="profile" aria-label="账号状态">
      <div class="avatar" aria-hidden="true">✿</div>
      <div class="profile-copy">
        <strong v-if="state === 'ready'">{{ profile?.nickname || '我的账号' }}</strong>
        <strong v-else-if="state === 'loading'">正在核验账号…</strong>
        <strong v-else-if="state === 'error'">账号暂时无法核验</strong>
        <strong v-else>先认识 cutePet</strong>
        <p v-if="state === 'ready'">{{ phoneLabel(profile?.phone) }} · 资料来自当前账号</p>
        <p v-else-if="state === 'error'">{{ error }}</p>
        <p v-else-if="state === 'guest'">公开内容可浏览，个人资料需登录</p>
      </div>
      <button v-if="state === 'error'" type="button" class="outline" @click="loadProfile">重试</button>
      <button v-else-if="state === 'guest'" type="button" class="primary" @click="open('/me', true)">账号入口</button>
    </section>

    <div class="section-title"><h2>常用入口</h2><span>私人内容需要账号核验</span></div>
    <nav class="gallery" aria-label="我的常用入口" data-testid="gallery">
      <button v-for="item in gallery" :key="item.id" type="button" @click="open(item.to)">
        <span class="icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path v-for="path in iconPaths[item.id]" :key="path" :d="path" /></svg></span><span>{{ item.label }}</span>
      </button>
    </nav>
    <p v-if="notice" class="notice" role="status">{{ notice }}</p>
    <p class="hint">消息未读数量须由可核验的本人消息接口返回，当前不显示角标。</p>

    <section class="menu" aria-label="设置与账号">
      <button type="button" @click="open('/settings', false)"><span>通知与设置</span><span aria-hidden="true">›</span></button>
      <button type="button" @click="open('/me/sync')"><span>离线草稿与同步</span><span aria-hidden="true">›</span></button>
      <button type="button" @click="open('/settings/delete')"><span>注销账号</span><span aria-hidden="true">›</span></button>
    </section>
    <p class="footnote">受限账号申诉与资料导出需要独立核验入口；当前尚未接通，不会跳转到普通登录页冒充处理。</p>
  </div>
</template>

<style scoped>
.me-page{width:min(100%,600px);margin:auto;padding:23px 16px 30px;display:grid;gap:16px;color:#2b2118}.heading h1{margin:2px 0 0;font-size:27px;letter-spacing:-.04em}.eyebrow{margin:0;color:#ad4a10;font-size:11px;font-weight:850;letter-spacing:.14em}.profile{min-height:116px;display:flex;align-items:center;gap:13px;padding:17px;border-radius:21px;background:linear-gradient(125deg,#ffe7d0,#fff8f0 70%,#fff);border:1px solid #f2d8c0}.avatar{flex:none;width:54px;height:54px;display:grid;place-items:center;border-radius:17px;background:#fff;color:#b85111;font-size:33px}.profile-copy{min-width:0;flex:1}.profile-copy strong{display:block;font-size:17px}.profile-copy p{margin:5px 0 0;color:#695c50;font-size:12px;line-height:1.5;overflow-wrap:anywhere}.primary,.outline{flex:none;min-height:44px;padding:0 13px;border-radius:11px;font-weight:750;font-size:12px}.primary{border:0;background:#b85111;color:#fff}.outline{border:1px solid #b85111;background:#fff;color:#a0420e}.section-title{display:flex;justify-content:space-between;align-items:baseline;gap:8px}.section-title h2{margin:0;font-size:17px}.section-title span{font-size:11px;color:#807267}.gallery{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:9px}.gallery button{min-width:0;min-height:82px;display:grid;justify-items:center;align-content:center;gap:6px;border:1px solid #eadfd4;border-radius:17px;background:#fff;color:#3c3129;font-size:12px;font-weight:700}.icon{width:33px;height:33px;display:grid;place-items:center;border-radius:10px;background:#fff0e2;color:#b85111;font-size:22px}.hint,.footnote{margin:0;color:#796b60;font-size:11px;line-height:1.7}.notice{margin:0;padding:11px 13px;border-radius:10px;background:#fff1e8;color:#8d3d0b;font-size:12px}.menu{display:grid;gap:8px}.menu button{min-height:51px;display:flex;align-items:center;justify-content:space-between;gap:10px;padding:0 15px;border:1px solid #eadfd4;border-radius:13px;background:#fff;color:#2b2118;text-align:left;font-size:14px;font-weight:700}.menu button span:last-child{color:#b85111;font-size:22px}.footnote{padding-bottom:12px}@media(max-width:360px){.profile{flex-wrap:wrap}.profile .primary,.profile .outline{margin-left:67px}.section-title span{max-width:130px;text-align:right}}
.me-page{padding-bottom:calc(30px + env(safe-area-inset-bottom,0px))}
.icon svg{width:22px;height:22px}
</style>

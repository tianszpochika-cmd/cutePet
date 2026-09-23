<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getAccessToken } from '@cutepet/api-client';

const router = useRouter();
const supported = ref(false);
const secure = ref(false);
const permission = ref<NotificationPermission | 'unsupported'>('unsupported');
const accountAvailable = ref(Boolean(getAccessToken()));
const permissionLabel = computed(() => permission.value === 'granted' ? '浏览器已授权' : permission.value === 'denied' ? '浏览器已拒绝' : permission.value === 'default' ? '尚未选择' : '此环境不支持');
const permissionHelp = computed(() => {
  if (!supported.value) return '当前浏览器不支持网页通知，仍可在登录后查看站内消息。';
  if (!secure.value) return '网页通知需要安全连接。当前页面不会请求系统权限。';
  if (permission.value === 'granted') return '浏览器允许网页通知，不等于平台离站推送已接通或消息已送达。';
  if (permission.value === 'denied') return '可在浏览器的网站权限设置中更改；页面无法自行重置拒绝状态。';
  return '正式隐私同意记录与平台通知订阅未接入，暂不发起权限请求。';
});
function refreshPermission() {
  supported.value = typeof window !== 'undefined' && 'Notification' in window;
  secure.value = typeof window !== 'undefined' && window.isSecureContext;
  permission.value = supported.value ? Notification.permission : 'unsupported';
  accountAvailable.value = Boolean(getAccessToken());
}
onMounted(() => { refreshPermission(); window.addEventListener('focus', refreshPermission); });
onUnmounted(() => window.removeEventListener('focus', refreshPermission));
</script>

<template>
  <div class="notify-page">
    <header class="page-head"><button type="button" class="back" aria-label="返回我的" @click="router.push('/me')">←</button><div><p class="eyebrow">PREFERENCES</p><h1>通知与设置</h1></div></header>
    <section class="intro"><span aria-hidden="true">✉</span><div><strong>提醒先看真实状态</strong><p>浏览器授权、站内消息与厂商推送是不同通道。这里不模拟授权，也不把发送尝试说成送达。</p></div></section>

    <section class="channel" aria-labelledby="web-title">
      <div class="top"><span class="icon" aria-hidden="true">◉</span><div><h2 id="web-title">浏览器通知</h2><p>当前设备与浏览器</p></div><span class="state" :class="permission">{{ permissionLabel }}</span></div>
      <p class="body">{{ permissionHelp }}</p>
      <button type="button" class="outline" @click="refreshPermission">刷新权限状态</button>
    </section>

    <section class="channel" aria-labelledby="inapp-title">
      <div class="top"><span class="icon" aria-hidden="true">✦</span><div><h2 id="inapp-title">站内消息</h2><p>提醒、互动、审核结果、家庭、系统</p></div><span class="state waiting">待核验</span></div>
      <p class="body">{{ accountAvailable ? '当前设备有访问令牌，但消息服务还缺少可核验的本人身份传递。' : '登录后可查看本人消息；当前没有可核验的收件箱。' }}未读数与已读状态只以服务端返回为准。</p>
      <router-link class="text-link" to="/messages">查看消息入口 →</router-link>
    </section>

    <section class="channel quiet" aria-labelledby="vendor-title">
      <div class="top"><span class="icon" aria-hidden="true">⌁</span><div><h2 id="vendor-title">厂商推送</h2><p>App 设备通道</p></div><span class="state waiting">未接入</span></div>
      <p class="body">移动 H5 没有原生厂商推送通道。是否支持离站触达，需要 App 通道及实际投递回执确认。</p>
    </section>
    <p class="footnote">隐私选择、免打扰时段和平台推送订阅需要可保存、可撤回的服务端设置接口；当前页面不保存本地开关冒充账号偏好。</p>
  </div>
</template>

<style scoped>
.notify-page{width:min(100%,620px);margin:auto;padding:18px 16px 35px;display:grid;gap:13px;color:#2b2118}.page-head{display:flex;align-items:center;gap:12px}.back{width:44px;height:44px;flex:none;border:1px solid #eadfd4;border-radius:12px;background:#fff;color:#b85111;font-size:20px}.eyebrow{margin:0 0 3px;color:#a84710;font-size:10px;font-weight:850;letter-spacing:.13em}.page-head h1{margin:0;font-size:23px}.intro{display:flex;gap:12px;align-items:start;padding:16px;border-radius:16px;background:#fff1e7;color:#5e4332}.intro>span{font-size:27px;color:#b85111}.intro strong{font-size:14px}.intro p{margin:5px 0 0;font-size:12px;line-height:1.7}.channel{display:grid;gap:14px;padding:18px;border:1px solid #eadfd4;border-radius:17px;background:#fff}.channel.quiet{background:#fbf9f6}.top{display:flex;align-items:center;gap:10px;min-width:0}.icon{width:39px;height:39px;flex:none;display:grid;place-items:center;border-radius:11px;background:#fff0e2;color:#b85111;font-size:23px}.top>div{min-width:0;flex:1}.top h2{margin:0;font-size:15px}.top p{margin:3px 0 0;color:#796b60;font-size:11px}.state{padding:5px 8px;border-radius:99px;background:#fff1e7;color:#92501e;font-size:10px;font-weight:800;white-space:nowrap}.state.granted{background:#e8f4eb;color:#286e42}.state.denied{background:#fff0ed;color:#a73830}.state.waiting,.state.unsupported{background:#f0ece8;color:#76685e}.body{margin:0;color:#695d53;font-size:12px;line-height:1.75}.outline{min-height:44px;justify-self:start;padding:0 14px;border:1px solid #b85111;border-radius:10px;background:#fff;color:#9e420e;font-size:12px;font-weight:750}.text-link{min-height:44px;display:inline-flex;align-items:center;justify-self:start;color:#a0440e;text-decoration:none;font-size:13px;font-weight:750}.footnote{margin:0;color:#77695e;font-size:11px;line-height:1.7}@media(max-width:360px){.top{flex-wrap:wrap}.state{margin-left:49px}}
.notify-page{padding-bottom:calc(35px + env(safe-area-inset-bottom,0px))}
</style>

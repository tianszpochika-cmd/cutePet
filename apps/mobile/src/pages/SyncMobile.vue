<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getAccessToken } from '@cutepet/api-client';

const router = useRouter();
const online = ref(true);
const hasToken = ref(Boolean(getAccessToken()));
function refreshConnection() {
  online.value = navigator.onLine;
  hasToken.value = Boolean(getAccessToken());
}
onMounted(() => {
  refreshConnection();
  window.addEventListener('online', refreshConnection);
  window.addEventListener('offline', refreshConnection);
});
onUnmounted(() => {
  window.removeEventListener('online', refreshConnection);
  window.removeEventListener('offline', refreshConnection);
});
</script>

<template>
  <div class="sync-page">
    <header class="page-head"><button type="button" class="back" aria-label="返回我的" @click="router.push('/me')">←</button><div><p class="eyebrow">OFFLINE &amp; SYNC</p><h1>离线与同步</h1></div></header>
    <section class="network" :class="{ offline: !online }" role="status"><span aria-hidden="true">{{ online ? '◉' : '○' }}</span><div><strong>{{ online ? '设备报告网络在线' : '设备报告网络离线' }}</strong><p>此状态来自浏览器，不代表平台接口连通或草稿已上传。</p></div></section>
    <section class="card" aria-labelledby="queue-title"><span class="symbol" aria-hidden="true">↗</span><h2 id="queue-title">待同步队列尚未接通</h2><p>当前没有可读取的本机草稿清单、逐条服务端回执与冲突版本。页面不能判断待同步数量，也不会展示演示记录或把重试标成“已同步”。</p><p v-if="!hasToken" class="subtle">当前未核验账号，私人记录不可与平台账号关联。</p><router-link class="link" to="/pets">回到宠物档案 →</router-link></section>
    <section class="steps" aria-labelledby="steps-title"><h2 id="steps-title">真实同步需要逐条核对</h2><ol><li>确认账号、宠物和当前共享权限仍有效。</li><li>核对本机草稿编号与服务端版本，冲突时由本人选择。</li><li>逐条展示上传、失败或失权结果；只有平台确认后才显示完成。</li></ol></section>
    <p class="footnote">登出或换号前，应先显示可核验的待同步数量与草稿去向。当前没有这个清单，因此本页不提供清除、覆盖或假重试按钮。</p>
  </div>
</template>

<style scoped>
.sync-page{width:min(100%,620px);margin:auto;padding:18px 16px calc(35px + env(safe-area-inset-bottom,0px));display:grid;gap:14px;color:#2b2118}.page-head{display:flex;align-items:center;gap:12px}.back{width:44px;height:44px;flex:none;border:1px solid #eadfd4;border-radius:12px;background:#fff;color:#b85111;font-size:20px}.eyebrow{margin:0 0 4px;color:#a84710;font-size:10px;font-weight:850;letter-spacing:.13em}.page-head h1{margin:0;font-size:23px}.network{display:flex;gap:11px;align-items:start;padding:14px;border-radius:13px;background:#eff7f0;color:#356744}.network.offline{background:#fff1e7;color:#92501e}.network>span{font-size:22px;line-height:1}.network strong{font-size:13px}.network p{margin:4px 0 0;font-size:11px;line-height:1.7}.card,.steps{padding:21px;border:1px solid #eadfd4;border-radius:19px;background:#fff}.symbol{width:47px;height:47px;display:grid;place-items:center;border-radius:13px;background:#fff0e2;color:#b85111;font-size:27px}.card h2{margin:17px 0 8px;font-size:19px}.card p,.steps li,.footnote{color:#6b5e53;font-size:12px;line-height:1.75}.card p{margin:0 0 10px}.subtle{padding:10px 12px;border-radius:9px;background:#faf7f3}.link{min-height:44px;display:inline-flex;align-items:center;color:#a1430e;font-size:13px;font-weight:750;text-decoration:none}.steps h2{margin:0;font-size:15px}.steps ol{margin:12px 0 0;padding-left:21px}.steps li{margin:5px 0}.footnote{margin:0}
</style>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { publicEntryUrl } from '../domain/site';

type Entry = 'web' | 'mobile';
const route = useRoute();
const router = useRouter();
const active = ref<Entry>(route.query.end === 'mobile' ? 'mobile' : 'web');
const env = (import.meta as ImportMeta & { env: Record<string, string | undefined> }).env;
const webUrl = env.VITE_PUBLIC_WEB_VERIFIED === 'true' ? publicEntryUrl(env.VITE_PUBLIC_WEB_URL) : null;
const mobileUrl = env.VITE_PUBLIC_MOBILE_VERIFIED === 'true' ? publicEntryUrl(env.VITE_PUBLIC_MOBILE_URL) : null;
const currentUrl = computed(() => active.value === 'web' ? webUrl : mobileUrl);
const availabilityCopy = computed(() => {
  if (webUrl && mobileUrl) return 'Web 与移动 H5 的公开入口已配置。你可以按设备选择使用方式；目前没有安装包或应用商店下载。';
  if (webUrl) return 'Web 的公开入口已配置，移动 H5 的入口仍待核验；目前没有安装包或应用商店下载。';
  if (mobileUrl) return '移动 H5 的公开入口已配置，Web 的入口仍待核验；目前没有安装包或应用商店下载。';
  return 'Web 与移动 H5 的公开入口仍待核验。你可以先了解产品能力；目前没有安装包或应用商店下载。';
});
watch(() => route.query.end, (end) => { active.value = end === 'mobile' ? 'mobile' : 'web'; });
function selectEntry(entry: Entry) {
  active.value = entry;
  void router.replace({ query: { ...route.query, end: entry } });
}
</script>

<template>
  <div class="entry-page site-wrap">
    <div class="intro"><p class="site-eyebrow">START WITH CUTE PET</p><h1>选一个顺手的方式，<br />开始照顾它。</h1><p class="site-lead">{{ availabilityCopy }}</p></div>

    <div class="entry-choices" role="group" aria-label="选择使用设备">
      <button type="button" :aria-pressed="active === 'web'" @click="selectEntry('web')"><span class="choice-icon" aria-hidden="true">▣</span><strong>在电脑上使用</strong><small>档案、记录、家庭与内容</small></button>
      <button type="button" :aria-pressed="active === 'mobile'" @click="selectEntry('mobile')"><span class="choice-icon" aria-hidden="true">▯</span><strong>在手机上使用</strong><small>移动 H5，出门时随手查看</small></button>
    </div>

    <section class="entry-panel" aria-live="polite">
      <div class="panel-copy"><span class="status" :class="{ ready: currentUrl }">{{ currentUrl ? '已配置公开入口' : '公开入口准备中' }}</span><h2>{{ active === 'web' ? '用户端 Web' : '移动 H5' }}</h2><p>{{ active === 'web' ? '查看宠物档案、照护时间线和家庭协作，也可以阅读资讯与探索场所。' : '在手机浏览器中查看本次待办、记录健康变化与确认家人的处理状态。' }}</p>
        <a v-if="currentUrl" class="site-button" :href="currentUrl" target="_blank" rel="noopener noreferrer">打开{{ active === 'web' ? '用户端 Web' : '移动 H5' }} <span aria-hidden="true">↗</span></a>
        <div v-else class="pending"><strong>目前没有可公开跳转的地址</strong><span>地址、登录和运行状态须完成核验后配置；这里不会将访客带到本机或无效页面。</span><router-link class="site-text-link" to="/products">先了解产品能力 →</router-link></div>
      </div>
      <div class="panel-art" aria-hidden="true"><div class="art-circle"><span>{{ active === 'web' ? '▣' : '▯' }}</span></div><div class="art-card"><i></i><i></i><i></i></div></div>
    </section>

    <div class="entry-help"><div><h2>还有疑问？</h2><p>账号、家庭共享与权限规则可以先看帮助和产品说明。</p></div><router-link class="site-text-link" to="/help">前往帮助中心 →</router-link></div>
    <p class="entry-note">登录后的建档、待办处理和活动报名仍需在用户端核对并确认；官网不会替你提交这些操作。</p>
  </div>
</template>

<style scoped>
.entry-page{padding-block:68px 95px}.intro{text-align:center}.intro h1{margin:0;font-size:clamp(36px,4.6vw,56px);line-height:1.18;letter-spacing:-.06em}.intro .site-lead{margin:16px auto 0}
.entry-choices{display:grid;grid-template-columns:1fr 1fr;gap:16px;max-width:760px;margin:42px auto 25px}.entry-choices button{display:grid;justify-items:start;gap:4px;min-height:134px;padding:22px;border:1.5px solid var(--site-line);border-radius:21px;background:#fff;color:var(--site-ink);text-align:left}.entry-choices button[aria-pressed=true]{border-color:#ca885c;background:var(--site-soft)}.choice-icon{color:var(--site-action);font-size:26px}.entry-choices strong{font-size:17px}.entry-choices small{color:var(--site-muted);font-size:12px}
.entry-panel{display:grid;grid-template-columns:1.15fr .85fr;gap:35px;align-items:center;max-width:930px;min-height:345px;margin:auto;padding:40px;border:1px solid var(--site-line);border-radius:28px;background:#fff}.status{display:inline-flex;padding:5px 11px;border-radius:999px;background:#fff1e8;color:var(--site-action);font-size:11px;font-weight:800}.status.ready{background:#eaf7ef;color:#236646}.panel-copy h2{margin:16px 0 6px;font-size:27px}.panel-copy>p{max-width:470px;margin:0 0 23px;color:var(--site-muted);font-size:14px}.pending{display:grid;gap:5px;padding:16px;border-radius:15px;background:var(--site-bg)}.pending strong{font-size:14px}.pending span{color:var(--site-muted);font-size:12px}.pending .site-text-link{font-size:13px}.panel-art{position:relative;display:grid;place-items:center;min-height:250px;border-radius:22px;background:linear-gradient(140deg,#ffe0c4,#fff6e9)}.art-circle{display:grid;place-items:center;width:155px;height:155px;border:1px solid rgba(184,81,17,.18);border-radius:50%;background:#fff4e8;color:var(--site-action);font-size:82px}.art-card{position:absolute;right:25px;bottom:23px;display:grid;gap:8px;width:115px;padding:15px;border-radius:13px;background:#fff;box-shadow:0 12px 27px rgba(69,41,19,.13)}.art-card i{height:6px;border-radius:6px;background:#f6d9c1}.art-card i:first-child{width:56%;background:#ca885c}.art-card i:last-child{width:70%}
.entry-help{display:flex;align-items:center;justify-content:space-between;gap:20px;max-width:930px;margin:35px auto 0;padding:20px 4px;border-top:1px solid var(--site-line)}.entry-help h2{margin:0;font-size:18px}.entry-help p{margin:3px 0 0;color:var(--site-muted);font-size:13px}.entry-note{max-width:930px;margin:0 auto;color:var(--site-muted);font-size:12px}
@media(max-width:760px){.entry-page{padding-block:50px 70px}.entry-panel{grid-template-columns:1fr;padding:23px;gap:15px}.panel-art{min-height:185px}.art-circle{width:110px;height:110px;font-size:60px}.entry-help{align-items:start;flex-direction:column}}
@media(max-width:480px){.entry-choices{grid-template-columns:1fr;margin-top:30px}.entry-choices button{min-height:116px}.entry-panel{padding:20px}}
</style>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <div class="privacy"><button type="button" class="back" @click="router.push('/settings')">‹ 设置</button><p class="eyebrow">PRIVACY CONTROLS</p><h1>隐私设置</h1><p class="lead">宠物档案的访问由所有者逐宠授权。可选分析、互动可见性及同意状态需要读取并写入服务端设置；当前尚未接通，页面不会用本地开关冒充已保存的偏好。</p><div class="cards"><section><h2>宠物档案</h2><p>家庭关系本身不会自动公开个人宠物。共享范围请到具体宠物查看；逐成员授权接口接通前暂不能在页面修改。</p><router-link to="/family">查看家庭与共享说明 →</router-link></section><section><h2>可选分析与定位</h2><p>没有明确同意或浏览器授权时，不应启动可选分析与位置能力。当前设置状态尚不可读取。</p><span>设置暂不可用</span></section><section><h2>你的资料权利</h2><p>资料导出需要独立身份核验；注销需核对家庭及活动依赖。相应入口会说明当前可用状态。</p><div class="links"><router-link to="/settings/export">数据导出</router-link><router-link to="/settings/delete">注销账号</router-link></div></section></div><router-link class="policy" to="/legal/privacy">隐私政策发布状态 →</router-link></div>
</template>

<style scoped>
.privacy { width: min(760px, calc(100% - 32px)); margin: 42px auto; }.back { min-height: 44px; padding: 0; border: 0; background: transparent; color: #b85111; font-weight: 700; }.eyebrow { margin: 28px 0 9px; color: #b85111; font-size: 12px; font-weight: 800; letter-spacing: .14em; }h1 { margin: 0; font-size: clamp(31px, 5vw, 43px); }.lead { max-width: 680px; color: #64574d; line-height: 1.8; }.cards { display: grid; gap: 12px; margin: 26px 0; }.cards section { padding: 20px 23px; border: 1px solid #eadfd4; border-radius: 17px; background: #fff; }.cards h2 { margin: 0; font-size: 18px; }.cards p { margin: 7px 0; color: #64574d; line-height: 1.75; }.cards span { color: #8a7162; font-size: 12px; }.cards a,.policy { display: inline-flex; align-items: center; min-height: 44px; color: #b85111; font-weight: 700; text-decoration: none; }.links { display: flex; flex-wrap: wrap; gap: 12px; }
</style>

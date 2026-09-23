<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const channels = [
  { title: '照护提醒', body: '疫苗、驱虫等安排以站内待办为准；外部推送渠道与送达状态尚未接通。' },
  { title: '互动消息', body: '评论、审核和家庭变更可在消息中心查看，当前不能保证设备推送。' },
  { title: '免打扰', body: '时段配置尚未与通知服务同步，因此这里暂不保存开关状态。' },
];
</script>

<template>
  <div class="notifications"><button type="button" class="back" @click="router.push('/settings')">‹ 设置</button><p class="eyebrow">NOTIFICATIONS</p><h1>通知设置</h1><p class="lead">提醒事项和通知送达是两种状态。完成渠道接入后，你可以分别管理提醒与互动通知。</p><div class="cards"><section v-for="item in channels" :key="item.title"><h2>{{ item.title }}</h2><p>{{ item.body }}</p><span>设置暂不可用</span></section></div><router-link class="link" to="/messages">查看站内消息 →</router-link></div>
</template>

<style scoped>
.notifications { width: min(760px, calc(100% - 32px)); margin: 42px auto; }.back { min-height: 44px; padding: 0; border: 0; background: transparent; color: #b85111; font-weight: 700; }.eyebrow { margin: 28px 0 9px; color: #b85111; font-size: 12px; font-weight: 800; letter-spacing: .14em; }h1 { margin: 0; font-size: clamp(31px, 5vw, 43px); }.lead { max-width: 630px; color: #64574d; line-height: 1.8; }.cards { display: grid; gap: 12px; margin: 26px 0; }.cards section { padding: 20px 23px; border: 1px solid #eadfd4; border-radius: 17px; background: #fff; }.cards h2 { margin: 0; font-size: 18px; }.cards p { margin: 7px 0; color: #64574d; line-height: 1.7; }.cards span { color: #8a7162; font-size: 12px; }.link { display: inline-flex; align-items: center; min-height: 44px; color: #b85111; font-weight: 700; text-decoration: none; }
</style>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { getAccessToken } from '@cutepet/api-client';
import { COOLDOWN_DAYS } from '../../../web/src/domain/settings.ts';

const router = useRouter();
const hasToken = ref(Boolean(getAccessToken()));
const dependencies = [
  { title: '共享中的宠物', detail: '所有者需先转移所有权或解除共享。', to: '/family' },
  { title: '家庭管理责任', detail: '管理员职责与成员关系需核对并完成交接。', to: '/family' },
  { title: '报名与已发布活动', detail: '进行中的事项需在平台处理并核对结果。', to: '/me/activity-center' },
];
</script>

<template>
  <div class="delete-page">
    <header class="page-head"><button type="button" class="back" aria-label="返回我的" @click="router.push('/me')">←</button><div><p class="eyebrow">ACCOUNT &amp; PRIVACY</p><h1>注销账号</h1></div></header>
    <section class="intro"><span aria-hidden="true">◇</span><div><strong>先核对依赖，再提交申请</strong><p>注销影响档案、共享与活动。当前缺少可核验的完整依赖清单和申请状态，页面不会代替平台提交或撤销。</p></div></section>

    <section v-if="!hasToken" class="card" role="status"><h2>先核验账号</h2><p>此页面不读取匿名访客的私人数据。完成正式登录后，才可检查本人注销条件。</p><router-link class="action-link" :to="{ path: '/login', query: { return: '/settings/delete' } }">前往账号入口 →</router-link></section>

    <section class="card" aria-labelledby="deps-title"><div class="section-head"><h2 id="deps-title">需要平台核对的事项</h2><span>数量未知</span></div><p>以下是核对范围，并非你的实际待处理项。当前不能据此判断“可以注销”。</p><ul class="deps"><li v-for="item in dependencies" :key="item.title"><div><strong>{{ item.title }}</strong><small>{{ item.detail }}</small></div><router-link :to="item.to" :aria-label="'查看' + item.title">查看 →</router-link></li></ul></section>

    <section class="card period"><h2>{{ COOLDOWN_DAYS }} 天冷静期 · 规则说明</h2><p>正式申请成功后应由平台返回申请时间、到期时间及状态。冷静期内撤销也必须单独提交并取得回执；再次登录不自动撤销。</p><p>已发布内容的匿名化与家庭关系变化须以正式规则和平台处理结果为准。</p></section>
    <button type="button" class="disabled-action" disabled data-testid="submit">注销申请待接入</button>
    <p class="footnote">当前没有提交申请，也没有生成冷静期。账号导出、依赖处理和撤销结果需要后续接入。</p>
  </div>
</template>

<style scoped>
.delete-page{width:min(100%,620px);margin:auto;padding:18px 16px calc(35px + env(safe-area-inset-bottom,0px));display:grid;gap:14px;color:#2b2118}.page-head{display:flex;align-items:center;gap:12px}.back{width:44px;height:44px;flex:none;border:1px solid #eadfd4;border-radius:12px;background:#fff;color:#b85111;font-size:20px}.eyebrow{margin:0 0 4px;color:#a84710;font-size:10px;font-weight:850;letter-spacing:.13em}.page-head h1{margin:0;font-size:23px}.intro{display:flex;gap:12px;align-items:start;padding:16px;border-radius:15px;background:#fff1e7;color:#65452e}.intro>span{color:#b85111;font-size:27px}.intro strong{font-size:14px}.intro p{margin:5px 0 0;font-size:12px;line-height:1.7}.card{padding:20px;border:1px solid #eadfd4;border-radius:17px;background:#fff}.card h2{margin:0;font-size:17px}.card p,.footnote{color:#6d6055;font-size:12px;line-height:1.75}.card p{margin:9px 0 0}.section-head{display:flex;justify-content:space-between;align-items:baseline;gap:9px}.section-head span{color:#8b7765;font-size:11px;white-space:nowrap}.deps{list-style:none;margin:15px 0 0;padding:0}.deps li{min-height:66px;display:flex;justify-content:space-between;align-items:center;gap:12px;padding:12px 0;border-top:1px solid #f0e8df}.deps strong,.deps small{display:block}.deps strong{font-size:13px}.deps small{margin-top:4px;color:#75685d;font-size:11px;line-height:1.5}.deps a,.action-link{min-height:44px;flex:none;display:inline-flex;align-items:center;color:#a0440e;text-decoration:none;font-size:12px;font-weight:750}.period{background:#fffaf4}.disabled-action{min-height:48px;border:0;border-radius:12px;background:#e5ddd7;color:#665d55;font-size:14px;font-weight:750}.footnote{margin:0}@media(max-width:350px){.deps li{align-items:start}.deps a{padding-top:1px}}
</style>

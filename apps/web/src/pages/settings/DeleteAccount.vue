<script setup lang="ts">
import { useRouter } from 'vue-router';
import { COOLDOWN_DAYS } from '../../domain/settings';

const router = useRouter();
</script>

<template>
  <div class="delete-page"><button type="button" class="back" @click="router.push('/settings')">‹ 设置</button><p class="eyebrow">ACCOUNT CONTROL</p><h1>注销账号</h1><p class="lead">账号注销会影响宠物档案、家庭协作、投稿与活动记录。提交前需要先从平台读取并核对这些依赖，再提供明确的最终确认。</p><section class="card"><h2>申请入口暂不可用</h2><p>当前服务端尚不能完整核对你作为宠物所有者、家庭管理员和活动组织者的全部依赖；页面也无法读取真实的待处理清单。为避免遗漏，暂不发送注销申请。</p><button type="button" disabled>暂不能提交注销申请</button></section><section class="info"><h2>接通后应如何处理</h2><ol><li>列出需要转交或解除的宠物、家庭和活动关系。</li><li>逐项处理后，重新确认影响范围。</li><li>平台受理并返回申请时间后，进入 {{ COOLDOWN_DAYS }} 天冷静期；撤销结果以平台确认为准。</li></ol><router-link to="/help">需要协助？联系支持 →</router-link></section></div>
</template>

<style scoped>
.delete-page { width: min(720px, calc(100% - 32px)); margin: 42px auto; }.back { min-height: 44px; padding: 0; border: 0; background: transparent; color: #b85111; font-weight: 700; }.eyebrow { margin: 28px 0 9px; color: #b85111; font-size: 12px; font-weight: 800; letter-spacing: .14em; }h1 { margin: 0; font-size: clamp(31px, 5vw, 43px); }.lead { color: #64574d; line-height: 1.8; }.card,.info { margin-top: 24px; padding: clamp(22px, 4vw, 32px); border: 1px solid #eadfd4; border-radius: 22px; background: #fff; }.card { border-color: #ebd5cc; background: #fff8f4; }.card h2,.info h2 { margin: 0; font-size: 20px; }.card p,.info li { color: #64574d; line-height: 1.8; }.card button { min-height: 46px; padding: 0 20px; border: 0; border-radius: 999px; background: #c8b4a7; color: #fff; cursor: not-allowed; }.info ol { padding-left: 22px; }.info a { display: inline-flex; align-items: center; min-height: 44px; color: #b85111; font-weight: 700; text-decoration: none; }
</style>

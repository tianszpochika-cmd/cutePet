<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { api } from '@cutepet/api-client';
import { ADMIN_TRANSFER_SEPARATION, TRANSFER_TTL_HOURS } from '../../domain/closedLoop';

const family = ref<{ name: string; role: string } | null>(null);
const loading = ref(true);
const error = ref('');

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.familyMine() as { name?: unknown; role?: unknown };
    if (typeof response.name !== 'string' || typeof response.role !== 'string') throw new Error('家庭信息暂时无法读取');
    family.value = { name: response.name, role: response.role };
  } catch (cause) {
    family.value = null;
    error.value = cause instanceof Error ? cause.message : '家庭信息加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="transfer-page">
    <nav><router-link to="/family">← 返回家庭</router-link></nav>
    <header><p class="eyebrow">家庭管理</p><h1>转交管理员，需要当事人确认。</h1><p>{{ ADMIN_TRANSFER_SEPARATION }}</p></header>
    <p v-if="loading" class="state" role="status">正在读取家庭信息…</p>
    <section v-else-if="error" class="state error" role="alert"><h2>家庭信息暂时无法读取</h2><p>{{ error }}</p><button type="button" @click="load">重新加载</button></section>
    <section v-else-if="family?.role !== 'OWNER'" class="state"><h2>当前身份不能发起管理员转交</h2><p>只有家庭创建者可以发起。宠物所有权不随管理员身份一同转移。</p><router-link to="/family">返回家庭概况 →</router-link></section>
    <template v-else-if="family">
      <section class="subject"><span>当前家庭</span><h2>{{ family.name }}</h2><p>你是当前家庭创建者</p></section>
      <section class="effects"><h2>转交前需要确认</h2><ol><li><strong>选择同家庭接收人</strong><p>双方分别核验身份，并确认继续承担家庭管理职责。</p></li><li><strong>等待接收人答复</strong><p>申请最长保留 {{ TRANSFER_TTL_HOURS }} 小时；接受前，原管理员身份保持有效。</p></li><li><strong>家庭角色与宠物资料分开</strong><p>转交管理员不会让接收人自动看到或管理其他成员未共享的宠物。</p></li></ol></section>
      <section class="unavailable" role="status"><h2>当前暂不能发送转交申请</h2><p>此页尚无法核对可接收的家庭成员。为避免发给错误的人，暂不提供提交操作；管理员身份没有变化。</p><router-link to="/family">返回家庭概况 →</router-link></section>
    </template>
  </div>
</template>

<style scoped>
.transfer-page{width:min(760px,calc(100% - 32px));margin:0 auto;padding:28px 0 90px;display:grid;gap:20px}nav a{display:inline-flex;align-items:center;min-height:44px;color:#5c3c8c;text-decoration:none;font-weight:700}.eyebrow{margin:0 0 8px;color:#73539a;font-size:12px;font-weight:800;letter-spacing:.08em}header h1{margin:0;font-size:clamp(29px,4vw,42px);line-height:1.2}header>p:last-child{margin:12px 0 0;color:#6d6259}.state,.subject,.effects,.unavailable{padding:25px;border:1px solid #e9ded3;border-radius:20px;background:#fff}.state h2,.effects h2,.unavailable h2{margin:0 0 9px;font-size:19px}.state p,.unavailable p{margin:0;color:#6d6259;font-size:14px;line-height:1.8}.state a,.unavailable a{display:inline-flex;align-items:center;min-height:44px;margin-top:11px;color:#5c3c8c;font-weight:700;text-decoration:none}.state button{min-height:43px;margin-top:13px;padding:8px 17px;border:0;border-radius:999px;background:#5c3c8c;color:#fff;font-weight:700;cursor:pointer}.error{border-color:#e9c5c5}.subject{background:linear-gradient(120deg,#f4eefb,#fff)}.subject>span{color:#73539a;font-size:12px;font-weight:750}.subject h2{margin:5px 0;font-size:28px}.subject p{margin:0;color:#6d6259;font-size:13px}.effects ol{display:grid;gap:14px;margin:18px 0 0;padding:0;list-style:none;counter-reset:step}.effects li{padding:18px;border-radius:15px;background:#faf7f3;counter-increment:step}.effects li::before{content:'0' counter(step);float:right;color:#5c3c8c;font-size:13px;font-weight:800}.effects strong{font-size:15px}.effects p{margin:6px 0 0;color:#6d6259;font-size:13px;line-height:1.7}.unavailable{background:#faf8f6}
</style>

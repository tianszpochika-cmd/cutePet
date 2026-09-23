<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, ApiClientError } from '@cutepet/api-client';
import { TRANSFER_TTL_HOURS } from '../../domain/closedLoop';

const route = useRoute();
const router = useRouter();
const petId = String(route.params.id ?? '');
const pet = ref<{ name: string } | null>(null);
const family = ref<{ name: string } | null>(null);
const loading = ref(true);
const denied = ref(false);
const error = ref('');

async function load() {
  loading.value = true;
  denied.value = false;
  error.value = '';
  pet.value = null;
  family.value = null;
  if (!/^\d+$/.test(petId)) { error.value = '宠物地址无效'; loading.value = false; return; }
  try {
    const [petResponse, familyResponse] = await Promise.all([
      api.petGet({ params: { id: petId } }),
      api.familyMine(),
    ]);
    const petData = petResponse as { name?: unknown };
    const familyData = familyResponse as { name?: unknown };
    if (typeof petData.name !== 'string' || typeof familyData.name !== 'string') throw new Error('转移信息暂时无法读取');
    pet.value = { name: petData.name };
    family.value = { name: familyData.name };
  } catch (cause) {
    if (cause instanceof ApiClientError && (cause.status === 403 || cause.status === 404)) denied.value = true;
    else error.value = cause instanceof Error ? cause.message : '转移信息加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="transfer-page">
    <nav><button type="button" @click="router.back()">← 返回宠物</button></nav>
    <header><p class="eyebrow">宠物所有权</p><h1>转移之前，先看清影响。</h1><p>宠物所有权转移与家庭管理员转交是两件独立的事。</p></header>
    <p v-if="loading" class="state" role="status">正在核对宠物与家庭信息…</p>
    <section v-else-if="denied" class="state"><h2>无法查看这只宠物的转移信息</h2><p>宠物可能不存在，或你不是所有者。这里不会展示私有档案信息。</p><router-link to="/pets">返回我的宠物 →</router-link></section>
    <section v-else-if="error" class="state error" role="alert"><h2>转移信息暂时无法读取</h2><p>{{ error }}</p><button type="button" @click="load">重新加载</button></section>
    <template v-else-if="pet && family">
      <section class="subject"><span>准备转移的宠物</span><h2>{{ pet.name }}</h2><p>当前家庭：{{ family.name }}</p></section>
      <section class="effects"><h2>转移需要双方确认</h2><ol><li><strong>所有者发起申请</strong><p>需要从同一家庭中选择具备资格的接收人，并核对当前共享与提醒。</p></li><li><strong>接收人阅读影响并确认</strong><p>申请有效期为 {{ TRANSFER_TTL_HOURS }} 小时。接收前，原所有者的权限不变。</p></li><li><strong>接受后更新权限</strong><p>原所有者转为只读；其他可管理授权降为只读。历史记录和处理人继续保留。</p></li></ol></section>
      <section class="unavailable" role="status"><h2>当前暂不能发送转移申请</h2><p>此页尚无法核对同家庭接收人的身份与资格。为避免转给错误的人，暂不提供提交操作；这只宠物的所有权没有变化。</p><router-link to="/family">查看家庭概况 →</router-link></section>
    </template>
  </div>
</template>

<style scoped>
.transfer-page{width:min(760px,calc(100% - 32px));margin:0 auto;padding:28px 0 90px;display:grid;gap:20px}nav button{min-height:44px;padding:0;border:0;background:none;color:#5c3c8c;font-weight:700;cursor:pointer}.eyebrow{margin:0 0 8px;color:#73539a;font-size:12px;font-weight:800;letter-spacing:.08em}header h1{margin:0;font-size:clamp(29px,4vw,42px);line-height:1.2}header>p:last-child{margin:12px 0 0;color:#6d6259}.state,.subject,.effects,.unavailable{padding:25px;border:1px solid #e9ded3;border-radius:20px;background:#fff}.state h2,.effects h2,.unavailable h2{margin:0 0 9px;font-size:19px}.state p,.unavailable p{margin:0;color:#6d6259;font-size:14px;line-height:1.8}.state a,.unavailable a{display:inline-flex;align-items:center;min-height:44px;margin-top:11px;color:#5c3c8c;font-weight:700;text-decoration:none}.state button{min-height:43px;margin-top:13px;padding:8px 17px;border:0;border-radius:999px;background:#5c3c8c;color:#fff;font-weight:700;cursor:pointer}.error{border-color:#e9c5c5}.subject{background:linear-gradient(120deg,#f4eefb,#fff)}.subject>span{color:#73539a;font-size:12px;font-weight:750}.subject h2{margin:5px 0;font-size:28px}.subject p{margin:0;color:#6d6259;font-size:13px}.effects ol{display:grid;gap:14px;margin:18px 0 0;padding:0;list-style:none;counter-reset:step}.effects li{padding:18px;border-radius:15px;background:#faf7f3;counter-increment:step}.effects li::before{content:'0' counter(step);float:right;color:#5c3c8c;font-size:13px;font-weight:800}.effects strong{font-size:15px}.effects p{margin:6px 0 0;color:#6d6259;font-size:13px;line-height:1.7}.unavailable{background:#faf8f6}
</style>

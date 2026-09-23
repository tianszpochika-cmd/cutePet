<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, ApiClientError } from '@cutepet/api-client';

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
    if (typeof petData.name !== 'string' || typeof familyData.name !== 'string') throw new Error('共享信息暂时无法读取');
    pet.value = { name: petData.name };
    family.value = { name: familyData.name };
  } catch (cause) {
    if (cause instanceof ApiClientError && (cause.status === 403 || cause.status === 404)) denied.value = true;
    else error.value = cause instanceof Error ? cause.message : '共享信息加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="share-page">
    <nav><button type="button" @click="router.back()">← 返回</button></nav>
    <header><p class="eyebrow">逐宠共享</p><h1>先看清范围，再给家人授权。</h1><p>家庭身份与宠物权限是两回事。只有这只宠物的所有者能调整共享。</p></header>
    <p v-if="loading" class="state" role="status">正在核对宠物与家庭信息…</p>
    <section v-else-if="denied" class="state"><h2>无法查看这只宠物的共享信息</h2><p>宠物可能不存在，或当前账号无权读取。这里不会显示私有档案信息。</p><router-link to="/pets">返回我的宠物 →</router-link></section>
    <section v-else-if="error" class="state error" role="alert"><h2>共享信息暂时无法读取</h2><p>{{ error }}</p><button type="button" @click="load">重新加载</button></section>
    <template v-else-if="pet && family">
      <section class="pet-card"><div><span>当前宠物</span><h2>{{ pet.name }}</h2><p>所属家庭：{{ family.name }}</p></div><span class="owner-badge">权限尚未核验</span></section>
      <section class="principles"><h2>家人能做什么？</h2><div class="levels"><article><span>只读</span><p>查看这只已共享宠物的档案、记录和待办，不能修改或处理。</p></article><article><span>可管理</span><p>可以新增记录、处理待办；更正健康记录时仅限自己录入的条目。</p></article><article><span>所有者</span><p>决定共享对象和权限，核验并生成健康摘要，负责转移、归档或删除。</p></article></div></section>
      <section class="unavailable" role="status"><h2>暂时无法安全调整成员权限</h2><p>当前无法核对每位家人对这只宠物的现有权限，因此这里不提供修改按钮。现有共享状态不会因打开此页而改变。</p><router-link to="/family">返回家庭概况 →</router-link></section>
    </template>
  </div>
</template>

<style scoped>
.share-page{width:min(850px,calc(100% - 32px));margin:0 auto;padding:28px 0 90px;display:grid;gap:20px}nav button{min-height:44px;padding:0;border:0;background:none;color:#5c3c8c;font-weight:700;cursor:pointer}.eyebrow{margin:0 0 8px;color:#73539a;font-size:12px;font-weight:800;letter-spacing:.08em}header h1{margin:0;font-size:clamp(29px,4vw,42px);line-height:1.2}header>p:last-child{margin:12px 0 0;color:#6d6259}.state,.pet-card,.principles,.unavailable{padding:25px;border:1px solid #e9ded3;border-radius:20px;background:#fff}.state h2,.principles h2,.unavailable h2{margin:0 0 8px;font-size:19px}.state p,.unavailable p{margin:0;color:#6d6259;font-size:14px;line-height:1.8}.state a,.unavailable a{display:inline-flex;align-items:center;min-height:44px;margin-top:11px;color:#5c3c8c;font-weight:700;text-decoration:none}.state button{min-height:43px;margin-top:13px;padding:8px 17px;border:0;border-radius:999px;background:#5c3c8c;color:#fff;font-weight:700;cursor:pointer}.error{border-color:#e9c5c5}.pet-card{display:flex;justify-content:space-between;align-items:center;gap:20px;background:linear-gradient(120deg,#f4eefb,#fff)}.pet-card span:first-child{color:#73539a;font-size:12px;font-weight:750}.pet-card h2{margin:5px 0;font-size:28px}.pet-card p{margin:0;color:#6d6259;font-size:13px}.owner-badge{flex:none;padding:7px 12px;border-radius:999px;background:#fff;color:#5c3c8c;font-size:12px;font-weight:700}.levels{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:13px;margin-top:19px}.levels article{padding:19px;border-radius:15px;background:#f6f1fb}.levels span{color:#5c3c8c;font-size:15px;font-weight:800}.levels p{margin:9px 0 0;color:#6d6259;font-size:13px;line-height:1.7}.unavailable{background:#faf8f6}@media(max-width:650px){.levels{grid-template-columns:1fr}.pet-card{align-items:start;flex-direction:column}}
</style>

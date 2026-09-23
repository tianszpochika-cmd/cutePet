<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';

interface Pet { id: number; name: string; species: string; state: string; deletedAt?: string | null }
type View = 'share' | 'transfer' | 'recycle';

const route = useRoute();
const router = useRouter();
const view = computed<View>(() => route.path === '/pets/recycle' ? 'recycle' :
  route.path.endsWith('/transfer') ? 'transfer' : 'share');
const title = computed(() => view.value === 'recycle' ? '归档与回收站' :
  view.value === 'transfer' ? '所有权' : '共享范围');
const authorized = ref(Boolean(getAccessToken()));
const loading = ref(false);
const error = ref('');
const pet = ref<Pet | null>(null);
const inactivePets = ref<Pet[]>([]);
let requestVersion = 0;

function validPet(value: unknown, expectedId?: string): value is Pet {
  if (!value || typeof value !== 'object') return false;
  const row = value as Record<string, unknown>;
  return typeof row.id === 'number' && Number.isSafeInteger(row.id) && row.id > 0 &&
    (!expectedId || String(row.id) === expectedId) &&
    typeof row.name === 'string' && typeof row.species === 'string' &&
    typeof row.state === 'string' &&
    (row.deletedAt === undefined || row.deletedAt === null || typeof row.deletedAt === 'string');
}
async function load() {
  const version = ++requestVersion;
  authorized.value = Boolean(getAccessToken());
  pet.value = null;
  inactivePets.value = [];
  error.value = '';
  if (!authorized.value) { loading.value = false; return; }
  loading.value = true;
  try {
    if (view.value === 'recycle') {
      const result: unknown = await api.petsList({ query: { all: true } });
      if (!Array.isArray(result) || !result.every((item) => validPet(item))) {
        throw new Error('归档列表格式暂无法确认');
      }
      if (version === requestVersion) {
        inactivePets.value = result.filter((item) => item.state === 'ARCHIVED' || item.state === 'DELETED');
      }
    } else {
      const id = String(route.params.id ?? '');
      if (!/^[1-9]\d*$/.test(id)) throw new Error('宠物编号无效');
      const result: unknown = await api.petGet({ path: { id } });
      if (!validPet(result, id)) throw new Error('宠物档案格式暂无法确认');
      if (version === requestVersion) pet.value = result;
    }
  } catch (cause) {
    if (version === requestVersion) error.value = cause instanceof Error ? cause.message : '资料读取失败';
  } finally { if (version === requestVersion) loading.value = false; }
}
watch(() => route.fullPath, () => { void load(); }, { immediate: true });
</script>

<template>
  <div class="x-pages">
    <header class="top">
      <button type="button" class="back" @click="router.push('/pets')">← 宠物列表</button>
      <span class="eyebrow">PET CARE SETTINGS</span>
      <h1>{{ title }}</h1>
      <p v-if="view === 'recycle'">查看平台返回的非活动档案。</p>
      <p v-else>先核对宠物，再查看此操作的当前可用状态。</p>
    </header>
    <section v-if="!authorized" class="state" role="status">
      <h2>登录后查看</h2><p>宠物与共享信息仅对有权账号开放。</p>
      <button type="button" class="primary" @click="router.push({ path: '/login', query: { return: route.fullPath } })">前往登录</button>
    </section>
    <p v-else-if="loading" class="state compact" role="status">正在读取平台资料…</p>
    <section v-else-if="error" class="state error-state" role="alert">
      <h2>暂时无法读取</h2><p>{{ error }}</p><button type="button" class="secondary" @click="load">重新读取</button>
    </section>

    <template v-else-if="view === 'recycle'">
      <section v-if="!inactivePets.length" class="state">
        <span class="mark" aria-hidden="true">✿</span>
        <h2>没有归档或回收站档案</h2>
        <p>平台目前没有返回属于你的非活动宠物。</p>
        <button type="button" class="secondary" @click="router.push('/pets')">返回在照护列表</button>
      </section>
      <template v-else>
        <p class="count">{{ inactivePets.length }} 份非活动档案</p>
        <section v-for="item in inactivePets" :key="item.id" class="card recycle-card" data-testid="x06">
          <div class="identity"><span class="mark" aria-hidden="true">✿</span><div><h2>{{ item.name }}</h2><p>{{ item.species }} · {{ item.state === 'ARCHIVED' ? '已归档' : '回收站' }}</p></div></div>
          <p v-if="item.state === 'DELETED'" class="hint">软删除的恢复窗口需由平台核对，本页不估算剩余天数。</p>
          <p v-else class="hint">已归档宠物不出现在默认在照护列表。</p>
          <div class="unavailable">恢复操作等待可信身份链路接入。当前只展示平台返回的状态，尚未执行恢复。</div>
          <button type="button" class="secondary" data-testid="restore" disabled>暂不能恢复</button>
        </section>
      </template>
    </template>

    <template v-else-if="pet">
      <section class="card identity">
        <span class="mark" aria-hidden="true">✿</span>
        <div><h2>{{ pet.name }}</h2><p>{{ pet.species }} · {{ pet.state === 'ACTIVE' ? '在照护' : '档案状态：' + pet.state }}</p></div>
      </section>
      <section v-if="view === 'share'" class="card feature" data-testid="x03">
        <h2>共享范围尚未接入</h2>
        <p>当前接口不能返回这只宠物的完整成员与权限列表，因此这里不会显示示例成员，也不会推断谁拥有编辑权。</p>
        <div class="unavailable">共享邀请与权限变更需要服务端核验所有者身份并返回实际授权结果。</div>
        <button type="button" class="secondary" disabled>暂不能管理共享</button>
      </section>
      <section v-else class="card feature" data-testid="x04">
        <h2>所有权转移尚未接入</h2>
        <p>转移前需要明确接收方、验证步骤、有效期和完成后的共享影响；当前页面无法从平台核对这些状态。</p>
        <div class="unavailable">转移申请不会在这里发送，宠物所有者不会发生变化。</div>
        <button type="button" class="secondary" disabled>暂不能发起转移</button>
      </section>
      <button type="button" class="text-button" @click="router.push('/pets/' + pet.id)">返回 {{ pet.name }} 的档案 →</button>
    </template>
  </div>
</template>

<style scoped>
.x-pages{display:grid;gap:14px;padding:18px 16px calc(34px + env(safe-area-inset-bottom));color:#30241d}.top{display:grid;justify-items:start;gap:6px}.back{min-height:44px;padding:0;border:0;background:transparent;color:#94400c;font-size:13px;font-weight:750}.eyebrow{color:#a74b0f;font-size:11px;font-weight:800;letter-spacing:.12em}.top h1{margin:0;font-size:27px;line-height:1.2}.top p,.state p,.card p{margin:0;color:#706155;font-size:13px;line-height:1.7}.state,.card{padding:18px;border:1px solid #e8dbcd;border-radius:18px;background:#fff}.state{display:grid;justify-items:start;gap:12px}.state.compact{display:block;color:#706155}.error-state{background:#fff4f1}.state h2,.card h2{margin:0;font-size:18px}.count{margin:0;color:#8f430f;font-size:12px;font-weight:800}.mark{display:grid;place-items:center;width:47px;height:47px;flex:none;border-radius:15px;background:#fff0e1;color:#b85111;font-size:26px}.identity{display:flex;align-items:center;gap:12px}.identity p{margin-top:4px}.recycle-card,.feature{display:grid;gap:13px}.recycle-card .identity{padding:0;border:0}.hint{font-size:12px}.unavailable{padding:13px;border-radius:11px;background:#fff4e7;color:#67451f;font-size:13px;line-height:1.65}.primary,.secondary{min-height:48px;padding:10px 14px;border-radius:12px;font-size:14px;font-weight:750}.primary{border:0;background:#b85111;color:#fff}.secondary{border:1px solid #d0a782;background:#fff;color:#8f3e0d}.secondary:disabled{opacity:.62}.text-button{min-height:44px;padding:0;border:0;background:transparent;color:#93400c;text-align:left;font-size:13px;font-weight:750}button:focus-visible{outline:3px solid #783307;outline-offset:2px}@media(max-width:350px){.state,.card{padding:15px}.top h1{font-size:24px}}
</style>

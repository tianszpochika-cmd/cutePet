<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getAccessToken } from '@cutepet/api-client';

const route = useRoute();
const router = useRouter();
const authorized = computed(() => Boolean(getAccessToken()));
const reference = computed(() => {
  const id = String(route.params.id ?? '');
  return /^[1-9]\d*$/.test(id) ? id : '';
});
</script>

<template>
  <div class="todo-confirm">
    <header class="top">
      <button type="button" class="back" @click="router.push('/')">← 今日照护</button>
      <span class="eyebrow">CARE COMPLETION</span>
      <h1>完成照护待办</h1>
      <p>完成一项健康待办，需要把它与真实的健康记录一起核对。</p>
    </header>
    <section v-if="!authorized" class="card state" role="status">
      <span class="mark" aria-hidden="true">◇</span>
      <h2>登录后查看待办</h2>
      <p>待办及完成情况属于有权照护的账号。</p>
      <button type="button" class="primary" @click="router.push({ path: '/login', query: { return: route.fullPath } })">前往登录</button>
    </section>
    <section v-else class="card state" role="status">
      <span class="mark" aria-hidden="true">◇</span>
      <h2>完成操作尚未接入</h2>
      <p v-if="reference" class="reference">链接中的待办编号：#{{ reference }}。这只是链接参数，页面尚不能从平台核对待办详情。</p>
      <p v-else class="reference">待办编号无效，无法核对待办详情。</p>
      <p>当前接口没有提供待办详情读取，也没有“关联有效健康记录并原子完成待办”的回执契约。这里不会显示宠物、经办人、下次日期或已完成状态。</p>
      <div class="notice">如果照护已经发生，可先为对应宠物创建健康记录。该记录不会自动完成本待办；待服务端闭环能力接入后才能确认完成。</div>
      <button type="button" class="primary" data-testid="confirm" disabled>暂不能确认完成</button>
      <button type="button" class="secondary" @click="router.push('/pets')">查看宠物档案</button>
      <button type="button" class="text-button" @click="router.push('/')">返回今日照护 →</button>
    </section>
  </div>
</template>

<style scoped>
.todo-confirm{display:grid;gap:17px;padding:18px 16px calc(36px + env(safe-area-inset-bottom));color:#30241d}.top{display:grid;justify-items:start;gap:7px}.back{min-height:44px;padding:0;border:0;background:transparent;color:#94420d;font-size:13px;font-weight:750}.eyebrow{color:#a6490e;font-size:11px;font-weight:800;letter-spacing:.12em}.top h1{margin:0;font-size:27px;line-height:1.2}.top p,.state p{margin:0;color:#706155;font-size:13px;line-height:1.7}.card{padding:20px;border:1px solid #e8d9ca;border-radius:18px;background:#fff}.state{display:grid;justify-items:start;gap:14px}.mark{display:grid;place-items:center;width:48px;height:48px;border-radius:15px;background:#fff1e1;color:#b85111;font-size:26px}.state h2{margin:0;font-size:19px}.state .reference{color:#684b35;font-weight:750}.notice{padding:14px;border-radius:12px;background:#fff4e7;color:#65451f;font-size:13px;line-height:1.7}.primary,.secondary{width:100%;min-height:48px;padding:10px 14px;border-radius:12px;font-size:14px;font-weight:750}.primary{border:0;background:#b85111;color:#fff}.primary:disabled{opacity:.58}.secondary{border:1px solid #d1ad8c;background:#fff;color:#8d3d0c}.text-button{min-height:44px;padding:0;border:0;background:transparent;color:#93410e;font-size:13px;font-weight:750}button:focus-visible{outline:3px solid #783307;outline-offset:2px}@media(max-width:350px){.top h1{font-size:24px}.card{padding:16px}}
</style>

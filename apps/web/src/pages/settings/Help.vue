<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { HELP_SECTIONS } from '../../domain/settings';

const router = useRouter();
const open = ref<string | null>('account');
const description = ref('');
const sending = ref(false);
const result = ref('');
const error = ref('');

const FAQ: Record<string, { q: string; a: string }[]> = {
  account: [
    { q: '登录为什么暂不可用？', a: '正式协议和可验证的短信通道尚未同时就绪，登录页可以查看流程示意。' },
    { q: '遇到账号限制怎么办？', a: '可从账号状态页查看说明，并通过客服工单描述问题；真实处罚状态以平台通知为准。' },
  ],
  pet: [
    { q: '如何开始建立宠物档案？', a: '从首页或宠物列表主动选择新建；只有平台返回档案编号，页面才会进入详情。' },
    { q: '为什么体重暂不能提交？', a: '体重写入尚未纳入当前前端契约。已有曲线可以查看，新的输入不会被误标为已保存。' },
  ],
  content: [
    { q: '投稿状态在哪里看？', a: '投稿中心会展示平台可返回的状态；明细不完整时会明确提示，不能把本地编辑视为已发布。' },
    { q: '能在稿件待审时继续修改吗？', a: '待审稿件由审核流程处理，修改与重新提交须以平台实际可用操作为准。' },
  ],
  explore: [
    { q: '场所信息是否保证准确？', a: '场所资料可能变化，出行前请再次核对营业时间与联系方式。' },
    { q: '活动报名何时算成功？', a: '只有平台返回实际报名结果才能确认；查看活动卡片或勾选告知不等于报名成功。' },
  ],
  privacy: [
    { q: '如何申请数据导出？', a: '数据导出页会显示当前接入状态。独立身份核验完成前暂不开放申请。' },
    { q: '如何注销账号？', a: '注销页会说明影响范围；依赖核对不完整时暂不能提交申请，可联系支持。' },
  ],
};

async function submitTicket() {
  result.value = '';
  error.value = '';
  if (!getAccessToken()) {
    void router.push({ path: '/login', query: { return: '/help' } });
    return;
  }
  if (description.value.trim().length < 10) {
    error.value = '请至少写 10 个字，说明遇到的问题。';
    return;
  }
  sending.value = true;
  try {
    const response = await api.ticketsCreate({ body: { actionType: 'CUSTOMER_SERVICE', reason: description.value.trim() } }) as unknown as { id?: number | string };
    if (!response?.id) throw new Error('服务端未返回工单编号，请到我的工单确认后再重试。');
    result.value = `已提交工单 #${response.id}。处理进度以工单记录为准。`;
    description.value = '';
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '提交失败，请保留输入后重试。';
  } finally {
    sending.value = false;
  }
}
</script>

<template>
  <div class="help">
    <header>
      <button type="button" class="back" @click="router.push('/me')">‹ 返回</button>
      <h1>帮助与客服</h1>
    </header>

    <!-- 申诉入口置顶（合规 A7/U82：受限用户可达） -->
    <button
      type="button"
      class="appeal"
      data-testid="appeal-entry"
      @click="router.push('/blocked')"
    >
      ⚖️ 处置申诉入口 / 受限账号查询（X15）
    </button>

    <nav class="sections">
      <button
        v-for="s in HELP_SECTIONS"
        :key="s.id"
        type="button"
        :class="{ on: open === s.id }"
        @click="open = open === s.id ? null : s.id"
      >
        {{ s.label }}
      </button>
    </nav>

    <div v-if="open" class="faq">
      <div v-for="f in FAQ[open]" :key="f.q" class="qa">
        <strong>{{ f.q }}</strong>
        <p>{{ f.a }}</p>
      </div>
    </div>

    <section class="ticket" data-testid="ticket">
      <h2>没解决？联系客服</h2>
      <textarea v-model="description" rows="4" placeholder="描述你遇到的问题，不要填写验证码或密码" aria-label="问题描述" />
      <button type="button" class="primary" :disabled="sending" @click="submitTicket">{{ sending ? '提交中…' : '提交工单' }}</button>
      <p v-if="error" class="err" role="alert">{{ error }}</p>
      <p v-if="result" class="ok" role="status">{{ result }}</p>
    </section>
  </div>
</template>

<style scoped>
.help { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 14px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #b85111; }
.appeal { height: 48px; border: none; border-radius: 12px; background: #f5f3ff; color: #6d5bd0; font-weight: 600; text-align: left; padding: 0 16px; }
.sections { display: flex; gap: 6px; overflow-x: auto; }
.sections button { height: 32px; padding: 0 12px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; white-space: nowrap; }
.sections button.on { background: #b85111; color: #fff; font-weight: 600; }
.faq { display: grid; gap: 10px; }
.qa { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; display: grid; gap: 6px; }
.qa p { margin: 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.ticket { display: grid; gap: 10px; }
.ticket h2 { font-size: 16px; margin: 0; }
textarea { border: 1px solid #f0e6dc; border-radius: 12px; padding: 12px; font-family: inherit; }
.primary { height: 44px; border: none; border-radius: 999px; background: #b85111; color: #fff; font-weight: 600; }
.primary:disabled { opacity: .6; cursor: wait; }
.err { color: #a63322; font-size: 13px; }
.ok { color: #22c55e; font-size: 13px; }
</style>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { HELP_SECTIONS } from '../../domain/settings';

const router = useRouter();
const open = ref<string | null>('account');
const submitted = ref(false);

const FAQ: Record<string, { q: string; a: string }[]> = {
  account: [
    { q: '如何换绑手机号？', a: '设置 → 账号与安全 → 换绑，需原号验证（T1.3）。' },
    { q: '被封禁了怎么办？', a: '访问受限账号页（X15）可申诉与查询状态，无需恢复账号。' },
  ],
  pet: [
    { q: '删掉的宠物能恢复吗？', a: '软删 30 天内可恢复；超期需联系人工（U66）。' },
    { q: '体重记录支持斤吗？', a: '支持，kg/斤切换，存储统一 kg（决议）。' },
  ],
  content: [
    { q: '投稿一直待审核？', a: '审核 SLA 24h；超时会在看板标红催办。' },
    { q: '3 次驳回会怎样？', a: '质量退修累计 3 次暂停投稿 7 天，不计违规（U76）。' },
  ],
  explore: [
    { q: '为什么不能连续评价？', a: '同一场所每日限 1 条（防刷决议）。' },
    { q: '报名被拒绝的可能原因？', a: '90 天窗口/截止/已开始/满员/重复/未勾同意（U68/U69）。' },
  ],
  privacy: [
    { q: '如何导出我的数据？', a: '设置 → 数据导出：核验后生成 JSON 并留痕。' },
    { q: '注销有什么后果？', a: '见注销页依赖清单；冷静期 15 天可撤销；UGC 匿名化保留。' },
  ],
};

function submitTicket() {
  submitted.value = true;
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
      <textarea v-model="(undefined as unknown as string)" rows="3" placeholder="描述你的问题…" />
      <button v-if="!submitted" type="button" class="primary" @click="submitTicket">提交工单（48h 反馈）</button>
      <p v-else class="ok">工单已提交，进度见消息中心。</p>
    </section>
  </div>
</template>

<style scoped>
.help { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 14px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.appeal { height: 48px; border: none; border-radius: 12px; background: #f5f3ff; color: #6d5bd0; font-weight: 600; text-align: left; padding: 0 16px; }
.sections { display: flex; gap: 6px; overflow-x: auto; }
.sections button { height: 32px; padding: 0 12px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; white-space: nowrap; }
.sections button.on { background: #ff7a2f; color: #fff; font-weight: 600; }
.faq { display: grid; gap: 10px; }
.qa { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; display: grid; gap: 6px; }
.qa p { margin: 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.ticket { display: grid; gap: 10px; }
.ticket h2 { font-size: 16px; margin: 0; }
textarea { border: 1px solid #f0e6dc; border-radius: 12px; padding: 12px; font-family: inherit; }
.primary { height: 44px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; }
.ok { color: #22c55e; font-size: 13px; }
</style>

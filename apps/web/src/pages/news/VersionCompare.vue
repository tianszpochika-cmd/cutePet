<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { versionHeader, VERSION_DIFF_REQUIRED, WITHDRAW_THEN_EDIT, versionOutcome } from '../../domain/closedLoop';

const route = useRoute();
const router = useRouter();
const submissionId = String(route.params.id);
const liveVersion = ref(1);
const head = () => versionHeader(liveVersion.value);
const decision = ref<'approved' | 'rejected' | null>(null);
const showDiff = ref(false);
</script>

<template>
  <div class="x14">
    <header>
      <button type="button" class="back" @click="router.push('/submissions')">‹ 我的投稿</button>
      <h1>内容修改版本（X14）</h1>
    </header>

    <div class="versions" data-testid="version-head">
      <span class="live">{{ head().live }}</span>
      <span class="arrow">→</span>
      <span class="draft">{{ head().draft }}</span>
    </div>

    <p class="rule">{{ WITHDRAW_THEN_EDIT }}</p>

    <button type="button" class="ghost" data-testid="toggle-diff" @click="showDiff = !showDiff">
      {{ showDiff ? '收起对比' : '查看对比预览' }}{{ VERSION_DIFF_REQUIRED ? '（必看）' : '' }}
    </button>

    <section v-if="showDiff" class="diff" data-testid="diff">
      <div class="col">
        <h3>{{ head().live }}（线上）</h3>
        <p>原段落：幼猫喂养需要注意少食多餐……</p>
      </div>
      <div class="col added">
        <h3>{{ head().draft }}（修改稿）</h3>
        <p>＋新增段落：换粮需要 7 天过渡期……</p>
        <p>－删除段落：旧的错误建议……</p>
      </div>
    </section>

    <div class="actions">
      <button type="button" class="primary" data-testid="approve" @click="decision = 'approved'">通过（替换上线）</button>
      <button type="button" class="ghost" data-testid="reject" @click="decision = 'rejected'">驳回（保留旧版）</button>
    </div>

    <p v-if="decision" class="result" data-testid="result">{{ versionOutcome(decision) }}</p>

    <button type="button" class="link" @click="router.push('/news')">前往线上文查看旧版 →</button>
  </div>
</template>

<style scoped>
.x14 { max-width: 640px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.versions { display: flex; gap: 10px; align-items: center; font-weight: 600; }
.versions .live { background: #e7f8ef; color: #15803d; border-radius: 999px; padding: 4px 12px; font-size: 13px; }
.versions .draft { background: #fff1e8; color: #b45309; border-radius: 999px; padding: 4px 12px; font-size: 13px; }
.arrow { color: #7a6e63; }
.rule { background: #f5f3ff; color: #6d5bd0; border-radius: 8px; padding: 10px 12px; font-size: 13px; }
.diff { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.col { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 12px; }
.col.added { background: #f0fdf4; }
.col h3 { margin: 0 0 6px; font-size: 13px; color: #7a6e63; }
.col p { margin: 4px 0; font-size: 13px; color: #2b2118; }
.actions { display: flex; gap: 10px; }
.primary { flex: 1; height: 46px; border: none; border-radius: 999px; background: #22c55e; color: #fff; font-weight: 600; }
.ghost { flex: 1; height: 46px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.result { background: #eaf1ff; color: #2563eb; border-radius: 8px; padding: 10px 12px; font-size: 13px; }
.link { background: none; border: none; color: #ff7a2f; font-size: 13px; text-align: left; }
</style>

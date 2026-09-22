<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { reviewBlockers, suspectReviewHint, duplicateDayBlocked, starsPercent } from '../../domain/explore';

const route = useRoute();
const router = useRouter();
const poiId = String(route.params.id);

const scores = reactive({ friendly: 0, env: 0, service: 0 });
const content = ref('');
const attempted = ref(false);
const alreadyReviewedToday = ref(false); // 1 天 1 条（防刷决议）；真实状态随接口
const error = ref('');
const success = ref(false);

const blockers = computedBlockers();
const hint = ref<string | null>(null);

function computedBlockers() {
  return () =>
    attempted.value ? reviewBlockers({ ...scores, content: content.value }) : [];
}

function setScore(key: 'friendly' | 'env' | 'service', value: number) {
  scores[key] = value;
  hint.value = suspectReviewHint({ ...scores, content: content.value });
}

async function submit() {
  attempted.value = true;
  if (duplicateDayBlocked(alreadyReviewedToday.value)) {
    error.value = '同一场所每日仅可评价一次（防刷决议）';
    return;
  }
  if (reviewBlockers({ ...scores, content: content.value }).length > 0) return;
  error.value = '';
  try {
    const res = (await api.poiReviewCreate({
      path: { id: poiId },
      body: {
        scoreFriendly: scores.friendly,
        scoreEnv: scores.env,
        scoreService: scores.service,
        content: content.value,
        deviceKey: `web-${navigator.userAgent.length}`,
        accountAgeDays: 100,
        imageHashes: [],
      },
    })) as unknown as { state?: string };
    success.value = true;
    alreadyReviewedToday.value = true;
    setTimeout(() => void router.push(`/explore/poi/${poiId}`), 600);
    void res;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '提交失败（dev 需启动 explore-service）';
  }
}
</script>

<template>
  <div class="review">
    <header>
      <button type="button" class="back" @click="router.push(`/explore/poi/${poiId}`)">‹ 返回</button>
      <h1>写评价</h1>
    </header>

    <div v-for="row in ([['friendly', '宠物友好度'], ['env', '环境'], ['service', '服务']] as const)" :key="row[0]" class="row">
      <span>{{ row[1] }}</span>
      <button
        v-for="n in 5"
        :key="n"
        type="button"
        :class="['star', { on: scores[row[0]] >= n }]"
        :data-testid="`${row[0]}-${n}`"
        @click="setScore(row[0], n)"
      >
        ★
      </button>
      <span v-if="scores[row[0]] > 0" class="pct">{{ starsPercent(scores[row[0]]! / 5 * 5) }}%</span>
    </div>

    <textarea
      v-model="content"
      rows="5"
      placeholder="说说带崽体验（1–500 字）"
      data-testid="review-content"
      @input="hint = suspectReviewHint({ ...scores, content })"
    />

    <p v-if="hint" class="hint" data-testid="suspect-hint">{{ hint }}</p>
    <p v-if="error" class="err">{{ error }}</p>
    <p v-if="success" class="ok" data-testid="review-success">已提交（先发后审；正常内容即时展示）</p>

    <button type="button" class="primary" data-testid="submit-review" @click="submit">发布评价</button>
    <p class="tip">同一场所每日限 1 条（防刷决议）；全五星短评将进入人工复审。</p>
  </div>
</template>

<style scoped>
.review {
  max-width: 560px;
  margin: 0 auto;
  padding: 24px 16px;
  display: grid;
  gap: 12px;
}
header {
  display: flex;
  gap: 12px;
  align-items: center;
}
.back {
  background: none;
  border: none;
  color: #ff7a2f;
}
.row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}
.row > span:first-child {
  width: 80px;
  color: #2b2118;
}
.star {
  border: none;
  background: none;
  font-size: 24px;
  color: #e5ddd3;
  cursor: pointer;
  padding: 0 2px;
}
.star.on {
  color: #ffb020;
}
.pct {
  color: #7a6e63;
  font-size: 12px;
}
textarea {
  border: 1px solid #f0e6dc;
  border-radius: 12px;
  padding: 12px;
  font-family: inherit;
  font-size: 15px;
}
.primary {
  height: 48px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.hint {
  background: #fff1e8;
  color: #b45309;
  font-size: 13px;
  border-radius: 8px;
  padding: 8px 12px;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
.ok {
  color: #22c55e;
  font-size: 13px;
}
.tip {
  color: #7a6e63;
  font-size: 12px;
}
</style>

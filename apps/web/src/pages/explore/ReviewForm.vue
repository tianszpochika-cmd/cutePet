<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { reviewBlockers, suspectReviewHint } from '../../domain/explore';

const route = useRoute();
const router = useRouter();
const poiId = String(route.params.id);
const scores = reactive({ friendly: 0, env: 0, service: 0 });
const content = ref('');
const attemptedCopy = ref(false);
const feedback = ref('');
const hint = computed(() => suspectReviewHint({ ...scores, content: content.value }));
const blockers = computed(() => reviewBlockers({ ...scores, content: content.value }));
const validDraft = computed(() => blockers.value.length === 0);

function setScore(key: 'friendly' | 'env' | 'service', value: number) {
  scores[key] = value;
  feedback.value = '';
}
async function copyDraft() {
  attemptedCopy.value = true;
  if (!validDraft.value) return;
  const draft = [
    '宠物友好度 ' + scores.friendly + '/5 · 环境 ' + scores.env + '/5 · 服务 ' + scores.service + '/5',
    content.value.trim(),
  ].join('\n');
  try {
    await navigator.clipboard.writeText(draft);
    feedback.value = '草稿已复制。此操作没有发布评价。';
  } catch {
    feedback.value = '复制未成功，请手动选择正文。此操作没有发布评价。';
  }
}
</script>

<template>
  <div class="review">
    <button type="button" class="back" @click="router.push('/explore/poi/' + poiId)">← 返回场所详情</button>
    <p class="eyebrow">YOUR NOTES · 到访记录</p>
    <h1>写一份评价草稿</h1>
    <p class="intro">可以先整理体验。身份与防刷校验尚未由服务端接通，因此当前不能发布；内容只留在这个页面，离开后不会保存。</p>

    <section class="form-card" aria-labelledby="ratings-title">
      <h2 id="ratings-title">分项评分</h2>
      <div v-for="row in ([['friendly', '宠物友好度'], ['env', '环境'], ['service', '服务']] as const)" :key="row[0]" class="row">
        <span class="row-label">{{ row[1] }}</span>
        <div class="stars" role="group" :aria-label="row[1]">
          <button v-for="n in 5" :key="n" type="button" :class="['star', { on: scores[row[0]] >= n }]" :aria-label="row[1] + ' ' + n + ' 分'" :aria-pressed="scores[row[0]] === n" :data-testid="row[0] + '-' + n" @click="setScore(row[0], n)">★</button>
        </div>
        <span class="selected-score">{{ scores[row[0]] ? scores[row[0]] + ' / 5' : '未评分' }}</span>
      </div>
      <label class="content-label" for="review-content">到访体验</label>
      <textarea id="review-content" v-model="content" rows="6" maxlength="500" placeholder="说说带宠物到访的真实体验（最多 500 字）" data-testid="review-content" @input="feedback = ''" />
      <p class="counter">{{ content.trim().length }} / 500 字</p>
      <p v-if="hint" class="hint" data-testid="suspect-hint">{{ hint }}</p>
      <p v-if="attemptedCopy && !validDraft" class="error" role="alert">请给三项各打 1–5 分，并填写评价内容。</p>
      <p v-if="feedback" class="feedback" role="status">{{ feedback }}</p>
      <div class="actions">
        <button type="button" class="copy" @click="copyDraft">复制草稿</button>
        <button type="button" class="disabled-action" data-testid="submit-review" disabled aria-describedby="publish-reason">发布待接入</button>
      </div>
      <p id="publish-reason" class="tip">正式发布需由服务端核验登录身份、账号时间和设备限频，避免把本地填写的数据当作审核凭证。</p>
    </section>
  </div>
</template>

<style scoped>
.review{max-width:680px;margin:auto;padding:28px 20px 80px;color:#2b2118}.back{border:0;background:none;padding:0;color:#a64613;cursor:pointer;font:inherit}.eyebrow{color:#a64613;font-weight:800;font-size:12px;letter-spacing:.14em;margin:28px 0 7px}h1{font-size:clamp(28px,4vw,40px);letter-spacing:-.03em;margin:0}.intro{line-height:1.7;color:#6e6358;margin:12px 0 24px}.form-card{background:#fff;border:1px solid #eaded1;border-radius:18px;padding:24px;box-shadow:0 8px 28px #3a231308}h2{font-size:18px;margin:0 0 20px}.row{display:flex;align-items:center;gap:10px;min-height:51px}.row-label{width:84px;flex:none;font-size:14px}.stars{display:flex;gap:1px}.star{border:0;background:transparent;color:#d9d0c6;font-size:25px;line-height:1;padding:3px;cursor:pointer}.star.on{color:#edae32}.selected-score{margin-left:auto;color:#786b60;font-size:12px}.content-label{display:block;font-size:14px;font-weight:700;margin:22px 0 8px}textarea{box-sizing:border-box;width:100%;border:1px solid #d9ccbf;border-radius:12px;padding:12px;font:inherit;line-height:1.6;resize:vertical;color:#2b2118}.counter{text-align:right;color:#85786c;font-size:12px;margin:5px 0 0}.hint,.error,.feedback{border-radius:10px;padding:10px 12px;font-size:13px;line-height:1.5}.hint{background:#fff5e9;color:#8a5523}.error{background:#fff1ef;color:#a6372b}.feedback{background:#eff7ec;color:#365c3a}.actions{display:flex;gap:10px;margin-top:20px}.actions button{flex:1;min-height:45px;border-radius:999px;font:inherit}.copy{background:#a64613;border:1px solid #a64613;color:#fff;cursor:pointer}.disabled-action{background:#ede9e4;border:1px solid #ede9e4;color:#756b62;cursor:not-allowed}.tip{font-size:12px;line-height:1.6;color:#7b6e62;margin:13px 0 0}@media(max-width:520px){.form-card{padding:18px}.row{flex-wrap:wrap;gap:3px}.row-label{width:100%}.selected-score{margin-left:8px}.actions{flex-direction:column}}
</style>

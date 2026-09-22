<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { SUBMISSION_KINDS, submitBlockers, shouldAutosave } from '../../../web/src/domain/submission.ts';

const router = useRouter();
const form = reactive({ kind: 'ARTICLE', title: '', body: '', channel: '猫' });
const attempted = ref(false);
const savedHint = ref('');
let lastSaved: number | null = null;

const blockers = () => (attempted.value ? submitBlockers(form) : []);

function touch() {
  if (shouldAutosave(true, lastSaved, Date.now())) {
    lastSaved = Date.now();
    savedHint.value = `草稿已存 ${new Date(lastSaved).toLocaleTimeString()}`; // 30s 自动存（决议）
  }
}

function submit() {
  attempted.value = true;
  if (submitBlockers(form).length > 0) return;
  void router.push('/me');
}
</script>

<template>
  <div class="m-write">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>创作</h1>
      <span v-if="savedHint" class="saved">{{ savedHint }}</span>
    </header>

    <nav class="kinds">
      <button v-for="k in SUBMISSION_KINDS" :key="k.kind" type="button" :class="{ on: form.kind === k.kind }" @click="form.kind = k.kind">
        {{ k.label }}
      </button>
    </nav>

    <input v-model="form.title" placeholder="标题（1–120 字）" data-testid="title" maxlength="120" @input="touch" />
    <select v-model="form.channel" data-testid="channel">
      <option v-for="c in ['猫', '犬', '营养', '疾病健康', '训练行为', '异宠', '行业快讯']" :key="c">{{ c }}</option>
    </select>
    <textarea v-model="form.body" rows="8" placeholder="正文（≥10 字）30 秒自动存草稿" data-testid="body" @input="touch" />

    <p v-for="b in blockers()" :key="b" class="err" data-testid="blocker">{{ b }}</p>

    <div class="actions">
      <button type="button" class="ghost" @click="touch">存草稿</button>
      <button type="button" class="primary" data-testid="submit" @click="submit">提交审核</button>
    </div>
    <p class="tip">先审后发（SLA 24h）；3 次质量退修暂停 7 天、不计违规（U76）。</p>
  </div>
</template>

<style scoped>
.m-write { padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; color: #ff7a2f; font-size: 20px; }
h1 { margin: 0; font-size: 18px; flex: 1; }
.saved { color: #22c55e; font-size: 11px; }
.kinds { display: flex; gap: 8px; }
.kinds button { border: none; background: #fff; color: #7a6e63; border-radius: 999px; padding: 9px 14px; font-size: 13px; min-height: 40px; box-shadow: inset 0 0 0 1px #f0e6dc; }
.kinds button.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
input, select, textarea { border: 1px solid #f0e6dc; border-radius: 14px; padding: 13px 14px; font-size: 16px; font-family: inherit; background: #fff; }
input { height: 50px; }
.actions { display: flex; gap: 12px; }
.actions button { flex: 1; height: 50px; border: none; border-radius: 999px; font-size: 15px; font-weight: 700; min-height: 44px; }
.primary { background: #ff7a2f; color: #fff; }
.ghost { background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.err { color: #ef4444; font-size: 13px; margin: 0; }
.tip { color: #7a6e63; font-size: 12px; margin: 0; }
</style>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { SUBMISSION_KINDS, submitBlockers, shouldAutosave, unsavedGuard } from '../../domain/submission';
import { CHANNELS } from '../../domain/news';

const route = useRoute();
const router = useRouter();
const editingId = route.params.id ? String(route.params.id) : null;

const form = reactive({
  kind: 'ARTICLE',
  title: '',
  body: '',
  channel: '猫',
  cover: '',
  tags: '',
  relatedProduct: '',
  sourceEvidence: '',
  interestDeclared: false,
});
const attempted = ref(false);
const lastSavedAt = ref<number | null>(null);
const dirty = ref(false);
const savedHint = ref('');
const error = ref('');
const submitted = ref(false);

const blockers = computed(() =>
  attempted.value
    ? submitBlockers({ kind: form.kind, title: form.title, body: form.body, channel: form.channel })
    : [],
);

function touch() {
  dirty.value = true;
  if (shouldAutosave(true, lastSavedAt.value, Date.now())) void autosave();
}

async function autosave() {
  try {
    if (editingId) {
      await api.submissionUpdate({
        path: { id: editingId },
        body: { title: form.title, body: form.body, cover: form.cover || undefined, tags: form.tags || undefined },
      });
    } else {
      const res = (await api.submissionCreate({
        body: {
          kind: form.kind,
          title: form.title,
          body: form.body,
          channel: form.channel,
          cover: form.cover || undefined,
          tags: form.tags || undefined,
        },
      })) as unknown as { submissionId?: number };
      if (res?.submissionId) {
        void router.replace(`/write/${res.submissionId}`);
      }
    }
    lastSavedAt.value = Date.now();
    dirty.value = false;
    savedHint.value = `已保存于 ${new Date(lastSavedAt.value).toLocaleTimeString()}`;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败（dev 需启动 content-service）';
  }
}

async function submit() {
  attempted.value = true;
  if (submitBlockers({ kind: form.kind, title: form.title, body: form.body, channel: form.channel }).length > 0) {
    return;
  }
  if (dirty.value) await autosave();
  const id = editingId ?? extractEditingId();
  if (!id) return;
  try {
    await api.submissionSubmit({ path: { id } });
    submitted.value = true;
    void router.push('/submissions');
  } catch (e) {
    error.value = e instanceof Error ? e.message : '提交失败';
  }
}

function extractEditingId(): string | null {
  const m = location.pathname.match(/\/write\/(\d+)/);
  return m ? m[1]! : null;
}

function leaveGuard() {
  const guard = unsavedGuard(dirty.value);
  if (guard.block && !confirm(guard.message)) return;
  void router.push('/submissions');
}
</script>

<template>
  <div class="editor">
    <header>
      <button type="button" class="back" @click="leaveGuard">‹ 返回</button>
      <h1>{{ editingId ? '编辑投稿' : '写新内容' }}</h1>
      <span v-if="savedHint" class="saved" data-testid="saved-hint">{{ savedHint }}</span>
    </header>

    <div class="kinds">
      <button
        v-for="k in SUBMISSION_KINDS"
        :key="k.kind"
        type="button"
        :class="{ on: form.kind === k.kind }"
        :disabled="!!editingId"
        @click="form.kind = k.kind"
      >
        {{ k.label }}
      </button>
    </div>

    <input
      v-model="form.title"
      class="title"
      placeholder="标题（1–120 字）"
      data-testid="title"
      maxlength="120"
      @input="touch"
    />
    <div class="row">
      <select v-model="form.channel" data-testid="channel" @change="touch">
        <option v-for="c in CHANNELS.slice(1)" :key="c" :value="c">{{ c }}</option>
      </select>
      <input v-model="form.tags" placeholder="标签，逗号分隔" @input="touch" />
    </div>

    <!-- #42：评测类型增加 关联商品/来源证据/利益关系/专业复核提示 -->
    <section v-if="form.kind === 'REVIEW'" class="review-fields" data-testid="review-extra">
      <input v-model="form.relatedProduct" placeholder="关联商品（名称/链接）" @input="touch" />
      <input v-model="form.sourceEvidence" placeholder="来源证据（资料链接，必填）" data-testid="source-evidence" @input="touch" />
      <label class="interest">
        <input v-model="form.interestDeclared" type="checkbox" data-testid="review-interest" @change="touch" />
        利益关系声明：与品牌方无利益关联 / 已披露合作
      </label>
      <p class="hint">健康科普类内容将提示「专业复核」要求（发布前需具备资质的复核人确认——U94）。</p>
    </section>
    <textarea
      v-model="form.body"
      rows="14"
      placeholder="正文（不少于 10 字）"
      data-testid="body"
      @input="touch"
    />

    <p v-for="b in blockers" :key="b" class="err" data-testid="blocker">{{ b }}</p>
    <p v-if="error" class="err">{{ error }}</p>

    <div class="actions">
      <button type="button" class="ghost" data-testid="save-draft" @click="touch(); autosave()">存草稿</button>
      <button type="button" class="primary" data-testid="submit-review" @click="submit">提交审核</button>
    </div>
    <p class="tip">提交后进入「待审核 → 审核中 → 结果」时间线；30 秒自动保存（决议）。</p>
  </div>
</template>

<style scoped>
.editor {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 16px 64px;
  display: grid;
  gap: 12px;
}
header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.back {
  background: none;
  border: none;
  color: #ff7a2f;
}
.saved {
  margin-left: auto;
  color: #22c55e;
  font-size: 12px;
}
.kinds {
  display: flex;
  gap: 8px;
}
.kinds button {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: #f7f1ea;
  color: #7a6e63;
}
.kinds button.on {
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.title {
  height: 52px;
  border: 1px solid #f0e6dc;
  border-radius: 12px;
  padding: 0 16px;
  font-size: 18px;
}
.row {
  display: flex;
  gap: 8px;
}
.row select,
.row input {
  height: 40px;
  border: 1px solid #f0e6dc;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 14px;
}
.row input {
  flex: 1;
}
textarea {
  border: 1px solid #f0e6dc;
  border-radius: 12px;
  padding: 14px;
  font-family: inherit;
  font-size: 15px;
}
.actions {
  display: flex;
  gap: 12px;
}
.actions button {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: 999px;
  font-weight: 600;
}
.primary {
  background: #ff7a2f;
  color: #fff;
}
.ghost {
  background: #fff;
  color: #7a6e63;
  box-shadow: inset 0 0 0 1px #f0e6dc;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
.review-fields {
  display: grid;
  gap: 8px;
}
.review-fields input[type='text'] {
  height: 44px;
  border: 1px solid #f0e6dc;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 14px;
}
.interest {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
  color: #7a6e63;
}
.hint {
  background: #fff1e8;
  color: #b45309;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 12px;
}
.tip {
  color: #7a6e63;
  font-size: 12px;
}
</style>

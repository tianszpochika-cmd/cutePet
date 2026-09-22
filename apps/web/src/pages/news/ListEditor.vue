<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { LIST_EDITOR_STEPS, listEditorBlockers } from '../../domain/closedLoop';

const router = useRouter();
const form = reactive({ title: '', intro: '', interestDeclared: false });
const items = ref<{ id: number; name: string; reason: string; available: boolean }[]>([
  { id: 1, name: '幼猫无谷粮', reason: '性价比高', available: true },
  { id: 2, name: '互动逗猫棒', reason: '消耗精力', available: false }, // 演示：已下架
]);
const step = ref(0);
const availabilityChanged = ref(true); // 演示：提交前发现上下架变化
const revalidated = ref(false);
const attempted = ref(false);
const submitted = ref(false);

const blockers = () =>
  attempted.value
    ? listEditorBlockers({
        title: form.title,
        itemCount: items.value.length,
        interestDeclared: form.interestDeclared,
        itemsChangedAvailability: availabilityChanged.value,
        revalidated: revalidated.value,
      })
    : [];

function move(index: number, dir: -1 | 1) {
  const target = index + dir;
  if (target < 0 || target >= items.value.length) return;
  const arr = [...items.value];
  [arr[index], arr[target]] = [arr[target]!, arr[index]!];
  items.value = arr; // 拖动或按钮排序
}

function submit() {
  attempted.value = true;
  if (listEditorBlockers({
    title: form.title,
    itemCount: items.value.length,
    interestDeclared: form.interestDeclared,
    itemsChangedAvailability: availabilityChanged.value,
    revalidated: revalidated.value,
  }).length > 0) return;
  submitted.value = true;
}
</script>

<template>
  <div class="x13">
    <header>
      <button type="button" class="back" @click="router.push('/submissions')">‹ 创作中心</button>
      <h1>好物清单编辑（X13）</h1>
    </header>

    <nav class="steps">
      <span v-for="(s, i) in LIST_EDITOR_STEPS" :key="s" :class="{ on: i === step }" @click="step = i">{{ i + 1 }}.{{ s }}</span>
    </nav>

    <template v-if="step === 0">
      <label class="field">清单标题 <input v-model="form.title" data-testid="list-title" placeholder="如：幼猫第一份粮清单" /></label>
      <label class="field">导语 <textarea v-model="form.intro" rows="2" /></label>
    </template>

    <template v-else-if="step === 1 || step === 2 || step === 3">
      <ul class="items">
        <li v-for="(it, i) in items" :key="it.id" :class="{ off: !it.available }">
          <span>{{ i + 1 }}. {{ it.name }}<em v-if="!it.available">（已下架）</em></span>
          <span class="ops">
            <button type="button" @click="move(i, -1)">↑</button>
            <button type="button" @click="move(i, 1)">↓</button>
          </span>
        </li>
      </ul>
      <label v-if="step === 3" class="confirm">
        <input v-model="form.interestDeclared" type="checkbox" data-testid="interest" />
        声明：本清单来源与利益关系（无利益关联 / 已披露合作关系）
      </label>
    </template>

    <template v-else-if="step === 4">
      <section class="preview">
        <h2>{{ form.title || '（未填标题）' }}</h2>
        <p>{{ form.intro }}</p>
        <ol><li v-for="it in items" :key="it.id">{{ it.name }} — {{ it.reason }}</li></ol>
      </section>
      <p v-if="availabilityChanged" class="warn" data-testid="availability-warn">
        提交前检测到商品上下架变化，请先重新校验。
      </p>
      <button v-if="availabilityChanged && !revalidated" type="button" class="ghost" data-testid="revalidate" @click="revalidated = true">
        重新校验商品状态
      </button>
      <p v-else-if="availabilityChanged" class="ok">校验完成，可提交。</p>
    </template>

    <template v-else>
      <p class="muted">预览确认无误后提交审核（先审后发）。</p>
    </template>

    <p v-for="b in blockers()" :key="b" class="err" data-testid="blocker">{{ b }}</p>
    <p v-if="submitted" class="ok" data-testid="submitted">已提交审核，进度见「我的投稿」。</p>

    <div class="actions">
      <button type="button" class="ghost" @click="step = Math.max(0, step - 1)">上一步</button>
      <button v-if="step < LIST_EDITOR_STEPS.length - 1" type="button" class="primary" @click="step++">下一步</button>
      <button v-else type="button" class="primary" data-testid="submit" @click="submit">保存并提交</button>
    </div>
  </div>
</template>

<style scoped>
.x13 { max-width: 640px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.steps { display: flex; gap: 6px; overflow-x: auto; }
.steps span { font-size: 12px; color: #7a6e63; background: #f7f1ea; border-radius: 999px; padding: 5px 10px; white-space: nowrap; cursor: pointer; }
.steps span.on { background: #ff7a2f; color: #fff; font-weight: 600; }
.field { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
.field input, .field textarea { border: 1px solid #f0e6dc; border-radius: 12px; padding: 10px 12px; font-family: inherit; font-size: 15px; font-weight: 400; }
.items { list-style: none; padding: 0; display: grid; gap: 8px; margin: 0; }
.items li { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 12px 14px; display: flex; justify-content: space-between; align-items: center; font-size: 14px; }
.items li.off { color: #7a6e63; background: #faf7f3; }
.items em { color: #b45309; font-style: normal; font-size: 12px; }
.ops { display: flex; gap: 4px; }
.ops button { width: 30px; height: 30px; border: none; border-radius: 8px; background: #f7f1ea; color: #7a6e63; }
.confirm { display: flex; gap: 8px; font-size: 13px; color: #7a6e63; align-items: center; }
.preview { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; }
.preview h2 { margin: 0 0 6px; font-size: 16px; }
.preview p { color: #7a6e63; font-size: 13px; margin: 0 0 8px; }
.preview ol { padding-left: 18px; display: grid; gap: 4px; font-size: 14px; }
.warn { background: #fdecec; color: #b91c1c; border-radius: 8px; padding: 8px 12px; font-size: 13px; }
.actions { display: flex; gap: 10px; }
.actions button { flex: 1; height: 46px; border: none; border-radius: 999px; font-weight: 600; }
.primary { background: #ff7a2f; color: #fff; }
.ghost { background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.err { color: #ef4444; font-size: 13px; }
.ok { color: #22c55e; font-size: 13px; }
.muted { color: #7a6e63; font-size: 14px; }
</style>

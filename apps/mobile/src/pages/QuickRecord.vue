<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { recordSuccessMotion } from '../domain/mobileMotion';
import { recordFields, validateRecord, buildRecurrenceRule } from '../../../web/src/domain/pet.ts';

const route = useRoute();
const router = useRouter();
const kind = ref(String(route.query.kind ?? '体重'));
const values = ref<Record<string, string>>({});
const attempted = ref(false);
const success = ref(false);

const fields = computed(() => recordFields(kind.value));
const invalid = computed(() => (attempted.value ? validateRecord(kind.value, values.value) : null));

const motionAllowed = recordSuccessMotion(true, true); // 业务成功后才允许成功动效（闭环§4）

function save() {
  attempted.value = true;
  if (validateRecord(kind.value, values.value)) return;
  success.value = true;
  // 真实提交随本地阶段；下次提醒预览（疫苗/驱虫自动排期）
  void buildRecurrenceRule;
}
</script>

<template>
  <div class="m-record">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>{{ kind }}记录</h1>
    </header>

    <template v-if="!success">
      <label v-for="f in fields" :key="f.key">
        {{ f.label }}{{ f.required ? ' *' : '' }}
        <input
          :type="f.type === 'date' ? 'date' : f.type === 'number' ? 'number' : 'text'"
          :step="f.type === 'number' ? '0.1' : undefined"
          :max="f.type === 'date' ? new Date().toISOString().slice(0, 10) : undefined"
          :data-testid="f.key"
          @input="values[f.key] = ($event.target as HTMLInputElement).value"
        />
      </label>

      <p v-if="invalid" class="err" data-testid="error">
        {{ invalid.startsWith('REQUIRED:') ? `请填写 ${invalid.slice(9)}` : invalid }}
      </p>

      <button type="button" class="primary" data-testid="save" @click="save">保存</button>
    </template>

    <!-- 记成功动效（业务成功后播放） -->
    <section v-else class="success" :class="{ pop: motionAllowed }" data-testid="success">
      <span class="check">✅</span>
      <h2>已保存</h2>
      <p>{{ kind }}记录进入时间线{{ kind === '疫苗' || kind === '驱虫' ? '，下次提醒已自动排期' : '' }}</p>
      <div class="actions">
        <button type="button" class="primary" @click="router.push('/pets')">返回宠物</button>
        <button type="button" class="ghost" @click="success = false">再记一条</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.m-record { padding: 16px; display: grid; gap: 14px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; font-size: 20px; color: #ff7a2f; }
h1 { margin: 0; font-size: 18px; }
label { display: grid; gap: 8px; font-size: 14px; font-weight: 600; background: #fff; border-radius: 16px; padding: 14px; box-shadow: inset 0 0 0 1px #f0e6dc; }
input { height: 46px; border: 1px solid #f0e6dc; border-radius: 12px; padding: 0 14px; font-size: 16px; font-weight: 400; }
.primary { height: 52px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-size: 16px; font-weight: 700; min-height: 44px; }
.ghost { height: 52px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; min-height: 44px; }
.err { color: #ef4444; font-size: 13px; margin: 0; }
.success { text-align: center; display: grid; gap: 10px; justify-items: center; padding: 32px 0; }
.success.pop { animation: pop 400ms cubic-bezier(0.34,1.56,0.64,1); }
@keyframes pop { 0% { transform: scale(.9); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
.check { font-size: 56px; }
.success h2 { margin: 0; color: #22c55e; }
.success p { margin: 0; color: #7a6e63; font-size: 14px; }
.actions { display: flex; gap: 12px; width: 100%; }
.actions button { flex: 1; }
</style>

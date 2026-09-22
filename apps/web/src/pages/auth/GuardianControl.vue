<script setup lang="ts">
import { ref } from 'vue';
import { guardianControlFlow, GUARDIAN_REVOKE_EFFECTS } from '../../domain/closedLoop';

const phone = ref('');
const linked = ref(true); // 演示：是否存在关联账号
const result = ref<{ ok: boolean; display: string; leak: boolean } | null>(null);
const consentState = ref('CONSENTED');

function verify() {
  // X16：无关联不泄露账号存在性
  result.value = guardianControlFlow({ guardianPhoneMatches: phone.value === '13900000001', minorLinked: linked.value });
}

function revoke() {
  if (!confirm('撤回同意后将：限制孩子账号会话、关闭其推送。确认撤回？')) return;
  consentState.value = 'REVOKED';
}
</script>

<template>
  <div class="x16">
    <header>
      <button type="button" class="back" onclick="history.back()">‹ 返回</button>
      <h1>监护人控制（X16）</h1>
    </header>

    <section class="card">
      <p class="hint">从登录页「监护人服务」进入。仅需监护人手机号核验；不会要求登录儿童账号。</p>
      <label class="field">监护人手机号（演示：13900000001）
        <input v-model="phone" data-testid="g-phone" placeholder="11 位手机号" />
      </label>
      <label class="switch"><input v-model="linked" type="checkbox" /> 存在关联的儿童账号（演示开关）</label>
      <button type="button" class="primary" data-testid="verify" @click="verify">核验并查询</button>
      <p v-if="result" class="result" data-testid="result" :class="{ warn: !result.ok }">
        {{ result.display }}
      </p>
      <p v-if="result && result.leak" class="err">不应出现：泄露了账号存在性</p>
    </section>

    <section v-if="result?.ok" class="card">
      <h2>同意状态</h2>
      <span :class="['badge', consentState === 'CONSENTED' ? 'ok' : 'off']">{{ consentState === 'CONSENTED' ? '已同意' : '已撤回' }}</span>
      <button type="button" class="danger" data-testid="revoke" @click="revoke">撤回同意</button>
      <ul class="effects">
        <li v-for="e in GUARDIAN_REVOKE_EFFECTS" :key="e">· {{ e }}</li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.x16 { max-width: 520px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 15px; }
.hint { color: #7a6e63; font-size: 13px; margin: 0; }
.field { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
.field input { height: 44px; border: 1px solid #f0e6dc; border-radius: 12px; padding: 0 12px; font-weight: 400; }
.switch { display: flex; gap: 8px; font-size: 13px; color: #7a6e63; align-items: center; }
.primary { height: 46px; border: none; border-radius: 999px; background: #9b8cff; color: #fff; font-weight: 600; }
.result { background: #e7f8ef; color: #15803d; border-radius: 8px; padding: 10px 12px; font-size: 13px; }
.result.warn { background: #fdecec; color: #b91c1c; }
.badge { border-radius: 999px; padding: 3px 12px; font-size: 13px; font-weight: 600; justify-self: start; }
.badge.ok { background: #e7f8ef; color: #22c55e; }
.badge.off { background: #f0e6dc; color: #7a6e63; }
.danger { height: 44px; border: none; border-radius: 999px; background: #ef4444; color: #fff; font-weight: 600; }
.effects { margin: 0; padding-left: 16px; font-size: 13px; color: #7a6e63; display: grid; gap: 4px; }
.err { color: #ef4444; font-size: 13px; }
</style>

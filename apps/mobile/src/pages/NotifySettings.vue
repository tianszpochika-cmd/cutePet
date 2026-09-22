<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { CHANNEL_META, nextPermissionState, webNotifyBlockers, channelPanel, type PermissionState } from '../domain/notify';

const router = useRouter();
const privacyAgreed = ref(true);
const permission = ref<PermissionState>('default');
const secureContext = ref(true);

const panels = () => ({
  WEB: channelPanel('WEB_NOTIFICATION', permission.value, privacyAgreed.value),
  INAPP: channelPanel('IN_APP', permission.value, privacyAgreed.value),
  VENDOR: channelPanel('VENDOR_PUSH', permission.value, privacyAgreed.value),
});

async function requestWeb() {
  const blockers = webNotifyBlockers({
    privacyAgreed: privacyAgreed.value,
    permission: permission.value,
    secureContext: secureContext.value,
  });
  if (blockers.length > 0) return;
  // 真实 Notification.requestPermission 随本地阶段（此处驱动同一状态机）
  const next = nextPermissionState(permission.value, 'userGrant');
  if (next !== 'STAY') permission.value = next;
}

function denyDemo() {
  const next = nextPermissionState(permission.value, 'userDeny');
  if (next !== 'STAY') permission.value = next;
}
</script>

<template>
  <div class="m-notify">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>通知设置（三通道）</h1>
    </header>

    <section class="channel" data-testid="web-channel">
      <div class="head">
        <strong>{{ CHANNEL_META.WEB_NOTIFICATION.label }}</strong>
        <span :class="['state', permission]">{{ permission }}</span>
      </div>
      <p class="note">{{ CHANNEL_META.WEB_NOTIFICATION.note }}</p>
      <div class="actions">
        <button type="button" class="primary" data-testid="request-permission" @click="requestWeb">请求授权</button>
        <button type="button" class="ghost" @click="denyDemo">模拟用户拒绝</button>
        <button type="button" class="ghost" @click="permission = 'default'">重置</button>
      </div>
      <p v-if="panels().WEB.hint && permission !== 'granted'" class="hint" data-testid="web-hint">{{ panels().WEB.hint }}</p>
    </section>

    <section class="channel" data-testid="inapp-channel">
      <div class="head">
        <strong>{{ CHANNEL_META.IN_APP.label }}</strong>
        <span class="state granted">always on</span>
      </div>
      <p class="note">{{ panels().INAPP.hint }}（提醒/互动/审核结果/家庭/系统 五类）</p>
    </section>

    <section class="channel stub" data-testid="vendor-channel">
      <div class="head">
        <strong>{{ CHANNEL_META.VENDOR_PUSH.label }}</strong>
        <span class="state stub">stub</span>
      </div>
      <p class="note">{{ CHANNEL_META.VENDOR_PUSH.note }}</p>
    </section>

    <label class="row">
      <span>隐私已同意（未同意则可选通知能力不加载）</span>
      <input v-model="privacyAgreed" type="checkbox" data-testid="privacy-toggle" />
    </label>

    <router-link class="link" to="/messages">去消息中心 →</router-link>
  </div>
</template>

<style scoped>
.m-notify { padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; color: #ff7a2f; font-size: 20px; }
h1 { margin: 0; font-size: 17px; }
.channel { background: #fff; border-radius: 18px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.channel.stub { background: #faf7f3; }
.head { display: flex; justify-content: space-between; align-items: center; }
.state { border-radius: 999px; padding: 3px 12px; font-size: 11px; font-weight: 700; text-transform: uppercase; }
.state.default { background: #fff1e8; color: #b45309; }
.state.granted { background: #e7f8ef; color: #15803d; }
.state.denied { background: #fdecec; color: #b91c1c; }
.state.stub { background: #f0e6dc; color: #7a6e63; }
.note { margin: 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.actions button { border: none; border-radius: 999px; padding: 11px 16px; font-size: 13px; font-weight: 600; min-height: 44px; }
.primary { background: #ff7a2f; color: #fff; }
.ghost { background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.hint { background: #fdecec; color: #b91c1c; border-radius: 10px; padding: 8px 12px; font-size: 12px; margin: 0; }
.row { background: #fff; border-radius: 14px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; display: flex; justify-content: space-between; align-items: center; font-size: 14px; gap: 12px; min-height: 48px; }
.link { color: #ff7a2f; text-decoration: none; font-size: 14px; font-weight: 600; padding: 12px 0; }
</style>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import { api } from '@cutepet/api-client';
import { JOIN_SCOPE_COPY } from '../../domain/family';

const route = useRoute();
const code = ref(typeof route.query.code === 'string' ? route.query.code : '');
const acknowledged = ref(false);
const busy = ref(false);
const done = ref(false);
const error = ref('');

async function join() {
  const inviteCode = code.value.trim();
  if (!inviteCode) { error.value = '请输入邀请码'; return; }
  if (!acknowledged.value) { error.value = '请先阅读并确认共享范围'; return; }
  if (busy.value || done.value) return;
  busy.value = true;
  error.value = '';
  try {
    const result = (await api.familyJoin({ body: { code: inviteCode } })) as {
      familyId?: number | string;
      role?: string;
      ownPetsShared?: boolean;
    };
    if (!result || !/^\d+$/.test(String(result.familyId ?? '')) || result.role !== 'MEMBER' || result.ownPetsShared !== false) {
      throw new Error('未收到可靠的加入确认，请返回家庭概况核对后再操作');
    }
    done.value = true;
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '加入失败，邀请码可能已失效或你已有家庭';
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <div class="join-page">
    <nav><router-link to="/family">← 返回家庭</router-link></nav>
    <header><p class="eyebrow">加入家庭</p><h1>先看清范围，再决定是否加入。</h1><p>加入家庭不会把你自己的宠物自动分享给别人。</p></header>
    <section v-if="done" class="success" data-testid="join-ok" role="status"><span aria-hidden="true">✓</span><h2>已加入家庭</h2><p>这次加入已由平台确认。你自己的宠物仍保持私有；已共享宠物的可见范围由所有者设置。</p><router-link to="/family">查看家庭概况 →</router-link></section>
    <template v-else>
      <section class="scope" data-testid="join-scope"><h2>加入后，你可以看到什么？</h2><p>{{ JOIN_SCOPE_COPY }}</p><ul><li>可见范围取决于每只宠物的共享设置。</li><li>默认只读；需要处理待办时，由宠物所有者单独授权。</li><li>收藏、草稿和私人联系方式不会随家庭关系共享。</li></ul></section>
      <form class="join-form" @submit.prevent="join"><label for="invite-code">家人发来的邀请码</label><input id="invite-code" v-model="code" data-testid="join-code" autocomplete="off" placeholder="输入邀请码" /><p class="caution">当前无法在提交前显示家庭名称与管理员。请先向邀请人核对邀请码来源；一人同时只能加入一个家庭。</p><label class="agree"><input v-model="acknowledged" type="checkbox" /><span>我已阅读共享范围，并确认加入家人的家庭</span></label><p v-if="error" class="error" role="alert">{{ error }}</p><button type="submit" class="primary" :disabled="busy || !acknowledged" data-testid="join-btn">{{ busy ? '加入中…' : '确认加入' }}</button></form>
    </template>
  </div>
</template>

<style scoped>
.join-page{width:min(630px,calc(100% - 32px));margin:0 auto;padding:28px 0 90px;display:grid;gap:20px}nav a{display:inline-flex;align-items:center;min-height:44px;color:#5c3c8c;text-decoration:none;font-weight:700}.eyebrow{margin:0 0 8px;color:#73539a;font-size:12px;font-weight:800;letter-spacing:.08em}header h1{margin:0;font-size:clamp(29px,4vw,42px);line-height:1.2}header>p:last-child{margin:12px 0 0;color:#6d6259}.scope,.join-form,.success{padding:25px;border:1px solid #e9ded3;border-radius:20px;background:#fff}.scope{background:#f4eefb}.scope h2,.success h2{margin:0 0 10px;font-size:20px}.scope p,.scope li,.success p{color:#5d4a70;font-size:14px;line-height:1.8}.scope p{margin:0}.scope ul{display:grid;gap:7px;margin:15px 0 0;padding-left:19px}.join-form{display:grid;gap:12px}.join-form>label:first-child{font-size:14px;font-weight:700}.join-form>input{min-height:49px;padding:0 15px;border:1px solid #d9cce5;border-radius:12px;font-size:16px}.caution{margin:0;color:#6d6259;font-size:13px;line-height:1.7}.agree{display:flex;align-items:center;gap:10px;padding:8px 0;color:#443a35;font-size:14px}.agree input{width:18px;height:18px;accent-color:#5c3c8c}.primary{min-height:48px;border:0;border-radius:999px;background:#5c3c8c;color:#fff;font-weight:750;cursor:pointer}.primary:disabled{opacity:.55;cursor:not-allowed}.error{margin:0;color:#a43636;font-size:13px}.success{display:grid;justify-items:start;gap:10px;background:#f2f9f4}.success>span{display:grid;place-items:center;width:48px;height:48px;border-radius:50%;background:#dcf3e2;color:#176c3f;font-size:24px}.success p{margin:0}.success a{display:inline-flex;align-items:center;min-height:44px;color:#176c3f;font-weight:750;text-decoration:none}
</style>

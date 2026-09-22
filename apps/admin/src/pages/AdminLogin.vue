<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { PERMISSIONS, loginBlockersAdmin } from '../domain/workbench';

const router = useRouter();
const username = ref('admin');
const password = ref('');
const granted = ref<string[]>(['dashboard.view.all']); // 登录后由 /me 拉取；dev 预览
const error = ref('');

function login() {
  const blockers = loginBlockersAdmin({
    username: username.value,
    password: password.value,
    granted: granted.value,
  });
  if (blockers.length > 0) {
    error.value = blockers.join(' / ');
    return;
  }
  void router.push('/dashboard');
}

function quickRole(role: string) {
  // dev 快捷：切换预置角色预览权限（真实鉴权随本地阶段接 IAM）
  const preset: Record<string, string[]> = {
    reviewer: ['review.article', 'comment.manage', 'report.handle', 'user.view', 'dashboard.view.all'],
    admin: [...PERMISSIONS],
  };
  granted.value = preset[role] ?? granted.value;
}
</script>

<template>
  <div class="login">
    <div class="card">
      <h1>cutePet 管理端</h1>
      <label>账号<input v-model="username" data-testid="username" /></label>
      <label>密码<input v-model="password" type="password" data-testid="password" placeholder="≥6 位" /></label>
      <div class="quick">
        <span>dev 角色预览：</span>
        <button type="button" @click="quickRole('reviewer')">审核员</button>
        <button type="button" @click="quickRole('admin')">管理员</button>
      </div>
      <p v-if="error" class="err" data-testid="error">{{ error }}</p>
      <button type="button" class="primary" data-testid="login" @click="login">登录工作台</button>
    </div>
  </div>
</template>

<style scoped>
.login { min-height: 90vh; display: grid; place-items: center; }
.card { width: min(380px, calc(100vw - 32px)); background: #fff; border-radius: 20px; box-shadow: 0 12px 32px rgba(43,33,24,.1); padding: 32px 28px; display: grid; gap: 14px; }
h1 { margin: 0; font-size: 20px; text-align: center; }
label { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
input { height: 44px; border: 1px solid #f0e6dc; border-radius: 12px; padding: 0 14px; font-size: 15px; font-weight: 400; }
.quick { display: flex; gap: 8px; align-items: center; font-size: 12px; color: #7a6e63; }
.quick button { height: 28px; padding: 0 10px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; font-size: 12px; }
.primary { height: 46px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; }
.err { color: #ef4444; font-size: 13px; margin: 0; }
</style>

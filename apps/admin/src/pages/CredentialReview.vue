<script setup lang="ts">
import { ref } from 'vue';
import { credentialTransition, CREDENTIAL_MATERIAL_VISIBLE_TO, directPublishRight, type CredentialState } from '../domain/governance';

const apps = ref([
  { userId: 201, org: '毛球宠物店', state: 'APPLY' as CredentialState, expiresAt: '2027-09-22', material: '营业执照.pdf / 法人身份证.jpg' },
  { userId: 202, org: '爱心领养站', state: 'APPROVED' as CredentialState, expiresAt: '2026-12-31', material: '机构登记证.pdf' },
  { userId: 203, org: '快乐训练营', state: 'REJECTED' as CredentialState, expiresAt: '', material: '材料不清晰.png' },
]);
const message = ref('');

function act(userId: number, action: 'approve' | 'reject' | 'revoke' | 'expire') {
  const app = apps.value.find((a) => a.userId === userId);
  if (!app) return;
  const next = credentialTransition(app.state, action);
  if (next === null) {
    message.value = `#${userId}：当前状态 ${app.state} 不接受 ${action}`;
    return;
  }
  app.state = next;
  message.value = `#${userId} → ${next}${next === 'APPROVED' ? '（直发资格生效，领养仍人工审 U89）' : ''}（activity.manage，操作者已留痕）`;
}
</script>

<template>
  <div class="cred">
    <p class="meta">{{ CREDENTIAL_MATERIAL_VISIBLE_TO }}</p>
    <table>
      <thead><tr><th>机构</th><th>状态</th><th>有效期</th><th>材料</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="a in apps" :key="a.userId" :data-testid="`cred-${a.userId}`">
          <td>{{ a.org }}（#{{ a.userId }}）</td>
          <td><span :class="['state', a.state.toLowerCase()]">{{ a.state }}</span></td>
          <td>{{ a.expiresAt || '—' }}</td>
          <td class="material">{{ a.material }}</td>
          <td class="ops">
            <template v-if="a.state === 'APPLY'">
              <button type="button" class="chip" data-testid="approve" @click="act(a.userId, 'approve')">通过</button>
              <button type="button" class="chip danger" @click="act(a.userId, 'reject')">驳回</button>
            </template>
            <template v-else-if="a.state === 'APPROVED'">
              <button type="button" class="chip danger" @click="act(a.userId, 'revoke')">撤销</button>
              <button type="button" class="chip" @click="act(a.userId, 'expire')">标记到期</button>
            </template>
            <span v-else class="dim">直发权：{{ directPublishRight(a.state) ? '生效' : '无' }}</span>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-if="message" class="msg" data-testid="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.cred { display: grid; gap: 12px; }
table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 12px; overflow: hidden; }
th, td { text-align: left; padding: 10px 12px; font-size: 13px; border-bottom: 1px solid #f0e6dc; }
th { background: #faf7f3; color: #7a6e63; font-size: 12px; }
.state { border-radius: 999px; padding: 2px 10px; font-size: 11px; font-weight: 600; }
.state.apply { background: #fff1e8; color: #b45309; }
.state.approved { background: #e7f8ef; color: #15803d; }
.state.rejected, .state.revoked, .state.expired { background: #fdecec; color: #b91c1c; }
.material { color: #7a6e63; font-size: 12px; }
.ops { display: flex; gap: 6px; }
.chip { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 5px 12px; font-size: 12px; cursor: pointer; }
.chip.danger { background: #fdecec; color: #b91c1c; }
.dim { color: #a89b8f; font-size: 12px; }
.meta { margin: 0; color: #7a6e63; font-size: 13px; }
.msg { background: #e7f8ef; color: #15803d; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin: 0; }
</style>

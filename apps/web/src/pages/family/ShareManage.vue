<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { visibleShares, shareChangeNotifyTargets, OWNER_VERIFY_COPY, type ShareMemberView } from '../../domain/closedLoop';

const router = useRouter();
const isOwner = ref(true);
const viewerId = ref(1);

const before: ShareMemberView[] = [
  { userId: 1, petId: 10, share: 'MANAGE' },
  { userId: 2, petId: 10, share: 'READONLY' },
  { userId: 3, petId: 10, share: null },
];
const views = ref<ShareMemberView[]>(before.map((v) => ({ ...v })));
const visible = () => visibleShares(views.value, viewerId.value, isOwner.value);
const pendingNotices = ref<number[]>([]);

function toggleShare(userId: number) {
  const v = views.value.find((x) => x.userId === userId);
  if (!v) return;
  const after = views.value.map((x) =>
    x.userId === userId
      ? { ...x, share: x.share === 'MANAGE' ? ('READONLY' as const) : x.share === 'READONLY' ? null : ('MANAGE' as const) }
      : x,
  );
  pendingNotices.value = shareChangeNotifyTargets(views.value, after); // 变更通知受影响成员
  views.value = after;
}
</script>

<template>
  <div class="x03">
    <header>
      <button type="button" class="back" @click="router.back()">‹ 返回</button>
      <h1>共享授权（X03）</h1>
    </header>

    <p class="verify">{{ OWNER_VERIFY_COPY }}</p>

    <div class="switch">
      <label><input v-model="isOwner" type="radio" :value="true" /> 所有者视角</label>
      <label><input v-model="isOwner" type="radio" :value="false" /> 成员视角（仅见自己）</label>
    </div>

    <ul class="list">
      <li v-for="v in visible()" :key="v.userId" class="row" :data-testid="`share-${v.userId}`">
        <span>成员 #{{ v.userId }}</span>
        <select v-if="isOwner" :value="v.share ?? ''" @change="toggleShare(v.userId)">
          <option value="">未共享（私有）</option>
          <option value="READONLY">只读</option>
          <option value="MANAGE">可管理</option>
        </select>
        <em v-else>{{ v.share === 'MANAGE' ? '可管理' : v.share === 'READONLY' ? '只读' : '—' }}</em>
      </li>
      <li v-if="visible().length === 1 && !isOwner" class="muted">
        （其他成员的档位对你不可见——每人只看自己的权限）
      </li>
    </ul>

    <p v-if="pendingNotices.length > 0" class="notice" data-testid="notify">
      已通知受影响成员：{{ pendingNotices.map((id) => `#${id}`).join('、') }}
    </p>
  </div>
</template>

<style scoped>
.x03 { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.verify { background: #f5f3ff; color: #6d5bd0; border-radius: 12px; padding: 12px; font-size: 13px; }
.switch { display: flex; gap: 14px; font-size: 14px; }
.list { list-style: none; padding: 0; display: grid; gap: 8px; margin: 0; }
.row { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; display: flex; justify-content: space-between; align-items: center; font-size: 14px; }
select { height: 36px; border-radius: 999px; border: 1px solid #f0e6dc; padding: 0 10px; }
em { color: #7a6e63; font-style: normal; }
.notice { background: #e7f8ef; color: #15803d; border-radius: 8px; padding: 10px 12px; font-size: 13px; }
.muted { color: #7a6e63; font-size: 13px; list-style: none; }
</style>

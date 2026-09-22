<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  OFFLINE_COPY,
  conflictOptions,
  applyConflictChoice,
  syncRowCopy,
  LOGOUT_SYNC_CHOICES,
  type OfflineStatus,
} from '../domain/mobileMotion';

const router = useRouter();
const status = ref<OfflineStatus>('CONFLICT');
const rows = ref<{ id: number; label: string; state: 'PENDING' | 'FAILED' | 'SUCCESS' | 'LOST_RIGHT' }[]>([
  { id: 1, label: '体重 12.5kg（今天）', state: 'PENDING' },
  { id: 2, label: '疫苗记录', state: 'FAILED' },
  { id: 3, label: '驱虫记录', state: 'SUCCESS' },
]);
const notice = ref('');

const banner = computed(() => OFFLINE_COPY[status.value]);
const options = () => conflictOptions(status.value);

function choose(choice: 'DISCARD_LOCAL' | 'CONFIRM_OVERWRITE') {
  notice.value = applyConflictChoice(choice).notice;
  status.value = 'ONLINE';
}
</script>

<template>
  <div class="m-sync">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>离线与同步（T10.6）</h1>
    </header>

    <section :class="['banner', banner.tone]" data-testid="banner">{{ banner.banner }}</section>

    <!-- 冲突双选：永不自动覆盖（#32） -->
    <section v-if="options().length" class="conflict" data-testid="conflict">
      <h2>选择保留哪一份</h2>
      <button
        v-for="o in options()"
        :key="o.choice"
        type="button"
        :class="o.danger ? 'danger' : 'ghost'"
        :data-testid="`choice-${o.choice}`"
        @click="choose(o.choice)"
      >
        {{ o.label }}
      </button>
    </section>

    <ul class="list">
      <li v-for="r in rows" :key="r.id" :data-testid="`sync-${r.id}`">
        <div>
          <strong>{{ r.label }}</strong>
          <p>{{ syncRowCopy(r.state) }}</p>
        </div>
        <button v-if="r.state === 'FAILED'" type="button" class="retry" @click="r.state = 'SUCCESS'">重试</button>
      </li>
    </ul>

    <section class="logout">
      <h2>登出前的选择</h2>
      <button v-for="c in LOGOUT_SYNC_CHOICES" :key="c" type="button" class="ghost" @click="notice = `已选择：${c}`">
        {{ c }}
      </button>
    </section>

    <p v-if="notice" class="notice" data-testid="notice">{{ notice }}</p>
  </div>
</template>

<style scoped>
.m-sync { padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; color: #ff7a2f; font-size: 20px; }
h1 { margin: 0; font-size: 17px; }
.banner { border-radius: 14px; padding: 12px 14px; font-size: 13px; line-height: 1.7; }
.banner.warn { background: #fff1e8; color: #b45309; }
.banner.error { background: #fdecec; color: #b91c1c; }
.banner.ok { background: #e7f8ef; color: #15803d; }
.conflict { background: #fff; border-radius: 18px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.conflict h2 { margin: 0; font-size: 15px; }
.conflict button { border: none; border-radius: 14px; padding: 14px; font-size: 14px; font-weight: 600; text-align: left; min-height: 48px; }
.ghost { background: #f7f1ea; color: #2b2118; }
.danger { background: #fdecec; color: #b91c1c; }
.list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
.list li { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; display: flex; justify-content: space-between; align-items: center; gap: 10px; min-height: 44px; }
.list p { margin: 4px 0 0; color: #7a6e63; font-size: 12px; }
.retry { border: none; background: #fff1e8; color: #ff7a2f; border-radius: 999px; padding: 10px 16px; font-weight: 600; min-height: 44px; }
.logout { background: #fff; border-radius: 18px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.logout h2 { margin: 0; font-size: 15px; }
.logout button { border: none; border-radius: 14px; padding: 14px; font-size: 14px; text-align: left; min-height: 48px; }
.notice { background: #eaf1ff; color: #2563eb; border-radius: 12px; padding: 12px; font-size: 13px; margin: 0; }
</style>

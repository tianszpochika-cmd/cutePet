<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { activityPublishBlockers, publishTargetState, type ActivityInput } from '../../domain/closedLoop';

const router = useRouter();
const form = reactive({
  type: '活动' as '活动' | '领养',
  title: '',
  beginsAt: '',
  endsAt: '',
  deadline: '',
  quota: 20,
  orgState: 'NONE' as ActivityInput['orgState'],
});

const attempted = ref(false);
const previewed = ref(false);

const input = computed<ActivityInput>(() => ({
  type: form.type,
  title: form.title,
  beginsAt: form.beginsAt,
  endsAt: form.endsAt,
  deadline: form.deadline || null,
  quota: form.quota,
  orgState: form.orgState,
}));

const blockers = computed(() => (attempted.value ? activityPublishBlockers(input.value) : []));

function submit() {
  attempted.value = true;
  if (activityPublishBlockers(input.value).length > 0) return;
  previewed.value = true;
}
</script>

<template>
  <div class="x09">
    <header>
      <button type="button" class="back" @click="router.push('/me/activity-center')">‹ 活动中心</button>
      <h1>发布活动 / 领养（X09）</h1>
    </header>

    <nav class="types">
      <button type="button" :class="{ on: form.type === '活动' }" @click="form.type = '活动'">活动</button>
      <button type="button" :class="{ on: form.type === '领养' }" @click="form.type = '领养'">领养</button>
    </nav>

    <label class="field">标题 <input v-model="form.title" data-testid="title" placeholder="如：秋日遛宠嘉年华" /></label>
    <label class="field">开始 <input v-model="form.beginsAt" /></label>
    <label class="field">结束 <input v-model="form.endsAt" /></label>
    <label class="field">报名截止（可空） <input v-model="form.deadline" /></label>
    <label class="field">名额（0=不限） <input v-model.number="form.quota" type="number" data-testid="quota" /></label>

    <p class="hint">这里可检查时间、报名窗口与名额。机构认证状态应由平台读取，当前不能自行选择。活动发布接口尚未接通，表单不会提交到平台。</p>

    <p v-for="b in blockers" :key="b" class="err" data-testid="blocker">{{ b }}</p>

    <button type="button" class="primary" data-testid="publish" @click="submit">检查发布条件</button>
    <p v-if="previewed" class="ok" data-testid="submit-result">表单检查通过。按当前状态预计进入{{ publishTargetState(input) === 'PUBLISHED' ? '发布' : '审核' }}流程；尚未提交或发布。</p>
  </div>
</template>

<style scoped>
.x09 { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #b85111; }
.types { display: flex; gap: 8px; }
.types button { height: 34px; padding: 0 16px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; }
.types button.on { background: #b85111; color: #fff; font-weight: 600; }
.types select { margin-left: auto; height: 34px; border-radius: 999px; border: 1px solid #f0e6dc; }
.field { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
.field input { height: 44px; border: 1px solid #f0e6dc; border-radius: 12px; padding: 0 12px; font-size: 14px; font-weight: 400; }
.hint { background: #f5f3ff; color: #6d5bd0; border-radius: 8px; padding: 10px 12px; font-size: 12px; line-height: 1.7; }
.primary { height: 48px; border: none; border-radius: 999px; background: #b85111; color: #fff; font-weight: 600; }
.ok { color: #22c55e; font-size: 14px; font-weight: 600; }
.err { color: #ef4444; font-size: 13px; }
</style>

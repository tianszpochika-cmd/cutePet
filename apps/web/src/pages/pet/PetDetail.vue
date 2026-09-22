<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const petId = String(route.params.id);
const tab = ref<'timeline' | 'records' | 'reminders' | 'profile'>('timeline');

const tabs = computed(() =>
  [
    { id: 'timeline', label: '时间线' },
    { id: 'records', label: '健康记录' },
    { id: 'reminders', label: '提醒' },
    { id: 'profile', label: '档案资料' },
  ] as const,
);
</script>

<template>
  <div class="pet-detail">
    <header>
      <button type="button" class="back" @click="router.push('/pets')">‹ 返回</button>
      <h1>宠物详情 <span class="id">#{{ petId }}</span></h1>
      <div class="actions">
        <button type="button" class="primary" data-testid="go-record" @click="router.push(`/pets/${petId}/record`)">
          ＋ 记录
        </button>
        <button type="button" class="ghost" @click="router.push(`/pets/${petId}/weights`)">曲线</button>
        <button type="button" class="ghost" @click="router.push(`/pets/${petId}/reminders`)">提醒</button>
      </div>
    </header>

    <nav class="tabs">
      <button
        v-for="t in tabs"
        :key="t.id"
        type="button"
        :class="{ on: tab === t.id }"
        @click="tab = t.id"
      >
        {{ t.label }}
      </button>
    </nav>

    <section class="panel">
      <p v-if="tab === 'timeline'" data-testid="panel-timeline" class="muted">
        时间线：体重 / 健康记录 / 提醒完成 / 里程碑按时间聚合（类型筛选、接口接通后填充）。
      </p>
      <p v-else-if="tab === 'records'" data-testid="panel-records" class="muted">
        健康记录：疫苗 / 驱虫 / 体检 / 就医 / 用药 / 过敏六类（＋记录页录入，疫苗/驱虫自动排期提醒）。
      </p>
      <p v-else-if="tab === 'reminders'" data-testid="panel-reminders" class="muted">
        提醒中心：待处理 / 已启用 / 已完成 / 已过期分组
        <button type="button" class="link" @click="router.push(`/pets/${petId}/reminders`)">进入 →</button>
      </p>
      <p v-else data-testid="panel-profile" class="muted">
        档案资料：昵称 / 物种 / 品种 / 生日 / 归档与删除（软删 30 天，二次确认文案由 domain 生成）。
      </p>
    </section>
  </div>
</template>

<style scoped>
.pet-detail {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px;
}
header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
h1 {
  font-size: 22px;
}
.id {
  color: #7a6e63;
  font-size: 14px;
  font-weight: 400;
}
.actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}
.actions button {
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
}
.primary {
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.ghost {
  background: #fff;
  color: #7a6e63;
  box-shadow: inset 0 0 0 1px #f0e6dc;
}
.back {
  background: none;
  border: none;
  color: #ff7a2f;
  cursor: pointer;
}
.tabs {
  display: flex;
  gap: 4px;
  background: #f7f1ea;
  border-radius: 999px;
  padding: 4px;
  margin: 16px 0;
}
.tabs button {
  flex: 1;
  height: 36px;
  border: none;
  border-radius: 999px;
  background: none;
  color: #7a6e63;
  cursor: pointer;
}
.tabs button.on {
  background: #fff;
  color: #ff7a2f;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(43, 33, 24, 0.06);
}
.panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  padding: 20px;
  min-height: 200px;
}
.muted {
  color: #7a6e63;
  font-size: 14px;
  line-height: 1.7;
}
.link {
  background: none;
  border: none;
  color: #ff7a2f;
  cursor: pointer;
}
</style>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { starsPercent, distanceLabel } from '../../domain/explore';

const route = useRoute();
const router = useRouter();
const poiId = String(route.params.id);

interface Poi {
  id: number;
  name: string;
  type: string;
  city: string;
  address: string;
  phone: string;
  openHours: string;
  attrs: string;
  state: string;
  avg: number | null;
  reviewCount: number;
  navClicks: number;
}

const poi = ref<Poi | null>(null);
const error = ref('');

onMounted(async () => {
  try {
    poi.value = (await api.poiGet({ path: { id: poiId } })) as unknown as Poi;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 explore-service）';
  }
});

async function navClick() {
  try {
    await api.poiNavClick({ path: { id: poiId } });
    alert('已记录导航点击（L5 转化），调起系统地图属本地/部署阶段');
  } catch {
    error.value = '导航计数失败';
  }
}

function phoneVisible(): boolean {
  // 决议：登录可见完整电话；游客脱敏（登录态接线随本地阶段）
  return true;
}
void distanceLabel;
</script>

<template>
  <div class="poi-detail">
    <header>
      <button type="button" class="back" @click="router.push('/explore')">‹ 返回地图</button>
      <h1 v-if="poi">{{ poi.name }}</h1>
    </header>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-else-if="!poi" class="muted">加载中…</p>

    <template v-else>
      <p v-if="poi.state === 'CLOSED'" class="closed" data-testid="closed-banner">该场所已标记停业</p>

      <div class="score">
        <template v-if="poi.avg !== null">
          <span class="stars" :style="{ width: `${starsPercent(poi.avg)}%` }" />
          <strong>{{ poi.avg }}</strong>
          <span class="muted">（{{ poi.reviewCount }} 条）</span>
        </template>
        <span v-else class="muted" data-testid="no-score">暂无评分</span>
      </div>

      <ul class="info">
        <li>类型：{{ poi.type }}（{{ poi.city }}）</li>
        <li>地址：{{ poi.address }}</li>
        <li>电话：{{ phoneVisible() ? poi.phone : '登录后可见' }}</li>
        <li>营业：{{ poi.openHours }}</li>
        <li v-if="poi.attrs">属性：{{ poi.attrs }}</li>
        <li>导航点击：{{ poi.navClicks }}</li>
      </ul>

      <div class="actions">
        <button type="button" class="primary" data-testid="nav" @click="navClick">🧭 导航</button>
        <button
          type="button"
          class="ghost"
          data-testid="write-review"
          @click="router.push(`/explore/poi/${poi.id}/review`)"
        >
          ✍️ 写评价
        </button>
        <button
          type="button"
          class="ghost"
          @click="alert('纠错表单：address/phone/open_hours/closed/attrs → 工单同类合并，采纳回写并通知')"
        >
          🔧 纠错
        </button>
        <button
          type="button"
          class="ghost report"
          @click="alert('举报面板：类型+说明 → 48h 内通知结果（合规 B2）')"
        >
          🚩 举报
        </button>
      </div>

      <section class="reviews">
        <h2>用户评价</h2>
        <p class="muted">评价列表随接口填充；待审评价仅作者可见（U81）。</p>
      </section>
    </template>
  </div>
</template>

<style scoped>
.poi-detail {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 16px;
  display: grid;
  gap: 14px;
}
header {
  display: flex;
  gap: 12px;
  align-items: center;
}
.back {
  background: none;
  border: none;
  color: #ff7a2f;
}
.closed {
  background: #fdecec;
  color: ##ef4444;
  color: #ef4444;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
}
.score {
  display: flex;
  gap: 8px;
  align-items: center;
}
.stars {
  height: 14px;
  background: #ffb020;
  border-radius: 999px;
}
.info {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 8px;
  font-size: 14px;
  color: #2b2118;
}
.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.actions button {
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
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
.reviews h2 {
  font-size: 17px;
}
.muted {
  color: #7a6e63;
  font-size: 14px;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface CurvePoint {
  at: string;
  kg: number;
}

interface Curve {
  points: CurvePoint[];
  hasBase: boolean;
  latestKg?: number;
  highlighted?: boolean;
  trend?: string;
  baseAt?: string;
  baseKg?: number;
}

const route = useRoute();
const router = useRouter();
const petId = String(route.params.id);
const curve = ref<Curve | null>(null);
const error = ref('');

onMounted(async () => {
  try {
    curve.value = (await api.petWeights({ path: { id: petId } })) as unknown as Curve;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 pet-service）';
  }
});

const path = computed(() => {
  if (!curve.value || curve.value.points.length === 0) return '';
  const pts = curve.value.points;
  const xs = pts.map((p) => Date.parse(p.at));
  const minY = Math.min(...pts.map((p) => p.kg));
  const maxY = Math.max(...pts.map((p) => p.kg));
  const spanY = maxY - minY || 1;
  const spanX = Math.max(...xs) - Math.min(...xs) || 1;
  return pts
    .map((p, i) => {
      const x = ((xs[i]! - Math.min(...xs)) / spanX) * 280;
      const y = 80 - ((p.kg - minY) / spanY) * 60;
      return `${i === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(' ');
});
</script>

<template>
  <div class="weights">
    <header>
      <button type="button" class="back" @click="router.push(`/pets/${petId}`)">‹ 返回</button>
      <h1>体重曲线</h1>
    </header>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-else-if="!curve" class="muted">加载中…</p>
    <p v-else-if="curve.points.length === 0" class="muted" data-testid="empty">
      暂无数据 —— 到「＋ 记录 · 体重」录入第一条（0.1–200kg）。
    </p>

    <template v-else>
      <div class="stats">
        <span>最新 {{ curve.latestKg }} kg</span>
        <span v-if="curve.hasBase">基准 {{ curve.baseKg }} kg（{{ curve.baseAt?.slice(0, 10) }}）</span>
        <span :class="{ hl: curve.highlighted }" data-testid="trend">
          趋势 {{ curve.trend }}{{ curve.highlighted ? '（≥5% 高亮）' : '' }}
        </span>
      </div>
      <svg viewBox="0 0 290 90" class="chart" role="img" aria-label="体重曲线">
        <path :d="path" fill="none" stroke="#ff7a2f" stroke-width="2" />
        <circle
          v-for="(p, i) in curve.points"
          :key="i"
          :cx="((Date.parse(p.at) - Math.min(...curve.points.map((q) => Date.parse(q.at)))) /
            (Math.max(...curve.points.map((q) => Date.parse(q.at))) - Math.min(...curve.points.map((q) => Date.parse(q.at))) || 1)) * 280"
          :cy="80 - ((p.kg - Math.min(...curve.points.map((q) => q.kg))) /
            ((Math.max(...curve.points.map((q) => q.kg)) - Math.min(...curve.points.map((q) => q.kg))) || 1)) * 60"
          r="3"
          fill="#ff7a2f"
        />
      </svg>
      <ul class="list">
        <li v-for="(p, i) in [...curve.points].reverse()" :key="i">
          {{ p.at.slice(0, 10) }} · {{ p.kg }} kg
        </li>
      </ul>
      <button type="button" class="primary" @click="router.push(`/pets/${petId}/record`)">＋ 记录体重</button>
    </template>
  </div>
</template>

<style scoped>
.weights {
  max-width: 560px;
  margin: 32px auto;
  padding: 16px;
  display: grid;
  gap: 16px;
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
.stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 14px;
  color: #7a6e63;
}
.stats .hl {
  color: #ff7a2f;
  font-weight: 600;
}
.chart {
  width: 100%;
  background: #fff;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
}
.list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 6px;
  font-size: 14px;
  color: #7a6e63;
}
.primary {
  height: 48px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.muted {
  color: #7a6e63;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>

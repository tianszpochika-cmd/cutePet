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
const loading = ref(true);

onMounted(() => { void loadCurve(); });

async function loadCurve() {
  loading.value = true;
  error.value = '';
  try {
    const result = (await api.petWeights({ path: { id: petId } })) as unknown;
    if (!result || typeof result !== 'object' || !('points' in result) || !Array.isArray(result.points) ||
        !result.points.every((point) => point && typeof point.at === 'string' &&
          Number.isFinite(Date.parse(point.at)) && typeof point.kg === 'number' && Number.isFinite(point.kg))) {
      throw new Error('体重曲线格式异常');
    }
    curve.value = result as Curve;
  } catch (e) {
    curve.value = null;
    error.value = e instanceof Error ? e.message : '体重曲线读取失败';
  } finally {
    loading.value = false;
  }
}

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

    <p v-if="loading" class="muted" role="status">正在读取体重曲线…</p>
    <div v-else-if="error" class="error-state" role="alert"><p>{{ error }}</p><button type="button" class="ghost" @click="loadCurve">重试</button></div>
    <div v-else-if="!curve || curve.points.length === 0" class="empty" data-testid="empty">
      <h2>还没有体重数据</h2>
      <p>体重写入接口尚未纳入当前前端契约。曲线只展示平台已有数据，录入功能接通后才能添加基线。</p>
    </div>

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
      <p class="notice">当前仅展示平台已有体重数据；新增体重待接口接入。</p>
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
  color: #a8470c;
  min-height: 44px;
  cursor: pointer;
}
.stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 14px;
  color: #7a6e63;
}
.stats .hl {
  color: #a8470c;
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
.empty, .error-state { background: #fff; border: 1px solid #f0e6dc; border-radius: 16px; padding: 20px; color: #604b3b; }
.empty h2 { margin: 0 0 8px; font-size: 18px; }
.empty p, .error-state p { margin: 0; line-height: 1.6; }
.ghost { margin-top: 10px; min-height: 44px; padding: 0 16px; border: 1px solid #dacabc; border-radius: 999px; color: #684b39; background: #fff; cursor: pointer; }
.notice { color: #706255; font-size: 13px; }
.ghost:focus-visible, .back:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
.muted {
  color: #7a6e63;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>

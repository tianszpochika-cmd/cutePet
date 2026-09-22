<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { POI_TYPES, RADII, buildPoiQuery, distanceLabel } from '../../domain/explore';

const router = useRouter();
const filters = reactive<{ city?: string; type?: string; radiusKm: number }>({ type: undefined, radiusKm: 5 });
const items = ref<
  { id: number; name: string; type: string; distanceKm: number; avg: number | null; reviewCount: number }[]
>([]);
const error = ref('');
const panelOpen = ref(false);
const city = ref('北京');

async function load() {
  error.value = '';
  try {
    const query = buildPoiQuery({ city: city.value, type: filters.type, radiusKm: filters.radiusKm });
    const res = (await api.poisSearch({ query })) as unknown as {
      items: { id: number; name: string; type: string; distanceKm: number; avg: number | null; reviewCount: number }[];
    };
    items.value = res.items ?? [];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 explore-service）';
  }
}

onMounted(load);
</script>

<template>
  <div class="explore">
    <header>
      <h1>附近探索</h1>
      <select v-model="city" data-testid="city" @change="load">
        <option>北京</option>
        <option>上海</option>
        <option>成都</option>
        <option>杭州</option>
        <option>广州</option>
      </select>
      <button type="button" class="filter-btn" data-testid="open-filter" @click="panelOpen = !panelOpen">
        筛选
      </button>
    </header>

    <div v-if="panelOpen" class="panel" data-testid="filter-panel">
      <div class="group">
        <button
          v-for="t in POI_TYPES"
          :key="t"
          type="button"
          :class="{ on: filters.type === t }"
          @click="filters.type = filters.type === t ? undefined : t; load()"
        >
          {{ t }}
        </button>
      </div>
      <div class="group">
        <button
          v-for="r in RADII"
          :key="r"
          type="button"
          :class="{ on: filters.radiusKm === r }"
          @click="filters.radiusKm = r; load()"
        >
          {{ r }}km
        </button>
      </div>
    </div>

    <!-- 地图示意面板（持证地图 SDK 属部署期 MapProvider 接口；本地测试用占位画布） -->
    <div class="map" data-testid="map-placeholder" aria-label="地图占位">
      <span v-for="it in items.slice(0, 8)" :key="it.id" class="pin">{{ it.type.slice(0, 2) }}</span>
      <p class="map-note">地图占位（持证 SDK 随 MapProvider 接入；列表与地图选中态联动）</p>
    </div>

    <p v-if="error" class="err">{{ error }}</p>
    <ul v-else class="list">
      <li v-for="it in items" :key="it.id">
        <button type="button" class="poi" :data-testid="`poi-${it.id}`" @click="router.push(`/explore/poi/${it.id}`)">
          <strong>{{ it.name }}</strong>
          <span class="meta">
            {{ it.type }} · {{ distanceLabel(it.distanceKm) }} ·
            {{ it.avg === null ? '暂无评分' : `★ ${it.avg}` }}（{{ it.reviewCount }}）
          </span>
        </button>
      </li>
      <li v-if="items.length === 0 && !error" class="muted" data-testid="empty">
        附近暂无数据 —— 成为第一个贡献者（纠错/评价入口在详情页）。
      </li>
    </ul>
  </div>
</template>

<style scoped>
.explore {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px;
}
header {
  display: flex;
  gap: 12px;
  align-items: center;
}
select {
  height: 36px;
  border-radius: 999px;
  border: 1px solid #f0e6dc;
  padding: 0 12px;
}
.filter-btn {
  margin-left: auto;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  padding: 12px;
  margin-top: 12px;
  display: grid;
  gap: 8px;
}
.group {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.group button {
  height: 30px;
  padding: 0 12px;
  border: none;
  border-radius: 999px;
  background: #f7f1ea;
  color: #7a6e63;
  font-size: 13px;
}
.group button.on {
  background: #ff7a2f;
  color: #fff;
}
.map {
  position: relative;
  height: 240px;
  margin-top: 12px;
  border-radius: 16px;
  background: repeating-linear-gradient(45deg, #f4efe9, #f4efe9 12px, #faf6f1 12px, #faf6f1 24px);
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  align-content: flex-start;
}
.pin {
  background: #2fbf71;
  color: #fff;
  font-size: 12px;
  border-radius: 999px 999px 999px 4px;
  padding: 4px 8px;
}
.map-note {
  position: absolute;
  bottom: 8px;
  left: 16px;
  right: 16px;
  color: #7a6e63;
  font-size: 12px;
}
.list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 10px;
  margin-top: 12px;
}
.poi {
  width: 100%;
  text-align: left;
  background: #fff;
  border: none;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  border-radius: 12px;
  padding: 14px 16px;
  display: grid;
  gap: 4px;
  cursor: pointer;
}
.meta {
  color: #7a6e63;
  font-size: 13px;
}
.muted {
  color: #7a6e63;
  font-size: 14px;
  list-style: none;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>

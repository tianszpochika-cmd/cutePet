<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { POI_TYPES, RADII, distanceLabel, buildPoiQuery } from '../../../web/src/domain/explore.ts';

const router = useRouter();
const type = ref<string | null>(null);
const radius = ref(5);
const items = ref([
  { id: 1, name: '朝阳宠物公园', type: '宠物公园', km: 0.8, avg: 4.6 },
  { id: 2, name: '仁心宠物医院', type: '宠物医院', km: 2.1, avg: 4.9 },
  { id: 3, name: '毛球美容', type: '美容', km: 4.4, avg: null },
]);

function apply() {
  void buildPoiQuery({ type: type.value ?? undefined, radiusKm: radius.value }); // 与 Web 同一查询构造
}
</script>

<template>
  <div class="m-explore">
    <header>
      <h1>附近探索</h1>
      <button type="button" class="map-btn" @click="router.push('/events')">活动 →</button>
    </header>

    <div class="filters">
      <button
        v-for="t in POI_TYPES"
        :key="t"
        type="button"
        :class="{ on: type === t }"
        @click="type = type === t ? null : t; apply()"
      >
        {{ t }}
      </button>
    </div>
    <div class="radii">
      <button v-for="r in RADII" :key="r" type="button" :class="{ on: radius === r }" @click="radius = r; apply()">
        {{ r }}km
      </button>
    </div>

    <!-- 地图占位（持证 SDK 随 MapProvider 接入） -->
    <div class="map" data-testid="map">
      <span v-for="i in items.slice(0, 3)" :key="i.id" class="pin">{{ i.name.slice(0, 2) }}</span>
    </div>

    <ul class="list">
      <li v-for="i in items" :key="i.id" :data-testid="`poi-${i.id}`" @click="router.push('/explore/poi/' + i.id)">
        <strong>{{ i.name }}</strong>
        <span class="meta">{{ i.type }} · {{ distanceLabel(i.km) }} · {{ i.avg === null ? '暂无评分' : `★ ${i.avg}` }}</span>
      </li>
      <li v-if="items.length === 0" class="empty">附近暂无数据——可在详情页纠错/评价</li>
    </ul>
  </div>
</template>

<style scoped>
.m-explore { padding: 16px; display: grid; gap: 12px; }
header { display: flex; justify-content: space-between; align-items: center; }
h1 { margin: 0; font-size: 20px; }
.map-btn { border: none; background: #e7f8ef; color: #15803d; border-radius: 999px; padding: 9px 14px; font-weight: 600; min-height: 40px; }
.filters { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; }
.filters button, .radii button { border: none; background: #fff; color: #7a6e63; border-radius: 999px; padding: 8px 14px; font-size: 13px; white-space: nowrap; min-height: 40px; box-shadow: inset 0 0 0 1px #f0e6dc; }
.filters button.on, .radii button.on { background: #2fbf71; color: #fff; font-weight: 600; box-shadow: none; }
.radii { display: flex; gap: 8px; }
.map { height: 150px; border-radius: 18px; background: repeating-linear-gradient(45deg, #eef4ef, #eef4ef 12px, #f6faf6 12px, #f6faf6 24px); display: flex; gap: 8px; padding: 14px; align-items: flex-start; }
.pin { background: #2fbf71; color: #fff; font-size: 11px; border-radius: 999px 999px 999px 4px; padding: 4px 8px; }
.list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
li { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; display: grid; gap: 4px; min-height: 44px; }
.meta { color: #7a6e63; font-size: 12px; }
.empty { color: #7a6e63; font-size: 13px; text-align: center; box-shadow: none; background: none; }
</style>

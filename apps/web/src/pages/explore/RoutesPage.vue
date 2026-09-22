<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface RouteRow {
  id: number;
  name: string;
  city: string;
  distanceM: number;
  durationMin: number;
  difficulty: string;
  state: string;
}

const router = useRouter();
const items = ref<RouteRow[]>([]);
const error = ref('');

onMounted(async () => {
  try {
    items.value = (await api.routesList()) as unknown as RouteRow[];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 explore-service）';
  }
});

function publish() {
  const points = prompt('地图选点连线（一期：依次输入 "lng,lat;lng,lat;…"，至少 2 点）');
  if (points === null) return;
  void createRoute(points);
}

async function createRoute(points: string) {
  try {
    await api.routeCreate({
      body: {
        city: '北京',
        name: '我的遛宠路线',
        points,
        durationMin: 30,
        difficulty: '简单',
        note: '',
      },
    });
    alert('已提交审核（先审后发，通过后出现在列表）');
    items.value = (await api.routesList()) as unknown as RouteRow[];
  } catch (e) {
    alert(e instanceof Error ? e.message : '发布失败（点位格式：lng,lat;lng,lat）');
  }
}
</script>

<template>
  <div class="routes">
    <header>
      <button type="button" class="back" @click="router.push('/explore')">‹ 地图</button>
      <h1>遛宠路线</h1>
      <button type="button" class="primary" data-testid="publish-route" @click="publish">＋ 发布路线</button>
    </header>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-if="items.length === 0 && !error" class="muted" data-testid="empty">
      还没有已发布路线 —— 点「发布路线」创建（选点 ≥2，提交后先审后发）。
    </p>

    <ul class="list">
      <li v-for="r in items" :key="r.id" class="card">
        <strong>{{ r.name }}</strong>
        <span class="meta">
          {{ r.city }} · {{ Math.round(r.distanceM / 100) / 10 }}km · {{ r.durationMin }} 分钟 ·
          {{ r.difficulty }}
        </span>
        <span class="tag">{{ r.state }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.routes {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 16px;
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
.primary {
  margin-left: auto;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: #2fbf71;
  color: #fff;
  font-weight: 600;
}
.list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 10px;
  margin-top: 12px;
}
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  padding: 14px 16px;
  display: grid;
  gap: 4px;
}
.meta {
  color: #7a6e63;
  font-size: 13px;
}
.tag {
  font-size: 12px;
  color: #4d8dff;
  background: #eaf1ff;
  border-radius: 999px;
  padding: 2px 8px;
  justify-self: start;
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

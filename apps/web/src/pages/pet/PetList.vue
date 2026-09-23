<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface PetRow {
  id: number | string;
  name: string;
  species: string;
  breed: string;
  state?: string;
}

const router = useRouter();
const pets = ref<PetRow[]>([]);
const loading = ref(true);
const error = ref('');

onMounted(() => { void loadPets(); });

async function loadPets() {
  loading.value = true;
  error.value = '';
  try {
    const res = await api.petsList();
    if (!Array.isArray(res) || !res.every((row) =>
      row && typeof row === 'object' && (typeof row.id === 'number' || typeof row.id === 'string') &&
      typeof row.name === 'string' && typeof row.species === 'string')) {
      throw new Error('宠物列表格式异常');
    }
    pets.value = res as PetRow[];
  } catch (e) {
    pets.value = [];
    error.value = e instanceof Error ? e.message : '宠物列表读取失败';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="pet-list">
    <header class="intro">
      <div>
        <span class="eyebrow">照护档案</span>
        <h1>我的宠物</h1>
        <p class="muted">从这里进入健康记录、体重与提醒。档案仅显示平台确认可见的内容。</p>
      </div>
      <button type="button" class="new-pet" @click="router.push('/pets/new')">＋ 新建宠物</button>
    </header>
    <p v-if="loading" class="loading" role="status">正在读取宠物档案…</p>
    <div v-else-if="error" class="error-state" role="alert" data-testid="pets-error">
      <p>{{ error }}</p>
      <button type="button" @click="loadPets">重新加载</button>
    </div>

    <div v-else-if="pets.length === 0" class="empty" data-testid="pets-empty">
      <span class="empty-icon" aria-hidden="true">♡</span>
      <h2>还没有宠物档案</h2>
      <p class="muted">从一只宠物开始，之后可逐步补充照护记录。</p>
      <button type="button" @click="router.push('/pets/new')">开始建档</button>
    </div>

    <ul v-else class="cards">
      <li v-for="p in pets" :key="p.id">
        <button type="button" class="card" @click="router.push(`/pets/${p.id}`)">
          <strong>{{ p.name }}</strong>
          <span>{{ p.species }}{{ p.breed ? ` · ${p.breed}` : '' }}</span>
          <small>查看档案 →</small>
        </button>
      </li>
    </ul>
    <button type="button" class="recycle-link" @click="router.push('/pets/recycle')">查看归档与回收站 →</button>
  </div>
</template>

<style scoped>
.pet-list {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px;
}
.intro { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: end; gap: 16px; margin-bottom: 24px; }
.intro h1 { margin: 4px 0 8px; font-size: clamp(26px, 4vw, 34px); color: #2b2118; }
.intro p { margin: 0; max-width: 48ch; line-height: 1.6; }
.eyebrow { font-size: 13px; color: #a8470c; font-weight: 700; }
.new-pet, .error-state button { min-height: 44px; border: 0; border-radius: 999px; background: #b85111; color: #fff; font-weight: 700; padding: 0 20px; cursor: pointer; }
.loading, .error-state { padding: 28px; border: 1px solid #f0e6dc; border-radius: 18px; background: #fff; }
.error-state { display: grid; gap: 8px; color: #a12a28; }
.error-state p { margin: 0; }
.error-state button { justify-self: start; }
.cards {
  list-style: none;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.card {
  width: 100%;
  background: #fff;
  border: none;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  border-radius: 16px;
  padding: 20px 16px;
  display: grid;
  gap: 6px;
  text-align: left;
  cursor: pointer;
  min-height: 130px;
}
.card strong { color: #2b2118; font-size: 19px; }
.card span { color: #706255; font-size: 14px; }
.card small { align-self: end; color: #a8470c; font-weight: 700; }
.card:hover { box-shadow: inset 0 0 0 2px #c17749, 0 8px 24px #2b21180c; }
.empty {
  text-align: center;
  padding: 48px 16px;
  display: grid;
  gap: 8px;
  justify-items: center;
  background: #fff;
  border: 1px solid #f0e6dc;
  border-radius: 20px;
}
.empty h2 { margin: 0; font-size: 20px; }
.empty p { margin: 0 0 8px; }
.empty-icon { display: grid; place-items: center; width: 60px; height: 60px; border-radius: 18px; background: #fff1e8; color: #a8470c; font-size: 28px; }
.empty button,
.cards button {
  border: none;
}
.empty button {
  border-radius: 999px;
  background: #b85111;
  color: #fff;
  height: 48px;
  padding: 0 24px;
  font-weight: 600;
}
.recycle-link { margin-top: 20px; min-height: 44px; border: 0; background: transparent; color: #a8470c; cursor: pointer; font-weight: 600; }
button:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
@media (max-width: 600px) { .new-pet { width: 100%; } .cards { grid-template-columns: 1fr; } }
.muted {
  color: #7a6e63;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface PetRow {
  id: number;
  name: string;
  species: string;
  breed: string;
  state: string;
}

const router = useRouter();
const pets = ref<PetRow[]>([]);
const loading = ref(true);
const error = ref('');

onMounted(async () => {
  try {
    const res = await api.petsList();
    pets.value = (res as unknown as PetRow[]) ?? [];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 环境需先启动网关与 pet-service）';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="pet-list">
    <h1>我的宠物</h1>
    <p v-if="loading" class="muted">加载中…</p>
    <p v-else-if="error" class="err" data-testid="pets-error">{{ error }}</p>

    <div v-else-if="pets.length === 0" class="empty" data-testid="pets-empty">
      <p>还没有宠物档案</p>
      <button type="button" @click="router.push('/pets/new')">＋ 新建宠物（4 步向导）</button>
    </div>

    <ul v-else class="cards">
      <li v-for="p in pets" :key="p.id">
        <button type="button" class="card" @click="router.push(`/pets/${p.id}`)">
          <strong>{{ p.name }}</strong>
          <span>{{ p.species }} · {{ p.breed }}</span>
        </button>
      </li>
      <li>
        <button type="button" class="card add" @click="router.push('/pets/new')">＋ 新建宠物</button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.pet-list {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px;
}
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
}
.card.add {
  border: 1px dashed #ffb98a;
  color: #ff7a2f;
  place-content: center;
  min-height: 96px;
}
.empty {
  text-align: center;
  padding: 48px 0;
  display: grid;
  gap: 12px;
  justify-items: center;
}
.empty button,
.cards button {
  border: none;
}
.empty button {
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  height: 48px;
  padding: 0 24px;
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

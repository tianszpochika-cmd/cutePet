<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { convertWeight, weightLabel } from '../domain/mobileMotion';

const router = useRouter();
const unit = ref<'kg' | 'jin'>('kg');
const pets = ref([
  { id: 1, name: '旺财', species: '犬', breed: '柯基', kg: 12.5 },
  { id: 2, name: '咪咪', species: '猫', breed: '英短', kg: 4.2 },
]);

function toggleUnit() {
  unit.value = unit.value === 'kg' ? 'jin' : 'kg'; // 决议双单位，存储统一 kg
}
void convertWeight;
</script>

<template>
  <div class="m-pets">
    <header>
      <h1>我的宠物</h1>
      <button type="button" class="unit" data-testid="unit" @click="toggleUnit">
        单位：{{ unit === 'kg' ? 'kg' : '斤' }} ⇄
      </button>
    </header>

    <ul class="list">
      <li v-for="p in pets" :key="p.id" :data-testid="`pet-${p.id}`" @click="router.push(`/pets/${p.id}`)">
        <span class="avatar">🐾</span>
        <div>
          <strong>{{ p.name }}</strong>
          <p>{{ p.species }} · {{ p.breed }} · {{ weightLabel(p.kg, unit) }}</p>
        </div>
        <span class="go">→</span>
      </li>
    </ul>

    <button type="button" class="add" @click="router.push('/pets/new')">＋ 新建档案（4 步）</button>

    <div class="links">
      <router-link to="/pets/recycle">回收站/归档</router-link>
      <router-link to="/me/sync">待同步记录</router-link>
    </div>
  </div>
</template>

<style scoped>
.m-pets { padding: 16px; display: grid; gap: 14px; }
header { display: flex; justify-content: space-between; align-items: center; }
h1 { margin: 0; font-size: 20px; }
.unit { border: none; background: #fff; border-radius: 999px; padding: 9px 14px; color: #4d8dff; font-weight: 600; min-height: 40px; box-shadow: inset 0 0 0 1px #f0e6dc; }
.list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
li { background: #fff; border-radius: 18px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; display: flex; gap: 14px; align-items: center; min-height: 64px; }
.avatar { width: 46px; height: 46px; border-radius: 999px; background: #fff1e8; display: grid; place-items: center; font-size: 22px; }
li p { margin: 4px 0 0; color: #7a6e63; font-size: 13px; }
.go { margin-left: auto; color: #a89b8f; font-size: 18px; }
.add { height: 54px; border: 1px dashed #ffb98a; border-radius: 18px; background: #fff; color: #ff7a2f; font-size: 15px; font-weight: 600; min-height: 44px; }
.links { display: flex; justify-content: space-between; font-size: 13px; }
.links a { color: #ff7a2f; text-decoration: none; padding: 10px 0; min-height: 44px; display: inline-flex; align-items: center; }
</style>

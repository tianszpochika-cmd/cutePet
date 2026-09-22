<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { JOIN_SCOPE_COPY, DEFAULT_NEW_MEMBER_SHARE, ME_GALLERY, galleryGate } from '../../../web/src/domain/family.ts';

const router = useRouter();
const members = ref([
  { id: 1, name: '我', role: 'OWNER', pets: '旺财、咪咪' },
  { id: 2, name: '豆豆爸', role: 'ADMIN', pets: '（共享）豆豆 · 可管理' },
  { id: 3, name: '奶奶', role: 'MEMBER', pets: '（共享）旺财 · 只读' },
]);
void ME_GALLERY;
void galleryGate;
</script>

<template>
  <div class="m-family">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>我的家庭</h1>
      <button type="button" class="invite" @click="router.push('/family/invite')">邀请</button>
    </header>

    <p class="scope">{{ JOIN_SCOPE_COPY }}</p>
    <p class="meta">新成员默认档位：{{ DEFAULT_NEW_MEMBER_SHARE === 'READONLY' ? '只读' : '可管理' }} · 一人限一个家庭</p>

    <ul class="list">
      <li v-for="m in members" :key="m.id" :data-testid="`member-${m.id}`">
        <strong>{{ m.name }}</strong>
        <span class="role">{{ m.role === 'OWNER' ? '所有者' : m.role === 'ADMIN' ? '管理员' : '成员' }}</span>
        <p>{{ m.pets }}</p>
      </li>
    </ul>

    <div class="links">
      <router-link to="/family/join">输入邀请码</router-link>
      <router-link to="/pets/1/share">共享管理（X03）</router-link>
      <router-link to="/pets/1/transfer">转移所有权（X04）</router-link>
    </div>
  </div>
</template>

<style scoped>
.m-family { padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; color: #9b8cff; font-size: 20px; }
h1 { margin: 0; font-size: 18px; flex: 1; }
.invite { border: none; background: #9b8cff; color: #fff; border-radius: 999px; padding: 10px 16px; font-weight: 600; min-height: 40px; }
.scope { background: #f5f3ff; color: #5b4bc4; border-radius: 14px; padding: 12px 14px; font-size: 13px; line-height: 1.8; margin: 0; }
.meta { color: #7a6e63; font-size: 12px; margin: 0; }
.list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
li { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; display: grid; grid-template-columns: 1fr auto; gap: 4px 8px; min-height: 44px; }
li p { grid-column: 1 / -1; margin: 0; color: #7a6e63; font-size: 13px; }
.role { background: #f5f3ff; color: #6d5bd0; border-radius: 999px; padding: 3px 10px; font-size: 11px; font-weight: 600; align-self: start; }
.links { display: grid; gap: 8px; }
.links a { background: #fff; border-radius: 14px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; color: #2b2118; text-decoration: none; font-size: 14px; min-height: 44px; display: flex; align-items: center; }
</style>

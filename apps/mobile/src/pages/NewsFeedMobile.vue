<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { CHANNELS, channelParam, disclaimerFor } from '../../../web/src/domain/news.ts';

const router = useRouter();
const channel = ref('推荐');
const items = ref([
  { slug: 'puppy-food', title: '幼猫换粮的七个误区', ch: '猫', likes: 128 },
  { slug: 'summer-water', title: '夏季饮水注意事项', ch: '营养', likes: 96 },
  { slug: 'train-lease', title: '牵引绳怎么选', ch: '训练行为', likes: 61 },
]);

const disclaimer = () => disclaimerFor(channel.value);
void channelParam;
</script>

<template>
  <div class="m-news">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>宠物资讯</h1>
      <button type="button" class="write" @click="router.push('/write')">✍️ 创作</button>
    </header>

    <nav class="chips">
      <button v-for="c in CHANNELS" :key="c" type="button" :class="{ on: channel === c }" @click="channel = c">
        {{ c }}
      </button>
    </nav>

    <p v-if="disclaimer()" class="disclaimer" data-testid="disclaimer">⚠️ {{ disclaimer() }}</p>

    <ul class="list">
      <li v-for="a in items" :key="a.slug" :data-testid="`article-${a.slug}`" @click="router.push('/news/' + a.slug)">
        <strong>{{ a.title }}</strong>
        <span class="meta">{{ a.ch }} · 👍 {{ a.likes }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.m-news { padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; color: #ff7a2f; font-size: 20px; }
h1 { margin: 0; font-size: 18px; flex: 1; }
.write { border: none; background: #fff1e8; color: #ff7a2f; border-radius: 999px; padding: 10px 14px; font-weight: 600; min-height: 40px; }
.chips { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; }
.chips button { border: none; background: #fff; color: #7a6e63; border-radius: 999px; padding: 8px 14px; font-size: 13px; white-space: nowrap; min-height: 40px; box-shadow: inset 0 0 0 1px #f0e6dc; }
.chips button.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
.disclaimer { background: #fff1e8; color: #b45309; border-radius: 12px; padding: 10px 12px; font-size: 12px; margin: 0; }
.list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
li { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; display: grid; gap: 6px; min-height: 44px; }
.meta { color: #7a6e63; font-size: 12px; }
</style>

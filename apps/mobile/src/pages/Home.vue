<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const loggedIn = ref(true); // 会话态接线随本地阶段
const pendingReminders = ref<{ pet: string; type: string; due: string }[]>([
  { pet: '旺财', type: '疫苗', due: '明天' },
]);
const quick = ref(false); // 一键记录 Sheet

const GOODS = [
  { id: 1, name: '幼猫无谷粮', cat: '主粮', hot: 820 },
  { id: 2, name: '互动逗猫棒', cat: '玩具', hot: 512 },
  { id: 3, name: '皮肤护理喷雾', cat: '医疗保健', hot: 388 },
];
const NEWS = [
  { slug: 'puppy-food', title: '幼猫换粮的七个误区', ch: '猫' },
  { slug: 'summer-water', title: '夏季饮水注意', ch: '营养' },
];

const reminder = computed(() =>
  pendingReminders.value.length ? pendingReminders.value[0]! : null,
);
</script>

<template>
  <div class="m-home">
    <!-- L3 待办条（置顶） -->
    <button v-if="reminder" type="button" class="reminder" data-testid="reminder-bar" @click="router.push('/todos/1/confirm')">
      ⏰ {{ reminder.pet }} · {{ reminder.type }} · {{ reminder.due }}
      <span class="go">去完成 →</span>
    </button>

    <section class="pet-card">
      <div class="avatar">🐾</div>
      <div>
        <strong>旺财</strong>
        <p>犬 · 柯基 · 12.5kg</p>
      </div>
      <button type="button" class="detail" @click="router.push('/pets')">档案 →</button>
    </section>

    <!-- 一键记录（L3/L2） -->
    <button type="button" class="quick" data-testid="quick-record" @click="quick = true">＋ 一键记录</button>

    <!-- 用品分区（决议：并入首页，不做独立 Tab） -->
    <section class="block" data-testid="goods-block">
      <header><h2>逛好物</h2><span>纯内容导购 · 不交易</span></header>
      <ul>
        <li v-for="g in GOODS" :key="g.id">
          <strong>{{ g.name }}</strong>
          <span class="meta">{{ g.cat }} · 热度 {{ g.hot }}</span>
        </li>
      </ul>
      <button type="button" class="link" @click="router.push('/goods')">全部商品 →</button>
    </section>

    <section class="block" data-testid="news-block">
      <header><h2>精选资讯</h2></header>
      <ul>
        <li v-for="n in NEWS" :key="n.slug" @click="router.push('/news')">
          <strong>{{ n.title }}</strong>
          <span class="meta">{{ n.ch }}</span>
        </li>
      </ul>
      <button type="button" class="link" @click="router.push('/news')">进入资讯 →</button>
    </section>

    <p v-if="!loggedIn" class="guest">未登录：建档与提醒需先登录（L1 闸门）</p>

    <!-- 一键记录 Sheet（手势：下滑关闭 120px / 快速下滑） -->
    <div v-if="quick" class="mask" @click.self="quick = false">
      <div class="sheet" data-testid="quick-sheet">
        <div class="handle" />
        <h3>一键记录</h3>
        <div class="kinds">
          <button v-for="k in ['体重', '疫苗', '驱虫', '就医', '用药', '过敏']" :key="k" type="button" @click="router.push('/quick-record?kind=' + k)">
            {{ k }}
          </button>
        </div>
        <p class="tip">下滑或点击空白关闭（位移 ≥120px 或快速下滑触发 dismiss）</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.m-home { padding: 16px 16px 24px; display: grid; gap: 14px; }
.reminder { border: none; background: #fff1e8; color: #b45309; border-radius: 16px; padding: 14px 16px; font-size: 14px; font-weight: 600; display: flex; justify-content: space-between; align-items: center; min-height: 44px; }
.go { color: #ff7a2f; }
.pet-card { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: flex; gap: 14px; align-items: center; }
.avatar { width: 52px; height: 52px; border-radius: 999px; background: #fff1e8; display: grid; place-items: center; font-size: 26px; }
.pet-card p { margin: 4px 0 0; color: #7a6e63; font-size: 13px; }
.detail { margin-left: auto; border: none; background: #fff1e8; color: #ff7a2f; border-radius: 999px; padding: 10px 16px; font-weight: 600; min-height: 44px; }
.quick { height: 54px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-size: 17px; font-weight: 700; }
.block { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.block header { display: flex; justify-content: space-between; align-items: baseline; }
.block h2 { margin: 0; font-size: 16px; }
.block header span { color: #7a6e63; font-size: 11px; }
ul { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
li { display: flex; justify-content: space-between; align-items: center; gap: 8px; font-size: 14px; min-height: 40px; }
.meta { color: #7a6e63; font-size: 12px; }
.link { border: none; background: none; color: #ff7a2f; font-size: 13px; font-weight: 600; text-align: left; min-height: 44px; }
.guest { color: #7a6e63; font-size: 13px; text-align: center; }
.mask { position: fixed; inset: 0; background: rgba(43,33,24,.4); z-index: 50; display: flex; align-items: flex-end; }
.sheet { width: 100%; background: #fff; border-radius: 24px 24px 0 0; padding: 12px 20px calc(28px + env(safe-area-inset-bottom)); max-height: 78vh; animation: sheet-up 320ms cubic-bezier(0.22,1,0.36,1); }
@keyframes sheet-up { from { transform: translateY(100%); } to { transform: translateY(0); } }
.handle { width: 44px; height: 5px; border-radius: 999px; background: #e5ddd3; margin: 4px auto 12px; }
.sheet h3 { margin: 0 0 12px; font-size: 16px; }
.kinds { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.kinds button { height: 52px; border: none; border-radius: 16px; background: #f7f1ea; color: #2b2118; font-size: 15px; font-weight: 600; min-height: 44px; }
.tip { color: #a89b8f; font-size: 11px; text-align: center; margin: 14px 0 0; }
</style>

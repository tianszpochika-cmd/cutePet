<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getAccessToken } from '@cutepet/api-client';
import { FAVORITE_GROUPS, MESSAGE_CATEGORIES } from '../../../web/src/domain/family.ts';

type Area = 'messages' | 'favorites' | 'activity';
const route = useRoute();
const router = useRouter();
const area = computed<Area>(() => route.path === '/messages' ? 'messages' : route.path === '/me/favorites' ? 'favorites' : 'activity');
const hasToken = ref(Boolean(getAccessToken()));
const messageTab = ref<(typeof MESSAGE_CATEGORIES)[number]['id']>('REMINDER');
const favoriteTab = ref<(typeof FAVORITE_GROUPS)[number]>('内容');
const activityTab = ref<'我的报名' | '我发布的' | '机构认证'>('我的报名');
const activityTabs = ['我的报名', '我发布的', '机构认证'] as const;
const title = computed(() => area.value === 'messages' ? '消息中心' : area.value === 'favorites' ? '我的收藏' : '活动中心');
const favoriteCopy: Record<(typeof FAVORITE_GROUPS)[number], string> = {
  内容: '内容收藏汇总需要已核验的本人身份，当前无法读取你的真实列表。',
  商品: '用品收藏保存在商品服务，个人汇总尚未联通，不能据此判断你是否收藏过商品。',
  清单: '清单收藏的个人汇总尚未联通。',
  场所: '场所收藏保存在探索服务，个人汇总尚未联通。',
  路线: '路线收藏保存在探索服务，个人汇总尚未联通。',
};
const activityCopy: Record<(typeof activityTabs)[number], string> = {
  我的报名: '本人报名明细尚无可核验的列表入口，这里不显示虚构的报名编号或“没有报名”。',
  我发布的: '本人发布记录与活动当前状态尚未联通，不能在这里确认发布或取消结果。',
  机构认证: '机构身份、资质有效期及审核进度需要平台核验，当前不能切换或展示模拟认证状态。',
};
const browseTarget = computed(() => area.value === 'favorites'
  ? favoriteTab.value === '内容' ? '/news' : favoriteTab.value === '商品' || favoriteTab.value === '清单' ? '/goods' : '/explore'
  : area.value === 'activity' ? '/events' : '/settings/notify');
const browseLabel = computed(() => area.value === 'favorites' ? '浏览公开内容' : area.value === 'activity' ? '浏览公开活动' : '查看通知设置');
</script>

<template>
  <div class="detail-page">
    <header class="page-head"><button type="button" class="back" aria-label="返回我的" @click="router.push('/me')">←</button><div><p class="eyebrow">PERSONAL SPACE</p><h1>{{ title }}</h1></div></header>
    <section v-if="!hasToken" class="guest" role="status"><span aria-hidden="true">✿</span><h2>个人记录需要账号</h2><p>公开内容可继续浏览；消息、收藏和报名只属于本人。正式登录入口接通后才能核对这些资料。</p><router-link :to="{ path: '/login', query: { return: route.fullPath } }">前往账号入口 →</router-link></section>

    <template v-else>
      <p class="account-note" role="status">当前设备持有访问令牌，但此页尚未取得可核验的本人列表。下方是栏目结构，不代表记录为空。</p>

      <template v-if="area === 'messages'">
        <nav class="tabs" aria-label="消息分类"><button v-for="item in MESSAGE_CATEGORIES" :key="item.id" type="button" :class="{ on: messageTab === item.id }" :aria-pressed="messageTab === item.id" @click="messageTab = item.id">{{ item.label }}</button></nav>
        <section class="status-card" role="status"><span class="symbol" aria-hidden="true">✉</span><h2>{{ MESSAGE_CATEGORIES.find((item) => item.id === messageTab)?.label }}消息待接入</h2><p>消息接口尚未建立可核验的本人身份传递，当前不读取私人消息、未读数或已读状态，也不提供假“全部已读”。</p><router-link :to="browseTarget">{{ browseLabel }} →</router-link></section>
      </template>

      <template v-else-if="area === 'favorites'">
        <nav class="tabs" aria-label="收藏分组"><button v-for="item in FAVORITE_GROUPS" :key="item" type="button" :class="{ on: favoriteTab === item }" :aria-pressed="favoriteTab === item" @click="favoriteTab = item">{{ item }}</button></nav>
        <section class="status-card" role="status"><span class="symbol" aria-hidden="true">♡</span><h2>{{ favoriteTab }}收藏待联通</h2><p>{{ favoriteCopy[favoriteTab] }}这里不会把未读取的结果说成“尚无收藏”。</p><router-link :to="browseTarget">{{ browseLabel }} →</router-link></section>
      </template>

      <template v-else>
        <nav class="tabs" aria-label="活动中心分类"><button v-for="item in activityTabs" :key="item" type="button" :class="{ on: activityTab === item }" :aria-pressed="activityTab === item" @click="activityTab = item">{{ item }}</button></nav>
        <section class="status-card" role="status"><span class="symbol" aria-hidden="true">✦</span><h2>{{ activityTab }}待联通</h2><p>{{ activityCopy[activityTab] }}</p><router-link :to="browseTarget">{{ browseLabel }} →</router-link></section>
      </template>
    </template>
  </div>
</template>

<style scoped>
.detail-page{width:min(100%,620px);margin:auto;padding:18px 16px calc(35px + env(safe-area-inset-bottom,0px));display:grid;gap:15px;color:#2b2118}.page-head{display:flex;align-items:center;gap:12px}.back{width:44px;height:44px;flex:none;border:1px solid #eadfd4;border-radius:12px;background:#fff;color:#b85111;font-size:20px}.eyebrow{margin:0 0 4px;color:#a84710;font-size:10px;font-weight:850;letter-spacing:.13em}.page-head h1{margin:0;font-size:23px}.account-note{margin:0;padding:12px 14px;border-left:3px solid #b85111;border-radius:10px;background:#fff1e7;color:#6e4830;font-size:12px;line-height:1.7}.guest,.status-card{padding:23px;border:1px solid #eadfd4;border-radius:19px;background:#fff}.guest>span,.symbol{width:50px;height:50px;display:grid;place-items:center;border-radius:15px;background:#fff0e2;color:#b85111;font-size:30px}.guest h2,.status-card h2{margin:17px 0 7px;font-size:19px}.guest p,.status-card p{margin:0;color:#6d5f54;font-size:13px;line-height:1.75}.guest a,.status-card a{min-height:44px;display:inline-flex;align-items:center;margin-top:10px;color:#a0440e;text-decoration:none;font-size:13px;font-weight:750}.tabs{display:flex;gap:7px;overflow-x:auto;padding:1px 0 7px;scrollbar-width:thin}.tabs button{min-height:44px;flex:none;padding:0 14px;border:1px solid #eadfd4;border-radius:99px;background:#fff;color:#695d52;font-size:12px;font-weight:700;white-space:nowrap}.tabs button.on{border-color:#b85111;background:#b85111;color:#fff}@media(max-width:350px){.tabs button{padding:0 12px}}
</style>

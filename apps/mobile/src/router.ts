import { createRouter, createWebHashHistory } from 'vue-router';

/**
 * 移动 H5 路由（H5 用 hash 模式：静态托管/扫码直达无需服务端回退配置）。
 * 4-Tab 由 MobileLayout 管理；Tab 内为同一栈（stackAction 规则见 domain）。
 */
export const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/login', name: 'mLogin', component: () => import('./pages/Login.vue'), meta: { task: 'T10.2' } },
    {
      path: '/',
      component: () => import('./pages/MobileLayout.vue'),
      children: [
        { path: '', name: 'mHome', component: () => import('./pages/Home.vue'), meta: { task: 'T10.2' } },
        { path: 'quick-record', name: 'mQuickRecord', component: () => import('./pages/QuickRecord.vue'), meta: { task: 'T10.2' } },
        { path: 'pets', name: 'mPets', component: () => import('./pages/PetTab.vue'), meta: { task: 'T10.2' } },
        { path: 'explore', name: 'mExplore', component: () => import('./pages/ExploreTab.vue'), meta: { task: 'T10.3' } },
        { path: 'news', name: 'mNews', component: () => import('./pages/NewsFeedMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'write', name: 'mWrite', component: () => import('./pages/WriteMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'family', name: 'mFamily', component: () => import('./pages/FamilyMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'family/invite', name: 'mFamilyInvite', component: () => import('./pages/FamilyMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'family/join', name: 'mFamilyJoin', component: () => import('./pages/FamilyMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'me', name: 'mMe', component: () => import('./pages/MeTab.vue'), meta: { task: 'T10.3' } },
        { path: 'settings', name: 'mSettings', component: () => import('./pages/NotifySettings.vue'), meta: { task: 'T10.4' } },
        { path: 'settings/notify', name: 'mNotify', component: () => import('./pages/NotifySettings.vue'), meta: { task: 'T10.4' } },
        { path: 'settings/delete', name: 'mDelete', component: () => import('./pages/DeleteMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'me/sync', name: 'mSync', component: () => import('./pages/SyncMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'todos/:id/confirm', name: 'mTodoX01', component: () => import('./pages/TodoX01.vue'), meta: { task: 'T10.6' } },
        { path: 'pets/:id/share', name: 'mShareX', component: () => import('./pages/XPagesMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'pets/:id/transfer', name: 'mTransferX', component: () => import('./pages/XPagesMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'pets/recycle', name: 'mRecycleX', component: () => import('./pages/XPagesMobile.vue'), meta: { task: 'T10.6' } },
        // 轻量占位：用品详情/POI 详情/商品清单等由本地阶段以 Web 同构组件补全（入口与状态已就位）
        { path: 'goods', redirect: '/' },
        { path: 'goods/:id', redirect: '/' },
        { path: 'explore/poi/:id', redirect: '/explore' },
        { path: 'events', redirect: '/explore' },
        { path: 'news/:slug', redirect: '/news' },
        { path: 'pets/new', redirect: '/pets' },
        { path: 'pets/:id', redirect: '/pets' },
        { path: 'messages', redirect: '/me' },
        { path: 'me/favorites', redirect: '/me' },
        { path: 'me/activity-center', redirect: '/me' },
        { path: ':pathMatch(.*)*', redirect: '/' },
      ],
    },
  ],
});

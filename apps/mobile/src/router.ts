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
        { path: 'pets/new', name: 'mNewPet', component: () => import('./pages/PetFlowMobile.vue'), meta: { task: 'T10.2' } },
        { path: 'pets/:id', name: 'mPetDetail', component: () => import('./pages/PetFlowMobile.vue'), meta: { task: 'T10.2' } },
        { path: 'explore', name: 'mExplore', component: () => import('./pages/ExploreTab.vue'), meta: { task: 'T10.3' } },
        { path: 'news', name: 'mNews', component: () => import('./pages/NewsFeedMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'news/:slug', name: 'mArticle', component: () => import('./pages/ArticleMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'write', name: 'mWrite', component: () => import('./pages/WriteMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'family', name: 'mFamily', component: () => import('./pages/FamilyMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'family/invite', name: 'mFamilyInvite', component: () => import('./pages/FamilyMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'family/join', name: 'mFamilyJoin', component: () => import('./pages/FamilyMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'me', name: 'mMe', component: () => import('./pages/MeTab.vue'), meta: { task: 'T10.3' } },
        { path: 'messages', name: 'mMessages', component: () => import('./pages/AccountDetailMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'me/favorites', name: 'mFavorites', component: () => import('./pages/AccountDetailMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'me/activity-center', name: 'mActivityCenter', component: () => import('./pages/AccountDetailMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'settings', name: 'mSettings', component: () => import('./pages/NotifySettings.vue'), meta: { task: 'T10.4' } },
        { path: 'settings/notify', name: 'mNotify', component: () => import('./pages/NotifySettings.vue'), meta: { task: 'T10.4' } },
        { path: 'settings/delete', name: 'mDelete', component: () => import('./pages/DeleteMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'me/sync', name: 'mSync', component: () => import('./pages/SyncMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'todos/:id/confirm', name: 'mTodoX01', component: () => import('./pages/TodoX01.vue'), meta: { task: 'T10.6' } },
        { path: 'pets/:id/share', name: 'mShareX', component: () => import('./pages/XPagesMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'pets/:id/transfer', name: 'mTransferX', component: () => import('./pages/XPagesMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'pets/recycle', name: 'mRecycleX', component: () => import('./pages/XPagesMobile.vue'), meta: { task: 'T10.6' } },
        { path: 'goods', name: 'mGoods', component: () => import('./pages/GoodsMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'goods/:id', name: 'mGoodsDetail', component: () => import('./pages/GoodsDetailMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'explore/poi/:id', name: 'mPoiDetail', component: () => import('./pages/PoiDetailMobile.vue'), meta: { task: 'T10.3' } },
        { path: 'events', name: 'mEvents', component: () => import('./pages/EventsMobile.vue'), meta: { task: 'T10.3' } },
        { path: ':pathMatch(.*)*', name: 'mNotFound', component: () => import('./pages/NotFoundMobile.vue') },
      ],
    },
  ],
});

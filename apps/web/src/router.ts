import { createRouter, createWebHistory } from 'vue-router';
import { safeReturnPath } from './domain/auth';

/**
 * 用户端 Web 路由总表（信息架构 §4 站点地图）。
 * 本轮落地：T7.1 认证 / T7.2 宠物生命链 / T7.3 首页；
 * 其余任务以 ComingSoon 占位并标注任务号，随 M7 后续轮次替换为真实页面。
 */
export const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ---- T7.3 首页 ----
    { path: '/', name: 'home', component: () => import('./pages/home/Home.vue'), meta: { task: 'T7.3' } },

    // ---- T7.1 认证流 ----
    { path: '/login', name: 'login', component: () => import('./pages/auth/Login.vue'), meta: { task: 'T7.1' } },
    { path: '/login/sms', name: 'loginSms', component: () => import('./pages/auth/Sms.vue'), meta: { task: 'T7.1' } },
    { path: '/login/age', name: 'loginAge', component: () => import('./pages/auth/AgeGate.vue'), meta: { task: 'T7.1' } },
    { path: '/login/guardian', name: 'loginGuardian', component: () => import('./pages/auth/Guardian.vue'), meta: { task: 'T7.1' } },

    // ---- T7.2 宠物生命链 ----
    { path: '/pets', name: 'pets', component: () => import('./pages/pet/PetList.vue'), meta: { task: 'T7.2' } },
    { path: '/pets/new', name: 'petWizard', component: () => import('./pages/pet/Wizard.vue'), meta: { task: 'T7.2' } },
    { path: '/pets/:id', name: 'petDetail', component: () => import('./pages/pet/PetDetail.vue'), meta: { task: 'T7.2' } },
    { path: '/pets/:id/record', name: 'petRecord', component: () => import('./pages/pet/RecordSheet.vue'), meta: { task: 'T7.2' } },
    { path: '/pets/:id/weights', name: 'petWeights', component: () => import('./pages/pet/WeightChart.vue'), meta: { task: 'T7.2' } },
    { path: '/pets/:id/reminders', name: 'petReminders', component: () => import('./pages/pet/ReminderCenter.vue'), meta: { task: 'T7.2' } },

    // ---- T7.4 资讯端 ----
    { path: '/news', name: 'news', component: () => import('./pages/news/NewsFeed.vue'), meta: { task: 'T7.4' } },
    { path: '/news/:slug', name: 'article', component: () => import('./pages/news/ArticleDetail.vue'), meta: { task: 'T7.4' } },
    { path: '/search', name: 'search', component: () => import('./pages/news/SearchPage.vue'), meta: { task: 'T7.4' } },

    // ---- T7.5 创作中心 ----
    { path: '/write', name: 'write', component: () => import('./pages/news/WriteEditor.vue'), meta: { task: 'T7.5' } },
    { path: '/write/:id', name: 'writeEdit', component: () => import('./pages/news/WriteEditor.vue'), meta: { task: 'T7.5' } },
    { path: '/submissions', name: 'submissions', component: () => import('./pages/news/Submissions.vue'), meta: { task: 'T7.5' } },

    // ---- T7.6 探索 ----
    { path: '/explore', name: 'explore', component: () => import('./pages/explore/ExploreMap.vue'), meta: { task: 'T7.6' } },
    { path: '/explore/poi/:id', name: 'poiDetail', component: () => import('./pages/explore/PoiDetail.vue'), meta: { task: 'T7.6' } },
    { path: '/explore/poi/:id/review', name: 'poiReview', component: () => import('./pages/explore/ReviewForm.vue'), meta: { task: 'T7.6' } },
    { path: '/explore/routes', name: 'routes', component: () => import('./pages/explore/RoutesPage.vue'), meta: { task: 'T7.6' } },
    { path: '/events', name: 'events', component: () => import('./pages/explore/EventsPage.vue'), meta: { task: 'T7.6' } },

    // ---- T7.7 用品 ----
    { path: '/goods', name: 'goods', component: () => import('./pages/goods/GoodsChannel.vue'), meta: { task: 'T7.7' } },
    { path: '/goods/:id', name: 'product', component: () => import('./pages/goods/ProductDetail.vue'), meta: { task: 'T7.7' } },
    { path: '/lists/:id', name: 'listDetail', component: () => import('./pages/goods/ListDetail.vue'), meta: { task: 'T7.7' } },

    // ---- T7.8 家庭 + 个人中心 + 消息 + 活动中心 ----
    { path: '/family', name: 'family', component: () => import('./pages/family/FamilyPage.vue'), meta: { task: 'T7.8' } },
    { path: '/family/invite', name: 'familyInvite', component: () => import('./pages/family/FamilyInvite.vue'), meta: { task: 'T7.8' } },
    { path: '/family/join', name: 'familyJoin', component: () => import('./pages/family/FamilyJoin.vue'), meta: { task: 'T7.8' } },
    { path: '/me', name: 'me', component: () => import('./pages/me/Me.vue'), meta: { task: 'T7.8' } },
    { path: '/me/favorites', name: 'favorites', component: () => import('./pages/me/Favorites.vue'), meta: { task: 'T7.8' } },
    { path: '/me/activity-center', name: 'activityCenter', component: () => import('./pages/me/ActivityCenter.vue'), meta: { task: 'T7.8' } },
    { path: '/messages', name: 'messages', component: () => import('./pages/me/Messages.vue'), meta: { task: 'T7.8' } },

    // ---- T7.9 设置与合规 ----
    { path: '/settings', name: 'settings', component: () => import('./pages/settings/Settings.vue'), meta: { task: 'T7.9' } },
    { path: '/settings/privacy', name: 'privacy', component: () => import('./pages/settings/Privacy.vue'), meta: { task: 'T7.9' } },
    { path: '/settings/notifications', name: 'notifications', component: () => import('./pages/settings/Notifications.vue'), meta: { task: 'T7.9' } },
    { path: '/settings/export', name: 'export', component: () => import('./pages/settings/Export.vue'), meta: { task: 'T7.9' } },
    { path: '/settings/delete', name: 'deleteAccount', component: () => import('./pages/settings/DeleteAccount.vue'), meta: { task: 'T7.9' } },
    { path: '/help', name: 'help', component: () => import('./pages/settings/Help.vue'), meta: { task: 'T7.9' } },
    { path: '/legal/:doc', name: 'legal', component: () => import('./pages/settings/LegalDoc.vue'), meta: { task: 'T7.9' } },
    { path: '/blocked', name: 'blocked', component: () => import('./pages/settings/Blocked.vue'), meta: { task: 'T7.9' } },

    // ---- T7.11 闭环页面 X01–X16 的 Web 执行 ----
    { path: '/todos/:id/confirm', name: 'todoConfirm', component: () => import('./pages/todo/TodoConfirm.vue'), meta: { task: 'T7.11' } },
    { path: '/plans/:id', name: 'planDetail', component: () => import('./pages/todo/PlanDetail.vue'), meta: { task: 'T7.11' } },
    { path: '/pets/:id/share', name: 'shareManage', component: () => import('./pages/family/ShareManage.vue'), meta: { task: 'T7.11' } },
    { path: '/pets/:id/transfer', name: 'transferPet', component: () => import('./pages/family/TransferPet.vue'), meta: { task: 'T7.11' } },
    { path: '/family/transfer-admin', name: 'transferAdmin', component: () => import('./pages/family/TransferAdmin.vue'), meta: { task: 'T7.11' } },
    { path: '/pets/recycle', name: 'recycleBin', component: () => import('./pages/pet/RecycleBin.vue'), meta: { task: 'T7.11' } },
    { path: '/me/sync', name: 'syncQueue', component: () => import('./pages/me/SyncQueue.vue'), meta: { task: 'T7.11' } },
    { path: '/me/activity-center/publish', name: 'publishActivity', component: () => import('./pages/me/PublishActivity.vue'), meta: { task: 'T7.11' } },
    { path: '/me/signups/:id', name: 'signupDetail', component: () => import('./pages/me/SignupDetail.vue'), meta: { task: 'T7.11' } },
    { path: '/me/activity-center/published/:id', name: 'activityManage', component: () => import('./pages/me/ActivityManage.vue'), meta: { task: 'T7.11' } },
    { path: '/write/list/new', name: 'listEditor', component: () => import('./pages/news/ListEditor.vue'), meta: { task: 'T7.11' } },
    { path: '/submissions/:id/version', name: 'versionCompare', component: () => import('./pages/news/VersionCompare.vue'), meta: { task: 'T7.11' } },
    { path: '/guardian/control', name: 'guardianControl', component: () => import('./pages/auth/GuardianControl.vue'), meta: { task: 'T7.11' } },

    // ---- 剩余任务占位 ----
    { path: '/:pathMatch(.*)*', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
  ],
});

/** 登录闸门（T7.1）：未登录写操作 → 携安全回跳进 /login（U93 由 safeReturnPath 保证） */
export function requireLogin(loggedIn: boolean, currentPath: string, action: string): string {
  const target = safeReturnPath(currentPath);
  if (loggedIn) return target;
  const query = new URLSearchParams({ return: target, resume: action.slice(0, 80) });
  return '/login?' + query.toString();
}

import { createRouter, createWebHistory } from 'vue-router';

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

    // ---- 后续任务占位（M7 剩余） ----
    { path: '/news', meta: { task: 'T7.4' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/news/:slug', meta: { task: 'T7.4' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/search', meta: { task: 'T7.4' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/write', meta: { task: 'T7.5' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/write/:id', meta: { task: 'T7.5' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/submissions', meta: { task: 'T7.5' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/explore', meta: { task: 'T7.6' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/explore/poi/:id', meta: { task: 'T7.6' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/explore/poi/:id/review', meta: { task: 'T7.6' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/explore/routes', meta: { task: 'T7.6' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/events', meta: { task: 'T7.6' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/goods', meta: { task: 'T7.7' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/goods/:id', meta: { task: 'T7.7' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/lists/:id', meta: { task: 'T7.7' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/family', meta: { task: 'T7.8' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/family/invite', meta: { task: 'T7.8' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/family/join', meta: { task: 'T7.8' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/me', meta: { task: 'T7.8' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/me/favorites', meta: { task: 'T7.8' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/messages', meta: { task: 'T7.8' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/settings', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/settings/privacy', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/settings/notifications', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/settings/export', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/settings/delete', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/help', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/legal/:doc', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/blocked', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
    { path: '/:pathMatch(.*)*', meta: { task: 'T7.9' }, component: () => import('./pages/ComingSoon.vue') },
  ],
});

/** 登录闸门（T7.1）：未登录写操作 → 携安全回跳进 /login（U93 由 safeReturnPath 保证） */
export function requireLogin(
  loggedIn: boolean,
  currentPath: string,
  action: string,
): string | '/login' {
  if (loggedIn) return '/login';
  const sep = currentPath.includes('?') ? '&' : '?';
  const target = encodeURIComponent(currentPath);
  return `/login${sep}return=${target}&resume=${encodeURIComponent(action)}`;
}

import { createRouter, createWebHistory } from 'vue-router';
import { isPreviewActive } from './adminPreview';

/**
 * 管理端路由（信息架构 §7 侧边栏结构）。
 * 当前仅允许显式进入内存只读预览；真实权限仍须由服务端逐路由及逐对象校验。
 */
export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'adminLogin', component: () => import('./pages/AdminLogin.vue'), meta: { task: 'T8.1' } },
    {
      path: '/',
      component: () => import('./pages/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: () => import('./pages/Dashboard.vue'), meta: { task: 'T8.5' } },
        { path: 'todos', name: 'opsTodos', component: () => import('./pages/OpsTodos.vue'), meta: { task: 'T8.6' } },
        { path: 'review', name: 'review', component: () => import('./pages/ReviewWorkbench.vue'), meta: { task: 'T8.2' } },
        { path: 'content', name: 'contentOps', component: () => import('./pages/ContentOps.vue'), meta: { task: 'T8.3' } },
        { path: 'products', name: 'productOps', component: () => import('./pages/ProductOps.vue'), meta: { task: 'T8.3' } },
        { path: 'explore', name: 'exploreOps', component: () => import('./pages/ExploreOps.vue'), meta: { task: 'T8.3' } },
        { path: 'credentials', name: 'credentials', component: () => import('./pages/CredentialReview.vue'), meta: { task: 'T8.6' } },
        { path: 'governance', name: 'governance', component: () => import('./pages/UserGovernance.vue'), meta: { task: 'T8.4' } },
        { path: 'audit', name: 'audit', component: () => import('./pages/AuditLogs.vue'), meta: { task: 'T8.5' } },
        { path: 'roles', name: 'roles', component: () => import('./pages/RolesAdmin.vue'), meta: { task: 'T8.5' } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
});

router.beforeEach((to) => {
  if (to.name === 'adminLogin' || isPreviewActive()) return true;
  return { name: 'adminLogin', query: { return: to.fullPath } };
});

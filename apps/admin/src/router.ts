import { createRouter, createWebHistory } from 'vue-router';

/**
 * 管理端路由（信息架构 §7 侧边栏结构）。
 * 权限过滤在 AdminLayout（visibleNav）；路由本身按 T 编号标注任务归属。
 */
export const router = createRouter({
  history: createWebHistory('/admin/'),
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

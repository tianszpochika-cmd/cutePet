import { createRouter, createWebHistory } from 'vue-router';

/** 官网路由（信息架构 §8：产品·服务·内容·下载·关于·联系 + 分享页）。 */
export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('./pages/SiteLayout.vue'),
      children: [
        { path: '', name: 'siteHome', component: () => import('./pages/Home.vue'), meta: { task: 'T9.1' } },
        { path: 'products', name: 'siteProducts', component: () => import('./pages/Products.vue'), meta: { task: 'T9.2' } },
        { path: 'products/:id', name: 'siteProductDetail', component: () => import('./pages/ProductDetail.vue'), meta: { task: 'T9.2' } },
        { path: 'family', name: 'siteFamily', component: () => import('./pages/FamilyLanding.vue'), meta: { task: 'T9.2' } },
        { path: 'services', name: 'siteServices', component: () => import('./pages/Services.vue'), meta: { task: 'T9.3' } },
        { path: 'events', name: 'siteEvents', component: () => import('./pages/SiteEvents.vue'), meta: { task: 'T9.3' } },
        { path: 'cases', name: 'siteCases', component: () => import('./pages/Cases.vue'), meta: { task: 'T9.3' } },
        { path: 'center', name: 'siteCenter', component: () => import('./pages/Center.vue'), meta: { task: 'T9.3' } },
        { path: 'download', name: 'siteDownload', component: () => import('./pages/Download.vue'), meta: { task: 'T9.3' } },
        { path: 'about', name: 'siteAbout', component: () => import('./pages/About.vue'), meta: { task: 'T9.4' } },
        { path: 'contact', name: 'siteContact', component: () => import('./pages/Contact.vue'), meta: { task: 'T9.4' } },
        { path: 'help', name: 'siteHelp', component: () => import('./pages/SiteHelp.vue'), meta: { task: 'T9.4' } },
        { path: 'legal/:doc', name: 'siteLegal', component: () => import('./pages/SiteLegal.vue'), meta: { task: 'T9.4' } },
        { path: 'campaign', name: 'siteCampaign', component: () => import('./pages/Campaign.vue'), meta: { task: 'T9.4' } },
        { path: 'brand', name: 'siteBrand', component: () => import('./pages/About.vue'), meta: { task: 'T9.5' } },
        { path: 'share/:kind/:id', name: 'siteShare', component: () => import('./pages/SharePreview.vue'), meta: { task: 'T9.6' } },
        { path: ':pathMatch(.*)*', name: 'site404', component: () => import('./pages/NotFound.vue'), meta: { task: 'T9.4' } },
      ],
    },
  ],
});

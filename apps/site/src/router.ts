import { createRouter, createWebHistory } from 'vue-router';
import { FOUR_SECTIONS } from './domain/site';

const legalTitles: Record<string, string> = {
  terms: '用户协议',
  privacy: '隐私政策',
  community: '社区规范',
  children: '儿童个人信息处理规则',
  report: '投诉举报与版权',
};

/** 官网路由（信息架构 §8：产品·服务·内容·下载·关于·联系 + 分享页）。 */
export const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition;
    if (to.hash) return { el: to.hash };
    return to.path !== from.path ? { top: 0 } : false;
  },
  routes: [
    {
      path: '/',
      component: () => import('./pages/SiteLayout.vue'),
      children: [
        { path: '', name: 'siteHome', component: () => import('./pages/Home.vue'), meta: { task: 'T9.1', title: '首页' } },
        { path: 'products', name: 'siteProducts', component: () => import('./pages/Products.vue'), meta: { task: 'T9.2', title: '产品能力' } },
        { path: 'products/:id', name: 'siteProductDetail', component: () => import('./pages/ProductDetail.vue'), meta: { task: 'T9.2', title: '产品能力' } },
        { path: 'family', name: 'siteFamily', component: () => import('./pages/FamilyLanding.vue'), meta: { task: 'T9.2', title: '家庭共享' } },
        { path: 'services', name: 'siteServices', component: () => import('./pages/Services.vue'), meta: { task: 'T9.3', title: '使用场景' } },
        { path: 'events', name: 'siteEvents', component: () => import('./pages/SiteEvents.vue'), meta: { task: 'T9.3', title: '本地活动' } },
        { path: 'cases', name: 'siteCases', component: () => import('./pages/Cases.vue'), meta: { task: 'T9.3', title: '使用案例' } },
        { path: 'center', name: 'siteCenter', component: () => import('./pages/Center.vue'), meta: { task: 'T9.3', title: '养宠资讯' } },
        { path: 'download', name: 'siteDownload', component: () => import('./pages/Download.vue'), meta: { task: 'T9.3', title: '开始使用' } },
        { path: 'about', name: 'siteAbout', component: () => import('./pages/About.vue'), meta: { task: 'T9.4', title: '关于我们' } },
        { path: 'contact', name: 'siteContact', component: () => import('./pages/Contact.vue'), meta: { task: 'T9.4', title: '联系我们' } },
        { path: 'help', name: 'siteHelp', component: () => import('./pages/SiteHelp.vue'), meta: { task: 'T9.4', title: '帮助中心' } },
        { path: 'legal/:doc', name: 'siteLegal', component: () => import('./pages/SiteLegal.vue'), meta: { task: 'T9.4', title: '使用规则' } },
        { path: 'campaign', name: 'siteCampaign', component: () => import('./pages/Campaign.vue'), meta: { task: 'T9.4', title: '专题' } },
        { path: 'brand', name: 'siteBrand', component: () => import('./pages/About.vue'), meta: { task: 'T9.5', title: '关于我们' } },
        { path: 'share/:kind/:id', name: 'siteShare', component: () => import('./pages/SharePreview.vue'), meta: { task: 'T9.6', title: '分享内容' } },
        { path: ':pathMatch(.*)*', name: 'site404', component: () => import('./pages/NotFound.vue'), meta: { task: 'T9.4', title: '页面未找到' } },
      ],
    },
  ],
});

router.afterEach((to) => {
  if (typeof document === 'undefined') return;
  let title = typeof to.meta.title === 'string' ? to.meta.title : '官网';
  if (to.name === 'siteProductDetail') {
    title = FOUR_SECTIONS.find((item) => item.id === to.params.id)?.label ?? '能力未找到';
  } else if (to.name === 'siteLegal') {
    title = legalTitles[String(to.params.doc)] ?? '规则未找到';
  }
  document.title = to.name === 'siteHome' ? 'cutePet · 让每一天都更懂你的宠物' : `${title} · cutePet`;
});

<script setup lang="ts">
import { SITE_NAV } from '../domain/site';
</script>

<template>
  <div class="site">
    <header class="top">
      <router-link class="logo" to="/">🐾 cutePet</router-link>
      <nav>
        <router-link v-for="n in SITE_NAV" :key="n.id" :to="n.to">{{ n.label }}</router-link>
      </nav>
      <router-link class="cta" to="/download">立即体验</router-link>
    </header>

    <main>
      <router-view />
    </main>

    <footer class="foot">
      <div class="cols">
        <div>
          <strong>cutePet</strong>
          <p>宠物管理 · 宠物资讯 · 宠物探索 · 宠物用品（纯内容导购）</p>
        </div>
        <div>
          <strong>快速链接</strong>
          <p>
            <router-link to="/products">产品</router-link> ·
            <router-link to="/services">场景</router-link> ·
            <router-link to="/center">内容中心</router-link> ·
            <router-link to="/download">下载</router-link>
          </p>
        </div>
        <div>
          <strong>合规</strong>
          <p>
            <router-link to="/contact">投诉举报与版权通道</router-link><br />
            用户协议 / 随私政策 / 社区规范 / 儿童信息规则见应用内「规则」页
          </p>
        </div>
      </div>
      <p class="icp">
        © 2026 cutePet（占位品牌名） · ICP 备案信息位（上线前补充） · 联系邮箱见「联系」页
      </p>
    </footer>
  </div>
</template>

<style scoped>
.top {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 68px;
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 0 32px;
  background: rgba(255, 249, 243, 0.92);
  backdrop-filter: blur(12px);
}
.logo { font-weight: 800; font-size: 18px; color: #2b2118; text-decoration: none; }
nav { display: flex; gap: 22px; }
nav a { color: #7a6e63; text-decoration: none; font-size: 15px; }
nav a.router-link-active { color: #ff7a2f; font-weight: 600; }
.cta { margin-left: auto; background: #ff7a2f; color: #fff; border-radius: 999px; padding: 9px 20px; text-decoration: none; font-size: 14px; font-weight: 600; }
main { min-height: 60vh; }
.foot { background: #2b2118; color: #f7f1ea; padding: 40px 32px 24px; margin-top: 64px; }
.cols { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 24px; max-width: 1120px; margin: 0 auto; }
.foot strong { display: block; margin-bottom: 8px; font-size: 14px; }
.foot p { margin: 0; color: #a89b8f; font-size: 13px; line-height: 1.8; }
.foot a { color: #ffd0b3; text-decoration: none; }
.icp { max-width: 1120px; margin: 28px auto 0; padding-top: 16px; border-top: 1px solid #463a30; color: #a89b8f; font-size: 12px; }
</style>

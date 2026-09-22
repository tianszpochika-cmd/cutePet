<script setup lang="ts">
// 全局框架：顶栏导航（信息架构 §2）+ 路由出口
</script>

<template>
  <div class="app">
    <header class="topbar">
      <router-link class="logo" to="/">🐾 cutePet</router-link>
      <nav>
        <router-link to="/">首页</router-link>
        <router-link to="/pets">宠物管理</router-link>
        <router-link to="/news">资讯</router-link>
        <router-link to="/explore">探索</router-link>
        <router-link to="/goods">用品</router-link>
      </nav>
      <div class="right">
        <router-link to="/messages">🔔</router-link>
        <router-link to="/family">👨‍👩‍👧</router-link>
        <router-link class="login" to="/login">登录</router-link>
      </div>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.topbar {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  box-shadow: 0 2px 8px rgba(43, 33, 24, 0.06);
  position: sticky;
  top: 0;
  z-index: 10;
}
.logo {
  font-weight: 700;
  color: #2b2118;
  text-decoration: none;
}
nav {
  display: flex;
  gap: 20px;
}
nav a {
  color: #7a6e63;
  text-decoration: none;
  font-size: 15px;
}
nav a.router-link-active {
  color: #ff7a2f;
  font-weight: 600;
}
.right {
  margin-left: auto;
  display: flex;
  gap: 16px;
  align-items: center;
}
.right a {
  text-decoration: none;
  color: #7a6e63;
}
.right .login {
  color: #ff7a2f;
  font-weight: 600;
}
main {
  min-height: calc(100vh - 64px);
}
</style>

<template>
  <section class="not-found"><span aria-hidden="true">✿</span><h1>这个页面暂时找不到</h1><p>你可以返回首页，继续查看今天的照护、资讯与探索。</p><router-link to="/">返回首页</router-link></section>
</template>
<style scoped>
.not-found { min-height: 65vh; display: grid; align-content: center; justify-items: center; gap: 10px; padding: 24px; text-align: center; }
.not-found span { color: var(--primary); font-size: 48px; }
.not-found h1 { margin: 0; font-size: 23px; }
.not-found p { margin: 0 0 12px; color: var(--ink-2); font-size: 13px; line-height: 1.7; }
.not-found a { display: inline-flex; align-items: center; justify-content: center; min-height: 46px; padding: 0 22px; border-radius: 12px; color: #fff; background: var(--primary); text-decoration: none; font-weight: 700; }
</style>

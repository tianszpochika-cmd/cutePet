<script setup lang="ts">
import { computed, ref } from 'vue';
import { PRODUCTS, downloadLandingModel, downloadFor, DOWNLOAD_SOURCE, type ClientEnd } from '../domain/site';

// T9.6：UA 识别端 → 落地页主推；用户可手动切换端查看（信息结构一致）
const ua = typeof navigator === 'undefined' ? 'web' : navigator.userAgent;
const detected = downloadLandingModel(ua).end;
const active = ref<ClientEnd>(detected);
const model = computed(() => downloadFor(active.value));

const qrPlaceholder = 'QR（部署期生成：安装地址+校验值二维码，非应用商店分发）';
</script>

<template>
  <div class="download">
    <h1>下载与体验</h1>
    <p class="lead">
      分发渠道：设计预览版（<strong>{{ DOWNLOAD_SOURCE.channel }}</strong>）——
      不通过应用商店分发；发布包将附 SHA-256 校验与签名。
    </p>

    <nav class="ends">
      <button
        v-for="p in PRODUCTS"
        :key="p.id"
        type="button"
        :class="{ on: active === p.id }"
        :data-testid="`end-${p.id}`"
        @click="active = p.id as ClientEnd"
      >
        {{ p.label }}{{ p.id === detected ? '（当前端）' : '' }}
      </button>
    </nav>

    <section class="panel" data-testid="landing">
      <div class="info">
        <h2>{{ model.productLabel }}</h2>
        <dl>
          <div><dt>版本</dt><dd>{{ model.version }}</dd></div>
          <div><dt>更新</dt><dd>{{ model.updated }}</dd></div>
          <div><dt>渠道</dt><dd>{{ model.channel }}</dd></div>
          <div><dt>校验</dt><dd>{{ model.checksumNote }}</dd></div>
          <div><dt>入口</dt><dd>{{ model.openHref }}</dd></div>
        </dl>
        <h3>本端特性</h3>
        <ul><li v-for="f in model.features" :key="f">{{ f }}</li></ul>
        <div class="actions">
          <a class="primary" :href="model.openHref" target="_blank" rel="noopener">打开 {{ model.productLabel }}</a>
          <span class="note">结构与其余三端完全一致（仅端标识不同）——跨端一致性任务核心</span>
        </div>

        <!-- T9.6 手机入口（手机号验证码登录直达） -->
        <div class="phone-entry" data-testid="phone-entry">
          <strong>手机快捷入口</strong>
          <p>手机号验证码一键登录（与 Web / 移动端同一登录流，dev 固定码 123456）。</p>
          <a class="ghost" href="http://localhost:18580/login" target="_blank" rel="noopener">手机号登录 →</a>
        </div>
      </div>
      <div class="qr">
        <div class="qr-box" aria-label="二维码占位">{{ qrPlaceholder }}</div>
        <p class="meta">扫码直达本端入口；校园/展会渠道可贴此物料。</p>
      </div>
    </section>

    <section class="changelog">
      <h2>更新日志</h2>
      <ul>
        <li><strong>{{ DOWNLOAD_SOURCE.version }}</strong>（{{ DOWNLOAD_SOURCE.updated }}）：四大板块落地、闭环 X01–X16 执行完成、管理端工作台上线（设计预览）。</li>
      </ul>
      <p class="meta">运行验证与真机安装属本地测试阶段（E01–E08 门禁项），本机不做任何安装/启动。</p>
    </section>
  </div>
</template>

<style scoped>
.download { max-width: 960px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 26px; }
h1 { font-size: 32px; margin: 0; }
.lead { color: #7a6e63; font-size: 15px; line-height: 1.8; margin: 0; }
.ends { display: flex; gap: 8px; flex-wrap: wrap; }
.ends button { height: 36px; padding: 0 16px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 13px; cursor: pointer; }
.ends button.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
.panel { background: #fff; border-radius: 24px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 32px; display: grid; grid-template-columns: 1.6fr 1fr; gap: 28px; }
.info h2 { margin: 0 0 14px; font-size: 22px; }
dl { margin: 0 0 16px; display: grid; gap: 8px; }
dl div { display: flex; gap: 12px; font-size: 14px; }
dt { color: #7a6e63; width: 44px; }
dd { margin: 0; color: #2b2118; font-weight: 600; }
.info h3 { font-size: 14px; color: #7a6e63; margin: 0 0 8px; }
ul { margin: 0 0 18px; padding-left: 18px; font-size: 14px; color: #2b2118; display: grid; gap: 6px; }
.actions { display: grid; gap: 10px; justify-items: start; }
.phone-entry { background: #fff9f3; border-radius: 16px; padding: 16px; display: grid; gap: 8px; }
.phone-entry strong { font-size: 14px; }
.phone-entry p { margin: 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.phone-entry .ghost { justify-self: start; background: #fff; color: #ff7a2f; border-radius: 999px; padding: 9px 20px; text-decoration: none; font-weight: 600; box-shadow: inset 0 0 0 1px #ffb98a; }
.primary { background: #ff7a2f; color: #fff; border-radius: 999px; padding: 13px 28px; text-decoration: none; font-weight: 600; }
.note { color: #4d8dff; font-size: 13px; }
.qr { display: grid; gap: 12px; place-items: center; }
.qr-box { width: 180px; height: 180px; border-radius: 16px; background: repeating-conic-gradient(#2b2118 0 25%, #fff 0 50%) 0 0 / 24px 24px; display: grid; place-items: center; text-align: center; color: #fff; font-size: 11px; padding: 10px; text-shadow: 0 1px 4px #000; }
.changelog h2 { font-size: 20px; }
.changelog ul { padding-left: 18px; }
.meta { color: #7a6e63; font-size: 13px; }
@media (max-width: 760px) { .panel { grid-template-columns: 1fr; } }
</style>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { guestCapabilities } from '../../domain/settings';

const router = useRouter();
// #31：未同意隐私 → 受限游客（不加载可选分析/定位）
const agreed = reactive({ privacy: true });
const caps = () => guestCapabilities(agreed.privacy);

const toggles = reactive({ profileDiscoverable: false, shareAnalytics: false });
</script>

<template>
  <div class="privacy">
    <header>
      <button type="button" class="back" @click="router.push('/settings')">‹ 设置</button>
      <h1>隐私设置</h1>
    </header>

    <ul class="list">
      <li>
        <span>档案可见性</span>
        <em>仅自己与家庭成员（唯一选项，设计如此）</em>
      </li>
      <li>
        <span>互动可见性</span>
        <select v-model="toggles.profileDiscoverable">
          <option :value="false">仅自己</option>
          <option :value="true">所有人</option>
        </select>
      </li>
      <li>
        <span>可选分析数据</span>
        <select v-model="toggles.shareAnalytics">
          <option :value="false">不提供（默认）</option>
          <option :value="true">提供（匿名统计）</option>
        </select>
      </li>
      <li>
        <span>隐私政策同意状态</span>
        <label><input v-model="agreed.privacy" type="checkbox" data-testid="privacy-agree" /> 已同意</label>
      </li>
    </ul>

    <p class="hint" data-testid="guest-caps">
      当前能力：分析 {{ caps().analytics ? '已启用' : '未加载（受限游客）' }} ·
      定位 {{ caps().geo ? '已启用' : '未加载（受限游客）' }}（#31：未同意不加载可选能力）
    </p>
    <button type="button" class="ghost" @click="router.push('/legal/privacy')">查看隐私政策全文</button>
  </div>
</template>

<style scoped>
.privacy { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 14px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.list { list-style: none; padding: 0; display: grid; gap: 8px; }
.list li { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; display: flex; justify-content: space-between; align-items: center; font-size: 14px; }
.list em { color: #7a6e63; font-style: normal; font-size: 13px; }
.hint { background: #fff1e8; color: #b45309; border-radius: 12px; padding: 12px; font-size: 13px; line-height: 1.7; }
.ghost { height: 44px; border: none; border-radius: 999px; background: #fff; color: #ff7a2f; box-shadow: inset 0 0 0 1px #f0e6dc; }
</style>

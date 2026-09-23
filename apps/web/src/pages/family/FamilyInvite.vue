<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { JOIN_SCOPE_COPY } from '../../domain/family';

const router = useRouter();
const family = ref<{ name: string; role: string } | null>(null);
const loading = ref(true);
const invite = ref<{ code: string; expiresAt: string } | null>(null);
const busy = ref(false);
const error = ref('');
const notice = ref('');
const canInvite = computed(() => family.value?.role === 'OWNER' || family.value?.role === 'ADMIN');
const canShareLink = typeof window !== 'undefined' && !['localhost', '127.0.0.1'].includes(window.location.hostname);

onMounted(async () => {
  try {
    const result = await api.familyMine() as { name?: unknown; role?: unknown };
    if (typeof result.name !== 'string' || typeof result.role !== 'string') throw new Error('家庭信息暂时无法读取');
    family.value = { name: result.name, role: result.role };
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '家庭信息加载失败';
  } finally {
    loading.value = false;
  }
});

async function generate() {
  if (!canInvite.value || busy.value) return;
  busy.value = true;
  error.value = '';
  notice.value = '';
  try {
    const result = await api.familyInviteCreate() as { code?: unknown; expiresAt?: unknown };
    if (typeof result.code !== 'string' || !result.code ||
        typeof result.expiresAt !== 'string') throw new Error('邀请结果暂时无法读取');
    invite.value = { code: result.code, expiresAt: result.expiresAt };
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '生成邀请码失败，请重试';
  } finally {
    busy.value = false;
  }
}

async function copy(kind: 'code' | 'link') {
  if (!invite.value) return;
  if (kind === 'link' && !canShareLink) {
    error.value = '当前地址只在本机可访问，请复制邀请码发给家人';
    return;
  }
  const value = kind === 'code'
    ? invite.value.code
    : new URL(router.resolve({ path: '/family/join', query: { code: invite.value.code } }).href, window.location.origin).href;
  error.value = '';
  notice.value = '';
  try {
    if (!navigator.clipboard?.writeText) throw new Error('浏览器不支持自动复制，请手动选中邀请码');
    await navigator.clipboard.writeText(value);
    notice.value = kind === 'code' ? '邀请码已复制' : '邀请链接已复制';
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '复制失败，请手动选中邀请码';
  }
}
</script>

<template>
  <div class="invite-page">
    <nav><router-link to="/family">← 返回家庭</router-link></nav>
    <header><p class="eyebrow">家庭邀请</p><h1>让家人知道，能一起照顾什么。</h1><p>分享邀请码前，先向家人说明宠物档案的共享范围。新成员加入后默认只读。</p></header>
    <section class="scope"><h2>加入前的说明</h2><p>{{ JOIN_SCOPE_COPY }}</p></section>

    <p v-if="loading" class="state" role="status">正在读取家庭信息…</p>
    <section v-else-if="!family" class="state"><h2>暂时无法生成邀请</h2><p>请先确认已经加入家庭，并能正常读取家庭信息。</p><router-link to="/family">返回家庭页 →</router-link></section>
    <section v-else-if="!canInvite" class="state"><h2>当前身份不能邀请成员</h2><p>你在“{{ family.name }}”中是普通成员。请联系家庭创建者或管理员获取邀请。</p></section>
    <template v-else>
      <section class="invite-card">
        <div class="card-top"><div><span>来自</span><h2>{{ family.name }}</h2></div><span class="badge">由家庭创建者或管理员生成</span></div>
        <template v-if="invite"><p class="code" data-testid="invite-card">{{ invite.code }}</p><p class="expiry">有效至 {{ invite.expiresAt.replace('T', ' ').slice(0, 16) }}。再次生成新邀请码不会自动使旧码失效。</p><div class="actions"><button type="button" class="primary" @click="copy('code')">复制邀请码</button><button v-if="canShareLink" type="button" class="secondary" @click="copy('link')">复制邀请链接</button></div></template>
        <p v-else class="hint">确认家庭名称与共享说明后，再生成真正的邀请码。</p>
        <button type="button" class="text-button" :disabled="busy" data-testid="generate" @click="generate">{{ busy ? '生成中…' : invite ? '再生成一个邀请码' : '生成邀请码' }}</button>
      </section>
      <p v-if="!canShareLink" class="local-note">当前为本地访问地址，复制邀请码比分享链接更合适。</p>
    </template>
    <p v-if="error" class="message error" role="alert">{{ error }}</p>
    <p v-if="notice" class="message success" role="status">{{ notice }}</p>
  </div>
</template>

<style scoped>
.invite-page{width:min(660px,calc(100% - 32px));margin:0 auto;padding:28px 0 90px;display:grid;gap:18px}nav a{display:inline-flex;align-items:center;min-height:44px;color:#5c3c8c;text-decoration:none;font-weight:700}.eyebrow{margin:0 0 8px;color:#73539a;font-size:12px;font-weight:800;letter-spacing:.08em}header h1{margin:0;font-size:clamp(29px,4vw,42px);line-height:1.2}header>p:last-child{margin:12px 0 0;color:#6d6259}.scope,.state,.invite-card{padding:24px;border:1px solid #e9ded3;border-radius:19px;background:#fff}.scope{background:#f4eefb}.scope h2,.state h2{margin:0 0 8px;font-size:17px}.scope p,.state p{margin:0;color:#5d4a70;font-size:14px;line-height:1.75}.state a{display:inline-flex;margin-top:12px;color:#5c3c8c}.card-top{display:flex;justify-content:space-between;gap:14px;align-items:start}.card-top span:first-child{color:#6d6259;font-size:12px}.card-top h2{margin:3px 0 0;font-size:24px}.badge{padding:5px 11px;border-radius:999px;background:#f4eefb;color:#5c3c8c;font-size:11px}.code{overflow-wrap:anywhere;margin:25px 0 8px;padding:22px;border-radius:14px;background:#f8f3fc;color:#4b2a7c;font-size:clamp(22px,4vw,31px);font-weight:800;letter-spacing:.13em;text-align:center}.expiry,.hint,.local-note{color:#6d6259;font-size:13px;line-height:1.7}.actions{display:flex;flex-wrap:wrap;gap:10px;margin-top:20px}.actions button,.text-button{min-height:46px;padding:10px 20px;border:0;border-radius:999px;font-weight:700;cursor:pointer}.primary{background:#5c3c8c;color:#fff}.secondary{background:#fff;color:#5c3c8c;box-shadow:inset 0 0 0 1px #d9cce5}.text-button{margin-top:15px;background:#f2eafa;color:#5c3c8c}.text-button:disabled{opacity:.55}.local-note{margin:0}.message{margin:0;padding:12px 15px;border-radius:12px;font-size:13px}.error{background:#fdecec;color:#a43636}.success{background:#e7f8ef;color:#176c3f}@media(max-width:560px){.card-top{flex-direction:column}.actions button,.text-button{width:100%}}
</style>

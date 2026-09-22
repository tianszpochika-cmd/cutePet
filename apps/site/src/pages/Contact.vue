<script setup lang="ts">
import { reactive, ref } from 'vue';
import { contactBlockers, CONTACT_CHANNELS } from '../domain/site';

const form = reactive({ name: '', email: '', kind: '问题反馈', message: '' });
const attempted = ref(false);
const submitted = ref(false);
const error = ref('');

const blockers = () => (attempted.value ? contactBlockers(form) : []);

function submit() {
  attempted.value = true;
  const out = contactBlockers(form);
  if (out.length > 0) {
    error.value = out.join(' / ');
    return;
  }
  error.value = '';
  submitted.value = true;
}
</script>

<template>
  <div class="contact">
    <h1>联系我们</h1>

    <section class="channels">
      <article v-for="c in CONTACT_CHANNELS" :key="c.id">
        <h3>{{ c.label }}</h3>
        <p>{{ c.info }}</p>
      </article>
    </section>

    <form class="form" @submit.prevent="submit">
      <label>称呼 <input v-model="form.name" data-testid="name" /></label>
      <label>邮箱 <input v-model="form.email" type="email" data-testid="email" placeholder="you@example.com" /></label>
      <label>类型
        <select v-model="form.kind" data-testid="kind">
          <option v-for="k in ['商务合作', '媒体采访', '问题反馈', '投诉举报']" :key="k">{{ k }}</option>
        </select>
      </label>
      <label>留言（10–1000 字）
        <textarea v-model="form.message" rows="6" data-testid="message" />
      </label>

      <p v-for="b in blockers()" :key="b" class="err" data-testid="blocker">{{ b }}</p>
      <p v-if="error" class="err">{{ error }}</p>

      <button v-if="!submitted" type="submit" class="primary" data-testid="submit">提交</button>
      <p v-else class="ok" data-testid="ok">已收到，我们会在 2 个工作日内通过邮箱回复。</p>
    </form>

    <p class="meta">投诉举报请优先使用应用内举报入口（48 小时内反馈处理结果）；版权通知-删除见规则页通道。</p>
  </div>
</template>

<style scoped>
.contact { max-width: 720px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 24px; }
h1 { font-size: 32px; margin: 0; }
.channels { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; }
.channels article { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 18px; }
.channels h3 { margin: 0 0 8px; font-size: 15px; }
.channels p { margin: 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.form { background: #fff; border-radius: 24px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 28px; display: grid; gap: 14px; }
label { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
input, select, textarea { border: 1px solid #f0e6dc; border-radius: 12px; padding: 12px 14px; font-size: 15px; font-family: inherit; font-weight: 400; background: #fff; }
.primary { height: 48px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; font-size: 15px; }
.err { color: #ef4444; font-size: 13px; margin: 0; }
.ok { color: #22c55e; font-size: 14px; margin: 0; }
.meta { color: #7a6e63; font-size: 13px; line-height: 1.7; }
</style>

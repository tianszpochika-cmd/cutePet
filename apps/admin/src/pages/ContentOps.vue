<script setup lang="ts">
import { ref } from 'vue';
import { takedownBlockers, pinAllowed, taxonomyTagValid, bannerScheduleValid } from '../domain/operations';

const articles = ref([
  { id: 1, title: '幼猫换粮的七个误区', state: 'PUBLISHED', top: false, channel: '猫' },
  { id: 2, title: '夏季饮水注意', state: 'PUBLISHED', top: true, channel: '营养' },
  { id: 3, title: '待审：训练心得', state: 'PENDING', top: false, channel: '训练行为' },
]);
const message = ref('');
const takedownId = ref<number | null>(null);
const takedownReason = ref('');
const newTag = ref('');
const banner = ref({ slot: 'home_top', title: '', startsAt: '', endsAt: '' });

function takedown(id: number) {
  const blockers = takedownBlockers(takedownReason.value);
  if (blockers.length > 0) {
    message.value = '下架必填原因';
    return;
  }
  const row = articles.value.find((a) => a.id === id);
  if (row) {
    row.state = 'TAKEDOWN';
    row.top = false;
  }
  message.value = `#${id} 已下架（原因已记录、进入审计）；治理清除走治理工单并传播（U88）`;
  takedownId.value = null;
  takedownReason.value = '';
}

function togglePin(id: number) {
  const row = articles.value.find((a) => a.id === id);
  if (!row) return;
  if (!pinAllowed(row.state)) {
    message.value = '置顶仅限已发布内容';
    return;
  }
  row.top = !row.top;
  message.value = `#${id} ${row.top ? '已置顶' : '已取消置顶'}（article.pinned.schedule）`;
}

function addTag() {
  if (!taxonomyTagValid(newTag.value)) {
    message.value = '标签需 1–24 字符';
    return;
  }
  message.value = `标签「${newTag.value.trim()}」已创建/合并（taxonomy.manage）`;
  newTag.value = '';
}

function saveBanner() {
  if (!bannerScheduleValid(banner.value.startsAt, banner.value.endsAt)) {
    message.value = '排期：结束必须晚于开始（或留空=常驻）';
    return;
  }
  message.value = `推荐位「${banner.value.slot}」已保存（banner.manage）`;
}
</script>

<template>
  <div class="content-ops">
    <section class="card">
      <h2>文章管理</h2>
      <table>
        <thead>
          <tr><th>标题</th><th>频道</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="a in articles" :key="a.id">
            <td>{{ a.title }} <span v-if="a.top" class="pin">置顶</span></td>
            <td>{{ a.channel }}</td>
            <td>{{ a.state }}</td>
            <td class="ops">
              <button type="button" class="chip" @click="togglePin(a.id)">{{ a.top ? '取消置顶' : '置顶' }}</button>
              <button type="button" class="chip danger" @click="takedownId = a.id">下架</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="takedownId !== null" class="inline">
        <input v-model="takedownReason" placeholder="下架原因（必填，进入审计）" data-testid="takedown-reason" />
        <button type="button" class="chip danger" data-testid="takedown-confirm" @click="takedown(takedownId)">确认下架 #{{ takedownId }}</button>
        <button type="button" class="chip" @click="takedownId = null">取消</button>
      </div>
    </section>

    <section class="card">
      <h2>分类与标签</h2>
      <div class="inline">
        <input v-model="newTag" placeholder="新标签（1–24 字）" data-testid="tag-input" />
        <button type="button" class="chip" data-testid="tag-add" @click="addTag">创建/合并</button>
      </div>
      <p class="meta">频道固定 7 个；标签重复自动合并（taxonomy.manage）。</p>
    </section>

    <section class="card">
      <h2>推荐位（Banner）</h2>
      <div class="inline">
        <select v-model="banner.slot"><option value="home_top">首页顶部</option><option value="product_hero">用品头图</option></select>
        <input v-model="banner.title" placeholder="标题" />
        <input v-model="banner.startsAt" type="date" />
        <span>→</span>
        <input v-model="banner.endsAt" type="date" />
        <button type="button" class="chip" data-testid="banner-save" @click="saveBanner">保存排期</button>
      </div>
    </section>

    <p v-if="message" class="msg" data-testid="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.content-ops { display: grid; gap: 14px; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 15px; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 8px 10px; font-size: 13px; border-bottom: 1px solid #f0e6dc; }
th { color: #7a6e63; font-size: 12px; }
.pin { color: #ff7a2f; font-size: 11px; font-weight: 600; }
.ops { display: flex; gap: 6px; }
.chip { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 5px 12px; font-size: 12px; cursor: pointer; }
.chip.danger { background: #fdecec; color: #b91c1c; }
.inline { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.inline input, .inline select { height: 36px; border: 1px solid #f0e6dc; border-radius: 10px; padding: 0 10px; font-size: 13px; }
.meta { margin: 0; color: #7a6e63; font-size: 13px; }
.msg { background: #e7f8ef; color: #15803d; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin: 0; }
</style>

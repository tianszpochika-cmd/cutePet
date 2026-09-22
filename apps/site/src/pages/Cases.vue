<script setup lang="ts">
import { ref } from 'vue';
import { CASE_CATEGORIES, filterCases, type CaseItem } from '../domain/site';

const category = ref<string>('全部');
const items = ref<CaseItem[]>([
  { id: 1, category: '宠物医院', title: '仁心宠物医院：把随访提醒做进日常', summary: '疫苗与复诊排期模板落地，随访完成率提升。', published: true },
  { id: 2, category: '宠物店', title: '毛球生活馆：内容导购替代硬广', summary: '评测+清单结构上线，读者信任度提高。', published: true },
  { id: 3, category: '救助机构', title: '爱心领养站：信息公开但不做交易', summary: '领养卡无报名/交易按钮，售卖举报双通道。', published: true },
  { id: 4, category: '品牌方', title: '某粮品牌：来源声明与利益披露实践', summary: '（待审案例——官网不展示）', published: false },
]);
const shown = () => filterCases(items.value, category.value === '全部' ? '全部' : category.value);
</script>

<template>
  <div class="cases">
    <h1>客户案例</h1>
    <nav class="cats">
      <button type="button" :class="{ on: category === '全部' }" @click="category = '全部'">全部</button>
      <button
        v-for="c in CASE_CATEGORIES"
        :key="c"
        type="button"
        :class="{ on: category === c }"
        @click="category = c"
      >
        {{ c }}
      </button>
    </nav>

    <section class="grid">
      <article v-for="c in shown()" :key="c.id" class="card" :data-testid="`case-${c.id}`">
        <span class="tag">{{ c.category }}</span>
        <h2>{{ c.title }}</h2>
        <p>{{ c.summary }}</p>
      </article>
    </section>
    <p v-if="shown().length === 0" class="empty">该分类暂无已发布案例。</p>
  </div>
</template>

<style scoped>
.cases { max-width: 1120px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 18px; }
h1 { font-size: 32px; margin: 0; }
.cats { display: flex; gap: 8px; flex-wrap: wrap; }
.cats button { height: 34px; padding: 0 16px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 13px; cursor: pointer; }
.cats button.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; }
.card { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 24px; display: grid; gap: 10px; }
.tag { justify-self: start; background: #eaf1ff; color: #4d8dff; border-radius: 999px; padding: 3px 12px; font-size: 12px; }
.card h2 { margin: 0; font-size: 17px; line-height: 1.5; }
.card p { margin: 0; color: #7a6e63; font-size: 14px; line-height: 1.7; }
.empty { color: #7a6e63; text-align: center; }
</style>

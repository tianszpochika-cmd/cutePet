<script setup lang="ts">
import { ref } from 'vue';
import { parseProductCsv, csvImportSummary, type CsvParseResult } from '../domain/operations';

const products = ref([
  { id: 1, name: '幼猫无谷粮', category: '主粮', state: 'ON_SHELF' },
  { id: 2, name: '皮肤护理喷雾', category: '医疗保健', state: 'ON_SHELF' },
  { id: 3, name: '旧款猫砂', category: '清洁护理', state: 'OFF_SHELF' },
]);
const csvText = ref('name,category,sourceType\n逗猫球,玩具,EDITORIAL\n肾脏处方粮,主粮,EDITORIAL\n,主粮,CSV_IMPORT');
const preview = ref<CsvParseResult | null>(null);
const message = ref('');

function runPreview() {
  preview.value = parseProductCsv(csvText.value);
}

function doImport() {
  if (!preview.value) runPreview();
  if (!preview.value || preview.value.accepted.length === 0) {
    message.value = '无合法行可导入（三通道同规则——禁品类/来源/字段，U77）';
    return;
  }
  const r = preview.value;
  for (const row of r.accepted) {
    products.value.push({ id: products.value.length + 1, name: row.name, category: row.category, state: 'ON_SHELF' });
  }
  message.value = csvImportSummary(r);
}

function setState(id: number, governance: boolean) {
  const p = products.value.find((x) => x.id === id);
  if (!p) return;
  p.state = governance ? 'CLEARED' : p.state === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF';
  message.value = governance
    ? `#${id} 治理清除：引用处去敏感、不可原样再上（U88）`
    : `#${id} → ${p.state}（下架在历史内容中带已下架标）`;
}
</script>

<template>
  <div class="product-ops">
    <section class="card">
      <h2>商品库</h2>
      <table>
        <thead><tr><th>名称</th><th>类目</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td>{{ p.name }}</td>
            <td>{{ p.category }}</td>
            <td><span :class="['state', p.state.toLowerCase()]">{{ p.state }}</span></td>
            <td class="ops">
              <button type="button" class="chip" @click="setState(p.id, false)">上/下架</button>
              <button type="button" class="chip danger" @click="setState(p.id, true)">治理清除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p class="meta">创建/编辑走 product.create.edit（禁止品类与来源校验在表单提交时同规则拦截）。</p>
    </section>

    <section class="card">
      <h2>CSV 批量导入（product.import.csv）</h2>
      <textarea v-model="csvText" rows="5" data-testid="csv" />
      <div class="inline">
        <button type="button" class="chip" data-testid="csv-preview" @click="runPreview">校验预览</button>
        <button type="button" class="chip primary" data-testid="csv-import" @click="doImport">确认导入</button>
      </div>
      <ul v-if="preview" class="result" data-testid="csv-result">
        <li>✅ 通过 {{ preview.accepted.length }} 条</li>
        <li v-for="r in preview.rejected" :key="r.line">❌ 第 {{ r.line }} 行：{{ r.reason }}</li>
      </ul>
      <p class="meta">逐行同规则：兽药/处方/来源/字段——不能绕（U77）。</p>
    </section>

    <p v-if="message" class="msg" data-testid="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.product-ops { display: grid; gap: 14px; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 15px; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 8px 10px; font-size: 13px; border-bottom: 1px solid #f0e6dc; }
th { color: #7a6e63; font-size: 12px; }
.state { border-radius: 999px; padding: 2px 8px; font-size: 11px; font-weight: 600; }
.state.on_shelf { background: #e7f8ef; color: #15803d; }
.state.off_shelf { background: #fff1e8; color: #b45309; }
.state.cleared { background: #fdecec; color: #b91c1c; }
.ops { display: flex; gap: 6px; }
.chip { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 5px 12px; font-size: 12px; cursor: pointer; }
.chip.primary { background: #4d8dff; color: #fff; }
.chip.danger { background: #fdecec; color: #b91c1c; }
.inline { display: flex; gap: 8px; }
textarea { border: 1px solid #f0e6dc; border-radius: 12px; padding: 10px; font-family: ui-monospace, monospace; font-size: 13px; }
.result { margin: 0; padding-left: 18px; font-size: 13px; display: grid; gap: 4px; color: #2b2118; }
.meta { margin: 0; color: #7a6e63; font-size: 12px; }
.msg { background: #eaf1ff; color: #2563eb; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin: 0; }
</style>

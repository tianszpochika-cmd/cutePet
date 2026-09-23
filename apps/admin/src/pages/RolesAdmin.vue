<script setup lang="ts">
import { computed, ref } from 'vue';
import { PERMISSIONS, presetRoleMatrix } from '../domain/workbench';

const presets = presetRoleMatrix();
const roleNames = Object.keys(presets);
const selectedPreset = ref(roleNames[0] ?? '');
const search = ref('');
const granted = computed(() => new Set(presets[selectedPreset.value] ?? []));
const roleLabels: Record<string, string> = {
  editor: '内容编辑',
  reviewer: '审核员',
  poiOperator: '探索运营',
  productOperator: '商品运营',
  supervisor: '主管',
  administrator: '管理员',
};
const permissionGroups = [
  { title: '内容与审核', items: PERMISSIONS.slice(0, 10) },
  { title: '用品与导购', items: PERMISSIONS.slice(10, 15) },
  { title: '场所与活动', items: PERMISSIONS.slice(15, 22) },
  { title: '用户与家庭', items: PERMISSIONS.slice(22, 27) },
  { title: '数据与系统', items: PERMISSIONS.slice(27) },
];
function visiblePermissions(items: readonly string[]): string[] {
  const term = search.value.trim().toLowerCase();
  return term ? items.filter((item) => item.toLowerCase().includes(term)) : [...items];
}
function highRisk(permission: string): boolean {
  return ['user.ban', 'rbac.manage', 'system.settings'].includes(permission);
}
</script>

<template>
  <div class="roles">
    <header class="page-head">
      <p class="eyebrow">ACCESS CONTROL · ROLE MATRIX</p>
      <h1>角色与权限</h1>
      <p>查看预置角色的权限定义，评审高危能力的边界。</p>
    </header>

    <div class="preview-note" role="status">
      <span aria-hidden="true">ⓘ</span>
      <div><strong>当前显示制度模板，不是实时授权</strong><p>管理员身份、服务端角色列表和权限变更回执尚未接入。本页不会创建角色、修改任何账号权限或写入审计。</p></div>
    </div>

    <div class="workspace">
      <aside class="role-list" aria-label="预置角色模板">
        <h2>预置角色 <small>{{ roleNames.length }} 类</small></h2>
        <button v-for="name in roleNames" :key="name" type="button" :class="{ active: selectedPreset === name }" :aria-pressed="selectedPreset === name" @click="selectedPreset = name">
          <strong>{{ roleLabels[name] || name }}</strong><small>{{ name }} · {{ presets[name]?.length ?? 0 }} 项</small>
        </button>
        <p>模板来自权限目录。实际账号归属与授权需以服务端查询为准。</p>
      </aside>

      <section class="matrix" aria-labelledby="matrix-title">
        <div class="matrix-head">
          <div><p class="eyebrow">PERMISSION REFERENCE</p><h2 id="matrix-title">{{ roleLabels[selectedPreset] || selectedPreset }} · 权限模板</h2><p>本地定义包含 {{ granted.size }} / {{ PERMISSIONS.length }} 个权限点。</p></div>
          <span class="read-only">只读</span>
        </div>
        <div class="toolbar">
          <label for="perm-search">查找权限点</label>
          <input id="perm-search" v-model="search" type="search" placeholder="输入权限标识，如 user.ban" />
        </div>
        <div class="groups">
          <section v-for="group in permissionGroups" v-show="visiblePermissions(group.items).length" :key="group.title" class="perm-group">
            <h3>{{ group.title }}</h3>
            <ul>
              <li v-for="permission in visiblePermissions(group.items)" :key="permission">
                <code>{{ permission }}</code>
                <span v-if="highRisk(permission)" class="risk">高危</span>
                <span class="grant" :class="{ yes: granted.has(permission) }">{{ granted.has(permission) ? '模板包含' : '模板未包含' }}</span>
              </li>
            </ul>
          </section>
          <p v-if="!permissionGroups.some((group) => visiblePermissions(group.items).length)" class="no-match">没有匹配的权限点。试试更短的关键词。</p>
        </div>
        <p class="matrix-foot">高危封禁、角色权限与系统设置要求二次确认及全量审计。业务域、对象归属和工单授权仍须由服务端逐动作核验。</p>
      </section>
    </div>

    <section class="custom-note">
      <div><p class="eyebrow">CUSTOM ROLE</p><h2>自定义角色配置待接入</h2><p>创建前需读取当前角色与版本、校验 32 点白名单及管理员类约束。保存时再核验操作者权限，二次确认高危授权，并返回实际影响范围与审计编号。</p></div>
      <button type="button" data-testid="save-role" disabled>创建角色待接入</button>
    </section>
  </div>
</template>

<style scoped>
.roles{max-width:1320px;margin:auto;display:grid;gap:17px;color:#2b2118}.page-head{margin:4px 0 0}.eyebrow{font-size:11px;font-weight:800;letter-spacing:.15em;color:#B85111;margin:0 0 6px}.page-head h1{font-size:29px;letter-spacing:-.03em;margin:0}.page-head>p:last-child{font-size:13px;color:#6b5e52;margin:8px 0 0}.preview-note{display:flex;gap:12px;align-items:start;border:1px solid #ecd8b8;background:#fff8eb;color:#654a2c;border-radius:13px;padding:15px 17px;line-height:1.6}.preview-note>span{font-size:20px}.preview-note strong{font-size:13px}.preview-note p{font-size:12px;margin:3px 0 0}.workspace{display:grid;grid-template-columns:245px minmax(0,1fr);gap:16px;align-items:start}.role-list,.matrix,.custom-note{background:#fff;border:1px solid #e9ded2;border-radius:14px;box-shadow:0 8px 24px #2b211808}.role-list{padding:16px;display:grid;gap:6px}.role-list h2{display:flex;justify-content:space-between;align-items:baseline;font-size:15px;margin:0 0 9px}.role-list h2 small{color:#85786d;font-size:11px}.role-list button{width:100%;border:1px solid transparent;border-radius:9px;background:#fff;text-align:left;padding:11px 12px;cursor:pointer;color:#4b4037;font:inherit}.role-list button.active{border-color:#efceb2;background:#fff4e9;box-shadow:inset 3px 0 #B85111}.role-list button strong{display:block;font-size:13px}.role-list button small{display:block;color:#85776a;margin-top:3px;font-size:11px}.role-list>p{font-size:11px;color:#817367;line-height:1.6;margin:12px 2px 0}.matrix{overflow:hidden}.matrix-head{display:flex;justify-content:space-between;gap:12px;align-items:start;padding:18px;border-bottom:1px solid #eee5dc}.matrix-head h2{font-size:18px;margin:0}.matrix-head p:last-child{font-size:12px;color:#75695f;margin:5px 0 0}.read-only{font-size:11px;color:#5b4c82;background:#f3effa;border-radius:999px;padding:5px 10px}.toolbar{display:flex;align-items:center;gap:12px;padding:14px 18px}.toolbar label{font-size:12px;font-weight:700;color:#62564b}.toolbar input{min-height:40px;flex:1;min-width:0;border:1px solid #d9cabb;border-radius:9px;padding:0 11px;font:inherit}.groups{padding:0 18px 18px;display:grid;gap:10px}.perm-group{border:1px solid #e9ded2;border-radius:9px;overflow:hidden}.perm-group h3{font-size:12px;color:#6c5748;background:#fff9f3;margin:0;padding:10px 12px}.perm-group ul{list-style:none;padding:0;margin:0}.perm-group li{display:flex;align-items:center;gap:8px;min-height:37px;padding:6px 12px;border-top:1px solid #f2eae2}.perm-group code{font-size:12px;color:#4f4035;overflow-wrap:anywhere}.risk{font-size:10px;color:#9b342e;background:#fff0ee;border-radius:999px;padding:2px 7px}.grant{margin-left:auto;font-size:11px;color:#8b7c6e;white-space:nowrap}.grant.yes{font-weight:700;color:#236646}.no-match{font-size:12px;color:#776b60;padding:20px}.matrix-foot{border-top:1px solid #eee5dc;margin:0;padding:14px 18px;color:#75675b;font-size:11px;line-height:1.7}.custom-note{display:flex;justify-content:space-between;align-items:center;gap:22px;padding:20px}.custom-note h2{font-size:16px;margin:0}.custom-note p:last-child{font-size:12px;color:#6b5f54;line-height:1.7;margin:6px 0 0;max-width:770px}.custom-note button{min-height:40px;border:0;border-radius:9px;background:#e9e3dd;color:#73675d;padding:0 16px;white-space:nowrap;font:inherit;cursor:not-allowed}@media(max-width:980px){.workspace{grid-template-columns:1fr}.role-list{display:flex;flex-wrap:wrap;align-items:center}.role-list h2,.role-list>p{width:100%}.role-list button{width:auto}.custom-note{display:block}.custom-note button{margin-top:14px}}
</style>

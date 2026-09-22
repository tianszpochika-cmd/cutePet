<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { PERMISSIONS, presetRoleMatrix, roleSaveBlockers } from '../domain/workbench';

const presets = presetRoleMatrix();
const selectedPreset = ref('reviewer');
const mode = ref<'preset' | 'custom'>('custom');

const custom = reactive({ name: '', permissions: new Set<string>() as Set<string>, builtin: false });
const message = ref('');

function toggle(p: string) {
  if (custom.permissions.has(p)) custom.permissions.delete(p);
  else custom.permissions.add(p);
}

function save() {
  const blockers = roleSaveBlockers({
    roleName: custom.name,
    permissions: [...custom.permissions],
    builtin: custom.builtin,
  });
  if (blockers.length > 0) {
    message.value = blockers.join(' / ');
    return;
  }
  message.value = `角色「${custom.name}」已保存（${custom.permissions.size} 个权限点，rbac.manage 已审计留痕）`;
}
</script>

<template>
  <div class="roles">
    <nav class="modes">
      <button type="button" :class="{ on: mode === 'preset' }" @click="mode = 'preset'">预置 6 角色（只读）</button>
      <button type="button" :class="{ on: mode === 'custom' }" @click="mode = 'custom'">自定义角色</button>
    </nav>

    <section v-if="mode === 'preset'" class="card">
      <div class="preset-tabs">
        <button
          v-for="(_, name) in presets"
          :key="name"
          type="button"
          :class="{ on: selectedPreset === name }"
          @click="selectedPreset = name"
        >
          {{ name }}
        </button>
      </div>
      <p class="meta">共 {{ presets[selectedPreset]?.length ?? 0 }} / 32 权限点 · 内置角色不可修改（administrator 永不可编辑）</p>
      <div class="perms">
        <span
          v-for="p in PERMISSIONS"
          :key="p"
          :class="{ has: presets[selectedPreset]?.includes(p), high: p === 'rbac.manage' || p === 'system.settings' }"
        >
          {{ p }}
        </span>
      </div>
    </section>

    <section v-else class="card">
      <label>角色名（3–32 位字母数字-_）<input v-model="custom.name" data-testid="role-name" placeholder="如 ops-cn" /></label>
      <div class="perms editable">
        <button
          v-for="p in PERMISSIONS"
          :key="p"
          type="button"
          :class="{ on: custom.permissions.has(p) }"
          :data-testid="`perm-${p}`"
          @click="toggle(p)"
        >
          {{ p }}
        </button>
      </div>
      <p class="meta">已选 {{ custom.permissions.size }} / 32 · 高危两点（rbac.manage/system.settings）建议仅授予管理员类角色</p>
      <button type="button" class="chip primary" data-testid="save-role" @click="save">保存角色</button>
    </section>

    <p v-if="message" class="msg" data-testid="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.roles { display: grid; gap: 14px; }
.modes { display: flex; gap: 6px; }
.modes button { height: 34px; padding: 0 16px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 13px; }
.modes button.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 12px; }
.preset-tabs { display: flex; gap: 6px; flex-wrap: wrap; }
.preset-tabs button { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 5px 12px; font-size: 12px; cursor: pointer; }
.preset-tabs button.on { background: #4d8dff; color: #fff; font-weight: 600; }
.perms { display: flex; gap: 6px; flex-wrap: wrap; }
.perms span { background: #f7f1ea; color: #c9beb2; border-radius: 999px; padding: 4px 10px; font-size: 11px; }
.perms span.has { background: #e7f8ef; color: #15803d; font-weight: 600; }
.perms span.has.high { background: #fdecec; color: #b91c1c; }
.perms.editable button { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 5px 10px; font-size: 11px; cursor: pointer; }
.perms.editable button.on { background: #ff7a2f; color: #fff; font-weight: 600; }
label { display: grid; gap: 6px; font-size: 13px; font-weight: 600; }
label input { height: 40px; border: 1px solid #f0e6dc; border-radius: 10px; padding: 0 12px; font-weight: 400; }
.chip { height: 40px; border: none; border-radius: 999px; padding: 0 20px; font-size: 14px; cursor: pointer; justify-self: start; }
.chip.primary { background: #ff7a2f; color: #fff; font-weight: 600; }
.meta { margin: 0; color: #7a6e63; font-size: 13px; }
.msg { background: #e7f8ef; color: #15803d; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin: 0; }
</style>

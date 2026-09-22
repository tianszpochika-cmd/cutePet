<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { familyPageModel, type FamilyMemberView } from '../../domain/family';

const router = useRouter();
const members = ref<FamilyMemberView[]>([]);
const model = ref(familyPageModel([]));
const error = ref('');

onMounted(async () => {
  try {
    const res = (await api.familyMine()) as unknown as {
      familyId: number;
      name: string;
      role: string;
      members: number;
    };
    // 成员明细接口随本地阶段补齐；此处按响应组装展示骨架
    members.value = [];
    model.value = familyPageModel(members.value);
    void res;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '你还没有加入家庭（dev 需启动 iam-service）';
  }
});
</script>

<template>
  <div class="family">
    <header>
      <h1>我的家庭</h1>
      <button type="button" class="primary" @click="router.push('/family/invite')">邀请家人</button>
    </header>

    <p v-if="error" class="err" data-testid="family-error">{{ error }}</p>
    <template v-else>
      <p class="note">{{ model.note }}</p>

      <section>
        <h2>管理员</h2>
        <p v-if="model.admins.length === 0" class="muted">（成员接口接入后展示管理员列表）</p>
      </section>

      <section>
        <h2>宠物所有者</h2>
        <p class="muted">每只共享宠物展示其所有者；共享档位（可管理/只读）在宠物详情-共享管理中按所有者设置。</p>
      </section>
    </template>

    <div class="actions">
      <button type="button" class="ghost" @click="router.push('/family/join')">输入邀请码加入</button>
      <button
        type="button"
        class="ghost"
        @click="alert('所有权转移：同家庭接收者 → 影响清单 → 核验 → 发送；24h 待接受可撤销（X04）')"
      >
        转移宠物所有权（X04）
      </button>
      <button
        type="button"
        class="ghost"
        @click="alert('管理员转交：与宠物转移分离；待接受时原管理员仍有效（X05）')"
      >
        转交家庭管理员（X05）
      </button>
    </div>
  </div>
</template>

<style scoped>
.family { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 14px; }
header { display: flex; justify-content: space-between; align-items: center; }
.primary { height: 36px; padding: 0 16px; border: none; border-radius: 999px; background: #9b8cff; color: #fff; font-weight: 600; }
.note { background: #f5f3ff; color: #6d5bd0; border-radius: 12px; padding: 12px 14px; font-size: 13px; line-height: 1.7; }
section h2 { font-size: 15px; color: #7a6e63; }
.actions { display: grid; gap: 10px; }
.ghost { height: 44px; border: none; border-radius: 12px; background: #fff; color: #2b2118; box-shadow: inset 0 0 0 1px #f0e6dc; text-align: left; padding: 0 16px; }
.muted { color: #7a6e63; font-size: 13px; }
.err { color: #ef4444; font-size: 13px; }
</style>

<script setup lang="ts">
import { ageBand, beijingToday, getAuthFlow } from '../../domain/auth';

const flow = getAuthFlow();
const isMinor = (() => {
  if (!flow?.birthDate) return false;
  try {
    return ageBand(flow.birthDate, beijingToday()) === 'MINOR';
  } catch {
    return false;
  }
})();
</script>

<template>
  <div class="guardian-page">
    <router-link class="back" to="/login/age">← 返回年龄步骤</router-link>
    <div class="card">
      <p class="eyebrow">03 / 监护人流程</p>
      <h1>需要监护人独立核验。</h1>
      <p class="lead">未满 14 周岁用户不能通过成人手机号步骤直接注册。监护人应在了解儿童个人信息处理规则后，使用独立的身份与手机号核验流程作出同意。</p>

      <div class="notice" role="status">
        <strong>监护人核验暂未接入</strong>
        <p v-if="flow && !isMinor">当前年龄分支不适用此页面，请返回年龄步骤核对出生日期。</p>
        <p v-else-if="flow?.mode === 'preview'">当前只展示适用的流程，不会创建儿童账号，也不会记录监护人同意。</p>
        <p v-else-if="flow?.mode === 'live'">当前接口无法完成新账号创建、独立监护核验与同意的可靠闭环。儿童资料尚未提交；此处不会显示虚假的“已同意”或进入登录成功页。</p>
        <p v-else>当前步骤已过期，请从登录入口重新开始。</p>
      </div>

      <ol class="steps">
        <li><span>1</span><div><strong>阅读正式规则</strong><p>儿童个人信息处理规则需提供完整版本、生效日期和可查阅路径。</p></div></li>
        <li><span>2</span><div><strong>由监护人独立验证</strong><p>证明操作者确为监护人，不能只输入一个手机号就视为验证成功。</p></div></li>
        <li><span>3</span><div><strong>服务端确认后再继续</strong><p>同意状态由平台核验；失败或撤回时不得自动放行儿童账号。</p></div></li>
      </ol>

      <div class="actions">
        <router-link class="primary" to="/login">返回登录入口</router-link>
        <router-link class="secondary" to="/help">查看帮助</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.guardian-page { width: min(100% - 32px, 650px); margin: 42px auto 90px; }
.back { display: inline-flex; align-items: center; min-height: 44px; margin-bottom: 15px; color: #9b470e; font-weight: 700; text-decoration: none; }
.card { padding: clamp(25px, 5vw, 46px); border: 1px solid #eadfd4; border-radius: 24px; background: #fff; }
.eyebrow { margin: 0 0 10px; color: #a0440b; font-size: 12px; font-weight: 800; letter-spacing: .12em; }
h1 { margin: 0; font-size: clamp(27px, 4vw, 36px); line-height: 1.3; letter-spacing: -.04em; }
.lead { margin: 12px 0 24px; color: #64574d; line-height: 1.85; }
.notice { padding: 18px 20px; border-left: 4px solid #b85111; border-radius: 12px; background: #fff3e9; }
.notice strong { color: #8e3b0c; }
.notice p { margin: 6px 0 0; color: #704b37; font-size: 14px; line-height: 1.8; }
.steps { display: grid; gap: 16px; margin: 26px 0; padding: 0; list-style: none; }
.steps li { display: flex; align-items: flex-start; gap: 13px; }
.steps li > span { display: grid; place-items: center; width: 28px; height: 28px; flex: none; border-radius: 9px; background: #fff0e3; color: #9b470e; font-size: 12px; font-weight: 800; }
.steps strong { font-size: 15px; }
.steps p { margin: 3px 0 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.actions { display: flex; flex-wrap: wrap; gap: 11px; }
.primary, .secondary { display: inline-flex; align-items: center; justify-content: center; min-height: 48px; padding: 0 22px; border: 1px solid #b85111; border-radius: 999px; font-weight: 750; text-decoration: none; }
.primary { background: #b85111; color: #fff; }
.secondary { background: #fff; color: #b85111; }
</style>

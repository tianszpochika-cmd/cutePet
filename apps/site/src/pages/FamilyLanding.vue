<script setup lang="ts">
const MATRIX = [
  { action: '查看共享宠物', owner: '✅', manage: '✅', readonly: '✅', note: '未共享则对家人不可见（属于家庭≠访问个人未共享宠物）' },
  { action: '编辑健康记录', owner: '✅', manage: '✅', readonly: '❌', note: '只读不出现编辑入口' },
  { action: '处理提醒', owner: '✅', manage: '✅', readonly: '❌', note: '完成后显示处理人' },
  { action: '调整共享档位', owner: '✅', manage: '❌', readonly: '❌', note: '非所有者不能提升（U61）' },
  { action: '生成健康摘要', owner: '✅', manage: '❌', readonly: '❌', note: '摘要仅所有者（#35），分享不生成实时私有链接' },
  { action: '转移所有权', owner: '✅', manage: '❌', readonly: '❌', note: '24h 待接受可撤销，接收后共享重置只读' },
];

const FLOW = ['邀请码（7 天有效）', '加入时明确共享范围', '自有宠物仍私有', '新成员默认只读', '变更档位通知受影响成员'];
</script>

<template>
  <div class="family-landing">
    <section class="hero">
      <p class="eyebrow">家庭共享</p>
      <h1>一家人的照护，<br />在同一份时间线上</h1>
      <p class="sub">共享档位逐动作生效——可管理不等于全功能，所有者保留关键操作。</p>
      <router-link class="primary" to="/download">体验家庭共享</router-link>
    </section>

    <!-- 拓扑示意 -->
    <section class="topo">
      <h2>共享关系一览</h2>
      <svg viewBox="0 0 640 240" role="img" aria-label="家庭共享拓扑示意">
        <g stroke="#9b8cff" stroke-width="2" fill="none">
          <path d="M320,60 L160,170" />
          <path d="M320,60 L320,170" />
          <path d="M320,60 L480,170" />
        </g>
        <g fill="#9b8cff">
          <circle cx="320" cy="60" r="26" />
        </g>
        <g fill="#fff1e8" stroke="#ff7a2f" stroke-width="2">
          <circle cx="160" cy="170" r="22" />
          <circle cx="320" cy="170" r="22" />
          <circle cx="480" cy="170" r="22" />
        </g>
        <g fill="#2b2118" font-size="13" text-anchor="middle">
          <text x="320" y="65" fill="#fff">家庭</text>
          <text x="160" y="175">旺财·可管理</text>
          <text x="320" y="175">豆豆·只读</text>
          <text x="480" y="175">我的·私有</text>
        </g>
      </svg>
      <p class="note">右侧「我的」表示：未共享的个人宠物对家人不可见——家庭关系不越权。</p>
    </section>

    <section class="matrix">
      <h2>逐动作权限</h2>
      <table>
        <thead><tr><th>动作</th><th>所有者</th><th>可管理</th><th>只读</th><th>说明</th></tr></thead>
        <tbody>
          <tr v-for="m in MATRIX" :key="m.action">
            <td>{{ m.action }}</td><td>{{ m.owner }}</td><td>{{ m.manage }}</td><td>{{ m.readonly }}</td>
            <td class="note">{{ m.note }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="flow">
      <h2>加入流程</h2>
      <ol><li v-for="(f, i) in FLOW" :key="i">{{ f }}</li></ol>
      <p class="note">规则：一人同时只能属于一个家庭；管理员转交与宠物转移相互独立（待接受时原管理员有效）。</p>
    </section>

    <router-link class="ghost" to="/services">看完整服务场景 →</router-link>
  </div>
</template>

<style scoped>
.family-landing { max-width: 960px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 40px; }
.hero { text-align: center; background: linear-gradient(180deg, #f5f3ff, transparent); border-radius: 32px; padding: 64px 16px 48px; display: grid; gap: 16px; justify-items: center; }
.eyebrow { color: #9b8cff; font-weight: 700; letter-spacing: .12em; font-size: 13px; margin: 0; }
h1 { font-size: clamp(30px, 5vw, 46px); line-height: 1.25; margin: 0; }
.sub { color: #7a6e63; font-size: 16px; max-width: 520px; margin: 0; line-height: 1.8; }
.primary { background: #9b8cff; color: #fff; border-radius: 999px; padding: 13px 30px; text-decoration: none; font-weight: 600; }
.topo, .matrix, .flow { background: #fff; border-radius: 24px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 28px; display: grid; gap: 16px; }
h2 { margin: 0; font-size: 20px; }
svg { width: 100%; height: auto; }
.note { color: #7a6e63; font-size: 13px; line-height: 1.7; margin: 0; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 10px 12px; font-size: 14px; border-bottom: 1px solid #f0e6dc; }
th { color: #7a6e63; font-size: 12px; }
td { text-align: center; }
td:first-child, td:last-child { text-align: left; }
ol { margin: 0; padding-left: 20px; display: grid; gap: 10px; font-size: 15px; color: #2b2118; }
.ghost { justify-self: start; background: #fff; color: #7a6e63; border-radius: 999px; padding: 13px 28px; text-decoration: none; box-shadow: inset 0 0 0 1px #f0e6dc; }
</style>

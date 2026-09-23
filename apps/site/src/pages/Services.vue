<script setup lang="ts">
import { computed, ref } from 'vue';

const SCENES = [
  {
    id: 'beginner', number: '01', label: '新手养宠', subtitle: '从建档到第一次安排', mark: '✦',
    title: '刚把它接回家，先把重要的事记清。',
    lead: '不用一次记住所有细节。先建立档案，再把已有健康信息和下一次计划放到同一条照护路径里；有疑问时按主题寻找参考内容。',
    problems: ['健康信息散在照片与聊天里', '不确定上一次何时处理、下一次何时安排', '想看科普，但不把它当成诊疗建议'],
    steps: [
      { title: '建立宠物档案', copy: '填写物种、昵称与基础情况，照片可以稍后补。' },
      { title: '补上健康记录', copy: '按实际发生时间记录疫苗、驱虫、体检或就医事件。' },
      { title: '安排下次提醒', copy: '核对日期和周期，分清计划与本次待办。' },
      { title: '按主题查资料', copy: '从公开资讯中找到相关参考，涉医内容遵循免责声明。' },
    ],
    abilities: [{ label: '宠物管理', to: '/products/manage' }, { label: '宠物资讯', to: '/products/news' }],
    note: '建档、记录和设置提醒都在用户端完成；保存或完成须由平台确认。',
    related: '/products/manage', relatedLabel: '了解宠物管理',
  },
  {
    id: 'multi', number: '02', label: '多宠家庭', subtitle: '每只宠物、每位家人都清楚', mark: '♡',
    title: '照顾的不止一只，事情也能各自清楚。',
    lead: '每只宠物保留独立档案与安排。需要家人接力时，先明确共享哪些宠物、谁能处理提醒，避免一件事被重复完成。',
    problems: ['多只宠物的记录容易串在一起', '家人可能同时处理同一个待办', '希望协作，也要保留未共享档案的私密性'],
    steps: [
      { title: '分别建立档案', copy: '每只宠物有自己的健康记录、时间线与提醒计划。' },
      { title: '查看各自待办', copy: '切换宠物，先确认这件事属于谁。' },
      { title: '按范围邀请家人', copy: '只共享需要协作的宠物，设置只读或可管理权限。' },
      { title: '交接后核对进度', copy: '查看处理状态与处理人；若家人已完成，不重复写入。' },
    ],
    abilities: [{ label: '宠物管理', to: '/products/manage' }, { label: '家庭共享', to: '/family' }],
    note: '加入家庭不会自动共享个人宠物；家庭管理员也不会因此获得他人宠物的数据权限。',
    related: '/family', relatedLabel: '了解家庭共享',
  },
  {
    id: 'travel', number: '03', label: '带宠出行', subtitle: '先把地点与活动看清', mark: '⌖',
    title: '出门前，先看看哪里适合一起去。',
    lead: '从城市和场所条件开始，查看详情、路线与活动信息。路线和评价是参考，是否适合带宠仍需结合现场规则判断。',
    problems: ['不知道附近有哪些宠物友好场所', '希望提前核对设施与营业信息', '想了解活动条件与个人信息用途后再决定'],
    steps: [
      { title: '确定城市与条件', copy: '允许定位时查看附近；未授权时手动选择城市。' },
      { title: '核对场所详情', copy: '按类型及友好属性筛选，出发前再核实现场规则。' },
      { title: '参考遛宠路线', copy: '浏览公开路线与路径说明，不把它当成到店保证。' },
      { title: '了解活动再报名', copy: '先读公开信息，进入用户端后阅读告知并确认报名。' },
    ],
    abilities: [{ label: '宠物探索', to: '/products/explore' }, { label: '宠物资讯', to: '/products/news' }],
    note: '场所、路线与活动以当前公开数据为准；地图或定位不可用时应保留列表与手选城市。',
    related: '/products/explore', relatedLabel: '了解宠物探索',
  },
] as const;

const selected = ref<(typeof SCENES)[number]['id']>('beginner');
const active = computed(() => SCENES.find((scene) => scene.id === selected.value) ?? SCENES[0]!);
</script>

<template>
  <div class="services-page">
    <section class="hero"><div class="site-wrap hero-grid"><div><p class="site-eyebrow">MOMENTS THAT MATTER</p><h1>照顾它的方式，<br /><span>从你现在的生活开始。</span></h1><p class="site-lead">新手养宠、多宠家庭、带宠出行，是三种不同的起点。选一个最接近你的场景，看看哪些步骤能帮上忙。</p><a class="site-button" href="#choose-scene">选择场景 <span aria-hidden="true">↓</span></a></div><div class="hero-note"><span>使用路径示意</span><strong>先想清楚一件事，<br />再选择合适的能力。</strong><p>这些情境是产品说明，不是真实用户故事，也没有使用虚构的场所、活动或个人数据。</p></div></div></section>

    <section id="choose-scene" class="site-wrap chooser" aria-labelledby="choose-heading">
      <p class="site-eyebrow">CHOOSE YOUR MOMENT</p><h2 id="choose-heading" class="site-section-title">现在的你，<br />正在面对哪件事？</h2>
      <div class="scene-options" aria-label="使用场景">
        <button v-for="scene in SCENES" :key="scene.id" type="button" :class="{ selected: selected === scene.id }" :aria-pressed="selected === scene.id" @click="selected = scene.id"><span class="scene-mark" aria-hidden="true">{{ scene.mark }}</span><span><strong>{{ scene.label }}</strong><small>{{ scene.subtitle }}</small></span><span class="arrow" aria-hidden="true">↗</span></button>
      </div>

      <article class="scene-detail" :class="active.id" aria-live="polite">
        <div class="scene-head"><div><p class="site-eyebrow">{{ active.number }} / 03 · {{ active.label }}</p><h3>{{ active.title }}</h3><p>{{ active.lead }}</p></div><aside><strong>可能遇到的事</strong><ul><li v-for="problem in active.problems" :key="problem">{{ problem }}</li></ul></aside></div>
        <div class="scene-process"><div class="block-heading"><h4>可以怎样开始</h4><span>四步使用路径 · 在用户端由本人操作</span></div><ol><li v-for="(step, index) in active.steps" :key="step.title"><span>{{ String(index + 1).padStart(2, '0') }}</span><strong>{{ step.title }}</strong><p>{{ step.copy }}</p></li></ol></div>
        <div class="scene-bottom"><div><h4>对应的产品能力</h4><div class="ability-links"><router-link v-for="ability in active.abilities" :key="ability.to" :to="ability.to">{{ ability.label }} <span aria-hidden="true">→</span></router-link></div></div><p>{{ active.note }}</p></div>
      </article>
    </section>

    <section class="entry"><div class="site-wrap entry-grid"><div><p class="site-eyebrow">START WITH A CLEAR PATH</p><h2 class="site-section-title">理解了使用方式，<br />再进入用户端。</h2><p>官网帮助你认识任务路径。建档、记录、家庭邀请和活动报名，都要在用户端按登录、权限与告知规则确认。</p><div class="entry-actions"><router-link class="site-button" :to="active.related">{{ active.relatedLabel }} <span aria-hidden="true">↗</span></router-link><router-link class="site-button outline" to="/download">查看 Web 与手机入口 <span aria-hidden="true">↗</span></router-link></div></div><div class="entry-cards"><div><span aria-hidden="true">▣</span><strong>在电脑上使用</strong><p>适合查看档案、时间线与更多细节。</p><router-link to="/download?end=web">查看 Web 使用方式 →</router-link></div><div><span aria-hidden="true">▯</span><strong>在手机上使用</strong><p>适合随手查看待办、记录与附近信息。</p><router-link to="/download?end=mobile">查看手机使用方式 →</router-link></div></div></div></section>
  </div>
</template>

<style scoped>
.hero{padding:90px 0 110px;background:radial-gradient(circle at 83% 14%,#ffe1c5,transparent 35%),linear-gradient(135deg,#fff9f3,#f8ede1)}.hero-grid{display:grid;grid-template-columns:1fr .72fr;align-items:center;gap:10%}.hero h1{margin:0;font-size:clamp(40px,4.7vw,67px);line-height:1.16;letter-spacing:-.065em}.hero h1 span{color:var(--site-action)}.hero .site-lead{margin-top:23px;font-size:17px}.hero .site-button{margin-top:30px}.hero-note{max-width:390px;padding:34px;border:1px solid #ead4c2;border-radius:26px;background:#fff;box-shadow:0 24px 50px rgba(74,44,24,.06)}.hero-note>span{color:var(--site-action);font-size:12px;font-weight:800;letter-spacing:.1em}.hero-note strong{display:block;margin:28px 0 12px;font-size:28px;line-height:1.35}.hero-note p{margin:0;color:var(--site-muted);font-size:13px}.chooser{padding-top:100px;padding-bottom:110px}.scene-options{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14px;margin:38px 0 20px}.scene-options button{display:flex;align-items:center;gap:15px;min-height:98px;padding:20px;border:1px solid var(--site-line);border-radius:20px;background:#fff;color:var(--site-ink);text-align:left}.scene-options button.selected{border-color:#c86b32;background:#fff1e8;box-shadow:0 8px 25px rgba(77,45,25,.06)}.scene-mark{display:grid;place-items:center;flex:none;width:45px;height:45px;border-radius:14px;background:#fff1e8;color:var(--site-action);font-size:25px}.scene-options strong,.scene-options small{display:block}.scene-options strong{font-size:16px}.scene-options small{margin-top:3px;color:var(--site-muted);font-size:12px}.scene-options .arrow{margin-left:auto;color:var(--site-action)}.scene-detail{padding:38px;border:1px solid var(--site-line);border-radius:27px;background:#fff}.scene-detail.multi{--scene-tint:#f2ecfb}.scene-detail.travel{--scene-tint:#edf8f1}.scene-detail.beginner{--scene-tint:#fff1e8}.scene-head{display:grid;grid-template-columns:1.1fr .7fr;gap:9%;align-items:start}.scene-head h3{margin:0;font-size:clamp(26px,3vw,41px);line-height:1.25;letter-spacing:-.04em}.scene-head>div>p:last-child{max-width:660px;margin:18px 0 0;color:var(--site-muted);font-size:15px}.scene-head aside{padding:23px;border-radius:19px;background:var(--scene-tint)}.scene-head aside strong{font-size:14px}.scene-head aside ul{display:grid;gap:9px;margin:14px 0 0;padding-left:18px;color:var(--site-muted);font-size:13px}.scene-process{margin-top:37px;padding-top:32px;border-top:1px solid var(--site-line)}.block-heading{display:flex;justify-content:space-between;align-items:baseline;gap:15px}.block-heading h4,.scene-bottom h4{margin:0;font-size:18px}.block-heading span{color:var(--site-muted);font-size:12px}.scene-process ol{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;margin:25px 0 0;padding:0;list-style:none}.scene-process li{padding:20px;border-radius:18px;background:#faf7f3}.scene-process li>span{color:var(--site-action);font-size:12px;font-weight:800}.scene-process li strong{display:block;margin-top:21px;font-size:15px}.scene-process li p{margin:8px 0 0;color:var(--site-muted);font-size:13px}.scene-bottom{display:flex;justify-content:space-between;gap:35px;align-items:end;margin-top:34px;padding-top:27px;border-top:1px solid var(--site-line)}.ability-links{display:flex;flex-wrap:wrap;gap:10px;margin-top:14px}.ability-links a{display:inline-flex;align-items:center;min-height:44px;padding:8px 16px;border-radius:999px;background:var(--scene-tint);color:var(--site-action);font-size:13px;font-weight:700;text-decoration:none}.scene-bottom>p{max-width:420px;margin:0;color:var(--site-muted);font-size:13px}.entry{padding:95px 0;background:#f6efe8}.entry-grid{display:grid;grid-template-columns:1fr .9fr;gap:8%;align-items:center}.entry-grid>div>p:not(.site-eyebrow){color:var(--site-muted)}.entry-actions{display:flex;flex-wrap:wrap;gap:10px;margin-top:25px}.entry-cards{display:grid;grid-template-columns:1fr 1fr;gap:14px}.entry-cards>div{display:flex;flex-direction:column;min-height:220px;padding:22px;border:1px solid #ead8c7;border-radius:20px;background:#fff}.entry-cards span{color:var(--site-action);font-size:27px}.entry-cards strong{margin-top:18px;font-size:16px}.entry-cards p{margin:6px 0 16px;color:var(--site-muted);font-size:12px}.entry-cards a{margin-top:auto;color:var(--site-action);font-size:12px;font-weight:750;text-decoration:none}@media(max-width:1000px){.scene-process ol{grid-template-columns:repeat(2,minmax(0,1fr))}.hero-grid{gap:5%}}@media(max-width:760px){.hero{padding:65px 0 75px}.hero-grid,.scene-head,.entry-grid{grid-template-columns:1fr;gap:30px}.hero-note{max-width:none}.chooser{padding-top:72px;padding-bottom:72px}.scene-options{grid-template-columns:1fr}.scene-options button{min-height:78px}.scene-detail{padding:25px}.scene-bottom,.block-heading{align-items:start;flex-direction:column}.entry{padding:72px 0}}@media(max-width:520px){.scene-process ol,.entry-cards{grid-template-columns:1fr}.entry-actions .site-button{width:100%}.scene-process li strong{margin-top:11px}}
</style>

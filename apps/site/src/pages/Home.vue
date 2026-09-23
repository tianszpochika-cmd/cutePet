<script setup lang="ts">
import { computed, ref } from 'vue';
import { FOUR_SECTIONS } from '../domain/site';

const stories = [
  { label: '早上 · 看今天的安排', hint: '提醒和待办先看一眼', title: '今天要做的事', first: '查看本次接种提醒', firstSub: '确认记录，再处理待办', second: '下一次安排', secondSub: '完成后由平台确认生成', note: '下一次安排更清楚' },
  { label: '白天 · 留下照护记录', hint: '健康变化接在时间线上', title: '最近的记录', first: '记录今天的体重', firstSub: '波动较大时再次确认', second: '补充健康事件', secondSub: '从上次记录接着看', note: '上一次照护随时可查' },
  { label: '傍晚 · 家人接力照顾', hint: '先看处理状态，避免重复', title: '家庭协作', first: '查看谁已处理本次待办', firstSub: '服务端结果才算完成', second: '核对自己的共享范围', secondSub: '未共享的宠物保持私有', note: '家人接力，分工更清楚' },
] as const;
const storyIndex = ref(0);
const activeStory = computed(() => stories[storyIndex.value]!);

function selectStory(index: number) {
  storyIndex.value = (index + stories.length) % stories.length;
}
function onStoryKey(event: KeyboardEvent, index: number) {
  if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return;
  event.preventDefault();
  const next = (index + (event.key === 'ArrowRight' ? 1 : -1) + stories.length) % stories.length;
  selectStory(next);
  document.getElementById('story-tab-' + next)?.focus();
}
</script>

<template>
  <div class="home">
    <section class="hero">
      <div class="site-wrap hero-grid">
        <div class="hero-copy">
          <span class="hero-kicker"><span aria-hidden="true">✦</span> 给每一位认真养宠的人</span>
          <h1>让每一天，<br /><span>都更懂你的宠物。</span></h1>
          <p>从健康记录、下次提醒到家庭分工，把散在各处的养宠小事，放进一个清楚的日常里。</p>
          <div class="hero-actions">
            <router-link class="site-button" to="/download?end=web">Web 使用方式 <span aria-hidden="true">↗</span></router-link>
            <router-link class="site-button outline" to="/download?end=mobile">手机使用方式 <span aria-hidden="true">↗</span></router-link>
          </div>
          <div class="hero-foot"><span class="hero-heart" aria-hidden="true">♥</span>为你和家人的每一次用心，留下一份清楚的记录。</div>
        </div>
        <div class="hero-visual">
          <div class="hero-blob" aria-hidden="true"></div>
          <img class="hero-pets" src="/hero-pets.png" alt="一只狗和一只猫并肩的暖色示意插画" />
          <div class="float-card top"><small>今天要记得</small><strong><span aria-hidden="true">✦</span> 下次接种提醒</strong></div>
          <div class="float-card bottom"><small>共同照顾</small><strong><span class="status-dot" aria-hidden="true"></span> 家人已同步安排</strong></div>
          <span class="visual-note">插画与界面为产品示意</span>
        </div>
      </div>
    </section>

    <section class="site-wrap values" aria-label="照护价值">
      <div><span class="value-number">01</span><strong>上次照护有记录</strong><p>疫苗、体重、就医与用药，回看时有迹可循。</p></div>
      <div><span class="value-number">02</span><strong>下一件事有安排</strong><p>计划与本次待办分开看，减少遗漏和重复。</p></div>
      <div><span class="value-number">03</span><strong>一家人有同步</strong><p>按宠物共享，清楚知道谁处理、谁可查看。</p></div>
    </section>

    <section class="site-section features" id="features">
      <div class="site-wrap">
        <div class="section-heading"><div><p class="site-eyebrow">WHAT WE CARE FOR</p><h2 class="site-section-title">照顾它的每一面，<br />都能从这里开始。</h2><p class="site-lead">四类能力围绕同一个日常展开：先把照护做清楚，再去阅读、出门和挑选用品。</p></div><router-link class="site-text-link" to="/products">查看全部能力 <span aria-hidden="true">→</span></router-link></div>
        <div class="feature-grid">
          <article v-for="(feature, index) in FOUR_SECTIONS" :key="feature.id" class="feature-card" :class="feature.id">
            <div class="feature-head"><span class="feature-icon" aria-hidden="true">{{ ['✦', '▤', '⌖', '◇'][index] }}</span><span>0{{ index + 1 }} / 04</span></div>
            <h3>{{ feature.label }}</h3><p>{{ feature.desc }}</p>
            <router-link :to="feature.to" :aria-label="'了解' + feature.label">认识这项能力 <span aria-hidden="true">→</span></router-link>
          </article>
        </div>
      </div>
    </section>

    <section class="site-section journey" id="journey">
      <div class="site-wrap journey-grid">
        <div class="journey-copy"><p class="site-eyebrow">A DAY TOGETHER</p><h2 class="site-section-title">一天的照护，<br />连成安心的日常。</h2><p class="site-lead">这是一条操作情境示意。真正的记录与待办须由用户端和平台确认。</p>
          <div class="story-tabs" role="tablist" aria-label="一天的照护场景">
            <button v-for="(story, index) in stories" :id="'story-tab-' + index" :key="story.label" type="button" role="tab" :aria-selected="storyIndex === index" aria-controls="story-panel" :tabindex="storyIndex === index ? 0 : -1" @click="selectStory(index)" @keydown="onStoryKey($event, index)">
              <span class="story-number">0{{ index + 1 }}</span><span><strong>{{ story.label }}</strong><small>{{ story.hint }}</small></span>
            </button>
          </div>
          <router-link class="site-text-link" to="/services">查看更多使用场景 <span aria-hidden="true">→</span></router-link>
        </div>
        <div id="story-panel" class="journey-stage" role="tabpanel" :aria-labelledby="'story-tab-' + storyIndex">
          <span class="journey-count">0{{ storyIndex + 1 }} / 03</span>
          <button class="story-next" type="button" aria-label="下一个照护场景" @click="selectStory(storyIndex + 1)">→</button>
          <div class="phone" aria-label="移动端界面示意">
            <div class="phone-speaker" aria-hidden="true"></div>
            <div class="phone-content"><div class="phone-top"><strong>cutePet<span>.</span></strong><span class="phone-avatar" aria-hidden="true">☺</span></div>
              <p class="phone-greeting">今天，也一起照顾好它。</p><p class="phone-sub">照护示意 · 非真实宠物资料</p>
              <div class="phone-pet"><span aria-hidden="true">🐾</span><strong>示例宠物<small>今天的安排</small></strong></div>
              <h3>{{ activeStory.title }}</h3>
              <div class="phone-row"><span class="row-icon" aria-hidden="true">◷</span><span><strong>{{ activeStory.first }}</strong><small>{{ activeStory.firstSub }}</small></span></div>
              <div class="phone-row"><span class="row-icon" aria-hidden="true">♡</span><span><strong>{{ activeStory.second }}</strong><small>{{ activeStory.secondSub }}</small></span></div>
            </div>
            <div class="phone-tabs" aria-hidden="true"><span>首页</span><span>资讯</span><span>探索</span><span>我的</span></div>
          </div>
          <div class="story-note"><strong>{{ activeStory.note }}</strong><small>交互及资料均为设计示意</small></div>
        </div>
      </div>
    </section>

    <section class="site-section family" id="family"><div class="site-wrap family-panel"><div><p class="site-eyebrow">CARE TOGETHER</p><h2 class="site-section-title">养宠是家人的事，<br />分工也可以很轻松。</h2><p class="site-lead">邀请家人一起照顾，按每只宠物设置可管理或只读范围。加入家庭并不自动开放私有档案。</p><router-link class="site-text-link" to="/family">了解家庭共享规则 <span aria-hidden="true">→</span></router-link></div><div class="family-diagram" role="img" aria-label="家庭协作示意：宠物所有者与家人按授权查看同一只宠物，未共享宠物保持私有"><div class="orbit"></div><div class="pet-bubble" aria-hidden="true">♥</div><div class="person owner"><span aria-hidden="true">☺</span><strong>我<small>宠物所有者</small></strong></div><div class="person member"><span aria-hidden="true">♡</span><strong>家人<small>按授权查看</small></strong></div><p>未共享的宠物仍保持私有</p></div></div></section>

    <section class="site-section more" id="more"><div class="site-wrap"><p class="site-eyebrow">MORE TO EXPLORE</p><h2 class="site-section-title">认真照顾，也尽情一起生活。</h2><p class="site-lead">照护之外，还有值得读的内容、值得去的地方，以及更有依据的用品参考。</p><div class="more-grid"><article><span class="more-icon pink" aria-hidden="true">▤</span><h3>养宠知识</h3><p>按主题阅读公开内容；审核中或已下架的内容不会作为推荐展示。</p><router-link class="site-text-link" to="/products/news">认识宠物资讯 →</router-link></article><article><span class="more-icon green" aria-hidden="true">⌖</span><h3>和它出门</h3><p>探索友好场所、路线与活动；报名仍需进入用户端确认。</p><router-link class="site-text-link" to="/products/explore">认识宠物探索 →</router-link></article><article><span class="more-icon blue" aria-hidden="true">◇</span><h3>选用品更有数</h3><p>查看资料、评测与清单。第一版没有站内订单与支付。</p><router-link class="site-text-link" to="/products/goods">认识宠物用品 →</router-link></article></div></div></section>

    <section class="site-section faq" id="faq"><div class="site-wrap faq-grid"><div><p class="site-eyebrow">GOOD TO KNOW</p><h2 class="site-section-title">你可能还想知道</h2><p class="site-lead">把重要的使用边界说清楚，开始时更安心。</p></div><div class="faq-items"><details><summary>家人能一起管理宠物档案吗？</summary><p>可以。宠物所有者逐只授权；可管理和只读范围不同，未共享的宠物保持私有。健康摘要仅由所有者处理。</p></details><details><summary>官网能直接记录或报名活动吗？</summary><p>官网负责介绍与公开阅读。记录和报名进入用户端，并由你核对后确认。</p></details><details><summary>能在这里购买宠物用品吗？</summary><p>第一版用品模块提供资料、评测与清单作为参考，不提供订单、支付或站内交易。</p></details></div></div></section>

    <section class="site-section start" id="start"><div class="site-wrap start-grid"><div><p class="site-eyebrow">START EVERYDAY CARE</p><h2 class="site-section-title">从第一份宠物档案，<br />开始更有条理的照顾。</h2><p class="site-lead">选择适合自己的使用方式。仅在入口地址经验证并公开配置后，页面才会显示实际跳转。</p></div><div class="entry-grid"><router-link class="entry-card" to="/download?end=web"><span class="entry-symbol" aria-hidden="true">▣</span><h3>在电脑上使用</h3><p>使用 Web 查看档案、记录与照护安排。</p><strong>查看 Web 入口 <span aria-hidden="true">↗</span></strong></router-link><router-link class="entry-card" to="/download?end=mobile"><span class="entry-symbol" aria-hidden="true">▯</span><h3>在手机上使用</h3><p>移动 H5 适合出门时查看与记录。</p><strong>查看手机入口 <span aria-hidden="true">↗</span></strong></router-link></div></div></section>
  </div>
</template>

<style scoped>
.site-section{padding:100px 0}.hero{position:relative;overflow:hidden;padding:58px 0 90px;background:radial-gradient(circle at 84% 38%,#ffead8 0,transparent 29%),linear-gradient(145deg,#fff9f3 47%,#fff4e9)}
.hero::before{content:"";position:absolute;width:650px;height:650px;right:-160px;top:-220px;border:1px solid #f5dec9;border-radius:50%;pointer-events:none}
.hero-grid{position:relative;display:grid;grid-template-columns:1fr 1.02fr;align-items:center;gap:50px;min-height:510px}.hero-copy{padding-bottom:20px}
.hero-kicker{display:inline-flex;align-items:center;gap:8px;padding:8px 13px;border:1px solid #f4dfcd;border-radius:999px;background:#fff;color:#925029;font-size:12px;font-weight:800}
.hero-kicker span{color:var(--site-orange);font-size:16px}
.hero h1{margin:25px 0 20px;font-size:clamp(45px,4.4vw,66px);line-height:1.17;font-weight:850;letter-spacing:-.075em}.hero h1 span{color:var(--site-action)}
.hero-copy>p{max-width:510px;margin:0;color:var(--site-muted);font-size:18px;line-height:1.9}.hero-actions{display:flex;gap:13px;flex-wrap:wrap;margin:31px 0}.hero-foot{display:flex;align-items:center;gap:12px;color:var(--site-muted);font-size:13px}.hero-heart{display:grid;place-items:center;flex:none;width:28px;height:28px;border-radius:50%;background:#fff0e3;color:var(--site-action)}
.hero-visual{position:relative;display:grid;place-items:center;min-height:520px}.hero-blob{position:absolute;inset:55px 22px 20px;border-radius:48% 52% 46% 54%;background:linear-gradient(140deg,#ffd9b9,#fff0df 55%,#e9ddc4)}
.hero-pets{position:relative;z-index:1;width:100%;max-height:510px;object-fit:contain;filter:drop-shadow(0 25px 20px rgba(121,76,47,.15))}
.float-card{position:absolute;z-index:2;padding:13px 16px;border:1px solid #f4e7da;border-radius:18px;background:rgba(255,255,255,.95);box-shadow:0 16px 40px rgba(69,41,19,.09)}.float-card.top{top:60px;left:0;transform:rotate(-5deg)}.float-card.bottom{right:0;bottom:64px;transform:rotate(5deg)}.float-card small{display:block;margin-bottom:4px;color:var(--site-muted);font-size:11px}.float-card strong{display:flex;align-items:center;gap:7px;font-size:13px}.float-card strong span:first-child{color:var(--site-action)}.status-dot{width:8px;height:8px;border-radius:50%;background:#27865a}.visual-note{position:absolute;right:20px;bottom:2px;color:var(--site-muted);font-size:11px}
.values{position:relative;display:grid;grid-template-columns:repeat(3,1fr);margin-top:-42px;padding:20px 24px;border:1px solid var(--site-line);border-radius:21px;background:#fff;box-shadow:0 16px 48px rgba(69,41,19,.07)}
.values>div{padding:6px 28px}.values>div+div{border-left:1px solid var(--site-line)}.value-number{display:block;color:var(--site-action);font-size:12px;font-weight:800}.values strong{display:block;font-size:17px}.values p{margin:4px 0 0;color:var(--site-muted);font-size:13px;line-height:1.6}
.section-heading{display:flex;justify-content:space-between;align-items:end;gap:20px;margin-bottom:36px}.feature-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:15px}
.feature-card{display:flex;flex-direction:column;min-height:280px;padding:24px;border:1px solid var(--site-line);border-radius:23px;background:#fff}.feature-card.manage{background:#fff1e8}.feature-card.news{background:#fff0f3}.feature-card.explore{background:#eaf7ef}.feature-card.goods{background:#edf3ff}
.feature-head{display:flex;justify-content:space-between;align-items:center;color:#6e5a4a;font-size:11px;font-weight:800}.feature-icon{display:grid;place-items:center;width:42px;height:42px;border-radius:14px;background:#fff;color:var(--site-action);font-size:24px}.feature-card h3{margin:31px 0 8px;font-size:22px}.feature-card p{margin:0;color:var(--site-muted);font-size:13px;line-height:1.7}.feature-card a{display:inline-flex;align-items:center;min-height:44px;margin-top:auto;color:var(--site-action);font-size:13px;font-weight:800;text-decoration:none}
.journey{background:#f7f0e9}.journey-grid{display:grid;grid-template-columns:1fr 1fr;gap:75px;align-items:center}.story-tabs{display:grid;gap:9px;margin:30px 0 20px}.story-tabs button{display:flex;align-items:center;gap:17px;width:100%;min-height:72px;padding:12px 17px;border:1px solid var(--site-line);border-radius:17px;background:#fff;color:var(--site-ink);text-align:left}.story-tabs button[aria-selected=true]{border-color:#d98755;background:#fff4ea;box-shadow:0 8px 22px rgba(69,41,19,.04)}.story-number{color:var(--site-action);font-size:15px;font-weight:800}.story-tabs strong,.story-tabs small{display:block}.story-tabs strong{font-size:14px}.story-tabs small{color:var(--site-muted);font-size:12px}
.journey-stage{position:relative;display:grid;place-items:center;min-height:510px;border-radius:30px;background:radial-gradient(circle at 50% 50%,#ffe1c5,#faebdd 58%,#f0dfd0)}.journey-count{position:absolute;top:24px;left:26px;color:#705849;font-size:12px;font-weight:800}.story-next{position:absolute;top:15px;right:20px;display:grid;place-items:center;width:44px;height:44px;border:1px solid #e8c6aa;border-radius:50%;background:#fff;color:var(--site-action);font-size:23px}
.phone{position:relative;display:flex;flex-direction:column;width:245px;height:435px;overflow:hidden;border:7px solid #302a26;border-radius:34px;background:#fff9f3;box-shadow:0 22px 45px rgba(69,41,19,.17);transform:rotate(-4deg)}.phone-speaker{width:60px;height:8px;margin:10px auto 0;border-radius:9px;background:#302a26}.phone-content{flex:1;padding:18px 16px}.phone-top{display:flex;justify-content:space-between;align-items:center}.phone-top strong{font-size:16px}.phone-top strong span{color:var(--site-orange)}.phone-avatar{display:grid;place-items:center;width:28px;height:28px;border-radius:50%;background:#ffe3c8}.phone-greeting{margin:17px 0 0;font-size:15px;font-weight:800}.phone-sub{margin:0;color:var(--site-muted);font-size:10px}.phone-pet{display:flex;align-items:center;gap:9px;margin:17px 0;padding:9px;border-radius:13px;background:#fff}.phone-pet>span{display:grid;place-items:center;width:34px;height:34px;border-radius:10px;background:#ffdfc5;color:var(--site-action)}.phone-pet strong{font-size:11px}.phone-pet small{display:block;color:var(--site-muted);font-size:9px}.phone-content h3{font-size:12px}.phone-row{display:flex;align-items:center;gap:9px;margin:7px 0;padding:9px;border-radius:11px;background:#fff}.row-icon{color:var(--site-action)}.phone-row strong,.phone-row small{display:block}.phone-row strong{font-size:10px}.phone-row small{color:var(--site-muted);font-size:9px}.phone-tabs{display:flex;justify-content:space-around;padding:9px 4px;background:#fff;color:var(--site-muted);font-size:9px}.phone-tabs span:first-child{color:var(--site-action)}.story-note{position:absolute;right:15px;bottom:22px;padding:11px 14px;border-radius:12px;background:#fff;box-shadow:0 10px 24px rgba(69,41,19,.1)}.story-note strong,.story-note small{display:block}.story-note strong{font-size:12px}.story-note small{color:var(--site-muted);font-size:10px}
.family-panel{display:grid;grid-template-columns:1.05fr .95fr;gap:50px;align-items:center;padding:50px 55px;border-radius:32px;background:#f2ecfb}.family-panel .site-text-link{margin-top:18px}.family-diagram{position:relative;min-height:305px}.orbit{position:absolute;inset:12px 35px;border:2px dashed #cdb9e7;border-radius:50%}.pet-bubble{position:absolute;top:105px;left:calc(50% - 43px);display:grid;place-items:center;width:86px;height:86px;border-radius:50%;background:#fff;color:#7453b6;font-size:38px;box-shadow:0 8px 20px rgba(69,41,19,.07)}.person{position:absolute;display:flex;align-items:center;gap:8px;padding:10px 12px;border-radius:14px;background:#fff;box-shadow:0 8px 20px rgba(69,41,19,.07)}.person.owner{top:22px;left:8px}.person.member{right:0;bottom:64px}.person>span{font-size:22px;color:#7453b6}.person strong{font-size:12px}.person small{display:block;color:var(--site-muted);font-size:10px}.family-diagram p{position:absolute;bottom:0;left:0;margin:0;color:#614d73;font-size:12px}
.more{padding-top:5px}.more-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:17px;margin-top:34px}.more-grid article{display:flex;flex-direction:column;min-height:242px;padding:23px;border:1px solid var(--site-line);border-radius:22px;background:#fff}.more-icon{display:grid;place-items:center;width:40px;height:40px;border-radius:12px;font-size:22px}.more-icon.pink{background:#fff0f3;color:#c33f63}.more-icon.green{background:#eaf7ef;color:#27865a}.more-icon.blue{background:#edf3ff;color:#3e72cf}.more-grid h3{margin:16px 0 5px;font-size:19px}.more-grid p{margin:0;color:var(--site-muted);font-size:13px}.more-grid .site-text-link{margin-top:auto;font-size:13px}
.faq{background:#fff}.faq-grid{display:grid;grid-template-columns:.8fr 1.2fr;gap:65px}.faq-items{border-top:1px solid var(--site-line)}.faq-items details{border-bottom:1px solid var(--site-line)}.faq-items summary{display:flex;align-items:center;justify-content:space-between;min-height:68px;cursor:pointer;font-size:16px;font-weight:700;list-style:none}.faq-items summary::after{content:"+";color:var(--site-action);font-size:24px}.faq-items details[open] summary::after{content:"−"}.faq-items p{margin:0 0 23px;color:var(--site-muted);font-size:14px}
.start{background:#fff1e8}.start-grid{display:grid;grid-template-columns:1fr 1fr;gap:52px;align-items:center}.entry-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}.entry-card{display:flex;flex-direction:column;min-height:218px;padding:23px;border:1px solid #eed7c6;border-radius:20px;background:#fff;color:var(--site-ink);text-decoration:none}.entry-card h3{margin:11px 0 5px;font-size:18px}.entry-card p{margin:0;color:var(--site-muted);font-size:12px}.entry-card strong{margin-top:auto;color:var(--site-action);font-size:12px}.entry-symbol{color:var(--site-action);font-size:26px}
@media(max-width:1000px){.feature-grid{grid-template-columns:repeat(2,1fr)}.journey-grid{gap:35px}}
@media(max-width:760px){.site-section{padding:74px 0}.hero{padding:45px 0 74px}.hero-grid,.journey-grid,.family-panel,.faq-grid,.start-grid{grid-template-columns:1fr}.hero-grid{gap:10px}.hero-copy{padding:0}.hero h1{font-size:clamp(38px,10vw,56px)}.hero-copy>p{font-size:16px}.hero-visual{min-height:370px}.hero-pets{max-height:365px}.hero-blob{inset:28px 18px 14px}.float-card.top{top:23px}.float-card.bottom{bottom:35px}.values{grid-template-columns:1fr;margin-top:-35px;padding:10px 18px}.values>div{padding:12px 4px}.values>div+div{border-left:0;border-top:1px solid var(--site-line)}.section-heading{align-items:start;flex-direction:column}.journey-stage{min-height:485px}.family-panel{padding:32px 24px;gap:20px}.more{padding-top:0}.more-grid{grid-template-columns:1fr}.faq-grid{gap:24px}.start-grid{gap:25px}}
@media(max-width:480px){.hero-actions .site-button{width:100%}.hero-visual{min-height:330px}.hero-pets{max-height:310px}.float-card{padding:9px 11px}.float-card strong{font-size:10px}.float-card small{font-size:9px}.feature-grid,.entry-grid{grid-template-columns:1fr}.feature-card{min-height:220px}.feature-card h3{margin-top:18px}.journey-stage{min-height:440px}.phone{transform:rotate(-3deg) scale(.9)}}
</style>

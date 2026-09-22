/**
 * 演示数据构建器（纯函数，T0.5）。
 * 口径来源：开发任务规划 §0.1-2 —— ≥2 账号、≥3 宠物、30 篇文章、200 POI、
 * 50 商品、举报/投稿样例、1 个家庭组。
 * 约束：本文件只产出数据结构；真实入库执行归开发机（本地测试阶段）。
 */

export const CHANNELS = ['犬', '猫', '异宠', '营养', '疾病健康', '训练行为', '行业快讯'];
const POI_TYPES = ['宠物店', '宠物医院', '宠物公园', '宠物友好餐厅', '寄养', '美容', '训练'];
const PRODUCT_CATEGORIES = ['主粮', '零食', '玩具', '清洁护理', '医疗保健', '出行用品', '服饰窝垫'];
const CITIES = [
  { name: '北京', lng: 116.4, lat: 39.9 },
  { name: '上海', lng: 121.47, lat: 31.23 },
  { name: '成都', lng: 104.07, lat: 30.57 },
  { name: '杭州', lng: 120.15, lat: 30.28 },
  { name: '广州', lng: 113.26, lat: 23.13 },
];

const round = (n, p = 6) => Math.round(n * 10 ** p) / 10 ** p;

export function buildSeedData(now = '2026-09-22') {
  const accounts = [
    { id: 'u1', phone: '13800000001', nickname: '旺财妈', role: 'USER' },
    { id: 'u2', phone: '13800000002', nickname: '豆豆爸', role: 'USER' },
  ];

  const pets = [
    { id: 'p1', ownerId: 'u1', name: '旺财', species: '犬', breed: '柯基', birth: '2022-05-01' },
    { id: 'p2', ownerId: 'u1', name: '咪咪', species: '猫', breed: '英短', birth: '2023-03-12' },
    { id: 'p3', ownerId: 'u2', name: '豆豆', species: '猫', breed: '布偶', birth: '2021-11-20' },
  ];

  const articles = Array.from({ length: 30 }, (_, i) => ({
    id: `a${i + 1}`,
    title: `精选养宠知识 第${i + 1}期 · ${CHANNELS[i % CHANNELS.length]}`,
    channel: CHANNELS[i % CHANNELS.length],
    authorId: i % 5 === 0 ? 'u2' : 'editor',
    status: i < 20 ? 'PUBLISHED' : i < 25 ? 'PENDING' : i < 28 ? 'REJECTED' : 'DRAFT',
    publishedAt: i < 20 ? now : null,
  }));

  const pois = Array.from({ length: 200 }, (_, i) => {
    const city = CITIES[i % CITIES.length];
    return {
      id: `poi${i + 1}`,
      name: `${city.name}${POI_TYPES[i % POI_TYPES.length]}·${i + 1}号店`,
      type: POI_TYPES[i % POI_TYPES.length],
      city: city.name,
      lng: round(city.lng + ((i % 17) - 8) * 0.01),
      lat: round(city.lat + ((i % 13) - 6) * 0.01),
      open: i % 9 !== 0,
    };
  });

  const products = Array.from({ length: 50 }, (_, i) => ({
    id: `g${i + 1}`,
    name: `示范商品 ${i + 1}号`,
    category: PRODUCT_CATEGORIES[i % PRODUCT_CATEGORIES.length],
    // BR-06 禁止品类：兽药/处方类一律不入 seed
    tags: ['示范'],
    status: i % 47 === 0 ? 'OFF_SHELF' : 'ON_SHELF',
  }));

  const submissions = articles
    .filter((a) => a.status !== 'PUBLISHED')
    .slice(0, 6)
    .map((a, i) => ({ id: `s${i + 1}`, articleId: a.id, state: a.status, rejectCount: a.status === 'REJECTED' ? 1 : 0 }));

  const reports = [
    { id: 'r1', targetType: 'ARTICLE', targetId: 'a21', reason: '广告引流', state: 'OPEN' },
    { id: 'r2', targetType: 'REVIEW', targetId: 'rv9', reason: '虚假信息', state: 'OPEN' },
    { id: 'r3', targetType: 'USER', targetId: 'u9', reason: '违规昵称', state: 'RESOLVED' },
  ];

  const family = {
    id: 'f1',
    name: '旺财的一家',
    ownerId: 'u1',
    members: [
      { userId: 'u1', level: 'OWNER' },
      { userId: 'u2', level: 'MANAGE' },
    ],
    sharedPets: [{ petId: 'p1', level: 'MANAGE' }],
    invite: { code: 'FAM7DAY', expiresAt: now },
  };

  const reminders = [
    { id: 'm1', petId: 'p1', type: '疫苗', recurrence: { kind: 'everyDays', days: 180, start: now }, state: 'ACTIVE' },
    { id: 'm2', petId: 'p1', type: '驱虫', recurrence: { kind: 'everyDays', days: 90, start: now }, state: 'ACTIVE' },
    { id: 'm3', petId: 'p2', type: '体检', recurrence: { kind: 'yearly', month: 6, day: 15, start: now }, state: 'ACTIVE' },
    { id: 'm4', petId: 'p3', type: '喂药', recurrence: { kind: 'once', date: now }, state: 'DONE' },
  ];

  const healthRecords = [
    { id: 'h1', petId: 'p1', kind: '疫苗', date: now, note: '狂犬' },
    { id: 'h2', petId: 'p1', kind: '驱虫', date: now, note: '体外' },
    { id: 'h3', petId: 'p2', kind: '体检', date: now, note: '常规' },
    { id: 'h4', petId: 'p3', kind: '就医', date: now, note: '皮肤' },
    { id: 'h5', petId: 'p3', kind: '用药', date: now, note: '外用药7天' },
    { id: 'h6', petId: 'p1', kind: '过敏', date: now, note: '牛肉' },
  ];

  return {
    generatedFor: 'cutepet-local-demo',
    generatedAt: now,
    accounts,
    pets,
    articles,
    pois,
    products,
    submissions,
    reports,
    family,
    reminders,
    healthRecords,
    counts: {
      accounts: accounts.length,
      pets: pets.length,
      articles: articles.length,
      pois: pois.length,
      products: products.length,
      submissions: submissions.length,
      reports: reports.length,
      families: 1,
      reminders: reminders.length,
      healthRecords: healthRecords.length,
    },
  };
}

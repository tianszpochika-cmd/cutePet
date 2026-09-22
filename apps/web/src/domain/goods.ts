/**
 * 用品端领域逻辑（T7.7 · 需求-用品 E1–E3 · U77/U88 引用展示镜像）。
 */

export const CATEGORIES = ['主粮', '零食', '玩具', '清洁护理', '医疗保健', '出行用品', '服饰窝垫'] as const;

export const SORT_OPTIONS = [
  { id: 'hot', label: '热度（30天浏览+收藏×3）' },
  { id: 'new', label: '最新上架' },
] as const;

/** 禁止品类关键词（展示层兜底；权威校验在后端 ProductRules） */
export const BANNED_KEYWORDS = ['兽药', '处方'];

/** 详情页兜底：命中黑名单 → 商品不可见文案 */
export function bannedProductGuard(category: string, name: string): string | null {
  for (const kw of ['兽药', '处方']) {
    if (category.includes(kw) || name.includes(kw)) return '该商品不适用本平台（禁止品类：兽药/处方类）。';
  }
  if (category === '医疗保健' && name.includes('药')) {
    return '该商品不适用本平台（药品类不入库）。';
  }
  return null;
}

/** U88 引用展示：普通下架带标保留历史、治理清除去敏感 */
export function refStateCopy(state: string): { text: string; tone: 'normal' | 'offShelf' | 'removed' } {
  switch (state) {
    case 'OFF_SHELF':
      return { text: '已下架（历史评测内容保留可读）', tone: 'offShelf' };
    case 'CLEARED':
      return { text: '商品信息已移除', tone: 'removed' };
    default:
      return { text: '', tone: 'normal' };
  }
}

/** U77：编辑部测试徽标仅编辑+员工显示 */
export function editorTestBadge(hasEditorPermission: boolean, authorIsStaff: boolean): boolean {
  return hasEditorPermission && authorIsStaff;
}

export const PRICE_DISCLAIMER = '参考价格仅供参考，不构成要约；本平台不提供购买服务（方案 A 纯内容导购）。';

/** 评测来源声明（强制字段展示） */
export function sourceLabel(sourceType: string): string {
  switch (sourceType) {
    case 'EDITORIAL':
      return '资料整理';
    case 'BRAND_INFO':
      return '品牌提供';
    case 'USER_SUBMIT':
      return '用户提交';
    case 'CSV_IMPORT':
      return '批量导入';
    default:
      return '未标注来源';
  }
}

/** 收藏成功后的清单提示 */
export function favoriteToast(favorited: boolean): string {
  return favorited ? '已加入我的商品收藏' : '已取消收藏';
}

/**
 * RBAC 权限点与预置角色（单一事实源）——追溯：功能设计-管理端 §6（32 权限点 + 6 预置角色）。
 * 纯函数，双端与服务端共用；服务端 M6 落地时以此为准。
 */

export const PERMISSIONS = [
  'article.create',
  'article.publish.direct',
  'article.edit.any',
  'article.takedown',
  'article.pinned.schedule',
  'review.article',
  'comment.manage',
  'report.handle',
  'taxonomy.manage',
  'banner.manage',
  'product.create.edit',
  'product.takedown',
  'product.import.csv',
  'list.manage',
  'review.bind.product',
  'poi.create.edit',
  'poi.close',
  'correction.handle',
  'ugv.review.hide',
  'route.approve',
  'activity.manage',
  'activity.export',
  'user.view',
  'user.ban',
  'user.post.right.cancel',
  'family.dispute',
  'appeal.handle',
  'dashboard.view.all',
  'notify.template',
  'audit.view',
  'rbac.manage',
  'system.settings',
] as const;

export type Permission = (typeof PERMISSIONS)[number];

export const ROLES: Record<string, readonly Permission[]> = {
  editor: [
    'article.create',
    'article.edit.any',
    'article.pinned.schedule',
    'taxonomy.manage',
    'banner.manage',
    'list.manage',
    'review.bind.product',
  ],
  reviewer: [
    'article.takedown',
    'review.article',
    'comment.manage',
    'report.handle',
    'ugv.review.hide',
    'route.approve',
    'user.view',
    'user.post.right.cancel',
    'dashboard.view.all',
  ],
  poiOperator: [
    'poi.create.edit',
    'poi.close',
    'correction.handle',
    'ugv.review.hide',
    'route.approve',
    'activity.manage',
    'activity.export',
  ],
  productOperator: ['product.create.edit', 'product.takedown', 'product.import.csv', 'list.manage', 'review.bind.product'],
  supervisor: PERMISSIONS.filter((p) => p !== 'rbac.manage' && p !== 'system.settings'),
  administrator: [...PERMISSIONS],
};

/** 权限校验：拥有任一所需权限即通过（一期 required 单点；扩展时可传数组取或） */
export function can(granted: readonly string[], required: Permission | string): boolean {
  return granted.includes(required);
}

/**
 * 看板访问（§6.1 注：各域看板隐含于业务权限）：
 * 有全量看板权限，或持有任一业务域权限即可见本域看板。
 */
const DOMAIN_PERMS: readonly string[] = [
  'article.create',
  'product.create.edit',
  'poi.create.edit',
  'review.article',
  'activity.manage',
];

export function canViewDashboard(granted: readonly string[]): boolean {
  if (granted.includes('dashboard.view.all')) return true;
  return granted.some((p) => DOMAIN_PERMS.includes(p));
}

export function isPermission(value: string): value is Permission {
  return (PERMISSIONS as readonly string[]).includes(value);
}

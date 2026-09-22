import type { RouteSpec, HttpMethod } from './routes.ts';
import { isPermission } from './permissions.ts';

export interface SpecIssue {
  routeId?: string;
  message: string;
}

const METHODS: HttpMethod[] = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'];
const AUTH = ['public', 'user', 'admin'];
const PARAM_RE = /^\/[A-Za-z0-9_\-/:]*$/;
const PARAM_TOKEN_RE = /^:[A-Za-z][A-Za-z0-9_]*$/;

/**
 * 路由契约完整性校验（纯函数）——contracts-validate 的内核。
 * 规则：id/method+path 唯一；auth/method 合法；admin 必带合法权限点；
 *       非 admin 不得带权限点；路径格式合法且参数 token 合法；32 权限点全被引用（覆盖完整性）。
 */
export function checkSpec(routes: RouteSpec[]): SpecIssue[] {
  const issues: SpecIssue[] = [];
  const ids = new Set<string>();
  const mp = new Set<string>();

  for (const r of routes) {
    if (!r.id || !/^[a-z][A-Za-z0-9]*$/.test(r.id)) {
      issues.push({ routeId: r.id, message: `id 非法: "${r.id}"` });
    }
    if (ids.has(r.id)) issues.push({ routeId: r.id, message: `id 重复: ${r.id}` });
    ids.add(r.id);

    if (!METHODS.includes(r.method)) issues.push({ routeId: r.id, message: `method 非法: ${r.method}` });
    if (!AUTH.includes(r.auth)) issues.push({ routeId: r.id, message: `auth 非法: ${r.auth}` });

    const key = `${r.method} ${r.path}`;
    if (mp.has(key)) issues.push({ routeId: r.id, message: `路由重复: ${key}` });
    mp.add(key);

    if (!r.path.startsWith('/') || !PARAM_RE.test(r.path)) {
      issues.push({ routeId: r.id, message: `path 非法: ${r.path}` });
    }
    for (const seg of r.path.split('/')) {
      if (seg.startsWith(':') && !PARAM_TOKEN_RE.test(seg)) {
        issues.push({ routeId: r.id, message: `路径参数非法: ${seg}` });
      }
    }

    if (r.auth === 'admin') {
      if (!r.permission) issues.push({ routeId: r.id, message: 'admin 路由缺少 permission' });
      else if (!isPermission(r.permission)) issues.push({ routeId: r.id, message: `permission 不在 32 权限点内: ${r.permission}` });
    } else if (r.permission) {
      issues.push({ routeId: r.id, message: '非 admin 路由不应携带 permission' });
    }
    if (!r.summary) issues.push({ routeId: r.id, message: 'summary 缺失' });
  }

  return issues;
}

/** 覆盖完整性：每个权限点至少被一条路由引用（防权限点漂移） */
export function uncoveredPermissions(routes: RouteSpec[], permissions: readonly string[]): string[] {
  const used = new Set(routes.map((r) => r.permission).filter(Boolean));
  return permissions.filter((p) => !used.has(p));
}

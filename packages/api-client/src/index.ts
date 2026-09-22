/**
 * 由 contracts 生成的 API 客户端（T0.4 全量 schema 后启用生成脚本）。
 * 基础约定：网关 http://127.0.0.1:18470，JWT 双 token，错误统一格式。
 */
export const GATEWAY_BASE = 'http://127.0.0.1:18470';

export type ApiError = { code: string; message: string; requestId?: string };

export function isApiError(value: unknown): value is ApiError {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as ApiError).code === 'string' &&
    typeof (value as ApiError).message === 'string'
  );
}

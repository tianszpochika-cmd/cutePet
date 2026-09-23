/** api-client 请求内核（纯逻辑可测；网络调用属本地测试阶段）。 */

/** 默认走同源 /api 反向代理，避免 H5 指向设备自身的 localhost。 */
export const GATEWAY_BASE = '/api';
let gatewayBase = GATEWAY_BASE;

export function setGatewayBase(value: string | null | undefined): void {
  const raw = value?.trim() ?? '';
  if (!raw) {
    gatewayBase = GATEWAY_BASE;
    return;
  }
  const parsed = new URL(raw);
  if (!['http:', 'https:'].includes(parsed.protocol) || parsed.username || parsed.password || parsed.search || parsed.hash) {
    throw new Error('网关地址必须是不含账号、查询和片段的 HTTP(S) 地址');
  }
  gatewayBase = parsed.href.replace(/\/$/, '');
}

export function getGatewayBase(): string {
  return gatewayBase;
}

export interface CallArgs {
  params?: Record<string, string | number>;
  /** 兼容各端现有的 path 参数写法。 */
  path?: Record<string, string | number>;
  query?: Record<string, string | number | boolean | undefined | null>;
  body?: unknown;
  signal?: AbortSignal;
}

let accessToken: string | null = null;
export function setAccessToken(token: string | null): void {
  accessToken = token;
}
export function getAccessToken(): string | null {
  return accessToken;
}

export class ApiClientError extends Error {
  readonly status: number;
  readonly code: string;
  readonly requestId?: string;

  constructor(status: number, code: string, message: string, requestId?: string) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.code = code;
    this.requestId = requestId;
  }
}

export function isApiError(value: unknown): value is { code: string; message: string; requestId?: string } {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as { code?: unknown }).code === 'string' &&
    typeof (value as { message?: unknown }).message === 'string'
  );
}

/** 路径参数替换 + query 序列化（纯函数，可单测） */
export function buildUrl(path: string, args: CallArgs = {}): string {
  const resolved = path.replace(/:([A-Za-z][A-Za-z0-9_]*)/g, (_m, key: string) => {
    const value = args.params?.[key] ?? args.path?.[key];
    if (value === undefined || value === null || value === '') {
      throw new ApiClientError(400, 'MISSING_PARAM', `缺少路径参数: ${key}`);
    }
    return encodeURIComponent(String(value));
  });
  const entries = Object.entries(args.query ?? {}).filter(([, v]) => v !== undefined && v !== null);
  if (entries.length === 0) return resolved;
  const qs = entries
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&');
  return `${resolved}?${qs}`;
}

export async function request<T>(method: string, path: string, args: CallArgs = {}): Promise<T> {
  const url = gatewayBase + buildUrl(path, args);
  const headers: Record<string, string> = {};
  if (accessToken) headers.Authorization = `Bearer ${accessToken}`;
  if (args.body !== undefined) headers['Content-Type'] = 'application/json';

  const res = await fetch(url, {
    method,
    headers,
    body: args.body !== undefined ? JSON.stringify(args.body) : undefined,
    signal: args.signal,
  });

  if (res.status === 204) return undefined as T;

  const text = await res.text();
  let payload: unknown = null;
  try {
    payload = text ? JSON.parse(text) : null;
  } catch {
    payload = null;
  }

  if (!res.ok) {
    if (isApiError(payload)) {
      throw new ApiClientError(res.status, payload.code, payload.message, payload.requestId);
    }
    throw new ApiClientError(res.status, `HTTP_${res.status}`, text || res.statusText);
  }
  return payload as T;
}

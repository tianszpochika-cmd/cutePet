/** api-client 请求内核（纯逻辑可测；网络调用属本地测试阶段）。 */

export const GATEWAY_BASE = 'http://127.0.0.1:18470';

export interface CallArgs {
  params?: Record<string, string>;
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
    const value = args.params?.[key];
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
  const url = GATEWAY_BASE + buildUrl(path, args);
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

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

export type Row = Record<string, any>;

export class ApiError extends Error {
  constructor(message: string, public status: number) { super(message); }
}

export const session = {
  get token() { return localStorage.getItem('reoagms_token') || ''; },
  get user(): { name: string; email: string; role: string } | null {
    try { return JSON.parse(localStorage.getItem('reoagms_session') || 'null'); } catch { return null; }
  },
  save(token: string, user: { name: string; email: string; role: string }) {
    localStorage.setItem('reoagms_token', token);
    localStorage.setItem('reoagms_session', JSON.stringify(user));
  },
  clear() { localStorage.removeItem('reoagms_token'); localStorage.removeItem('reoagms_session'); },
};

let onUnauthorized: () => void = () => {};
export const setUnauthorizedHandler = (fn: () => void) => { onUnauthorized = fn; };

async function request(path: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers);
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (session.token) headers.set('Authorization', `Bearer ${session.token}`);
  const response = await fetch(`${API_BASE}${path.startsWith('/') ? path : `/${path}`}`, { ...init, headers });
  if (response.status === 401 && !path.includes('/auth/')) onUnauthorized();
  return response;
}

export async function api<T = any>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await request(path, init);
  const text = await response.text();
  let body: any;
  if (text) { try { body = JSON.parse(text); } catch { body = text; } }
  if (!response.ok) {
    const detail = body?.validationErrors ? Object.values(body.validationErrors).join(', ') : '';
    throw new ApiError(detail || body?.message || body?.error || `Request failed (${response.status})`, response.status);
  }
  return unwrap(body) as T;
}

/** Services answer either with a bare payload, a Spring page, or an { success, data } envelope. */
export function unwrap(body: any): any {
  if (body && typeof body === 'object' && !Array.isArray(body) && 'success' in body && 'data' in body) return body.data;
  return body;
}

export function asArray<T = Row>(value: any): T[] {
  const v = unwrap(value);
  if (Array.isArray(v)) return v;
  if (Array.isArray(v?.content)) return v.content;
  if (Array.isArray(v?.items)) return v.items;
  return [];
}

export const get = <T = Row>(path: string) => api(path).then((v) => asArray<T>(v));
export const post = <T = Row>(path: string, body?: unknown) => api<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) });
export const put = <T = Row>(path: string, body?: unknown) => api<T>(path, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body) });
export const del = (path: string) => api(path, { method: 'DELETE' });

export async function download(path: string, filename: string) {
  const response = await request(path);
  if (!response.ok) throw new ApiError(`Export failed (${response.status})`, response.status);
  const url = URL.createObjectURL(await response.blob());
  const a = document.createElement('a');
  a.href = url; a.download = filename; a.click();
  URL.revokeObjectURL(url);
}

/** The backend stores LocalDateTime in UTC without a zone designator. */
export const parseTime = (value?: string | null) => (value ? new Date(/[zZ]|[+-]\d\d:?\d\d$/.test(value) ? value : `${value}Z`) : null);
export const toApiTime = (d: Date) => d.toISOString().slice(0, 19);

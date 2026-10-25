const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

export class ApiError extends Error {
  constructor(message: string, public status: number) { super(message); }
}

export async function api<T = unknown>(path: string, init: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('reoagms_token');
  const headers = new Headers(init.headers);
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const response = await fetch(`${API_BASE}${path.startsWith('/') ? path : `/${path}`}`, { ...init, headers });
  const text = await response.text();
  let body: any = undefined;
  if (text) { try { body = JSON.parse(text); } catch { body = text; } }
  if (!response.ok) {
    const message = body?.message || body?.error || (typeof body === 'string' ? body : `Request failed (${response.status})`);
    throw new ApiError(message, response.status);
  }
  return body as T;
}

export const endpoints = {
  assets: '/v1/assets', facilities: '/v1/facilities', sensors: '/v1/sensors',
  readings: '/v1/sensor-readings', alerts: '/v1/alerts', rules: '/v1/alert-rules', telemetry: '/v1/telemetry',
  workOrders: '/v1/work-orders', tasks: '/v1/maintenance-tasks', technicians: '/v1/technicians',
  history: '/v1/maintenance-history', maintenanceReports: '/v1/maintenance-reports',
  dashboards: '/v1/dashboards', kpis: '/v1/kpis', energyReports: '/v1/energy-reports', performanceReports: '/v1/performance-reports',
  notifications: '/v1/notifications', users: '/v1/users',
};

export function asArray<T = any>(value: any): T[] {
  if (Array.isArray(value)) return value;
  if (Array.isArray(value?.content)) return value.content;
  if (Array.isArray(value?.data)) return value.data;
  if (Array.isArray(value?.items)) return value.items;
  return [];
}

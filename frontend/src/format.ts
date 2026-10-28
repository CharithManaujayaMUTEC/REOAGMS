import { parseTime } from './api';

export const label = (value?: string | null) =>
  value ? value.toLowerCase().split('_').map((w) => w.charAt(0).toUpperCase() + w.slice(1)).join(' ') : '—';

export const num = (value: any, digits = 1) => {
  const n = Number(value);
  if (value === null || value === undefined || value === '' || Number.isNaN(n)) return '—';
  return n.toLocaleString(undefined, { maximumFractionDigits: digits, minimumFractionDigits: 0 });
};

/** Compact power/energy: 12,400 kW -> 12.4 MW */
export const power = (kw: number, unit: 'kW' | 'kWh' = 'kW') => {
  const abs = Math.abs(kw);
  const u = unit === 'kW' ? ['kW', 'MW', 'GW'] : ['kWh', 'MWh', 'GWh'];
  if (abs >= 1e6) return { value: num(kw / 1e6, 2), unit: u[2] };
  if (abs >= 1e3) return { value: num(kw / 1e3, 2), unit: u[1] };
  return { value: num(kw, 1), unit: u[0] };
};

export const timeOnly = (value?: string | null) =>
  parseTime(value)?.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) ?? '—';

export const dateTime = (value?: string | null) => {
  const d = parseTime(value);
  return d ? `${d.toLocaleDateString([], { month: 'short', day: 'numeric' })} ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}` : '—';
};

export const ago = (value?: string | null) => {
  const d = parseTime(value);
  if (!d) return '—';
  const s = Math.max(0, Math.round((Date.now() - d.getTime()) / 1000));
  if (s < 60) return `${s}s ago`;
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
};

export type Tone = 'cyan' | 'volt' | 'green' | 'red' | 'orange' | 'violet' | 'muted';

export const severityTone = (s?: string): Tone =>
  ({ CRITICAL: 'red', HIGH: 'orange', MEDIUM: 'volt', LOW: 'cyan' } as Record<string, Tone>)[s || ''] || 'muted';

export const statusTone = (s?: string): Tone =>
  ({
    OPEN: 'red', ACKNOWLEDGED: 'volt', RESOLVED: 'green', ACTIVE: 'green', ONLINE: 'green', OFFLINE: 'muted', WARNING: 'volt',
    FAULT: 'red', FAULTY: 'red', MAINTENANCE: 'orange', INACTIVE: 'muted', RETIRED: 'muted', CALIBRATING: 'violet',
    CREATED: 'cyan', ASSIGNED: 'violet', IN_PROGRESS: 'volt', COMPLETED: 'green', CANCELLED: 'muted',
    SENT: 'green', PENDING: 'volt', FAILED: 'red', RETRYING: 'orange',
    AVAILABLE: 'green', BUSY: 'orange', OFF_DUTY: 'muted',
  } as Record<string, Tone>)[s || ''] || 'muted';

export const ROLE_LABEL: Record<string, string> = {
  ADMIN: 'System Administrator', OPERATIONS_MANAGER: 'Operations Manager', PLANT_OPERATOR: 'Plant Operator',
  MAINTENANCE_ENGINEER: 'Maintenance Engineer', EXECUTIVE_MANAGER: 'Executive Management',
};

const MANAGERS = ['ADMIN', 'OPERATIONS_MANAGER'];
export const can = {
  manage: (role?: string) => MANAGERS.includes(role || ''),
  operate: (role?: string) => [...MANAGERS, 'PLANT_OPERATOR'].includes(role || ''),
  maintain: (role?: string) => [...MANAGERS, 'MAINTENANCE_ENGINEER'].includes(role || ''),
  admin: (role?: string) => role === 'ADMIN',
};

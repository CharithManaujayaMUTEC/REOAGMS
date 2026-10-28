import { useEffect, useState, type ReactNode, type FormEvent } from 'react';
import { BatteryCharging, Droplets, Sun, Waves, Wind, Zap, X, Inbox, Loader2, CheckCircle2, AlertTriangle, Info, OctagonAlert } from 'lucide-react';
import { Area, AreaChart, ResponsiveContainer } from 'recharts';
import type { Tone } from './format';
import { useToast } from './store';

export const FACILITY_ICON: Record<string, typeof Zap> = {
  SOLAR_FARM: Sun, WIND_FARM: Wind, HYDRO_PLANT: Droplets, BATTERY_STORAGE: BatteryCharging, TIDAL_POWER_PLANT: Waves,
};
export const FacilityIcon = ({ type, size = 18 }: { type?: string; size?: number }) => {
  const Icon = FACILITY_ICON[type || ''] || Zap;
  return <Icon size={size} />;
};

export const Panel = ({ title, subtitle, actions, children, className = '', flush = false }: {
  title?: ReactNode; subtitle?: ReactNode; actions?: ReactNode; children: ReactNode; className?: string; flush?: boolean;
}) => (
  <section className={`panel ${className}`}>
    {(title || actions) && (
      <header className="panel-head">
        <div><h3>{title}</h3>{subtitle && <p>{subtitle}</p>}</div>
        {actions && <div className="panel-actions">{actions}</div>}
      </header>
    )}
    <div className={flush ? 'panel-body flush' : 'panel-body'}>{children}</div>
  </section>
);

export const Badge = ({ tone = 'muted', children, dot = false }: { tone?: Tone; children: ReactNode; dot?: boolean }) => (
  <span className={`badge tone-${tone}`}>{dot && <i className="dot" />}{children}</span>
);

export function Kpi({ label, value, unit, icon, tone = 'cyan', hint, spark }: {
  label: string; value: ReactNode; unit?: string; icon: ReactNode; tone?: Tone; hint?: ReactNode; spark?: number[];
}) {
  return (
    <div className={`kpi tone-${tone}`}>
      <div className="kpi-top">
        <span className="kpi-label">{label}</span>
        <span className="kpi-icon">{icon}</span>
      </div>
      <div className="kpi-value">{value}{unit && <small>{unit}</small>}</div>
      {hint && <div className="kpi-hint">{hint}</div>}
      {spark && spark.length > 1 && (
        <div className="kpi-spark">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={spark.map((v, i) => ({ i, v }))} margin={{ top: 2, right: 0, bottom: 0, left: 0 }}>
              <defs><linearGradient id={`sp-${label}`} x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="currentColor" stopOpacity={0.45} /><stop offset="100%" stopColor="currentColor" stopOpacity={0} /></linearGradient></defs>
              <Area type="monotone" dataKey="v" stroke="currentColor" strokeWidth={1.6} fill={`url(#sp-${label})`} isAnimationActive={false} />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  );
}

export const Spinner = ({ label = 'Loading' }: { label?: string }) => (
  <div className="state"><Loader2 className="spin" size={22} /><span>{label}…</span></div>
);
export const Empty = ({ title = 'Nothing here yet', hint }: { title?: string; hint?: string }) => (
  <div className="state"><Inbox size={26} /><b>{title}</b>{hint && <span>{hint}</span>}</div>
);
export const ErrorState = ({ message, retry }: { message: string; retry?: () => void }) => (
  <div className="state error"><OctagonAlert size={24} /><b>Could not load data</b><span>{message}</span>{retry && <button className="btn ghost sm" onClick={retry}>Retry</button>}</div>
);

export type Column<T> = { key: string; header: string; render?: (row: T) => ReactNode; className?: string; align?: 'right' };
export function DataTable<T extends Record<string, any>>({ columns, rows, empty, rowKey = 'id', onRowClick }: {
  columns: Column<T>[]; rows: T[]; empty?: string; rowKey?: string; onRowClick?: (row: T) => void;
}) {
  if (!rows.length) return <Empty title={empty || 'No records'} />;
  return (
    <div className="table-wrap">
      <table className="table">
        <thead><tr>{columns.map((c) => <th key={c.key} className={c.align === 'right' ? 'r' : ''}>{c.header}</th>)}</tr></thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={row[rowKey] ?? i} onClick={onRowClick ? () => onRowClick(row) : undefined} className={onRowClick ? 'click' : ''}>
              {columns.map((c) => <td key={c.key} className={`${c.className || ''}${c.align === 'right' ? ' r' : ''}`}>{c.render ? c.render(row) : (row[c.key] ?? '—')}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function Modal({ title, subtitle, onClose, children, wide = false }: { title: string; subtitle?: string; onClose: () => void; children: ReactNode; wide?: boolean }) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);
  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`modal ${wide ? 'wide' : ''}`} role="dialog" aria-modal="true" aria-label={title}>
        <header><div><h3>{title}</h3>{subtitle && <p>{subtitle}</p>}</div><button className="icon-btn" onClick={onClose} aria-label="Close"><X size={18} /></button></header>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  );
}

export type FieldDef = {
  name: string; label: string; type?: 'text' | 'number' | 'select' | 'date' | 'datetime-local' | 'textarea' | 'email' | 'password' | 'checkbox';
  options?: { value: string; label: string }[]; required?: boolean; step?: string; placeholder?: string; half?: boolean; defaultValue?: any;
};

export function FormModal({ title, subtitle, fields, submitLabel = 'Save', onSubmit, onClose }: {
  title: string; subtitle?: string; fields: FieldDef[]; submitLabel?: string;
  onSubmit: (values: Record<string, any>) => Promise<unknown>; onClose: () => void;
}) {
  const [values, setValues] = useState<Record<string, any>>(() => Object.fromEntries(fields.map((f) => [f.name, f.defaultValue ?? (f.type === 'checkbox' ? false : '')])));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const set = (name: string, v: any) => setValues((s) => ({ ...s, [name]: v }));

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true); setError('');
    const out: Record<string, any> = {};
    for (const f of fields) {
      const v = values[f.name];
      if (v === '' || v === undefined) { out[f.name] = null; continue; }
      out[f.name] = f.type === 'number' ? Number(v) : v;
    }
    try { await onSubmit(out); onClose(); }
    catch (err: any) { setError(err?.message || 'Request failed'); setBusy(false); }
  };

  return (
    <Modal title={title} subtitle={subtitle} onClose={onClose}>
      <form onSubmit={submit} className="form-grid">
        {fields.map((f) => (
          <label key={f.name} className={`field ${f.half ? 'half' : ''} ${f.type === 'checkbox' ? 'check' : ''}`}>
            <span>{f.label}{f.required && <em>*</em>}</span>
            {f.type === 'select' ? (
              <select required={f.required} value={values[f.name]} onChange={(e) => set(f.name, e.target.value)}>
                <option value="">Select…</option>
                {f.options?.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
            ) : f.type === 'textarea' ? (
              <textarea rows={3} required={f.required} value={values[f.name]} placeholder={f.placeholder} onChange={(e) => set(f.name, e.target.value)} />
            ) : f.type === 'checkbox' ? (
              <input type="checkbox" checked={!!values[f.name]} onChange={(e) => set(f.name, e.target.checked)} />
            ) : (
              <input type={f.type || 'text'} step={f.step} required={f.required} value={values[f.name]} placeholder={f.placeholder} onChange={(e) => set(f.name, e.target.value)} />
            )}
          </label>
        ))}
        {error && <div className="form-error full"><AlertTriangle size={16} />{error}</div>}
        <div className="form-actions full">
          <button type="button" className="btn ghost" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn primary" disabled={busy}>{busy ? <Loader2 className="spin" size={16} /> : null}{submitLabel}</button>
        </div>
      </form>
    </Modal>
  );
}

export function Tabs<T extends string>({ tabs, active, onChange }: { tabs: { id: T; label: string; count?: number }[]; active: T; onChange: (id: T) => void }) {
  return (
    <div className="tabs" role="tablist">
      {tabs.map((t) => (
        <button key={t.id} role="tab" aria-selected={active === t.id} className={active === t.id ? 'active' : ''} onClick={() => onChange(t.id)}>
          {t.label}{t.count !== undefined && <span>{t.count}</span>}
        </button>
      ))}
    </div>
  );
}

export function Toasts() {
  const { toasts, dismiss } = useToast();
  const icon = { ok: <CheckCircle2 size={18} />, error: <OctagonAlert size={18} />, alert: <AlertTriangle size={18} />, info: <Info size={18} /> };
  return (
    <div className="toasts" aria-live="polite">
      {toasts.map((t) => (
        <div key={t.id} className={`toast ${t.tone}`} onClick={() => dismiss(t.id)}>
          {icon[t.tone]}<div><b>{t.title}</b>{t.body && <p>{t.body}</p>}</div>
        </div>
      ))}
    </div>
  );
}

/** Runs an async action, surfacing success/failure as a toast. */
export function useAction() {
  const { push } = useToast();
  return async (fn: () => Promise<unknown>, okTitle: string) => {
    try { await fn(); push({ tone: 'ok', title: okTitle }); return true; }
    catch (e: any) { push({ tone: 'error', title: 'Action failed', body: e?.message }); return false; }
  };
}

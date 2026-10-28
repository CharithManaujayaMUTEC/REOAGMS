import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { Client } from '@stomp/stompjs';
import { get, session, setUnauthorizedHandler, type Row } from './api';

/* ---------------------------------------------------------------- auth */
type SessionUser = { name: string; email: string; role: string };
type AuthCtx = { user: SessionUser | null; signIn: (token: string, user: SessionUser) => void; signOut: () => void };
const AuthContext = createContext<AuthCtx>({ user: null, signIn: () => {}, signOut: () => {} });
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(() => (session.token ? session.user : null));
  const signOut = useCallback(() => { session.clear(); setUser(null); }, []);
  const signIn = useCallback((token: string, u: SessionUser) => { session.save(token, u); setUser(u); }, []);
  useEffect(() => { setUnauthorizedHandler(signOut); }, [signOut]);
  const value = useMemo(() => ({ user, signIn, signOut }), [user, signIn, signOut]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/* --------------------------------------------------------------- toasts */
export type Toast = { id: number; tone: 'ok' | 'error' | 'alert' | 'info'; title: string; body?: string };
type ToastCtx = { toasts: Toast[]; push: (t: Omit<Toast, 'id'>) => void; dismiss: (id: number) => void };
const ToastContext = createContext<ToastCtx>({ toasts: [], push: () => {}, dismiss: () => {} });
export const useToast = () => useContext(ToastContext);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const seq = useRef(0);
  const dismiss = useCallback((id: number) => setToasts((t) => t.filter((x) => x.id !== id)), []);
  const push = useCallback((t: Omit<Toast, 'id'>) => {
    const id = ++seq.current;
    setToasts((list) => [...list.slice(-1), { ...t, id }]);
    setTimeout(() => dismiss(id), t.tone === 'alert' ? 6000 : 4000);
  }, [dismiss]);
  const value = useMemo(() => ({ toasts, push, dismiss }), [toasts, push, dismiss]);
  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>;
}

/* ----------------------------------------------------------- reference */
type RefCtx = {
  facilities: Row[]; assets: Row[]; sensors: Row[];
  facilityName: (id?: string) => string; assetName: (id?: string) => string; assetFacility: (id?: string) => Row | undefined;
  reload: () => void;
};
const RefContext = createContext<RefCtx>({
  facilities: [], assets: [], sensors: [], facilityName: () => '—', assetName: () => '—', assetFacility: () => undefined, reload: () => {},
});
export const useRef_ = () => useContext(RefContext);

export function RefProvider({ children }: { children: ReactNode }) {
  const [facilities, setFacilities] = useState<Row[]>([]);
  const [assets, setAssets] = useState<Row[]>([]);
  const [sensors, setSensors] = useState<Row[]>([]);
  const load = useCallback(async () => {
    const [f, a, s] = await Promise.allSettled([get('/v1/facilities'), get('/v1/assets?size=500'), get('/v1/sensors?size=500')]);
    if (f.status === 'fulfilled') setFacilities(f.value);
    if (a.status === 'fulfilled') setAssets(a.value);
    if (s.status === 'fulfilled') setSensors(s.value);
  }, []);
  useEffect(() => { void load(); const t = setInterval(load, 30000); return () => clearInterval(t); }, [load]);
  const value = useMemo<RefCtx>(() => {
    const fm = new Map(facilities.map((f) => [f.id, f]));
    const am = new Map(assets.map((a) => [a.id, a]));
    return {
      facilities, assets, sensors, reload: load,
      facilityName: (id) => fm.get(id)?.name ?? '—',
      assetName: (id) => am.get(id)?.name ?? '—',
      assetFacility: (id) => fm.get(am.get(id)?.facilityId),
    };
  }, [facilities, assets, sensors, load]);
  return <RefContext.Provider value={value}>{children}</RefContext.Provider>;
}

/* ---------------------------------------------------------- live (STOMP) */
export type LiveAlert = {
  alertId: string; assetId?: string; facilityId?: string; metricType?: string; value?: number; threshold?: number;
  severity: string; message: string; occurredAt?: string; receivedAt: number;
};
type LiveCtx = { connected: boolean; alerts: LiveAlert[]; tick: number };
const LiveContext = createContext<LiveCtx>({ connected: false, alerts: [], tick: 0 });
export const useLive = () => useContext(LiveContext);

export function LiveProvider({ children }: { children: ReactNode }) {
  const [connected, setConnected] = useState(false);
  const [alerts, setAlerts] = useState<LiveAlert[]>([]);
  const [tick, setTick] = useState(0);
  const { push } = useToast();
  const pushRef = useRef(push);
  pushRef.current = push;

  useEffect(() => {
    const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const client = new Client({
      brokerURL: `${scheme}://${window.location.host}/ws`,
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/alerts', (frame) => {
          try {
            const e = JSON.parse(frame.body);
            const live: LiveAlert = { ...e, receivedAt: Date.now() };
            setAlerts((list) => [live, ...list].slice(0, 25));
            setTick((t) => t + 1);
            pushRef.current({ tone: 'alert', title: `${e.severity} alert`, body: e.message });
          } catch { /* ignore malformed frame */ }
        });
      },
      onWebSocketClose: () => setConnected(false),
      onStompError: () => setConnected(false),
    });
    client.activate();
    return () => { void client.deactivate(); };
  }, []);

  const value = useMemo(() => ({ connected, alerts, tick }), [connected, alerts, tick]);
  return <LiveContext.Provider value={value}>{children}</LiveContext.Provider>;
}

/* ------------------------------------------------------------ polling */
export function usePoll<T>(fn: () => Promise<T>, deps: unknown[] = [], intervalMs = 0) {
  const [data, setData] = useState<T | undefined>(undefined);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const fnRef = useRef(fn);
  fnRef.current = fn;
  const alive = useRef(true);

  const reload = useCallback(async () => {
    try { const v = await fnRef.current(); if (alive.current) { setData(v); setError(''); } }
    catch (e: any) { if (alive.current) setError(e?.message || 'Request failed'); }
    finally { if (alive.current) setLoading(false); }
  }, []);

  useEffect(() => {
    alive.current = true;
    setLoading(true);
    void reload();
    const t = intervalMs > 0 ? setInterval(reload, intervalMs) : undefined;
    return () => { alive.current = false; if (t) clearInterval(t); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, intervalMs, reload]);

  return { data, error, loading, reload };
}

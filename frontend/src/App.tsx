import { useEffect, useState, type ReactNode } from 'react';
import { Activity, BarChart3, BellRing, Building2, LayoutDashboard, LogOut, Mail, Menu, ShieldCheck, Wrench, Zap } from 'lucide-react';
import { get } from './api';
import { can, ROLE_LABEL } from './format';
import { AuthProvider, LiveProvider, RefProvider, ToastProvider, useAuth, useLive, usePoll } from './store';
import { Toasts } from './ui';
import Admin from './pages/Admin';
import Alerts from './pages/Alerts';
import Analytics from './pages/Analytics';
import Assets from './pages/Assets';
import Login from './pages/Login';
import Maintenance from './pages/Maintenance';
import Monitoring from './pages/Monitoring';
import Notifications from './pages/Notifications';
import Overview from './pages/Overview';

type PageDef = { id: string; title: string; subtitle: string; icon: ReactNode; roles?: (role?: string) => boolean };
const PAGES: PageDef[] = [
  { id: 'overview', title: 'Control room', subtitle: 'Portfolio status at a glance', icon: <LayoutDashboard size={18} /> },
  { id: 'monitoring', title: 'Live monitoring', subtitle: 'Real-time sensor telemetry', icon: <Activity size={18} /> },
  { id: 'alerts', title: 'Alerts', subtitle: 'Incidents and detection rules', icon: <BellRing size={18} /> },
  { id: 'maintenance', title: 'Maintenance', subtitle: 'Work orders and field crew', icon: <Wrench size={18} /> },
  { id: 'assets', title: 'Assets', subtitle: 'Facilities, equipment and sensors', icon: <Building2 size={18} /> },
  { id: 'analytics', title: 'Analytics', subtitle: 'KPIs and energy reports', icon: <BarChart3 size={18} /> },
  { id: 'notifications', title: 'Notifications', subtitle: 'Delivery history', icon: <Mail size={18} /> },
  { id: 'admin', title: 'Administration', subtitle: 'Users and audit trail', icon: <ShieldCheck size={18} />, roles: can.admin },
];

const currentPage = () => window.location.hash.replace('#/', '') || 'overview';

function Shell() {
  const { user, signOut } = useAuth();
  const live = useLive();
  const [page, setPage] = useState(currentPage);
  const [menu, setMenu] = useState(false);
  const [clock, setClock] = useState(new Date());
  const openAlerts = usePoll(() => get('/v1/alerts').then((a) => a.filter((x) => x.status === 'OPEN').length), [live.tick], 10000);

  useEffect(() => {
    const onHash = () => setPage(currentPage());
    window.addEventListener('hashchange', onHash);
    const t = setInterval(() => setClock(new Date()), 1000);
    return () => { window.removeEventListener('hashchange', onHash); clearInterval(t); };
  }, []);

  const go = (id: string) => { window.location.hash = `/${id}`; setMenu(false); };
  const visible = PAGES.filter((p) => !p.roles || p.roles(user?.role));
  const active = visible.find((p) => p.id === page) ?? visible[0];

  const body = {
    overview: <Overview onNavigate={go} />, monitoring: <Monitoring />, alerts: <Alerts />, maintenance: <Maintenance />,
    assets: <Assets />, analytics: <Analytics />, notifications: <Notifications />, admin: <Admin />,
  }[active.id];

  return (
    <div className="shell">
      <aside className={`sidebar ${menu ? 'open' : ''}`}>
        <div className="brand"><span className="brand-mark"><Zap size={20} /></span><div><b>REOAGMS</b><small>GRID OPERATIONS</small></div></div>
        <nav>
          {visible.map((p) => (
            <button key={p.id} className={`nav-item ${active.id === p.id ? 'active' : ''}`} onClick={() => go(p.id)}>
              {p.icon}<span>{p.title}</span>
              {p.id === 'alerts' && !!openAlerts.data && <em className="count">{openAlerts.data}</em>}
            </button>
          ))}
        </nav>
        <div className="sidebar-foot">
          <div className={`link-state ${live.connected ? 'on' : ''}`}><i />{live.connected ? 'Realtime link established' : 'Realtime link offline'}</div>
          <div className="user-chip">
            <span className="avatar">{(user?.name || '?').split(' ').map((s) => s[0]).slice(0, 2).join('')}</span>
            <div><b>{user?.name}</b><small>{ROLE_LABEL[user?.role || ''] ?? user?.role}</small></div>
            <button className="icon-btn" onClick={signOut} title="Sign out" aria-label="Sign out"><LogOut size={16} /></button>
          </div>
        </div>
      </aside>
      {menu && <div className="scrim" onClick={() => setMenu(false)} />}

      <main className="main">
        <header className="topbar">
          <button className="icon-btn menu-btn" onClick={() => setMenu(true)} aria-label="Open menu"><Menu size={20} /></button>
          <div className="title"><h1>{active.title}</h1><p>{active.subtitle}</p></div>
          <div className="top-right">
            <span className={`pill ${live.connected ? 'live' : ''}`}><i />{live.connected ? 'LIVE' : 'RECONNECTING'}</span>
            <span className="clock mono">{clock.toLocaleTimeString([], { hour12: false })}</span>
          </div>
          <div className="pulse-line" />
        </header>
        <div className="content" key={active.id}>{body}</div>
      </main>
    </div>
  );
}

function Gate() {
  const { user } = useAuth();
  return user ? <RefProvider><LiveProvider><Shell /></LiveProvider></RefProvider> : <Login />;
}

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <Gate />
        <Toasts />
      </AuthProvider>
    </ToastProvider>
  );
}

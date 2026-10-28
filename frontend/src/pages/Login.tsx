import { useState, type FormEvent } from 'react';
import { Activity, ArrowRight, Loader2, LockKeyhole, Mail, ShieldCheck, User, Zap } from 'lucide-react';
import { api } from '../api';
import { ROLE_LABEL } from '../format';
import { useAuth } from '../store';

const DEMO = [
  { email: 'admin@reoagms.com', role: 'ADMIN' },
  { email: 'manager@reoagms.com', role: 'OPERATIONS_MANAGER' },
  { email: 'operator@reoagms.com', role: 'PLANT_OPERATOR' },
  { email: 'engineer@reoagms.com', role: 'MAINTENANCE_ENGINEER' },
  { email: 'executive@reoagms.com', role: 'EXECUTIVE_MANAGER' },
];
const DEMO_PASSWORD = 'Reoagms@2026';

export default function Login() {
  const { signIn } = useAuth();
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      const res = await api(mode === 'login' ? '/v1/auth/login' : '/v1/auth/register', {
        method: 'POST',
        body: JSON.stringify(mode === 'login' ? { email, password } : { firstName, lastName, email, password }),
      });
      if (!res?.token) throw new Error('The server did not return a session token.');
      signIn(res.token, { name: `${res.firstName ?? ''} ${res.lastName ?? ''}`.trim() || res.email, email: res.email, role: res.role });
    } catch (err: any) {
      setError(err.status === 401 || err.status === 403 ? 'Invalid email or password.' : err.message || 'Sign-in failed.');
      setBusy(false);
    }
  };

  return (
    <div className="login-screen">
      <div className="login-hero">
        <div className="grid-glow" />
        <svg className="bolt-art" viewBox="0 0 400 520" aria-hidden="true">
          <defs><linearGradient id="g" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stopColor="#ffe66d" /><stop offset="1" stopColor="#ff9f1c" /></linearGradient></defs>
          <path className="bolt" d="M240 20 70 290h110l-40 210 190-300H215z" fill="url(#g)" />
          <circle className="ring r1" cx="200" cy="260" r="150" /><circle className="ring r2" cx="200" cy="260" r="205" />
        </svg>
        <div className="hero-copy">
          <div className="brand big"><span className="brand-mark"><Zap size={22} /></span><div><b>REOAGMS</b><small>RENEWABLE ENERGY OPERATIONS</small></div></div>
          <h1>Command every<br />megawatt, <em>live.</em></h1>
          <p>Unified monitoring, maintenance and analytics for solar, wind, hydro, tidal and battery assets — streamed in real time over RabbitMQ and WebSocket.</p>
          <ul className="hero-points">
            <li><Activity size={16} />Live telemetry &amp; threshold alerting</li>
            <li><ShieldCheck size={16} />JWT security with role-based access control</li>
            <li><Zap size={16} />Automatic work orders for critical faults</li>
          </ul>
        </div>
      </div>

      <div className="login-panel">
        <form className="login-card" onSubmit={submit}>
          <div className="eyebrow">SECURE ACCESS</div>
          <h2>{mode === 'login' ? 'Sign in to the console' : 'Create an operator account'}</h2>
          <p className="muted">{mode === 'login' ? 'Use your REOAGMS credentials.' : 'New accounts receive the Plant Operator role. An administrator can change it later.'}</p>

          {mode === 'register' && (
            <div className="row">
              <label className="field"><span>First name</span><div className="input-icon"><User size={16} /><input required value={firstName} onChange={(e) => setFirstName(e.target.value)} /></div></label>
              <label className="field"><span>Last name</span><div className="input-icon"><User size={16} /><input required value={lastName} onChange={(e) => setLastName(e.target.value)} /></div></label>
            </div>
          )}
          <label className="field"><span>Email</span><div className="input-icon"><Mail size={16} /><input required type="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@reoagms.com" /></div></label>
          <label className="field"><span>Password</span><div className="input-icon"><LockKeyhole size={16} /><input required type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" /></div></label>

          {error && <div className="form-error"><ShieldCheck size={16} />{error}</div>}
          <button className="btn primary lg full" disabled={busy}>{busy ? <Loader2 className="spin" size={18} /> : <>{mode === 'login' ? 'Sign in' : 'Create account'}<ArrowRight size={18} /></>}</button>
          <button type="button" className="link" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
            {mode === 'login' ? 'New here? Create an account' : 'Already registered? Sign in'}
          </button>

          {mode === 'login' && (
            <div className="demo-box">
              <div className="eyebrow">DEMO ACCOUNTS · click to fill</div>
              <div className="demo-grid">
                {DEMO.map((d) => (
                  <button type="button" key={d.email} onClick={() => { setEmail(d.email); setPassword(DEMO_PASSWORD); }}>
                    <b>{ROLE_LABEL[d.role]}</b><span>{d.email}</span>
                  </button>
                ))}
              </div>
              <small className="muted">Shared demo password: <code>{DEMO_PASSWORD}</code></small>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}

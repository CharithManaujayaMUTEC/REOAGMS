import { useMemo } from 'react';
import { Activity, AlertTriangle, Battery, Building2, Gauge, Radio, Wrench, Zap } from 'lucide-react';
import { Area, AreaChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api, get, parseTime, toApiTime } from '../api';
import { ago, num, power, severityTone, statusTone } from '../format';
import { useLive, usePoll, useRef_ } from '../store';
import { Badge, Empty, FacilityIcon, Kpi, Panel } from '../ui';

const SEV_COLOR: Record<string, string> = { CRITICAL: '#ff4d6d', HIGH: '#ff9f1c', MEDIUM: '#ffd60a', LOW: '#22d3ee' };
const tipStyle = { background: '#0a1426', border: '1px solid rgba(34,211,238,.35)', borderRadius: 10, color: '#e6f1ff', fontSize: 12 };

export default function Overview({ onNavigate }: { onNavigate: (page: string) => void }) {
  const ref = useRef_();
  const live = useLive();

  const summary = usePoll(() => api('/v1/dashboard/summary'), [live.tick], 8000);
  const alerts = usePoll(() => get('/v1/alerts'), [live.tick], 8000);
  const orders = usePoll(() => get('/v1/work-orders'), [live.tick], 12000);
  // Latest output: readings from the last minute, newest value per power sensor.
  const output = usePoll(async () => {
    const from = toApiTime(new Date(Date.now() - 90_000));
    return get(`/v1/sensor-readings?metricType=POWER&from=${from}`);
  }, [], 5000);
  // Energy trend per facility from the Analytics service, summed into a portfolio curve.
  const trend = usePoll(async () => {
    const results = await Promise.allSettled(ref.facilities.map((f) => api(`/v1/analytics/dashboard?facilityId=${f.id}`)));
    const buckets = new Map<string, number>();
    for (const r of results) {
      if (r.status !== 'fulfilled') continue;
      for (const p of r.value?.energyTrend ?? []) buckets.set(p.period, (buckets.get(p.period) ?? 0) + Number(p.value));
    }
    return [...buckets.entries()].sort(([a], [b]) => a.localeCompare(b)).slice(-24)
      .map(([period, value]) => ({ time: parseTime(period)!.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }), value: Math.round(value) }));
  }, [ref.facilities.length], 20000);

  const latestByAsset = useMemo(() => {
    const newest = new Map<string, any>();
    for (const r of output.data ?? []) {
      const prev = newest.get(r.sensorId);
      if (!prev || r.timestamp > prev.timestamp) newest.set(r.sensorId, r);
    }
    const byAsset = new Map<string, number>();
    for (const r of newest.values()) byAsset.set(r.assetId, (byAsset.get(r.assetId) ?? 0) + Number(r.value));
    return byAsset;
  }, [output.data]);

  const facilityOutput = (facilityId: string) =>
    ref.assets.filter((a) => a.facilityId === facilityId).reduce((sum, a) => sum + (latestByAsset.get(a.id) ?? 0), 0);
  const totalOutput = ref.facilities.reduce((sum, f) => sum + facilityOutput(f.id), 0);
  const capacity = ref.facilities.reduce((sum, f) => sum + Number(f.capacityMW || 0), 0);

  const s = summary.data ?? {};
  const openAlerts = (alerts.data ?? []).filter((a) => a.status === 'OPEN');
  const activeOrders = (orders.data ?? []).filter((o) => !['COMPLETED', 'CANCELLED'].includes(o.status));
  const onlineTotal = (s.onlineDevices ?? 0) + (s.offlineDevices ?? 0) + (s.warningDevices ?? 0);
  const health = onlineTotal ? Math.round(((s.onlineDevices ?? 0) / onlineTotal) * 100) : 100;
  const out = power(totalOutput);
  const severityData = Object.entries(s.alertsBySeverity ?? {}).map(([name, value]) => ({ name, value: Number(value) })).filter((d) => d.value > 0);
  const recent = [...(alerts.data ?? [])].sort((a, b) => String(b.timestamp).localeCompare(String(a.timestamp))).slice(0, 7);

  return (
    <div className="page">
      <div className="kpi-grid">
        <Kpi label="Live generation" value={out.value} unit={out.unit} icon={<Zap size={18} />} tone="volt" hint={`${num((totalOutput / 1000 / (capacity || 1)) * 100, 0)}% of ${num(capacity, 0)} MW installed`} />
        <Kpi label="Open alerts" value={s.openAlerts ?? openAlerts.length} icon={<AlertTriangle size={18} />} tone={(s.openAlerts ?? 0) > 0 ? 'red' : 'green'} hint={`${s.acknowledgedAlerts ?? 0} acknowledged · ${s.resolvedAlerts ?? 0} resolved`} />
        <Kpi label="Grid assets online" value={`${health}%`} icon={<Radio size={18} />} tone="green" hint={`${s.onlineDevices ?? 0} online · ${s.warningDevices ?? 0} warning · ${s.offlineDevices ?? 0} offline`} />
        <Kpi label="Active work orders" value={activeOrders.length} icon={<Wrench size={18} />} tone="violet" hint={`${(orders.data ?? []).filter((o) => o.status === 'CREATED').length} awaiting assignment`} />
        <Kpi label="Readings (24h)" value={num(s.totalReadingsLast24h, 0)} icon={<Activity size={18} />} tone="cyan" hint="Telemetry ingested via RabbitMQ pipeline" />
      </div>

      <div className="grid-main">
        <Panel title="Portfolio energy output" subtitle="Hourly generation across all facilities · Analytics Service" className="span-2"
          actions={<Badge tone="cyan" dot>kWh / hour</Badge>}>
          <div className="chart-lg">
            {trend.data?.length ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={trend.data} margin={{ top: 10, right: 12, left: -8, bottom: 0 }}>
                  <defs><linearGradient id="gen" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#ffd60a" stopOpacity={0.55} /><stop offset="100%" stopColor="#ffd60a" stopOpacity={0} /></linearGradient></defs>
                  <CartesianGrid stroke="rgba(120,160,220,.1)" vertical={false} />
                  <XAxis dataKey="time" tick={{ fill: '#7f93b2', fontSize: 11 }} tickLine={false} axisLine={false} minTickGap={24} />
                  <YAxis tick={{ fill: '#7f93b2', fontSize: 11 }} tickLine={false} axisLine={false} width={56} tickFormatter={(v) => (v >= 1000 ? `${v / 1000}k` : v)} />
                  <Tooltip contentStyle={tipStyle} formatter={(v) => [`${num(Number(v), 0)} kWh`, 'Energy']} />
                  <Area type="monotone" dataKey="value" stroke="#ffd60a" strokeWidth={2.2} fill="url(#gen)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : <Empty title="Waiting for generation data" hint="Start the sensor simulator to stream telemetry." />}
          </div>
        </Panel>

        <Panel title="Alert severity" subtitle="All recorded alerts">
          <div className="chart-md">
            {severityData.length ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={severityData} dataKey="value" nameKey="name" innerRadius={52} outerRadius={78} paddingAngle={3} stroke="none">
                    {severityData.map((d) => <Cell key={d.name} fill={SEV_COLOR[d.name]} />)}
                  </Pie>
                  <Tooltip contentStyle={tipStyle} />
                </PieChart>
              </ResponsiveContainer>
            ) : <Empty title="No alerts" />}
          </div>
          <div className="legend">{severityData.map((d) => <span key={d.name}><i style={{ background: SEV_COLOR[d.name] }} />{d.name} <b>{d.value}</b></span>)}</div>
        </Panel>
      </div>

      <div className="grid-main">
        <Panel title="Facilities" subtitle="Live output per site" className="span-2" actions={<button className="link" onClick={() => onNavigate('assets')}>Manage →</button>}>
          <div className="facility-grid">
            {ref.facilities.map((f) => {
              const o = power(facilityOutput(f.id));
              const siteAlerts = openAlerts.filter((a) => a.facilityId === f.id).length;
              const pct = Math.min(100, (facilityOutput(f.id) / 1000 / Number(f.capacityMW || 1)) * 100);
              return (
                <div key={f.id} className="facility-card">
                  <div className="facility-top"><span className="facility-icon"><FacilityIcon type={f.type} /></span><div><b>{f.name}</b><small>{f.province}, {f.country}</small></div>
                    {siteAlerts ? <Badge tone="red" dot>{siteAlerts} alert{siteAlerts > 1 ? 's' : ''}</Badge> : <Badge tone="green" dot>Nominal</Badge>}</div>
                  <div className="facility-output"><b>{o.value}</b><span>{o.unit}</span><small>of {num(f.capacityMW, 0)} MW</small></div>
                  <div className="meter"><i style={{ width: `${pct}%` }} /></div>
                </div>
              );
            })}
            {!ref.facilities.length && <Empty title="No facilities registered" />}
          </div>
        </Panel>

        <Panel title="Live alert feed" subtitle={live.connected ? 'WebSocket · STOMP connected' : 'Connecting…'}
          actions={<Badge tone={live.connected ? 'green' : 'muted'} dot>{live.connected ? 'LIVE' : 'OFFLINE'}</Badge>}>
          <ul className="feed">
            {live.alerts.slice(0, 4).map((a) => (
              <li key={a.alertId + a.receivedAt} className="fresh">
                <Badge tone={severityTone(a.severity)}>{a.severity}</Badge>
                <div><b>{ref.assetName(a.assetId)}</b><p>{a.message}</p></div><time>now</time>
              </li>
            ))}
            {recent.slice(0, Math.max(0, 7 - Math.min(4, live.alerts.length))).map((a) => (
              <li key={a.id}>
                <Badge tone={severityTone(a.severity)}>{a.severity}</Badge>
                <div><b>{ref.assetName(a.assetId)}</b><p>{a.message}</p></div><time>{ago(a.timestamp)}</time>
              </li>
            ))}
            {!recent.length && !live.alerts.length && <Empty title="All systems nominal" />}
          </ul>
          <button className="btn ghost sm full" onClick={() => onNavigate('alerts')}>Open alert console</button>
        </Panel>
      </div>

      <Panel title="Grid status" subtitle="Asset distribution by operational state" actions={<Gauge size={16} />}>
        <div className="status-row">
          {['ACTIVE', 'MAINTENANCE', 'FAULT', 'INACTIVE'].map((st) => (
            <div key={st} className="status-tile"><Badge tone={statusTone(st)} dot>{st}</Badge><b>{ref.assets.filter((a) => a.status === st).length}</b><small>assets</small></div>
          ))}
          <div className="status-tile"><Badge tone="cyan"><Building2 size={12} /> Facilities</Badge><b>{ref.facilities.length}</b><small>sites</small></div>
          <div className="status-tile"><Badge tone="violet"><Battery size={12} /> Sensors</Badge><b>{ref.sensors.length}</b><small>registered</small></div>
        </div>
      </Panel>
    </div>
  );
}

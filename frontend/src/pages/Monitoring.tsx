import { useEffect, useMemo, useState } from 'react';
import { BatteryMedium, Signal } from 'lucide-react';
import { CartesianGrid, Line, LineChart, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { get, parseTime, toApiTime } from '../api';
import { ago, label, num, statusTone } from '../format';
import { useRef_, usePoll } from '../store';
import { Badge, DataTable, Empty, Panel, Spinner } from '../ui';

const METRIC_COLOR: Record<string, string> = {
  POWER: '#ffd60a', TEMPERATURE: '#ff7a59', VOLTAGE: '#22d3ee', CURRENT: '#8b5cf6', WIND_SPEED: '#34f5a0',
  SOLAR_IRRADIANCE: '#ffb703', WATER_FLOW: '#4cc9f0', PRESSURE: '#c77dff', HUMIDITY: '#90e0ef',
};
const WINDOWS = [{ label: '5 min', ms: 5 * 60_000 }, { label: '15 min', ms: 15 * 60_000 }, { label: '1 hour', ms: 3600_000 }, { label: '24 hours', ms: 24 * 3600_000 }];
const tipStyle = { background: '#0a1426', border: '1px solid rgba(34,211,238,.35)', borderRadius: 10, color: '#e6f1ff', fontSize: 12 };

export default function Monitoring() {
  const ref = useRef_();
  const [assetId, setAssetId] = useState('');
  const [windowMs, setWindowMs] = useState(WINDOWS[1].ms);
  useEffect(() => { if (!assetId && ref.assets.length) setAssetId(ref.assets[0].id); }, [ref.assets, assetId]);

  const readings = usePoll(async () => {
    if (!assetId) return [];
    return get(`/v1/sensor-readings?assetId=${assetId}&from=${toApiTime(new Date(Date.now() - windowMs))}`);
  }, [assetId, windowMs], 5000);
  const telemetry = usePoll(() => get('/v1/telemetry'), [], 8000);

  const sensors = ref.sensors.filter((s) => s.assetId === assetId);
  const bySensor = useMemo(() => {
    const m = new Map<string, any[]>();
    for (const r of readings.data ?? []) { const l = m.get(r.sensorId) ?? []; l.push(r); m.set(r.sensorId, l); }
    for (const l of m.values()) l.sort((a, b) => String(a.timestamp).localeCompare(String(b.timestamp)));
    return m;
  }, [readings.data]);

  const latestDevice = useMemo(() => {
    const m = new Map<string, any>();
    for (const t of telemetry.data ?? []) { const p = m.get(t.assetId); if (!p || t.lastSeenAt > p.lastSeenAt) m.set(t.assetId, t); }
    return [...m.values()].sort((a, b) => ref.assetName(a.assetId).localeCompare(ref.assetName(b.assetId)));
  }, [telemetry.data, ref]);

  const asset = ref.assets.find((a) => a.id === assetId);

  return (
    <div className="page">
      <Panel
        title="Live telemetry" subtitle={asset ? `${asset.name} · ${ref.facilityName(asset.facilityId)}` : 'Select an asset'}
        actions={<>
          <select className="select" value={assetId} onChange={(e) => setAssetId(e.target.value)}>
            {ref.facilities.map((f) => (
              <optgroup key={f.id} label={f.name}>
                {ref.assets.filter((a) => a.facilityId === f.id).map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
              </optgroup>
            ))}
          </select>
          <select className="select" value={windowMs} onChange={(e) => setWindowMs(Number(e.target.value))}>
            {WINDOWS.map((w) => <option key={w.ms} value={w.ms}>Last {w.label}</option>)}
          </select>
          <Badge tone="green" dot>Auto-refresh 5s</Badge>
        </>}
      >
        {readings.loading && !readings.data ? <Spinner label="Reading sensors" /> : !sensors.length ? <Empty title="No sensors on this asset" /> : (
          <div className="sensor-grid">
            {sensors.map((s) => {
              const series = bySensor.get(s.id) ?? [];
              const last = series[series.length - 1];
              const color = METRIC_COLOR[s.type] || '#22d3ee';
              const data = series.map((r) => ({ t: parseTime(r.timestamp)!.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }), v: Number(r.value) }));
              return (
                <div key={s.id} className="sensor-card">
                  <div className="sensor-head"><div><small>{label(s.type)}</small><b>{s.name.split(' · ')[1] ?? s.name}</b></div>
                    <Badge tone={statusTone(s.status)} dot>{label(s.status)}</Badge></div>
                  <div className="sensor-value" style={{ color }}>{last ? num(last.value, 1) : '—'}<small>{s.unit}</small></div>
                  <div className="sensor-time">{last ? `Updated ${ago(last.timestamp)}` : 'No data in window'}</div>
                  <div className="chart-sm">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={data} margin={{ top: 6, right: 6, left: -22, bottom: 0 }}>
                        <CartesianGrid stroke="rgba(120,160,220,.08)" vertical={false} />
                        <XAxis dataKey="t" hide />
                        <YAxis tick={{ fill: '#7f93b2', fontSize: 10 }} tickLine={false} axisLine={false} domain={['auto', 'auto']} width={46} />
                        <Tooltip contentStyle={tipStyle} formatter={(v) => [`${num(Number(v), 2)} ${s.unit}`, label(s.type)]} />
                        {s.maxThreshold != null && <ReferenceLine y={s.maxThreshold} stroke="#ff4d6d" strokeDasharray="4 4" />}
                        <Line type="monotone" dataKey="v" stroke={color} strokeWidth={2} dot={false} isAnimationActive={false} />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </Panel>

      <Panel title="Device heartbeat" subtitle="Latest telemetry from every connected asset">
        <DataTable rows={latestDevice} empty="No telemetry received yet" columns={[
          { key: 'asset', header: 'Asset', render: (r) => <div><b>{ref.assetName(r.assetId)}</b><small className="sub">{ref.facilityName(r.facilityId)}</small></div> },
          { key: 'status', header: 'Status', render: (r) => <Badge tone={statusTone(r.status)} dot>{label(r.status)}</Badge> },
          { key: 'battery', header: 'Battery', render: (r) => <span className="inline-metric"><BatteryMedium size={14} />{num(r.batteryLevel, 0)}%</span> },
          { key: 'signal', header: 'Signal', render: (r) => <span className="inline-metric"><Signal size={14} />{num(r.signalStrength, 0)} dBm</span> },
          { key: 'seen', header: 'Last seen', render: (r) => <span className="mono muted">{ago(r.lastSeenAt)}</span> },
        ]} />
      </Panel>
    </div>
  );
}

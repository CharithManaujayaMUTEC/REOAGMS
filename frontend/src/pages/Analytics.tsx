import { useEffect, useState } from 'react';
import { Download, FileBarChart2, Gauge, Leaf, TimerReset, Wrench, Zap } from 'lucide-react';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api, download, get, parseTime, post, toApiTime } from '../api';
import { can, dateTime, label, num, power } from '../format';
import { useAuth, usePoll, useRef_ } from '../store';
import { Badge, DataTable, FormModal, Kpi, Panel, Spinner, useAction } from '../ui';

const tipStyle = { background: '#0a1426', border: '1px solid rgba(34,211,238,.35)', borderRadius: 10, color: '#e6f1ff', fontSize: 12 };
const RANGES = [{ label: '24 hours', days: 1, group: 'HOUR' }, { label: '7 days', days: 7, group: 'DAY' }, { label: '30 days', days: 30, group: 'DAY' }];

export default function Analytics() {
  const { user } = useAuth();
  const ref = useRef_();
  const run = useAction();
  const [facilityId, setFacilityId] = useState('');
  const [range, setRange] = useState(RANGES[0]);
  const [generating, setGenerating] = useState<'energy' | 'performance' | null>(null);
  useEffect(() => { if (!facilityId && ref.facilities.length) setFacilityId(ref.facilities[0].id); }, [ref.facilities, facilityId]);

  const from = () => toApiTime(new Date(Date.now() - range.days * 86400_000));
  const dash = usePoll(() => facilityId ? api(`/v1/analytics/dashboard?facilityId=${facilityId}&from=${from()}`) : Promise.resolve(null), [facilityId, range], 30000);
  const stats = usePoll(() => facilityId ? api(`/v1/analytics/statistics?facilityId=${facilityId}&from=${from()}&groupBy=${range.group}`) : Promise.resolve(null), [facilityId, range], 30000);
  const energy = usePoll(() => get('/v1/energy-reports'), []);
  const perf = usePoll(() => get('/v1/performance-reports'), []);

  const d = dash.data;
  const total = power(Number(d?.totalEnergy ?? 0), 'kWh');
  const peak = power(Number(d?.peakEnergy ?? 0), 'kWh');
  const series = (stats.data?.series ?? []).map((p: any) => {
    const t = parseTime(p.period)!;
    return { label: range.group === 'HOUR' ? t.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : t.toLocaleDateString([], { month: 'short', day: 'numeric' }), value: Math.round(Number(p.value)) };
  });
  const canGenerate = can.manage(user?.role);

  const exportCsv = (kind: 'energy-reports' | 'performance-reports', r: any) =>
    run(() => download(`/v1/${kind}/${r.id}/export?format=CSV`, `${r.name.replace(/\W+/g, '_')}.csv`), 'Report exported');

  return (
    <div className="page">
      <Panel title="Performance analytics" subtitle="Energy, availability and efficiency computed by the Analytics Service from live telemetry"
        actions={<>
          <select className="select" value={facilityId} onChange={(e) => setFacilityId(e.target.value)}>
            {ref.facilities.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
          </select>
          <select className="select" value={range.label} onChange={(e) => setRange(RANGES.find((r) => r.label === e.target.value)!)}>
            {RANGES.map((r) => <option key={r.label}>{r.label}</option>)}
          </select>
        </>}>
        {dash.loading && !d ? <Spinner label="Calculating KPIs" /> : (
          <div className="kpi-grid">
            <Kpi label="Total energy" value={total.value} unit={total.unit} icon={<Zap size={18} />} tone="volt" hint={`Peak hour ${peak.value} ${peak.unit}`} />
            <Kpi label="Availability" value={`${num(d?.availabilityPercentage, 1)}%`} icon={<TimerReset size={18} />} tone="green" hint="Share of time online" />
            <Kpi label="Efficiency" value={`${num(d?.efficiencyPercentage, 1)}%`} icon={<Gauge size={18} />} tone="cyan" hint={`Capacity factor ${num(d?.capacityFactorPercentage, 1)}%`} />
            <Kpi label="Utilisation" value={`${num(d?.utilizationPercentage, 1)}%`} icon={<Leaf size={18} />} tone="violet" hint="Of rated capacity" />
            <Kpi label="Maintenance" value={`${num(d?.maintenanceCompletionRate, 0)}%`} icon={<Wrench size={18} />} tone="orange" hint={`${d?.openWorkOrderCount ?? 0} open · ${d?.activeAlertCount ?? 0} active alerts`} />
          </div>
        )}
      </Panel>

      <Panel title="Energy production" subtitle={`${label(range.group)}ly totals · ${ref.facilityName(facilityId)}`} actions={<Badge tone="cyan" dot>kWh</Badge>}>
        <div className="chart-lg">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={series} margin={{ top: 10, right: 8, left: -6, bottom: 0 }}>
              <defs><linearGradient id="bar" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#22d3ee" /><stop offset="100%" stopColor="#0e7490" /></linearGradient></defs>
              <CartesianGrid stroke="rgba(120,160,220,.1)" vertical={false} />
              <XAxis dataKey="label" tick={{ fill: '#7f93b2', fontSize: 11 }} tickLine={false} axisLine={false} minTickGap={18} />
              <YAxis tick={{ fill: '#7f93b2', fontSize: 11 }} tickLine={false} axisLine={false} width={58} tickFormatter={(v) => (v >= 1000 ? `${v / 1000}k` : v)} />
              <Tooltip contentStyle={tipStyle} cursor={{ fill: 'rgba(34,211,238,.08)' }} formatter={(v) => [`${num(Number(v), 0)} kWh`, 'Energy']} />
              <Bar dataKey="value" fill="url(#bar)" radius={[5, 5, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Panel>

      <div className="grid-two">
        <Panel title="Energy reports" subtitle="Saved reports with CSV export" actions={canGenerate && <button className="btn primary sm" onClick={() => setGenerating('energy')}><FileBarChart2 size={15} />Generate</button>}>
          <DataTable rows={[...(energy.data ?? [])].sort((a, b) => String(b.generatedAt).localeCompare(String(a.generatedAt))).slice(0, 8)} empty="No energy reports yet" columns={[
            { key: 'name', header: 'Report', render: (r) => <div><b>{r.name}</b><small className="sub">{ref.facilityName(r.facilityId)} · {dateTime(r.generatedAt)}</small></div> },
            { key: 'total', header: 'Total', render: (r) => { const p = power(Number(r.totalEnergy), 'kWh'); return <span className="mono">{p.value} {p.unit}</span>; } },
            { key: 'var', header: 'Variance', render: (r) => r.variancePercentage == null ? '—' : <span className="mono">{num(r.variancePercentage, 1)}%</span> },
            { key: 'x', header: '', align: 'right', render: (r) => <button className="icon-btn" title="Export CSV" onClick={() => exportCsv('energy-reports', r)}><Download size={15} /></button> },
          ]} />
        </Panel>
        <Panel title="Performance reports" subtitle="Availability, efficiency and downtime" actions={canGenerate && <button className="btn primary sm" onClick={() => setGenerating('performance')}><FileBarChart2 size={15} />Generate</button>}>
          <DataTable rows={[...(perf.data ?? [])].sort((a, b) => String(b.generatedAt).localeCompare(String(a.generatedAt))).slice(0, 8)} empty="No performance reports yet" columns={[
            { key: 'name', header: 'Report', render: (r) => <div><b>{r.name}</b><small className="sub">{label(r.scopeType)} · {dateTime(r.generatedAt)}</small></div> },
            { key: 'avail', header: 'Availability', render: (r) => <span className="mono">{num(r.availabilityPercentage, 1)}%</span> },
            { key: 'eff', header: 'Efficiency', render: (r) => <span className="mono">{num(r.efficiencyPercentage, 1)}%</span> },
            { key: 'x', header: '', align: 'right', render: (r) => <button className="icon-btn" title="Export CSV" onClick={() => exportCsv('performance-reports', r)}><Download size={15} /></button> },
          ]} />
        </Panel>
      </div>

      {generating && (
        <FormModal title={generating === 'energy' ? 'Generate energy report' : 'Generate performance report'} submitLabel="Generate" onClose={() => setGenerating(null)}
          fields={[
            { name: 'name', label: 'Report name', required: true, defaultValue: `${ref.facilityName(facilityId)} — ${range.label}` },
            { name: 'facilityId', label: 'Facility', type: 'select', required: true, defaultValue: facilityId, options: ref.facilities.map((f) => ({ value: f.id, label: f.name })) },
            { name: 'days', label: 'Look-back (days)', type: 'number', required: true, defaultValue: range.days },
          ]}
          onSubmit={async (v) => {
            const end = new Date(); const start = new Date(end.getTime() - Number(v.days) * 86400_000);
            const base = { name: v.name, periodStart: toApiTime(start), periodEnd: toApiTime(end) };
            if (generating === 'energy') { await post('/v1/energy-reports', { ...base, facilityId: v.facilityId }); void energy.reload(); }
            else { await post('/v1/performance-reports', { ...base, scopeType: 'FACILITY', scopeId: v.facilityId }); void perf.reload(); }
          }} />
      )}
    </div>
  );
}

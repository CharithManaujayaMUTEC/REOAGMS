import { useState } from 'react';
import { Check, CheckCheck, Plus, Trash2 } from 'lucide-react';
import { del, get, post, put } from '../api';
import { ago, can, label, num, severityTone, statusTone } from '../format';
import { useAuth, useLive, usePoll, useRef_ } from '../store';
import { Badge, DataTable, FormModal, Panel, Tabs, useAction, type Column } from '../ui';

const opts = (values: string[]) => values.map((v) => ({ value: v, label: label(v) }));
const METRICS = ['TEMPERATURE', 'VOLTAGE', 'CURRENT', 'POWER', 'WIND_SPEED', 'SOLAR_IRRADIANCE', 'WATER_FLOW', 'HUMIDITY', 'PRESSURE'];
const OPERATORS = [
  { value: 'GREATER_THAN', label: '> greater than' }, { value: 'GREATER_THAN_OR_EQUAL', label: '≥ greater or equal' },
  { value: 'LESS_THAN', label: '< less than' }, { value: 'LESS_THAN_OR_EQUAL', label: '≤ less or equal' }, { value: 'EQUAL', label: '= equal' },
];
const SYMBOL: Record<string, string> = { GREATER_THAN: '>', GREATER_THAN_OR_EQUAL: '≥', LESS_THAN: '<', LESS_THAN_OR_EQUAL: '≤', EQUAL: '=' };

type Tab = 'alerts' | 'rules';

export default function Alerts() {
  const { user } = useAuth();
  const ref = useRef_();
  const live = useLive();
  const run = useAction();
  const [tab, setTab] = useState<Tab>('alerts');
  const [status, setStatus] = useState('');
  const [severity, setSeverity] = useState('');
  const [creating, setCreating] = useState(false);

  const alerts = usePoll(() => get('/v1/alerts'), [live.tick], 6000);
  const rules = usePoll(() => get('/v1/alert-rules'), [], 20000);

  const canOperate = can.operate(user?.role);
  const canManage = can.manage(user?.role);
  const rows = [...(alerts.data ?? [])]
    .filter((a) => (!status || a.status === status) && (!severity || a.severity === severity))
    .sort((a, b) => String(b.timestamp).localeCompare(String(a.timestamp)));

  const transition = async (id: string, action: 'acknowledge' | 'resolve') => {
    if (await run(() => put(`/v1/alerts/${id}/${action}`), action === 'acknowledge' ? 'Alert acknowledged' : 'Alert resolved')) void alerts.reload();
  };

  const alertCols: Column<any>[] = [
    { key: 'severity', header: 'Severity', render: (r) => <Badge tone={severityTone(r.severity)} dot>{r.severity}</Badge> },
    { key: 'asset', header: 'Asset', render: (r) => <div><b>{ref.assetName(r.assetId)}</b><small className="sub">{ref.facilityName(r.facilityId)}</small></div> },
    { key: 'message', header: 'Condition', render: (r) => <span className="wrap">{r.message}</span> },
    { key: 'value', header: 'Value / limit', render: (r) => <span className="mono">{num(r.value, 1)} <span className="muted">/ {num(r.threshold, 1)}</span></span> },
    { key: 'status', header: 'Status', render: (r) => <Badge tone={statusTone(r.status)}>{label(r.status)}</Badge> },
    { key: 'time', header: 'Raised', render: (r) => <span className="mono muted">{ago(r.timestamp)}</span> },
    {
      key: 'actions', header: '', align: 'right', render: (r) => canOperate && r.status !== 'RESOLVED' ? (
        <div className="row-actions">
          {r.status === 'OPEN' && <button className="btn ghost sm" onClick={() => transition(r.id, 'acknowledge')}><Check size={14} />Acknowledge</button>}
          <button className="btn ghost sm" onClick={() => transition(r.id, 'resolve')}><CheckCheck size={14} />Resolve</button>
        </div>
      ) : null,
    },
  ];

  const ruleCols: Column<any>[] = [
    { key: 'name', header: 'Rule', render: (r) => <div><b>{r.name}</b><small className="sub">{r.description}</small></div> },
    { key: 'cond', header: 'Condition', render: (r) => <span className="mono">{label(r.metricType)} {SYMBOL[r.operator]} {num(r.threshold, 1)}</span> },
    { key: 'scope', header: 'Scope', render: (r) => r.assetId ? ref.assetName(r.assetId) : <span className="muted">All assets</span> },
    { key: 'severity', header: 'Severity', render: (r) => <Badge tone={severityTone(r.severity)}>{r.severity}</Badge> },
    { key: 'enabled', header: 'State', render: (r) => <Badge tone={r.enabled ? 'green' : 'muted'} dot>{r.enabled ? 'Enabled' : 'Disabled'}</Badge> },
    {
      key: 'actions', header: '', align: 'right', render: (r) => canManage ? (
        <button className="icon-btn danger" title="Delete rule" onClick={async () => {
          if (window.confirm(`Delete rule "${r.name}"?`) && await run(() => del(`/v1/alert-rules/${r.id}`), 'Rule deleted')) void rules.reload();
        }}><Trash2 size={15} /></button>
      ) : null,
    },
  ];

  const count = (s: string) => (alerts.data ?? []).filter((a) => a.status === s).length;

  return (
    <div className="page">
      <Panel
        title="Alert console" subtitle="Threshold breaches detected by the Monitoring Service · pushed live over STOMP"
        actions={tab === 'alerts' ? <>
          <select className="select" value={status} onChange={(e) => setStatus(e.target.value)}><option value="">All statuses</option>{opts(['OPEN', 'ACKNOWLEDGED', 'RESOLVED']).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}</select>
          <select className="select" value={severity} onChange={(e) => setSeverity(e.target.value)}><option value="">All severities</option>{opts(['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}</select>
        </> : canManage ? <button className="btn primary" onClick={() => setCreating(true)}><Plus size={16} />New rule</button> : null}
      >
        <Tabs<Tab> active={tab} onChange={setTab} tabs={[
          { id: 'alerts', label: 'Alerts', count: count('OPEN') },
          { id: 'rules', label: 'Detection rules', count: (rules.data ?? []).length },
        ]} />
        {tab === 'alerts'
          ? <DataTable columns={alertCols} rows={rows} empty="No alerts match the current filters" />
          : <DataTable columns={ruleCols} rows={rules.data ?? []} empty="No rules configured" />}
      </Panel>

      {creating && (
        <FormModal title="New detection rule" subtitle="Evaluated against every incoming sensor reading" submitLabel="Create rule"
          onClose={() => setCreating(false)}
          fields={[
            { name: 'name', label: 'Rule name', required: true },
            { name: 'metricType', label: 'Metric', type: 'select', options: opts(METRICS), required: true, half: true },
            { name: 'operator', label: 'Operator', type: 'select', options: OPERATORS, required: true, half: true, defaultValue: 'GREATER_THAN' },
            { name: 'threshold', label: 'Threshold', type: 'number', step: 'any', required: true, half: true },
            { name: 'severity', label: 'Severity', type: 'select', options: opts(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']), required: true, half: true, defaultValue: 'HIGH' },
            { name: 'assetId', label: 'Limit to asset (optional)', type: 'select', options: ref.assets.map((a) => ({ value: a.id, label: a.name })) },
            { name: 'description', label: 'Description', type: 'textarea' },
          ]}
          onSubmit={async (v) => { await post('/v1/alert-rules', { ...v, enabled: true }); void rules.reload(); }} />
      )}
    </div>
  );
}

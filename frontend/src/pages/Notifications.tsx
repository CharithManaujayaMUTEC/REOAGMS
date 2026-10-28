import { RotateCw } from 'lucide-react';
import { get, post } from '../api';
import { ago, can, label, statusTone } from '../format';
import { useAuth, useLive, usePoll } from '../store';
import { Badge, DataTable, Kpi, Panel, useAction } from '../ui';
import { CheckCircle2, Clock, Mail, XCircle } from 'lucide-react';

export default function Notifications() {
  const { user } = useAuth();
  const live = useLive();
  const run = useAction();
  const list = usePoll(() => get('/v1/notifications'), [live.tick], 8000);
  const rows = [...(list.data ?? [])].sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt)));
  const count = (s: string) => rows.filter((n) => n.status === s).length;

  return (
    <div className="page">
      <div className="kpi-grid four">
        <Kpi label="Total notifications" value={rows.length} icon={<Mail size={18} />} tone="cyan" hint="Created from RabbitMQ alert events" />
        <Kpi label="Delivered" value={count('SENT')} icon={<CheckCircle2 size={18} />} tone="green" />
        <Kpi label="Pending / retrying" value={count('PENDING') + count('RETRYING')} icon={<Clock size={18} />} tone="volt" />
        <Kpi label="Failed" value={count('FAILED')} icon={<XCircle size={18} />} tone="red" hint="Configure SMTP credentials to deliver email" />
      </div>
      <Panel title="Notification history" subtitle="Email, SMS and push messages dispatched by the Notification Service">
        <DataTable rows={rows.slice(0, 100)} empty="No notifications yet" columns={[
          { key: 'status', header: 'Status', render: (r) => <Badge tone={statusTone(r.status)} dot>{label(r.status)}</Badge> },
          { key: 'channel', header: 'Channel', render: (r) => label(r.channel) },
          { key: 'subject', header: 'Subject', render: (r) => <div><b>{r.subject}</b><small className="sub wrap">{r.message}</small></div> },
          { key: 'recipient', header: 'Recipient', render: (r) => <span className="mono muted">{r.recipient}</span> },
          { key: 'retry', header: 'Retries', render: (r) => r.retryCount },
          { key: 'time', header: 'Created', render: (r) => <span className="mono muted">{ago(r.createdAt)}</span> },
          {
            key: 'x', header: '', align: 'right', render: (r) => r.status === 'FAILED' && can.manage(user?.role) ? (
              <button className="btn ghost sm" onClick={async () => { if (await run(() => post(`/v1/notifications/${r.id}/retry`), 'Retry queued')) void list.reload(); }}><RotateCw size={13} />Retry</button>
            ) : null,
          },
        ]} />
      </Panel>
    </div>
  );
}

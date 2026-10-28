import { useState } from 'react';
import { CheckCircle2, Play, Plus, UserPlus, Wrench } from 'lucide-react';
import { get, post, put } from '../api';
import { ago, can, dateTime, label, statusTone } from '../format';
import { useAuth, usePoll, useRef_ } from '../store';
import { Badge, DataTable, Empty, FormModal, Modal, Panel, useAction } from '../ui';

const COLUMNS = [
  { id: 'CREATED', title: 'Created', hint: 'Awaiting assignment' },
  { id: 'ASSIGNED', title: 'Assigned', hint: 'Technician allocated' },
  { id: 'IN_PROGRESS', title: 'In progress', hint: 'Work underway' },
  { id: 'COMPLETED', title: 'Completed', hint: 'Closed out' },
];

export default function Maintenance() {
  const { user } = useAuth();
  const ref = useRef_();
  const run = useAction();
  const canWork = can.maintain(user?.role);
  const canManage = can.manage(user?.role);
  const [creating, setCreating] = useState(false);
  const [assigning, setAssigning] = useState<any>(null);
  const [techId, setTechId] = useState('');

  const orders = usePoll(() => get('/v1/work-orders'), [], 6000);
  const techs = usePoll(() => get('/v1/technicians'), [], 15000);
  const techName = (id?: string) => (techs.data ?? []).find((t) => t.id === id)?.name;

  const act = async (id: string, action: string, ok: string) => {
    if (await run(() => put(`/v1/work-orders/${id}/${action}`), ok)) void orders.reload();
  };
  const list = (status: string) => [...(orders.data ?? [])].filter((o) => o.status === status)
    .sort((a, b) => String(b.scheduledAt).localeCompare(String(a.scheduledAt))).slice(0, status === 'COMPLETED' ? 8 : 50);

  return (
    <div className="page">
      <Panel
        title="Work orders" subtitle="Corrective orders are opened automatically for HIGH and CRITICAL alerts"
        actions={canWork && <button className="btn primary" onClick={() => setCreating(true)}><Plus size={16} />New work order</button>}
      >
        <div className="kanban">
          {COLUMNS.map((col) => {
            const items = list(col.id);
            return (
              <div key={col.id} className="kanban-col">
                <header><Badge tone={statusTone(col.id)} dot>{col.title}</Badge><span>{(orders.data ?? []).filter((o) => o.status === col.id).length}</span></header>
                <small className="muted">{col.hint}</small>
                <div className="kanban-list">
                  {items.map((o) => (
                    <article key={o.id} className="wo-card">
                      <div className="wo-top"><Badge tone={o.maintenanceType === 'CORRECTIVE' ? 'orange' : 'cyan'}>{label(o.maintenanceType)}</Badge><time>{ago(o.scheduledAt)}</time></div>
                      <b>{o.title}</b>
                      <p className="wo-asset"><Wrench size={13} />{ref.assetName(o.assetId)}</p>
                      <p className="wo-desc">{(o.description || '').replace(/\s*\[alert:[^\]]+\]/, '')}</p>
                      <div className="wo-foot">
                        <span className="tech">{techName(o.technicianId) ?? <em>Unassigned</em>}</span>
                        {canWork && (
                          <div className="row-actions">
                            {(o.status === 'CREATED' || o.status === 'ASSIGNED') && <button className="btn ghost sm" onClick={() => { setAssigning(o); setTechId(o.technicianId ?? ''); }}><UserPlus size={13} />Assign</button>}
                            {o.status === 'ASSIGNED' && <button className="btn ghost sm" onClick={() => act(o.id, 'start', 'Work started')}><Play size={13} />Start</button>}
                            {o.status === 'IN_PROGRESS' && <button className="btn primary sm" onClick={() => act(o.id, 'complete', 'Work order completed')}><CheckCircle2 size={13} />Complete</button>}
                          </div>
                        )}
                      </div>
                    </article>
                  ))}
                  {!items.length && <Empty title="Empty" />}
                </div>
              </div>
            );
          })}
        </div>
      </Panel>

      <Panel title="Technicians" subtitle="Field crew and availability" actions={canManage && <TechButton onDone={() => techs.reload()} />}>
        <DataTable rows={techs.data ?? []} empty="No technicians registered" columns={[
          { key: 'name', header: 'Technician', render: (r) => <b>{r.name}</b> },
          { key: 'specialization', header: 'Specialization' },
          { key: 'contact', header: 'Contact', render: (r) => <span className="mono muted">{r.phone} · {r.email}</span> },
          { key: 'availability', header: 'Availability', render: (r) => <Badge tone={statusTone(r.availability)} dot>{label(r.availability)}</Badge> },
          { key: 'load', header: 'Open orders', render: (r) => (orders.data ?? []).filter((o) => o.technicianId === r.id && !['COMPLETED', 'CANCELLED'].includes(o.status)).length },
        ]} />
      </Panel>

      {creating && (
        <FormModal title="New work order" subtitle="Preventive or corrective maintenance" submitLabel="Create" onClose={() => setCreating(false)}
          fields={[
            { name: 'title', label: 'Title', required: true },
            { name: 'assetId', label: 'Asset', type: 'select', required: true, options: ref.assets.map((a) => ({ value: a.id, label: `${a.name} — ${ref.facilityName(a.facilityId)}` })) },
            { name: 'maintenanceType', label: 'Type', type: 'select', required: true, half: true, defaultValue: 'PREVENTIVE', options: [{ value: 'PREVENTIVE', label: 'Preventive' }, { value: 'CORRECTIVE', label: 'Corrective' }] },
            { name: 'technicianId', label: 'Technician (optional)', type: 'select', half: true, options: (techs.data ?? []).map((t) => ({ value: t.id, label: t.name })) },
            { name: 'scheduledAt', label: 'Scheduled for', type: 'datetime-local' },
            { name: 'description', label: 'Description', type: 'textarea' },
          ]}
          onSubmit={async (v) => { await post('/v1/work-orders', { ...v, scheduledAt: v.scheduledAt ? `${v.scheduledAt}:00` : null }); void orders.reload(); }} />
      )}

      {assigning && (
        <Modal title="Assign technician" subtitle={assigning.title} onClose={() => setAssigning(null)}>
          <div className="assign-list">
            {(techs.data ?? []).map((t) => (
              <button key={t.id} className={`assign-row ${techId === t.id ? 'selected' : ''}`} onClick={() => setTechId(t.id)}>
                <div><b>{t.name}</b><small>{t.specialization}</small></div><Badge tone={statusTone(t.availability)} dot>{label(t.availability)}</Badge>
              </button>
            ))}
          </div>
          <div className="form-actions">
            <button className="btn ghost" onClick={() => setAssigning(null)}>Cancel</button>
            <button className="btn primary" disabled={!techId} onClick={async () => {
              if (await run(() => put(`/v1/work-orders/${assigning.id}/assign/${techId}`), 'Technician assigned')) { setAssigning(null); void orders.reload(); }
            }}>Assign</button>
          </div>
        </Modal>
      )}
      <p className="hint-line">Last refreshed {dateTime(new Date().toISOString().slice(0, 19))}</p>
    </div>
  );
}

function TechButton({ onDone }: { onDone: () => void }) {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button className="btn ghost" onClick={() => setOpen(true)}><Plus size={16} />Add technician</button>
      {open && (
        <FormModal title="Add technician" submitLabel="Add" onClose={() => setOpen(false)}
          fields={[
            { name: 'name', label: 'Full name', required: true },
            { name: 'specialization', label: 'Specialization' },
            { name: 'phone', label: 'Phone', half: true },
            { name: 'email', label: 'Email', type: 'email', half: true },
            { name: 'availability', label: 'Availability', type: 'select', defaultValue: 'AVAILABLE', options: [{ value: 'AVAILABLE', label: 'Available' }, { value: 'BUSY', label: 'Busy' }, { value: 'OFF_DUTY', label: 'Off duty' }] },
          ]}
          onSubmit={async (v) => { await post('/v1/technicians', v); onDone(); }} />
      )}
    </>
  );
}

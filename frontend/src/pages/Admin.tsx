import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { del, get, post } from '../api';
import { ago, label, ROLE_LABEL } from '../format';
import { useAuth, usePoll } from '../store';
import { Badge, DataTable, FormModal, Panel, Tabs, useAction } from '../ui';

type Tab = 'users' | 'audit';
const ROLES = Object.keys(ROLE_LABEL);
const ACTION_TONE: Record<string, 'green' | 'red' | 'cyan' | 'volt' | 'muted'> = {
  LOGIN_SUCCESS: 'green', LOGIN_FAILED: 'red', USER_CREATED: 'cyan', USER_DELETED: 'volt', USER_REGISTERED: 'cyan',
};

export default function Admin() {
  const { user } = useAuth();
  const run = useAction();
  const [tab, setTab] = useState<Tab>('users');
  const [creating, setCreating] = useState(false);
  const users = usePoll(() => get('/v1/users'), [], 15000);
  const audit = usePoll(() => get('/v1/audit-logs'), [], 8000);

  return (
    <div className="page">
      <Panel title="Administration" subtitle="User accounts, role assignment and the security audit trail"
        actions={tab === 'users' && <button className="btn primary" onClick={() => setCreating(true)}><Plus size={16} />New user</button>}>
        <Tabs<Tab> active={tab} onChange={setTab} tabs={[{ id: 'users', label: 'Users', count: (users.data ?? []).length }, { id: 'audit', label: 'Audit log', count: (audit.data ?? []).length }]} />
        {tab === 'users' ? (
          <DataTable rows={users.data ?? []} empty="No users" columns={[
            { key: 'name', header: 'User', render: (r) => <div><b>{r.firstName} {r.lastName}</b><small className="sub">{r.email}</small></div> },
            { key: 'role', header: 'Role', render: (r) => <Badge tone={r.role === 'ADMIN' ? 'violet' : 'cyan'}>{ROLE_LABEL[r.role] ?? label(r.role)}</Badge> },
            {
              key: 'x', header: '', align: 'right', render: (r) => r.email !== user?.email ? (
                <button className="icon-btn danger" title="Delete user" onClick={async () => {
                  if (window.confirm(`Delete ${r.email}?`) && await run(() => del(`/v1/users/${r.id}`), 'User deleted')) { void users.reload(); void audit.reload(); }
                }}><Trash2 size={15} /></button>
              ) : <Badge tone="muted">You</Badge>,
            },
          ]} />
        ) : (
          <DataTable rows={audit.data ?? []} empty="No audit events recorded" columns={[
            { key: 'time', header: 'When', render: (r) => <span className="mono muted">{ago(r.createdAt)}</span> },
            { key: 'action', header: 'Event', render: (r) => <Badge tone={ACTION_TONE[r.action] ?? 'muted'} dot>{label(r.action)}</Badge> },
            { key: 'actor', header: 'Actor', render: (r) => <span className="mono">{r.actor}</span> },
            { key: 'details', header: 'Details', render: (r) => <span className="wrap muted">{r.details}</span> },
          ]} />
        )}
      </Panel>
      {creating && (
        <FormModal title="Create user" subtitle="Administrators assign roles; self-registered users are always Plant Operators" submitLabel="Create user" onClose={() => setCreating(false)}
          fields={[
            { name: 'firstName', label: 'First name', required: true, half: true },
            { name: 'lastName', label: 'Last name', required: true, half: true },
            { name: 'email', label: 'Email', type: 'email', required: true },
            { name: 'password', label: 'Initial password', type: 'password', required: true, half: true },
            { name: 'role', label: 'Role', type: 'select', required: true, half: true, defaultValue: 'PLANT_OPERATOR', options: ROLES.map((r) => ({ value: r, label: ROLE_LABEL[r] })) },
          ]}
          onSubmit={async (v) => { await post('/v1/users', v); void users.reload(); void audit.reload(); }} />
      )}
    </div>
  );
}

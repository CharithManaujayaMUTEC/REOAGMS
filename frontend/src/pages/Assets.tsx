import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { del, post } from '../api';
import { can, label, num, statusTone } from '../format';
import { useAuth, useRef_ } from '../store';
import { Badge, DataTable, FacilityIcon, FormModal, Panel, Tabs, useAction, type Column, type FieldDef } from '../ui';

const opts = (values: string[]) => values.map((v) => ({ value: v, label: label(v) }));
const FACILITY_TYPES = ['SOLAR_FARM', 'WIND_FARM', 'HYDRO_PLANT', 'BATTERY_STORAGE', 'TIDAL_POWER_PLANT'];
const ASSET_TYPES = ['SOLAR_PANEL', 'WIND_TURBINE', 'HYDRO_TURBINE', 'INVERTER', 'BATTERY', 'TRANSFORMER', 'GENERATOR'];
const ASSET_STATUS = ['ACTIVE', 'INACTIVE', 'MAINTENANCE', 'FAULT', 'RETIRED'];
const SENSOR_TYPES = ['TEMPERATURE', 'VOLTAGE', 'CURRENT', 'POWER', 'WIND_SPEED', 'SOLAR_IRRADIANCE', 'WATER_FLOW', 'HUMIDITY', 'PRESSURE'];
const SENSOR_STATUS = ['ACTIVE', 'INACTIVE', 'FAULTY', 'CALIBRATING', 'DECOMMISSIONED'];

type Tab = 'facilities' | 'assets' | 'sensors';

export default function Assets() {
  const { user } = useAuth();
  const ref = useRef_();
  const run = useAction();
  const [tab, setTab] = useState<Tab>('facilities');
  const [creating, setCreating] = useState(false);
  const [filter, setFilter] = useState('');
  const editable = can.manage(user?.role);

  const remove = async (path: string, name: string) => {
    if (!window.confirm(`Delete "${name}"? This cannot be undone.`)) return;
    if (await run(() => del(path), `${name} deleted`)) ref.reload();
  };
  const actions = (path: string): Column<any> => ({
    key: 'actions', header: '', align: 'right',
    render: (r) => editable ? <button className="icon-btn danger" title="Delete" onClick={() => remove(`${path}/${r.id}`, r.name)}><Trash2 size={15} /></button> : null,
  });

  const facilityOptions = ref.facilities.map((f) => ({ value: f.id, label: f.name }));
  const assetOptions = ref.assets.map((a) => ({ value: a.id, label: a.name }));
  const match = (r: any) => !filter || JSON.stringify(r).toLowerCase().includes(filter.toLowerCase());

  const facilityCols: Column<any>[] = [
    { key: 'name', header: 'Facility', render: (r) => <div className="cell-icon"><span className="facility-icon sm"><FacilityIcon type={r.type} size={15} /></span><div><b>{r.name}</b><small>{label(r.type)}</small></div></div> },
    { key: 'location', header: 'Location', render: (r) => `${r.province}, ${r.country}` },
    { key: 'capacityMW', header: 'Capacity', render: (r) => <span className="mono">{num(r.capacityMW, 1)} MW</span> },
    { key: 'coords', header: 'Coordinates', render: (r) => <span className="mono muted">{num(r.latitude, 3)}, {num(r.longitude, 3)}</span> },
    { key: 'assets', header: 'Assets', render: (r) => <Badge tone="cyan">{ref.assets.filter((a) => a.facilityId === r.id).length}</Badge> },
    actions('/v1/facilities'),
  ];
  const assetCols: Column<any>[] = [
    { key: 'name', header: 'Asset', render: (r) => <div><b>{r.name}</b><small className="sub">{r.manufacturer} {r.model}</small></div> },
    { key: 'type', header: 'Type', render: (r) => label(r.type) },
    { key: 'facility', header: 'Facility', render: (r) => ref.facilityName(r.facilityId) },
    { key: 'ratedCapacity', header: 'Rated', render: (r) => <span className="mono">{num(r.ratedCapacity, 0)} kW</span> },
    { key: 'status', header: 'Status', render: (r) => <Badge tone={statusTone(r.status)} dot>{label(r.status)}</Badge> },
    { key: 'serialNumber', header: 'Serial', render: (r) => <span className="mono muted">{r.serialNumber}</span> },
    actions('/v1/assets'),
  ];
  const sensorCols: Column<any>[] = [
    { key: 'name', header: 'Sensor', render: (r) => <b>{r.name}</b> },
    { key: 'type', header: 'Metric', render: (r) => label(r.type) },
    { key: 'asset', header: 'Asset', render: (r) => ref.assetName(r.assetId) },
    { key: 'unit', header: 'Unit', render: (r) => <span className="mono">{r.unit}</span> },
    { key: 'status', header: 'Status', render: (r) => <Badge tone={statusTone(r.status)} dot>{label(r.status)}</Badge> },
    actions('/v1/sensors'),
  ];

  const forms: Record<Tab, { title: string; path: string; fields: FieldDef[] }> = {
    facilities: {
      title: 'Register facility', path: '/v1/facilities', fields: [
        { name: 'name', label: 'Facility name', required: true },
        { name: 'type', label: 'Type', type: 'select', options: opts(FACILITY_TYPES), required: true, half: true },
        { name: 'capacityMW', label: 'Capacity (MW)', type: 'number', step: 'any', required: true, half: true },
        { name: 'country', label: 'Country', required: true, half: true, defaultValue: 'Sri Lanka' },
        { name: 'province', label: 'Province', required: true, half: true },
        { name: 'latitude', label: 'Latitude', type: 'number', step: 'any', required: true, half: true },
        { name: 'longitude', label: 'Longitude', type: 'number', step: 'any', required: true, half: true },
      ],
    },
    assets: {
      title: 'Register asset', path: '/v1/assets', fields: [
        { name: 'name', label: 'Asset name', required: true },
        { name: 'type', label: 'Type', type: 'select', options: opts(ASSET_TYPES), required: true, half: true },
        { name: 'status', label: 'Status', type: 'select', options: opts(ASSET_STATUS), required: true, half: true, defaultValue: 'ACTIVE' },
        { name: 'facilityId', label: 'Facility', type: 'select', options: facilityOptions, required: true },
        { name: 'manufacturer', label: 'Manufacturer', half: true },
        { name: 'model', label: 'Model', half: true },
        { name: 'serialNumber', label: 'Serial number', half: true },
        { name: 'ratedCapacity', label: 'Rated capacity (kW)', type: 'number', step: 'any', half: true },
        { name: 'installationDate', label: 'Installation date', type: 'date' },
      ],
    },
    sensors: {
      title: 'Register sensor', path: '/v1/sensors', fields: [
        { name: 'name', label: 'Sensor name', required: true },
        { name: 'type', label: 'Metric', type: 'select', options: opts(SENSOR_TYPES), required: true, half: true },
        { name: 'status', label: 'Status', type: 'select', options: opts(SENSOR_STATUS), required: true, half: true, defaultValue: 'ACTIVE' },
        { name: 'assetId', label: 'Attached asset', type: 'select', options: assetOptions, required: true },
        { name: 'unit', label: 'Unit', required: true, half: true, placeholder: '°C, V, A, kW' },
        { name: 'serialNumber', label: 'Serial number', half: true },
        { name: 'minThreshold', label: 'Min threshold', type: 'number', step: 'any', half: true },
        { name: 'maxThreshold', label: 'Max threshold', type: 'number', step: 'any', half: true },
        { name: 'installationDate', label: 'Installation date', type: 'date' },
      ],
    },
  };
  const form = forms[tab];
  const rows = { facilities: ref.facilities, assets: ref.assets, sensors: ref.sensors }[tab].filter(match);
  const cols = { facilities: facilityCols, assets: assetCols, sensors: sensorCols }[tab];

  return (
    <div className="page">
      <Panel
        title="Asset registry" subtitle="Facilities, generation assets and the sensors attached to them"
        actions={<>
          <input className="search" placeholder="Search…" value={filter} onChange={(e) => setFilter(e.target.value)} />
          {editable && <button className="btn primary" onClick={() => setCreating(true)}><Plus size={16} />Add</button>}
        </>}
      >
        <Tabs<Tab> active={tab} onChange={setTab} tabs={[
          { id: 'facilities', label: 'Facilities', count: ref.facilities.length },
          { id: 'assets', label: 'Assets', count: ref.assets.length },
          { id: 'sensors', label: 'Sensors', count: ref.sensors.length },
        ]} />
        <DataTable columns={cols} rows={rows} empty={`No ${tab} match`} />
        {!editable && <p className="hint-line">Your role has read-only access to the registry.</p>}
      </Panel>
      {creating && (
        <FormModal title={form.title} fields={form.fields} submitLabel="Create"
          onClose={() => setCreating(false)}
          onSubmit={async (v) => { await post(form.path, v); ref.reload(); }} />
      )}
    </div>
  );
}

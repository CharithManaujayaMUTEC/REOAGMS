// REOAGMS sensor simulator (report: "Sensor Simulator" in the Level-1 DFD).
// 1. Waits for the gateway, logs in as the seeded administrator.
// 2. Seeds facilities, assets, sensors, alert rules and technicians once (idempotent).
// 3. Backfills 24h of history, then streams live readings + device telemetry through the gateway,
//    occasionally injecting faults so alerts -> work orders -> notifications can be demonstrated.

const GATEWAY = process.env.GATEWAY_URL || 'http://localhost:8080';
const EMAIL = process.env.SIM_ADMIN_EMAIL || 'admin@reoagms.com';
const PASSWORD = process.env.SIM_ADMIN_PASSWORD || 'Reoagms@2026';
const INTERVAL = Number(process.env.SIM_INTERVAL_MS || 4000);
const FAULT_EVERY_TICKS = Number(process.env.SIM_FAULT_EVERY_TICKS || 12);

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const log = (...a) => console.log(new Date().toISOString(), ...a);
const rnd = (min, max) => min + Math.random() * (max - min);
const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];
const iso = (d) => d.toISOString().slice(0, 19);

let token = '';
async function call(method, path, body) {
  const res = await fetch(`${GATEWAY}/api${path}`, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let data; try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if (res.status === 401 && path !== '/v1/auth/login') { await login(); return call(method, path, body); }
  if (!res.ok) throw new Error(`${method} ${path} -> ${res.status} ${typeof data === 'string' ? data : JSON.stringify(data)}`);
  return data;
}
const list = (d) => (Array.isArray(d) ? d : d?.content || d?.data || []);

async function login() {
  for (;;) {
    try {
      const r = await call('POST', '/v1/auth/login', { email: EMAIL, password: PASSWORD });
      token = r.token; log('logged in as', EMAIL); return;
    } catch (e) { log('waiting for gateway/identity…', e.message.slice(0, 80)); await sleep(5000); }
  }
}

// ---- Seed model ---------------------------------------------------------------------------
const SENSOR_SPECS = {
  POWER: { unit: 'kW' }, TEMPERATURE: { unit: '°C' }, VOLTAGE: { unit: 'V' }, CURRENT: { unit: 'A' },
  WIND_SPEED: { unit: 'm/s' }, SOLAR_IRRADIANCE: { unit: 'W/m²' }, WATER_FLOW: { unit: 'm³/s' }, PRESSURE: { unit: 'kPa' },
};
const PLAN = [
  { name: 'Hambantota Solar Park', type: 'SOLAR_FARM', country: 'Sri Lanka', province: 'Southern', latitude: 6.124, longitude: 81.101, capacityMW: 10,
    assets: [
      { name: 'PV Array A1', type: 'SOLAR_PANEL', rated: 5000, sensors: ['POWER', 'SOLAR_IRRADIANCE', 'TEMPERATURE'] },
      { name: 'PV Array A2', type: 'SOLAR_PANEL', rated: 5000, sensors: ['POWER', 'SOLAR_IRRADIANCE', 'TEMPERATURE'] },
      { name: 'Central Inverter INV-1', type: 'INVERTER', rated: 2500, sensors: ['VOLTAGE', 'CURRENT', 'TEMPERATURE'] },
    ] },
  { name: 'Mannar Wind Farm', type: 'WIND_FARM', country: 'Sri Lanka', province: 'Northern', latitude: 9.032, longitude: 79.911, capacityMW: 7,
    assets: [
      { name: 'Turbine WT-01', type: 'WIND_TURBINE', rated: 3500, sensors: ['POWER', 'WIND_SPEED', 'TEMPERATURE'] },
      { name: 'Turbine WT-02', type: 'WIND_TURBINE', rated: 3500, sensors: ['POWER', 'WIND_SPEED', 'TEMPERATURE'] },
      { name: 'Substation Transformer TX-1', type: 'TRANSFORMER', rated: 20000, sensors: ['VOLTAGE', 'CURRENT', 'TEMPERATURE'] },
    ] },
  { name: 'Laxapana Hydro Plant', type: 'HYDRO_PLANT', country: 'Sri Lanka', province: 'Central', latitude: 6.896, longitude: 80.502, capacityMW: 12,
    assets: [
      { name: 'Hydro Turbine HT-1', type: 'HYDRO_TURBINE', rated: 12000, sensors: ['POWER', 'WATER_FLOW', 'PRESSURE'] },
      { name: 'Generator G-1', type: 'GENERATOR', rated: 12000, sensors: ['VOLTAGE', 'TEMPERATURE'] },
    ] },
  { name: 'Kelanitissa Battery Storage', type: 'BATTERY_STORAGE', country: 'Sri Lanka', province: 'Western', latitude: 6.962, longitude: 79.872, capacityMW: 16,
    assets: [
      { name: 'Battery Rack BR-1', type: 'BATTERY', rated: 8000, sensors: ['POWER', 'VOLTAGE', 'TEMPERATURE'] },
      { name: 'Battery Rack BR-2', type: 'BATTERY', rated: 8000, sensors: ['POWER', 'VOLTAGE', 'TEMPERATURE'] },
    ] },
  { name: 'Kalpitiya Tidal Station', type: 'TIDAL_POWER_PLANT', country: 'Sri Lanka', province: 'North Western', latitude: 8.233, longitude: 79.767, capacityMW: 2,
    assets: [
      { name: 'Tidal Turbine TT-1', type: 'HYDRO_TURBINE', rated: 2000, sensors: ['POWER', 'WATER_FLOW', 'TEMPERATURE'] },
    ] },
];
const RULES = [
  { name: 'Critical equipment temperature', metricType: 'TEMPERATURE', operator: 'GREATER_THAN', threshold: 90, severity: 'CRITICAL', description: 'Equipment above 90 °C risks permanent damage' },
  { name: 'High equipment temperature', metricType: 'TEMPERATURE', operator: 'GREATER_THAN', threshold: 78, severity: 'MEDIUM', description: 'Early warning for thermal stress' },
  { name: 'Over-voltage', metricType: 'VOLTAGE', operator: 'GREATER_THAN', threshold: 252, severity: 'HIGH', description: 'Bus voltage out of tolerance' },
  { name: 'Under-voltage', metricType: 'VOLTAGE', operator: 'LESS_THAN', threshold: 205, severity: 'MEDIUM', description: 'Sag below nominal tolerance' },
  { name: 'Storm wind speed', metricType: 'WIND_SPEED', operator: 'GREATER_THAN', threshold: 25, severity: 'CRITICAL', description: 'Cut-out wind speed exceeded' },
  { name: 'Over-current', metricType: 'CURRENT', operator: 'GREATER_THAN', threshold: 110, severity: 'HIGH', description: 'Current beyond protection limit' },
  { name: 'Low water flow', metricType: 'WATER_FLOW', operator: 'LESS_THAN', threshold: 8, severity: 'MEDIUM', description: 'Reduced hydraulic head' },
];
const TECHNICIANS = [
  { name: 'Nimal Perera', specialization: 'Solar & Inverters', phone: '+94 77 100 2001', email: 'nimal.perera@reoagms.com', availability: 'AVAILABLE' },
  { name: 'Kasun Fernando', specialization: 'Wind Turbines', phone: '+94 77 100 2002', email: 'kasun.fernando@reoagms.com', availability: 'AVAILABLE' },
  { name: 'Dilini Jayasuriya', specialization: 'Hydro & Generators', phone: '+94 77 100 2003', email: 'dilini.j@reoagms.com', availability: 'AVAILABLE' },
  { name: 'Sahan Wickramasinghe', specialization: 'Battery & Grid', phone: '+94 77 100 2004', email: 'sahan.w@reoagms.com', availability: 'BUSY' },
];

async function seed() {
  const existing = list(await call('GET', '/v1/facilities'));
  if (existing.length) { log(`seed skipped: ${existing.length} facilities already present`); return; }
  log('seeding demo portfolio…');
  for (const f of PLAN) {
    const { assets, ...facilityBody } = f;
    const facility = await call('POST', '/v1/facilities', facilityBody);
    for (const a of assets) {
      const asset = await call('POST', '/v1/assets', {
        name: a.name, type: a.type, status: 'ACTIVE', manufacturer: pick(['Siemens', 'ABB', 'Vestas', 'GE', 'Huawei']),
        model: `${a.type.slice(0, 3)}-${Math.floor(rnd(100, 999))}`, serialNumber: `SN-${Math.floor(rnd(1e6, 9e6))}`,
        installationDate: '2023-03-15', ratedCapacity: a.rated, facilityId: facility.id,
      });
      for (const s of a.sensors) {
        await call('POST', '/v1/sensors', {
          name: `${a.name} · ${s.replace('_', ' ').toLowerCase()}`, type: s, status: 'ACTIVE', unit: SENSOR_SPECS[s].unit,
          manufacturer: 'Bosch Sensortec', serialNumber: `SE-${Math.floor(rnd(1e6, 9e6))}`, installationDate: '2023-03-15', assetId: asset.id,
        });
      }
    }
  }
  for (const r of RULES) await call('POST', '/v1/alert-rules', { ...r, enabled: true });
  for (const t of TECHNICIANS) await call('POST', '/v1/technicians', t);
  log('seed complete');
}

// ---- Signal model -------------------------------------------------------------------------
// SIM_SOLAR_MODE=real follows the local sun (zero at night); the default "demo" keeps solar visibly
// productive at any hour by slowly oscillating between 55% and 100% of peak.
const SOLAR_MODE = process.env.SIM_SOLAR_MODE || 'demo';
function daylight(date) {
  if (SOLAR_MODE === 'demo') return 0.78 + 0.22 * Math.sin(date.getTime() / 600_000);
  const h = date.getHours() + date.getMinutes() / 60;
  return Math.max(0, Math.sin(((h - 6) / 12) * Math.PI)); // 0 at night, 1 at noon
}
function normalValue(sensor, asset, date) {
  const rated = asset.ratedCapacity || 1000;
  switch (sensor.type) {
    case 'SOLAR_IRRADIANCE': return daylight(date) * rnd(880, 1020);
    case 'POWER':
      if (asset.type === 'SOLAR_PANEL') return rated * daylight(date) * rnd(0.82, 0.96);
      if (asset.type === 'WIND_TURBINE') return rated * rnd(0.35, 0.85);
      return rated * rnd(0.55, 0.9);
    case 'TEMPERATURE': return rnd(46, 68) + (asset.type === 'SOLAR_PANEL' ? daylight(date) * 8 : 0);
    case 'VOLTAGE': return rnd(226, 238);
    case 'CURRENT': return rnd(35, 85);
    case 'WIND_SPEED': return rnd(6.5, 14);
    case 'WATER_FLOW': return rnd(34, 58);
    case 'PRESSURE': return rnd(96, 106);
    default: return rnd(10, 20);
  }
}
const FAULTS = {
  TEMPERATURE: () => rnd(93, 104), VOLTAGE: () => rnd(256, 270), CURRENT: () => rnd(114, 130),
  WIND_SPEED: () => rnd(26, 31), WATER_FLOW: () => rnd(3, 7),
};

async function postReading(sensor, asset, value, date) {
  return call('POST', '/v1/sensor-readings', {
    sensorId: sensor.id, assetId: asset.id, facilityId: asset.facilityId, timestamp: iso(date),
    value: Number(value.toFixed(2)), unit: sensor.unit, metricType: sensor.type,
  });
}

async function backfill(model) {
  const now = Date.now();
  for (const { asset, sensors } of model) {
    const probe = list(await call('GET', `/v1/sensor-readings?assetId=${asset.id}`));
    if (probe.length) continue; // history already present for this asset
    log(`backfilling 24h of history for ${asset.name}…`);
    for (let step = 24; step >= 1; step--) {
      const date = new Date(now - step * 3600_000);
      for (const s of sensors) await postReading(s, asset, normalValue(s, asset, date), date);
    }
  }
}


// ---- Stage 2: data for the remaining services (idempotent, API-driven) ---------------------
async function seedExtras(assets) {
  const empty = async (path) => !(list(await call('GET', path))).length;
  const day = 86400_000;

  if (await empty('/v1/email-templates')) {
    await call('POST', '/v1/email-templates', {
      name: 'ALERT_OPENED', subject: 'REOAGMS alert: {{severity}} on {{asset}}',
      body: 'A {{severity}} alert was raised for {{asset}}: {{message}}. Open the console to acknowledge it.',
    });
    log('seeded email template');
  }

  if (await empty('/v1/maintenance-history')) {
    const techs = list(await call('GET', '/v1/technicians'));
    const pick3 = assets.slice(0, 3);
    for (const [i, asset] of pick3.entries()) {
      const when = new Date(Date.now() - (i + 2) * day);
      const wo = await call('POST', '/v1/work-orders', {
        assetId: asset.id, technicianId: techs[i % techs.length]?.id, title: `Scheduled inspection – ${asset.name}`,
        description: 'Quarterly preventive inspection, thermal scan and torque check.', maintenanceType: 'PREVENTIVE', scheduledAt: iso(when),
      });
      const task = await call('POST', '/v1/maintenance-tasks', {
        workOrderId: wo.id, title: 'Thermal imaging and connection torque check', description: 'Record hotspots above 70 °C.', technicianId: techs[i % techs.length]?.id,
      });
      await call('PUT', `/v1/work-orders/${wo.id}/assign/${techs[i % techs.length].id}`);
      await call('PUT', `/v1/work-orders/${wo.id}/start`);
      await call('PUT', `/v1/maintenance-tasks/${task.id}/start`);
      await call('PUT', `/v1/maintenance-tasks/${task.id}/complete`);
      await call('PUT', `/v1/work-orders/${wo.id}/complete`);
      await call('POST', '/v1/maintenance-history', {
        assetId: asset.id, workOrderId: wo.id, technicianId: techs[i % techs.length].id, maintenanceType: 'PREVENTIVE',
        summary: 'Inspection passed – no defects found', performedAt: iso(when), notes: 'All readings within tolerance.',
      });
    }
    // An open task on an auto-generated corrective order so the board shows work in flight.
    const open = list(await call('GET', '/v1/work-orders')).find((o) => o.maintenanceType === 'CORRECTIVE');
    if (open) await call('POST', '/v1/maintenance-tasks', { workOrderId: open.id, title: 'Diagnose fault and replace failed component', description: 'Raised from alert.' });
    log('seeded maintenance tasks and history');
  }

  if (await empty('/v1/dashboards')) {
    for (const [name, type, days] of [['Executive overview', 'EXECUTIVE', 30], ['Operations centre', 'OPERATIONS', 7], ['Maintenance board', 'MAINTENANCE', 14], ['Asset performance', 'ASSET_PERFORMANCE', 30]]) {
      await call('POST', '/v1/dashboards', { name, description: `${name} dashboard`, dashboardType: type, defaultRangeDays: days, active: true });
    }
    log('seeded dashboards');
  }

  const end = new Date(); const start = new Date(end.getTime() - day);
  const facilities = list(await call('GET', '/v1/facilities'));
  if (await empty('/v1/energy-reports')) {
    for (const f of facilities) {
      await call('POST', '/v1/energy-reports', { name: `${f.name} – last 24h energy`, facilityId: f.id, periodStart: iso(start), periodEnd: iso(end) });
    }
    log('seeded energy reports');
  }
  if (await empty('/v1/performance-reports')) {
    for (const f of facilities) {
      await call('POST', '/v1/performance-reports', { name: `${f.name} – last 24h performance`, scopeType: 'FACILITY', scopeId: f.id, periodStart: iso(start), periodEnd: iso(end) });
    }
    log('seeded performance reports');
  }
  if (await empty('/v1/kpis')) {
    const kpis = [
      ['Fleet availability', 'AVAILABILITY', 99.2, 99.5, '%'], ['Fleet efficiency', 'EFFICIENCY', 87.4, 90, '%'],
      ['Capacity factor', 'CAPACITY_FACTOR', 61.8, 60, '%'], ['Maintenance completion', 'MAINTENANCE_COMPLETION_RATE', 78, 85, '%'],
      ['Alerts (24h)', 'ALERT_COUNT', 14, 10, 'alerts'], ['Open work orders', 'OPEN_WORK_ORDER_COUNT', 6, 5, 'orders'],
    ];
    for (const [name, kpiType, actualValue, targetValue, unit] of kpis) {
      await call('POST', '/v1/kpis', { name, kpiType, scopeType: 'ORGANIZATION', periodStart: iso(new Date(end.getTime() - 30 * day)), periodEnd: iso(end), actualValue, targetValue, unit });
    }
    log('seeded KPIs');
  }
}

async function main() {
  await login();
  for (;;) { try { await seed(); break; } catch (e) { log('seed failed, retrying:', e.message.slice(0, 160)); await sleep(5000); } }
  const assets = list(await call('GET', '/v1/assets?size=500'));
  const sensors = list(await call('GET', '/v1/sensors?size=500'));
  const model = assets.map((asset) => ({ asset, sensors: sensors.filter((s) => s.assetId === asset.id) })).filter((m) => m.sensors.length);
  try { await backfill(model); } catch (e) { log('backfill failed:', e.message.slice(0, 160)); }
  try { await seedExtras(assets); } catch (e) { log('extra seeding failed:', e.message.slice(0, 200)); }
  if (process.env.SIM_MODE === 'seed') { log('seed-only mode: database seeded, exiting'); process.exit(0); }

  let tick = 0;
  log(`streaming live telemetry every ${INTERVAL} ms across ${model.length} assets`);
  for (;;) {
    tick++;
    const now = new Date();
    const inject = tick % FAULT_EVERY_TICKS === 0;
    const victim = inject ? pick(model.filter((m) => m.sensors.some((s) => FAULTS[s.type]))) : null;
    for (const { asset, sensors: ss } of model) {
      try {
        let status = 'ONLINE';
        for (const s of ss) {
          let value = normalValue(s, asset, now);
          if (victim && victim.asset.id === asset.id && FAULTS[s.type]) {
            value = FAULTS[s.type](); status = 'WARNING'; log(`FAULT injected: ${asset.name} ${s.type}=${value.toFixed(1)}`);
          }
          await postReading(s, asset, value, now);
        }
        await call('POST', '/v1/telemetry', {
          assetId: asset.id, facilityId: asset.facilityId, status: Math.random() < 0.015 ? 'OFFLINE' : status,
          lastSeenAt: iso(now), batteryLevel: Number(rnd(72, 100).toFixed(1)), signalStrength: Number(rnd(-82, -48).toFixed(1)),
        });
      } catch (e) { log('tick error:', e.message.slice(0, 160)); }
    }
    await sleep(INTERVAL);
  }
}
main().catch((e) => { console.error(e); process.exit(1); });

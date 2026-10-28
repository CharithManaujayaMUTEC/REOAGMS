// Builds one 7-slide academic deck per team member into docs/presentations.
// Run: node scripts/presentations/build-decks.js   (needs pptxgenjs)
const path = require('path');
const fs = require('fs');
const pptxgen = require('pptxgenjs');

const OUT = path.join(__dirname, '..', '..', 'deliverables');
fs.mkdirSync(OUT, { recursive: true });

const NAVY = '1B2A49', STEEL = '3D5A80', INK = '22304A', MUTED = '5B6B82', LINE = 'C9D3E0', TINT = 'EEF2F8', WHITE = 'FFFFFF', ACCENT = 'B5651D';
const HEAD = 'Cambria', BODY = 'Calibri';
const COURSE = 'EC8208 Software Architecture · University of Ruhuna · REOAGMS';

const MEMBERS = [
  {
    file: '1-Charith', name: 'Charith', id: 'EG/2021/4707',
    title: 'System Architecture, Identity, Gateway & Integration',
    scope: ['Hybrid architecture decision: layered, client–server, microservices, event-driven', 'Identity Service: JWT authentication, RBAC, audit log', 'API Gateway: routing, token validation, role policy', 'React console, Docker Compose, Kubernetes manifests, RabbitMQ infrastructure', 'Final integration and end-to-end testing'],
    fr: ['FR-01', 'FR-02', 'FR-03', 'FR-16', 'FR-18'],
    flowTitle: 'Request path through the platform',
    flow: [['React console', 'nginx · STOMP /ws'], ['API Gateway', 'JWT + role policy'], ['Identity Service', 'users · roles · audit'], ['identity_db', 'PostgreSQL']],
    table: [['JWT', 'HS256 token with subject and role claim, 24 h expiry'], ['Passwords', 'BCrypt hashes; self-registration is always Plant Operator'], ['Gateway', '7 routes under /api/v1; 401 without token, 403 by role'], ['Audit log', 'Login success and failure, user create and delete'], ['Deployment', 'Docker Compose and k8s/base manifests']],
    interfaces: ['POST /api/v1/auth/login and /register', 'GET /api/v1/users, /audit-logs (ADMIN only)', '/ws proxied to the Notification Service'],
    infra: ['RabbitMQ topic exchange reoagms.events with dead-letter queue', 'Redis cache, Prometheus and Grafana', 'nginx serves the production React build'],
    stats: [['19', 'containers in Docker Compose'], ['5', 'RBAC roles with seeded accounts'], ['7', 'gateway routes']],
    evidence: 'Role matrix verified: Manager can create a facility (200); Operator, Engineer and Executive receive 403.',
    demo: ['Sign in as Administrator', 'Control room: live KPIs and alert feed', 'Administration: users and audit log'],
    take: ['One entry point, one security policy', 'Services stay independent; events decouple them', 'Everything starts with one command'],
  },
  {
    file: '2-Nemal', name: 'Nemal', id: 'EG/2021/4710',
    title: 'Asset Service: Facilities, Assets & Sensors',
    scope: ['Facility, asset and sensor CRUD', 'Image upload for assets', 'Search, filtering, pagination and sorting', 'Bean Validation and structured error responses', 'Unit tests for services and image handling'],
    fr: ['FR-04', 'FR-05', 'FR-06'],
    flowTitle: 'Layered structure of the service',
    flow: [['Controller', 'REST + validation'], ['Service', 'business rules'], ['Repository', 'Spring Data JPA'], ['asset_db', 'PostgreSQL']],
    table: [['Facility', '5 types (solar, wind, hydro, battery, tidal); MW capacity and coordinates'], ['Asset', '7 types, 5 statuses, rated capacity in kW, serial number'], ['Sensor', '9 metric types, unit, min and max thresholds'], ['Images', 'Multipart upload stored per asset'], ['Search', 'JPA Specifications by name, status and type; paged, default 20']],
    interfaces: ['/api/v1/facilities, /assets, /sensors', '/api/v1/assets/search with page, size, sort', 'UUID identifiers shared with other services'],
    infra: ['Monitoring uses sensor and asset ids', 'Maintenance orders reference an asset', 'Analytics reads rated capacity for expected energy'],
    stats: [['5 / 11 / 32', 'facilities / assets / sensors in the database'], ['19', 'unit test methods'], ['9', 'sensor metric types']],
    evidence: 'Validation rejects blank names, future installation dates and non-positive capacity with field-level messages.',
    demo: ['Assets page: facilities, assets, sensors', 'Search and tab filtering', 'Add a facility; see validation'],
    take: ['Registry is the source of truth for ids', 'Pagination keeps lists fast', 'Write access is restricted by role'],
  },
  {
    file: '3-Govindu', name: 'Govindu', id: 'EG/2021/4701',
    title: 'Monitoring Service: Telemetry & Alerting',
    scope: ['Sensor readings and device telemetry ingestion', 'Configurable alert rules and alert generation', 'Historical data retrieval with filters', 'Dashboard summary APIs', 'RabbitMQ alert publisher'],
    fr: ['FR-07', 'FR-08', 'FR-09', 'FR-10', 'FR-13', 'FR-15'],
    flowTitle: 'From reading to alert event',
    flow: [['Reading API', 'POST /sensor-readings'], ['Rule evaluator', 'operator vs threshold'], ['Alert', 'OPEN → ACK → RESOLVED'], ['RabbitMQ', 'alert.opened.v1']],
    table: [['Rules', '5 operators, 4 severities, per asset or global'], ['De-duplication', 'One OPEN alert per rule and asset'], ['Event', 'monitoring.alert.opened.v1 on reoagms.events'], ['History', 'Filter by facility, asset, sensor, metric, time window'], ['Dashboard', 'Counts by status, severity and device state; Redis cache 10 s']],
    interfaces: ['/api/v1/sensor-readings, /telemetry', '/api/v1/alerts, /alert-rules', '/api/v1/dashboard/summary'],
    infra: ['Publishes AlertEvent v1 (schemaVersion, alertId, assetId, severity, value, threshold)', 'Consumed by Notification and Maintenance', 'Cache falls back to PostgreSQL if Redis is down'],
    stats: [['16,000+', 'readings stored in monitoring_db'], ['7', 'seeded detection rules'], ['11', 'unit test methods']],
    evidence: 'Device health uses the latest heartbeat of every asset, and dashboard answers are cached for 10 s in Redis.',
    demo: ['Live monitoring: sensor tiles and threshold line', 'A fault is injected by the simulator', 'Alert appears instantly via STOMP'],
    take: ['Rules are data, not code', 'Events are versioned contracts', 'Dedup protects operators from alert floods'],
  },
  {
    file: '4-Thurunu', name: 'Thurunu', id: 'EG/2021/4620',
    title: 'Maintenance Service: Work Orders & Field Crew',
    scope: ['Work orders and maintenance tasks', 'Technician assignment and availability', 'Preventive and corrective maintenance', 'Maintenance history and summary reports', 'Automatic corrective orders from alerts'],
    fr: ['FR-11', 'FR-12'],
    flowTitle: 'Work-order lifecycle',
    flow: [['Alert event', 'HIGH or CRITICAL'], ['Work order', 'status CREATED'], ['Assigned → In progress', 'technician + start'], ['Completed', 'history record']],
    table: [['Work order', 'CREATED → ASSIGNED → IN_PROGRESS → COMPLETED or CANCELLED'], ['Task', 'PENDING, IN_PROGRESS, COMPLETED with start and complete'], ['Technician', 'Specialisation and availability (available, busy, off duty)'], ['Types', 'PREVENTIVE and CORRECTIVE'], ['History and reports', 'Per-asset history; /maintenance-reports/summary']],
    interfaces: ['/api/v1/work-orders (assign, start, complete)', '/api/v1/maintenance-tasks, /technicians', '/api/v1/maintenance-history'],
    infra: ['Consumes queue maintenance.alert-opened.v1', 'Skips an asset that already has an active corrective order', 'Idempotent: alert id is stored in the description'],
    stats: [['14', 'work orders in the database'], ['4', 'technicians'], ['3', 'completed jobs in history']],
    evidence: 'Auto-creation verified end to end: a CRITICAL temperature alert produced a CORRECTIVE order within seconds.',
    demo: ['Maintenance board: auto-created order', 'Assign a technician', 'Start, then complete'],
    take: ['Faults become actionable work automatically', 'One active corrective order per asset', 'History keeps a full record of maintenance'],
  },
  {
    file: '5-Yasiru', name: 'Yasiru', id: 'EG/2021/4770',
    title: 'Notification Service: Alerts, Email & Real-time Push',
    scope: ['Email and SMS notification channels', 'Email templates', 'Notification history', 'Retry handling for failed deliveries', 'RabbitMQ alert consumer'],
    fr: ['FR-14', 'FR-15'],
    flowTitle: 'Alert delivery pipeline',
    flow: [['RabbitMQ queue', 'alert-opened queue'], ['Alert consumer', 'schema check'], ['Dispatchers', 'EMAIL · SMS · PUSH'], ['notification_db', 'history']],
    table: [['Consumer', 'Durable queue with dead-letter queue after retries'], ['Channels', 'EMAIL via SMTP, SMS dispatcher, in-app PUSH over STOMP'], ['Templates', 'CRUD for subject and body templates'], ['History', 'Status, retry count and failure reason per message'], ['Retry', 'Scheduler: up to 3 attempts, 60 s apart; manual retry']],
    interfaces: ['/api/v1/notifications (list, retry)', '/api/v1/email, /email-templates, /sms', 'WebSocket endpoint /ws'],
    infra: ['Input: AlertEvent v1 from Monitoring', 'Output: /topic/alerts and /topic/notifications', 'SMTP through MAIL_USERNAME and MAIL_PASSWORD'],
    stats: [['76+', 'notifications recorded'], ['3', 'automatic retries per message'], ['14', 'unit test methods']],
    evidence: 'Every attempt is stored with status, retry count and failure reason; in-app push reaches the console instantly over STOMP.',
    demo: ['Notifications page: PUSH delivered', 'Delivery history and retry control', 'Toast arrives over WebSocket'],
    take: ['Every delivery is recorded', 'In-app push works without SMTP', 'Consumers are decoupled from Monitoring'],
  },
  {
    file: '6-Sachinthana', name: 'Sachinthana', id: 'EG/2021/4828',
    title: 'Analytics Service: KPIs, Reports & Export',
    scope: ['Dashboard summaries and statistics', 'Energy and performance reports', 'Maintenance statistics and asset utilisation', 'KPIs with targets', 'Report export'],
    fr: ['FR-17'],
    flowTitle: 'How a report is produced',
    flow: [['Source APIs', '3 services via REST'], ['Calculation', 'energy + performance'], ['Reports and KPIs', 'analytics_db'], ['CSV export', '/{id}/export']],
    table: [['Energy', 'Average power per sensor per hour × hours = kWh; variance vs expected'], ['Performance', 'Availability, efficiency, capacity factor, utilisation, downtime'], ['Maintenance', 'Open orders, completion rate, alert count'], ['KPIs', '11 KPI types with target, actual and status'], ['Export', 'CSV download for energy and performance reports']],
    interfaces: ['/api/v1/analytics/dashboard, /statistics', '/api/v1/energy-reports, /performance-reports', '/api/v1/kpis, /dashboards'],
    infra: ['REST clients only; no shared databases', 'Aggregation restricted to POWER readings', 'Console charts use the hourly trend'],
    stats: [['5 + 5', 'energy and performance reports'], ['6', 'KPIs seeded'], ['11', 'unit test methods']],
    evidence: 'Energy is integrated from average power per hour, so every figure is true kWh, and each report exports to CSV.',
    demo: ['Analytics page: facility and range', 'Generate an energy report', 'Export CSV'],
    take: ['Reports are computed, stored and exportable', 'Correct units make the figures trustworthy', 'Executives get read-only access'],
  },
  {
    file: '7-All-Members', name: 'All Members', id: 'Team',
    title: 'Quality, Documentation & Integration',
    scope: ['Unit and integration tests', 'API documentation', 'Validation and error handling', 'Coordination so services integrate cleanly', 'Shared integration contracts'],
    fr: ['All FR', 'NFR'],
    flowTitle: 'Quality pipeline',
    flow: [['Contracts', 'docs/ contracts'], ['Service tests', 'JUnit + Mockito'], ['Gateway checks', 'auth + role matrix'], ['End-to-end', 'reading → alert → order']],
    table: [['Tests', '60 test methods across 7 modules'], ['Documentation', 'OpenAPI + Postman (Monitoring, Analytics), SQL schemas, ER diagrams'], ['Validation', 'Bean Validation; messages returned as validationErrors'], ['Errors', 'GlobalExceptionHandler per service, consistent JSON'], ['Compatibility', 'UUID references, versioned events, single gateway']],
    interfaces: ['docs/INTEGRATION_CONTRACTS.md (alert event v1)', 'docs/api: OpenAPI and Postman collections', 'docs/database and docs/diagrams'],
    infra: ['Verified chain: reading → alert → RabbitMQ', 'then work order, notification and STOMP push', 'Prometheus scrapes all 7 services'],
    stats: [['60', 'test methods'], ['7', 'services behind the gateway'], ['1', 'versioned event contract']],
    evidence: 'Shared contracts, validation and 60 test methods keep seven independent services compatible.',
    demo: ['RBAC: executive is read-only', 'Grafana platform dashboard', 'RabbitMQ and Prometheus targets'],
    take: ['Contracts first, then code', 'Tests protect each service boundary', 'Every service deploys independently'],
  },
];

const NARR = require('./narration');
MEMBERS.forEach((m) => Object.assign(m, NARR[m.name]));

function addMember(pres, m) {
  pres.addSection({ title: m.name });
  const heading = (s, text, kicker) => {
    s.addText(kicker, { x: 0.5, y: 0.3, w: 9, h: 0.25, fontFace: BODY, fontSize: 12, bold: true, color: ACCENT, charSpacing: 3, margin: 0, isTextBox: true });
    s.addText(text, { x: 0.5, y: 0.55, w: 9, h: 0.6, fontFace: HEAD, fontSize: 30, bold: true, color: NAVY, margin: 0, isTextBox: true });
  };

  // 1 Title
  let s = pres.addSlide({ sectionTitle: m.name });
  s.background = { color: WHITE };
  s.addText('RENEWABLE ENERGY OPERATIONS, ASSET AND GRID MANAGEMENT SYSTEM', { x: 0.7, y: 0.8, w: 8.6, h: 0.3, fontFace: BODY, fontSize: 12, bold: true, color: ACCENT, charSpacing: 3, margin: 0, isTextBox: true });
  s.addText(m.title, { x: 0.7, y: 1.35, w: 8.6, h: 1.6, fontFace: HEAD, fontSize: 38, bold: true, color: NAVY, margin: 0, valign: 'top', isTextBox: true });
  s.addText(m.name, { x: 0.7, y: 3.45, w: 8.6, h: 0.5, fontFace: BODY, fontSize: 24, bold: true, color: INK, margin: 0, isTextBox: true });
  s.addText('EC8208 Software Architecture · Continuous Assessment 01\nDepartment of Electrical and Information Engineering, University of Ruhuna', { x: 0.7, y: 4.3, w: 8.6, h: 0.6, fontFace: BODY, fontSize: 14, color: MUTED, margin: 0, isTextBox: true });
  s.addNotes(m.notes[0]);

  // 2 Role and scope
  s = pres.addSlide({ masterName: 'CONTENT', sectionTitle: m.name });
  heading(s, 'Role and scope', 'RESPONSIBILITIES');
  s.addText(m.scope.map((t, i) => ({ text: t, options: { bullet: true, breakLine: i < m.scope.length - 1, paraSpaceAfter: 6 } })), { x: 0.5, y: 1.45, w: 5.7, h: 3.5, fontFace: BODY, fontSize: 16, color: INK, valign: 'top', margin: 0, isTextBox: true });
  s.addShape(pres.ShapeType.roundRect, { x: 6.5, y: 1.45, w: 3.0, h: 0.75 + 0.42 * m.fr.length, fill: { color: TINT }, line: { color: LINE, width: 0.75 }, rectRadius: 0.08 });
  s.addText('Requirements covered', { x: 6.7, y: 1.6, w: 2.6, h: 0.3, fontFace: BODY, fontSize: 14, bold: true, color: STEEL, margin: 0, isTextBox: true });
  s.addText(m.fr.map((t, i) => ({ text: t, options: { breakLine: i < m.fr.length - 1, paraSpaceAfter: 4 } })), { x: 6.7, y: 2.0, w: 2.6, h: 0.42 * m.fr.length, fontFace: BODY, fontSize: 16, color: NAVY, bold: true, valign: 'top', margin: 0, isTextBox: true });
  s.addNotes(m.notes[1]);

  // 3 Design flow
  s = pres.addSlide({ masterName: 'CONTENT', sectionTitle: m.name });
  heading(s, m.flowTitle, 'DESIGN');
  const n = m.flow.length, gap = 0.4, bw = (9 - gap * (n - 1)) / n;
  m.flow.forEach(([t, sub], i) => {
    const x = 0.5 + i * (bw + gap);
    s.addShape(pres.ShapeType.roundRect, { x, y: 1.9, w: bw, h: 1.3, fill: { color: i === 0 ? NAVY : TINT }, line: { color: i === 0 ? NAVY : LINE, width: 1 }, rectRadius: 0.08, objectName: `step-${i + 1}` });
    s.addText(t, { x: x + 0.1, y: 1.95, w: bw - 0.2, h: 1.2, fontFace: HEAD, fontSize: 18, bold: true, color: i === 0 ? WHITE : NAVY, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
    s.addText(sub, { x, y: 3.3, w: bw, h: 0.55, fontFace: BODY, fontSize: 12, color: MUTED, align: 'center', valign: 'top', margin: 0, isTextBox: true });
    if (i < n - 1) s.addText('→', { x: x + bw, y: 2.25, w: gap, h: 0.6, fontFace: BODY, fontSize: 28, bold: true, color: ACCENT, align: 'center', margin: 0, isTextBox: true });
  });
  s.addNotes(m.notes[2]);

  // 4 Implementation table
  s = pres.addSlide({ masterName: 'CONTENT', sectionTitle: m.name });
  heading(s, 'Key implementation details', 'IMPLEMENTATION');
  const rows = [[{ text: 'Area', options: { bold: true, color: WHITE, fill: { color: NAVY } } }, { text: 'What was built', options: { bold: true, color: WHITE, fill: { color: NAVY } } }]]
    .concat(m.table.map(([a, b], i) => [{ text: a, options: { bold: true, color: NAVY, fill: { color: i % 2 ? WHITE : TINT } } }, { text: b, options: { color: INK, fill: { color: i % 2 ? WHITE : TINT } } }]));
  s.addTable(rows, { x: 0.5, y: 1.4, w: 9, colW: [2.0, 7.0], rowH: 0.6, fontFace: BODY, fontSize: 14, border: { type: 'solid', color: LINE, pt: 0.75 }, valign: 'middle', margin: [0.04, 0.12, 0.04, 0.12] });
  s.addNotes(m.notes[3]);

  // 5 Integration
  s = pres.addSlide({ masterName: 'CONTENT', sectionTitle: m.name });
  heading(s, 'Interfaces and integration', 'INTEGRATION');
  [['Interfaces', m.interfaces, 0.5], ['Connections', m.infra, 5.1]].forEach(([h, list, x]) => {
    s.addShape(pres.ShapeType.roundRect, { x, y: 1.45, w: 4.4, h: 3.1, fill: { color: TINT }, line: { color: LINE, width: 0.75 }, rectRadius: 0.08 });
    s.addText(h, { x: x + 0.25, y: 1.6, w: 3.9, h: 0.35, fontFace: HEAD, fontSize: 18, bold: true, color: NAVY, margin: 0, isTextBox: true });
    s.addText(list.map((t, i) => ({ text: t, options: { bullet: true, breakLine: i < list.length - 1, paraSpaceAfter: 8 } })), { x: x + 0.25, y: 2.05, w: 3.9, h: 2.4, fontFace: BODY, fontSize: 15, color: INK, valign: 'top', margin: 0, isTextBox: true });
  });
  s.addNotes(m.notes[4]);

  // 6 Evidence
  s = pres.addSlide({ masterName: 'CONTENT', sectionTitle: m.name });
  heading(s, 'Evidence from the running system', 'RESULTS');
  m.stats.forEach(([big, label], i) => {
    const x = 0.5 + i * 3.1;
    s.addShape(pres.ShapeType.roundRect, { x, y: 1.5, w: 2.8, h: 2.0, fill: { color: WHITE }, line: { color: LINE, width: 1 }, rectRadius: 0.08, shadow: { type: 'outer', color: '000000', opacity: 0.12, blur: 6, offset: 2, angle: 90 } });
    s.addText(big, { x, y: 1.65, w: 2.8, h: 0.9, fontFace: HEAD, fontSize: big.length > 6 ? 30 : 40, bold: true, color: NAVY, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
    s.addText(label, { x: x + 0.15, y: 2.6, w: 2.5, h: 0.8, fontFace: BODY, fontSize: 14, color: MUTED, align: 'center', valign: 'top', margin: 0, isTextBox: true });
  });
  s.addText(m.evidence, { x: 0.5, y: 3.85, w: 9, h: 0.9, fontFace: BODY, fontSize: 16, italic: true, color: INK, valign: 'top', margin: 0, isTextBox: true });
  s.addNotes(m.notes[5]);

  // 7 Demo and takeaways
  s = pres.addSlide({ masterName: 'CONTENT', sectionTitle: m.name });
  heading(s, 'Live demo and takeaways', 'SUMMARY');
  s.addText('Demo steps', { x: 0.5, y: 1.4, w: 4.3, h: 0.35, fontFace: HEAD, fontSize: 18, bold: true, color: NAVY, margin: 0, isTextBox: true });
  s.addText(m.demo.map((t, i) => ({ text: t, options: { bullet: { type: 'number' }, breakLine: i < m.demo.length - 1, paraSpaceAfter: 8 } })), { x: 0.5, y: 1.85, w: 4.3, h: 2.6, fontFace: BODY, fontSize: 16, color: INK, valign: 'top', margin: 0, isTextBox: true });
  s.addShape(pres.ShapeType.roundRect, { x: 5.2, y: 1.4, w: 4.3, h: 2.6, fill: { color: TINT }, line: { color: LINE, width: 0.75 }, rectRadius: 0.08 });
  s.addText('Key takeaways', { x: 5.45, y: 1.55, w: 3.8, h: 0.35, fontFace: HEAD, fontSize: 18, bold: true, color: NAVY, margin: 0, isTextBox: true });
  s.addText(m.take.map((t, i) => ({ text: t, options: { bullet: true, breakLine: i < m.take.length - 1, paraSpaceAfter: 8 } })), { x: 5.45, y: 2.0, w: 3.8, h: 2.0, fontFace: BODY, fontSize: 15, color: INK, valign: 'top', margin: 0, isTextBox: true });
  s.addNotes(m.notes[6]);

}


(async () => {
  const pres = new pptxgen();
  pres.layout = 'LAYOUT_16x9';
  pres.title = 'REOAGMS: Team Presentation';
  pres.author = 'REOAGMS project team';
  pres.company = 'University of Ruhuna';
  pres.theme = { headFontFace: HEAD, bodyFontFace: BODY };
  pres.defineSlideMaster({
    title: 'CONTENT', background: { color: WHITE },
    objects: [{ text: { text: COURSE, options: { x: 0.5, y: 5.25, w: 7.5, h: 0.25, fontFace: BODY, fontSize: 10, color: MUTED, margin: 0 } } }],
    slideNumber: { x: 9.0, y: 5.25, w: 0.5, h: 0.25, fontFace: BODY, fontSize: 10, color: MUTED, align: 'right' },
  });

  // Team title
  pres.addSection({ title: 'Introduction' });
  let s = pres.addSlide({ sectionTitle: 'Introduction' });
  s.background = { color: WHITE };
  s.addText('EC8208 SOFTWARE ARCHITECTURE · CONTINUOUS ASSESSMENT 01', { x: 0.7, y: 0.8, w: 8.6, h: 0.3, fontFace: BODY, fontSize: 12, bold: true, color: ACCENT, charSpacing: 3, margin: 0, isTextBox: true });
  s.addText('Renewable Energy Operations, Asset and Grid Management System', { x: 0.7, y: 1.2, w: 8.6, h: 1.9, fontFace: HEAD, fontSize: 36, bold: true, color: NAVY, margin: 0, valign: 'top', isTextBox: true });
  s.addText('A microservices and event-driven platform for distributed renewable energy facilities', { x: 0.7, y: 3.5, w: 8.6, h: 0.5, fontFace: BODY, fontSize: 18, color: INK, margin: 0, isTextBox: true });
  s.addText('Department of Electrical and Information Engineering\nUniversity of Ruhuna', { x: 0.7, y: 4.4, w: 8.6, h: 0.7, fontFace: BODY, fontSize: 14, color: MUTED, margin: 0, isTextBox: true });
  s.addNotes('Good morning. We are presenting REOAGMS, a microservices and event-driven platform for monitoring and maintaining distributed renewable energy facilities. Six of us each built a service, and we integrated them as one system.');

  // Team overview
  s = pres.addSlide({ masterName: 'CONTENT', sectionTitle: 'Introduction' });
  s.addText('THE TEAM', { x: 0.5, y: 0.3, w: 9, h: 0.25, fontFace: BODY, fontSize: 12, bold: true, color: ACCENT, charSpacing: 3, margin: 0, isTextBox: true });
  s.addText('Who built what', { x: 0.5, y: 0.55, w: 9, h: 0.6, fontFace: HEAD, fontSize: 30, bold: true, color: NAVY, margin: 0, isTextBox: true });
  const head = (t) => ({ text: t, options: { bold: true, color: WHITE, fill: { color: NAVY } } });
  const team = [
    ['Charith', 'Architecture, Identity, API Gateway, console, Docker, RabbitMQ, Kubernetes, integration'],
    ['Nemal', 'Asset Service: facilities, assets, sensors, images, search'],
    ['Govindu', 'Monitoring Service: readings, telemetry, alert rules, alerts'],
    ['Thurunu', 'Maintenance Service: work orders, tasks, technicians, history'],
    ['Yasiru', 'Notification Service: email, SMS, templates, retry, consumer'],
    ['Sachinthana', 'Analytics Service: KPIs, reports, export'],
    ['All members', 'Tests, API documentation, validation, error handling'],
  ];
  s.addTable([[head('Member'), head('Responsibility')]].concat(team.map(([a, b], i) => [
    { text: a, options: { bold: true, color: NAVY, fill: { color: i % 2 ? WHITE : TINT } } },
    { text: b, options: { color: INK, fill: { color: i % 2 ? WHITE : TINT } } }])),
  { x: 0.5, y: 1.3, w: 9, colW: [1.8, 7.2], rowH: 0.42, fontFace: BODY, fontSize: 14, border: { type: 'solid', color: LINE, pt: 0.75 }, valign: 'middle', margin: [0.03, 0.12, 0.03, 0.12] });
  s.addNotes('Here is who built what. Charith owns the architecture, identity, gateway and integration. Nemal built the Asset Service, Govindu the Monitoring Service, Thurunu the Maintenance Service, Yasiru the Notification Service and Sachinthana the Analytics Service. All of us wrote the tests, documentation, validation and error handling for our own services.');

  for (const m of MEMBERS) addMember(pres, m);

  // Closing
  pres.addSection({ title: 'Conclusion' });
  s = pres.addSlide({ sectionTitle: 'Conclusion' });
  s.background = { color: WHITE };
  s.addText('Thank you', { x: 0.7, y: 1.6, w: 8.6, h: 1.0, fontFace: HEAD, fontSize: 48, bold: true, color: NAVY, margin: 0, isTextBox: true });
  s.addText('Questions and discussion', { x: 0.7, y: 2.7, w: 8.6, h: 0.5, fontFace: BODY, fontSize: 22, color: INK, margin: 0, isTextBox: true });
  s.addText('REOAGMS · EC8208 Software Architecture · University of Ruhuna', { x: 0.7, y: 4.4, w: 8.6, h: 0.4, fontFace: BODY, fontSize: 14, color: MUTED, margin: 0, isTextBox: true });
  s.addNotes('Thank you for listening. We are happy to answer your questions.');

  await pres.writeFile({ fileName: path.join(OUT, 'REOAGMS-Team-Presentation.pptx') });
  console.log('built single team deck');

  // Word-for-word narration for the script PDF
  let md = '';
  for (const m of MEMBERS) {
    md += `\n### ${m.name}\n\n`;
    m.notes.forEach((t, i) => { md += `**Slide ${i + 1}.** ${t}\n\n`; });
  }
  fs.writeFileSync(path.join(__dirname, 'src', 'narration.md'), md);
  const segs = MEMBERS.flatMap((m) => m.live.map((l) => ({ ...l, name: m.name }))).sort((a, b) => a.time.localeCompare(b.time, undefined, { numeric: true }));
  let live = '';
  const NL = '\n';
  for (const g of segs) live += `### ${g.time} · ${g.name}${NL}${NL}*On screen:* ${g.screen}${NL}${NL}**${g.name} says:** ${g.say}${NL}${NL}`;
  live += `### 4:50–5:00 · All members${NL}${NL}**All say:** Thank you.${NL}${NL}`;
  fs.writeFileSync(path.join(__dirname, 'src', 'live.md'), live);
})();

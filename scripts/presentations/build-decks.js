// Builds the single 10-slide team deck (deliverables/REOAGMS-Team-Presentation.pptx)
// and src/live.md, the word-for-word script, from the same data so they never drift.
// Run: node scripts/presentations/build-decks.js   (needs pptxgenjs)
const path = require('path');
const fs = require('fs');
const pptxgen = require('pptxgenjs');

const OUT = path.join(__dirname, '..', '..', 'deliverables');
fs.mkdirSync(OUT, { recursive: true });

const BG = '0A1020', PANEL = '121B33', EDGE = '24314F', TEXT = 'F1F5FB', MUTED = '8C9BB8', CYAN = '22D3EE', LIME = 'A3E635';
const FONT = 'Segoe UI', FONT_B = 'Segoe UI Semibold';
const COURSE = 'EC8208 Software Architecture  ·  University of Ruhuna';

// Each service slide: flow (3-4 steps), stats (3), live cue, spoken words.
const SLIDES = [
  {
    kind: 'title', time: '0:00–0:10', who: 'Charith', screen: 'Title slide',
    say: 'Good morning. We are presenting REOAGMS, a platform that monitors and maintains renewable energy assets in real time.',
  },
  {
    kind: 'arch', time: '0:10–0:45', who: 'Charith', screen: 'Console: sign in as admin, then the Control room',
    say: 'REOAGMS is six Spring Boot microservices behind one API gateway, connected by RabbitMQ events, each with its own PostgreSQL database. I sign in as the administrator; the gateway validates my token and role. This is the control room: live generation, open alerts, device health and an hourly energy curve. Nemal will show where this data comes from.',
  },
  {
    kind: 'svc', n: '03', who: 'Nemal', name: 'Asset Service', time: '0:45–1:15', screen: 'Assets page: switch tabs, open Add, then cancel',
    head: 'The registry every service relies on',
    flow: [['Facility', '5 types'], ['Asset', '7 types'], ['Sensor', '9 metrics']],
    stats: [['5 · 11 · 32', 'facilities · assets · sensors'], ['Server-side', 'search and paging'], ['Validated', 'every form']],
    say: 'Thank you, Charith. The Asset Service owns the registry: five facilities, eleven assets and thirty-two sensors across solar, wind, hydro, battery and tidal. Lists are searched and paged on the server, and every form is validated, so a blank name is rejected. Only administrators and managers can change data. Every other service refers to these assets by ID. Govindu will show them producing live data.',
  },
  {
    kind: 'svc', n: '04', who: 'Govindu', name: 'Monitoring Service', time: '1:15–2:00', screen: 'Live monitoring page, then the toast and the Alerts page',
    head: 'From a sensor reading to an instant alert',
    flow: [['Reading', 'every few seconds'], ['Rule check', 'threshold'], ['Alert', 'one per asset'], ['RabbitMQ', 'event']],
    stats: [['16,000+', 'readings stored'], ['7', 'alert rules'], ['10 s', 'Redis cached dashboard']],
    say: 'Thanks, Nemal. The Monitoring Service receives a reading from every sensor and checks it against our alert rules. This tile is a live sensor and the dashed line is its threshold. The simulator has just injected a fault, and here is the alert, instantly. One alert is stored, one event goes to RabbitMQ, and the browser is updated over WebSocket. That same event is now reaching Thurunu.',
  },
  {
    kind: 'svc', n: '05', who: 'Thurunu', name: 'Maintenance Service', time: '2:00–2:35', screen: 'Maintenance board: newest card, Assign, Start, Complete',
    head: 'Faults become tracked work automatically',
    flow: [['Alert event', 'high or critical'], ['Work order', 'created'], ['Technician', 'assigned'], ['Completed', 'in history']],
    stats: [['0 clicks', 'to open a corrective order'], ['4', 'technicians'], ['1', 'active order per asset']],
    say: 'Yes. The Maintenance Service received the same event and, because the severity is high, it opened a corrective work order by itself. Here it is on the board. I assign a technician, start the work and complete it. Each step is its own endpoint, and the finished job is saved to the maintenance history. Yasiru will show how people are informed.',
  },
  {
    kind: 'svc', n: '06', who: 'Yasiru', name: 'Notification Service', time: '2:35–3:05', screen: 'Notifications page',
    head: 'Every alert delivered and recorded',
    flow: [['Queue', 'durable'], ['Consumer', 'schema check'], ['Channels', 'email · push'], ['History', 'stored']],
    stats: [['Instant', 'in-app push'], ['3', 'automatic retries'], ['100%', 'deliveries recorded']],
    say: 'The Notification Service read the same alert from its own RabbitMQ queue. In this history, the in-app push was delivered instantly, and every email attempt is stored with its status, retry count and reason. A scheduler retries up to three times, I can retry any message here, and failing messages go to a dead-letter queue, so nothing is lost. Sachinthana will turn all this into reports.',
  },
  {
    kind: 'svc', n: '07', who: 'Sachinthana', name: 'Analytics Service', time: '3:05–3:45', screen: 'Analytics page: pick a facility, Generate, then export CSV',
    head: 'Trustworthy energy figures and reports',
    flow: [['Source APIs', 'REST only'], ['Calculation', 'power to kWh'], ['Reports', 'stored'], ['CSV', 'export']],
    stats: [['kWh', 'integrated from power'], ['6', 'KPIs with targets'], ['5 + 5', 'energy and performance reports']],
    say: 'The Analytics Service calls the other services over REST and never touches their databases. For this facility it integrates average power into kilowatt-hours for each hour, and calculates availability, efficiency and capacity factor. I generate an energy report for the last day and export it as CSV for management. Executives see all of this read-only. Charith will close with security.',
  },
  {
    kind: 'rbac', time: '3:45–4:15', who: 'Charith', screen: 'Sign out, sign in as executive@reoagms.com, show the read-only pages',
    say: 'Security is enforced at the gateway. Five roles carry different rights. I sign in as the executive: there are no action buttons, and the API itself answers 403 for any write. Every login and user change is recorded in the audit log. Quality is built in as well, with sixty test methods across the services.',
  },
  {
    kind: 'ops', time: '4:15–4:50', who: 'Charith', screen: 'Grafana tab, then back to the slide',
    say: 'Operations are visible in Grafana, where Prometheus scrapes all seven services and shows platform health. Behind the demo are sixty test methods, validation and error handling in every service, and a versioned event contract that keeps the services compatible. The whole system starts with one docker compose command.',
  },
  {
    kind: 'end', time: '4:50–5:00', who: 'All members', screen: 'Thank you slide',
    say: 'Thank you. We are happy to take your questions.',
  },
];

const ARCH_SERVICES = ['Identity', 'Asset', 'Monitoring', 'Maintenance', 'Notification', 'Analytics'];
const ROLES = [['Administrator', 'Everything, users and audit log'], ['Operations Manager', 'Assets, rules, work orders, reports'], ['Plant Operator', 'Monitor and handle alerts'], ['Maintenance Engineer', 'Assign and complete work orders'], ['Executive', 'Read-only dashboards and reports']];
const OPS = [['60', 'test methods'], ['7', 'services monitored'], ['19', 'containers'], ['1', 'command to start']];

function frame(pres, s, kicker, title, n, time) {
  s.background = { color: BG };
  s.addText(kicker, { x: 0.6, y: 0.4, w: 7, h: 0.3, fontFace: FONT_B, fontSize: 11, color: CYAN, charSpacing: 4, margin: 0, isTextBox: true });
  s.addText(title, { x: 0.6, y: 0.75, w: 8.8, h: 0.7, fontFace: FONT_B, fontSize: 30, color: TEXT, margin: 0, isTextBox: true });
  s.addText(COURSE, { x: 0.6, y: 5.2, w: 6, h: 0.25, fontFace: FONT, fontSize: 9, color: MUTED, margin: 0, isTextBox: true });
  s.addText(`${n}  ·  ${time}`, { x: 7.4, y: 5.2, w: 2, h: 0.25, fontFace: FONT, fontSize: 9, color: MUTED, align: 'right', margin: 0, isTextBox: true });
}

function liveChip(pres, s, text) {
  s.addShape(pres.ShapeType.roundRect, { x: 0.6, y: 4.72, w: 4.6, h: 0.36, fill: { color: PANEL }, line: { color: CYAN, width: 0.75 }, rectRadius: 0.18 });
  s.addText([{ text: 'LIVE  ', options: { bold: true, color: CYAN } }, { text, options: { color: TEXT } }], { x: 0.78, y: 4.72, w: 4.4, h: 0.36, fontFace: FONT, fontSize: 11, valign: 'middle', margin: 0, isTextBox: true });
}

(async () => {
  const pres = new pptxgen();
  pres.layout = 'LAYOUT_16x9';
  pres.title = 'REOAGMS: Team Presentation';
  pres.author = 'REOAGMS project team';
  pres.company = 'University of Ruhuna';
  pres.theme = { headFontFace: FONT_B, bodyFontFace: FONT };

  SLIDES.forEach((d, idx) => {
    const num = String(idx + 1).padStart(2, '0');
    const s = pres.addSlide();
    s.addNotes(d.who + ' (' + d.time + '): ' + d.say);

    if (d.kind === 'title') {
      s.background = { color: BG };
      s.addText('EC8208  ·  SOFTWARE ARCHITECTURE', { x: 0.7, y: 0.9, w: 8.6, h: 0.3, fontFace: FONT_B, fontSize: 12, color: CYAN, charSpacing: 5, margin: 0, isTextBox: true });
      s.addText('REOAGMS', { x: 0.7, y: 1.4, w: 8.6, h: 1.2, fontFace: FONT_B, fontSize: 66, color: TEXT, margin: 0, isTextBox: true });
      s.addText('Renewable Energy Operations, Asset and Grid Management System', { x: 0.7, y: 2.65, w: 8.6, h: 0.5, fontFace: FONT, fontSize: 20, color: MUTED, margin: 0, isTextBox: true });
      s.addText('Charith  ·  Nemal  ·  Govindu  ·  Thurunu  ·  Yasiru  ·  Sachinthana', { x: 0.7, y: 4.35, w: 8.6, h: 0.35, fontFace: FONT_B, fontSize: 14, color: TEXT, margin: 0, isTextBox: true });
      s.addText('Department of Electrical and Information Engineering  ·  University of Ruhuna', { x: 0.7, y: 4.75, w: 8.6, h: 0.3, fontFace: FONT, fontSize: 11, color: MUTED, margin: 0, isTextBox: true });
    } else if (d.kind === 'arch') {
      frame(pres, s, '02 · CHARITH · ARCHITECTURE', 'One gateway, six independent services', num, d.time);
      s.addShape(pres.ShapeType.roundRect, { x: 0.6, y: 1.65, w: 8.8, h: 0.5, fill: { color: PANEL }, line: { color: EDGE, width: 0.75 }, rectRadius: 0.08 });
      s.addText('React console  ·  nginx  ·  live updates over WebSocket', { x: 0.6, y: 1.65, w: 8.8, h: 0.5, fontFace: FONT, fontSize: 13, color: TEXT, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      s.addShape(pres.ShapeType.roundRect, { x: 0.6, y: 2.35, w: 8.8, h: 0.5, fill: { color: CYAN }, line: { color: CYAN, width: 0.75 }, rectRadius: 0.08 });
      s.addText('API Gateway  ·  JWT validation  ·  role policy', { x: 0.6, y: 2.35, w: 8.8, h: 0.5, fontFace: FONT_B, fontSize: 14, color: BG, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      const w = (8.8 - 5 * 0.12) / 6;
      ARCH_SERVICES.forEach((name, i) => {
        const x = 0.6 + i * (w + 0.12);
        s.addShape(pres.ShapeType.roundRect, { x, y: 3.05, w, h: 0.85, fill: { color: PANEL }, line: { color: EDGE, width: 0.75 }, rectRadius: 0.08 });
        s.addText(name, { x, y: 3.1, w, h: 0.45, fontFace: FONT_B, fontSize: 11, color: TEXT, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
        s.addText('PostgreSQL', { x, y: 3.5, w, h: 0.3, fontFace: FONT, fontSize: 9, color: MUTED, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      });
      s.addShape(pres.ShapeType.roundRect, { x: 0.6, y: 4.1, w: 8.8, h: 0.5, fill: { color: PANEL }, line: { color: LIME, width: 0.75 }, rectRadius: 0.08 });
      s.addText('RabbitMQ events  ·  Redis cache  ·  Prometheus and Grafana', { x: 0.6, y: 4.1, w: 8.8, h: 0.5, fontFace: FONT, fontSize: 13, color: TEXT, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      liveChip(pres, s, d.screen.split(':')[0] + ': sign in, Control room');
    } else if (d.kind === 'svc') {
      frame(pres, s, `${d.n} · ${d.who.toUpperCase()} · ${d.name.toUpperCase()}`, d.head, num, d.time);
      const n = d.flow.length, gap = 0.3, bw = (8.8 - gap * (n - 1)) / n;
      d.flow.forEach(([t, sub], i) => {
        const x = 0.6 + i * (bw + gap);
        s.addShape(pres.ShapeType.roundRect, { x, y: 1.75, w: bw, h: 1.0, fill: { color: i === 0 ? CYAN : PANEL }, line: { color: i === 0 ? CYAN : EDGE, width: 0.75 }, rectRadius: 0.08 });
        s.addText(t, { x, y: 1.82, w: bw, h: 0.5, fontFace: FONT_B, fontSize: 15, color: i === 0 ? BG : TEXT, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
        s.addText(sub, { x, y: 2.28, w: bw, h: 0.35, fontFace: FONT, fontSize: 11, color: i === 0 ? BG : MUTED, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
        if (i < n - 1) s.addText('›', { x: x + bw, y: 1.95, w: gap, h: 0.6, fontFace: FONT_B, fontSize: 24, color: CYAN, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      });
      const sw = (8.8 - 2 * 0.3) / 3;
      d.stats.forEach(([big, label], i) => {
        const x = 0.6 + i * (sw + 0.3);
        s.addShape(pres.ShapeType.line, { x, y: 3.2, w: sw, h: 0, line: { color: EDGE, width: 1 } });
        s.addText(big, { x, y: 3.3, w: sw, h: 0.7, fontFace: FONT_B, fontSize: big.length > 8 ? 24 : 32, color: CYAN, valign: 'middle', margin: 0, isTextBox: true });
        s.addText(label, { x, y: 4.0, w: sw, h: 0.35, fontFace: FONT, fontSize: 12, color: MUTED, valign: 'top', margin: 0, isTextBox: true });
      });
      liveChip(pres, s, d.screen.split(':')[0].split(',')[0]);
    } else if (d.kind === 'rbac') {
      frame(pres, s, '08 · CHARITH · SECURITY', 'Five roles, enforced at the gateway', num, d.time);
      ROLES.forEach(([role, can], i) => {
        const y = 1.7 + i * 0.5;
        s.addShape(pres.ShapeType.roundRect, { x: 0.6, y, w: 8.8, h: 0.42, fill: { color: PANEL }, line: { color: EDGE, width: 0.75 }, rectRadius: 0.06 });
        s.addText(role, { x: 0.8, y, w: 3.0, h: 0.42, fontFace: FONT_B, fontSize: 13, color: CYAN, valign: 'middle', margin: 0, isTextBox: true });
        s.addText(can, { x: 3.9, y, w: 5.4, h: 0.42, fontFace: FONT, fontSize: 13, color: TEXT, valign: 'middle', margin: 0, isTextBox: true });
      });
      liveChip(pres, s, 'executive sign-in, read-only');
    } else if (d.kind === 'ops') {
      frame(pres, s, '09 · CHARITH · OPERATIONS AND QUALITY', 'Observable, tested, one command to run', num, d.time);
      const sw = (8.8 - 3 * 0.3) / 4;
      OPS.forEach(([big, label], i) => {
        const x = 0.6 + i * (sw + 0.3);
        s.addShape(pres.ShapeType.line, { x, y: 2.0, w: sw, h: 0, line: { color: EDGE, width: 1 } });
        s.addText(big, { x, y: 2.15, w: sw, h: 0.9, fontFace: FONT_B, fontSize: 46, color: i === 3 ? LIME : CYAN, valign: 'middle', margin: 0, isTextBox: true });
        s.addText(label, { x, y: 3.1, w: sw, h: 0.35, fontFace: FONT, fontSize: 13, color: MUTED, margin: 0, isTextBox: true });
      });
      liveChip(pres, s, 'Grafana platform health');
    } else {
      s.background = { color: BG };
      s.addText('Thank you', { x: 0.7, y: 1.7, w: 8.6, h: 1.2, fontFace: FONT_B, fontSize: 60, color: TEXT, margin: 0, isTextBox: true });
      s.addText('Questions and discussion', { x: 0.7, y: 3.0, w: 8.6, h: 0.5, fontFace: FONT, fontSize: 22, color: CYAN, margin: 0, isTextBox: true });
      s.addText('REOAGMS  ·  EC8208 Software Architecture  ·  University of Ruhuna', { x: 0.7, y: 4.7, w: 8.6, h: 0.3, fontFace: FONT, fontSize: 11, color: MUTED, margin: 0, isTextBox: true });
    }
  });

  await pres.writeFile({ fileName: path.join(OUT, 'REOAGMS-Team-Presentation.pptx') });
  console.log('built deck:', SLIDES.length, 'slides');

  // Word-for-word script, separated per member
  const words = SLIDES.reduce((a, d) => a + d.say.split(/\s+/).length, 0);
  const order = [...new Set(SLIDES.map((d) => d.who))];
  let md = `## Who does what\n\n| Member | Slides | Time on stage | Shows in the live demo |\n|---|---|---|---|\n`;
  const secs = (tm) => tm.split('–').map((x) => { const [m, s] = x.split(':'); return +m * 60 + +s; });
  order.forEach((who) => {
    const mine = SLIDES.map((d, i) => ({ d, n: i + 1 })).filter((x) => x.d.who === who);
    const total = mine.reduce((a, x) => { const [s, e] = secs(x.d.time); return a + e - s; }, 0);
    const SHOWS = { Charith: 'Login and Control room; executive read-only sign-in; Grafana', Nemal: 'Assets page', Govindu: 'Live monitoring and the alert', Thurunu: 'Maintenance board', Yasiru: 'Notifications page', Sachinthana: 'Analytics report and CSV export', 'All members': 'Closing thank-you' };
    const shows = SHOWS[who];
    md += `| **${who}** | ${mine.map((x) => x.n).join(', ')} | ${total} s | ${shows} |\n`;
  });
  md += `\nTotal spoken text: about ${words} words, under 5 minutes at a calm pace, with a few seconds spare for clicks. Each member's part below is complete: read only your own section.\n\n## The exact words, per member\n\n`;
  order.forEach((who) => {
    const mine = SLIDES.map((d, i) => ({ d, n: i + 1 })).filter((x) => x.d.who === who);
    md += `# ${who}\n\n`;
    mine.forEach(({ d, n }) => {
      md += `### Slide ${n} · ${d.time}\n\n*On screen:* ${d.screen}\n\n**${who} says:** ${d.say}\n\n`;
    });
  });
  fs.writeFileSync(path.join(__dirname, 'src', 'live.md'), md);
  console.log('words', words);
})();

// Builds the single 10-slide team deck (deliverables/REOAGMS-Team-Presentation.pptx)
// and src/live.md, the word-for-word script, from the same data so they never drift.
// Run: node scripts/presentations/build-decks.js   (needs pptxgenjs)
const path = require('path');
const fs = require('fs');
const pptxgen = require('pptxgenjs');

const OUT = path.join(__dirname, '..', '..', 'deliverables');
fs.mkdirSync(OUT, { recursive: true });

// Black-to-electric-blue gradient (assets/*.png from make-backgrounds.py) with white split slides.
const BLACK = '05060C', BLUE = '2B3DFF', BLUE_L = '8FA0FF', WHITE = 'FFFFFF', GREY = '5B6478', SOFT = 'F0F2F9', PALE = 'C9D1FF';
const ASSETS = path.join(__dirname, 'assets');
const FONT = 'Segoe UI Light', FONT_R = 'Segoe UI', FONT_B = 'Segoe UI Semibold';
const COURSE = 'EC8208 Software Architecture  ·  University of Ruhuna';

// Each service slide: flow (3-4 steps), stats (3), live cue, spoken words.
const SLIDES = [
  {
    kind: 'title', time: '0:00–0:15', who: 'Charith', screen: 'Title slide',
    say: 'Good morning, everyone. We are Group 9, and we are presenting REOAGMS, the Renewable Energy Operations, Asset and Grid Management System. It monitors and maintains renewable energy assets in real time.',
  },
  {
    kind: 'arch', time: '0:15–1:13', who: 'Charith', screen: 'Console: sign in as admin, then the Control room, pointing at the KPIs, the energy curve and the alert feed',
    say: 'REOAGMS is six Spring Boot microservices behind one API gateway: identity, asset, monitoring, maintenance, notification and analytics. Each service owns its own PostgreSQL database and never reads another\'s, and they talk to each other through RabbitMQ events. The React console reaches everything through the gateway, which checks the token and the role on every request. I sign in as the administrator, and this is the control room: live generation, open alerts, device health and an hourly energy curve, all coming from real data in the databases. Nemal will show where that data comes from.',
  },
  {
    kind: 'svc', n: '03', who: 'Nemal', name: 'Asset Service', time: '1:13–2:08', screen: 'Assets page: switch the three tabs, search, open Add, submit blank to show validation, then cancel',
    head: 'The registry every service relies on',
    flow: [['Facility', '5 types'], ['Asset', '7 types'], ['Sensor', '9 metrics']],
    stats: [['5 · 11 · 32', 'facilities · assets · sensors'], ['Server-side', 'search and paging'], ['Validated', 'every form']],
    say: 'Thank you, Charith. The Asset Service owns the registry of everything we monitor. As you can see, there are five facilities, eleven assets and thirty-two sensors, covering solar, wind, hydro, battery storage and tidal. I switch between the facility, asset and sensor tabs, and search runs on the server with paging, so lists stay fast. Now I open the add form and leave the name blank: the form rejects it, because every input is validated. Other services refer to these assets only by ID. Govindu will now show these assets producing live data.',
  },
  {
    kind: 'svc', n: '04', who: 'Govindu', name: 'Monitoring Service', time: '2:08–3:46', screen: 'Live monitoring page (tiles and dashed threshold), wait for the toast, then the Alerts page: acknowledge the alert',
    head: 'From a sensor reading to an instant alert',
    flow: [['Reading', 'every few seconds'], ['Rule check', 'threshold'], ['Alert', 'one per asset'], ['RabbitMQ', 'event']],
    stats: [['16,000+', 'readings stored'], ['7', 'alert rules'], ['10 s', 'Redis cached dashboard']],
    say: 'Thanks, Nemal. The Monitoring Service receives a reading from every sensor every few seconds and keeps the history: more than sixteen thousand readings are already stored. On the live monitoring page each tile is a real sensor, and the dashed line is its alert threshold. Every reading is checked against our seven alert rules. The simulator has just injected a fault, and here is the toast, instantly. On the Alerts page you can see the alert with its severity, the asset and the value that broke the threshold. Behind this, one alert is stored, and we keep only one open alert per rule and asset, so operators are never flooded. One event is published to RabbitMQ, and the Notification Service pushes it to this browser over WebSocket. The dashboard totals are cached in Redis for ten seconds, and if Redis ever stops, PostgreSQL answers instead. That same event is now reaching Thurunu\'s service.',
  },
  {
    kind: 'svc', n: '05', who: 'Thurunu', name: 'Maintenance Service', time: '3:46–5:24', screen: 'Maintenance board: newest card, Assign a technician, Start, Complete, then show the history',
    head: 'Faults become tracked work automatically',
    flow: [['Alert event', 'high or critical'], ['Work order', 'created'], ['Technician', 'assigned'], ['Completed', 'in history']],
    stats: [['0 clicks', 'to open a corrective order'], ['4', 'technicians'], ['1', 'active order per asset']],
    say: 'Yes. The Maintenance Service listens on its own RabbitMQ queue, received the same event and, because the severity is high, it opened a corrective work order by itself, with no one clicking anything. Here it is on the board, in the Created column. It stores the alert ID, so a repeated message never creates a duplicate, and it keeps one active corrective order per asset. Now I assign one of our four technicians. The card moves to Assigned. I start the work, and it moves to In progress. Then I complete it. Each step is its own endpoint, and the finished job is written to the maintenance history for that asset. So a fault becomes tracked, auditable work from the first reading to the last repair. Yasiru will show how people are informed.',
  },
  {
    kind: 'svc', n: '06', who: 'Yasiru', name: 'Notification Service', time: '5:24–7:02', screen: 'Notifications page: point at PUSH delivered, open a message, use Retry on one entry',
    head: 'Every alert delivered and recorded',
    flow: [['Queue', 'durable'], ['Consumer', 'schema check'], ['Channels', 'email · push'], ['History', 'stored']],
    stats: [['Instant', 'in-app push'], ['3', 'automatic retries'], ['100%', 'deliveries recorded']],
    say: 'The Notification Service read the same alert from its own durable RabbitMQ queue, so it never depends on the Monitoring Service being online. The consumer checks the event schema, then sends the message on each channel. In this history, the in-app push shows as delivered, and it reached this browser instantly. Every email attempt is stored with its status, its retry count and the reason, so nothing is hidden. If delivery fails, a scheduler retries up to three times, sixty seconds apart, and I can also retry any message from this page. Messages that keep failing go to a dead-letter queue, so no alert is ever lost. Email settings come from environment variables, so no credentials are in the code. Sachinthana will now turn all this data into reports.',
  },
  {
    kind: 'svc', n: '07', who: 'Sachinthana', name: 'Analytics Service', time: '7:02–8:40', screen: 'Analytics page: pick a facility, show the hourly energy chart and KPIs, Generate an energy report, click the CSV export',
    head: 'Trustworthy energy figures and reports',
    flow: [['Source APIs', 'REST only'], ['Calculation', 'power to kWh'], ['Reports', 'stored'], ['CSV', 'export']],
    stats: [['kWh', 'integrated from power'], ['6', 'KPIs with targets'], ['5 + 5', 'energy and performance reports']],
    say: 'The Analytics Service calls the other services over REST and never touches their databases. For this facility, I pick it from the selector, and the chart shows hourly energy. Our figures are true kilowatt-hours: the service takes average power for each hour and integrates it over time. It also calculates availability, efficiency, capacity factor and utilisation, and compares the energy with what the rated capacity should have produced. Now I generate an energy report for the last day. It is stored, and I export it as CSV for management. We also keep six KPIs with targets, dashboards and performance reports. Executives see all of this read-only, and Nemal will show exactly who is allowed to see what.',
  },
  {
    kind: 'rbac', time: '8:40–9:23', who: 'Nemal', screen: 'Sign out, sign in as executive@reoagms.com (read-only pages), then optionally operator@reoagms.com',
    say: 'Access is enforced at the gateway, and five roles carry different rights, as listed on this slide. I sign out and sign in as the executive. Every page is visible, but there are no action buttons, and the API itself returns forbidden for any write. Now the operator: it can handle alerts, but cannot create a facility. Passwords are stored as BCrypt hashes, and every login and user change is written to the audit log, which the administrator can review. Charith will close.',
  },
  {
    kind: 'ops', time: '9:23–9:48', who: 'Charith', screen: 'Grafana tab (REOAGMS Platform Health), then back to the slide',
    say: 'Finally, operations. Grafana shows platform health from Prometheus, which scrapes all seven services. Sixty test methods protect the services, validation and error handling are built into every one of them, and one docker compose command starts all nineteen containers.',
  },
  {
    kind: 'end', time: '9:48–10:00', who: 'All members', screen: 'Thank you slide',
    say: 'Thank you very much. We are happy to take your questions.',
  },
];

const GROUP = [['EG/2021/4707', 'Perera G C M'], ['EG/2021/4620', 'Kularathne MDYB'], ['EG/2021/4710', 'Perera K P D N'], ['EG/2021/4828', 'Thejana BPGL'], ['EG/2021/4701', 'Pabasara PMGT'], ['EG/2021/4770', 'Sachinthana K AD']];
const ARCH_SERVICES = ['Identity', 'Asset', 'Monitoring', 'Maintenance', 'Notification', 'Analytics'];
const ROLES = [['Administrator', 'Everything, users and audit log'], ['Operations Manager', 'Assets, rules, work orders, reports'], ['Plant Operator', 'Monitor and handle alerts'], ['Maintenance Engineer', 'Assign and complete work orders'], ['Executive', 'Read-only dashboards and reports']];
const OPS = [['60', 'test methods'], ['7', 'services monitored'], ['19', 'containers'], ['1', 'command to start']];

// Logo mark, menu dots and a corner triangle, drawn in the zone's ink colour.
function chrome(pres, s, onDark, corner) {
  const c = onDark ? WHITE : BLACK;
  [0, 1, 2].forEach((i) => s.addShape(pres.ShapeType.rect, { x: 0.6 + i * 0.1, y: 0.26, w: 0.07, h: 0.17, fill: { color: c }, line: { color: c, width: 0 } }));
  [0, 1, 2].forEach((i) => s.addShape(pres.ShapeType.ellipse, { x: 9.12 + i * 0.12, y: 0.32, w: 0.06, h: 0.06, fill: { color: c }, line: { color: c, width: 0 } }));
  if (corner !== false) s.addShape(pres.ShapeType.rtTriangle, { x: 9.24, y: 0.66, w: 0.16, h: 0.16, rotate: 180, fill: { color: c }, line: { color: c, width: 0 } });
}

// Title with its last words in the accent colour (the text itself is untouched).
function titleRuns(title, onDark) {
  const w = title.split(' ');
  const k = w.length >= 5 ? 3 : Math.max(1, Math.min(2, w.length - 1));
  return [
    { text: w.slice(0, w.length - k).join(' ') + ' ', options: { color: onDark ? WHITE : BLACK } },
    { text: w.slice(w.length - k).join(' '), options: { color: onDark ? BLUE_L : BLUE } },
  ];
}

function frame(pres, s, kicker, title, n, time, mode, big) {
  const dark = mode === 'dark';
  s.background = { path: path.join(ASSETS, dark ? 'dark.png' : 'split.png') };
  chrome(pres, s, dark && true, true);
  const ky = big ? 1.1 : 0.6, ty = big ? 1.4 : 0.9;
  s.addText(kicker, { x: 0.6, y: ky, w: 8.8, h: 0.24, fontFace: FONT_B, fontSize: 10.5, color: dark ? BLUE_L : BLUE, charSpacing: 3, margin: 0, isTextBox: true });
  s.addText(titleRuns(title, dark), { x: 0.6, y: ty, w: 8.8, h: 0.75, fontFace: FONT, fontSize: big ? 34 : 32, margin: 0, isTextBox: true });
  s.addShape(pres.ShapeType.line, { x: 0.6, y: 5.12, w: 8.8, h: 0, line: { color: WHITE, width: 0.75, transparency: 75 } });
  s.addText(COURSE, { x: 0.6, y: 5.2, w: 6, h: 0.25, fontFace: FONT_R, fontSize: 9, color: PALE, margin: 0, isTextBox: true });
  s.addText(`${n}  ·  ${time}`, { x: 7.4, y: 5.2, w: 2, h: 0.25, fontFace: FONT_R, fontSize: 9, color: PALE, align: 'right', margin: 0, isTextBox: true });
}

function glass(pres, s, x, y, w, h, solid) {
  s.addShape(pres.ShapeType.roundRect, solid
    ? { x, y, w, h, fill: { color: BLUE }, line: { color: BLUE, width: 0.75 }, rectRadius: 0.06 }
    : { x, y, w, h, fill: { color: WHITE, transparency: 90 }, line: { color: WHITE, width: 0.75, transparency: 70 }, rectRadius: 0.06 });
}

function liveChip(pres, s, text) {
  s.addShape(pres.ShapeType.roundRect, { x: 0.6, y: 4.7, w: 4.6, h: 0.36, fill: { color: WHITE, transparency: 88 }, line: { color: WHITE, width: 0.75, transparency: 70 }, rectRadius: 0.18 });
  s.addText([{ text: 'LIVE  ', options: { bold: true, color: BLUE_L } }, { text, options: { color: WHITE } }], { x: 0.8, y: 4.7, w: 4.3, h: 0.36, fontFace: FONT_R, fontSize: 11, valign: 'middle', margin: 0, isTextBox: true });
}

function stat(pres, s, x, y, w, big, label, size, color) {
  s.addShape(pres.ShapeType.line, { x, y, w: 0.5, h: 0, line: { color: BLUE_L, width: 2.25 } });
  s.addText(big, { x, y: y + 0.1, w, h: 0.8, fontFace: FONT, fontSize: size, color: color || WHITE, valign: 'middle', margin: 0, isTextBox: true });
  s.addText(label, { x, y: y + 0.9, w, h: 0.32, fontFace: FONT_R, fontSize: 12, color: PALE, valign: 'top', margin: 0, isTextBox: true });
}

(async () => {
  const pres = new pptxgen();
  pres.layout = 'LAYOUT_16x9';
  pres.title = 'REOAGMS: Team Presentation';
  pres.author = 'REOAGMS project team';
  pres.company = 'University of Ruhuna';
  pres.theme = { headFontFace: FONT_B, bodyFontFace: FONT_R };

  SLIDES.forEach((d, idx) => {
    const num = String(idx + 1).padStart(2, '0');
    const s = pres.addSlide();
    s.addNotes(d.who + ' (' + d.time + '): ' + d.say);

    if (d.kind === 'title' || d.kind === 'end') {
      s.background = { path: path.join(ASSETS, 'dark.png') };
      chrome(pres, s, true, true);
    }
    if (d.kind === 'title') {
      s.addText('GROUP 9  ·  EC8208  ·  SOFTWARE ARCHITECTURE', { x: 0.7, y: 0.9, w: 8.6, h: 0.3, fontFace: FONT_B, fontSize: 12, color: BLUE_L, charSpacing: 5, margin: 0, isTextBox: true });
      s.addText('REOAGMS', { x: 0.7, y: 1.35, w: 8.6, h: 1.2, fontFace: FONT_R, fontSize: 66, color: WHITE, margin: 0, isTextBox: true });
      s.addText('Renewable Energy Operations, Asset and Grid Management System', { x: 0.7, y: 2.65, w: 8.6, h: 0.5, fontFace: FONT, fontSize: 21, color: PALE, margin: 0, isTextBox: true });
      GROUP.forEach(([id, name], i) => {
        const x = 0.7 + Math.floor(i / 3) * 3.4, y = 3.5 + (i % 3) * 0.38;
        s.addText([{ text: id + '   ', options: { color: BLUE_L } }, { text: name, options: { color: WHITE, bold: true } }], { x, y, w: 3.3, h: 0.32, fontFace: FONT_R, fontSize: 13, margin: 0, valign: 'middle', isTextBox: true });
      });
      s.addText('Department of Electrical and Information Engineering  ·  University of Ruhuna', { x: 0.7, y: 4.85, w: 8.6, h: 0.3, fontFace: FONT_R, fontSize: 11, color: PALE, margin: 0, isTextBox: true });
    } else if (d.kind === 'arch') {
      frame(pres, s, '02 · CHARITH · ARCHITECTURE', 'One gateway, six independent services', num, d.time, 'dark');
      glass(pres, s, 0.6, 1.75, 8.8, 0.5);
      s.addText('React console  ·  nginx  ·  live updates over WebSocket', { x: 0.6, y: 1.75, w: 8.8, h: 0.5, fontFace: FONT_R, fontSize: 13, color: WHITE, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      glass(pres, s, 0.6, 2.4, 8.8, 0.5, true);
      s.addText('API Gateway  ·  JWT validation  ·  role policy', { x: 0.6, y: 2.4, w: 8.8, h: 0.5, fontFace: FONT_B, fontSize: 14, color: WHITE, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      const w = (8.8 - 5 * 0.12) / 6;
      ARCH_SERVICES.forEach((name, i) => {
        const x = 0.6 + i * (w + 0.12);
        glass(pres, s, x, 3.05, w, 0.85);
        s.addText(name, { x, y: 3.1, w, h: 0.45, fontFace: FONT_B, fontSize: 11, color: WHITE, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
        s.addText('PostgreSQL', { x, y: 3.5, w, h: 0.3, fontFace: FONT_R, fontSize: 9, color: PALE, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      });
      glass(pres, s, 0.6, 4.05, 8.8, 0.5);
      s.addText('RabbitMQ events  ·  Redis cache  ·  Prometheus and Grafana', { x: 0.6, y: 4.05, w: 8.8, h: 0.5, fontFace: FONT_R, fontSize: 13, color: WHITE, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      liveChip(pres, s, d.screen.split(':')[0] + ': sign in, Control room');
    } else if (d.kind === 'svc') {
      frame(pres, s, `${d.n} · ${d.who.toUpperCase()} · ${d.name.toUpperCase()}`, d.head, num, d.time, 'split');
      const n = d.flow.length, gap = 0.3, bw = (8.8 - gap * (n - 1)) / n;
      d.flow.forEach(([t, sub], i) => {
        const x = 0.6 + i * (bw + gap);
        s.addShape(pres.ShapeType.roundRect, { x, y: 1.75, w: bw, h: 1.0, fill: { color: i === 0 ? BLUE : SOFT }, line: { color: i === 0 ? BLUE : SOFT, width: 0 }, rectRadius: 0.06 });
        s.addText(t, { x, y: 1.82, w: bw, h: 0.5, fontFace: FONT_B, fontSize: 15, color: i === 0 ? WHITE : BLACK, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
        s.addText(sub, { x, y: 2.28, w: bw, h: 0.35, fontFace: FONT_R, fontSize: 11, color: i === 0 ? PALE : GREY, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
        if (i < n - 1) s.addText('›', { x: x + bw, y: 1.95, w: gap, h: 0.6, fontFace: FONT_B, fontSize: 24, color: BLUE, align: 'center', valign: 'middle', margin: 0, isTextBox: true });
      });
      const sw = (8.8 - 2 * 0.3) / 3;
      d.stats.forEach(([big, label], i) => stat(pres, s, 0.6 + i * (sw + 0.3), 3.4, sw, big, label, big.length > 8 ? 26 : 36));
      liveChip(pres, s, d.screen.split(':')[0].split(',')[0]);
    } else if (d.kind === 'rbac') {
      frame(pres, s, '08 · NEMAL · SECURITY', 'Five roles, enforced at the gateway', num, d.time, 'dark');
      ROLES.forEach(([role, can], i) => {
        const y = 1.7 + i * 0.5;
        glass(pres, s, 0.6, y, 8.8, 0.42);
        s.addText(role, { x: 0.8, y, w: 3.0, h: 0.42, fontFace: FONT_B, fontSize: 13, color: BLUE_L, valign: 'middle', margin: 0, isTextBox: true });
        s.addText(can, { x: 3.9, y, w: 5.4, h: 0.42, fontFace: FONT_R, fontSize: 13, color: WHITE, valign: 'middle', margin: 0, isTextBox: true });
      });
      liveChip(pres, s, 'executive sign-in, read-only');
    } else if (d.kind === 'ops') {
      frame(pres, s, '09 · CHARITH · OPERATIONS AND QUALITY', 'Observable, tested, one command to run', num, d.time, 'split', true);
      const sw = (8.8 - 3 * 0.3) / 4;
      OPS.forEach(([big, label], i) => stat(pres, s, 0.6 + i * (sw + 0.3), 3.4, sw, big, label, 46, WHITE));
      liveChip(pres, s, 'Grafana platform health');
    } else {
      s.addText('Thank you', { x: 0.7, y: 1.7, w: 8.6, h: 1.2, fontFace: FONT_R, fontSize: 60, color: WHITE, margin: 0, isTextBox: true });
      s.addText('Questions and discussion', { x: 0.7, y: 3.0, w: 8.6, h: 0.5, fontFace: FONT, fontSize: 22, color: BLUE_L, margin: 0, isTextBox: true });
      s.addText('REOAGMS  ·  EC8208 Software Architecture  ·  University of Ruhuna', { x: 0.7, y: 4.7, w: 8.6, h: 0.3, fontFace: FONT_R, fontSize: 11, color: PALE, margin: 0, isTextBox: true });
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
    const SHOWS = { Charith: 'Login and Control room; Grafana', Nemal: 'Assets page; executive read-only sign-in', Govindu: 'Live monitoring and the alert', Thurunu: 'Maintenance board', Yasiru: 'Notifications page', Sachinthana: 'Analytics report and CSV export', 'All members': 'Closing thank-you' };
    const shows = SHOWS[who];
    md += `| **${who}** | ${mine.map((x) => x.n).join(', ')} | ${total} s | ${shows} |\n`;
  });
  md += `\nTotal spoken text: about ${words} words, for a 10-minute slot at a calm pace, including the pauses for clicks. Each member's part below is complete: read only your own section.\n\n## The exact words, per member\n\n`;
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

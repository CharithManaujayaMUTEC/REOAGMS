// Renders the markdown sources in ./src to PDF through headless Edge/Chrome.
// Run from a folder where `puppeteer-core` and python `markdown` are installed.
import puppeteer from 'puppeteer-core';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
const here = path.dirname(fileURLToPath(import.meta.url));
const out = path.join(here, '..', '..', 'deliverables');
const exe = process.env.BROWSER || 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe';
const css = `body{font-family:Calibri,Arial,sans-serif;color:#22304a;font-size:11pt;line-height:1.45;margin:0}
h1{font-family:Cambria,serif;color:#1b2a49;font-size:24pt;margin:0 0 6pt}h2{font-family:Cambria,serif;color:#1b2a49;font-size:15pt;margin:18pt 0 6pt}
code{background:#eef2f8;padding:1px 4px;border-radius:3px;font-size:9.5pt}pre{background:#eef2f8;padding:8pt;border-radius:4px;font-size:9pt;white-space:pre-wrap}
table{border-collapse:collapse;width:100%;margin:8pt 0;font-size:10pt}th{background:#1b2a49;color:#fff;text-align:left}th,td{border:1px solid #c9d3e0;padding:5pt 7pt;vertical-align:top}tr:nth-child(even) td{background:#f5f8fc}
h1+h3{margin-top:6pt}h1{page-break-before:always}strong{color:#1b2a49}ul,ol{margin:4pt 0 4pt 18pt}`;
const browser = await puppeteer.launch({ executablePath: exe, headless: true, args: ['--no-sandbox'] });
for (const name of ['DEMO_SCRIPT', 'TEAM_GUIDE']) {
  const parts = name === 'DEMO_SCRIPT' ? ['DEMO_SCRIPT', 'live'] : [name];
  const py = "import markdown,sys; print(markdown.markdown(''.join(open(f,encoding='utf-8').read()+chr(10)*2 for f in sys.argv[1:]),extensions=['tables','fenced_code']))";
  const html = execFileSync('python', ['-c', py, ...parts.map((x) => path.join(here, 'src', `${x}.md`))], { encoding: 'utf8', env: { ...process.env, PYTHONIOENCODING: 'utf-8' } });
  const page = await browser.newPage();
  await page.setContent(`<html><head><meta charset="utf-8"><style>${css}</style></head><body>${html}</body></html>`);
  await page.pdf({ path: path.join(out, `${name}.pdf`), format: 'A4', printBackground: true, margin: { top: '18mm', bottom: '18mm', left: '16mm', right: '16mm' }, displayHeaderFooter: true, headerTemplate: '<span></span>', footerTemplate: '<div style="font-size:8px;color:#5b6b82;width:100%;text-align:center">REOAGMS · EC8208 Software Architecture · University of Ruhuna · <span class="pageNumber"></span>/<span class="totalPages"></span></div>' });
  console.log('pdf', name);
}
await browser.close();

// Renders src/ARCH_QA.html (system diagram, patterns, Q&A) to deliverables/REOAGMS-Architecture-Guide.pdf.
// Run from a folder where `puppeteer-core` is installed; set REPO to the repo root if not run in place.
import puppeteer from 'puppeteer-core';
import { fileURLToPath, pathToFileURL } from 'node:url';
import path from 'node:path';
const here = process.env.REPO ? path.join(process.env.REPO, 'scripts', 'presentations') : path.dirname(fileURLToPath(import.meta.url));
const out = path.join(here, '..', '..', 'deliverables', 'REOAGMS-Architecture-Guide.pdf');
const exe = process.env.BROWSER || 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe';
const browser = await puppeteer.launch({ executablePath: exe, headless: true, args: ['--no-sandbox'] });
const page = await browser.newPage();
await page.goto(pathToFileURL(path.join(here, 'src', 'ARCH_QA.html')).href, { waitUntil: 'load' });
await page.pdf({ path: out, format: 'A4', printBackground: true, margin: { top: '14mm', bottom: '16mm', left: '14mm', right: '14mm' }, displayHeaderFooter: true, headerTemplate: '<span></span>', footerTemplate: '<div style="font-size:8px;color:#5b6478;width:100%;text-align:center">REOAGMS · EC8208 Software Architecture · Group 9 · <span class="pageNumber"></span>/<span class="totalPages"></span></div>' });
await browser.close();
console.log('pdf', out);

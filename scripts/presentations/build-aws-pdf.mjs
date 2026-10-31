// Renders src/AWS_DEPLOYMENT.html to deliverables/REOAGMS-AWS-Deployment.pdf and a PNG preview.
// Run from a folder where `puppeteer-core` is installed; set REPO to the repo root if not run in place.
import puppeteer from 'puppeteer-core';
import { fileURLToPath, pathToFileURL } from 'node:url';
import path from 'node:path';
const here = process.env.REPO ? path.join(process.env.REPO, 'scripts', 'presentations') : path.dirname(fileURLToPath(import.meta.url));
const out = path.join(here, '..', '..', 'deliverables', 'REOAGMS-AWS-Deployment');
const exe = process.env.BROWSER || 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe';
const browser = await puppeteer.launch({ executablePath: exe, headless: true, args: ['--no-sandbox'] });
const page = await browser.newPage();
await page.setViewport({ width: 1400, height: 940, deviceScaleFactor: 1.5 });
await page.goto(pathToFileURL(path.join(here, 'src', 'AWS_DEPLOYMENT.html')).href, { waitUntil: 'load' });
await page.pdf({ path: out + '.pdf', preferCSSPageSize: true, printBackground: true });
await browser.close();
console.log('pdf', out + '.pdf');

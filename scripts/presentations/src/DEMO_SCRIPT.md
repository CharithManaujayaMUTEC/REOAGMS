# REOAGMS: Presentation script

This script gives the **exact words** each member says. The team deck `REOAGMS-Team-Presentation.pptx` has **10 slides** and runs together with the live console as one **5-minute** demo on one laptop. Each member speaks while their slide is up and switches to the app for the action shown under *On screen*. The same words are in the slides' speaker notes.

## Before you start
- Stack running (`docker compose up -d`), browser signed in as `admin@reoagms.com` on http://localhost:5173, a second tab on Grafana. Zoom 100 %, notifications silenced.
- Two minutes earlier, open the Alerts page and resolve the open alerts; the simulator then raises fresh events within about a minute.
- Rehearse with a stopwatch. Hand over in one sentence, as written.
- If the fault has not arrived by 1:30, Govindu says "the simulator injects one every minute" and Thurunu uses the newest order already on the board.

## Credentials
| Use | Login |
|---|---|
| Console (all roles) | `admin@reoagms.com`, `manager@reoagms.com`, `operator@reoagms.com`, `engineer@reoagms.com`, `executive@reoagms.com` · password `Reoagms@2026` |
| Grafana http://localhost:3001 | user `admin` · password `reoagms` |
| RabbitMQ http://localhost:15672 | `reoagms` / `reoagms_password` |
| Prometheus http://localhost:9090 | no login |

## Speaking order and time


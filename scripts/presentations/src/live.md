## Who does what

| Member | Slides | Time on stage | Shows in the live demo |
|---|---|---|---|
| **Charith** | 1, 2, 8, 9 | 110 s | Login and Control room; executive read-only sign-in; Grafana |
| **Nemal** | 3 | 30 s | Assets page |
| **Govindu** | 4 | 45 s | Live monitoring and the alert |
| **Thurunu** | 5 | 35 s | Maintenance board |
| **Yasiru** | 6 | 30 s | Notifications page |
| **Sachinthana** | 7 | 40 s | Analytics report and CSV export |
| **All members** | 10 | 10 s | Closing thank-you |

Total spoken text: about 506 words, under 5 minutes at a calm pace, with a few seconds spare for clicks. Each member's part below is complete: read only your own section.

## The exact words, per member

# Charith

### Slide 1 · 0:00–0:10

*On screen:* Title slide

**Charith says:** Good morning. We are presenting REOAGMS, a platform that monitors and maintains renewable energy assets in real time.

### Slide 2 · 0:10–0:45

*On screen:* Console: sign in as admin, then the Control room

**Charith says:** REOAGMS is six Spring Boot microservices behind one API gateway, connected by RabbitMQ events, each with its own PostgreSQL database. I sign in as the administrator; the gateway validates my token and role. This is the control room: live generation, open alerts, device health and an hourly energy curve. Nemal will show where this data comes from.

### Slide 8 · 3:45–4:15

*On screen:* Sign out, sign in as executive@reoagms.com, show the read-only pages

**Charith says:** Security is enforced at the gateway. Five roles carry different rights. I sign in as the executive: there are no action buttons, and the API itself answers 403 for any write. Every login and user change is recorded in the audit log. Quality is built in as well, with sixty test methods across the services.

### Slide 9 · 4:15–4:50

*On screen:* Grafana tab, then back to the slide

**Charith says:** Operations are visible in Grafana, where Prometheus scrapes all seven services and shows platform health. Behind the demo are sixty test methods, validation and error handling in every service, and a versioned event contract that keeps the services compatible. The whole system starts with one docker compose command.

# Nemal

### Slide 3 · 0:45–1:15

*On screen:* Assets page: switch tabs, open Add, then cancel

**Nemal says:** Thank you, Charith. The Asset Service owns the registry: five facilities, eleven assets and thirty-two sensors across solar, wind, hydro, battery and tidal. Lists are searched and paged on the server, and every form is validated, so a blank name is rejected. Only administrators and managers can change data. Every other service refers to these assets by ID. Govindu will show them producing live data.

# Govindu

### Slide 4 · 1:15–2:00

*On screen:* Live monitoring page, then the toast and the Alerts page

**Govindu says:** Thanks, Nemal. The Monitoring Service receives a reading from every sensor and checks it against our alert rules. This tile is a live sensor and the dashed line is its threshold. The simulator has just injected a fault, and here is the alert, instantly. One alert is stored, one event goes to RabbitMQ, and the browser is updated over WebSocket. That same event is now reaching Thurunu.

# Thurunu

### Slide 5 · 2:00–2:35

*On screen:* Maintenance board: newest card, Assign, Start, Complete

**Thurunu says:** Yes. The Maintenance Service received the same event and, because the severity is high, it opened a corrective work order by itself. Here it is on the board. I assign a technician, start the work and complete it. Each step is its own endpoint, and the finished job is saved to the maintenance history. Yasiru will show how people are informed.

# Yasiru

### Slide 6 · 2:35–3:05

*On screen:* Notifications page

**Yasiru says:** The Notification Service read the same alert from its own RabbitMQ queue. In this history, the in-app push was delivered instantly, and every email attempt is stored with its status, retry count and reason. A scheduler retries up to three times, I can retry any message here, and failing messages go to a dead-letter queue, so nothing is lost. Sachinthana will turn all this into reports.

# Sachinthana

### Slide 7 · 3:05–3:45

*On screen:* Analytics page: pick a facility, Generate, then export CSV

**Sachinthana says:** The Analytics Service calls the other services over REST and never touches their databases. For this facility it integrates average power into kilowatt-hours for each hour, and calculates availability, efficiency and capacity factor. I generate an energy report for the last day and export it as CSV for management. Executives see all of this read-only. Charith will close with security.

# All members

### Slide 10 · 4:50–5:00

*On screen:* Thank you slide

**All members says:** Thank you. We are happy to take your questions.


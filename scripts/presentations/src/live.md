## Who does what

| Member | Slides | Time on stage | Shows in the live demo |
|---|---|---|---|
| **Charith** | 1, 2, 9 | 49 s | Login and Control room; Grafana |
| **Nemal** | 3, 8 | 49 s | Assets page; executive read-only sign-in |
| **Govindu** | 4 | 49 s | Live monitoring and the alert |
| **Thurunu** | 5 | 49 s | Maintenance board |
| **Yasiru** | 6 | 49 s | Notifications page |
| **Sachinthana** | 7 | 49 s | Analytics report and CSV export |
| **All members** | 10 | 6 s | Closing thank-you |

Total spoken text: about 419 words, under 5 minutes at a calm pace, with a few seconds spare for clicks. Each member's part below is complete: read only your own section.

## The exact words, per member

# Charith

### Slide 1 · 0:00–0:08

*On screen:* Title slide

**Charith says:** Good morning. We are presenting REOAGMS, a platform that monitors and maintains renewable energy assets in real time.

### Slide 2 · 0:08–0:33

*On screen:* Console: sign in as admin, then the Control room

**Charith says:** REOAGMS is six Spring Boot microservices behind one API gateway, connected by RabbitMQ events, each with its own PostgreSQL database. I sign in as the administrator, and this is the control room with live data. Nemal will show where it comes from.

### Slide 9 · 4:38–4:54

*On screen:* Grafana tab, then back to the slide

**Charith says:** Grafana shows platform health from Prometheus across all seven services. Sixty tests protect the services, and one docker compose command starts everything.

# Nemal

### Slide 3 · 0:33–1:00

*On screen:* Assets page: switch tabs, open Add, then cancel

**Nemal says:** Thank you, Charith. The Asset Service owns the registry: five facilities, eleven assets and thirty-two sensors. Lists are searched and paged on the server, and every form is validated, so a blank name is rejected. Govindu will show these assets producing live data.

### Slide 8 · 4:16–4:38

*On screen:* Sign out, sign in as executive@reoagms.com, show the read-only pages

**Nemal says:** Access is enforced at the gateway, and five roles carry different rights. I sign in as the executive: there are no action buttons, and the API itself refuses any write. Charith will close.

# Govindu

### Slide 4 · 1:00–1:49

*On screen:* Live monitoring page, then the toast and the Alerts page

**Govindu says:** Thanks, Nemal. The Monitoring Service receives a reading from every sensor and checks it against our alert rules. This tile is a live sensor and the dashed line is its threshold. The simulator has just injected a fault, and here is the alert, instantly. One alert is stored, one event goes to RabbitMQ, and the browser is updated over WebSocket. That same event is now reaching Thurunu.

# Thurunu

### Slide 5 · 1:49–2:38

*On screen:* Maintenance board: newest card, Assign, Start, Complete

**Thurunu says:** Yes. The Maintenance Service received the same event and, because the severity is high, it opened a corrective work order by itself. Here it is on the board. I assign a technician, start the work and complete it. Each step is its own endpoint, and the finished job is saved to the maintenance history. Yasiru will show how people are informed.

# Yasiru

### Slide 6 · 2:38–3:27

*On screen:* Notifications page

**Yasiru says:** The Notification Service read the same alert from its own RabbitMQ queue. In this history, the in-app push was delivered instantly, and every email attempt is stored with its status, retry count and reason. A scheduler retries up to three times, I can retry any message here, and failing messages go to a dead-letter queue, so nothing is lost. Sachinthana will turn all this into reports.

# Sachinthana

### Slide 7 · 3:27–4:16

*On screen:* Analytics page: pick a facility, Generate, then export CSV

**Sachinthana says:** The Analytics Service calls the other services over REST and never touches their databases. For this facility it integrates average power into kilowatt-hours for each hour, and calculates availability, efficiency and capacity factor. I generate an energy report for the last day and export it as CSV for management. Nemal will show who is allowed to see it.

# All members

### Slide 10 · 4:54–5:00

*On screen:* Thank you slide

**All members says:** Thank you. We are happy to take your questions.


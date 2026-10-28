### 0:00–0:40 · Charith

*On screen:* Login page, then Control room

**Charith says:** Good morning. We are presenting REOAGMS, the Renewable Energy Operations, Asset and Grid Management System. It is a set of Spring Boot microservices behind a single API gateway, connected by RabbitMQ events, and everything you will see is real data in six PostgreSQL databases. I sign in as the administrator; the gateway checks my token and my role. This is the control room: live generation, open alerts, device health, an hourly energy curve and a live alert feed. Nemal will show where this data comes from.

### 0:40–1:10 · Nemal

*On screen:* Assets page; open Add, then cancel

**Nemal says:** Thank you, Charith. The Asset Service owns the registry: five facilities, eleven assets and thirty-two sensors, covering solar, wind, hydro, battery and tidal. Lists are paginated and searchable on the server, and every form is validated, so a blank name or a future installation date is rejected. Only administrators and operations managers can add or delete; other roles see this page read-only. Every other service refers to these assets by ID. Govindu will show them producing live data.

### 1:10–2:00 · Govindu

*On screen:* Live monitoring page; wait for the toast; open the Alerts page if needed

**Govindu says:** Thanks, Nemal. The Monitoring Service receives a reading from every sensor every few seconds and checks it against our alert rules. This tile is a live sensor, and the dashed line is its threshold. The simulator has just injected a fault, and here is the alert, instantly. The rule fired, the alert was stored once, one event was published to RabbitMQ, and the Notification Service pushed it to this browser over WebSocket. We keep one open alert per rule and asset, so operators are never flooded. That same event also reached Thurunu's service.

### 2:00–2:40 · Thurunu

*On screen:* Maintenance board: newest card, Assign, Start, Complete

**Thurunu says:** Yes. The Maintenance Service consumed the same event and, because the severity is high, it opened a corrective work order automatically. Here it is on the board, in Created. I assign a technician, start the work, and complete it. Each step is its own endpoint, and the finished job is written to the maintenance history. So a fault becomes tracked work with no manual step. Yasiru will show how people are told about it.

### 2:40–3:10 · Yasiru

*On screen:* Notifications page

**Yasiru says:** The Notification Service consumed the same alert event from its own RabbitMQ queue. In this history you can see every message: the in-app push was delivered instantly, and every email attempt is stored with its status, retry count and reason. The scheduler retries up to three times, and I can retry any message from here. Messages that keep failing go to a dead-letter queue, so nothing is lost. Sachinthana will turn all this data into reports.

### 3:10–4:00 · Sachinthana

*On screen:* Analytics page: pick facility; Generate → Energy report; click the download icon

**Sachinthana says:** The Analytics Service calls the other services over REST, never their databases. For this facility it integrates average power into kilowatt-hours per hour, and calculates availability, efficiency, capacity factor and utilisation. The chart is hourly energy. I generate an energy report for the last day and export it as CSV for management. We also keep KPIs, dashboards and performance reports, and executives see all of this read-only. Charith will close.

### 4:00–5:00 · Charith

*On screen:* Sign out, sign in as executive@reoagms.com, then the Grafana tab

**Charith says:** To close. On security: I sign in as the executive. There are no action buttons, and the API itself returns 403 for any write. For operations, Grafana shows platform health from Prometheus, which scrapes all seven services. On quality: sixty test methods, validation and error handling in every service, and a versioned event contract that keeps the services compatible. The whole system starts with one docker compose command. Thank you. We are happy to take questions.

### 4:50–5:00 · All members

**All say:** Thank you.


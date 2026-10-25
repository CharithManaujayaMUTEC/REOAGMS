# REOAGMS React operations console

React + TypeScript + Vite dashboard. It calls `/api/v1/...` endpoints through the API Gateway. In Docker, Vite proxies `/api` to `http://api-gateway:8080`; when running locally outside Docker, set `VITE_GATEWAY_PROXY=http://localhost:8080` in `.env`.

```powershell
npm install
npm run dev
npm run build
```

The Overview uses live service data where available. The energy trend chart is labelled illustrative because the backend does not yet expose a documented time-series endpoint matching the chart. The generic JSON create panel is a development aid; use the service DTO contract for request bodies.

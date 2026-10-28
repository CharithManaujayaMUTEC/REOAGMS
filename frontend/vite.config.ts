import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  // Read frontend/.env as well as the shell so `npm run dev` works outside Docker.
  const env = { ...loadEnv(mode, process.cwd(), ''), ...process.env };
  const gateway = env.VITE_GATEWAY_PROXY || 'http://api-gateway:8080';
  const notifications = env.VITE_WS_PROXY || 'http://localhost:8086';
  return {
    plugins: [react()],
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        '/api': { target: gateway, changeOrigin: true },
        '/ws': { target: notifications, ws: true, changeOrigin: true },
      },
    },
    build: { chunkSizeWarningLimit: 900 },
  };
});

import { defineConfig } from 'vite';

export default defineConfig({
  server: { proxy: { '/api': process.env.VCRAFT_API_TARGET || 'http://localhost:8080' } },
});

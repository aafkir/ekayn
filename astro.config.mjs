import { defineConfig } from 'astro/config';
import cloudflare from '@astrojs/cloudflare';

export default defineConfig({
  output: 'server',
  adapter: cloudflare(),
  security: { checkOrigin: false },
  site: import.meta.env.PUBLIC_SITE_URL || 'https://tifilit.com',
  redirects: {
    '/es/': '/',
  },
});

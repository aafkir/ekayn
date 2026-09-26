import { defineConfig } from 'astro/config';

export default defineConfig({
  site: 'https://www.tadelaktnatural.net',
  output: 'static',
  build: { format: 'directory' }
});

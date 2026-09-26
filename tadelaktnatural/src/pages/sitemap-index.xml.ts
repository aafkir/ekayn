import type { APIRoute } from 'astro';
import { site, services, posts } from '../data/site';

export const GET: APIRoute = () => {
  const urls = ['/', '/es/', '/es/que-es-el-tadelakt/', '/es/servicios/', ...services.map((s) => `/es/servicios/${s.slug}/`), '/es/proyectos/', '/es/sobre-nosotros/', '/es/preguntas-frecuentes/', '/es/contacto/', '/es/aviso-legal/', '/es/privacidad/', '/es/cookies/', '/es/blog/', ...posts.map((p) => `/es/blog/${p.slug}/`)].map((path) => `<url><loc>${site.url}${path}</loc></url>`).join('');
  return new Response(`<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">${urls}</urlset>`, { headers: { 'Content-Type': 'application/xml' } });
};

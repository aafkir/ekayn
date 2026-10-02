import type { APIRoute } from 'astro';
import { products } from '../data/products';
export const GET: APIRoute = ({ site }) => {
  const base = site ?? new URL('https://ekayn.com');
  const urls = [new URL('/', base), new URL('/contact', base), ...products.map((product) => new URL(`/produits/${product.slug}`, base))].map((url) => `<url><loc>${url}</loc></url>`).join('');
  return new Response(`<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">${urls}</urlset>`, { headers: { 'Content-Type': 'application/xml' } });
};

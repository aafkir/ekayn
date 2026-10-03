import type { APIRoute } from 'astro';
import { site } from '../data/site';

export const GET: APIRoute = () => {
  const groups = [
    ['/es/','/fr/','/en/'], ['/es/que-es-el-tadelakt/','/fr/quest-ce-que-le-tadelakt/','/en/what-is-tadelakt/'],
    ['/es/servicios/','/fr/services/','/en/services/'],
    ['/es/servicios/banos-y-duchas/','/fr/services/salles-de-bains-et-douches/','/en/services/bathrooms-and-showers/'],
    ['/es/servicios/hammams/','/fr/services/hammams/','/en/services/hammams/'],
    ['/es/servicios/piscinas/','/fr/services/piscines/','/en/services/pools/'],
    ['/es/servicios/cocinas-y-encimeras/','/fr/services/cuisines-et-plans-de-travail/','/en/services/kitchens-and-worktops/'],
    ['/es/servicios/paredes-y-suelos/','/fr/services/murs-et-sols/','/en/services/walls-and-floors/'],
    ['/es/proyectos/','/fr/realisations/','/en/projects/'],
    ['/es/sobre-nosotros/','/fr/a-propos/','/en/about/'], ['/es/preguntas-frecuentes/','/fr/questions-frequentes/','/en/faq/'],
    ['/es/contacto/','/fr/contact/','/en/contact/'], ['/es/aviso-legal/','/fr/mentions-legales/','/en/legal-notice/'],
    ['/es/privacidad/','/fr/confidentialite/','/en/privacy/'], ['/es/cookies/','/fr/cookies/','/en/cookies/'],
    ['/es/tienda/','/fr/boutique/','/en/shop/'], ['/es/tienda/carrito/','/fr/boutique/panier/','/en/shop/cart/'],
    ['/es/tienda/cal-de-marrakech-para-tadelakt/','/fr/boutique/chaux-de-marrakech-pour-tadelakt/','/en/shop/marrakech-lime-for-tadelakt/'],
    ['/es/tienda/jabon-negro-para-tadelakt/','/fr/boutique/savon-noir-pour-tadelakt/','/en/shop/black-soap-for-tadelakt/'],
    ['/es/tienda/piedra-especial-para-tadelakt/','/fr/boutique/pierre-speciale-pour-tadelakt/','/en/shop/special-tadelakt-polishing-stone/'],
    ['/es/blog/','/fr/blog/','/en/blog/'],
    ['/es/blog/tadelakt-en-banos/'], ['/es/blog/historia-origen-tadelakt/'], ['/es/blog/tadelakt-exteriores/'],
    ['/es/blog/tadelakt-en-cocinas/'], ['/es/blog/colores-tadelakt-inspiracion/'], ['/es/blog/beneficios-tadelakt-banos-modernos/'],
    ['/es/blog/tadelakt-vs-microcemento/'], ['/es/blog/por-que-elegir-tadelakt-para-el-bano/'],
    ['/es/blog/como-mantener-y-limpiar-el-tadelakt-correctamente/'], ['/es/blog/tecnica-el-tadelakt/'],
    ['/es/blog/blog-post-title-four-g95ew-cmaht/'], ['/es/blog/blog-post-title-four-g95ew/'],
    ['/es/blog/que-es-el-tadelakt-origen-propiedades-aplicaciones/','/fr/blog/quest-ce-que-le-tadelakt-origine-proprietes-applications/','/en/blog/what-is-tadelakt-origin-properties-uses/'],
    ['/es/blog/tadelakt-para-banos-y-duchas/','/fr/blog/tadelakt-salles-de-bains-et-douches/','/en/blog/tadelakt-bathrooms-and-showers/'],
    ['/es/blog/tadelakt-o-microcemento/','/fr/blog/tadelakt-ou-microciment/','/en/blog/tadelakt-or-microcement/']
  ];
  const urls = groups.map((group) => `<url><loc>${site.url}${group[0]}</loc>${group.map((path, i) => `<xhtml:link rel="alternate" hreflang="${['es','fr','en'][i]}" href="${site.url}${path}"/>`).join('')}<xhtml:link rel="alternate" hreflang="x-default" href="${site.url}${group[0]}"/></url>`).join('');
  return new Response(`<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">${urls}</urlset>`, { headers: { 'Content-Type': 'application/xml' } });
};

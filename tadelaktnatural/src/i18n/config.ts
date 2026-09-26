export type Locale = 'es' | 'fr' | 'en';

export const locales = {
  es: { label: 'Español', lang: 'es', home: '/es/' },
  fr: { label: 'Français', lang: 'fr', home: '/fr/' },
  en: { label: 'English', lang: 'en', home: '/en/' }
} as const;

export const ui = {
  es: { menu:'Menú', quote:'Solicitar presupuesto', contact:'Contacto', services:'Servicios', projects:'Proyectos', blog:'Blog', about:'Sobre nosotros', material:'El tadelakt', explore:'Explorar', skip:'Saltar al contenido', legal:'Aviso legal', privacy:'Privacidad', cookies:'Cookies' },
  fr: { menu:'Menu', quote:'Demander un devis', contact:'Contact', services:'Services', projects:'Réalisations', blog:'Journal', about:'À propos', material:'Le tadelakt', explore:'Explorer', skip:'Aller au contenu', legal:'Mentions légales', privacy:'Confidentialité', cookies:'Cookies' },
  en: { menu:'Menu', quote:'Request a quote', contact:'Contact', services:'Services', projects:'Projects', blog:'Journal', about:'About us', material:'Tadelakt', explore:'Explore', skip:'Skip to content', legal:'Legal notice', privacy:'Privacy', cookies:'Cookies' }
} as const;

export const languagePaths = {
  es: { home:'/es/', material:'/es/que-es-el-tadelakt/', services:'/es/servicios/', projects:'/es/proyectos/', about:'/es/sobre-nosotros/', faq:'/es/preguntas-frecuentes/', contact:'/es/contacto/', blog:'/es/blog/', legal:'/es/aviso-legal/', privacy:'/es/privacidad/', cookies:'/es/cookies/' },
  fr: { home:'/fr/', material:'/fr/quest-ce-que-le-tadelakt/', services:'/fr/services/', projects:'/fr/realisations/', about:'/fr/a-propos/', faq:'/fr/questions-frequentes/', contact:'/fr/contact/', blog:'/fr/blog/', legal:'/fr/mentions-legales/', privacy:'/fr/confidentialite/', cookies:'/fr/cookies/' },
  en: { home:'/en/', material:'/en/what-is-tadelakt/', services:'/en/services/', projects:'/en/projects/', about:'/en/about/', faq:'/en/faq/', contact:'/en/contact/', blog:'/en/blog/', legal:'/en/legal-notice/', privacy:'/en/privacy/', cookies:'/en/cookies/' }
} as const;

export const localizedServices = {
  fr: { 'salles-de-bains-et-douches':['Salles de bains et douches','Des surfaces continues et chaleureuses pour les pièces humides, réalisées avec soin.'], hammams:['Hammams','Des finitions minérales pour créer des atmosphères calmes et tactiles.'], piscines:['Piscines','Des revêtements naturels pour des projets où la matière compte.'], 'cuisines-et-plans-de-travail':['Cuisines et plans de travail','Une finition artisanale pour réunir couleur, texture et continuité.'], 'murs-et-sols':['Murs et sols','De la chaux et des matériaux naturels pour transformer les intérieurs.'] },
  en: { 'bathrooms-and-showers':['Bathrooms and showers','Continuous, warm surfaces for wet rooms, made with care.'], hammams:['Hammams','Mineral finishes for calm, tactile and lasting spaces.'], pools:['Pools','Natural finishes for projects where material and detail matter.'], 'kitchens-and-worktops':['Kitchens and worktops','A crafted alternative to bring colour, texture and continuity together.'], 'walls-and-floors':['Walls and floors','Lime and natural materials applied to transform interiors.'] }
} as const;

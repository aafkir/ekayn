# Tadelakt Natural

Site statique Astro pour Tadelakt Natural. L’espagnol (`/es/`) reste la langue principale ; le français (`/fr/`) et l’anglais (`/en/`) disposent maintenant de pages, URLs, contenus et métadonnées localisés. Les espaces `/it/` et `/de/` restent préparés mais `noindex` tant que leurs traductions ne sont pas relues.

Le logo fourni est utilisé depuis `public/assets/logo/logo.png`, à partir de `assets/logo.png`.

## Développement

```bash
npm install
npm run dev
```

Vérifications : `npm run check`, `npm run build` et `npm run preview`.

## Cloudflare Pages

- Framework preset : `Astro`
- Build command : `npm run build`
- Output directory : `dist`
- Node : `22.14.0` (`.nvmrc`)
- Variables d’environnement : aucune pour cette version

Le fichier `public/_redirects` conserve les anciennes URL principales. `public/_headers` ajoute des en-têtes de sécurité de base. Le sitemap est généré à `/sitemap-index.xml` et `robots.txt` le référence.

## Formulaire

Le formulaire de devis est volontairement honnête : il n’envoie pas encore de données. Connecter ultérieurement Cloudflare Pages Functions, Formspree ou un service équivalent après validation de la politique de confidentialité. Ne jamais placer de clé secrète dans le navigateur.

## Contenu à confirmer

Voir [CONTENT_TODO.md](./CONTENT_TODO.md) avant mise en production. Les informations manquantes n’ont pas été inventées.

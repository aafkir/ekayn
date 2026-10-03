# Audit technique SEO — PrepCivique

Date de l’audit : 2026-10-03
Périmètre : code source disponible dans `prepcivique/prepcivique-ui` et artefacts du build local.
Mode : lecture seule du site ; aucun fichier applicatif n’a été modifié.

## 1. Résumé de la stack

- Frontend Angular standalone, version `21.2.x`, avec TypeScript `5.9.x` et Angular Router (`prepcivique-ui/package.json:14-35`).
- Rendu hybride Angular SSR + prerender statique : `@angular/ssr`, `outputMode: "server"`, entrée SSR et routes de prerender (`prepcivique-ui/angular.json:42-73`, `prepcivique-ui/src/app/app.config.server.ts:1-8`, `prepcivique-ui/src/app/app.routes.server.ts:10-82`).
- Serveur SSR Node/Express prévu sur le port `PORT` ou `4000` (`prepcivique-ui/src/server.ts:12-60`).
- Déploiement documenté sur Cloudflare Pages, avec sortie `dist/prepcivique-ui/browser` (`prepcivique-ui/DEPLOYMENT.md:1-12`). Le code ne permet pas de confirmer le projet Cloudflare effectivement utilisé en production.
- Routage déclaratif Angular, composants lazy-loaded, guards pour les slugs et paramètres autorisés (`prepcivique-ui/src/app/app.routes.ts:12-103`, `prepcivique-ui/src/app/core/routing/known-slug.guard.ts:9-21`, `prepcivique-ui/src/app/core/routing/allowed-param.guard.ts:4-14`).
- Contenu SEO public principalement codé en dur dans des fichiers TypeScript : articles (`features/articles/data/articles.data.ts`), questions SEO, fiches thématiques et pages d’information. Le QCM dynamique utilise aussi un service API (`features/examen-civique/examen-civique-page.component.ts:42-58`). Aucun CMS n’est identifiable dans le frontend audité.
- Les environnements pointent vers une API locale en développement et une API Google Cloud Run en production (`prepcivique-ui/src/environments/environment.ts:1-6`, `prepcivique-ui/src/environments/environment.prod.ts:1-6`).
- Le dépôt Git racine est `/Users/afkir/Desktop/dev`; `prepcivique/` est ignoré par son `.gitignore` racine. Le code audité n’est donc pas versionné dans ce dépôt, ce qui limite la création d’une PR exploitable contenant le code et le rapport ensemble.

## 2. Verdict rendu serveur / client

### Verdict : SSR/prerender présent et favorable au SEO, mais dépendance au fallback Cloudflare à sécuriser

Le contenu SEO n’est pas injecté uniquement après JavaScript côté client :

1. Angular est configuré pour produire un bundle serveur (`angular.json:61-73`) et les routes publiques sont déclarées en `RenderMode.Prerender` (`src/app/app.routes.server.ts:10-52`).
2. Les pages à paramètres sont prerenderisées à partir des tableaux TypeScript (`src/app/app.routes.server.ts:29-77`).
3. Le build local du 2026-10-03 a produit 107 routes prerenderisées dans `prepcivique-ui/dist/prepcivique-ui/browser`.
4. Vérification de l’HTML initial généré : les pages d’accueil, QCM, article et questions contiennent dès le HTML initial un `<title>`, une meta description, un canonical et un `<h1>`. Par exemple :
   - `dist/prepcivique-ui/browser/examen-civique/index.html` contient le titre et le H1 QCM ;
   - `dist/prepcivique-ui/browser/articles/examen-civique-2026-inscription-prix-duree-resultat/index.html` contient le titre, la description, le contenu de l’article et le H1 ;
   - `dist/prepcivique-ui/browser/questions-examen-civique/naturalisation/index.html` contient le titre, la description et le H1 spécifique au parcours.

Limite importante : `prepcivique-ui/public/_redirects:1` contient `/* /index.html 200`. Une URL inconnue servie par Cloudflare peut donc recevoir l’index avec un statut 200 avant que le routeur Angular ne redirige vers l’accueil (`src/app/app.routes.ts:100-103`). Cela crée un risque de soft-404. Le statut HTTP réel en production n’est pas vérifiable depuis le code local.

## 3. Tableau des problèmes classés par priorité

| Priorité | Point | Problème | Fichiers | Correctif recommandé | Effort |
|---|---|---|---|---|---|
| Critique | 404 / soft-404 | Le wildcard Angular redirige toute route inconnue vers `/`, et le fallback Cloudflare sert `index.html` en 200. Les slugs invalides sont aussi redirigés vers une page valide par les guards. | `src/app/app.routes.ts:100-103`; `src/app/core/routing/known-slug.guard.ts:15-21`; `src/app/core/routing/allowed-param.guard.ts:10-14`; `public/_redirects:1` | Mettre en place une vraie page 404 et un comportement serveur/CDN retournant HTTP 404 pour les routes inconnues et les slugs inexistants. Tester plusieurs URL invalides avec `curl -I`. | L |
| Haute | Canonical | Les pages articles prerenderisées contiennent deux `<link rel="canonical">` : le canonical de `src/index.html` puis celui ajouté par `ArticleDetailComponent`. Le build local en a détecté 28. | `src/index.html:13`; `src/app/features/articles/article-detail.component.ts:30-52`; `src/app/app.component.ts:24-26` | Centraliser la création/mise à jour du canonical et supprimer l’élément initial ou empêcher l’ajout dupliqué. Vérifier aussi `og:url` après navigation SPA. | M |
| Haute | Sitemap | `public/sitemap.xml` contient 106 URL, sans `<lastmod>`. La comparaison avec les 107 fichiers prerenderisés du build montre que `/livret-citoyen` est absent du sitemap. | `public/sitemap.xml:1-106`; `src/app/app.routes.server.ts:10-27` | Générer le sitemap à partir de la même source que les routes prerenderisées, ajouter `/livret-citoyen`, et produire `lastmod` seulement à partir de dates fiables. Supprimer l’entretien manuel des listes. | M |
| Haute | Open Graph / Twitter Cards | `src/index.html` fournit les balises OG/Twitter de l’accueil, et les articles les mettent à jour, mais les composants QCM, fiches, pages publiques, questions et RDV ne définissent pas leurs balises OG/Twitter propres. En navigation SPA, d’anciennes valeurs peuvent rester dans le `<head>`. Aucun `og:image`/`twitter:image` n’a été trouvé dans le code audité. | `src/index.html:14-27`; `src/app/features/articles/article-detail.component.ts:38-48`; `src/app/features/examen-civique/examen-civique-page.component.ts:83-89`; composants SEO listés ci-dessous | Centraliser les métadonnées par route : `og:title`, `og:description`, `og:url`, `og:type`, `og:image`, `twitter:card`, `twitter:title`, `twitter:description`, et nettoyer les valeurs héritées lors d’une navigation. | M |
| Haute | Normalisation des URL | Aucun redirect HTTP explicite http→https, www→non-www, slash final ou casse n’est présent dans le code. Le canonical fixe `https://prepcivique.com`, mais cela ne remplace pas une normalisation serveur. | `src/app/core/seo/seo.service.ts:4-46`; `public/_redirects:1`; `src/server.ts:30-44` | Vérifier la configuration Cloudflare réelle et imposer une seule variante publique : HTTPS, domaine choisi, sans slash final sauf racine. Tester avec les quatre variantes et `curl -I`. | M |
| Haute | Meta robots | La base contient `index,follow,max-image-preview:large`, les articles le définissent explicitement, mais la plupart des composants ne mettent pas à jour `robots` lors d’une navigation SPA. Les pages filtrées par query string sont canoniquées vers l’URL propre sans stratégie explicite `noindex`. | `src/index.html:12`; `src/app/features/articles/article-detail.component.ts:37`; `src/app/core/seo/seo.service.ts:26-45`; `src/app/features/examen-civique/examen-civique-page.component.ts:95-121` | Définir une politique par route : indexation des pages éditoriales, canonical/noindex des vues filtrées, noindex des espaces privés et sessions. Vérifier aussi les en-têtes `X-Robots-Tag` côté CDN, non vérifiables depuis le code. | M |
| Moyenne | Titles | Les titles sont uniques sur les fichiers prerenderisés contrôlés, mais 76 sur 107 dépassent 60 caractères ; plusieurs articles atteignent 70–84 caractères. Cela augmente le risque de troncature dans Google. | `src/app/features/articles/data/articles.data.ts:48-1289`; composants de pages SEO ; mesure sur `dist/prepcivique-ui/browser/**/index.html` | Revoir les titles par intention et limiter les modèles trop répétitifs. Conserver une cible indicative, sans couper artificiellement les mots-clés importants. | M |
| Moyenne | Meta descriptions | Les descriptions sont générées par page pour les principales pages, mais 50 sur 107 sont inférieures à 120 caractères et 3 dépassent 160 caractères dans le build local. Certaines fiches utilisent des textes très génériques et sans accents. | `src/app/features/articles/data/articles.data.ts`; `src/app/features/thematic-sheets/pages/*/*.component.ts`; mesure sur les HTML prerenderisés | Harmoniser les descriptions par intention de recherche et vérifier leur adéquation au contenu réel. Ne pas dupliquer mécaniquement les descriptions de fiches. | M |
| Moyenne | Données structurées | Plusieurs JSON-LD sont injectés par les constructeurs Angular : Organization/WebSite, FAQPage, Article/BreadcrumbList, Question, ItemList et DigitalDocument. La présence syntaxique est vérifiée dans le HTML prerenderisé, mais aucune validation externe Schema.org/Rich Results n’a été exécutée. Les articles n’incluent pas d’image, et les pages Question utilisent un type `Question` à valider selon l’éligibilité recherchée. | `src/index.html:29-49`; `src/app/home/home.component.ts:35-55`; `src/app/features/articles/article-detail.component.ts:54-80`; `src/app/features/questions-officielles/question-seo-detail.component.ts:36-47`; `src/app/features/public-info/public-info-page.component.ts:42-56`; `src/app/features/articles/articles-page.component.ts:55-68`; `src/app/features/manuel-candidat/manuel-candidat.component.ts:34-44` | Valider chaque modèle dans les outils Schema.org/Google, ajouter les propriétés réellement disponibles (`image`, `dateModified`, `url`, etc.) et ne conserver que les types reflétant le contenu visible. | M |
| Moyenne | Images | Les templates audités ont 11 images ; aucune ne déclare explicitement `width`/`height`. Cinq utilisent `loading="lazy"`. Les dimensions intrinsèques des images ne sont donc pas réservées systématiquement, ce qui peut contribuer au CLS. Les `alt` sont présents à plusieurs endroits mais trois sont fournis par binding et doivent être vérifiés au rendu. | `src/app/features/expert-appointment/expert-appointment-page.component.html:61-65`; `src/app/features/examen-civique/components/qcm-hero/qcm-hero.component.html:25-31`; `src/app/features/thematic-sheets/thematic-sheets.component.html:21`; `src/app/home/components/revision-path-illustration/revision-path-illustration.component.html:22-27`; `src/app/home/components/how-it-works/how-it-works.component.html:20-90` | Ajouter dimensions ou `aspect-ratio` stabilisant la mise en page, conserver lazy-loading pour les images hors écran, réserver eager/high seulement au LCP, convertir/optimiser les visuels lourds selon leur usage. | M |
| Moyenne | Liens internes / cohérence d’URL | Le routage utilise majoritairement de vrais `<a>`/`routerLink`, ce qui est favorable. Il existe cependant deux familles d’URL pour les fiches (`/fiches-par-thematique/...` et `/fiches/...`) ; le service canonicalise les secondes vers les premières. Les liens cassés n’ont pas été vérifiables exhaustivement sans crawler HTTP déployé. | `src/app/features/thematic-sheets/thematic-sheets.routes.ts:10-191`; `src/app/core/seo/seo.service.ts:41-45`; templates thématiques | Choisir une seule famille d’URL dans les liens internes, rediriger l’autre en 301 au serveur/CDN et exécuter un crawl des liens internes en production. | M |
| Moyenne | Filtres / duplication | La page QCM lit `filtre`, `filter`, `parcours` et `theme` depuis la query string et modifie le contenu visible. Le canonical retire ces paramètres via `SeoService`, ce qui évite l’indexation canonique de chaque variante, mais la politique noindex n’est pas explicite. La page Articles filtre côté composant sans URL dédiée. Aucune pagination n’a été trouvée dans le code audité. | `src/app/features/examen-civique/examen-civique-page.component.ts:95-121`; `src/app/core/seo/seo.service.ts:26-45`; `src/app/features/articles/articles-page.component.ts:40-45` | Décider quelles variantes filtrées doivent être indexables. Pour les autres, conserver un canonical propre et/ou `noindex,follow`; éviter de créer des URLs de filtre indexables sans contenu distinct. | S |
| Basse | Hreflang | Le document HTML porte `lang="fr"`, mais aucun `hreflang` n’est défini. Pour un site uniquement français, l’absence est cohérente. | `src/index.html:2` | Ne rien ajouter tant qu’il n’existe pas de versions linguistiques indexables et réellement équivalentes. | S |
| Basse | Tracking | Google Analytics 4 est présent en production avec consentement utilisateur et page views SPA. Aucun script Search Console ou Bing Webmaster n’est identifiable dans le code. La vérification des propriétés et des conversions est externe au dépôt. | `src/environments/environment.prod.ts:1-6`; `src/app/core/analytics/analytics-consent.service.ts:8-80`; `src/app/app.component.ts:15-26` | Vérifier dans GA4, Search Console et Bing Webmaster que les propriétés utilisent le bon domaine, que les pages SPA sont mesurées et que le consentement respecte la configuration de production. | S |
| Basse | Performance bundles | Le build local mesure environ 494,52 kB bruts pour les chunks initiaux, 131,47 kB transférés estimés, et un chunk PDF lazy d’environ 406,72 kB. Les budgets Angular existent : warning à 500 kB et erreur à 1 MB. La performance réelle LCP/INP/CLS n’est pas mesurable depuis le code seul. | `angular.json:83-98`; mesures de `npm run build` du 2026-10-03 | Mesurer Lighthouse mobile et les Core Web Vitals en production, puis traiter d’abord LCP, images héro et chargements PDF. Conserver le PDF lazy et surveiller le seuil initial. | M |

### Composants de métadonnées observés

Les titres et descriptions sont définis dans les composants suivants :

- Accueil : `src/app/home/home.component.ts:37-43`.
- QCM : `src/app/features/examen-civique/examen-civique-page.component.ts:83-89`.
- Liste de questions : `src/app/features/questions-officielles/questions-officielles-page.component.ts:92-95`.
- Question individuelle : `src/app/features/questions-officielles/question-seo-detail.component.ts:29-35`.
- Articles : `src/app/features/articles/articles-page.component.ts:47-53`.
- Article individuel : `src/app/features/articles/article-detail.component.ts:33-48`.
- Fiches thématiques et détails : `src/app/features/thematic-sheets/thematic-sheets.component.ts:55-61` et les composants `src/app/features/thematic-sheets/pages/*/*.component.ts`.
- Pages FAQ, naturalisation et centres : `src/app/features/public-info/public-info-page.component.ts:35-40` et `src/app/features/public-info/public-info.data.ts:26-288`.
- Manuel, livret et RDV : `src/app/features/manuel-candidat/manuel-candidat.component.ts:26-32`, `src/app/features/livret/livret.component.ts:53-63`, `src/app/features/expert-appointment/expert-appointment-page.component.ts:73-82`.

## 4. Éléments déjà corrects à préserver

- Le rendu serveur/prerender est réellement activé ; le contenu éditorial principal est disponible dans le HTML initial généré.
- `lang="fr"` est présent dans `src/index.html:2`.
- `robots.txt` autorise le contenu public, bloque les zones privées et déclare le sitemap (`public/robots.txt:1-11`).
- Le sitemap utilise des URL absolues HTTPS, sans doublons détectés ; les 28 slugs d’articles présents dans `articles.data.ts` sont bien présents dans le sitemap.
- Les routes publiques importantes de la mission sont déclarées et prerenderisées, notamment QCM, questions par parcours, articles, fiches, FAQ, naturalisation, centres et RDV (`src/app/app.routes.server.ts:10-77`).
- Les principales pages prerenderisées contrôlées ont un seul H1 ; le comptage automatisé sur les 107 HTML générés n’a trouvé aucun fichier avec zéro ou plusieurs H1.
- Les titles sont uniques sur les 107 HTML générés contrôlés.
- Les articles disposent de données éditoriales dédiées (`title`, `seoTitle`, `description`, `seoDescription`, date, sections) dans `features/articles/data/articles.data.ts`.
- Les liens internes sont majoritairement de vrais éléments `<a>` avec `routerLink`, et les liens d’articles/fiches/QCM sont rendus dans le HTML des composants.
- Des liens vers les sources officielles sont présents dans les articles, par exemple `features/articles/data/articles.data.ts:132-144`.
- Le consentement précède le chargement de Google Analytics ; le service n’envoie pas de page view avant acceptation (`src/app/core/analytics/analytics-consent.service.ts:17-30`, `44-58`).
- Les budgets de bundle et l’optimisation de production sont configurés (`angular.json:75-98`).
- Les routes privées importantes sont exclues de `robots.txt` et le service canonical retire leur canonical (`public/robots.txt:3-9`, `src/app/core/seo/seo.service.ts:29-39`).

## 5. Risques de cannibalisation

### `/examen-civique` vs article général 2026

- `/examen-civique` cible l’action : QCM, examens blancs et parcours (`src/app/features/examen-civique/examen-civique-page.component.ts:83-89`).
- `/articles/examen-civique-2026-inscription-prix-duree-resultat` cible l’information : inscription, prix, durée et résultats (`src/app/features/articles/data/articles.data.ts:48-146`).
- Le risque est maîtrisable si la page QCM reste la page transactionnelle/pratique et l’article la page informationnelle, avec des liens réciproques et des titles distincts. Éviter de donner aux deux pages le même H1, title et paragraphe d’introduction.

### `/naturalisation` vs `/questions-examen-civique/naturalisation`

- `/naturalisation` est une page d’information générale sur le parcours (`src/app/features/public-info/public-info.routes.ts:19-22`, `public-info.data.ts:149-170`).
- `/questions-examen-civique/naturalisation` est une page de questions/QCM ciblée (`src/app/features/questions-officielles/questions-officielles-page.component.ts:42-52`, `92-99`).
- Les intentions sont distinctes mais proches. Recommandation : faire de `/naturalisation` la page “démarches et parcours” et de la page questions la page “questions officielles et entraînement”, avec ancres internes différentes.

### Articles `questions-officielles-*` vs `/questions-examen-civique/*`

- Les trois articles `questions-officielles-examen-civique-*` sont des guides éditoriaux dans `features/articles/data/articles.data.ts`.
- Les pages `/questions-examen-civique/{parcours}` sont des hubs de questions et d’entraînement (`src/app/app.routes.ts:58-84`).
- La cannibalisation est probable sur les requêtes “questions officielles examen civique [parcours]”. Il faut attribuer explicitement : article = explication, contexte officiel, méthode et sources ; hub = liste interactive/QCM. Les liens doivent utiliser des ancres cohérentes et les titles doivent exprimer cette différence.

## 6. Plan de correction ordonné

### Étape 1 — Sécuriser l’indexation technique

1. Corriger le double canonical des articles.
2. Vérifier la configuration Cloudflare et renvoyer un vrai 404 pour les routes inconnues.
3. Tester http/https, www/non-www, slash final, URL en majuscules et paramètres avec `curl -I`.

### Étape 2 — Fiabiliser la couverture sitemap

1. Ajouter `/livret-citoyen`.
2. Mettre en place une génération contrôlée à partir des mêmes sources que `app.routes.server.ts`.
3. Vérifier après chaque build que sitemap et prerender ne divergent pas.

### Étape 3 — Centraliser les métadonnées

1. Construire un service unique de title, description, canonical, robots, OG et Twitter.
2. Nettoyer les balises héritées pendant les navigations SPA.
3. Ajouter des images sociales et vérifier les URL absolues.

### Étape 4 — Définir la politique des filtres

1. Lister les query strings réellement utiles au SEO.
2. Canonicaliser ou `noindex,follow` les variantes non éditoriales.
3. Contrôler qu’aucune variante de filtre ne soit ajoutée au sitemap.

### Étape 5 — Qualifier contenu et données structurées

1. Réduire les titles trop longs et homogénéiser les descriptions.
2. Valider JSON-LD Article, BreadcrumbList, FAQPage, Question et DigitalDocument.
3. Vérifier que chaque donnée structurée décrit un contenu visible dans le HTML initial.

### Étape 6 — Mesurer le mobile en production

1. Lancer Lighthouse mobile et PageSpeed sur l’accueil, QCM, article, fiche et questions.
2. Mesurer LCP, CLS, INP, poids image et poids JavaScript.
3. Corriger uniquement les goulots mesurés, en priorité l’image LCP et les changements de layout.

## 7. Questions ouvertes pour le propriétaire du site

- Quel est le projet Cloudflare Pages et quelle configuration de redirection/domaine est active en production ?
- Le domaine canonique définitif est-il `https://prepcivique.com` ou `https://www.prepcivique.com` ?
- Les réponses HTTP des URL invalides (`/abc`, slug d’article inexistant, question inexistante) sont-elles bien 404 en production ?
- Le dépôt Git qui contient réellement `prepcivique-ui` est-il différent de `/Users/afkir/Desktop/dev` ? Le `.gitignore` actuel ignore tout `prepcivique/`, ce qui empêche une PR code complète depuis ce dépôt.
- Qui possède l’accès Google Search Console et peut fournir les rapports Indexation, Pages, Sitemaps et Core Web Vitals ?
- Une propriété Bing Webmaster Tools existe-t-elle ?
- Le site doit-il rester uniquement en français, ou des versions A2/anglais/autres langues sont-elles prévues ?
- Les dates `2026` présentes dans les titles, articles et sitemap correspondent-elles au calendrier éditorial et aux informations officielles actuellement validées ?
- Les pages filtrées par query string doivent-elles être indexées individuellement, ou uniquement la page canonique sans paramètres ?
- Les images sociales officielles (logo, visuel de partage, dimensions souhaitées) sont-elles disponibles ?
- Les routes `/fiches/...` sont-elles conservées pour compatibilité historique, ou peuvent-elles être redirigées définitivement vers `/fiches-par-thematique/...` ?

## Limites de vérification

- Aucun crawl HTTP de `prepcivique.com` n’a été effectué ; les statuts, headers CDN, redirections effectives, certificat, cache et contenu réellement servi par Cloudflare restent à confirmer en production.
- Aucune validation Google Rich Results, Schema Markup Validator, Lighthouse distante ou Search Console n’a été exécutée.
- Le rapport a été produit à partir du code et du build local disponibles le 2026-10-03.

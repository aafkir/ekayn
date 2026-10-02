# Ekayn

Site Astro de bijoux d’inspiration traditionnelle.

## Développement

```sh
npm install
cp .env.example .env
npm run dev
```

Le projet utilise l’adaptateur Cloudflare en rendu serveur pour les endpoints de contact, Checkout et webhook. Les pages publiques restent rendues par Astro.

## Configuration

Les variables sont listées dans `.env.example`. Ne commitez jamais `.env` ni de clé Stripe.

- `PUBLIC_SITE_URL` : domaine public du site, `https://ekayn.com`.
- `STRIPE_SECRET_KEY` : clé Stripe de test `sk_test_…`.
- `STRIPE_WEBHOOK_SECRET` : secret `whsec_…` du webhook.
- `RESEND_API_KEY`, `CONTACT_EMAIL`, `CONTACT_FROM` : service d’envoi du formulaire de contact.
- `SHIPPING_COUNTRIES` : pays autorisés, `FR` par défaut. La livraison reste à 0 € car c’est la seule règle actuellement définie.

## Stripe et Cloudflare

Le navigateur transmet uniquement les identifiants produits et les quantités. Les prix sont relus côté serveur dans `src/data/products.ts` et transmis à Stripe en centimes d’euros.

Créer une KV Cloudflare avec le binding `ORDERS` pour enregistrer durablement les commandes et rendre le webhook idempotent. Le paiement nécessite une clé Stripe de test ; aucun basculement en production n’est effectué.

Pour tester le webhook localement :

```sh
stripe listen --forward-to http://localhost:4321/api/stripe-webhook
```

Les pages `/success` et `/cancel` conservent le panier en cas d’annulation. La page de succès vérifie le statut auprès de Stripe côté serveur avant de retirer uniquement les quantités de la commande du panier.

## Contact

Le formulaire `/contact` utilise Resend côté serveur. Sans `RESEND_API_KEY` et `CONTACT_EMAIL`, il renvoie une erreur explicite et ne simule pas l’envoi. Un honeypot et une limitation simple de cinq messages par heure et adresse cliente sont activés.

## Vérifications

```sh
npm run check
npm run build
```

Les tests de paiement réels et de webhook nécessitent les identifiants Stripe de test et le binding KV `ORDERS`; ils restent à exécuter dans l’environnement Cloudflare configuré.

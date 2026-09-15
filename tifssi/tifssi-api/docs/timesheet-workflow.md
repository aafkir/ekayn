# Workflow CRA mensuel

Une Timesheet est unique par `(profileId, year, month)`. La première saisie du
mois crée automatiquement une feuille DRAFT. Toutes les missions du profil sur
ce mois partagent la même feuille. Un changement de mois/profil rattache la
saisie à la feuille correspondante, uniquement si les deux feuilles sont éditables.

| Action | État initial | État final |
| --- | --- | --- |
| Saisir / modifier / supprimer un TimeEntry | DRAFT ou REJECTED | Inchangé |
| Soumettre la feuille non vide | DRAFT ou REJECTED | SUBMITTED |
| Valider la feuille | SUBMITTED | VALIDATED |
| Rejeter avec motif | SUBMITTED | REJECTED |

Les TimeEntries sont verrouillés dans une feuille SUBMITTED ou VALIDATED.
Un rejet permet la correction puis une nouvelle soumission. Le motif et les
informations de rejet sont effacés lors de cette nouvelle soumission.

## API

- GET `/api/timesheets?profileId=…&year=2026&month=9` : filtres facultatifs,
  combinables indépendamment. Liste non paginée, triée par période décroissante.
- GET `/api/timesheets/{id}` : statut officiel et toutes les saisies du mois,
  triées par date puis identifiant.
- POST `/api/timesheets/{id}/submit` : aucune validation journalière.
- POST `/api/timesheets/{id}/validate?managerId=…` : managerId facultatif.
- POST `/api/timesheets/{id}/reject` : `{ "reason": "Journée à corriger", "managerId": 7 }`.
  Motif obligatoire, au plus 2000 caractères. managerId facultatif.

Paramètres/motif invalides : 400 ; ressource inexistante : 404 ; transition
interdite, feuille verrouillée ou écriture concurrente : 409.

Le champ historique `TimeEntry.status` est déprécié. Les anciens enregistrements
restent lisibles ; les nouvelles saisies ont DRAFT, omis ou fourni par le client.
POST/PATCH TimeEntry refuse VALIDATED : la validation individuelle n'existe plus.
Les transitions Timesheet ne modifient jamais ce champ historique. La facturation
sélectionne désormais les temps appartenant aux Timesheets VALIDATED.

Les absences conservent leur API et leurs statuts indépendants. Aucune action
Timesheet ne les modifie et elles ne déterminent jamais le statut du CRA.

## Persistance et migration

V7 rattache les saisies existantes à leur feuille profil/année/mois, conserve
les décisions mensuelles existantes et crée DRAFT pour les mois sans Timesheet.
Aucune approbation mensuelle n'est déduite des statuts journaliers historiques.
La relation devient obligatoire, avec un index et une version de concurrence.
La création initiale est sérialisée par profil ; une décision concurrente ne
peut pas écraser une autre décision ou une modification de saisie.

V8 corrige un écart préexistant : `currency_code` et `notes` du modèle Invoice
manquaient dans les migrations. La devise initiale EUR correspond au défaut
du service existant, sans écraser une valeur déjà présente.

## Seed DEV

En juillet 2026 : Nina DRAFT, Hugo SUBMITTED, Sara VALIDATED, Claire REJECTED avec
motif, deux saisies quotidiennes par feuille. Les statuts sont explicitement
mensuels et tous les TimeEntries DEV sont DRAFT. Les feuilles déjà présentes
ne sont pas réinitialisées lors d'un redémarrage.

Swagger est généré depuis les contrôleurs et DTO. Le test de documentation
vérifie les endpoints, le motif requis et exporte `target/openapi.json`.

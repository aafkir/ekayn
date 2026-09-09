# tifssi-api

Backend Java 21 / Spring Boot pour une application SaaS de gestion ESN.

## Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- Spring Web
- Spring Data JPA
- Bean Validation
- Lombok
- MapStruct
- springdoc OpenAPI

## Architecture

Le projet suit une architecture de monolithe modulaire orientee domaine.

Modules metier :

- `shared`
- `crm`
- `staffing`
- `projects`
- `timesheets`
- `expenses`
- `absences`
- `billing`

Chaque module expose les packages suivants :

- `api`
- `application`
- `domain`
- `infrastructure`

La couche `api` est prevue pour accueillir plus tard des sous-packages dedies a `backoffice` et `intranet` sur la meme API, sans casser les couches `application`, `domain` et `infrastructure`.

## Demarrage local

### Lancer PostgreSQL

Le projet embarque un `docker-compose.yml` minimal pour un PostgreSQL local.

```bash
docker compose up -d postgres
docker compose ps postgres
```

La base expose par defaut :

- host : `localhost`
- port : `5432`
- database : `tifssi`
- user : `tifssi`
- password : `tifssi`

### Lancer l'application Spring Boot

Avec la configuration par defaut :

```bash
mvn spring-boot:run
```

Ou avec les variables explicites :

```bash
export DB_URL=jdbc:postgresql://localhost:5432/tifssi
export DB_USERNAME=tifssi
export DB_PASSWORD=tifssi
mvn spring-boot:run
```

Pour charger le jeu de donnees de demo minimal :

```bash
export APP_DEMO_DATA_ENABLED=true
mvn spring-boot:run
```

### Verifier les endpoints techniques et la documentation

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/v3/api-docs
```

Swagger UI :

- metier + API REST : `http://localhost:8080/swagger-ui/index.html`
- spec OpenAPI metier : `http://localhost:8080/v3/api-docs`
- health check : `http://localhost:8080/actuator/health`

## Variables utiles

La configuration par defaut est prevue pour un PostgreSQL local :

- `DB_URL=jdbc:postgresql://localhost:5432/tifssi`
- `DB_USERNAME=tifssi`
- `DB_PASSWORD=tifssi`
- `APP_DEMO_DATA_ENABLED=false`

## Tests

Les tests unitaires couvrent les services metier, et les tests d'integration demarrent un PostgreSQL reel via Testcontainers.
Docker doit donc etre disponible localement.

```bash
mvn test
```

## Migration BDD

Flyway execute automatiquement les scripts situes dans `src/main/resources/db/migration`.
La migration initiale `V1__init.sql` cree les schemas metier, les tables de la MVP, les cles et les index associes.

## API REST MVP

CRUD et workflows disponibles sans securite sur les ressources suivantes :

- `GET|POST /api/companies`
- `GET|PATCH|DELETE /api/companies/{id}`
- `GET|POST /api/contacts`
- `GET|PATCH|DELETE /api/contacts/{id}`
- `GET|POST /api/needs`
- `GET|PATCH|DELETE /api/needs/{id}`
- `POST /api/needs/{needId}/win-and-create-project`
- `GET|POST /api/profiles`
- `GET|PATCH|DELETE /api/profiles/{id}`
- `GET /api/submissions/{id}`
- `POST /api/submissions`
- `GET /api/submissions?needId=...`
- `GET /api/submissions?profileId=...`
- `PATCH /api/submissions/{id}/status`
- `PATCH /api/submissions/{id}`
- `DELETE /api/submissions/{id}`
- `GET|POST /api/projects`
- `GET|PATCH|DELETE /api/projects/{id}`
- `GET|POST /api/missions`
- `GET|PATCH|DELETE /api/missions/{id}`
- `GET|POST /api/expenses`
- `PATCH|DELETE /api/expenses/{id}`
- `GET /api/expenses/summary`
- `GET|POST /api/time-entries`
- `PATCH|DELETE /api/time-entries/{id}`
- `GET /api/time-entries/summary`
- `GET|POST /api/absences`
- `PATCH|DELETE /api/absences/{id}`
- `GET /api/absences/summary`
- `GET|POST /api/projects/{projectId}/invoices`
- `GET|DELETE /api/invoices/{id}`
- `POST /api/invoices/{id}/lines`
- `POST /api/invoices/{id}/generate-from-times-and-expenses`

Les reponses utilisent des ids `Long`, des dates ISO-8601 et des erreurs JSON standardisees.

## Postman / Newman

Collection Postman :

- [postman/tifssi-api.postman_collection.json](/Users/afkir/Desktop/dev/tifssi/postman/tifssi-api.postman_collection.json)
- [postman/tifssi-api.local.postman_environment.json](/Users/afkir/Desktop/dev/tifssi/postman/tifssi-api.local.postman_environment.json)

Execution avec Newman :

```bash
newman run postman/tifssi-api.postman_collection.json \
  -e postman/tifssi-api.local.postman_environment.json
```

La collection :

- initialise et reutilise les ids entre les requetes
- suit les endpoints exposes dans les controllers Spring MVC du projet
- couvre les workflows CRM, staffing, projects, expenses, timesheets, absences et billing
- contient un cas d'erreur de validation standardise pour `Company`

## Exemples de payloads

### Company

```json
{
  "legalName": "Acme Conseil",
  "displayName": "Acme",
  "registrationNumber": "RCS-123456",
  "vatNumber": "FR12345678901",
  "websiteUrl": "https://acme.example",
  "emailAddress": "contact@acme.example",
  "phoneNumber": "+33102030405",
  "billingAddress": "12 rue de Paris",
  "cityName": "Paris",
  "postalCode": "75001",
  "countryCode": "FR"
}
```

### Contact

```json
{
  "companyId": 1,
  "firstName": "Lea",
  "lastName": "Martin",
  "jobTitle": "Directrice achats",
  "emailAddress": "lea.martin@acme.example",
  "phoneNumber": "+33601020304",
  "primaryContact": true
}
```

### Need

```json
{
  "companyId": 1,
  "contactId": 1,
  "needReference": "NEED-2026-001",
  "title": "Consultant Java Senior",
  "description": "Mission de build backend Spring Boot.",
  "status": "OPEN",
  "startDate": "2026-05-01",
  "endDate": "2026-12-31",
  "locationName": "Paris",
  "remotePossible": true,
  "targetDailyRate": 650.00
}
```

### Profile

```json
{
  "type": "INTERNAL",
  "firstName": "Nina",
  "lastName": "Dupont",
  "emailAddress": "nina.dupont@tifssi.example",
  "phoneNumber": "+33611121314",
  "jobTitle": "Developpeuse backend",
  "seniorityLabel": "Senior",
  "active": true,
  "defaultDailyRate": 700.00,
  "availabilityDate": "2026-04-15"
}
```

### Submission

```json
{
  "needId": 1,
  "profileId": 1,
  "proposedDailyRate": 650.00,
  "comment": "First shortlist"
}
```

### Project

```json
{
  "companyId": 1,
  "originNeedId": 1,
  "projectCode": "PRJ-2026-001",
  "projectName": "Plateforme staffing",
  "description": "Projet gagne suite au besoin NEED-2026-001.",
  "status": "ACTIVE",
  "startDate": "2026-05-01",
  "endDate": "2026-12-31",
  "budgetAmount": 120000.00
}
```

### Mission

```json
{
  "projectId": 1,
  "profileId": 1,
  "roleName": "Lead Backend",
  "startDate": "2026-05-01",
  "endDate": "2026-10-31",
  "dailyRate": 700.00,
  "allocationPercent": 100,
  "status": "ACTIVE"
}
```

### Expense

```json
{
  "missionId": 1,
  "profileId": 1,
  "expenseDate": "2026-05-06",
  "category": "TRAVEL",
  "amount": 120.50,
  "currency": "EUR",
  "comment": "Taxi aeroport",
  "receiptUrl": "https://example.com/receipt.pdf",
  "status": "DRAFT",
  "billable": true
}
```

### Time Entry

```json
{
  "missionId": 1,
  "profileId": 1,
  "workDate": "2026-05-06",
  "quantity": 1,
  "unitType": "DAY",
  "comment": "Kickoff client",
  "status": "DRAFT"
}
```

### Absence

```json
{
  "profileId": 1,
  "type": "PAID_LEAVE",
  "startDate": "2026-07-10",
  "endDate": "2026-07-12",
  "quantity": 3.00,
  "comment": "Conges d'ete",
  "status": "SUBMITTED"
}
```

### Invoice

```json
{
  "invoiceNumber": "INV-2026-001",
  "issueDate": "2026-07-01",
  "dueDate": "2026-07-31",
  "status": "DRAFT"
}
```

### Invoice Line

```json
{
  "lineType": "FIXED_FEE",
  "description": "Setup package",
  "quantity": 1.00,
  "unit": "PACKAGE",
  "unitPrice": 1000.00,
  "vatRate": 20.00
}
```

### Exemple PATCH

```json
{
  "status": "WON",
  "locationName": "Lyon"
}
```

## Format d'erreur

Exemple de reponse d'erreur de validation :

```json
{
  "timestamp": "2026-04-06T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/api/companies",
  "fieldErrors": [
    {
      "field": "legalName",
      "message": "must not be blank"
    }
  ]
}
```

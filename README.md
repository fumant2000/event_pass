# EventPass — Backend API

> API REST Spring Boot pour la gestion d'événements et de billetterie avec authentification JWT.

---

## Table des matières

- [Présentation](#présentation)
- [Stack technique](#stack-technique)
- [Prérequis](#prérequis)
- [Installation et démarrage](#installation-et-démarrage)
- [Configuration](#configuration)
- [Structure du projet](#structure-du-projet)
- [Authentification](#authentification)
- [Endpoints API](#endpoints-api)
  - [Auth](#-auth---apiauthxxxxx)
  - [Événements](#-événements---apieventsxxxxx)
  - [Tickets](#-tickets---apiticketsxxxxx)
  - [Transactions](#-transactions---apitransactionsxxxxx)
- [Rôles et permissions](#rôles-et-permissions)
- [Tester avec Postman](#tester-avec-postman)
- [Base de données](#base-de-données)
- [Conventions de contribution](#conventions-de-contribution)
- [Équipe](#équipe)

---

## Présentation

EventPass est une application mobile de gestion d'événements et de billetterie. Ce dépôt contient le **backend** qui expose une API REST consommée par l'application Kotlin Android.

**Fonctionnalités principales :**

- Inscription / connexion avec JWT
- Trois rôles : `USER`, `ORGANIZER`, `ADMIN`
- Création, modification et annulation d'événements
- Achat de tickets avec génération d'un QR code unique (UUID)
- Validation à l'entrée par scan QR (organisateur)
- Remboursement automatique en cas d'annulation d'événement
- Historique complet des transactions

---

## Stack technique

| Composant | Technologie |
|---|---|
| Langage | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Sécurité | Spring Security 7 + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + Hibernate 7 |
| Base de données | H2 (mémoire — dev) |
| Build | Maven |
| Utilitaires | Lombok |

---

## Prérequis

- Java 17+ ([télécharger](https://adoptium.net/))
- Maven 3.8+ (ou utiliser le wrapper `./mvnw` inclus)
- Un client REST pour tester : [Postman](https://www.postman.com/) ou [Insomnia](https://insomnia.rest/)

Vérifier l'installation :
```bash
java -version    # doit afficher 17.x.x
./mvnw -version  # doit afficher Apache Maven 3.x.x
```

---

## Installation et démarrage

```bash
# 1. Cloner le projet
git clone https://github.com/votre-org/eventpass-backend.git
cd eventpass-backend

# 2. Lancer le serveur
./mvnw spring-boot:run
```

Au démarrage, la console affiche :

```
Hibernate: create table users ...
Hibernate: create table events ...
Hibernate: create table tickets ...
Hibernate: create table transactions ...
✅ Admin créé : admin@eventpass.com / Admin1234!
Tomcat started on port 8080
Started BackendApplication in 2.3 seconds
```

L'API est disponible sur **`http://localhost:8080`**

> **Note :** La base de données est en mémoire (H2). Toutes les données sont réinitialisées à chaque redémarrage. C'est intentionnel en développement.

---

## Configuration

Fichier : `src/main/resources/application.properties`

```properties
# Application
spring.application.name=eventpass-backend

# Base de données H2 en mémoire
spring.datasource.url=jdbc:h2:mem:eventpass
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# JWT
app.jwt.secret=EventPass2024SuperSecretKeyThatIsVeryLongAndSecure!!
app.jwt.expiration=86400000   # 24 heures en millisecondes

# Serveur
server.port=8080
```

### Passer en base persistante (optionnel)

Pour conserver les données entre les redémarrages, modifier deux lignes :

```properties
spring.datasource.url=jdbc:h2:file:./data/eventpass;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=update
```

---

## Structure du projet

```
src/main/java/com/eventpass/backend/
│
├── config/
│   ├── SecurityConfig.java          # Règles Spring Security, filtres, rôles
│   ├── JwtAuthFilter.java           # Filtre JWT (lecture du header Authorization)
│   ├── JwtService.java              # Génération et validation des tokens JWT
│   └── DataInitializer.java         # Création du compte admin au démarrage
│
├── controller/
│   ├── AuthController.java          # /api/auth/**
│   ├── EventController.java         # /api/events/**
│   ├── TicketController.java        # /api/tickets/**
│   └── TransactionController.java   # /api/transactions/**
│
├── dto/
│   ├── request/                     # Corps des requêtes entrantes (validation @Valid)
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── EventRequest.java
│   │   ├── BuyTicketRequest.java
│   │   └── ValidateTicketRequest.java
│   └── response/                    # Corps des réponses sortantes
│       ├── AuthResponse.java
│       ├── EventResponse.java
│       ├── TicketResponse.java
│       ├── TransactionResponse.java
│       └── MessageResponse.java
│
├── entity/                          # Entités JPA (tables en base)
│   ├── User.java
│   ├── Event.java
│   ├── Ticket.java
│   └── Transaction.java
│
├── enums/
│   ├── Role.java                    # USER | ORGANIZER | ADMIN
│   ├── UserStatus.java              # ACTIVE | SUSPENDED | PENDING_ORGANIZER
│   ├── EventStatus.java             # PUBLISHED | CANCELLED | COMPLETED
│   ├── TicketStatus.java            # VALID | USED | REFUNDED
│   └── TransactionType.java         # PURCHASE | REFUND
│
├── repository/                      # Interfaces Spring Data JPA
│   ├── UserRepository.java
│   ├── EventRepository.java
│   ├── TicketRepository.java
│   └── TransactionRepository.java
│
├── service/                         # Logique métier
│   ├── CustomUserDetailsService.java
│   ├── AuthService.java
│   ├── EventService.java
│   ├── TicketService.java
│   └── TransactionService.java
│
└── BackendApplication.java          # Point d'entrée Spring Boot
```

---

## Authentification

L'API utilise **JWT (JSON Web Token)**. Chaque requête protégée doit inclure le token dans le header HTTP :

```
Authorization: Bearer <votre_token_jwt>
```

**Flux complet :**

```
POST /api/auth/register  ──→  { token, role, userId, email, name }
POST /api/auth/login     ──→  { token, role, userId, email, name }
                                         │
                              Copier le token
                                         │
                                         ▼
         Header : Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Le token expire après **24 heures**. Il faut se reconnecter pour en obtenir un nouveau.

---

## Endpoints API

### 🔐 Auth — `/api/auth/xxxxx`

#### `POST /api/auth/register` — Créer un compte
> Accès libre

**Corps de la requête :**
```json
{
  "name": "Alice Martin",
  "email": "alice@example.com",
  "password": "motdepasse123",
  "phone": "655000001"
}
```

**Réponse `200` :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 2,
  "name": "Alice Martin",
  "email": "alice@example.com",
  "role": "USER"
}
```

---

#### `POST /api/auth/login` — Se connecter
> Accès libre

**Corps de la requête :**
```json
{
  "email": "alice@example.com",
  "password": "motdepasse123"
}
```

**Réponse `200` :** même format que `/register`

---

#### `POST /api/auth/request-organizer` — Demander le rôle organisateur
> 🔒 Requiert un token JWT

Pas de corps. Le statut passe à `PENDING_ORGANIZER` en attente de validation admin.

**Réponse `200` :**
```json
{
  "message": "Demande de statut organisateur soumise",
  "success": true
}
```

---

#### `POST /api/auth/approve-organizer/{userId}` — Approuver un organisateur
> 🔒 ADMIN uniquement

**Exemple :** `POST /api/auth/approve-organizer/3`

**Réponse `200` :**
```json
{
  "message": "Utilisateur promu organisateur",
  "success": true
}
```

---

#### `POST /api/auth/forgot-password?email=xxx` — Mot de passe oublié
> Accès libre

**Exemple :** `POST /api/auth/forgot-password?email=alice@example.com`

**Réponse `200` :**
```json
{
  "message": "Token de reset : 550e8400-e29b-41d4-a716",
  "success": true
}
```
> En développement, le token est retourné directement. En production, il serait envoyé par email.

---

#### `POST /api/auth/reset-password?token=xxx&newPassword=yyy` — Réinitialiser le mot de passe
> Accès libre

**Réponse `200` :**
```json
{
  "message": "Mot de passe mis à jour",
  "success": true
}
```

---

### 📅 Événements — `/api/events/xxxxx`

#### `GET /api/events` — Liste des événements publiés
> Accès libre

**Réponse `200` :**
```json
[
  {
    "id": 1,
    "title": "Concert Jazz Yaoundé",
    "description": "Soirée jazz au Palais des Congrès",
    "location": "Palais des Congrès, Yaoundé",
    "category": "Musique",
    "eventDate": "2025-08-15T20:00:00",
    "capacity": 200,
    "availableSeats": 147,
    "price": 5000.0,
    "status": "PUBLISHED",
    "organizerName": "Bob Dupont",
    "organizerId": 3,
    "createdAt": "2025-05-17T10:00:00"
  }
]
```

---

#### `GET /api/events/{id}` — Détail d'un événement
> Accès libre

**Exemple :** `GET /api/events/1`

**Réponse `200` :** même format qu'un élément de la liste ci-dessus.

**Réponse `500` si introuvable :**
```json
{ "message": "Événement introuvable" }
```

---

#### `POST /api/events` — Créer un événement
> 🔒 ORGANIZER ou ADMIN

**Corps de la requête :**
```json
{
  "title": "Concert Jazz Yaoundé",
  "description": "Soirée jazz au Palais des Congrès",
  "location": "Palais des Congrès, Yaoundé",
  "category": "Musique",
  "imageUrl": "https://example.com/image.jpg",
  "eventDate": "2025-08-15T20:00:00",
  "capacity": 200,
  "price": 5000.0
}
```

**Réponse `200` :** l'événement créé au format `EventResponse`

---

#### `PUT /api/events/{id}` — Modifier un événement
> 🔒 ORGANIZER (propriétaire uniquement) ou ADMIN

Même format de corps que la création. La capacité ne peut pas être inférieure au nombre de tickets déjà vendus.

**Réponse `200` :** l'événement modifié

---

#### `POST /api/events/{id}/cancel` — Annuler un événement + remboursements
> 🔒 ORGANIZER (propriétaire uniquement) ou ADMIN

Pas de corps. Annule l'événement **et rembourse automatiquement** tous les tickets `VALID`.

**Réponse `200` :**
```json
{
  "message": "Événement annulé, remboursements en cours",
  "success": true
}
```

---

#### `GET /api/events/my` — Mes événements (organisateur)
> 🔒 ORGANIZER ou ADMIN

**Réponse `200` :** liste des événements créés par l'utilisateur connecté

---

### 🎫 Tickets — `/api/tickets/xxxxx`

#### `POST /api/tickets/buy` — Acheter un ticket
> 🔒 Tout utilisateur authentifié

**Corps de la requête :**
```json
{
  "eventId": 1
}
```

**Réponse `200` :**
```json
{
  "id": 12,
  "qrCode": "550e8400-e29b-41d4-a716-446655440000",
  "eventId": 1,
  "eventTitle": "Concert Jazz Yaoundé",
  "eventLocation": "Palais des Congrès, Yaoundé",
  "eventDate": "2025-08-15T20:00:00",
  "amount": 5000.0,
  "status": "VALID",
  "purchasedAt": "2025-05-17T14:32:00",
  "usedAt": null
}
```

> Le champ `qrCode` (UUID) est la **valeur à encoder en QR code** dans l'application mobile.

---

#### `GET /api/tickets/my` — Mes tickets
> 🔒 Tout utilisateur authentifié

**Réponse `200` :** liste de tous les tickets de l'utilisateur connecté, triés du plus récent au plus ancien.

---

#### `POST /api/tickets/validate` — Scanner et valider un QR code
> 🔒 ORGANIZER ou ADMIN

**Corps de la requête :**
```json
{
  "qrCode": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Réponse `200` (ticket valide) :**
```json
{
  "message": "✅ Entrée validée — Bienvenue Alice Martin",
  "success": true
}
```

**Réponse `200` (ticket déjà utilisé) :**
```json
{
  "message": "Ticket déjà utilisé le 2025-08-15T20:45:00",
  "success": false
}
```

---

#### `DELETE /api/tickets/{ticketId}` — Annuler son propre ticket
> 🔒 Tout utilisateur authentifié (propriétaire du ticket uniquement)

**Exemple :** `DELETE /api/tickets/12`

**Réponse `200` :**
```json
{
  "message": "Ticket annulé et remboursé",
  "success": true
}
```

---

### 💳 Transactions — `/api/transactions/xxxxx`

#### `GET /api/transactions/history` — Historique des transactions
> 🔒 Tout utilisateur authentifié

**Réponse `200` :**
```json
[
  {
    "id": 5,
    "amount": 5000.0,
    "type": "PURCHASE",
    "reference": "PUR-A1B2C3D4",
    "description": "Achat ticket : Concert Jazz Yaoundé",
    "createdAt": "2025-05-17T14:32:00",
    "ticketId": 12,
    "eventTitle": "Concert Jazz Yaoundé"
  },
  {
    "id": 8,
    "amount": 5000.0,
    "type": "REFUND",
    "reference": "REF-E5F6G7H8",
    "description": "Remboursement : Concert Jazz Yaoundé (annulé)",
    "createdAt": "2025-05-18T09:00:00",
    "ticketId": 12,
    "eventTitle": "Concert Jazz Yaoundé"
  }
]
```

---

## Rôles et permissions

| Endpoint | Libre | USER | ORGANIZER | ADMIN |
|---|:---:|:---:|:---:|:---:|
| GET /api/events | ✅ | ✅ | ✅ | ✅ |
| GET /api/events/{id} | ✅ | ✅ | ✅ | ✅ |
| POST /api/events | | | ✅ | ✅ |
| PUT /api/events/{id} | | | ✅ (owner) | ✅ |
| POST /api/events/{id}/cancel | | | ✅ (owner) | ✅ |
| GET /api/events/my | | | ✅ | ✅ |
| POST /api/tickets/buy | | ✅ | ✅ | ✅ |
| GET /api/tickets/my | | ✅ | ✅ | ✅ |
| POST /api/tickets/validate | | | ✅ | ✅ |
| DELETE /api/tickets/{id} | | ✅ (owner) | ✅ (owner) | ✅ |
| GET /api/transactions/history | | ✅ | ✅ | ✅ |
| POST /api/auth/approve-organizer | | | | ✅ |

---

## Tester avec Postman

### 1. Importer les variables d'environnement

Créer un environnement Postman avec ces variables :

| Variable | Valeur initiale |
|---|---|
| `base_url` | `http://localhost:8080` |
| `token` | *(vide — rempli automatiquement)* |
| `admin_token` | *(vide — rempli avec le token admin)* |

### 2. Auto-sauvegarder le token

Dans l'onglet **Tests** de la requête `/api/auth/login` (ou `/register`), coller ce script :

```javascript
const response = pm.response.json();
if (response.token) {
    pm.environment.set("token", response.token);
    console.log("✅ Token sauvegardé :", response.role);
}
```

### 3. Utiliser le token dans toutes les requêtes

Dans l'onglet **Authorization** de chaque requête :
- Type : `Bearer Token`
- Token : `{{token}}`

### 4. Scénario de test complet

```
1.  POST {{base_url}}/api/auth/register         → créer compte user
2.  POST {{base_url}}/api/auth/login            → connexion admin (admin@eventpass.com)
    → sauvegarder dans {{admin_token}}
3.  POST {{base_url}}/api/auth/request-organizer (avec {{token}} user)
4.  POST {{base_url}}/api/auth/approve-organizer/2 (avec {{admin_token}})
5.  POST {{base_url}}/api/auth/login            → reconnecter user (il est maintenant ORGANIZER)
6.  POST {{base_url}}/api/events                → créer un événement
7.  GET  {{base_url}}/api/events                → vérifier que l'événement apparaît
8.  POST {{base_url}}/api/tickets/buy { "eventId": 1 }   → acheter un ticket
9.  GET  {{base_url}}/api/tickets/my            → voir le ticket et copier le qrCode
10. POST {{base_url}}/api/tickets/validate { "qrCode": "..." }
11. GET  {{base_url}}/api/transactions/history  → voir l'historique
```

### Compte admin par défaut

```
Email    : admin@eventpass.com
Password : Admin1234!
```

---

## Base de données

### Console H2 (interface web)

Accessible pendant que le serveur tourne :

```
URL     : http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:eventpass
Username: sa
Password: (laisser vide)
```

### Schéma des tables

```
users
  id, name, email, password, phone, role, status,
  reset_token, reset_token_expiry, created_at

events
  id, title, description, location, category, image_url,
  event_date, capacity, available_seats, price, status,
  organizer_id (FK→users), created_at, updated_at

tickets
  id, qr_code (unique), user_id (FK→users),
  event_id (FK→events), status, amount,
  purchased_at, used_at

transactions
  id, user_id (FK→users), ticket_id (FK→tickets),
  amount, type, reference, description, created_at
```

---

## Conventions de contribution

### Branches Git

```
main          → code stable, démo
develop       → intégration continue
feature/xxx   → nouvelles fonctionnalités  (ex: feature/email-otp)
fix/xxx       → corrections de bugs        (ex: fix/ticket-refund)
```

### Workflow

```bash
# 1. Créer une branche depuis develop
git checkout develop
git checkout -b feature/ma-fonctionnalite

# 2. Coder + commiter
git add .
git commit -m "feat: description courte de la fonctionnalité"

# 3. Pousser et ouvrir une Pull Request vers develop
git push origin feature/ma-fonctionnalite
```

### Convention de commits

```
feat:     nouvelle fonctionnalité
fix:      correction de bug
refactor: réécriture sans changement de comportement
docs:     documentation uniquement
test:     ajout ou modification de tests
chore:    tâches de maintenance (dépendances, config)
```

### Ajouter un nouvel endpoint

1. Créer/modifier le DTO dans `dto/request/` ou `dto/response/`
2. Ajouter la méthode dans le `Service` correspondant
3. Ajouter la route dans le `Controller` correspondant
4. Si nouvel accès public → mettre à jour `SecurityConfig.java`
5. Mettre à jour ce README (section **Endpoints API**)

---

## Équipe

| Rôle | Responsable |
|---|---|
| Backend (ce dépôt) | — |
| Frontend Android (Kotlin) | — |
| Design UI/UX | — |
| Documentation | — |

---

## Liens utiles

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security 7 Migration Guide](https://docs.spring.io/spring-security/reference/migration/index.html)
- [jjwt Documentation](https://github.com/jwtk/jjwt)
- [H2 Database Console](http://localhost:8080/h2-console) *(serveur démarré requis)*

---




// Utilisateur normal
POST /api/auth/request-organizer
→ { message: "Demande soumise", success: true }

// Admin — voir les demandes
GET /api/admin/organizer-requests
→ [
    { userId: 3, name: "Alice", email: "alice@..", status: "PENDING_ORGANIZER", ... },
    { userId: 5, name: "Bob",   email: "bob@..",   status: "PENDING_ORGANIZER", ... }
  ]

// Admin — approuver
POST /api/admin/organizer-requests/3/approve
→ { message: "✅ Alice est maintenant organisateur", success: true }

// Admin — rejeter
POST /api/admin/organizer-requests/5/reject
→ { message: "Demande de Bob refusée", success: true }

// Dashboard admin au login
GET /api/admin/stats
→ {
    totalUsers: 42,
    totalOrganizers: 7,
    pendingOrganizerRequests: 2,   ← badge de notification
    totalEvents: 15,
    activeEvents: 8,
    totalTicketsSold: 312,
    totalRevenue: 1560000
  }


  *Projet réalisé dans le cadre de l'exposé Java — Génie Logiciel*

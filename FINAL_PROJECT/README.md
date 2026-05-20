# FINAL_PROJECT — Chatroom SOAP + REST

Projet final du cours **Web Services** (DIC3 2026, ESP DGI).

Application de **chatroom distribuée** demontrant **deux protocoles** (SOAP puis REST) avec des **microservices polyglottes** (Java, Node.js, Python) consommes par un meme frontend PHP via le pattern **Port + 2 Adapters**.

## Sommaire

- [Objectifs pedagogiques](#objectifs-pedagogiques)
- [Architecture](#architecture)
- [Pattern Port + 2 Adapters](#pattern-port--2-adapters)
- [Pre-requis](#pre-requis)
- [Demarrage rapide](#demarrage-rapide)
- [Phase 1 — SOAP](#phase-1--soap)
- [Phase 2 — REST](#phase-2--rest)
- [Bascule SOAP / REST](#bascule-soap--rest)
- [Tests par curl](#tests-par-curl)
- [Demonstration interop](#demonstration-interop)
- [Structure du depot](#structure-du-depot)
- [Schema de la base de donnees](#schema-de-la-base-de-donnees)
- [Points pedagogiques cles](#points-pedagogiques-cles)
- [Depannage](#depannage)

---

## Objectifs pedagogiques

1. Mettre en oeuvre **deux protocoles de services web** sur un meme cas metier :
   - **SOAP** (contrat WSDL, enveloppes XML)
   - **REST** (ressources, verbes HTTP, content negotiation **JSON + XML**)
2. Composer **deux services autonomes** par phase qui **communiquent entre eux** via le protocole de la phase (et non via la base) :
   - `AuthService` (register / login / logout / validateToken)
   - `ChatService` (createRoom / listRooms / sendMessage / getMessages)
3. Demontrer **l'interoperabilite reelle** en faisant cohabiter **trois langages** :
   - Java (JAX-WS et JAX-RS)
   - Node.js (node-soap)
   - Python (Flask)
4. Cote frontend, appliquer le pattern **Port + Adapters** pour basculer entre SOAP et REST **sans modifier les pages**.
5. Continuer la progression du cours :
   - **TP0** : Java RMI (callback)
   - **TP1** : XML-RPC (polling + dual-channel)
   - **FINAL_PROJECT** : SOAP + REST polyglottes

---

## Architecture

```
                       ┌────────────────────────────────────┐
                       │     Frontend PHP — port 8000       │
                       │  ClientInterface (PORT)            │
                       │  ├─ SoapBackendClient (Adapter 1)  │
                       │  └─ RestBackendClient (Adapter 2)  │
                       │  Selection par BACKEND_MODE        │
                       └─────────┬───────────────┬──────────┘
                                 │               │
                       BACKEND_MODE=soap   BACKEND_MODE=rest
                                 │               │
            ┌────────────────────┘               └────────────────────┐
            ▼                                                          ▼
  ┌──────────────────────────┐                       ┌──────────────────────────┐
  │ soap-auth-java           │                       │ rest-auth-java           │
  │ Java 21 / JAX-WS Metro   │                       │ Java 21 / JAX-RS Jersey  │
  │ port 9001 (/auth?wsdl)   │                       │ + Grizzly  port 8081     │
  │ DB: chatroom_auth_soap   │                       │ DB: chatroom_auth_rest   │
  └────────────┬─────────────┘                       └────────────┬─────────────┘
               │ SOAP validateToken                              │ REST GET /validate
               ▼                                                  ▼
  ┌──────────────────────────┐                       ┌──────────────────────────┐
  │ soap-chat-node           │                       │ rest-chat-python         │
  │ Node 20 / node-soap      │                       │ Python 3.12 / Flask      │
  │ + Express   port 9002    │                       │ + xmltodict  port 8082   │
  │ DB: chatroom_chat_soap   │                       │ DB: chatroom_chat_rest   │
  └──────────────────────────┘                       └──────────────────────────┘

                       ┌────────────────────────────────────┐
                       │ PostgreSQL 16 (Docker) — port 5432 │
                       │ 4 bases : chatroom_{auth,chat}_*   │
                       └────────────────────────────────────┘
```

**Docker n'est utilise que pour PostgreSQL.** Les services applicatifs (Java, Node, Python, PHP) sont lances en local.

---

## Pattern Port + 2 Adapters

Cote frontend PHP, **les pages ne dependent que d'une interface metier abstraite** (`ClientInterface`). Deux implementations concretes coexistent et sont selectionnees par variable d'environnement.

```
frontend-php/src/
├── Client/
│   ├── ClientInterface.php         ← LE PORT (interface metier)
│   ├── BackendException.php
│   ├── SoapBackendClient.php       ← ADAPTER 1 (Phase 1 — wrap 2 SoapClient)
│   └── RestBackendClient.php       ← ADAPTER 2 (Phase 2 — cURL JSON)
└── ClientFactory.php               ← selectionne l'adapter selon BACKEND_MODE
```

Les pages (`login.php`, `register.php`, `chat.php`, `api-proxy.php`) appellent uniquement `ClientFactory::make()->methode(...)`. **La bascule SOAP↔REST se fait par variable d'environnement, sans toucher au code applicatif.**

---

## Pre-requis

| Outil | Version minimum | Verification |
|---|---|---|
| Docker + Docker Compose | 20+ | `docker --version` |
| Java JDK | 17+ (testé 21) | `java --version` |
| Maven | 3.8+ (resout les jars la premiere fois) | `mvn --version` |
| Node.js | 18+ | `node --version` |
| Python | 3.10+ avec `venv` | `python3 --version` |
| PHP CLI | 8.2+ avec `ext-soap`, `ext-curl`, `ext-xml` | `php -m \| grep -E '(soap\|curl)'` |

### Installer les extensions PHP manquantes (Debian/Ubuntu)

```bash
sudo apt update
sudo apt install -y php-soap php-curl php-xml
```

---

## Demarrage rapide

```bash
cd FINAL_PROJECT
cp .env.example .env                  # PostgreSQL password, BACKEND_MODE
docker compose up -d postgres         # lance PostgreSQL + cree les 4 bases
docker compose ps                     # postgres doit etre "healthy"
docker compose exec postgres psql -U postgres -c '\l' | grep chatroom
```

La sortie attendue liste les 4 bases :
```
 chatroom_auth_rest    | postgres | UTF8 ...
 chatroom_auth_soap    | postgres | UTF8 ...
 chatroom_chat_rest    | postgres | UTF8 ...
 chatroom_chat_soap    | postgres | UTF8 ...
```

---

## Phase 1 — SOAP

Trois processus dans trois terminaux distincts. Les deux services backend sont **autonomes** et communiquent en SOAP.

### Terminal A — AuthService Java/JAX-WS (port 9001)

```bash
cd soap/auth-service
./run.sh
```

Premier lancement : Maven telecharge les jars JAX-WS (Metro) dans `lib/` (operation effectuee une seule fois). Puis `javac` compile, puis `java` publie l'endpoint.

**Verification :**
```bash
curl -s http://localhost:9001/auth?wsdl | head -20
```

### Terminal B — ChatService Node.js/node-soap (port 9002)

```bash
cd soap/chat-service
npm install                # premiere fois seulement
node src/app.js
```

**Verification :**
```bash
curl -s http://localhost:9002/chat?wsdl | head -20
```

Le ChatService Node appelle l'AuthService Java en SOAP a chaque operation pour valider le token.

### Terminal C — Frontend PHP (port 8000)

```bash
cd frontend-php
BACKEND_MODE=soap php -S 0.0.0.0:8000 -t public
```

Ouvrir `http://localhost:8000` dans deux navigateurs (ou onglets prives) :
1. Creer deux comptes (alice, bob)
2. Creer un salon "general"
3. Envoyer des messages — polling 1.5 s les remonte chez l'autre utilisateur

---

## Phase 2 — REST

Meme principe, trois processus dans trois terminaux. Le ChatService Python appelle l'AuthService Java en REST pour valider le token. Les services REST supportent **JSON et XML** via `Accept`.

### Terminal A — AuthService Java/Jersey+Grizzly (port 8081)

```bash
cd rest/auth-service-java
./run.sh
```

### Terminal B — ChatService Python/Flask (port 8082)

```bash
cd rest/chat-service-python
./run.sh
```

Premier lancement : creation d'un venv et installation de `flask`, `xmltodict`, `psycopg`, `requests`.

### Terminal C — Frontend PHP (port 8000)

```bash
cd frontend-php
BACKEND_MODE=rest php -S 0.0.0.0:8000 -t public
```

**Note** : les bases SOAP et REST sont **distinctes** — recreer alice/bob lors du basculement.

---

## Bascule SOAP / REST

Le frontend PHP lit la variable `BACKEND_MODE` au demarrage. Pour basculer, **changer la valeur et relancer `php -S`** :

```bash
# Phase SOAP
BACKEND_MODE=soap php -S 0.0.0.0:8000 -t public

# Phase REST
BACKEND_MODE=rest php -S 0.0.0.0:8000 -t public
```

**Aucune ligne de code applicatif ne change** — toute la bascule passe par `ClientFactory::make()`.

---

## Tests par curl

### AuthService SOAP (Java)

```bash
curl -X POST http://localhost:9001/auth \
  -H 'Content-Type: text/xml; charset=utf-8' \
  -H 'SOAPAction: ""' \
  -d '<?xml version="1.0"?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"
            xmlns:auth="http://chatroom.dic3/auth">
  <S:Body>
    <auth:register>
      <username>alice</username>
      <password>secret</password>
    </auth:register>
  </S:Body>
</S:Envelope>'
```

### AuthService REST (Java) — reponse JSON

```bash
curl -X POST -H 'Content-Type: application/json' \
     -d '{"username":"alice","password":"secret"}' \
     http://localhost:8081/register
```

### AuthService REST (Java) — reponse XML

```bash
TOKEN=$(curl -s -X POST -H 'Content-Type: application/json' \
        -d '{"username":"alice","password":"secret"}' \
        http://localhost:8081/login | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')

curl -H "Authorization: Bearer $TOKEN" \
     -H 'Accept: application/xml' \
     http://localhost:8081/validate
```

### ChatService REST (Python) — JSON et XML

```bash
# Liste des salons en JSON
curl -H "Authorization: Bearer $TOKEN" -H 'Accept: application/json' \
     http://localhost:8082/rooms

# Memes salons en XML
curl -H "Authorization: Bearer $TOKEN" -H 'Accept: application/xml' \
     http://localhost:8082/rooms

# Envoyer un message
curl -X POST -H "Authorization: Bearer $TOKEN" \
     -H 'Content-Type: application/json' \
     -d '{"content":"Bonjour !"}' \
     http://localhost:8082/rooms/1/messages
```

---

## Demonstration interop

Pour illustrer la composition de services distribues, **couper l'AuthService pendant que le frontend tourne** :

```bash
# Phase 1 (SOAP) — couper le service Java pendant que le ChatService Node tourne
Ctrl+C sur le terminal A (soap-auth-java)
# Envoyer un message depuis le navigateur → erreur propre dans la console
# Le ChatService Node ne crashe pas, il renvoie une fault SOAP

# Phase 2 (REST) — couper le service Java pendant que ChatService Python tourne
Ctrl+C sur le terminal A (rest-auth-java)
# Envoyer un message → erreur HTTP 502/401 propre cote frontend
```

---

## Structure du depot

```
FINAL_PROJECT/
├── README.md                                    ← ce fichier
├── .env.example
├── .gitignore
├── docker-compose.yml                           ← postgres uniquement
│
├── database/
│   └── init/                                    ← scripts d'init montes dans postgres
│       ├── 01-create-databases.sql              ← 4 CREATE DATABASE
│       ├── 02-auth-soap.sql                     ← schema chatroom_auth_soap
│       ├── 03-chat-soap.sql                     ← schema chatroom_chat_soap
│       ├── 04-auth-rest.sql                     ← schema chatroom_auth_rest
│       ├── 05-chat-rest.sql                     ← schema chatroom_chat_rest
│       └── 06-seed.sql                          ← salons par defaut
│
├── soap/
│   ├── auth-service/                            ← Java JAX-WS — port 9001
│   │   ├── build.xml                            ← build Ant (compatible TP1)
│   │   ├── pom-deps.xml                         ← deps Maven pour Metro + JDBC + jBCrypt
│   │   ├── run.sh                               ← compile + lance (sans Ant requis)
│   │   └── src/esp/dgi/ws/soap/auth/
│   │       ├── api/
│   │       │   ├── AuthService.java             ← @WebService interface
│   │       │   ├── dto/{UserDTO,TokenInfoDTO}.java
│   │       │   └── faults/AuthFault.java        ← @WebFault
│   │       └── server/
│   │           ├── ServerApp.java               ← Endpoint.publish
│   │           ├── AuthServiceImpl.java
│   │           ├── db/{DbConnection,UserDao,SessionDao}.java
│   │           └── util/PasswordHasher.java     ← jBCrypt
│   │
│   └── chat-service/                            ← Node.js node-soap — port 9002
│       ├── package.json                         ← express, soap, pg
│       ├── wsdl/chat.wsdl                       ← contract-first
│       └── src/
│           ├── app.js                           ← express + soap.listen
│           ├── service.js                       ← createRoom, listRooms, sendMessage, getMessages
│           ├── authClient.js                    ← SoapClient sortant vers soap-auth-java
│           ├── db.js                            ← pg.Pool
│           └── repositories/{rooms.js, messages.js}
│
├── rest/
│   ├── auth-service-java/                       ← Java JAX-RS Jersey+Grizzly — port 8081
│   │   ├── build.xml
│   │   ├── pom-deps.xml                         ← Jersey 3.1, JAXB, Jackson, JDBC, jBCrypt
│   │   ├── run.sh
│   │   └── src/esp/dgi/ws/rest/auth/
│   │       ├── api/dto/{CredentialsDTO,TokenDTO,TokenInfoDTO,ErrorDTO}.java   ← @XmlRootElement
│   │       └── server/
│   │           ├── Main.java                    ← Grizzly bootstrap
│   │           ├── resources/AuthResource.java  ← @Produces JSON + XML
│   │           ├── services/AuthService.java
│   │           ├── db/{DbConnection,UserDao,SessionDao}.java
│   │           ├── util/PasswordHasher.java
│   │           └── exceptions/{AuthException,AuthExceptionMapper}.java
│   │
│   └── chat-service-python/                     ← Python Flask + xmltodict — port 8082
│       ├── app.py
│       ├── config.py
│       ├── requirements.txt
│       ├── run.sh
│       └── chatroom_chat/
│           ├── routes/{rooms.py, messages.py}
│           ├── services/chat_service.py
│           ├── repositories/{room_repository,message_repository}.py
│           ├── clients/auth_client.py           ← requests sortant vers rest-auth-java /validate
│           ├── middleware/auth_middleware.py    ← decorateur @require_auth
│           ├── http/response.py                 ← content negotiation JSON / XML
│           └── db/connection.py                 ← psycopg
│
└── frontend-php/                                ← PHP 8.2+ — port 8000
    ├── config/
    │   ├── bootstrap.php                        ← autoloader PSR-4
    │   └── config.php                           ← BACKEND_MODE + URLs
    ├── public/
    │   ├── index.php                            ← redirection
    │   ├── login.php, register.php, logout.php
    │   ├── chat.php                             ← page principale
    │   ├── api-proxy.php                        ← AJAX → ClientFactory
    │   └── assets/
    │       ├── css/style.css
    │       └── js/chat.js                       ← polling 1.5 s
    └── src/
        ├── Client/
        │   ├── ClientInterface.php              ← LE PORT
        │   ├── BackendException.php
        │   ├── SoapBackendClient.php            ← ADAPTER 1
        │   └── RestBackendClient.php            ← ADAPTER 2
        └── ClientFactory.php                    ← selection
```

---

## Schema de la base de donnees

Les **4 bases** ont des schemas symetriques. Phase SOAP et phase REST sont **isolees** (pas de partage de comptes, pas de partage de messages).

### Tables `users` et `sessions` (bases `auth_*`)

```sql
CREATE TABLE users (
  id            SERIAL PRIMARY KEY,
  username      VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,            -- bcrypt
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sessions (
  token       CHAR(64) PRIMARY KEY,               -- 32 bytes hex (SecureRandom)
  user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at  TIMESTAMP NOT NULL                  -- TTL 8 h
);
```

### Tables `rooms` et `messages` (bases `chat_*`)

```sql
CREATE TABLE rooms (
  id          SERIAL PRIMARY KEY,
  name        VARCHAR(80) NOT NULL UNIQUE,
  created_by  INTEGER NOT NULL,                   -- user_id opaque (pas de FK inter-base)
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
  id          BIGSERIAL PRIMARY KEY,              -- monotone, sert au polling sinceId
  room_id     INTEGER NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
  user_id     INTEGER NOT NULL,                   -- opaque, resolu via AuthService
  username    VARCHAR(50) NOT NULL,               -- denormalise (resolu au sendMessage)
  content     TEXT NOT NULL,
  created_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX messages_room_id_idx ON messages(room_id, id);
```

Le **username** est denormalise dans `messages` : au moment du `sendMessage`, ChatService appelle AuthService pour resoudre `(user_id, username)` et stocke les deux. Evite un appel inter-service a chaque `getMessages`.

---

## Points pedagogiques cles

### 1. Composition de services (au-dela du chat basique)

Le ChatService n'a **pas** de connaissance directe des utilisateurs. Il **delegue** la validation des tokens a l'AuthService **via le protocole de la phase** :
- Phase SOAP : Node → Java (SOAP sortant)
- Phase REST : Python → Java (REST sortant)

C'est ce qui donne tout son sens a l'exercice : on demontre la composition de services distribues sous deux paradigmes.

### 2. Contract-first vs Code-first

- **AuthService Java JAX-WS** : code-first (annotations `@WebService`, WSDL **auto-genere** par Metro)
- **ChatService Node node-soap** : **contract-first** (WSDL `wsdl/chat.wsdl` ecrit a la main, code implemente sur ce contrat)
- **AuthService Java JAX-RS** : annotations `@Path` + JAXB pour XML auto
- **ChatService Python Flask** : routes manuelles + serialisation manuelle (JSON/XML via `Accept`)

### 3. Content negotiation REST

Les services REST repondent en JSON **ou** en XML selon l'en-tete `Accept` du client :
- Java JAX-RS : `@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})` declaratif
- Python Flask : `request.accept_mimetypes.best_match(['application/json', 'application/xml'])` explicite

### 4. Pattern Port + Adapters (Hexagonal)

Le frontend PHP **n'implemente pas deux versions** des pages. Il utilise **une seule interface** (`ClientInterface`) et **deux adapters interchangeables**. C'est l'**inversion de dependance** appliquee a un client de services web — le code metier ne depend que de l'abstraction.

### 5. Polling cote navigateur

Le pattern polling de TP1 (boucle 1 s en Java Swing) est **porte cote navigateur** en JavaScript : `setInterval(poll, 1500)`. Le `sinceId` (BIGSERIAL monotone) evite les problemes de fuseaux horaires entre containers/services.

### 6. Polyglossie reelle

Trois langages, trois ecosystemes (Maven/Ant pour Java, npm pour Node, pip pour Python), un seul cas metier. Les services communiquent par des contrats standardises (WSDL pour SOAP, JSON Schema implicite pour REST) — pas de couplage de langage.

---

## Depannage

### `Cannot connect to the Docker daemon`

Demarrer Docker Desktop ou `sudo systemctl start docker`.

### `php -S : extension 'soap' not loaded`

Installer `php-soap` et redemarrer le shell (cf. [Pre-requis](#pre-requis)).

### Le ChatService demarre avant l'AuthService

C'est tolere : le client SOAP/REST sortant est cree **a la premiere utilisation** (lazy). Mais le premier `sendMessage` echouera si AuthService n'est pas encore up. Demarrer toujours AuthService avant ChatService.

### Bascule `BACKEND_MODE` et utilisateur introuvable

Normal : les bases SOAP et REST sont **separees**. Reinscrire les comptes apres bascule.

### Reset complet de la base

```bash
docker compose down -v          # supprime le volume pgdata
docker compose up -d postgres   # recree tout depuis database/init/
```

### Verifier les WSDLs et endpoints

```bash
curl -s http://localhost:9001/auth?wsdl  | head -5    # SOAP Java
curl -s http://localhost:9002/chat?wsdl  | head -5    # SOAP Node
curl -s http://localhost:8081/validate   -H 'Authorization: Bearer x'  # REST Java (renvoie 401)
curl -s http://localhost:8082/rooms      -H 'Authorization: Bearer x'  # REST Python (renvoie 401)
```

---

## Licence et auteur

Projet pedagogique — cours Web Services DIC3 2026, ESP DGI.

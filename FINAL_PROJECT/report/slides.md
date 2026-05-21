---
marp: true
theme: default
size: 16:9
paginate: true
backgroundColor: white
color: '#1a1a1a'
style: |
  section {
    font-family: -apple-system, "Segoe UI", "Helvetica Neue", Arial, sans-serif;
    padding: 60px 80px;
    font-size: 22px;
    line-height: 1.5;
  }
  h1 {
    color: #001489;
    font-size: 1.8em;
    margin-bottom: 0.3em;
    font-weight: 700;
  }
  h2 {
    color: #007749;
    font-size: 1.2em;
    margin-top: 0.4em;
    margin-bottom: 0.3em;
  }
  h3 {
    color: #555;
    font-size: 1em;
    margin-bottom: 0.2em;
  }
  section.title-page {
    justify-content: flex-start;
    text-align: left;
    padding-top: 120px;
  }
  section.title-page h1 {
    font-size: 2.4em;
    margin-bottom: 0.6em;
  }
  section.title-page .meta {
    color: #666;
    font-size: 0.95em;
    margin-top: 1.5em;
  }
  section.center {
    justify-content: center;
    text-align: center;
  }
  section.center h1 {
    font-size: 2.4em;
  }
  img {
    display: block;
    margin: 0 auto;
    max-height: 65vh;
    max-width: 90%;
  }
  table {
    margin: 0.5em auto;
    border-collapse: collapse;
  }
  table th, table td {
    border-bottom: 1px solid #ddd;
    padding: 6px 14px;
    text-align: left;
  }
  table th {
    color: #001489;
    border-bottom: 2px solid #001489;
  }
  code {
    background-color: #f4f4f4;
    padding: 1px 6px;
    border-radius: 3px;
    font-size: 0.92em;
  }
  blockquote {
    border-left: 3px solid #007749;
    padding-left: 1em;
    color: #555;
    font-style: italic;
  }
  .two-col {
    display: flex;
    gap: 40px;
    align-items: center;
    height: 100%;
  }
  .two-col > div {
    flex: 1;
  }
  .two-col img {
    max-height: 80vh;
    max-width: 100%;
    margin: 0;
  }
  .two-col ul {
    list-style: none;
    padding-left: 0;
  }
  .two-col ul li {
    padding: 6px 0;
    border-bottom: 1px solid #eee;
  }
  footer {
    color: #888;
    font-size: 0.65em;
  }
  section::after {
    color: #aaa;
    font-size: 0.75em;
  }
footer: 'Chatroom SOAP + REST — ESP DGI 2026'
---

<!-- _class: title-page -->
<!-- _paginate: false -->

# Chatroom distribuée SOAP + REST

## Architecture polyglotte avec frontend hexagonal

<div class="meta">

Groupe 3
École Supérieure Polytechnique — DGI
Cours Web Services — DIC3 — Mai 2026

</div>

---

# Plan

1. Sujet et fonctionnalités
2. Conception et architecture
3. Modèle de données et intégrité
4. Communication SOAP
5. Communication REST
6. Bilan

---

# Sujet

Le cas pratique du cours : une chatroom en SOAP et en REST.

---

# Fonctionnalités

<div class="two-col">
<div>

- S'inscrire
- Se connecter
- Créer ou rejoindre un salon
- Envoyer un message
- Voir les messages des autres en temps réel
- Se déconnecter

</div>
<div>

![](figures/plantuml/use-cases-simple.png)

</div>
</div>

---

# Architecture

<div class="two-col">
<div>

- Un même frontend pour deux protocoles
- Deux jeux de services backend interchangeables
- Validation du jeton à chaque opération
- Bases de données isolées par phase
- Bascule par variable d'environnement

</div>
<div>

![](figures/plantuml/architecture-globale-simple.png)

</div>
</div>

---

# Structuration des données

<div class="two-col">
<div>

## Comment garantir l'intégrité sans clé étrangère ?

Les deux bases d'un même backend sont **physiquement isolées**. PostgreSQL n'autorise pas de clé étrangère inter-base.

</div>
<div>

![](figures/plantuml/mcd.png)

</div>
</div>

---

# Polling côté vue

<div class="two-col">
<div>

## Comment livrer les nouveaux messages en temps réel ?

La vue interroge le serveur toutes les **1,5 secondes** et utilise un curseur `sinceId` monotone pour ne recevoir que les messages plus récents.

</div>
<div>

![](figures/plantuml/sequence-polling-simple.png)

</div>
</div>

---

# Stack technique

<div class="two-col">
<div>

- **PHP 8.2** côté frontend
- **SoapClient** et **cURL** dans la fabrique de client
- **Java 21** pour les deux AuthService
- **Node.js 20** pour le ChatService SOAP
- **Python 3.12** pour le ChatService REST
- **PostgreSQL 16** pour les quatre bases
- **bcrypt** pour les mots de passe
- **HTTP** entre le navigateur et le frontend

</div>
<div>

![](figures/plantuml/architecture-stack.png)

</div>
</div>

---

<!-- _class: title-page -->
<!-- _paginate: false -->

# Communication

Comment les services dialoguent

---

# SOAP : zoom sur l'architecture

<div class="two-col">
<div>

- Contrat **WSDL** décrivant les opérations
- Échanges en **enveloppes XML**
- Style **document/literal**
- Fautes structurées (`<soap:Fault>`)
- Génération auto des stubs côté PHP et Node

</div>
<div>

![](figures/plantuml/architecture-zoom-soap.png)

</div>
</div>

---

# Une enveloppe SOAP en pratique

<div class="two-col">
<div>

**Requête `login`**

```xml
<soap:Envelope>
  <soap:Body>
    <auth:login>
      <username>alice</username>
      <password>secret</password>
    </auth:login>
  </soap:Body>
</soap:Envelope>
```

</div>
<div>

**Réponse**

```xml
<soap:Envelope>
  <soap:Body>
    <auth:loginResponse>
      <return>abc123…</return>
    </auth:loginResponse>
  </soap:Body>
</soap:Envelope>
```

Codes d'erreur : `INVALID_CREDENTIALS`, `USER_ALREADY_EXISTS`, `INVALID_TOKEN`

</div>
</div>

---

# Nos deux WSDL

Chaque service SOAP du projet expose son WSDL ; voici les opérations qu'il rend disponibles.

| `AuthService` — `http://localhost:9001/auth` | `ChatService` — `http://localhost:9002/chat` |
|---|---|
| `register(username, password)` | `createRoom(token, name)` |
| `login(username, password)` | `listRooms(token)` |
| `logout(token)` | `sendMessage(token, roomId, content)` |
| `validateToken(token)` | `getMessages(token, roomId, sinceId)` |

---

# SOAP : envoi d'un message

![](figures/plantuml/sequence-sendmessage-soap.png)

---

# SOAP en action

<div class="two-col">
<div>

- **14 cas de test** chaînés par Property Transfers
- **23 assertions** vérifiées automatiquement
- Inspection du **XML brut** (onglet Raw)

</div>
<div>

![](figures/soapui/soapui-runner.png)

</div>
</div>

---

# REST : zoom sur l'architecture

<div class="two-col">
<div>

- Ressources `/rooms`, `/rooms/{id}/messages`
- Verbes HTTP : `GET`, `POST`
- Auth par en-tête `Authorization: Bearer <token>`
- Négociation **JSON ou XML** via `Accept`
- Pas d'état conversationnel

</div>
<div>

![](figures/plantuml/architecture-zoom-rest.png)

</div>
</div>

---

# Nos endpoints REST en pratique

Le projet expose **huit endpoints métier**, répartis entre les deux services.

| Verbe + chemin | Service | Code OK |
|---|---|---|
| `POST /register` | AuthService | 200 |
| `POST /login` | AuthService | 200 |
| `GET /validate` | AuthService | 200 |
| `POST /logout` | AuthService | 204 |
| `GET /rooms` | ChatService | 200 |
| `POST /rooms` | ChatService | 201 |
| `GET /rooms/{id}/messages` | ChatService | 200 |
| `POST /rooms/{id}/messages` | ChatService | 201 |

---

# Négociation de contenu en action

<div class="two-col">
<div>

**Même requête, format JSON**

```bash
curl -H 'Accept: application/json' \
     http://localhost:8082/rooms
```

```json
{"rooms": [
  {"id": 1, "name": "general"}
]}
```

</div>
<div>

**Même requête, format XML**

```bash
curl -H 'Accept: application/xml' \
     http://localhost:8082/rooms
```

```xml
<response>
  <rooms>
    <room>
      <id>1</id>
      <name>general</name>
    </room>
  </rooms>
</response>
```

</div>
</div>

---

# REST : envoi d'un message

![](figures/plantuml/sequence-sendmessage-rest.png)

---

# Postman en action

<div class="two-col">
<div>

- Collection organisée par service
- **28 requêtes** : JSON et XML pour chaque opération
- Property Transfers automatiques (token, roomId, sinceId)
- Rejouable via Runner ou **Newman CLI**

</div>
<div>

![](figures/postman/postman-runner-result.png)

</div>
</div>

---

# Bilan

- **Composition de services** observable : chaque opération métier traverse les deux services et illustre la délégation de l'authentification.
- **Polyglossie tenue** : Java, Node.js et Python interagissent sans friction grâce aux contrats standardisés.
- **Bascule SOAP ↔ REST** par simple variable d'environnement, sans modification du code applicatif, grâce au pattern **Port et Adaptateurs**.

> Merci de votre attention.

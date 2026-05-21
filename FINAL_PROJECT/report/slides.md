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

Mouhamed Lamine Faye Zeitune
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

# Sujet du projet

Une chatroom textuelle multi-utilisateurs implémentée **deux fois** sur le même cas métier :

- **Phase 1** en SOAP (Java JAX-WS Metro + Node.js node-soap)
- **Phase 2** en REST (Java Jersey + Python Flask)

Un même frontend PHP consomme les deux backends grâce au pattern **Port et Adaptateurs**, sélectionné par variable d'environnement.

> L'objectif est de comparer concrètement les deux paradigmes sur un cas identique.

---

# Fonctionnalités attendues

| AuthService | ChatService |
|---|---|
| `register` (création de compte) | `createRoom` (création de salon) |
| `login` (jeton de session) | `listRooms` (lister les salons) |
| `logout` (invalidation) | `sendMessage` (publier) |
| `validateToken` (contrôle interne) | `getMessages` (polling) |

Huit opérations métier au total, réparties en deux services autonomes.

---

# Cas d'utilisation

![](figures/plantuml/use-cases.png)

---

# Architecture globale

![](figures/plantuml/architecture-globale.png)

---

# Modèle conceptuel de données

![](figures/plantuml/mcd.png)

---

<!-- _class: center -->

# Comment garantir l'intégrité

# sans clé étrangère ?

Les deux bases d'un même backend sont **physiquement isolées**.
PostgreSQL n'autorise pas de FK inter-base.

---

# La solution : composition de services

![](figures/plantuml/liaison-bd-services.png)

---

# Polling côté navigateur

![](figures/plantuml/sequence-polling.png)

---

# Stack technique par bloc

| Bloc | Phase SOAP | Phase REST |
|---|---|---|
| Frontend | PHP 8.2 (SoapClient) | PHP 8.2 (cURL) |
| AuthService | Java 21 + JAX-WS Metro | Java 21 + Jersey + Grizzly |
| ChatService | Node.js 20 + node-soap | Python 3.12 + Flask |
| BD | PostgreSQL 16 (2 bases) | PostgreSQL 16 (2 bases) |
| Sécurité | bcrypt (cost 10) | bcrypt (cost 10) |

---

# SOAP : le contrat WSDL

Un WSDL décrit publiquement un service SOAP en **cinq sections imbriquées** :

| Section | Rôle |
|---|---|
| `<types>` | dictionnaire XSD des structures (DTOs) |
| `<message>` | regroupement logique des entrées et sorties |
| `<portType>` | interface abstraite : opérations + entrées + sorties + fautes |
| `<binding>` | protocole concret : SOAP sur HTTP, document/literal |
| `<service>` | adresse réelle de l'endpoint (URL) |

Un client SOAP **génère ses stubs automatiquement** à partir du WSDL.

---

# SOAP : envoi d'un message

![](figures/plantuml/sequence-sendmessage-soap.png)

---

# SoapUI en action

![h:480](figures/soapui/soapui-runner.png)

Projet SoapUI : **14 TestCases**, 6 Property Transfers, 23 assertions.
Test du flux complet avec validation inter-service.

---

# REST : principes

- **Ressources** identifiées par des URI (`/rooms`, `/rooms/{id}/messages`)
- **Verbes HTTP** : `POST` créer, `GET` lire, `DELETE` supprimer
- **Codes de statut** : 200, 201, 401, 404, 409
- **Authentification** : en-tête `Authorization: Bearer <token>`
- **Négociation de contenu** : un même endpoint sert JSON ou XML selon `Accept`

```http
GET /rooms HTTP/1.1
Authorization: Bearer abc...
Accept: application/xml
```

---

# REST : envoi d'un message

![](figures/plantuml/sequence-sendmessage-rest.png)

---

# Postman en action

![h:460](figures/postman/postman-runner-result.png)

Collection Postman : **28 requêtes** organisées par service.
Chaque opération existe en **JSON et en XML**, exécutables en boucle via le Collection Runner.

---

# Bilan

- **Composition de services** observable : chaque opération métier traverse les deux services et illustre la délégation de l'authentification.
- **Polyglossie tenue** : Java, Node.js et Python interagissent sans friction grâce aux contrats standardisés.
- **Bascule SOAP ↔ REST** par simple variable d'environnement, sans modification du code applicatif, grâce au pattern **Port et Adaptateurs**.

> Merci de votre attention.

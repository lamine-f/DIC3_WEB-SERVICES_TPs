# Postman — Chatroom REST

Collection et environnement Postman reutilisables pour tester l'interaction entre `rest-auth-java` (port 8081) et `rest-chat-python` (port 8082).

**Collection bilingue** : chaque operation metier est testee dans sa **version JSON** et dans sa **version XML** (negotiation de contenu exhaustive).

## Fichiers

- `chatroom-rest.postman_collection.json` — collection organisee **par service**, chaque operation declinee en JSON + XML :
  - `AuthService (Java JAX-RS — port 8081)` : 11 requetes (register x2, login x2, validate JSON+XML, token bidon, x2 paires)
  - `ChatService (Python Flask — port 8082)` : sous-dossiers `Rooms` (5 requetes) + `Messages` (8 requetes)
  - `AuthService — Logout (fin de scenario)` : 4 requetes (logout alice/bob en JSON et XML)
  - **Total : 28 requetes**
- `chatroom-rest.postman_environment.json` — environnement local (URLs + variables dynamiques)

## Strategie de test bilingue

Chaque operation metier (register, login, validate, createRoom, listRooms, sendMessage, getMessages, logout) est exposee dans **deux variantes consecutives** :

| Variante | Content-Type | Accept | Body | Extraction de valeur |
|---|---|---|---|---|
| **JSON** | `application/json` | `application/json` | `{ ... }` | `pm.response.json()` |
| **XML** | `application/xml` | `application/xml` | `<root>...</root>` | regex `<tag>([^<]+)</tag>` |

Cette redondance permet :
- de **valider les deux formats** sur tous les endpoints,
- de **comparer la verbosite** des deux representations dans l'onglet Body,
- de **demontrer la negotiation de contenu** des services Web modernes (un seul backend, deux formats de sortie).

Les requetes de **test negatif** (`Validate token invalide -> 401`, `Create room sans token -> 401`) restent en JSON uniquement : leur role est de valider le rejet, pas le format.

## Import dans Postman

1. Ouvrir Postman -> bouton **Import** (en haut a gauche)
2. Glisser-deposer les **deux** fichiers JSON
3. En haut a droite, selectionner l'environnement **"Chatroom REST — local"**

## Lancer les services avant les tests

```bash
# Terminal 1 — base de donnees
cd FINAL_PROJECT
docker compose up -d postgres

# Terminal 2 — AuthService Java (port 8081)
cd rest/auth-service-java && ./run.sh

# Terminal 3 — ChatService Python (port 8082)
cd rest/chat-service-python && ./run.sh
```

## Utilisation

### Mode 1 — Une requete a la fois

Cliquer sur une requete -> **Send**. Les scripts de test enrichissent automatiquement l'environnement.

Tu peux choisir d'envoyer **uniquement la version JSON ou XML** d'une operation pour comparer les payloads et les reponses.

### Mode 2 — Scenario complet (le plus utile)

Bouton **Runner** (en bas a gauche de Postman) :
1. Selectionner la collection **"Chatroom REST — FINAL_PROJECT"**
2. Selectionner l'environnement **"Chatroom REST — local"**
3. Bouton **Run**

L'ordre des 28 requetes alterne JSON et XML pour chaque operation. Le scenario complet :

1. Register alice JSON + XML (toleres 409 si deja crees)
2. Register bob JSON + XML
3. Login alice JSON puis XML (les deux ecrasent `tokenAlice` — c'est OK, meme utilisateur)
4. Login bob JSON puis XML (idem `tokenBob`)
5. Validate alice JSON + XML
6. Validate token bidon -> 401 (test negatif)
7. Create room JSON (avec timestamp unique) + Create room XML (avec timestamp different)
8. List rooms JSON + XML
9. Create room sans token -> 401 (test negatif)
10. Alice envoie message JSON + XML (deux messages distincts, chacun avec son timestamp)
11. Bob lit messages JSON + XML
12. Bob repond JSON + XML
13. Alice poll JSON + XML (avec curseur `lastSinceId`)
14. Logout alice + bob en JSON puis XML (le 2e logout sur token deja invalide accepte 401)

### Mode 3 — Stress test (boucle longue)

Dans le Runner :
- **Iterations** : `100` (ou plus)
- **Delay** : `500` ms
- **Run**

Avec 28 requetes par iteration, 100 iterations representent 2800 appels HTTP — utile pour detecter des fuites memoire, valider la stabilite du polling et stress-tester les deux formats simultanement.

### Mode 4 — En ligne de commande (CI / scripts)

```bash
npm install -g newman
cd FINAL_PROJECT/docs/postman
newman run chatroom-rest.postman_collection.json \
  -e chatroom-rest.postman_environment.json \
  --iteration-count 10 \
  --delay-request 200
```

Rapport HTML :
```bash
npm install -g newman-reporter-html
newman run chatroom-rest.postman_collection.json \
  -e chatroom-rest.postman_environment.json \
  -r cli,html \
  --reporter-html-export report.html
```

## Variables de l'environnement

| Variable | Origine | Description |
|---|---|---|
| `authUrl` | manuel | URL AuthService (defaut `http://localhost:8081`) |
| `chatUrl` | manuel | URL ChatService (defaut `http://localhost:8082`) |
| `userAlice` / `passwordAlice` | manuel | Credentials premier compte |
| `userBob` / `passwordBob` | manuel | Credentials second compte |
| `tokenAlice` / `tokenBob` | auto (login) | Token Bearer (JSON via `pm.response.json()`, XML via regex `<token>...</token>`) |
| `roomId` | auto (create room JSON ou XML, ou list rooms) | Identifiant de salon utilise par les messages |
| `roomName` / `roomNameXml` | auto (pre-request) | Noms uniques avec timestamp pour eviter collision JSON/XML |
| `messageId` | auto (send message) | Sert a verifier la reception cote receveur |
| `lastSinceId` | auto (get messages) | Polling cursor — mis a jour apres chaque poll |
| `aliceMsgJson` / `aliceMsgXml` | auto (pre-request) | Contenu distinct pour chaque variante |

## Format des bodies XML

### Pour `register` et `login` (Java JAX-RS)

Le DTO `CredentialsDTO` est annote `@XmlRootElement(name = "credentials")`, le body XML attendu est donc :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<credentials>
  <username>alice</username>
  <password>secret</password>
</credentials>
```

### Pour `createRoom` et `sendMessage` (Python Flask)

Le code Python utilise `xmltodict.parse()` et extrait la premiere cle du dictionnaire racine. N'importe quel nom de racine fonctionne :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<root>
  <name>general-xml-1234567890</name>
</root>
```

### Reponses XML

Le service Java Jersey emet du XML selon les annotations JAXB des DTOs (`<tokenDTO>`, `<tokenInfoDTO>`).
Le service Python emet du XML sous une racine `<response>` via `xmltodict.unparse({"response": payload})`.

L'extraction de valeurs dans les scripts de test utilise des regex simples :
- Token Java : `/<token>([^<]+)<\/token>/`
- Id Python (roomId, messageId) : `/<id>(\d+)<\/id>/`

## Astuces

- **Console** (View > Show Postman Console) : voir le contenu reel des requetes/reponses avec les variables resolues — particulierement utile pour debugger les versions XML.
- Pour basculer en distant, changer `authUrl` / `chatUrl` dans l'environnement sans toucher a la collection.
- Si un test echoue : panneau **Test Results** sous la reponse. Chaque `pm.test(...)` apparait coche/decoche.
- Pour ne lancer **que les versions JSON** ou **que les versions XML** depuis le Runner, decocher les requetes non desirees avant le **Run**.

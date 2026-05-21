# SoapUI — Chatroom SOAP

Guide pour tester les 2 services SOAP du projet avec SoapUI 5.9.1 :
- **AuthService** Java JAX-WS Metro — `http://localhost:9001/auth?wsdl`
- **ChatService** Node.js node-soap — `http://localhost:9002/chat?wsdl`

## TL;DR — projet pret a importer

```
SoapUI > File > Import Project > chatroom-soap-soapui-project.xml
```

Le projet contient :
- **2 interfaces** AuthService + ChatService (WSDL chargees a l'ouverture)
- **8 operations** avec requetes-stubs
- **3 TestSuites** (`01 - AuthService`, `02 - ChatService`, `03 - Logout`)
- **14 TestCases** chaines via **6 Property Transfers** (tokenAlice, tokenBob, roomId, messageId, lastSinceId)
- **23 assertions** (SOAP Response, Not SOAP Fault, SOAP Fault attendu, XPath Match)
- **12 Project Properties** pre-configurees

Demarrer les services puis : clic droit Project -> **Launch TestRunner** -> Run.

## Contenu de ce dossier

- **`chatroom-soap-soapui-project.xml`** — projet SoapUI complet pret a importer
- **`README.md`** — ce guide
- **`scenario.md`** — description detaillee du scenario (utile pour le rapport / la soutenance)
- **`sample-requests/*.xml`** — 8 enveloppes SOAP brutes (utilisables aussi avec `curl` ou recollables dans une requete)

## Alternative (creation manuelle)

Si le projet pre-fait ne s'importe pas correctement (incompatibilite de version SoapUI, etc.), tu peux le recreer en 5 minutes a la main en suivant les **etapes 1 a 6** plus bas. C'est aussi un excellent exercice pedagogique pour apprendre SoapUI.

## Pre-requis : lancer les 3 processus

```bash
# Terminal 1 : Postgres
cd FINAL_PROJECT
docker compose up -d postgres

# Terminal 2 : AuthService Java (port 9001)
cd soap/auth-service && ./run.sh

# Terminal 3 : ChatService Node.js (port 9002)
cd soap/chat-service && npm install && node src/app.js
```

Verifier que les WSDLs repondent :
```bash
curl -s http://localhost:9001/auth?wsdl | head -5
curl -s http://localhost:9002/chat?wsdl | head -5
```

## Etape 1 — Creer le projet SoapUI (1 min)

1. SoapUI -> `File > New SOAP Project`
2. **Project Name** : `Chatroom-SOAP`
3. **Initial WSDL** : `http://localhost:9001/auth?wsdl`
4. Cocher : "Create sample requests for all operations"
5. OK

L'interface `AuthServiceSoapBinding` apparait avec 4 operations (register, login, logout, validateToken) et leurs requetes-stubs deja remplies avec `?`.

Repeter pour ChatService :
1. Clic droit sur le projet -> `Add WSDL`
2. **URL** : `http://localhost:9002/chat?wsdl`
3. OK

L'interface `ChatServicePortBinding` apparait avec 4 operations (createRoom, listRooms, sendMessage, getMessages).

## Etape 2 — Project Properties (variables reutilisables, 1 min)

1. Clic sur le **projet** (racine du Navigator) -> onglet **Properties** en bas
2. Ajouter (bouton `+`) les proprietes du tableau ci-dessous

| Property | Value |
|---|---|
| `authUrl` | `http://localhost:9001/auth` |
| `chatUrl` | `http://localhost:9002/chat` |
| `userAlice` | `alice` |
| `passwordAlice` | `secret` |
| `userBob` | `bob` |
| `passwordBob` | `secret` |
| `tokenAlice` | *(vide)* |
| `tokenBob` | *(vide)* |
| `roomId` | *(vide)* |
| `messageId` | *(vide)* |
| `lastSinceId` | `0` |

Dans n'importe quelle requete on les reference avec : `${#Project#tokenAlice}`, etc.

## Etape 3 — Tester une requete manuelle (30 sec)

1. Double-cliquer sur l'operation `login` dans `AuthService`
2. Le panneau de gauche montre la requete-stub SoapUI
3. Remplacer `<username>?</username>` par `<username>${#Project#userAlice}</username>` et idem pour password
4. Cliquer la fleche verte **Submit Request**
5. Panneau de droite : reponse SOAP avec `<return>...token...</return>`

Si tu veux y aller plus vite : copier-coller le contenu de `sample-requests/auth-login.xml` dans le panneau de requete.

## Etape 4 — Generate TestSuite (1 clic)

1. Clic droit sur `AuthServiceSoapBinding` -> **Generate TestSuite**
2. Options par defaut, OK
3. SoapUI cree une TestSuite avec 1 TestCase par operation, chacun contenant 1 Test Step "Request"
4. Idem pour `ChatServicePortBinding`

Tu as maintenant 2 TestSuites de 4 TestCases chacune.

## Etape 5 — Property Transfer (chainer le token, 30 sec par chaine)

Le scenario complet a besoin d'extraire le token de la reponse `login` et de l'injecter dans `validateToken`, `createRoom`, `sendMessage`, etc.

**Configuration :**
1. Ouvrir le TestCase `login` (genere a l'etape 4)
2. Clic droit -> `Add Step` -> `Property Transfer` -> nom : `extract token`
3. Dans le panneau Property Transfer :
   - **Source** : Step `login - Request` (ou le nom de ton request), Property `Response`
   - **Path Language** : XPath
   - **Source XPath** : `//*[local-name()='return']`
   - **Target** : Project `Chatroom-SOAP`, Property `tokenAlice`
4. Faire pareil pour `login bob` -> `tokenBob`

Verification rapide : apres avoir lance le TestCase login, regarde l'onglet Properties du Project -> `tokenAlice` est rempli.

Voir `scenario.md` pour les autres Property Transfers (roomId, messageId, lastSinceId).

## Etape 6 — Assertions built-in (clic droit sur la requete)

Pour chaque requete, ajouter au moins 2 assertions via le bouton **Add Assertion** :

| Assertion | A quoi ca sert |
|---|---|
| **SOAP Response** | Verifie que la reponse est un XML SOAP valide |
| **Not SOAP Fault** | Echec si la reponse est un fault (sauf pour le test `ValidateToken bidon` ou on veut l'inverse : utiliser **SOAP Fault**) |
| **Schema Compliance** | Verifie que la reponse respecte le WSDL/XSD |
| **XPath Match** | Verifie qu'un noeud existe / a une valeur (ex: `//return/userId` non vide pour validate) |

## Etape 7 — Lancer la TestSuite complete

1. Double-cliquer sur la TestSuite `AuthService`
2. Bouton **Run** (fleche verte en haut a gauche)
3. Le tableau de droite affiche les TestCases verts (PASS) ou rouges (FAIL) avec le temps

Idem pour ChatService.

Pour tout enchainer : clic droit sur **Project** -> `Launch TestRunner`.

## Voir le trafic SOAP brut

Sur n'importe quelle requete executee :
- Onglet **Raw** (panneau requete ou reponse) : HTTP brut avec headers
- Onglet **XML** : XML formate
- Onglet **JSON** : si applicable (pas pour SOAP)
- Onglet **HTML** : rendu si la reponse contient du HTML

Pour capturer le trafic ChatService -> AuthService (composition de services), ajouter SoapUI comme **MockService** ou utiliser un proxy externe (ex: mitmproxy sur le port 9001).

## Mode CLI (testrunner.sh)

Pour rejouer un scenario en boucle / en CI :

```bash
~/SmartBear/SoapUI-5.9.1/bin/testrunner.sh \
  -s "AuthService" \
  -c "Login alice" \
  /chemin/vers/Chatroom-SOAP-soapui-project.xml
```

Pour repeter `N` fois avec un delai, l'envelopper dans un script bash ou utiliser un Groovy script dans le Project Setup (cf. doc SoapUI : `pm.executeAsync`).

## Voir aussi

- `scenario.md` — sequence complete des 14 TestCases avec Property Transfers et assertions
- `sample-requests/` — 8 enveloppes SOAP pretes a coller
- `../postman/` — equivalent REST (collection Postman + Newman)
- `../../README.md` — vue d'ensemble du FINAL_PROJECT

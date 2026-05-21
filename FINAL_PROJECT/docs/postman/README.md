# Postman — Chatroom REST

Collection et environnement Postman reutilisables pour tester l'interaction entre `rest-auth-java` (port 8081) et `rest-chat-python` (port 8082).

## Fichiers

- `chatroom-rest.postman_collection.json` — collection organisee **par service** :
  - `AuthService (Java JAX-RS — port 8081)` : register, login, validate
  - `ChatService (Python Flask — port 8082)` : sous-dossiers `Rooms` + `Messages`
  - `AuthService — Logout (fin de scenario)` : separe pour preserver l'ordre du Runner (logout = invalidation des tokens, doit s'executer apres les tests Chat)
- `chatroom-rest.postman_environment.json` — environnement local (URLs + variables dynamiques)

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

Cliquer sur une requete -> **Send**. Les scripts de test enrichissent automatiquement l'environnement :
- `Login alice` -> remplit `tokenAlice`
- `Create room` -> remplit `roomId`
- `Alice envoie un message` -> remplit `messageId`

L'onglet **Tests** de chaque requete contient les assertions (visibles dans le panneau "Test Results" apres envoi).

### Mode 2 — Scenario complet (le plus utile)

Bouton **Runner** (en bas a gauche de Postman) :
1. Selectionner la collection **"Chatroom REST — FINAL_PROJECT"**
2. Selectionner l'environnement **"Chatroom REST — local"**
3. Optionnel : cocher **Save responses** pour garder les reponses
4. Bouton **Run Chatroom REST**

L'ordre des requetes est deja correct (Auth -> Rooms -> Messages -> Logout). Le scenario complet :

1. Register alice + bob (tolere 409 si deja crees)
2. Login alice / bob -> tokens stockes
3. Validate token (JSON et XML pour montrer le content negotiation)
4. Validate token bidon -> doit retourner 401
5. Create room (nom unique avec timestamp pour eviter les collisions)
6. List rooms (JSON + XML)
7. Create room sans token -> doit retourner 401
8. Alice envoie un message
9. Bob poll les messages -> verifie que le message d'alice est bien recu
10. Bob repond
11. Alice poll avec `sinceId` -> ne doit voir que les nouveaux
12. Logout des deux

### Mode 3 — Boucle infinie pour tester la stabilite

Dans le Runner :
- Champ **Iterations** : mettre `100` (ou plus)
- Champ **Delay** : `500` ms entre les iterations
- **Run**

Postman rejouera tout le scenario `N` fois. Idéal pour :
- Detecter des fuites memoire
- Verifier la stabilite du polling
- Stress-tester l'AuthService (chaque iteration cree 2 sessions)

### Mode 4 — En ligne de commande (CI / scripts)

Installer **Newman** (le runner CLI de Postman) :

```bash
npm install -g newman
```

Lancer :

```bash
cd FINAL_PROJECT/docs/postman
newman run chatroom-rest.postman_collection.json \
  -e chatroom-rest.postman_environment.json \
  --iteration-count 10 \
  --delay-request 200
```

Sortie : rapport texte avec **assertions passees/echouees**.

Pour un rapport HTML :

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
| `tokenAlice` / `tokenBob` | auto (login) | Token Bearer stocke apres login |
| `roomId` | auto (create room ou list rooms) | Identifiant de salon utilise par les messages |
| `roomName` | auto (pre-request) | `general-<timestamp>` pour eviter les collisions |
| `messageId` | auto (send message) | Sert a verifier la reception cote receveur |
| `lastSinceId` | auto (get messages) | Polling cursor — mis a jour apres chaque poll |
| `aliceMsg` | auto (pre-request) | Contenu du message d'alice avec timestamp ISO |

## Astuces

- **Console** (View > Show Postman Console) : voir le contenu reel des requetes/reponses avec les variables resolues.
- Pour basculer en distant, changer `authUrl` / `chatUrl` dans l'environnement sans toucher a la collection.
- Si un test echoue : panneau **Test Results** sous la reponse. Chaque `pm.test(...)` apparait coche/decoche.

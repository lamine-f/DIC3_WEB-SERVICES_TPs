# Scenario SOAP — equivalent du Runner Postman

Sequence ordonnee de TestCases SoapUI a configurer une fois puis rejouable a l'infini via **TestSuite Runner**. Equivalent du scenario REST de `docs/postman/`.

## Variables Project Properties (a creer une seule fois)

| Propriete | Valeur initiale | Remplie par |
|---|---|---|
| `authUrl` | `http://localhost:9001/auth` | manuel |
| `chatUrl` | `http://localhost:9002/chat` | manuel |
| `userAlice` | `alice` | manuel |
| `passwordAlice` | `secret` | manuel |
| `userBob` | `bob` | manuel |
| `passwordBob` | `secret` | manuel |
| `tokenAlice` | *(vide)* | Property Transfer apres `Login alice` |
| `tokenBob` | *(vide)* | Property Transfer apres `Login bob` |
| `roomId` | *(vide)* | Property Transfer apres `CreateRoom` |
| `messageId` | *(vide)* | Property Transfer apres `SendMessage alice` |
| `lastSinceId` | `0` | Property Transfer apres `GetMessages bob` |

## Sequence

### TestSuite : AuthService (`http://localhost:9001/auth`)

| # | TestCase | Body cle | Property Transfer (apres) | Assertion |
|---|---|---|---|---|
| 1 | Register alice | `username=${#Project#userAlice}`, `password=${#Project#passwordAlice}` | aucun (token jete) | SOAP Response **ou** SOAP Fault `USER_ALREADY_EXISTS` (les 2 sont OK) |
| 2 | Register bob | `username=${#Project#userBob}`, `password=${#Project#passwordBob}` | aucun | idem |
| 3 | Login alice | `username/password` alice | `//return` -> `tokenAlice` | SOAP Response + XPath `//return` non vide |
| 4 | Login bob | idem bob | `//return` -> `tokenBob` | idem |
| 5 | ValidateToken alice | `token=${#Project#tokenAlice}` | aucun | XPath Match `//return/userId` existe |
| 6 | ValidateToken bidon | `token=not-a-token` | aucun | **SOAP Fault attendu** (code `INVALID_TOKEN`) |

### TestSuite : ChatService (`http://localhost:9002/chat`)

> NOTE : tous les TestCases ChatService dependent de `tokenAlice` et `tokenBob` deja remplis par AuthService. Configurer dans `TestSuite > Options : Run on dependent TestSuites = AuthService`.

| # | TestCase | Body cle | Property Transfer (apres) | Assertion |
|---|---|---|---|---|
| 7 | CreateRoom (alice) | `token=${#Project#tokenAlice}`, `name=general` | `//room/id` -> `roomId` | SOAP Response **ou** Fault `ROOM_ALREADY_EXISTS` |
| 8 | ListRooms (alice) | `token=${#Project#tokenAlice}` | (optionnel : si `roomId` vide -> `(//room/id)[1]`) | XPath `count(//room) >= 1` |
| 9 | SendMessage alice | `token=${#Project#tokenAlice}`, `roomId=${#Project#roomId}`, `content="ping ${=System.currentTimeMillis()}"` | `//messageId` -> `messageId` | XPath `//messageId` est un entier > 0 |
| 10 | GetMessages bob (sinceId=0) | `token=${#Project#tokenBob}`, `roomId=${#Project#roomId}`, `sinceId=0` | `//message[last()]/id` -> `lastSinceId` | XPath `//message[id='${#Project#messageId}']` existe (verifie la livraison) |
| 11 | SendMessage bob (reponse) | `token=${#Project#tokenBob}`, `roomId=${#Project#roomId}`, `content="pong"` | aucun | SOAP Response OK |
| 12 | GetMessages alice (sinceId polling) | `token=${#Project#tokenAlice}`, `roomId=${#Project#roomId}`, `sinceId=${#Project#lastSinceId}` | aucun | XPath `count(//message) >= 1` (au moins le pong de bob) |

### TestSuite : Logout (fin de scenario)

| # | TestCase | Body | Assertion |
|---|---|---|---|
| 13 | Logout alice | `token=${#Project#tokenAlice}` | `//return = true` |
| 14 | Logout bob | `token=${#Project#tokenBob}` | `//return = true` |

## Lancer le scenario complet

1. Clic droit sur le **Project** -> `Launch TestRunner`
2. Onglet `TestSuites` : cocher dans l'ordre AuthService, ChatService, Logout
3. **Launch**
4. La fenetre montre les TestCases en cours, les assertions vertes/rouges, et le temps total.

## Mode CLI (equivalent Newman)

```bash
cd ~/SmartBear/SoapUI-5.9.1/bin
./testrunner.sh -e http://localhost:9001/auth \
                -s AuthService \
                /chemin/vers/ton-projet.xml
```

Genere un rapport texte avec le detail des asserts (idem Newman). Utile pour la demo / la CI.

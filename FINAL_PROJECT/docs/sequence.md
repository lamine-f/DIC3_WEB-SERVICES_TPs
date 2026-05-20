# Diagrammes de sequence — Chatroom SOAP + REST

Documente le **flux complet** d'envoi et de reception d'un message, depuis la saisie d'Alice jusqu'a l'affichage chez Bob, dans les deux phases du projet.

Les diagrammes sont au format **Mermaid** — affiches automatiquement sur GitHub, dans IntelliJ avec le plugin Mermaid, ou collables sur [mermaid.live](https://mermaid.live).

---

## Phase 1 — SOAP

```mermaid
sequenceDiagram
  autonumber
  participant A as Alice<br/>(navigateur)
  participant B as Bob<br/>(navigateur)
  participant P as frontend-php<br/>(api-proxy.php + ClientFactory)
  participant CS as ChatService<br/>Node :9002
  participant AS as AuthService<br/>Java :9001
  participant DB as PostgreSQL :5435

  rect rgb(235, 245, 255)
    Note over A,P: 1) ALICE ENVOIE UN MESSAGE
    A->>+P: POST api-proxy.php?action=sendMessage<br/>(roomId=3, content="Hello")<br/>Cookie: PHPSESSID
    Note right of P: $_SESSION['token'] = token_alice<br/>BACKEND_MODE=soap → SoapBackendClient
    P->>+CS: SOAP sendMessage(token_alice, 3, "Hello")
    CS->>+AS: SOAP validateToken(token_alice)
    AS->>DB: SELECT user_id FROM sessions WHERE token=?
    AS->>DB: SELECT id, username FROM users WHERE id=?
    DB-->>AS: {id:1, username:"alice"}
    AS-->>-CS: TokenInfoDTO{userId:1, username:"alice"}
    CS->>DB: INSERT INTO messages<br/>(room_id, user_id, username, content)
    DB-->>CS: messageId=5
    CS-->>-P: {messageId: 5}
    P-->>-A: {id: 5}
    A->>A: poll() immediat<br/>(rafraichit son propre DOM)
  end

  rect rgb(245, 255, 235)
    Note over B,P: 2) BOB RECOIT (polling toutes les 1.5s)
    loop setInterval(poll, 1500)
      B->>+P: GET api-proxy.php?action=getMessages<br/>(roomId=3, sinceId=4)<br/>Cookie: PHPSESSID
      Note right of P: $_SESSION['token'] = token_bob
      P->>+CS: SOAP getMessages(token_bob, 3, 4)
      CS->>+AS: SOAP validateToken(token_bob)
      AS->>DB: SELECT / SELECT
      AS-->>-CS: TokenInfoDTO{userId:2, username:"bob"}
      CS->>DB: SELECT * FROM messages<br/>WHERE room_id=3 AND id>4 ORDER BY id
      DB-->>CS: [{id:5, username:"alice", content:"Hello", ...}]
      CS-->>-P: {message: [...]}
      P-->>-B: {messages: [...]}
      Note right of B: appendMessage(m) dans le DOM<br/>sinceId = max(sinceId, 5) = 5
    end
  end
```

---

## Phase 2 — REST

```mermaid
sequenceDiagram
  autonumber
  participant A as Alice<br/>(navigateur)
  participant B as Bob<br/>(navigateur)
  participant P as frontend-php<br/>(api-proxy.php + ClientFactory)
  participant CS as ChatService<br/>Python Flask :8082
  participant AS as AuthService<br/>Java JAX-RS :8081
  participant DB as PostgreSQL :5435

  rect rgb(255, 245, 235)
    Note over A,P: 1) ALICE ENVOIE UN MESSAGE
    A->>+P: POST api-proxy.php?action=sendMessage<br/>(roomId=3, content="Hello")
    Note right of P: BACKEND_MODE=rest → RestBackendClient
    P->>+CS: POST /rooms/3/messages<br/>Authorization: Bearer token_alice<br/>{"content":"Hello"}
    CS->>+AS: GET /validate<br/>Authorization: Bearer token_alice<br/>Accept: application/json
    AS->>DB: SELECT user_id FROM sessions WHERE token=?
    AS->>DB: SELECT id, username FROM users WHERE id=?
    DB-->>AS: {id:1, username:"alice"}
    AS-->>-CS: 200 {"userId":1,"username":"alice"}
    Note right of CS: @require_auth injecte g.user
    CS->>DB: INSERT INTO messages(...)
    DB-->>CS: messageId=5
    CS-->>-P: 201 {"id": 5}
    P-->>-A: {id: 5}
    A->>A: poll() immediat
  end

  rect rgb(255, 235, 245)
    Note over B,P: 2) BOB RECOIT (polling 1.5s)
    loop setInterval(poll, 1500)
      B->>+P: GET api-proxy.php?action=getMessages<br/>(roomId=3, sinceId=4)
      P->>+CS: GET /rooms/3/messages?sinceId=4<br/>Authorization: Bearer token_bob
      CS->>+AS: GET /validate (token_bob)
      AS->>DB: SELECT / SELECT
      AS-->>-CS: 200 {"userId":2,"username":"bob"}
      CS->>DB: SELECT * FROM messages WHERE room_id=3 AND id>4
      DB-->>CS: [{id:5, ...}]
      Note right of CS: Accept negotiation:<br/>render_json_or_xml(messages)
      CS-->>-P: 200 {"messages":[...]}<br/>(JSON ou XML selon Accept)
      P-->>-B: {messages: [...]}
      Note right of B: appendMessage / sinceId = 5
    end
  end
```

---

## Acteurs et stack

| # | Acteur | Role | Stack |
|---|---|---|---|
| 1 | **Alice / Bob** | Navigateur (HTML + JS polling 1.5 s) | `chat.js` |
| 2 | **frontend-php** | Pattern Port + 2 Adapters | `api-proxy.php` + `ClientFactory` + `SoapBackendClient` / `RestBackendClient` |
| 3 | **ChatService** | Microservice metier (rooms + messages) | Phase SOAP: Node.js + node-soap :9002 — Phase REST: Python Flask + xmltodict :8082 |
| 4 | **AuthService** | Microservice identite (users + sessions) | Phase SOAP: Java JAX-WS Metro :9001 — Phase REST: Java JAX-RS Jersey+Grizzly :8081 |
| 5 | **PostgreSQL** | Persistance | Docker :5435 — 4 bases isolees par phase |

## Points cles pedagogiques

1. **Le navigateur ne parle jamais SOAP ni REST en direct** : tout passe par `api-proxy.php`, qui est l'unique client des services. Le browser ne voit que JSON local.

2. **Pattern Port + Adapters** : `api-proxy.php` n'utilise que `ClientInterface`. La bascule SOAP ↔ REST se fait par variable d'environnement `BACKEND_MODE`, sans toucher au code applicatif.

3. **Composition de services obligatoire** : ChatService ne stocke pas de mot de passe. A **chaque** operation metier, il rappelle AuthService pour resoudre `(userId, username)` a partir du token. C'est ce qui donne du sens au TP — on demontre la composition de services distribues sous deux protocoles.

4. **Reception = polling, pas push** : Bob ne recoit pas le message en push. Sa boucle `setInterval(1500ms)` re-interroge le serveur. C'est le pattern de TP1 (polling toutes les 1 s en Java Swing) porte dans le navigateur. Latence max = 1.5 s.

5. **`sinceId` (BIGSERIAL monotone)** garantit qu'on ne recoit jamais deux fois le meme message et qu'on ne depend pas des horloges entre conteneurs/services.

6. **Content negotiation REST** : la reponse de ChatService Python varie selon `Accept: application/json` ou `application/xml`. Le frontend PHP demande toujours JSON (plus simple), mais on peut tester le XML directement en curl pour la demo.

## Differences entre les deux phases (resume)

| Aspect | Phase SOAP | Phase REST |
|---|---|---|
| Protocole | Enveloppes SOAP + WSDL | HTTP + JSON/XML |
| Transport token | Premier parametre de la methode | En-tete `Authorization: Bearer` |
| Format | XML structure | JSON par defaut, XML sur demande |
| ChatService | Node.js node-soap :9002 | Python Flask :8082 |
| AuthService | Java JAX-WS Metro :9001 | Java JAX-RS Jersey :8081 |
| Adapter PHP | `SoapClient` natif | `cURL` |
| Validation inter-service | `authClient.validateTokenAsync(token)` | `requests.get('/validate', headers={Authorization})` |

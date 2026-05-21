# Captures d'écran a fournir

Ce fichier liste les captures d'écran que tu dois prendre pour completer le chapitre 4 du rapport. Tant qu'elles ne sont pas placées dans les bons dossiers, le rapport affiche des cadres noirs vides à leur emplacement.

Pour chaque capture : prends-la, place-la dans le dossier indique, sous le nom exact précise. Puis remplacé dans le `.tex` du chapitre concerne la ligne `\fbox{...}` par `\includegraphics[width=...]{...}`.

## Postman (dossier `figures/postman/`)

| Fichier suggere | Quoi capturer | Référence dans le rapport |
|---|---|---|
| `postman-runner-result.png` | Resultat du **Collection Runner** après exécution complète : panneau de droite avec toutes les requêtes en vert, statistiques en haut (pass / fail / total). | `\figref{postman-runner}` dans `chapters/04-tests-validation.tex` |
| `postman-json.png` | Requête `GET /rooms` envoyee avec en-tête `Accept: application/json`, panneau de réponse formate JSON visible (onglet **Pretty**). | `\figref{postman-json}` |
| `postman-xml.png` | Mêmes endpoint et payload, mais en-tête `Accept: application/xml`. Réponse visible avec balises XML formatees. | `\figref{postman-xml}` |

### Procédure pour Postman

1. Lancer les services REST (`postgres`, `rest-auth-java`, `rest-chat-python`).
2. Dans Postman, ouvrir le **Runner** (bouton en bas a gauche).
3. Glisser la collection « Chatroom REST — FINAL_PROJECT » dans la zone de gauche, sélectionner l'environnement « Chatroom REST — local », bouton **Run**.
4. Une fois tous les tests passes, **alt+impr.écran** sur la fenetre du Runner -> sauvegarder en `postman-runner-result.png`.
5. Pour `postman-json.png` : ouvrir « ChatService > Rooms > List rooms (alice) - JSON », clic **Send**, capturer la fenetre entière (requête + réponse formatee).
6. Pour `postman-xml.png` : ouvrir « ChatService > Rooms > List rooms (alice) - XML », clic **Send**, capturer.

## SoapUI (dossier `figures/soapui/`)

| Fichier suggere | Quoi capturer | Référence dans le rapport |
|---|---|---|
| `soapui-navigator.png` | Vue **Navigator** après import du projet : arborescence montrant les 2 interfaces (`AuthService`, `ChatService`) avec leurs 4 opérations chacune, et les 3 TestSuites en dessous. | `\figref{soapui-navigator}` |
| `soapui-runner.png` | Fenetre **TestRunner** après exécution complète : barres vertes pour AuthService, ChatService, Logout, et compteur d'assertions reussies. | `\figref{soapui-runner}` |
| `soapui-raw-request.png` | Onglet **Raw** d'une requête `login` envoyee a `http://localhost:9001/auth` : header HTTP + enveloppe SOAP XML complète avec username/password de Alice. | `\figref{soapui-raw-request}` |
| `soapui-raw-response.png` | Onglet **Raw** de la réponse correspondante : header HTTP 200 + enveloppe SOAP de réponse avec le token retourne. | `\figref{soapui-raw-response}` |

### Procédure pour SoapUI

1. Lancer les services SOAP (`postgres`, `soap-auth-java`, `soap-chat-node`).
2. Dans SoapUI, **File > Import Project** -> sélectionner `docs/soapui/chatroom-soap-soapui-project.xml`.
3. Capturer la fenetre principale avec l'arborescence deployee -> `soapui-navigator.png`.
4. Clic droit sur le projet -> **Launch TestRunner** -> **Run**. Attendre que tout passé au vert. Capturer -> `soapui-runner.png`.
5. Double-clic sur n'importe quelle requête `login` déjà exécutée -> onglet **Raw** dans le panneau de requête. Capturer -> `soapui-raw-request.png`.
6. Idem dans le panneau de réponse -> `soapui-raw-response.png`.

## Frontend PHP (dossier `figures/postman/` ou `figures/soapui/`)

| Fichier suggere | Quoi capturer | Référence |
|---|---|---|
| `frontend-chat.png` | Capture de la page `/chat.php` dans le navigateur : sidebar avec les salons, fil de messages avec au moins 2 utilisateurs (alice et bob), zone de saisie. Bonus : ouvrir la console DevTools pour montrer les polls `getMessages?sinceId=...`. | `\figref{frontend-chat}` dans le chapitre 4 |

## Workflow recommandé

1. **Capturer toutes les images** en une session (services lances).
2. **Placer les fichiers** dans les bons sous-dossiers.
3. **Editer le chapitre 4** : remplacer chaque `\fbox{\rule{0pt}{Xcm}\rule{Ycm}{0pt}}` par `\includegraphics[width=\linewidth]{figures/postman/postman-runner-result.png}` (en adaptant le chemin et la largeur).
4. **Recompiler** : `cd report && make`.

## Format conseille

- **Format** : PNG (préférable a JPEG pour les captures avec du texte)
- **Résolution** : capture native à la résolution de l'écran (pas de redimensionnement)
- **Largeur LaTeX** : `width=\linewidth` pour les captures larges, `width=0.85\linewidth` pour des captures plus etroites avec marges

## Remarque

Les cadres vides (`\fbox{...}`) dans le rapport conservent l'emplacement et la legende exacts des futures captures. Le PDF compile sans erreur même avant que tu n'ajoutes les images, ce qui permet de relire le texte autour des figures sans attendre.

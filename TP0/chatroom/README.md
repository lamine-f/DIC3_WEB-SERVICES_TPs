# ChatRoom - Application de Chat Distribuée avec Java RMI

Application de chat en temps réel utilisant Java RMI (Remote Method Invocation) pour la communication client-serveur.

## Structure du Projet

```
chatroom/
├── build.xml          # Fichier de build Ant
├── src/
│   ├── api/           # Interfaces RMI partagées
│   ├── server/        # Implémentation du serveur RMI
│   └── client/        # Application cliente
├── build/             # Classes compilées (généré)
└── dist/              # JARs (généré)
```

### Modules

- **api** : Contient les interfaces RMI (`ChatRoom`, `MessageListener`) utilisées pour la communication distante
- **server** : Serveur de chat qui gère les connexions clients et la diffusion des messages
- **client** : Interface utilisateur Swing permettant de se connecter au chat et d'envoyer/recevoir des messages

## Prérequis

- Java 21 ou supérieur
- Apache Ant

## Build

Compiler l'ensemble du projet :

```bash
ant compile
```

Créer les JARs :

```bash
ant jar
```

Nettoyer les fichiers générés :

```bash
ant clean
```

## Exécution

### 1. Démarrer le Serveur

```bash
ant run-server
```

Le serveur démarre et enregistre le service RMI sur le registre local (port 1099).

### 2. Démarrer un Client

Dans un nouveau terminal :

```bash
ant run-client
```

Plusieurs clients peuvent se connecter simultanément au serveur.

### Exécution via JARs

Après `ant jar`, vous pouvez aussi exécuter directement :

```bash
cd dist
java -jar chatroom-server.jar   # Terminal 1
java -jar chatroom-client.jar   # Terminal 2
```

## Architecture Technique

```
┌─────────────┐         ┌─────────────┐
│   Client 1  │◄───────►│             │
└─────────────┘   RMI   │             │
                        │   Serveur   │
┌─────────────┐   RMI   │    RMI      │
│   Client 2  │◄───────►│             │
└─────────────┘         │             │
                        └─────────────┘
```

- **Communication bidirectionnelle** : Le serveur peut notifier les clients (callback RMI)
- **Registre RMI** : Utilisé pour la découverte du service de chat
- **Sérialisation** : Les messages sont sérialisés automatiquement par RMI

## Auteur
Mouhamed Lamine FAYE | ESP DGI - DIC3 2026

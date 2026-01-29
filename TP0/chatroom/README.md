# ChatRoom - Application de Chat Distribuée avec Java RMI

Application de chat en temps réel utilisant Java RMI (Remote Method Invocation) pour la communication client-serveur.

## Structure du Projet

```
chatroom/
├── chatroom-api/      # Interfaces RMI partagées entre client et serveur
├── chatroom-server/   # Implémentation du serveur RMI
├── chatroom-client/   # Application cliente
└── pom.xml            # POM parent Maven
```

### Modules

- **chatroom-api** : Contient les interfaces RMI (`ChatService`, `ChatClient`) utilisées pour la communication distante
- **chatroom-server** : Serveur de chat qui gère les connexions clients et la diffusion des messages
- **chatroom-client** : Interface utilisateur permettant de se connecter au chat et d'envoyer/recevoir des messages

## Prérequis

- Java 21 ou supérieur
- Maven 3.6+

## Build

Compiler l'ensemble du projet :

```bash
mvn clean install
```

## Exécution

### 1. Démarrer le Serveur

```bash
cd chatroom-server
mvn exec:java
```

Le serveur démarre et enregistre le service RMI sur le registre local.

### 2. Démarrer un Client

Dans un nouveau terminal :

```bash
cd chatroom-client
mvn exec:java
```

Plusieurs clients peuvent se connecter simultanément au serveur.

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

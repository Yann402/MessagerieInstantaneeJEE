Messagerie Instantanée JEE avec WebSocket

Application web de messagerie instantanée en temps réel développée en Java EE / Jakarta EE avec WebSocket, MySQL et architecture MVC/DAO.

Fonctionnalités

- Authentification sécurisée avec mots de passe chiffrés (BCrypt)
- Messagerie en temps réel via WebSocket
- Gestion des utilisateurs avec statuts (online, offline, away, banned)
- Système de permissions (Administrateur, Modérateur, Utilisateur)
- Bannissement d'utilisateurs avec motif personnalisable
- Mise à jour automatique des listes d'utilisateurs (temps réel)
- Système de logs** pour tracer les activités
- Mode sombre/clair
- Notifications toast élégantes

## 🏗️ Architecture

- **Pattern MVC** : Séparation Model-View-Controller
- **Pattern DAO** : Couche d'accès aux données
- **WebSocket (Jakarta)** : Communication bidirectionnelle temps réel
- **Pool de connexions DBCP2** : Gestion optimisée des connexions DB
- **AJAX** : Actions administrateur sans rechargement
- **BCrypt** : Chiffrement sécurisé des mots de passe

## 🛠️ Technologies

- **Backend** : Java EE / Jakarta EE, Servlets, JSP, WebSocket
- **Frontend** : HTML5, CSS3, JavaScript (Vanilla), AJAX
- **Base de données** : MySQL 8.0+
- **Serveur** : Apache Tomcat 10.x
- **Build** : Maven
- **Sécurité** : BCrypt, Filtres d'authentification, Validation serveur

## 📋 Prérequis

- JDK 17 ou supérieur
- Apache Tomcat 10.1+
- MySQL 8.0+
- Maven 3.6+ (optionnel)

## 🚀 Installation et Démarrage

### 1. Cloner le dépôt
```bash
git clone https://github.com/votre-username/MessagerieInstantaneeJEE.git
cd messagerie-instantanee-jee
```

### 2. Configurer la base de données

Créez la base de données MySQL :
```bash
mysql -u root -p < database/scriptSQL.sql
```

### ⚠️ **IMPORTANT - Génération des Mots de Passe**

**N.B.** : Les hash BCrypt présents dans le script SQL sont des **exemples non fonctionnels**. Vous **DEVEZ** générer vos propres hash avant de lancer l'application.

**Étapes obligatoires :**

1. **Exécutez la classe Java** `GeneratePasswordHash.java` :
```bash
   cd src/main/java/com/messagerie/test
   javac -cp ".:../../../../lib/jbcrypt-0.4.jar" GeneratePasswordHash.java
   java -cp ".:../../../../lib/jbcrypt-0.4.jar" GeneratePasswordHash
```
   
   Ou depuis votre IDE :
   - Ouvrez `src/main/java/com/messagerie/test/GeneratePasswordHash.java`
   - Exécutez la méthode `main()`

2. **Copiez les hash générés** dans la console :
```
   Admin hash: $2a$10$NouveauHashGenerePourAdmin...
   Moderateur hash: $2a$10$NouveauHashGenerePourModo...
   Utilisateur1 hash: $2a$10$NouveauHashGenerePourUser1...
   Utilisateur2 hash: $2a$10$NouveauHashGenerePourUser2...
```

3. **Remplacez les hash** dans le script SQL (`database/scriptSQL.sql`) :
```sql
   -- Remplacez les hash existants par ceux générés
   UPDATE user SET password = '$2a$10$VotreNouveauHash...' WHERE username = 'admin';
   UPDATE user SET password = '$2a$10$VotreNouveauHash...' WHERE username = 'moderateur';
   UPDATE user SET password = '$2a$10$VotreNouveauHash...' WHERE username = 'utilisateur1';
   UPDATE user SET password = '$2a$10$VotreNouveauHash...' WHERE username = 'utilisateur2';
```

4. **Exécutez les mises à jour** :
```bash
   mysql -u root -p db_messagerie_instantanee < database/scriptSQL.sql
```

**Pourquoi cette étape est nécessaire ?**
- Les hash BCrypt contiennent un **sel aléatoire** qui change à chaque génération
- Les hash fournis dans le script sont des placeholders invalides
- BCrypt génère des hash uniques même pour le même mot de passe
- Cette sécurité empêche les attaques par rainbow tables

### 3. Configurer la connexion DB

Modifiez `src/main/resources/db.properties` :
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/db_messagerie_instantanee?useSSL=false&serverTimezone=UTC
db.username=root
db.password=votre_mot_de_passe
```

### 4. Déployer sur Tomcat

- Copiez le WAR généré dans `webapps/` de Tomcat
- Ou déployez directement depuis votre IDE

### 5. Accéder à l'application

Ouvrez votre navigateur : `http://localhost:8080/messagerie-instantanee/`

## 👤 Comptes de Test

**Tous les mots de passe par défaut** : `admin123`

| Utilisateur | Permission | Fonctionnalités |
|-------------|------------|-----------------|
| `admin` | Administrateur | Toutes fonctions + changement de type |
| `moderateur` | Modérateur | Bannir/débannir utilisateurs normaux |
| `utilisateur1` | Utilisateur | Messagerie de base |
| `utilisateur2` | Utilisateur | Messagerie de base |

## 📂 Structure du Projet
```
src/
├── main/
│   ├── java/com/messagerie/
│   │   ├── dao/              # Interfaces et implémentations DAO
│   │   ├── filter/           # Filtres (authentification, session)
│   │   ├── model/            # Entités (User, Message, Log)
│   │   ├── service/          # Logique métier
│   │   ├── servlet/          # Contrôleurs (Login, Chat, Admin)
│   │   ├── util/             # Utilitaires (BCrypt, Pool, Session)
│   │   ├── websocket/        # Endpoints WebSocket
│   │   └── test/             # Classes de test et génération hash
│   ├── resources/
│   │   └── db.properties     # Configuration DB
│   └── webapp/
│       ├── css/              # Styles (mode sombre/clair)
│       ├── js/               # Scripts (WebSocket, AJAX, Toast)
│       ├── WEB-INF/
│       │   └── web.xml       # Configuration servlets
│       ├── chat.jsp          # Interface principale
│       ├── login.jsp         # Page de connexion
│       └── error.jsp         # Gestion erreurs
└── database/
    └── script_creation_db.sql # Script SQL complet
```

## 🔒 Sécurité

- ✅ **Mots de passe chiffrés** avec BCrypt (10 rounds)
- ✅ **Validation côté serveur** de toutes les actions
- ✅ **Filtres d'authentification** sur toutes les routes
- ✅ **Gestion des sessions** avec timeout
- ✅ **Protection XSS** (échappement HTML)
- ✅ **Permissions hiérarchiques** (Admin > Modo > User)
- ✅ **Audit des actions** via table `log`

## 🧪 Tests

Exécutez la classe de test des DAO :
```bash
java com.messagerie.test.TestDAOs
```

Fonctionnalités testées :
- ✅ Authentification et bannissement
- ✅ CRUD utilisateurs/messages/logs
- ✅ Mise à jour statuts en temps réel
- ✅ Système de permissions
- ✅ WebSocket broadcast

## 🐛 Problèmes Connus

- Les sessions WebSocket peuvent se déconnecter après 30 minutes d'inactivité

## 📝 Licence

Ce projet est un projet académique développé dans le cadre du cours JEE.

## 👨‍💻 Auteur

**Patrit Tennah Yann Félix** - Filière 2e Année Ingénierie des Systèmes Informatiques - 2025/2026

## 📧 Contact

Pour toute question : yannpatrit@gmail.com

Notes Importantes :

N.B 1 : Le dossier target/ est généré par Maven donc négligez celui que j'ai fournis.
N.B 2 : Pour l'exécution vous aurez différents problèmes, pensez à : changer la 'root' de votre fichier dans les propriétés de votre projet (clic droit + properties) dans 'Web Project Setting' par le nom de votre projet créé (1); toujours dans les propriétés dans 'target runtimes', cochez votre tomcat 10.+ ; ajouter votre projet à votre serveur.
N.B 3 : Ce projet ne marche qu'avec les serveurs tomcat 10.+, les versions 9 et autres en dessous ne marcheront pas.

# MeteoAppJava - Application Météo

Une application météo complète développée pour Android en Java, fournissant des informations météorologiques en temps réel, des prévisions et des données météo basées sur la localisation.

## Fonctionnalités Implémentées

- [x] Utilisation de l'API Preference (PreferencesManager pour les paramètres utilisateur)
- [x] Écriture/lecture dans un Fichier (FileUtils pour le cache de données)
- [x] Utilisation de SQLite (via la base de données Room)
- [x] Utilisation de Room (MeteoDatabase avec DAOs)
- [ ] Utilisation de Firebase
- [x] Nombre d'activités ou fragments supérieur ou égal à 3 (MainActivity, SearchActivity, DetailedForecastActivity)
- [x] Gestion du bouton Back (message pour confirmer que l'on veut réellement quitter l'application)
- [x] L'affichage d'une liste avec son adapter (affichage des prévisions météo)
- [x] L'affichage d'une liste avec un custom adapter (avec gestion d'événement sur les éléments météo)
- [x] La pertinence d'utilisation des layouts (L'application est responsive et supporte: portrait/paysage et tablette)
- [x] L'utilisation d'événements améliorant l'UX (rafraîchissement météo par glissement)
- [ ] La réalisation de composant graphique custom
- [x] Les tâches en background (threading pour les appels API)
- [x] Le codage d'un menu (utilisation d'un menu en resource XML)
- [x] L'application de patterns :
  - [x] Pattern Repository
  - [x] Singleton (Base de données)
  - [x] Architecture MVC
- [x] Demande des autorisations (permissions de localisation)
- [x] L'appel de WebServices (intégration de l'API OpenWeatherMap)
- [x] Utilisation des API Android :
  - [x] Géolocalisation
  - [x] Connectivité réseau
  - [x] Stockage de fichiers

## Détails Techniques

### Intégration API

L'application utilise l'API OpenWeatherMap pour récupérer les données météorologiques actuelles, les prévisions quotidiennes et horaires. Les données sont mises en cache localement pour un accès hors ligne.

### Base de Données

La persistance des données locales est réalisée à l'aide de la base de données Room avec des entités pour :

- Météo actuelle
- Prévisions quotidiennes
- Prévisions horaires
- Emplacements enregistrés

### Services de Localisation

L'application utilise les services de localisation d'Android pour fournir des informations météorologiques pour la position actuelle de l'utilisateur.

### Interface Utilisateur

L'interface suit les principes du Material Design et offre une interface propre et intuitive pour accéder aux informations météorologiques. L'application prend en charge les thèmes clair et sombre.

## Démarrage

1. Cloner le dépôt
2. Ouvrir le projet dans Android Studio
3. Ajouter votre clé API OpenWeatherMap dans le fichier de configuration approprié
4. Compiler et exécuter l'application

## Prérequis

- Android 5.0 (API niveau 21) ou supérieur
- Connexion Internet pour les données en temps réel
- Permissions de localisation pour la météo locale

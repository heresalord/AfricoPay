# AfricoPay — Document d'Exigences Produit (PRD)

**Projet :** AfricoPay
**Version :** 0.1.0
**Statut :** Développement Initial
**Langue principale :** Français
**Plateforme :** Android Smart POS (Android 13+)

---

## 1. Vision

AfricoPay est une application de paiement marchand conçue pour les terminaux Android Smart POS.

Son objectif est de fournir aux marchands une expérience de paiement complète, professionnelle et sécurisée, tout en restant indépendante de tout processeur de paiement spécifique.

L'application doit pouvoir fonctionner sur différents fabricants de Smart POS et intégrer un ou plusieurs partenaires de paiement sans modifier l'interface utilisateur.

La première version publique simulera les transactions de paiement tandis que toute l'infrastructure sera préparée pour une future intégration avec des processeurs de paiement certifiés.

---

## 2. Objectifs du Projet

AfricoPay doit :

- Détecter automatiquement les capacités du terminal.
- Afficher uniquement les méthodes de paiement prises en charge.
- Fournir une expérience de paiement professionnelle.
- Fonctionner hors ligne dans la mesure du possible.
- Être déployable à distance.
- Être maintenable grâce à une architecture modulaire.
- Être prête pour l'intégration SDK/API.
- Prendre en charge plusieurs fabricants de Smart POS.

---

## 3. Marchés Cibles

**Marchés primaires :**
- Bénin
- Togo
- Côte d'Ivoire
- Sénégal

**Expansion future :**
- UEMOA
- Afrique Francophone
- Déploiement Pan-Africain

---

## 4. Appareils Pris en Charge

L'application ne doit pas dépendre d'un seul fabricant de matériel.

**Cibles initiales :**
- ZCS
- Sunmi
- PAX
- Newland
- MoreFun
- Tout Android Smart POS prenant en charge Android 13+

---

## 5. Stack Technologique

| Composant | Technologie |
|-----------|-------------|
| Langage | Kotlin |
| UI | Jetpack Compose |
| Architecture | Clean Architecture |
| Pattern | MVVM |
| Injection de dépendances | Hilt |
| Base de données | Room |
| Réseau | Retrofit |
| Sérialisation | Kotlin Serialization |
| Asynchrone | Coroutines |
| Réactif | StateFlow |
| Journalisation | Timber |
| Gestion de version | Git / GitHub |
| CI/CD | GitHub Actions |

---

## 6. Gestion de Version

La gestion de version est obligatoire dès le premier commit.

**Dépôt :** GitHub

**Stratégie de branches :**
```
main
develop
feature/<nom-fonctionnalité>
release/<version>
hotfix/<version>
```

**Format des commits :**
```
feat:     Nouvelle fonctionnalité
fix:      Correction de bug
refactor: Refactorisation
docs:     Documentation
test:     Tests
ci:       Intégration continue
build:    Build
```

**Versionnage :** Semantic Versioning `MAJOR.MINOR.PATCH` — Exemple : `0.1.0`

---

## 7. Intégration Continue

**À chaque Push :**
- Compilation du projet
- Exécution des tests unitaires
- Génération de l'APK Debug

**À chaque Release :**
- Build de l'APK Release
- Upload des artefacts
- Génération des notes de version

---

## 8. Détection Matérielle

Au démarrage, AfricoPay détecte :

| Composant | Description |
|-----------|-------------|
| Version Android | OS du terminal |
| Fabricant | Marque du terminal |
| Modèle | Modèle exact |
| CPU | Processeur |
| RAM | Mémoire vive |
| Stockage | Espace disque |
| Batterie | Niveau et statut |
| Réseau | Wi-Fi, Données mobiles |
| Bluetooth | Disponibilité |
| NFC | Paiement sans contact |
| Lecteur EMV | Carte à puce |
| Lecteur Bande Magnétique | Carte à piste |
| Imprimante Thermique | Impression de reçus |
| Caméra | Scanner QR/codes |
| Scanner Code-barres | Lecture de codes |
| GPS | Géolocalisation |
| USB | Connectivité USB |

Le matériel détecté est stocké localement.

---

## 9. Méthodes de Paiement

L'application affiche automatiquement uniquement les méthodes de paiement compatibles.

**Méthodes disponibles :**

| Méthode | Matériel requis |
|---------|----------------|
| Carte Sans Contact | NFC |
| Carte à Puce (EMV) | Lecteur EMV |
| Bande Magnétique | Lecteur MSR |
| QR Code | Caméra / Scanner |
| Mobile Money | Réseau |
| Espèces | Aucun |

**Futures méthodes :**
- Google Pay
- Apple Pay
- Samsung Pay
- Virement Bancaire
- USSD

---

## 10. Écran d'Accueil

- Interface en français
- Clavier numérique
- Montant
- Devise (XOF par défaut)
- Bouton Continuer
- Bouton Annuler

---

## 11. Flux de Paiement

```
Marchand saisit le montant
        ↓
     Continuer
        ↓
AfricoPay affiche les méthodes compatibles
        ↓
Marchand sélectionne une méthode
        ↓
     Écran de paiement
        ↓
     Traitement
        ↓
     Résultat
        ↓
      Reçu
        ↓
Historique des transactions
```

---

## 12. Simulation de Paiement

Le simulateur remplace le futur processeur de paiement.

**Résultats possibles :**

| Code | Description |
|------|-------------|
| `APPROVED` | Approuvé |
| `DECLINED` | Refusé |
| `TIMEOUT` | Délai dépassé |
| `NETWORK_ERROR` | Erreur réseau |
| `CANCELLED` | Annulé |
| `CARD_EXPIRED` | Carte expirée |
| `INSUFFICIENT_FUNDS` | Fonds insuffisants |
| `ISSUER_OFFLINE` | Émetteur hors ligne |
| `UNKNOWN_ERROR` | Erreur inconnue |

Les règles de simulation sont configurables.

---

## 13. NFC

**Version actuelle :**
- Détecter la capacité NFC
- Écouter les événements NFC
- Détecter la présentation d'une carte/tag
- Simuler la transaction

**Version future :**
- Remplacer la simulation par le SDK partenaire

---

## 14. EMV

**Version actuelle :**
- Détecter le lecteur
- Afficher l'écran d'insertion
- Simuler le traitement

**Version future :** SDK Partenaire

---

## 15. Bande Magnétique

**Version actuelle :**
- Détecter le lecteur
- Afficher l'écran de glissement
- Simulation

**Version future :** SDK Partenaire

---

## 16. Paiements QR

**Version actuelle :**
- Générer un QR de test

**Version future :** API Processeur

---

## 17. Mobile Money

**Opérateurs :**
- MTN
- Moov
- Orange
- Wave

**Version actuelle :** Simulation

**Version future :** API Processeur

---

## 18. Impression de Reçus

Prise en charge de l'imprimante thermique intégrée.

**Le reçu comprend :**
- Marchand
- Date
- Heure
- Référence
- Type de Paiement
- Statut
- Montant
- Terminal
- Numéro de Reçu

**Les reçus de simulation incluent :** `TRANSACTION TEST`

---

## 19. Historique des Transactions

- Stockage local
- Recherche
- Filtrage
- Réimpression
- Export

**Future :** Synchronisation cloud

---

## 20. Tableau de Bord

| Indicateur | Description |
|-----------|-------------|
| Volume du jour | Nombre de transactions |
| Montant du jour | Total en XOF |
| Approuvées | Transactions réussies |
| Refusées | Transactions échouées |
| Statut appareil | État du terminal |
| Statut réseau | Connectivité |
| Batterie | Niveau de charge |
| Statut imprimante | Disponibilité |

---

## 21. Diagnostic Matériel

Page dédiée. Pour chaque composant :
- Disponible / Non disponible
- Version
- Informations du pilote (si disponible)

---

## 22. Paramètres Marchand

| Paramètre | Description |
|-----------|-------------|
| Nom Marchand | Nom commercial |
| ID Marchand | Identifiant unique |
| Adresse Boutique | Adresse physique |
| Téléphone | Numéro de contact |
| Devise | XOF par défaut |
| Langue | Français par défaut |
| Pied de Reçu | Message personnalisé |
| Mode Simulation | Activer/Désactiver |
| Version Application | Numéro de version |
| Version Base de Données | Schéma DB |
| ID Terminal | Identifiant terminal |

---

## 23. Configuration Distante (Future)

Configuration future depuis le backend :
- Profil marchand
- Devises
- Méthodes de paiement activées
- Pied de reçu
- Paramètres application
- Paramètres de risque
- Configuration terminal

Aucune mise à jour de l'application ne doit être requise pour les changements de configuration.

---

## 24. Feature Flags

Chaque fonctionnalité de paiement peut être activée à distance.

**Exemples :**
- Activer NFC
- Désactiver QR
- Activer Mobile Money
- Activer Nouveau SDK
- Activer Mode Test

Les feature flags sont téléchargés depuis le backend.

---

## 25. Mise à Jour Automatique

AfricoPay vérifie périodiquement les mises à jour.

**Future :**
- Téléchargement en arrière-plan
- Approbation administrateur
- Mise à jour silencieuse si supportée

---

## 26. Couche d'Abstraction Matérielle (HAL)

L'UI ne doit jamais communiquer directement avec les SDKs des fabricants.

**Interfaces :**
- `PrinterService`
- `NfcService`
- `EmvService`
- `MagStripeService`
- `ScannerService`
- `CameraService`

**Implémentations :**
- ZCS
- Sunmi
- PAX
- Newland
- MoreFun

Les futurs fabricants nécessitent uniquement une nouvelle implémentation.

---

## 27. Offline First

AfricoPay doit rester utilisable sans Internet.

**Stockage local :**
- Transactions
- Reçus
- Profil marchand
- Cache de configuration
- Journaux

La synchronisation s'effectue automatiquement au retour de la connectivité.

---

## 28. Enregistrement des Appareils

Chaque terminal reçoit un ID Appareil unique.

**Le backend stocke :**
- Appareil
- Marchand
- Version logicielle
- Modèle
- Statut
- Dernière synchronisation

---

## 29. Sécurité

- PIN Administrateur
- PIN Marchand
- Paramètres protégés
- Pas de secrets bancaires stockés
- Pas de PAN
- Pas de CVV
- Base de données locale chiffrée si approprié

---

## 30. Journalisation

**Enregistrement de :**
- Démarrage
- Arrêt
- Paiements
- Erreurs
- Impression
- Matériel
- Réseau
- Mises à jour
- Synchronisation

Les journaux sont exportables.

---

## 31. Backend (Future)

- Gestion des marchands
- Gestion des appareils
- Synchronisation des transactions
- Feature Flags
- Configuration
- Mises à jour logicielles
- Monitoring
- Reporting
- Authentification API

---

## 32. Futurs Partenaires de Paiement

L'application doit prendre en charge plusieurs processeurs via des adaptateurs interchangeables.

**Intégrations possibles :**
- Flutterwave
- PayDunya
- Onafriq
- MineSec
- Interswitch
- Banques acquéreuses locales

Aucun fournisseur de paiement ne doit nécessiter des modifications de l'interface utilisateur.

---

## 33. Écosystème Futur

- AfricoPay Mobile
- AfricoPay Smart POS
- AfricoPay Portail Marchand
- AfricoPay Gestionnaire d'Appareils
- AfricoPay Portail Partenaire
- AfricoPay APIs
- AfricoPay Dashboard
- AfricoPay Plateforme de Règlement

---

## 34. Objectif à Long Terme

AfricoPay deviendra un écosystème de paiement marchand complet capable de fonctionner sur plusieurs terminaux Android Smart POS à travers l'Afrique Francophone, offrant une expérience de paiement unifiée indépendante des fabricants de matériel et des processeurs de paiement, tout en permettant un déploiement rapide, une gestion à distance et une certification future.

**Structure Multi-Dépôt :**
- `africopay-android` — Application Android (APK)
- `africopay-backend` — APIs, gestion des appareils, configuration distante, feature flags
- `africopay-docs` — PRD, spécifications techniques, contrats API, schéma DB, directives UI

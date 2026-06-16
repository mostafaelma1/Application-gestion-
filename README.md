# MenuCalc — Coût des menus 🍲

MenuCalc est une application Android native pour **calculer le coût d'un menu
en restauration collective** (cantine, hôpital, école, entreprise…). On saisit
les **ingrédients** et leurs prix d'achat, on compose des **plats** (fiches
techniques), puis des **menus** servis à un nombre de couverts donné — et
l'application calcule le **coût par portion**, le **coût par personne**, le
**coût total** du service et un **prix de vente conseillé**.

Tout fonctionne **100% en local, hors-ligne et gratuitement** : aucun compte,
aucune clé API, aucune connexion internet. Toutes les données restent sur
l'appareil (SQLite/Room).

---

## ✨ Fonctionnalités

- **Accueil** — accès aux trois espaces : Ingrédients, Plats & recettes, Menus.
- **Ingrédients** — banque de matières premières : nom, unité (kg, L, pièce…)
  et prix d'achat par unité. Ajout / modification / suppression.
- **Plats & recettes (fiche technique)** — un plat produit un nombre de
  portions à partir d'une liste d'ingrédients quantifiés. L'app calcule en
  direct le **coût matière total** et le **coût par portion**.
- **Menus** — on regroupe des plats, on saisit le **nombre de couverts**, un
  pourcentage de **frais généraux** et une **marge**. L'app affiche :
  - Coût matière / personne
  - Frais généraux / personne
  - Coût de revient / personne
  - Marge / personne
  - **Prix de vente conseillé / personne**
  - **Food cost %**
  - Coût matière total, chiffre d'affaires et bénéfice estimé pour tout le service.
- **Réglages** — choix de la **devise** (DH, €, $…).

---

## 🧮 Modèle de calcul

```
Coût d'un plat        = Σ (quantité ingrédient × prix unitaire)
Coût par portion      = coût du plat ÷ nombre de portions
Coût matière (CM/pers)= Σ coût/portion des plats du menu
Frais généraux        = CM × frais%
Coût de revient       = CM + frais généraux
Marge                 = coût de revient × marge%
Prix de vente / pers. = coût de revient + marge
Food cost %           = CM ÷ prix de vente × 100
Totaux service        = valeurs/personne × nombre de couverts
```

La logique de calcul est isolée dans `calc/CostCalculator.kt` (fonctions pures,
sans dépendance Android).

---

## 🏗️ Tech stack

| Concern        | Choix                                            |
| -------------- | ------------------------------------------------ |
| Langage        | Kotlin                                           |
| UI             | Android Views + Material 3 + ViewBinding         |
| Persistance    | Room (SQLite)                                     |
| Async          | Kotlin Coroutines                                |
| Min / Target   | Android 7.0 (API 24) / Android 14 (API 34)       |

Aucune permission, aucune connexion internet.

---

## 🚀 Démarrage

1. Ouvrir le projet dans **Android Studio** — `local.properties` est créé
   automatiquement avec votre `sdk.dir`. En ligne de commande, copier
   `local.properties.sample` vers `local.properties` et renseigner `sdk.dir`.
2. Build & run :
   ```bash
   ./gradlew assembleDebug
   ```

Un APK est aussi publié automatiquement par GitHub Actions à chaque push sur la
branche de développement — voir les **Releases** du dépôt pour un téléchargement
direct.

---

## 📂 Structure du projet

```
app/src/main/java/com/menucalc/app/
├── MenuCalcApp.kt            # Application : DB + prefs
├── MainActivity.kt           # Accueil (navigation)
├── IngredientsActivity.kt    # Ingrédients : liste + ajout/édition
├── DishesActivity.kt         # Liste des plats
├── DishEditActivity.kt       # Fiche technique + recette + coûts
├── MenusActivity.kt          # Liste des menus
├── MenuEditActivity.kt       # Composition + résultat chiffré
├── SettingsActivity.kt       # Devise
├── calc/CostCalculator.kt    # Moteur de calcul (pur)
├── data/                     # Entités Room, DAO, base, Prefs
├── ui/                       # Adapters RecyclerView
└── util/Money.kt             # Formatage montants / %
```

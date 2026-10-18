# 🚀 GitHub Actions CI/CD Setup

## Overview

Ce projet utilise **GitHub Actions** pour compiler automatiquement les APKs à chaque push.

## ✅ Workflow Configuré

**Fichier:** `.github/workflows/build.yml`

### Déclencheurs
- ✅ Push sur `master`, `main`, `develop`
- ✅ Pull requests vers `master`, `main`
- ✅ Création de tags (release)

### Étapes Automatiques

1. **Build Release APK** → Optimisée, signée
2. **Build Debug APK** → Pour tests rapides
3. **Exécuter les tests** → Assurance qualité
4. **Upload des artifacts** → Téléchargeable pendant 30j (release) / 7j (debug)
5. **Commentaire auto sur PR** → Confirmation du build
6. **Création de Release** → Génération automatique sur tags

## 📋 Prérequis pour GitHub

### 1. Créer un repo GitHub

```bash
# À partir de D:\Workspace\Projets_Apps_Android\Cartes_invitations

git remote add origin https://github.com/YOUR_USERNAME/cartes-invitations.git
git branch -M master
git push -u origin master
```

### 2. Secrets GitHub optionnels (pour signature des APKs)

Si tu veux signer les APKs en production :

**Settings → Secrets and variables → Actions**

Ajoute :
- `KEYSTORE_FILE` (base64 du fichier .jks)
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

### 3. Permissions de Workflow

**Settings → Actions → General → Workflow permissions**

Autorise :
- ✅ Read and write permissions
- ✅ Allow GitHub Actions to create and approve pull requests

## 🎯 Comment Utiliser

### Build Automatique (À chaque push)

```bash
cd D:\Workspace\Projets_Apps_Android\Cartes_invitations

# Commit & push → GitHub Actions compile automatiquement
git add .
git commit -m "feat: amélioration du composable XYZ"
git push origin master
```

**Résultat :**
- ✅ Build lancé automatiquement
- ✅ APK disponible dans **Actions → Artifacts** après ~5-10 minutes
- ✅ Commentaire auto sur la PR (si PR)

### Build Local (Pour tests rapides)

```bash
# Compiler localement avec les mêmes commandes que CI
./gradlew assembleRelease
./gradlew assembleDebug
```

### Créer une Release (Tag)

```bash
# Créer un tag de version
git tag -a v1.1.0 -m "Release v1.1.0: Améliorations UI"
git push origin v1.1.0

# → GitHub Actions crée automatiquement une Release
# → APK téléchargeable depuis Releases
```

## 📊 Statut des Builds

Voir : `https://github.com/YOUR_USERNAME/cartes-invitations/actions`

## 🔄 Pour les Autres Projets Android

**Appliquer le même workflow à :**

```
✅ Cartes_invitations
📦 izaia_lire_plus (Flutter - adapter le workflow)
📦 TndT-Vocal (Gradle - même workflow)
```

### Template pour autres projets

Copier `.github/workflows/build.yml` dans les autres projets Android :

```bash
cp Cartes_invitations/.github/workflows/build.yml \
   izaia_lire_plus/.github/workflows/build.yml

cp Cartes_invitations/.github/workflows/build.yml \
   TndT-Vocal/.github/workflows/build.yml
```

## 🛠️ Troubleshooting

### ❌ Build échoue : "SDK not found"

**Solution :** Le workflow utilise `android-actions/setup-android@v3` qui configure le SDK automatiquement. Pas besoin de local.properties en CI.

### ❌ APKs non signées

**Solution :** Pour la signature de production, ajoute les secrets GitHub (voir Prérequis #2).

### ❌ Tests échouent

```bash
# Désactiver les tests temporairement dans le workflow
# (commenter la ligne "Run Tests")

# Ou corriger les tests localement d'abord :
./gradlew test
```

## 📦 Télécharger les APKs Compilées

1. Vais sur : `https://github.com/YOUR_USERNAME/cartes-invitations/actions`
2. Clique sur le dernier workflow "Build & Deploy APK"
3. Télécharge **InviteCard-release** ou **InviteCard-debug**
4. Installe sur le téléphone :

```bash
adb install -r InviteCard-release.apk
```

---

**Prochaines étapes :**
1. ✅ Crée un repo GitHub
2. ✅ Push le code
3. ✅ Regarde les workflows s'exécuter
4. ✅ Applique le même setup aux autres projets Android

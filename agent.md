# 🎨 Atelier de Création d'Invitations / InviteCard App – Agent Guide
> File created on May 30, 2026

Welcome to the **Atelier de Création d'Invitations (InviteCard)** project. This file provides context, tech stack overview, conventions, and developer guidelines for AI agents working on this codebase.

---

## 🎯 1. Project Context & Mission
The project is a dual-tier invitation card creator:
1. **Interactive Web Mockup** (Project Root): A sleek, client-side HTML/CSS/JS single-page application simulating an elegant 3D double-page folding wedding card. It includes custom themes ("Perles & Or", "Or Royal", "Blush Floral", "Nuit Étoilée"), interactive 3D folding controls, and print/PDF optimization.
2. **Native Android Application** (under `[app](file:///C:/ProjetsAndroid/Cartes_invitations/app)`): A fully native Jetpack Compose application designed to curate, edit, and share personalized invitations. It features real-time styling, QR RSVP code generation, Hilt dependency injection, view models, draft saving/loading, and a Gemini API integration for AI-powered element generation.

---

## 🏗️ 2. Tech Stack & Architecture

### Web Application (Root)
* **Core Layout**: HTML5 Semantic markup, responsive designs.
* **Styling**: Vanilla CSS3 supporting 3D transforms (`rotateY`, `transform-style: preserve-3d`), custom variables, print stylesheets (`@media print`).
* **Interactions**: Vanilla JavaScript for real-time bindings, dynamically rendering curved pearl strings, drag-and-drop state, and print previews.

### Android Application (`/app`)
* **Framework**: Kotlin & Jetpack Compose (Modern Declarative UI).
* **Dependency Injection**: Dagger Hilt (`@AndroidEntryPoint`, `@HiltAndroidApp`).
* **Image Loading**: Coil 3 (`AsyncImage`).
* **Architecture Pattern**: MVVM (Model-View-ViewModel) utilizing Kotlin StateFlows and Compose `collectAsStateWithLifecycle`.
* **Database / Storage**: Local JSON draft caching via a helper utility.

---

## 📂 3. File Map & Key References

### Web Core Files
* **[index.html](file:///C:/ProjetsAndroid/Cartes_invitations/index.html)**: Main web page. Defins the 3D card wrapper, settings panels, tab systems, guide modal, and print containers.
* **[index.css](file:///C:/ProjetsAndroid/Cartes_invitations/index.css)**: Holds all styles. Defins the premium visual identity (Pearl shadows, gold linear gradients, starry skies) and layout logic.
* **[index.js](file:///C:/ProjetsAndroid/Cartes_invitations/index.js)**: Runs page interactions. Handles 3D folding angles, dynamic pearl render paths, data bindings, photo preloading, and print hooks.

### Android Core Code
* **[MainActivity.kt](file:///C:/ProjetsAndroid/Cartes_invitations/app/src/main/java/com/invitecard/MainActivity.kt)**: Application entry point and navigation host (`NavHost`).
* **[InvitationEditorScreen.kt](file:///C:/ProjetsAndroid/Cartes_invitations/app/src/main/java/com/invitecard/ui/editor/InvitationEditorScreen.kt)**: Compose-based invitation editor UI. Features canvas layers, touch gesture listeners, bottom toolbars, and Hilt integration.
* **[InvitationEditorViewModel.kt](file:///C:/ProjetsAndroid/Cartes_invitations/app/src/main/java/com/invitecard/viewmodel/InvitationEditorViewModel.kt)**: Manages editor state (background color, rotation, saturation, items list, selected items, Gemini progress).
* **[CardExporter.kt](file:///C:/ProjetsAndroid/Cartes_invitations/app/src/main/java/com/invitecard/util/CardExporter.kt)**: Utility to print Compose bitmaps or save images to the device gallery.
* **[DraftManager.kt](file:///C:/ProjetsAndroid/Cartes_invitations/app/src/main/java/com/invitecard/util/DraftManager.kt)**: Serializes/deserializes composing element layouts to maintain drafts across sessions.
* **[QRCodeGenerator.kt](file:///C:/ProjetsAndroid/Cartes_invitations/app/src/main/java/com/invitecard/util/QRCodeGenerator.kt)**: Generates local QR codes for RSVP URLs using standard encoding.

---

## ⚡ 4. Essential CLI Commands

For Android development, you can use these Gradle commands from the root directory:

```powershell
# Build debug APK
./gradlew assembleDebug

# Install debug APK on connected device/emulator
./gradlew installDebug

# Clean build artifacts
./gradlew clean

# Run unit tests
./gradlew test
```

For the Web application, you can view the page by opening `index.html` in a web browser or starting a local HTTP server:

```powershell
# Start a simple HTTP server (if Python is installed)
python -m http.server 8000
```

---

## 🎨 5. Brand Identity & Design System

When building features or modifying files, preserve these premium styling details:
* **Palette**: 
  * *Perles & Or (Pearl)*: Warm ivory (#fdfbf7), gold gradients (#d4af37, #f3e5ab, #aa7c11), and elegant grey borders.
  * *Floral*: Soft pink gradients, rose tones, blush background.
  * *Nuit Étoilée (Starry)*: Deep midnight blue and golden stars.
* **Typography**:
  * Calligraphy / Cursive names: `Great Vibes` (default) or custom elegant scripts.
  * Captions & body: `Montserrat` (Clean Sans-Serif) and `Playfair Display` (Classic Serif) for titles.
* **Layout boundaries**: Keep print assets responsive, using Landscape orientation, removing margins, and supporting double-sided short-edge folding.

---

## 🛡️ 6. Guidelines for AI Agents

1. **Maintain Clean Code**: Keep ViewModels free of Android platform dependencies (use Hilt inject and context wrappers in utils).
2. **Handle Resources Gracefully**: Always close file streams and check permissions before performing read/write operations on the user's Android gallery.
3. **No Placeholders**: Ensure all resources (e.g., couple photo `couple.jpg`) resolve to valid default assets or fallback patterns.
4. **Preserve Comments**: Do not strip existing documentation or operational guides from `index.js` or Kotlin files.
5. **Dynamic Elements**: Keep elements in Compose editor moveable and scaleable using touch input, respecting user screen margins.

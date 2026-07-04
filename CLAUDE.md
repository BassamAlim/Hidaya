# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workflow
- Do not run Gradle builds (`./gradlew assembleDebug`, `compileDebugKotlin`, etc.) to verify changes. The user always verifies builds manually.

## Project Overview
Hidaya is an Android Islamic app built with Kotlin and Jetpack Compose. Originally developed with Java/XML, it was migrated to Kotlin/Jetpack Compose. The app provides prayer times, Quran reading, Islamic remembrances, Qibla direction, recitations, and various Islamic tools.

## Development Commands

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build Android App Bundle (AAB) for Play Store
./gradlew bundleRelease

# Clean build artifacts
./gradlew clean
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires Android device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "ExampleUnitTest"
```

### Development Commands
```bash
# Install debug build on connected device
./gradlew installDebug

# Check dependencies for updates
./gradlew dependencyUpdates
```

## Architecture Overview

### Clean Architecture Pattern
The app follows Clean Architecture with clear separation of concerns:
- **Domain Layer**: Business logic and entities
- **Data Layer**: Repositories, data sources (Room database, Preferences)
- **Presentation Layer**: UI components, ViewModels, and screens

### Project Structure
- `core/`: Shared functionality across features
  - `data/`: Repositories, data sources (Room database, DataStore preferences)
  - `di/`: Dependency injection modules (Hilt)
  - `enums/`: Application-wide enumerations
  - `helpers/`: Utility classes (Navigator, Alarm, PrayerTimeCalculator)
  - `models/`: Data models and entities
  - `nav/`: Navigation configuration
  - `ui/`: Shared UI components and theming
- `features/`: Feature-specific modules, each with domain/ui structure
  - Each feature contains: domain (business logic), ui (screens, ViewModels, UI state)

### Key Technologies
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **DI**: Hilt (Dagger)
- **Database**: Room with SQLite
- **Preferences**: DataStore (replacing SharedPreferences)
- **Navigation**: Jetpack Navigation Compose
- **Networking**: Firebase services
- **Media**: Media3 for audio playback
- **Location**: Google Play Services Location

### Database Structure
The app uses Room database (`HidayaDB.db`) located in `assets/databases/` with various entities for prayers, Quran data, books, remembrances, and user preferences.

### Feature Modules
Key features are organized as independent modules:
- **Prayers**: Prayer times calculation, notifications, settings
- **Quran**: Quran reader with audio recitations, bookmarks, search
- **Books**: Islamic books reader with search functionality
- **Remembrances**: Islamic remembrances (Athkar) by categories
- **Recitations**: Audio recitations by various reciters
- **Quiz**: Islamic knowledge quiz system
- **Qibla**: Compass pointing to Mecca
- **Radio**: Live Islamic radio streams

### Configuration
- Minimum SDK: 23 (Android 6.0)
- Target SDK: 36
- Java compatibility: VERSION_21
- Uses version catalog in `gradle/libs.versions.toml`
- Environment-specific configuration through `.env` file for signing configs

### Data Flow
1. UI screens observe ViewModels
2. ViewModels call domain use cases
3. Domain layer calls repositories
4. Repositories coordinate between local (Room/DataStore) and remote (Firebase) data sources
5. Data flows back through the same layers to update UI state
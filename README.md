# PassGo

A professional Android password manager with encrypted local storage.

> **Status:** Milestone 1 — Core Foundation (Complete)
> **Target:** Android 16 (API 36), APK distribution

## Architecture

Feature-first, single-module Android app built with:

- **Kotlin 2.4.0** + **Jetpack Compose** + **Material 3**
- **Hilt** for dependency injection
- **Navigation Compose** with bottom navigation + auth flow (setup/unlock)
- **Room + SQLCipher** for encrypted local storage
- **AndroidKeyStore** for hardware-backed key protection
- **EncryptedSharedPreferences** for master password hash/salt storage
- **PBKDF2-HMAC-SHA256** (600K iterations) for key derivation and password hashing
- **DataStore Preferences** for app settings persistence
- **Coroutines + Flow** for async operations

## Features

| Feature | Status |
|---|---|
| Encrypted local database (SQLCipher) | ✅ |
| Master password creation with strength validation | ✅ |
| Vault unlock with password verification | ✅ |
| Session management with auto-lock | ✅ |
| Theme switching (Light / Dark / System) | ✅ |
| Dashboard with vault stats and security tips | ✅ |
| App settings (theme, auto-lock, version) | ✅ |
| Logging infrastructure | ✅ |
| Error handling (AppResult) | ✅ |

## Screens

| Screen | Route | Description |
|---|---|---|
| Setup | `/setup` | Master password creation (first launch) |
| Unlock | `/unlock` | Password verification (subsequent launches) |
| Home | `/home` | Dashboard with stats, security status, tip |
| Vault | `/vault` | Encrypted items list |
| Premium | `/premium` | Upgrade features |
| Settings | `/settings` | Theme, auto-lock, app info |

## Build

```bash
# Debug APK
./gradlew :app:assembleDebug

# Unit tests
./gradlew testDebugUnitTest

# Instrumentation tests
./gradlew connectedDebugAndroidTest
```

## Milestones

| # | Milestone | Status |
|---|---|---|
| M0 | Project Foundation | ✅ Complete |
| M1 | Core Foundation | ✅ Complete |
| M2 | Vault CRUD | ⏳ Pending |
| M3 | Vault Features | ⏳ Pending |
| M4 | Autofill + Export | ⏳ Pending |
| M5 | Security + Polish | ⏳ Pending |

# Wupp

**Wupp** is a Jetpack Compose + Go (`whatsmeow`) Android companion client built for speed, WhatsApp-parity UI, privacy toggles, Channels, Statuses, Media Vault, and direct GitHub Release auto-updates.

## Features

- **Native-parity UI**: Chats, Updates (Statuses + `@newsletter` Channels), Communities, Settings, rich inline timestamps, floating reaction picker, contextual message bar, and read-only admin-locked group support.
- **Go `whatsmeow` Engine (`whatsbridge`)**: Real-time companion sync, monotonic delivery/read tick reconciliation, call & group stub tracking, and local media decryption.
- **Privacy & Media Controls**: Ghost Mode (freeze last seen), hide read/played/typing receipts, anti-delete retention, and ephemeral immunity toggles.
- **In-App Auto-Updater**: Checks GitHub Releases (`releases/latest`), notifies on new versions, displays release notes, downloads `.apk` assets with progress tracking, and launches Android package install.
- **In-App GitHub Issue & Feedback Reporting**: Report bugs, request features, or submit feedback straight from Settings or the top overflow menu with pre-filled device/version diagnostics.

## Architecture

- **Android UI & Persistence**: Kotlin, Jetpack Compose (Material 3), Room DB + FTS, Media3 ExoPlayer, Coil (`app/`).
- **WhatsApp Protocol Bridge**: Go + `go.mau.fi/whatsmeow` compiled to `app/libs/whatsbridge.aar` via `gomobile` (`whatsbridge/`).

## Building Locally

### Prerequisites
- Go `1.26+` and `gomobile` (`go install golang.org/x/mobile/cmd/gomobile@latest`)
- Android SDK (API 34) + Android NDK
- JDK 17 & Gradle `8.9+`

### 1. Build `whatsbridge.aar`
```bash
gomobile init
cd whatsbridge
gomobile bind -target=android/arm64 -androidapi 26 -o ../app/libs/whatsbridge.aar .
```

### 2. Configure GitHub Repo Slug (for Auto-Updater & Issues)
Update `WUPP_GITHUB_REPO=<owner>/<repo>` in `gradle.properties` (or automatically inferred from `git remote origin`).

### 3. Build APK
```bash
gradle assembleDebug
```

## Publishing a GitHub Release (Auto-Update Ready)

Tagging and pushing a semantic version tag automatically triggers `.github/workflows/release.yml`, which compiles `whatsbridge.aar`, builds and signs `Wupp-<tag>-arm64.apk`, generates `SHA256SUMS.txt`, and publishes a GitHub Release picked up by Wupp's in-app updater:

```bash
git tag v1.0.1
git push origin v1.0.1
```

Optional repository secrets for production release signing:
- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

## Feedback & Issue Tracking

- In the app: open **Settings -> Updates & GitHub Support** or tap **⋮ -> Report bug / Feedback**.
- Or open an issue directly using the Bug Report, Feature Request, and Feedback templates on GitHub.

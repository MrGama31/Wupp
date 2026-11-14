# Wupp

Wupp is an Android client for WhatsApp built with Jetpack Compose and Go (`whatsmeow`). It pairs as a linked companion device and adds privacy controls, view-once capture, channel and status viewing, and in-app updates backed by GitHub Releases.

The app is still under active development and does not have a stable release yet. New work goes to `dev`, and `main` is reserved for tested releases.

## What is in the app

- Chat threads, status updates, communities, and unencrypted channels (`@newsletter`) stored locally in Room with full-text search
- A Go bridge (`whatsbridge`) compiled to `whatsbridge.aar` that handles the WhatsApp Web companion connection, media decryption, group lock states, reactions, and live read/delivery receipts
- Privacy switches in Settings to freeze last seen, suppress read, played, and typing indicators, keep deleted messages and statuses, and block disappearing message expiry
- In-app update checks against GitHub Releases, with changelog previews, APK download progress, and one-tap package installation
- Built-in bug report, feature request, and feedback dialogs under Settings and the top menu that open pre-filled GitHub issues with app and device details

## Repository layout

`app/` contains the Kotlin and Jetpack Compose Android client. `whatsbridge/` contains the Go source that wraps `go.mau.fi/whatsmeow` via `gomobile`.

## Branches and releases

Because Wupp has not reached a stable `v1.0.0` yet, development happens on `dev`.

- Push work-in-progress commits and open pull requests against `dev`.
- Merge `dev` into `main` only when a build has been tested end to end and is ready to ship.
- Use pre-release tags such as `v0.1.0-alpha.1` or `v1.0.0-beta.1` when testing `.github/workflows/release.yml` on unfinished builds. Any tag containing a hyphen is automatically published as a pre-release on GitHub.
- Tagging `v1.0.0` (or another version without a hyphen) creates a standard GitHub release that the in-app updater picks up.

## Build locally

You need Go 1.26 or newer, `gomobile`, JDK 17, Gradle 8.9 or newer, and Android SDK 34 with NDK installed.

Compile the Go bridge first:

```bash
gomobile init
cd whatsbridge
gomobile bind -target=android/arm64 -androidapi 26 -o ../app/libs/whatsbridge.aar .
```

`app/build.gradle.kts` reads `BuildConfig.GITHUB_REPO` from `git remote origin` if present, or falls back to `WUPP_GITHUB_REPO` in `gradle.properties`. Once `whatsbridge.aar` is in `app/libs/`, build the debug APK:

```bash
gradle assembleDebug
```

## Signing GitHub releases

Tag pushes matching `v*.*.*` trigger `.github/workflows/release.yml`. If you add the four signing secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) in repository settings, CI signs the APK with your release key. Without those secrets, CI signs with an ephemeral key so test APKs still install during development.

## Report bugs and send feedback

Open **Settings > Updates & GitHub Support** inside Wupp, or tap the top-right menu and pick **Report bug / Feedback**. You can also open an issue directly on GitHub using the bug, feature request, or feedback forms.

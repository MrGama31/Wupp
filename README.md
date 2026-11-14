# Wupp

Wupp is an Android client for WhatsApp built with Jetpack Compose and Go (`whatsmeow`). It pairs as a linked companion device and adds privacy controls, view-once capture, channel and status viewing, and in-app updates backed by GitHub Releases.

The app is still under active development. Builds before `v1.0.0` are published as pre-releases on GitHub while work lands on `main`.

## What is in the app

- Chat threads, status updates, communities, and unencrypted channels (`@newsletter`) stored locally in Room with full-text search
- A Go bridge (`whatsbridge`) compiled to `whatsbridge.aar` that handles the WhatsApp Web companion connection, media decryption, group lock states, reactions, and live read and delivery receipts
- Privacy switches in Settings to freeze last seen, suppress read, played, and typing indicators, keep deleted messages and statuses, and block disappearing message expiry
- In-app update checks against GitHub Releases, with changelog previews, APK download progress, and one-tap package installation
- Built-in bug report, feature request, and feedback dialogs under Settings and the top menu that open pre-filled GitHub issues with app and device details

## Repository layout

`app/` contains the Kotlin and Jetpack Compose Android client. `whatsbridge/` contains the Go source that wraps `go.mau.fi/whatsmeow` via `gomobile`.

## Releases and versioning

Wupp uses `main` as its primary branch and distinguishes unstable test builds from stable releases with semantic version tags:

- Hyphenated tags such as `v0.1.0-beta.1` or `v1.0.0-alpha.1` trigger `.github/workflows/release.yml` and automatically publish as a GitHub **Pre-release**.
- Clean tags such as `v1.0.0` publish as a standard GitHub release and become the **Latest** release checked by the in-app updater.
- Every commit pushed to `main` also runs [ci.yml](.github/workflows/ci.yml) to verify the Go bridge and debug APK build.

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

Tag pushes matching `v*` trigger `.github/workflows/release.yml`. If you add the four signing secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) in repository settings, CI signs the APK with your consistent release key so updates install over older versions without losing data. Without those secrets, CI signs with a temporary key generated for that single run, which installs on a fresh device but requires uninstalling the previous temporary build before updating.

## Report bugs and send feedback

Open **Settings > Updates & GitHub Support** inside Wupp, or tap the top-right menu and pick **Report bug / Feedback**. You can also open an issue directly on GitHub using the bug, feature request, or feedback forms.

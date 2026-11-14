# Changelog

All notable changes to **Wupp** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- In-app GitHub Releases auto-updater with background launch checks, notification alerts, release notes preview, APK download progress, and one-tap installer (`WuppUpdater`).
- In-app Bug Report, Feature Request, and General Feedback dialogs pre-filled with diagnostics (`GitHubFeedbackDialog`).
- GitHub maintenance templates (`.github/ISSUE_TEMPLATE/`, `PULL_REQUEST_TEMPLATE.md`), CI/Release workflows (`.github/workflows/`), Dependabot, and project documentation.
- Monotonic real-time delivery/read ticks (`✓`, `✓✓`, blue `✓✓`), admin-only group compose locking + group change stub notices, floating emoji reaction bar, and contextual selection top bar.

## [1.0.0] - 2026-11-14

### Added
- Initial **Wupp** branding, custom adaptive app icon, notification icon, and first-install permission/background-service setup flow.
- Channels (`@newsletter`) & Status Updates parity with full-screen media and status viewers.
- Jetpack Compose UI backed by Room DB and Go `whatsmeow` bridge (`whatsbridge`).

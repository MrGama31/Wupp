# Changelog

This file tracks user-visible changes to Wupp following [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

No stable release has been tagged yet. Changes below live on `dev`.

## [Unreleased]

### Added
- In-app GitHub release checker and APK downloader (`WuppUpdater`) with startup checks, notifications, changelog view, and package installer handoff
- In-app bug report, feature request, and feedback forms pre-populated with app and Android device info
- GitHub Actions workflows for Go bridge vetting, debug CI builds on `main` and `dev`, and tagged APK releases
- Monotonic live delivery and read ticks on chat rows and messages, group admin-lock detection (`Only admins can send messages`), group change stub notices, floating reaction bar, and contextual message selection bar
- Wupp branding, adaptive icon, first-install permission setup screen, channel (`@newsletter`) viewer, and full-screen status and media viewers

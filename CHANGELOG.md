# Changelog

All notable user-visible changes to Wupp are listed here in simple words.

## [Unreleased]

## [0.1.0] - 2026-11-15

### Added
- Mandatory login setup screen after permissions, supporting both QR code scanning and phone number link codes before entering chats
- In-app GitHub release updater with startup checks, notifications, download progress, changelog viewer, and installer handoff
- In-app bug report, feature request, and feedback forms with one-tap clipboard copy and GitHub pre-fill
- Multi-ABI Go bridge (`arm64-v8a`, `armeabi-v7a`, `x86_64`) and signed GitHub Releases publishing `arm64-v8a` and `universal` APKs
- First-launch permission setup screen, privacy toggles (Ghost Mode, read/played/typing controls, anti-delete, view-once vault), channel viewer, status viewer, live message ticks, and emoji reactions

### Changed
- Set Android package ID to `com.wupp.app`
- Added sync cooldowns and version caching to cut duplicate WhatsApp server requests without slowing down normal use
- Publish only `arm64-v8a` and multi-ABI `universal` APKs for GitHub Releases

### Fixed
- Fixed real-time chat syncing and background push updates
- Fixed channel (`@newsletter`) images not loading
- Fixed missing profile pictures across chats, groups, and contacts
- Fixed initial status updates failing to load on link or launch

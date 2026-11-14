# Changelog

All notable user-visible changes to Wupp are listed here in plain language.

No release tag has been published on GitHub yet.

## [Unreleased]

### Added
- Mandatory login setup screen after permissions, supporting both QR code scanning and phone number link codes before entering chats
- In-app GitHub release updater with startup checks, notifications, download progress, changelog viewer, and installer handoff
- In-app bug report, feature request, and feedback forms with copy-to-clipboard support and GitHub pre-fill
- Multi-ABI Go bridge (`arm64-v8a`, `armeabi-v7a`, `x86_64`) and GitHub Actions release builds producing signed `arm64-v8a` and `universal` APKs
- First-launch permission setup screen, privacy controls (Ghost Mode, read/played/typing toggles, anti-delete, view-once vault), channel viewer, status viewer, live message ticks, and emoji reactions

### Changed
- Changed Android package ID to `com.wupp.app`
- Added safer sync cooldowns and version caching to reduce duplicate WhatsApp server requests while keeping normal speed

### Fixed
- Fixed real-time chat syncing and background push updates
- Fixed channel (`@newsletter`) images not loading
- Fixed missing profile pictures across chats, groups, and contacts
- Fixed initial status updates failing to load on link or launch

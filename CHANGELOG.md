# Changelog

## [Unreleased]

## [0.1.0] - 2026-11-15

- 🔗 **Mandatory link setup:** after granting permissions, link via QR scan or phone number code before entering chats.
- 🛡️ **Anti-ban and sync guards:** `Chrome (Mac OS)` companion profile, `90s` global sync cooldowns, jittered 3-worker media/avatar queues, dead-channel skipping, and an outbound guard blocking first messages to unknown numbers with no chat history.
- 🕵️ **Privacy switches:** Ghost Mode, hold blue read ticks, hide voice played receipts, hide typing/recording, keep deleted messages and statuses, and retain disappearing chats locally.
- 💬 **Fixed live sync, channels, and avatars:** split live events from heavy history sync so chats update in real time, fixed `@newsletter` channel pictures, profile photos, and initial status loading.
- 📦 **Signed `arm64-v8a` and `universal` builds:** moved package ID to `com.wupp.app` with multi-ABI `whatsbridge.aar` (`arm64-v8a`, `armeabi-v7a`, `x86_64`) so updates install in place without logging out.
- 🔄 **In-app updates and issue forms:** automatic GitHub release checks and APK installer handoff, plus pre-filled bug, feature, and feedback dialogs with one-tap copy.

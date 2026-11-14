# Contributing to Wupp

Thanks for wanting to help improve Wupp!

## Reporting Issues or Requesting Features

You can file issues directly from inside Wupp (**Settings -> Updates & GitHub Support**) or through the GitHub issue templates:
- **Bug Report**: include Wupp version, Android version/device, reproduction steps, and screenshots if relevant.
- **Feature Request**: describe the workflow or parity gap and desired behavior.
- **General Feedback**: share impressions on UI polish, speed, or usability.

## Pull Requests

1. Fork the repository and create a branch from `main` (e.g., `fix/delivery-tick-sync` or `feat/voice-waveform`).
2. Keep Go bridge changes vetted:
   ```bash
   cd whatsbridge && go vet ./...
   ```
3. Avoid modifying Room database entity schemas without bumping the version / handling migration cleanly.
4. Do not commit keystores (`*.jks`, `*.keystore`), `local.properties`, or large binary archives (`*.apk`, `*.xapk`).
5. Fill out the PR checklist and include before/after screenshots for UI changes.

# Contributing to Wupp

## Reporting issues and requesting features

You can file an issue from inside Wupp (**Settings > Updates & GitHub Support**) or directly from the GitHub issue forms. Include your Wupp version, Android version, device model, and steps to reproduce when reporting a bug.

## Pull requests

1. Fork the repository and create a branch from `main`, for example `fix/delivery-ticks` or `feat/voice-waveform`.
2. Run Go checks before committing bridge changes:
   ```bash
   cd whatsbridge && go vet ./...
   ```
3. Do not alter Room `@Entity` definitions without handling the database version and migration path.
4. Keep keystores (`*.jks`, `*.keystore`), `local.properties`, and `.apk` or `.xapk` files out of Git.
5. Open your pull request against `main` and attach before/after screenshots if you changed the UI.

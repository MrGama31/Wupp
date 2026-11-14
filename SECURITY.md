# Security policy

## Reporting a vulnerability

Do not file security bugs, credential leaks, or session-handling vulnerabilities as public GitHub issues. Report them privately through GitHub's Security tab if Private Vulnerability Reporting is enabled on this repository, or contact the maintainer directly with reproduction steps and the affected Wupp version.

## Handling local data

Wupp stores WhatsApp companion session keys, contacts, and downloaded media on the device. Never upload databases, session files, or signing keystores to issues or pull requests. Releases built by `.github/workflows/release.yml` include `SHA256SUMS.txt` so users can check downloaded APKs.

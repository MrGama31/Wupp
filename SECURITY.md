# Security Policy

## Reporting a Vulnerability

Please **do not** open a public GitHub issue for security vulnerabilities, authentication leaks, or credential-handling bugs.

Instead:
- Use GitHub's **Private Vulnerability Reporting** ("Report a vulnerability" under the Security tab) if enabled on the repository, or contact the repository maintainer directly.
- Include steps to reproduce, affected Wupp version, and impact assessment.

## Local Security Notes

- Session credentials and WhatsApp companion keys are stored locally on-device; never attach databases or keystores to issues or pull requests.
- Releases published via `.github/workflows/release.yml` include `SHA256SUMS.txt` checksums for APK verification.

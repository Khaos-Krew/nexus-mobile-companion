# Khaos Nexus Mobile Companion

Khaos Nexus Mobile Companion is the dedicated Android client for the Khaos Nexus platform. This repository is the exclusive home for mobile-only code, CI, planning, pull requests, APK update-channel files, and future mobile releases.

The Windows desktop application remains in `Khaos-Krew/Khaos-Nexus`. Mobile extends the shared platform; it does not replace the desktop app or duplicate its authority.

## Current implementation

The Android preview includes:

- a native Android application shell;
- the Khaos Nexus black, charcoal, onyx, ruby, and crimson visual system;
- Home, D&D, Servers, Nexus AI, Notifications, and Settings destinations;
- adaptive status-bar, display-cutout, gesture-navigation, and three-button-navigation safe areas;
- a signed, user-confirmed in-app APK Update Center;
- package, permission, signing, version, minimum-SDK, lint, unit-test, and SHA-256 validation;
- no production endpoint, desktop credential, Discord bot token, RCON password, provider credential, database service-role key, AI token, scheduler, or platform administration authority.

The approved desktop UI dependency is Khaos Nexus PR #194, merged at commit `0971f81b3264ce66a106a0ace129596e31c5ef62`.

## Android toolchain

- Android Gradle Plugin 9.3.0
- Gradle 9.5.0
- JDK 17
- compile and target API 36
- minimum API 26 / Android 8.0
- Java 17
- JUnit 4

## Build locally

Prerequisites:

- JDK 17
- Android SDK platform 36
- Android build tools 36.0.0
- Gradle 9.5.0

```bash
gradle --no-daemon --stacktrace clean testDebugUnitTest lintDebug assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Direct preview package:

```text
com.khaoskrew.nexuscompanion.preview
```

## Adaptive mobile layout

`NexusApplication` configures every activity for edge-to-edge rendering and applies live Android safe areas to the content container. It accounts for status bars, display cutouts, side insets, gesture navigation, and three-button navigation. Insets are replaced rather than accumulated after rotation, resume, or navigation-mode changes.

## Working in-app APK updates

Direct signed APK distribution is the primary update path. Google Play is not currently planned.

The app checks this durable HTTPS manifest:

```text
https://raw.githubusercontent.com/Khaos-Krew/nexus-mobile-companion/main/updates/preview/update-manifest.json
```

The update-manifest RSA public key is embedded in the app and also published at `updates/preview/update-manifest-public.b64`. Private APK-signing and manifest-signing keys are never committed to GitHub.

### One-time bootstrap

Existing `0.2.1` and older preview APKs were signed by temporary CI certificates. Android cannot replace those builds with a permanently signed update.

Install the stable-signed `0.2.2` bootstrap once. After that, later APKs signed with the same permanent preview certificate can replace the installed app through the Update Center.

### Update flow

1. The Update Center checks the signed HTTPS manifest.
2. The user reviews the available version and release notes.
3. The user approves download.
4. The app verifies the manifest signature, channel, host, package, version, signed size, APK SHA-256, APK identity, and signing-certificate continuity.
5. Android displays its own final installer confirmation.

Automatic download and silent installation are disabled.

### Channel files

`updates/preview/` contains:

- `update-manifest.json` — signed metadata for the newest approved APK;
- `Khaos-Nexus-Mobile-Companion-current.apk` — APK referenced by the manifest;
- `update-manifest-public.b64` — public manifest-verification key;
- `README.md` — channel operating procedure.

## Signing-key custody

The permanent preview APK signing keystore and separate update-manifest private key must be retained in at least two encrypted, access-controlled backups.

Losing the APK signing key prevents future builds from replacing the installed preview. Losing the manifest key prevents publication of trusted update metadata. Anyone who obtains either private key and its credentials could impersonate an approved update.

The GitHub workflow supports future stable signing after these repository secrets are configured:

- `PREVIEW_KEYSTORE_BASE64`
- `PREVIEW_KEYSTORE_PASSWORD`
- `PREVIEW_KEY_ALIAS`
- `PREVIEW_KEY_PASSWORD`

Until those secrets are provisioned, controlled APKs are re-signed offline with the permanent preview key before publication.

See [`docs/IN_APP_UPDATES.md`](docs/IN_APP_UPDATES.md) for the manifest contract and signing procedure.

## Product and architecture boundaries

### Mobile responsibilities

- Render mobile-optimized views and respect Android safe areas.
- Authenticate through an approved system-browser PKCE flow when live services are introduced.
- Maintain a revocable device session in secure storage.
- Request bounded, role-filtered projections.
- Cache only approved non-secret data.
- Submit guarded actions through authoritative backend APIs.
- Check and install only cryptographically verified APK updates after explicit user approval.

### Shared platform responsibilities

Khaos Nexus desktop and backend services continue to own protected credentials, Discord bots and interactions, the shared scheduler, game-server adapters, AI runtimes, campaign authority, permissions, auditing, and production release publication.

### Prohibited mobile behavior

- Direct RCON, hosting-provider, Discord, database service-role, or AI-sidecar connections.
- Embedded privileged credentials or tokens.
- A duplicate scheduler, AI router, Discord bot, notification engine, or permission model.
- Background autonomous administration.
- Silent maintenance, AI execution, or APK installation.

## Roadmap

1. Android preview foundation, safe areas, and signed Update Center.
2. Authentication and revocable device sessions.
3. Home, notifications, and deep links.
4. D&D read-only and encrypted bounded offline support.
5. Server status and guarded actions.
6. Nexus AI health and advisory review.
7. Accessibility, observability, security, performance, and real-device hardening.
8. Broader distribution only after separate Owner authorization.

## Repository workflow

- All mobile branches and pull requests target this repository's `main` branch.
- Mobile-only code must not be added to `Khaos-Krew/Khaos-Nexus`.
- Work starts from an assigned issue and exact commit.
- Pull requests remain focused and include tests and documentation.
- APK channel publication requires an increased version code, permanent APK signature, signed manifest, hash verification, and an install-over-previous-version test.

## Key issues

- #9 First installable Android APK
- #11 Canonical APK artifact from `main`
- #13 Android system navigation and safe-area correction
- #15 Stable in-app APK updates

## Release policy

No GitHub Release, Google Play upload, public deployment, or alternate release channel may be created without separate explicit Owner authorization. The repository-hosted preview update channel is limited to controlled direct APK testing.

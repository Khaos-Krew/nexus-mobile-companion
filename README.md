# Khaos Nexus Mobile Companion

Khaos Nexus Mobile Companion is the dedicated Android client for the Khaos Nexus platform. This repository is the exclusive home for mobile-only code, CI, planning, pull requests, and future mobile releases.

The Windows desktop application remains in `Khaos-Krew/Khaos-Nexus`. Mobile extends the shared platform; it does not replace the desktop application or duplicate its authority.

## Current implementation

The first installable Android preview is implemented through issue #9 and PR #10.

It includes:

- a native Android application shell;
- the Khaos Nexus black, charcoal, onyx, ruby, and crimson visual system;
- Home, D&D, Servers, Nexus AI, Notifications, and Settings destinations;
- local fixture data for device and usability testing;
- visibly locked privileged actions;
- a built-in signed Update Center for direct preview builds;
- unit tests and Android lint;
- package, permission, signing, version, and minimum-SDK verification;
- SHA-256 generation;
- an installable GitHub Actions APK artifact.

The application still contains no production endpoint, desktop credential, Discord bot token, RCON password, hosting-provider credential, database service-role key, AI token, scheduler, or platform administration authority.

The approved desktop UI dependency is Khaos Nexus PR #194, merged at commit `0971f81b3264ce66a106a0ace129596e31c5ef62`.

## Android toolchain

- Android Gradle Plugin 9.3.0
- Gradle 9.5.0
- JDK 17
- compile and target API 36
- minimum API 26 / Android 8.0
- Java 17 source
- standard Android platform widgets and Canvas rendering
- JUnit 4 tests

## Build locally

Prerequisites:

- JDK 17
- Android SDK platform 36
- Android build tools 36.0.0
- Gradle 9.5.0

Run:

```bash
gradle --no-daemon --stacktrace clean testDebugUnitTest lintDebug assembleDebug
```

The APK is created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The direct preview package is:

```text
com.khaoskrew.nexuscompanion.preview
```

## Built-in Update Center

The direct preview opens through an Update Center that can check a configured signed HTTPS channel before entering the companion.

Metadata checks may happen automatically. APK download and installation never happen automatically. The user must approve:

1. download and local verification;
2. Android's unknown-source setting when required;
3. Android's final package installation confirmation.

Before the installer opens, the app verifies:

- HTTPS and allowed hosts;
- manifest schema and channel;
- exact package name and newer version code;
- RSA/SHA-256 manifest signature;
- signed file size and APK SHA-256;
- APK package identity and version;
- replacement APK signing certificate matches the installed app.

The `debug` manifest alone includes `INTERNET` and `REQUEST_INSTALL_PACKAGES`. A future Google Play build must use Google Play's in-app update mechanism and must not merge the direct-preview package-install permission.

See [`docs/IN_APP_UPDATES.md`](docs/IN_APP_UPDATES.md) for the manifest contract, signing process, CI variables, stable preview keystore secrets, and rollout controls.

## Stable preview signing

Android only permits an APK to replace an installed app when both use the same signing certificate.

The workflow supports a stable preview keystore through these repository secrets:

- `PREVIEW_KEYSTORE_BASE64`
- `PREVIEW_KEYSTORE_PASSWORD`
- `PREVIEW_KEY_ALIAS`
- `PREVIEW_KEY_PASSWORD`

When those secrets are absent, CI falls back to an ephemeral Android debug key and records that state in the artifact's `signing-mode.txt`. Ephemeral clean-runner builds are installable but cannot reliably replace one another in place.

The update metadata channel uses separate repository variables:

- `UPDATE_MANIFEST_URL`
- `UPDATE_ALLOWED_HOSTS`
- `UPDATE_PUBLIC_KEY_B64`

When no approved manifest URL or public key is configured, the Update Center remains installed but opens the companion without downloading anything.

## APK boundary

The Owner authorized an installable APK and built-in update capability for controlled testing on August 4, 2026.

This does not authorize:

- a GitHub tag or Release;
- Play Store, Firebase, enterprise, or website distribution;
- production or staging deployment;
- a production signing key;
- live remote administration;
- silent or background installation;
- release-channel publication without separate approval.

GitHub Actions artifacts are temporary and authenticated, so they are not used as the application update channel.

## Product purpose

The companion will provide secure, focused access to selected Khaos Nexus capabilities away from the Windows desktop:

- monitoring and notifications;
- lightweight review and approval;
- D&D table support;
- read-only server health;
- explicitly authorized guarded actions;
- Nexus AI health and advisory review.

The desktop application remains the primary administration surface for credentials, complex setup, service supervision, scheduler authoring, release management, and high-risk operations.

## Architecture boundaries

### Mobile responsibilities

- Render mobile-optimized views.
- Authenticate through an approved system-browser PKCE flow.
- Maintain a revocable device session in platform secure storage.
- Request bounded, role-filtered projections.
- Cache only explicitly approved non-secret data.
- Submit actions and approvals through authoritative backend APIs.
- Display action status, audit references, conflicts, expiry, and retryable failures.
- Check and install only cryptographically verified updates after explicit user approval.

### Shared platform responsibilities

Khaos Nexus desktop and backend services continue to own:

- protected credentials;
- Discord interactions and registered bots;
- shared scheduler execution;
- game-server adapters and commands;
- AI runtimes and provider access;
- campaign authority and permission checks;
- audit and notification routing;
- production release publication.

### Prohibited mobile behavior

- Direct RCON, hosting-provider, Discord, database service-role, or AI-sidecar connections.
- Embedded desktop credentials, bot tokens, server passwords, provider keys, or AI tokens.
- A second scheduler, AI router, Discord bot, notification engine, or permission model.
- Background autonomous administration.
- Silent execution of maintenance, AI proposals, or application installation.

## Planned phases

1. **Android preview foundation** — installable shell and signed Update Center.
2. **Authentication and device sessions** — PKCE, secure storage, registration, capabilities, and revocation.
3. **Home, notifications, and deep links** — shared notification registration and safe detail retrieval.
4. **D&D read-only and offline** — player-safe projections and encrypted bounded cache.
5. **Server status and guarded actions** — server-side authorization, confirmation, idempotency, expiry, and audit.
6. **Nexus AI review** — health, findings, and advisory plan review only.
7. **Hardening** — accessibility, observability, security, performance, and real-device coverage.
8. **Release preparation** — only after separate explicit Owner authorization.

## Repository workflow

- All mobile branches and pull requests target this repository's `main` branch.
- Mobile-only code must not be added to `Khaos-Krew/Khaos-Nexus`.
- Production Control assigns an issue, exact starting commit, branch, dependencies, and release boundary.
- Pull requests remain focused on one vertical slice and include tests and documentation.
- Missing shared APIs are tracked with the appropriate desktop/backend owner, while mobile implementation remains here.

## Planning issues

- #1 Production roadmap
- #2 Architecture and toolchain
- #3 Authentication and device sessions
- #4 Navigation, Home, notifications, and deep links
- #5 D&D companion and offline support
- #6 Server monitoring and guarded actions
- #7 Nexus AI advisory workflows
- #8 Security, accessibility, observability, and preview gates
- #9 First installable Android APK

## Release policy

No tag, GitHub Release, Play Store artifact, deployment, or release channel may be created without separate explicit Owner authorization. The current APK is a controlled-testing artifact, not a production release.

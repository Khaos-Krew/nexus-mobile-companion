# Khaos Nexus Mobile Companion

Khaos Nexus Mobile Companion is the dedicated Android client for the Khaos Nexus platform. This repository is the exclusive home for mobile-only code, CI, planning, pull requests, and future mobile releases.

The Windows desktop application remains in `Khaos-Krew/Khaos-Nexus`. Mobile extends the shared platform; it does not replace the desktop application or duplicate its authority.

## Current implementation

The first installable Android preview is implemented through issue #9 and PR #10.

It includes:

- a native Android application shell;
- the Khaos Nexus black, charcoal, onyx, ruby, and crimson visual system;
- Home, D&D, Servers, Nexus AI, Notifications, and Settings destinations;
- original launcher and background graphics;
- local fixture data for device, layout, and usability testing;
- visibly locked privileged actions;
- unit tests and Android lint;
- package and minimum-SDK verification;
- SHA-256 generation;
- an installable GitHub Actions debug APK artifact.

The preview is deliberately offline. It requests no Android network permission and contains no production endpoint, desktop credential, Discord bot token, RCON password, hosting-provider credential, database service-role key, AI token, scheduler, updater, or release signing key.

The approved desktop UI dependency is Khaos Nexus PR #194, merged at commit `0971f81b3264ce66a106a0ace129596e31c5ef62`.

## APK boundary

The Owner authorized creation of an installable APK on August 4, 2026. This authorization covers a debug-signed APK for controlled direct testing.

It does not authorize:

- a GitHub tag or Release;
- Play Store, Firebase, enterprise, or website distribution;
- production or staging deployment;
- a production signing key;
- live remote administration;
- mobile updater or release-channel publication.

Android may require the tester to allow installation from the application used to open the APK. The preview package is separate from any future production package and can be removed normally through Android settings.

## Android toolchain

The preview uses a dependency-light native Android stack:

- Android Gradle Plugin 9.3.0;
- Gradle 9.5.0;
- JDK 17;
- Android compile and target API 36;
- minimum API 26 / Android 8.0;
- Java 17 source;
- standard Android platform widgets and Canvas rendering;
- JUnit 4 tests.

API 36 is used because it is available from the stable Android SDK channel on the clean GitHub Actions runner. See `docs/ADR-0001-native-android-preview.md` for the architecture and security decision.

## Build locally

Prerequisites:

- JDK 17;
- Android SDK platform 36;
- Android build tools 36.0.0;
- Gradle 9.5.0.

Run:

```bash
gradle --no-daemon --stacktrace clean testDebugUnitTest lintDebug assembleDebug
```

The local APK is created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The debug package identifier is:

```text
com.khaoskrew.nexuscompanion.preview
```

## GitHub Actions APK

Pull requests to `main` run `.github/workflows/android-preview-apk.yml`.

The workflow:

1. installs JDK 17, Android API 36, build tools 36.0.0, and Gradle 9.5.0;
2. runs unit tests and Android lint;
3. assembles the debug APK;
4. verifies package identity and minimum SDK with `aapt`;
5. generates `SHA256SUMS.txt`;
6. uploads `Khaos-Nexus-Mobile-Companion-APK` as a temporary workflow artifact.

The workflow does not create a tag, GitHub Release, deployment, or store upload.

## Product purpose

The companion will provide secure, focused access to selected Khaos Nexus capabilities away from the Windows desktop:

- monitoring and notifications;
- lightweight review and approval;
- D&D table support;
- read-only server health;
- explicitly authorized guarded actions;
- Nexus AI health and advisory review.

The desktop application remains the primary administration surface for credentials, complex setup, service supervision, scheduler authoring, release management, and high-risk operations.

## Navigation

### Home

Health summaries, pending approvals, upcoming activity, security state, and quick destinations.

### D&D

Campaigns, characters, sessions, encounters, maps, dice, notes, and reviewed AI-assisted proposals. The preview displays local fixtures only.

### Servers

Read-only previews for ARK, Palworld, Minecraft, and future approved modules. Mobile will never connect directly to RCON or hosting providers.

### Nexus AI

Separate D&D AI and Nexus AI Core health and review surfaces. The mobile app will not host an AI runtime or execute AI-generated maintenance automatically.

### Notifications

A safe actionable inbox. Future push payloads must be minimal and non-secret; authorized details are fetched only after authentication.

### Settings

Account, device sessions, notifications, privacy, security, diagnostics, accessibility, and build information.

When connectivity is implemented, navigation visibility will be derived from authenticated capabilities rather than hardcoded privilege assumptions.

## Architecture boundaries

### Mobile responsibilities

- Render mobile-optimized views.
- Authenticate through an approved system-browser PKCE flow.
- Maintain a revocable device session in platform secure storage.
- Request bounded, role-filtered projections.
- Cache only explicitly approved non-secret data.
- Submit actions and approvals through authoritative backend APIs.
- Display action status, audit references, conflicts, expiry, and retryable failures.

### Shared platform responsibilities

Khaos Nexus desktop and backend services continue to own:

- protected credentials;
- Discord interactions and registered bots;
- shared scheduler execution;
- game-server adapters and commands;
- AI runtimes and provider access;
- campaign authority and permission checks;
- audit and notification routing;
- updater and release publication.

### Prohibited mobile behavior

- Direct RCON, hosting-provider, Discord, database service-role, or AI-sidecar connections.
- Embedded desktop credentials, bot tokens, server passwords, provider keys, or AI tokens.
- A second scheduler, AI router, Discord bot, notification engine, updater, or permission model.
- Background autonomous administration.
- Silent execution of maintenance or AI proposals.

## Offline behavior

The current preview is fully offline. Future offline support remains intentionally limited:

- encrypted and size-bounded cache;
- read-only by default;
- visible stale timestamps and connection state;
- no secrets, raw audit payloads, protected GM data, or hidden Discord data;
- server revisions for conflict handling;
- explicit cache clearing and device revocation.

## Planned modules and phases

1. **Android preview foundation** — installable offline shell, completed by PR #10.
2. **Authentication and device sessions** — PKCE, secure storage, registration, capabilities, and revocation.
3. **Home, notifications, and deep links** — shared notification registration and safe detail retrieval.
4. **D&D read-only and offline** — player-safe projections and encrypted bounded cache.
5. **Server status and guarded actions** — server-side authorization, confirmation, idempotency, expiry, and audit.
6. **Nexus AI review** — health, findings, and advisory plan review only.
7. **Hardening** — accessibility, observability, security, performance, and real-device coverage.
8. **Release preparation** — only after separate explicit Owner authorization.

## Testing and release gates

Before any broader preview or production release:

- exact source and dependency baselines are recorded;
- unit, contract, integration, UI, lint, accessibility, and security checks pass;
- no protected credential enters the bundle, logs, diagnostics, notifications, analytics, or backups;
- authentication, revocation, offline deletion, deep links, push behavior, upgrades, and failure recovery pass real-device testing;
- shared API compatibility is verified against an approved Khaos Nexus baseline;
- signing, release notes, hashes, rollback, and distribution channel receive explicit approval.

A merged branch or successful APK workflow artifact is not a public release.

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

No tag, GitHub Release, Play Store artifact, deployment, or release channel may be created without separate explicit Owner authorization. The current APK is a debug-signed controlled-testing artifact, not a production release.

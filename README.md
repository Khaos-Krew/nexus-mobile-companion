# Khaos Nexus Mobile Companion

Khaos Nexus Mobile Companion is the dedicated Android client for the Khaos Nexus platform. This repository is the exclusive home for mobile-only code, planning, CI, and future mobile releases.

The Windows desktop application remains in [`Khaos-Krew/Khaos-Nexus`](https://github.com/Khaos-Krew/Khaos-Nexus). Mobile extends that platform; it does not replace the desktop application or duplicate its authority.

## Current status

**An installable offline Android preview is under active implementation in issue #9 and branch `foundation/android-preview-apk`.**

The first APK provides:

- a native Android application shell;
- the Khaos Nexus black, charcoal, onyx, ruby, and crimson visual identity;
- Home, D&D, Servers, Nexus AI, Notifications, and Settings navigation;
- local fixture data for device and usability testing;
- visibly locked privileged actions;
- unit tests, Android lint, APK identity verification, and SHA-256 generation;
- workflow-artifact-only APK distribution.

It intentionally does **not** connect to production services. The manifest requests no network permission and the source contains no production endpoint, desktop credential, Discord bot token, RCON password, provider key, AI token, scheduler, or signing key.

The required desktop UI baseline was completed in Khaos Nexus PR #194 at merge commit `0971f81b3264ce66a106a0ace129596e31c5ef62`.

## Installable preview boundary

The Owner authorized creation of an installable APK on August 4, 2026. This authorization covers a GitHub Actions debug artifact for direct testing only.

It does not authorize:

- a GitHub Release or tag;
- Play Store, Firebase, enterprise, or website distribution;
- production or staging deployment;
- a release signing key;
- live remote administration;
- updater or release-channel publication.

The debug APK uses the standard Android debug signature. Android may ask the tester to allow installation from the app used to open the APK.

## Android toolchain

The first preview uses a dependency-light native Android stack:

- Android Gradle Plugin 9.3.0;
- Gradle 9.5.0;
- JDK 17;
- compile and target API 37;
- minimum API 26 / Android 8.0;
- Java 17 source;
- standard Android platform widgets and Canvas graphics;
- JUnit 4 tests.

See [`docs/ADR-0001-native-android-preview.md`](docs/ADR-0001-native-android-preview.md) for the decision, rejected alternatives, and security contract.

## Build locally

Prerequisites:

- JDK 17;
- Android SDK platform 37;
- Android build tools 36.0.0;
- Gradle 9.5.0.

Run:

```bash
gradle --no-daemon clean testDebugUnitTest lintDebug assembleDebug
```

The APK is created at:

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

1. installs JDK 17, Android API 37, build tools 36.0.0, and Gradle 9.5.0;
2. runs unit tests and Android lint;
3. assembles the debug APK;
4. verifies package identity and minimum SDK with `aapt`;
5. generates `SHA256SUMS.txt`;
6. uploads `Khaos-Nexus-Mobile-Companion-APK` as a temporary GitHub Actions artifact.

No release is created by the workflow.

## Product purpose

The companion provides secure, focused access to selected Khaos Nexus capabilities away from the Windows desktop:

- monitoring and notifications;
- lightweight review and approval;
- D&D table support;
- read-only server health;
- explicitly authorized guarded actions;
- Nexus AI health and advisory review.

The desktop application remains the primary administration surface for credentials, complex setup, service supervision, release management, scheduler authoring, and high-risk operations.

## Navigation

### Home

Health summaries, pending approvals, upcoming activity, security status, and quick links.

### D&D

Campaigns, characters, sessions, encounters, maps, dice, notes, and reviewed AI-assisted proposals. The preview uses local fixtures only.

### Servers

Read-only status and future guarded command workflows for ARK, Palworld, Minecraft, and other approved modules. Mobile never connects to RCON or providers directly.

### Nexus AI

Separate D&D AI and Nexus AI Core health and review surfaces. The mobile app does not host either AI runtime and cannot execute AI-generated maintenance automatically.

### Notifications

A safe actionable inbox. Future push payloads must be minimal and non-secret; authorized details are fetched only after authentication.

### Settings

Account, devices, notification preferences, privacy, security, diagnostics, accessibility, and build information.

Navigation visibility will become capability-driven when authentication and the shared mobile API are implemented.

## Architecture boundaries

### Mobile responsibilities

- Render mobile-optimized views.
- Authenticate through an approved system-browser PKCE flow.
- Maintain a revocable device session in platform secure storage.
- Request bounded, role-filtered projections.
- Cache only explicitly approved non-secret data.
- Submit commands and approvals through authoritative backend APIs.
- Show action state, audit references, conflicts, expiry, and retryable failures.

### Shared platform responsibilities

Khaos Nexus desktop and backend services continue to own:

- protected credentials;
- Discord interactions and registered bots;
- scheduler execution;
- game-server adapters and commands;
- AI runtimes and provider access;
- campaign authority and permission checks;
- audit and notification routing;
- updater and release publication.

### Prohibited mobile behavior

- Direct RCON, hosting-provider, Discord, Supabase service-role, or AI-sidecar connections.
- Embedded desktop credentials, bot tokens, server passwords, provider keys, or AI tokens.
- A second scheduler, AI router, Discord bot, notification engine, updater, or permission model.
- Background autonomous administration.
- Silent execution of maintenance or AI proposals.

## Offline behavior

The preview is fully offline. Later offline support remains intentionally limited:

- encrypted and size-bounded cache;
- read-only by default;
- stale timestamps and clear connection state;
- no secrets, raw audit payloads, protected GM data, or hidden Discord data;
- server revisions for conflict handling;
- explicit cache clearing and device revocation.

## Planned implementation sequence

1. **Android preview foundation** — current issue #9.
2. **Authentication and device sessions** — PKCE, secure storage, registration, capability bootstrap, revocation.
3. **Home, notifications, and deep links** — shared notification registration and safe detail fetch.
4. **D&D read-only and offline** — player-safe projections and encrypted bounded cache.
5. **Server status and guarded actions** — server-side authorization, confirmation, idempotency, expiry, and audit.
6. **Nexus AI review** — health, findings, and advisory plan review only.
7. **Hardening** — accessibility, observability, security, performance, and real-device coverage.
8. **Release preparation** — only after separate explicit Owner authorization.

## Testing and release gates

Before any distributed preview or production release:

- exact source commit and dependency baselines are recorded;
- unit, contract, integration, UI, lint, accessibility, and security checks pass;
- no protected credential enters the bundle, logs, diagnostics, notifications, analytics, or backups;
- authentication, revocation, offline deletion, deep links, push behavior, upgrades, and failure recovery pass real-device testing;
- shared API compatibility is verified against an approved Khaos Nexus baseline;
- signing, release notes, hashes, rollback, and distribution channel receive explicit approval.

A merged feature branch or successful APK artifact is not a public release.

## Repository workflow

- All mobile branches and pull requests target this repository's `main` branch.
- Mobile-only code must not be added to `Khaos-Krew/Khaos-Nexus`.
- Production Control assigns an issue, exact starting commit, branch, dependencies, and release boundary.
- Pull requests remain focused on one vertical slice and include tests and documentation.
- Missing shared APIs are tracked in the desktop/backend repository, while mobile implementation stays here.

## Related planning issues

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

No tag, GitHub Release, Play Store artifact, deployment, or release channel may be created without separate explicit Owner authorization. The current workflow artifact is an installable debug APK for controlled testing, not a production release.

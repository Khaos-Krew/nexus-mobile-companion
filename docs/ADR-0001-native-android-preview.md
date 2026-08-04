# ADR-0001: Native Android preview foundation

- **Status:** Accepted for the first installable preview
- **Date:** 2026-08-04
- **Issue:** #9
- **Desktop baseline:** `Khaos-Krew/Khaos-Nexus` PR #194 merge commit `0971f81b3264ce66a106a0ace129596e31c5ef62`

## Decision

Build the first Khaos Nexus Mobile Companion APK as a dependency-light native Android application using:

- Android Gradle Plugin 9.3.0;
- Gradle 9.5.0;
- JDK 17;
- Android compile/target API 36;
- Android minimum API 26;
- Java 17 source;
- standard Android platform widgets and Canvas rendering;
- JUnit 4 tests;
- GitHub Actions for reproducible debug APK generation.

API 36 is used because it is available from the stable Android SDK channel on the clean GitHub Actions runner. This avoids depending on a preview SDK package while retaining Android 8.0+ device compatibility.

The first APK is an offline-safe user-interface and device-validation foundation. It does not request Android network permission and contains no live backend endpoint, credential, privileged action implementation, scheduler, Discord client, RCON client, hosting-provider client, database service-role client, or AI runtime.

## Why native Android first

- Produces an installable APK without an Expo/EAS account, Node.js toolchain, hosted build service, or cross-platform runtime.
- Keeps the dependency graph and attack surface small for the first device preview.
- Gives direct control over Android backup policy, cleartext restrictions, package identity, minimum SDK, accessibility, and artifact verification.
- Allows the Khaos Nexus visual system to be implemented with local resources and deterministic Canvas rendering.
- Establishes a reliable Android baseline before deciding whether iOS or Kotlin Multiplatform is justified.

## Rejected for this slice

### Expo / React Native

Rejected for the first APK because it adds Node/package-manager dependencies and normally introduces additional cloud-signing or native-generation workflow decisions. It may be reconsidered only through a later architecture decision.

### Flutter

Rejected for the first APK because it adds a larger runtime/toolchain and duplicates platform abstractions before mobile contracts are stable. It may be reconsidered if iOS parity becomes an approved near-term requirement.

### WebView wrapper

Rejected because a wrapped website would not provide the required secure mobile-session, offline, push, deep-link, accessibility, and device-management foundation.

## Security and authority boundaries

The preview must remain true to all of the following:

- No production or staging endpoint is compiled into the APK.
- No `INTERNET` permission is requested.
- Android cloud backup and device transfer are disabled for app data.
- Cleartext network traffic is disabled.
- No signing key is stored in the repository.
- The GitHub Actions artifact uses the standard Android debug signature and is for direct Owner testing only.
- Every future live request must use a versioned mobile-facing API and server-side authorization.
- Mobile never directly owns Discord, RCON, providers, scheduler execution, AI runtime, campaign authority, updater, or release publication.

## UI scope

The preview contains the approved primary destinations:

1. Home
2. D&D
3. Servers
4. Nexus AI
5. Notifications
6. Settings

All content is local fixture data. Privileged controls are visibly locked and explain the required future authentication/API gates.

## Build and artifact contract

Pull requests to `main` run:

```text
gradle clean testDebugUnitTest lintDebug assembleDebug
```

CI then verifies:

- package name `com.khaoskrew.nexuscompanion.preview`;
- minimum SDK 26;
- successful lint and unit tests;
- installable debug APK output;
- SHA-256 checksum;
- workflow-artifact-only distribution.

No tag, GitHub Release, Play Store upload, deployment, or mobile release channel is authorized by this ADR.

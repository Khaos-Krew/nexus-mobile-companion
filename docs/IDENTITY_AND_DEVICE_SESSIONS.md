# Mobile identity and device-session boundary

## Status

Phase 2 is active on issue #3. This document defines the Android client boundary before live Khaos Nexus identity endpoints are connected.

The first vertical slice is contract-first and mock-only. It does not add a production API URL, client secret, Discord token, service-role credential, RCON connection, scheduler, AI runtime, or privileged command path.

## Trust model

The mobile app is a public OAuth client. It cannot safely hold a client secret.

Authentication uses:

1. authorization code through the system browser;
2. PKCE `S256` with a cryptographically random verifier;
3. a cryptographically random one-time `state` value;
4. an exact registered app redirect URI;
5. immediate server-side code exchange;
6. short-lived access tokens;
7. rotating refresh tokens bound to a registered device session;
8. server-issued capability bootstrap after every login and refresh.

The app never derives roles or protected navigation from local assumptions.

## Authorization attempt

`AuthorizationAttempt` contains the PKCE verifier, challenge, OAuth state, creation time, and one-time consumed marker.

- The verifier is 64 random bytes encoded as unpadded Base64 URL text.
- The challenge is `BASE64URL(SHA256(verifier))`.
- The state is 32 random bytes encoded as unpadded Base64 URL text.
- Callback state comparison is constant-time.
- A callback can consume an attempt only once.
- Expired, mismatched, replayed, malformed, or wrong-redirect callbacks fail closed.
- `toString()` methods never contain the verifier, state, authorization code, tokens, device ID, or subject ID.

Authorization attempts are process-local in this first slice. A later UI integration must persist only the minimum encrypted transaction state required to survive process recreation, and must delete it after use or expiry.

## Session states

The local session state machine supports:

- `SIGNED_OUT`
- `AUTHORIZING`
- `ACTIVE`
- `REFRESHING`
- `REVOKED`
- `EXPIRED`
- `INCOMPATIBLE`
- `RECOVERABLE_ERROR`

Protected capabilities are available only while `ACTIVE`.

A restored encrypted token bundle enters `REFRESHING`, not `ACTIVE`. The shared backend must rotate the refresh token and return a fresh compatible capability bootstrap before protected navigation can appear.

Refresh replay, revoked devices, expired tokens, capability incompatibility, secure-storage failure, and callback validation failure remove local authority immediately.

## Encrypted storage

`AndroidKeystoreSessionStore` uses:

- a non-exportable Android Keystore AES-256 key;
- AES/GCM/NoPadding;
- a new randomized IV for every save;
- fixed authenticated additional data identifying the session format;
- private SharedPreferences containing only ciphertext, IV, and format version;
- app-level backup disabled by the manifest and extraction rules.

If the ciphertext, IV, format, key, authentication tag, JSON payload, or token contract cannot be validated, the encrypted record is cleared and the app remains signed out.

The current storage format includes access token, rotating refresh token, token expiries, device ID, subject ID, and rotation counter. None may be copied to logs, analytics, notifications, crash breadcrumbs, screenshots, clipboard, or plaintext preferences.

## Capability bootstrap

`CapabilitySet` is an immutable projection from the authoritative backend.

Public destinations:

- Home
- Account
- Settings

Protected destination capabilities:

| Destination | Required capability |
| --- | --- |
| D&D | `dnd.read` |
| Servers | `servers.read` |
| Nexus AI | `nexus-ai.read` |
| Alerts | `notifications.read` |

Device management and guarded operations use separate capabilities:

- `devices.read`
- `devices.revoke`
- `guarded-actions.request`

Unknown destinations, absent capabilities, incompatible contracts, and non-active sessions are denied.

The read-only local fixture never grants device revocation or guarded-action authority.

## Required backend contracts

Khaos Nexus must expose reviewed mobile-safe endpoints or a mobile BFF for:

### Authorization metadata

- authorization endpoint
- token endpoint
- supported PKCE method (`S256` only)
- registered mobile client ID
- registered callback URI
- supported scopes
- supported mobile contract versions

### Code exchange

Request must include:

- authorization code
- PKCE verifier
- redirect URI
- mobile client ID
- app version and mobile contract version
- device installation identifier or attestation reference as approved by architecture

Response must include:

- short-lived access token
- rotating refresh token
- access and refresh expiry timestamps
- device-session ID
- stable account subject reference
- capability bootstrap
- server time or signed time reference
- security/audit correlation reference

### Refresh rotation

Every successful refresh must invalidate the prior refresh token. Reuse of an older rotation must revoke or quarantine the device session according to backend security policy.

The response must include a strictly increasing rotation counter or equivalent replay-safe server contract.

### Device endpoints

- register or rename current device
- list current account devices
- revoke another device
- revoke current device
- terminate all sessions
- security-event projection

Every response is role-filtered, audited, versioned, and reauthorized server-side.

### Capability bootstrap

The bootstrap must include:

- contract version
- global roles
- campaign roles scoped by campaign ID
- module feature gates
- destination/read capabilities
- guarded-action request capabilities
- minimum supported app version
- server time and cache expiry

The client consumes explicit capability names; it does not reproduce backend role logic.

## Network requirements

- Certificate-valid HTTPS only outside local development.
- No embedded credentials in URLs.
- No redirects to unapproved hosts.
- No token in query strings.
- Bounded request and response sizes.
- Timeouts and cancellation.
- No automatic retry of authorization-code exchange or non-idempotent operations.
- Refresh operations require single-flight coordination to avoid token-rotation races.
- Protected requests are rejected while refresh is in progress.

Certificate pinning is deferred until the production API domain, certificate rotation process, and emergency recovery policy are approved. Incorrect pinning can create an unrecoverable outage.

## Redacted diagnostics

`RedactedDiagnostics` removes common OAuth fields, bearer credentials, JWT-like values, authorization codes, state values, device IDs, and subject IDs.

Production diagnostics may contain only:

- safe error code
- session state
- capability count
- contract version
- app version
- network category without URL query or body
- audit correlation reference approved for client visibility

Raw exceptions from identity providers, JSON payloads, crypto providers, and HTTP clients must not be logged directly.

## UI integration gates

Before the Account screen launches a real browser flow:

- the authorization and token endpoint contract is reviewed;
- the mobile client ID and callback URI are registered;
- Android app-link or custom-scheme ownership is approved;
- callback activity is non-exported except for its narrow intent filter;
- code exchange is implemented against the shared service;
- secure storage and process-death handling pass device tests;
- capability bootstrap controls the navigation model;
- task-switcher and screenshot policy is defined for protected screens;
- sign-out and local wipe are always available.

## Testing requirements

Current unit coverage includes:

- RFC 7636 PKCE vector;
- verifier generation constraints;
- OAuth state mismatch and replay;
- redirect mismatch;
- attempt expiry;
- provider-error redaction;
- capability denial and incompatible contracts;
- restored-session refresh-only behavior;
- refresh rotation replay;
- token expiry and revocation;
- secure-store failure;
- token and identifier log redaction.

Still required before live identity completion:

- Android Keystore instrumentation tests;
- process-death and device-reboot restoration;
- biometric or step-up policy tests if later approved;
- system-browser and callback instrumentation;
- backend contract tests;
- refresh single-flight integration tests;
- device revocation end-to-end tests;
- account-switch and all-session termination tests;
- real-device validation across Android 8.0, current Android, gesture navigation, and three-button navigation.

## Release boundary

This identity phase does not authorize a release tag, GitHub Release, public APK channel update, store upload, deployment, production endpoint, or live privileged action. Those remain separate Owner decisions.

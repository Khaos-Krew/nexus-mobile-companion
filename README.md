# Khaos Nexus Mobile Companion

Khaos Nexus Mobile Companion is the dedicated mobile client for the Khaos Nexus platform. This repository is the exclusive home for mobile-only planning and future mobile implementation.

The Windows desktop application remains in [`Khaos-Krew/Khaos-Nexus`](https://github.com/Khaos-Krew/Khaos-Nexus). The mobile companion extends that platform; it does not replace the desktop application or duplicate its services.

## Current status

**Planning only. No mobile implementation, release build, deployment, or published mobile artifact exists yet.**

The desktop UI baseline required before mobile planning was completed and merged in Khaos Nexus PR #194 at merge commit `0971f81b3264ce66a106a0ace129596e31c5ef62`.

All future mobile branches and pull requests must be created in this repository and target this repository's `main` branch. Mobile-only code must not be added to `Khaos-Krew/Khaos-Nexus`.

## Product purpose

The mobile companion provides secure, focused access to selected Khaos Nexus capabilities when the user is away from the Windows desktop. It is intended for monitoring, notifications, lightweight review and approval, D&D table support, and explicitly authorized remote actions.

The desktop application remains the primary administration and production-control surface for initial configuration, protected credentials, high-risk operations, service supervision, release management, and complex module editing.

## Relationship to Khaos Nexus desktop

The mobile app consumes approved shared contracts and services owned by the Khaos Nexus platform:

- shared backend and normalized API contracts;
- existing authentication and account linking;
- global roles, campaign roles, module gates, and authorization policies;
- existing audit records and notification routing;
- existing Discord Bot, D&D, game-server, scheduler, and AI service authorities;
- shared module identifiers, capability negotiation, and safe projections.

The mobile app must not introduce a second scheduler, AI runtime, Discord bot/router, game-server adapter, updater, credential store, campaign database, or permission model.

## Architecture boundaries

### Mobile responsibilities

- Render mobile-optimized navigation and module views.
- Authenticate the user and maintain a revocable mobile session.
- Request bounded, role-filtered data from shared services.
- Cache only explicitly approved, non-secret data for offline use.
- Register a mobile device and push-notification token.
- Submit explicit commands or approvals through authoritative backend APIs.
- Show action status, audit references, conflicts, and retryable failures.

### Desktop and shared-service responsibilities

- Own protected provider credentials, Discord bot tokens, server passwords, AI service tokens, and updater credentials.
- Own scheduler execution, AI processes, Discord interactions, game-server commands, module configuration, and authoritative state mutation.
- Enforce permissions and revalidate every mobile request server-side.
- Produce audit events and notification events.
- Apply rate limits, idempotency, conflict detection, and action confirmation rules.

### Prohibited mobile behavior

- Direct RCON, provider, Discord, Supabase service-role, or AI sidecar connections from the mobile client.
- Storing desktop credentials, bot tokens, server passwords, provider keys, or AI service tokens.
- Background autonomous administration.
- Executing maintenance proposals without explicit authorization.
- Creating a mobile-owned scheduler or duplicated notification engine.
- Publishing release builds without separate Owner authorization.

## Planned navigation

The planned shell follows the completed desktop information architecture while remaining mobile-first:

1. **Home** — alerts, health summaries, pending approvals, upcoming events, and recent activity.
2. **D&D** — campaigns, characters, sessions, encounters, maps, dice, notes, and approved AI-assisted proposals.
3. **Servers** — status, players, scheduled operations, warnings, and tightly controlled actions.
4. **Nexus AI** — service health, monitor findings, advisory proposals, and review-only workflows.
5. **Community** — Discord-linked announcements, tickets, events, and approved community tools.
6. **Notifications** — actionable inbox with deep links and audit context.
7. **Settings** — account, devices, notification preferences, security, diagnostics, and app information.

Navigation visibility must be capability-driven. Hidden or disabled modules must not appear as usable destinations.

## Supported and planned modules

### Initial supported scope

- Account sign-in, device registration, session management, and role-aware navigation.
- Home dashboard and notification inbox.
- Read-only platform, Discord, and game-server health.
- D&D campaign list, campaign overview, characters, upcoming sessions, session notes, dice history, and player-safe map views.
- Explicitly authorized low-risk actions with confirmation, idempotency, audit evidence, and server-side revalidation.
- Offline access to selected recent D&D and notification data.

### Later phases

- Encounter participation and turn-aware D&D controls.
- Push-driven server warnings and status changes.
- Owner/admin approval workflows for guarded server operations.
- Nexus AI monitor findings and advisory plan review.
- Community events, tickets, polls, and announcements.
- Module-specific mobile surfaces for ARK, Palworld, Minecraft, Warframe, and IdleOn where authoritative shared APIs exist.

### Desktop-only by default

- Initial bot and provider credential setup.
- Full server/module configuration.
- Scheduler creation and complex automation editing.
- AI provider and sidecar configuration.
- Release, updater, rollback, and publication controls.
- High-risk bulk moderation or destructive operations.

Any later mobile exposure of a desktop-only capability requires a separate security review and explicit Owner approval.

## Authentication and security

- Use the existing Khaos Nexus account and Discord-linked identity model.
- Prefer authorization-code flow with PKCE and system-browser authentication.
- Store refresh credentials only in platform secure storage; never in plaintext preferences, logs, analytics, crash reports, backups, or source control.
- Use short-lived access tokens, refresh-token rotation, device revocation, and bounded session lifetimes.
- Require step-up confirmation for guarded actions.
- Revalidate roles, campaign membership, module gates, channel authorization, and action scope on every request.
- Use certificate-valid HTTPS only outside local development.
- Redact identifiers and protected values from logs and diagnostics.
- Support remote session revocation from desktop/account administration.

## API boundary

The mobile client uses a versioned mobile-facing API or backend-for-frontend contract built over existing platform services.

Required properties:

- normalized, least-privilege response models;
- no direct database table access from untrusted client code unless protected by reviewed row-level security and the same normalized contract;
- cursor pagination and bounded payload sizes;
- idempotency keys for commands;
- optimistic-concurrency tokens or entity revisions for edits;
- explicit action state: accepted, queued, running, succeeded, failed, cancelled, or expired;
- structured permission and capability errors;
- audit-event identifiers returned for mutations;
- stable deep-link identifiers that contain no secrets;
- compatibility negotiation between mobile and desktop/backend versions.

## Offline behavior

Offline mode is intentionally limited:

- Cache an encrypted, size-bounded subset of recent user-approved data.
- Default offline content to read-only.
- Never cache secrets, private GM data without explicit authorization, server credentials, service tokens, raw audit payloads, or hidden Discord data.
- Queue only operations explicitly declared offline-safe.
- Show stale timestamps and connection state clearly.
- Resolve conflicts using server revisions; never silently overwrite authoritative state.
- Allow the user to clear cached data and revoke the device.

## Push notifications

Push delivery is an output of the shared Khaos Nexus notification system, not a new mobile scheduler.

Planned notification classes:

- server offline/online and restart warnings;
- failed scheduled operations;
- D&D session reminders and encounter-turn prompts;
- moderation or ticket assignments;
- Nexus AI monitor findings requiring review;
- security events, device sign-in, and session revocation.

Every push payload must be minimal, non-secret, and safe for lock-screen display. The app fetches authorized detail after opening. Notification preferences and quiet hours are stored through shared account services.

## Development workflow

1. Production Control assigns one GitHub issue, exact starting commit, branch, pull request target, dependencies, and release boundary.
2. Each implementation branch starts from the latest approved `main` commit in this repository.
3. Branch naming should use scoped prefixes such as `foundation/`, `feature/`, `integration/`, `test/`, or `docs/`.
4. Open a draft pull request before material implementation.
5. Keep each pull request focused on one vertical slice.
6. Document shared-contract dependencies on `Khaos-Krew/Khaos-Nexus` without placing mobile code there.
7. Require tests, security review, accessibility review, and release validation before merge.
8. Squash or merge according to repository policy while preserving issue and exact-head evidence.

## Planned branch and pull-request sequence

1. `foundation/mobile-architecture-and-toolchain` — app shell, build system, environment contract, linting, tests, and secure configuration placeholders.
2. `feature/auth-device-session` — authentication, secure token storage, device registration, revocation, and capability bootstrap.
3. `feature/navigation-home-notifications` — role-aware navigation, Home, notification inbox, deep links, and push registration.
4. `feature/dnd-readonly-offline` — campaign and character read views, session data, player-safe maps, and encrypted bounded cache.
5. `feature/server-status-guarded-actions` — server status and approved low-risk command workflow.
6. `feature/nexus-ai-review` — AI health, monitor findings, and advisory review without autonomous execution.
7. `integration/mobile-observability-hardening` — diagnostics, audit correlation, security hardening, accessibility, performance, and end-to-end tests.
8. `release/mobile-preview-candidate` — release preparation only after separate Owner authorization.

Parallel branches must not modify the same architectural contract without a recorded coordination decision.

## Testing strategy

- Unit tests for domain models, permissions, redaction, offline cache rules, deep links, and state reducers.
- Contract tests against versioned Khaos Nexus API fixtures.
- Authentication tests for PKCE, token rotation, revocation, expiry, and secure-storage failure.
- UI tests for navigation visibility, accessibility, loading, empty, denied, stale, and error states.
- Integration tests for push registration, notification detail fetch, idempotent commands, audit correlation, and conflict handling.
- Offline tests for encryption, cache limits, stale labeling, reconnect, conflict resolution, and cache clearing.
- Security tests ensuring no protected credentials enter app bundles, logs, analytics, notifications, screenshots where protected, or backups.
- Android and iOS build validation when each platform enters supported scope.
- Real-device validation on supported OS versions before any preview publication.

## Release gates

No mobile build may be published until separately authorized and all applicable gates pass:

- exact source commit recorded;
- dependency and secret scans pass;
- unit, contract, integration, UI, and accessibility tests pass;
- platform builds and signing configuration are verified without exposing signing secrets;
- privacy disclosures and notification behavior are reviewed;
- authentication, device revocation, offline data deletion, and upgrade behavior pass real-device testing;
- shared API compatibility is confirmed against the approved Khaos Nexus desktop/backend baseline;
- rollback or kill-switch strategy is documented;
- release notes, artifact hashes, and distribution channel are explicitly approved.

Preview, beta, store, enterprise, and production publication are separate authorization decisions.

## Roadmap

### Phase 0 — Planning and contracts

- Confirm product scope, architecture boundaries, platforms, API ownership, and security model.
- Inventory reusable Khaos Nexus services and missing mobile-safe contracts.
- Establish issue sequence, acceptance criteria, test strategy, and release policy.

### Phase 1 — Foundation

- Select and initialize the mobile toolchain.
- Add app shell, design tokens, navigation foundation, configuration handling, linting, tests, and CI.
- No production credentials or live privileged actions.

### Phase 2 — Identity and notifications

- Implement authentication, secure session storage, device management, capabilities, push registration, notification inbox, and deep links.

### Phase 3 — D&D companion

- Add campaign, character, session, notes, dice, and player-safe map experiences.
- Add bounded encrypted offline support.

### Phase 4 — Operations companion

- Add server health and carefully approved guarded actions.
- Add scheduler-result visibility without creating or owning schedules.

### Phase 5 — Nexus AI and community

- Add AI service health and advisory review.
- Add selected community, ticket, event, and announcement surfaces.

### Phase 6 — Hardening and preview readiness

- Complete observability, accessibility, performance, security, privacy, device coverage, and release-candidate validation.

## Dependencies on Khaos Nexus

Mobile implementation depends on approved shared contracts for:

- account authentication and device sessions;
- capability and permission bootstrap;
- notification registration and delivery;
- D&D player-safe projections and bounded mutations;
- game-server status and command lifecycle;
- audit-event lookup and action correlation;
- Nexus AI monitor and advisory projections;
- API version negotiation and deprecation policy.

Missing contracts must be planned as desktop/backend issues owned by `Khaos-Krew/Khaos-Nexus`; mobile code remains in this repository.

## Key risks

- **Authority duplication:** mitigated by routing every action through existing backend authorities.
- **Credential leakage:** mitigated by secure storage, short-lived tokens, redaction, secret scanning, and minimal push payloads.
- **Desktop/mobile contract drift:** mitigated by versioned schemas, contract fixtures, compatibility negotiation, and coordinated release gates.
- **Offline conflicts:** mitigated by read-only defaults, entity revisions, explicit conflict UI, and narrow queued-action scope.
- **Notification overload or leakage:** mitigated by preferences, quiet hours, minimal payloads, and detail fetch after authentication.
- **Unsafe remote control:** mitigated by server-side authorization, step-up confirmation, idempotency, audit records, action expiry, and desktop-only defaults.
- **Scope expansion:** mitigated by phased issues, WIP limits, one-slice pull requests, and explicit release authorization.

## Release policy

This repository currently contains planning documentation only. Do not create tags, publish builds, deploy services, upload store artifacts, or push to any mobile release channel without a separate explicit Owner instruction.

A merged feature branch is not a release. Release preparation and publication must use a separately assigned issue, exact commit, validated artifacts, and explicit authorization.

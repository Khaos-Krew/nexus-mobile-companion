# Preview APK update channel

This directory is the durable HTTPS source used by the Khaos Nexus Mobile Companion Update Center.

Public channel files:

- `update-manifest.json` — signed metadata for the newest approved preview APK.
- `Khaos-Nexus-Mobile-Companion-current.apk` — the APK referenced by the manifest.
- `update-manifest-public.b64` — RSA public key embedded in the app for manifest verification.

Private APK-signing and update-manifest-signing keys are not stored in this repository.

The app downloads from `raw.githubusercontent.com` only after the user reviews and approves the update. Before Android opens its installer, the app verifies the manifest signature, channel, package name, newer version code, signed file size, APK SHA-256, APK identity, and signing-certificate continuity.

Publishing a new update requires:

1. increasing `versionCode`;
2. building the APK;
3. signing it with the permanent preview APK certificate;
4. calculating its final size and SHA-256;
5. signing the canonical manifest with the separate update-manifest private key;
6. replacing the APK and manifest together;
7. validating installation over the previous stable-signed preview.

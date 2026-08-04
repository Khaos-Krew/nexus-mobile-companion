# Khaos Nexus Mobile in-app updates

## Decision

The Mobile Companion includes a signed, user-confirmed update flow for direct preview builds.

Update metadata may be checked automatically at startup when an approved channel is configured. APK download and installation are never automatic. The user must:

1. review the available version and release notes;
2. approve download and verification;
3. approve any Android unknown-source setting required for the direct preview;
4. approve Android's final package installer confirmation.

The updater is limited to replacing the Mobile Companion package itself. It is not a general package manager.

## Distribution split

### Direct preview builds

The `debug` source-set manifest includes:

- `android.permission.INTERNET` for the approved HTTPS update channel;
- `android.permission.REQUEST_INSTALL_PACKAGES` for a user-initiated handoff to Android's package installer.

These permissions are not present in the main manifest and therefore are not automatically included in a future store release build.

### Google Play builds

A future Google Play build must use Google Play's in-app update mechanism and must not merge the debug-only `REQUEST_INSTALL_PACKAGES` permission. That implementation requires a separate release-channel decision and Play configuration.

## Required verification

Before Android receives an APK installation session, the app verifies all of the following:

- HTTPS manifest URL;
- HTTPS APK URL;
- APK host is the manifest host or an explicitly allowlisted host;
- supported manifest schema;
- expected update channel;
- exact installed package name;
- newer version code;
- signed APK size;
- RSA/SHA-256 manifest signature;
- downloaded file size;
- downloaded APK SHA-256;
- downloaded APK package identity;
- downloaded APK version code;
- downloaded APK signing certificate matches the installed app.

Any mismatch rejects the update before the system installer opens.

## Stable signing requirement

Android updates require the installed and replacement APKs to use the same signing certificate.

The workflow supports a stable preview key through these repository secrets:

- `PREVIEW_KEYSTORE_BASE64`
- `PREVIEW_KEYSTORE_PASSWORD`
- `PREVIEW_KEY_ALIAS`
- `PREVIEW_KEY_PASSWORD`

When all four are configured, the preview APK uses that key. When any are missing, CI falls back to an ephemeral Android debug key and records that fact in `signing-mode.txt` inside the workflow artifact.

An APK signed with an ephemeral runner key cannot reliably update an APK produced by another clean runner. After the stable preview key is provisioned, testers may need one clean reinstall of the stable-signed preview before future in-app replacements work.

The preview key must not be reused for a production package.

## Update-channel configuration

The workflow reads these repository variables:

- `UPDATE_MANIFEST_URL` — exact HTTPS JSON manifest URL;
- `UPDATE_ALLOWED_HOSTS` — optional comma-separated APK CDN hosts;
- `UPDATE_PUBLIC_KEY_B64` — Base64 X.509 DER RSA public key.

The preview channel name is currently fixed to `preview`.

When the manifest URL or public key is absent, the Update Center remains installed but live update checks stay disabled. The companion opens normally.

GitHub Actions artifacts are temporary and authenticated, so they are not used as the application update channel. A later controlled prerelease must publish the APK and signed manifest to an approved durable HTTPS location.

## Manifest format

```json
{
  "schemaVersion": 1,
  "channel": "preview",
  "packageName": "com.khaoskrew.nexuscompanion.preview",
  "versionCode": 3,
  "versionName": "0.3.0-preview",
  "publishedAt": "2026-08-04T18:00:00Z",
  "apkUrl": "https://updates.example.com/mobile/Khaos-Nexus-Mobile-0.3.0-preview.apk",
  "sha256": "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
  "sizeBytes": 123456,
  "mandatory": false,
  "releaseNotes": "Update Center improvements and fixes.",
  "signature": "BASE64_RSA_SIGNATURE"
}
```

`versionCode` must increase for every published APK.

## Canonical signed payload

The manifest signature uses `SHA256withRSA` over this UTF-8 payload:

```text
schemaVersion=<integer>
channel=<string>
packageName=<string>
versionCode=<integer>
versionName=<string>
publishedAt=<string>
apkUrl=<string>
sha256=<lowercase hex>
sizeBytes=<integer>
mandatory=<true|false>
releaseNotesSha256=<lowercase SHA-256 of UTF-8 releaseNotes>
```

The JSON `signature` field is not part of the signed payload.

Use `tools/sign_update_manifest.py` to generate the canonical payload and signature.

## Update signing key

Generate a separate offline RSA update-metadata key:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out update-manifest-private.pem
openssl pkey -in update-manifest-private.pem -pubout -outform DER \
  | base64 -w0 > update-manifest-public.b64
```

- Keep `update-manifest-private.pem` outside the repository and CI logs.
- Store only the public Base64 value in `UPDATE_PUBLIC_KEY_B64`.
- The APK signing key and update-manifest signing key are deliberately separate.

## Signing a manifest

Prepare the manifest without a meaningful signature value, then run:

```bash
python3 tools/sign_update_manifest.py \
  --manifest update-manifest.json \
  --private-key update-manifest-private.pem \
  --output signed-update-manifest.json
```

The script writes the Base64 RSA signature into the output JSON.

## Rollout controls

- Automatic installation remains disabled.
- The `mandatory` field is informational in the preview and does not prevent the user from opening the companion.
- No update may be published without explicit Owner approval of the channel, signed APK, manifest, hashes, release notes, and rollback package.
- The current GitHub Actions artifact remains a controlled test artifact, not a public release.

#!/usr/bin/env python3
"""Create the canonical Khaos Nexus Mobile update payload and RSA signature."""

from __future__ import annotations

import argparse
import base64
import hashlib
import json
import pathlib
import subprocess
import tempfile
from typing import Any

REQUIRED_FIELDS = (
    "schemaVersion",
    "channel",
    "packageName",
    "versionCode",
    "versionName",
    "publishedAt",
    "apkUrl",
    "sha256",
    "sizeBytes",
    "mandatory",
    "releaseNotes",
)


def canonical_payload(manifest: dict[str, Any]) -> str:
    missing = [field for field in REQUIRED_FIELDS if field not in manifest]
    if missing:
        raise ValueError(f"Missing required manifest fields: {', '.join(missing)}")

    sha256 = str(manifest["sha256"]).lower()
    if len(sha256) != 64 or any(character not in "0123456789abcdef" for character in sha256):
        raise ValueError("sha256 must contain exactly 64 hexadecimal characters")

    release_notes = str(manifest["releaseNotes"])
    release_notes_sha256 = hashlib.sha256(release_notes.encode("utf-8")).hexdigest()
    mandatory = "true" if bool(manifest["mandatory"]) else "false"

    return "\n".join(
        (
            f"schemaVersion={int(manifest['schemaVersion'])}",
            f"channel={manifest['channel']}",
            f"packageName={manifest['packageName']}",
            f"versionCode={int(manifest['versionCode'])}",
            f"versionName={manifest['versionName']}",
            f"publishedAt={manifest['publishedAt']}",
            f"apkUrl={manifest['apkUrl']}",
            f"sha256={sha256}",
            f"sizeBytes={int(manifest['sizeBytes'])}",
            f"mandatory={mandatory}",
            f"releaseNotesSha256={release_notes_sha256}",
        )
    )


def sign(payload: str, private_key: pathlib.Path) -> str:
    with tempfile.TemporaryDirectory(prefix="nexus-update-sign-") as temporary_directory:
        directory = pathlib.Path(temporary_directory)
        payload_path = directory / "payload.txt"
        signature_path = directory / "signature.bin"
        payload_path.write_text(payload, encoding="utf-8", newline="")
        subprocess.run(
            (
                "openssl",
                "dgst",
                "-sha256",
                "-sign",
                str(private_key),
                "-out",
                str(signature_path),
                str(payload_path),
            ),
            check=True,
        )
        return base64.b64encode(signature_path.read_bytes()).decode("ascii")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", required=True, type=pathlib.Path)
    parser.add_argument("--private-key", required=True, type=pathlib.Path)
    parser.add_argument("--output", required=True, type=pathlib.Path)
    parser.add_argument(
        "--payload-output",
        type=pathlib.Path,
        help="Optional path for the exact canonical payload that was signed.",
    )
    arguments = parser.parse_args()

    manifest = json.loads(arguments.manifest.read_text(encoding="utf-8"))
    if not isinstance(manifest, dict):
        raise ValueError("Manifest root must be a JSON object")

    payload = canonical_payload(manifest)
    manifest["sha256"] = str(manifest["sha256"]).lower()
    manifest["signature"] = sign(payload, arguments.private_key)

    arguments.output.parent.mkdir(parents=True, exist_ok=True)
    arguments.output.write_text(
        json.dumps(manifest, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
        newline="",
    )
    if arguments.payload_output is not None:
        arguments.payload_output.parent.mkdir(parents=True, exist_ok=True)
        arguments.payload_output.write_text(payload, encoding="utf-8", newline="")

    print(f"Signed manifest written to {arguments.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

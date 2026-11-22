#!/usr/bin/env bash
# Builds a signed release APK and publishes it as a GitHub release, so the
# phone can download and sideload it (README, Install). Run by hand on the
# release Mac only: it needs the release keystore passwords, and publishing
# creates a tag on GitHub, which only the project's owner does (datum 0.1).
#
# The version comes from versionName in app/build.gradle.kts, and CHANGELOG.md
# must already have a section for it. Passwords are read from
# THYMIAN_KEYSTORE_PASSWORD and THYMIAN_KEY_PASSWORD, or asked for without
# echoing; they are never written to a file or printed.
set -euo pipefail

repository_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repository_root"

apksigner="${ANDROID_HOME:?ANDROID_HOME is not set}/build-tools/36.0.0/apksigner"
version_name="$(sed -n 's/^ *versionName = "\([^"]*\)".*/\1/p' app/build.gradle.kts)"
tag="v$version_name"
apk_path="app/build/outputs/apk/release/app-release.apk"
published_apk_name="thymian-$version_name.apk"
notes_file="$(mktemp)"
trap 'rm -f "$notes_file"' EXIT

fail() {
    echo "release: $1" >&2
    exit 1
}

[ -n "$version_name" ] || fail "no versionName found in app/build.gradle.kts"
[ -x "$apksigner" ] || fail "apksigner not found at $apksigner"
command -v gh >/dev/null || fail "the GitHub CLI (gh) is not installed"

# The release must be exactly the pushed main branch, so the tag points at the code that was built.
[ -z "$(git status --porcelain)" ] || fail "the working tree has uncommitted changes"
git fetch --quiet origin main
[ "$(git rev-parse HEAD)" = "$(git rev-parse origin/main)" ] || fail "HEAD is not the pushed origin/main; push first"
if gh release view "$tag" >/dev/null 2>&1; then fail "a release $tag already exists; raise versionName and versionCode"; fi

# Release notes are this version's CHANGELOG section.
awk -v heading="## [$version_name]" '
    index($0, heading) == 1 { inside = 1; next }
    inside && /^## \[/ { exit }
    inside { print }
' CHANGELOG.md > "$notes_file"
[ -s "$notes_file" ] || fail "CHANGELOG.md has no section headed ## [$version_name]"

if [ -z "${THYMIAN_KEYSTORE_PASSWORD:-}" ]; then
    read -r -s -p "Keystore password: " THYMIAN_KEYSTORE_PASSWORD
    echo
fi
if [ -z "${THYMIAN_KEY_PASSWORD:-}" ]; then
    read -r -s -p "Key password (Enter if the same as the keystore password): " THYMIAN_KEY_PASSWORD
    echo
    THYMIAN_KEY_PASSWORD="${THYMIAN_KEY_PASSWORD:-$THYMIAN_KEYSTORE_PASSWORD}"
fi
export THYMIAN_KEYSTORE_PASSWORD THYMIAN_KEY_PASSWORD

echo "release: building $tag"
./gradlew --quiet clean check assembleRelease

echo "release: verifying the signature"
"$apksigner" verify --print-certs "$apk_path" | grep -E "Signer #1 certificate (DN|SHA-256 digest)"

cp "$apk_path" "app/build/outputs/apk/release/$published_apk_name"
echo "release: publishing $tag to GitHub"
gh release create "$tag" "app/build/outputs/apk/release/$published_apk_name" \
    --target "$(git rev-parse HEAD)" \
    --title "Thymian $version_name" \
    --notes-file "$notes_file"
echo "release: done; the APK is attached to $tag"

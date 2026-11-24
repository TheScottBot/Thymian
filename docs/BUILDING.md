# Building, installing and releasing

## Requirements

- JDK 17 or later to run Gradle. Development uses the JetBrains runtime 25
  bundled with Android Studio, with `JAVA_HOME` pointing at it.
- The Android SDK with platform 37 and build tools 36.0.0, with `ANDROID_HOME`
  set. Gradle itself comes from the wrapper in this repository.
- Devices: minimum Android 14 (API 34). Targets Android 17 (API 37).

## Build and check

From the repository root:

```bash
./gradlew check assembleDebug
```

That runs every unit test, Android lint and ktlint with warnings as errors,
and the typography check, then builds
`app/build/outputs/apk/debug/app-debug.apk`. [TESTING.md](../TESTING.md) lists
each suite on its own.

To fix formatting that ktlint reports:

```bash
./gradlew ktlintFormat
```

## Install a debug build over USB

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then long press the home screen, choose Widgets and add Thymian.

## Debug APK from GitHub Actions

Every push and pull request runs `.github/workflows/ci.yml`, which uploads the
debug APK as the `thymian-debug-apk` artefact. To download the latest one:

```bash
gh run download --repo TheScottBot/Thymian --name thymian-debug-apk
```

A debug build from CI is signed with the CI runner's debug key, so it cannot
install over a build signed on another machine without uninstalling first,
which deletes widget settings. The Xiaomi only ever receives release builds
(CWD32).

## Release builds

Release builds are signed only on the release Mac, with the key `thymian` in
`~/Keystores/thymian-release.jks`. The passwords are never stored in the
repository or in Gradle files; the build reads `THYMIAN_KEYSTORE_PASSWORD`
and `THYMIAN_KEY_PASSWORD` from its environment and stops before compiling if
either, or the keystore, is missing.

To publish a release that the phone can download (installing steps are in the README):

1. Raise `versionCode` and set `versionName` in `app/build.gradle.kts`. Every
   release needs a higher `versionCode`, or Android will not install it over
   the previous one.
2. In `CHANGELOG.md`, rename `## [Unreleased]` to the new version, for example
   `## [1.0.0]`, and add a fresh empty `## [Unreleased]` above it.
3. Commit and push both changes.
4. Run:

```bash
./scripts/release.sh
```

It checks that the tree is clean and matches the pushed `origin/main`, that
the version has a changelog section and no release yet, asks for the two
passwords without echoing them unless both variables are already set, runs
every check, builds and signs the APK, prints the signing certificate's
fingerprint, and publishes a GitHub release `v` plus the version with the APK
attached as `thymian-` plus the version `.apk`. Publishing creates the tag on
GitHub.

The APK is published as a release asset rather than a GitHub package:
GitHub Packages holds libraries and container images, and an APK is a file to
download, which releases serve directly.

## Repository layout

| Path | Contents |
|---|---|
| `app/` | The Android app: widget provider, layouts, resources |
| `domain/` | Pure Kotlin value types and rules, no Android imports |
| `tools/typography-check/` | The datum 0.8 typography scan, run by `check` |
| `config/` | The typography check's explicit exclusion list |
| `scripts/` | `release.sh`, which builds, signs and publishes a release |
| `docs/` | Specification, Plan record and evidence, and the project records |

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
and `THYMIAN_KEY_PASSWORD` from the environment of `./gradlew assembleRelease`.
Set them only for that command and keep them out of shell history and files.
A release script that reads both from Dashlane is planned and will replace
this paragraph with the exact command.

The build stops before compiling if either variable or the keystore is
missing. The signed APK is `app/build/outputs/apk/release/app-release.apk`.

## Repository layout

| Path | Contents |
|---|---|
| `app/` | The Android app: widget provider, layouts, resources |
| `domain/` | Pure Kotlin value types and rules, no Android imports |
| `tools/typography-check/` | The datum 0.8 typography scan, run by `check` |
| `config/` | The typography check's explicit exclusion list |
| `docs/` | Specification, Plan record and evidence, and the project records |
| `spikes/` | Plan stage experiments, deleted once the Pixel soak is read |

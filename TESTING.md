# Testing

Every suite with its command and where it runs, the testing deviations, and
the gates that only the owner of the project can clear. Commands run from the
repository root.

## Automated suites

| Suite | Command | Runs where | What it covers |
|---|---|---|---|
| Everything below | `./gradlew check assembleDebug` | Mac and CI | The whole automated gate |
| Domain unit tests | `./gradlew :domain:test` | Mac and CI, JVM only | Domain rules; the import boundary that keeps Android out of `domain` |
| Typography scanner tests | `./gradlew :typography-check:test` | Mac and CI, JVM only | Each typography rule and exemption, against planted violations |
| Typography check | `./gradlew :typography-check:checkTypography` | Mac and CI | Every tracked and untracked file except the explicit list in `config/typography-excluded-paths.txt`; also on device test names with characters dex cannot hold, since CI never dexes on device tests |
| ktlint | `./gradlew ktlintCheck` | Mac and CI | Kotlin sources and build scripts, official Kotlin style |
| Android lint | `./gradlew :app:lintDebug :domain:lint` | Mac and CI | App and domain, warnings as errors |
| On device tests | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest` | Mac, on the API 34 emulator; not in CI, which has no device | The locale date pattern from Android's own data, and the renderer's RemoteViews applied as a launcher would |
| Weather icon conversion | `./gradlew :icon-conversion:test` | Mac and CI, JVM only | Every condition has a drawable; committed drawables and lookup match a fresh conversion; icons are tintable; masks become clip paths |
| Release signing guard | `./gradlew :app:assembleRelease` with the variables unset | Mac | Fails before compiling, naming each missing input |

CI is `.github/workflows/ci.yml`; it runs `./gradlew check assembleDebug` on
JDK 25 on every push and pull request.

## Proof that each check can fail

Datum 0.4 requires every check to be seen failing on a planted violation
before it is trusted.

| Check | Seen failing |
|---|---|
| Domain import boundary | Planted `android` and `androidx` imports in temporary source trees, in its own tests |
| Typography scanner | Twelve planted cases in its own tests; on first run it also caught real violations in new files |
| Typography check task | A planted untracked file with an em dash and a hyphen run, 22 November 2026, then deleted |
| ktlint | Reported real formatting violations in existing sources on first run, 22 November 2026, before they were fixed |
| Android lint | Reported three real issues on first run, 22 November 2026, before they were fixed |
| Release signing guard | Run without the variables, 22 November 2026 |
| Open-Meteo mutation test | With the temperature range check removed it passed at first, because random byte changes never formed an out of range number; boundary number substitution was added, and it then failed with "temperature 99.0", 22 November 2026 |
| On device test name rule | A comma planted in an on device test name was reported at its line, 22 November 2026 |
| Place search mutation test | With the coordinate check removed from the place search parser it failed, 22 November 2026 |
| Weather icon conversion | A one dp change planted in a committed drawable was not caught at first because Gradle called the tests up to date; the drawables, lookup and SVGs were declared as test inputs, and the planted change then failed the test, 22 November 2026 |
| On device tests | Failed first because nothing they call existed; then the en-GB date test failed on real locale data (below), 22 November 2026 |

## On device tests on the Pixel

Gradle's connected test task uninstalls the app afterwards, which would remove
a placed widget, so on the Pixel the tests run through the instrumentation
runner instead:

```bash
./gradlew assembleDebug assembleDebugAndroidTest
adb -s 61310DLCH000ZB install -r app/build/outputs/apk/debug/app-debug.apk
adb -s 61310DLCH000ZB install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s 61310DLCH000ZB shell am instrument -w fyi.punkdev.thymian.test/androidx.test.runner.AndroidJUnitRunner
```

Android's locale data differs by version: on 22 November 2026 the en-GB date
pattern was `EEE, d MMMM` on the API 34 emulator and `EEE d MMMM` on the Pixel
(API 37). The date test therefore checks field order and month width, with
the comma optional.

CWD38, 22 November 2026: the CWE5 test `talkback hears the condition and the
temperature with its unit` had a fixed weather timestamp that became more than
an hour old, so the CWE6 stale rule changed its description. Only that fixture
timestamp was changed, to the current time; its assertions are as written.

## Testing deviations

- Memory and undefined behaviour analysis does not apply: Kotlin on the JVM is
  memory safe and has no sanitiser (CWD20).
- Fuzzing uses a seeded mutation test over recorded fixtures in plain JUnit
  instead of a fuzzing engine (CWD20): `OpenMeteoForecastMutationTest`, seed
  20261122, run by `./gradlew :domain:test` on every push.

## Gates belonging to the owner

These cannot be cleared by an automated run or by the agent (datum 0.17,
items 19 and 20). Results are recorded in docs/HARDWARE_COMPATIBILITY.md, where only
the owner adds a row saying a gate passed.

- Every Done when check that needs a phone, judged on the Pixel 10 Pro.
- Appearance, legibility and TalkBack judgements.
- The 24 hour unattended refresh soak.
- Installing a release build over a previous release build without losing
  settings, which needs the signing passwords.
- Every Xiaomi 11T Pro check. None is available before delivery (CWD30); real
  use is recorded in docs/FIELD_NOTES.md.

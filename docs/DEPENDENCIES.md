# Dependencies

Every third party component, font and icon set, with the facts datum 0.13
requires before adding one. Versions are pinned in `gradle/libs.versions.toml`
or, for CI actions, by commit hash in `.github/workflows/ci.yml`. Read on
22 November 2026 unless stated.

## Shipped in the app

Nothing third party is compiled into the app yet beyond the Kotlin standard
library, which AGP's built in Kotlin adds.

| Component | Version | Licence | Why the platform is not enough |
|---|---|---|---|
| Kotlin standard library | 2.4.21 | Apache 2.0 | Kotlin is the required language (specification 2.3); the library is part of it |
| kotlinx-serialization-json (with kotlinx-serialization-core) | 1.13.0 | Apache 2.0 | See below |
| androidx.work:work-runtime | 2.12.0 | Apache 2.0 | See below |

The debug APK was 2,701,916 bytes after CWE1 and 9,071,708 bytes after CWE6, both on 22 November 2026; debug builds are not shrunk.

JetBrains Mono is not used (CWD3).

## Test only libraries

On device tests only; never in the app.

| Library | Version | Licence | Why |
|---|---|---|---|
| androidx.test:core | 1.7.0 | Apache 2.0 | Application context for on device tests |
| androidx.test:runner | 1.7.0 | Apache 2.0 | The instrumentation runner |
| androidx.test.ext:junit | 1.3.0 (brings JUnit 4.13.2, EPL 1.0) | Apache 2.0 | JUnit 4 runner for Android; JUnit 6 does not run on devices |

Only Android's own locale data and RemoteViews can answer what these tests
check, so a JVM test cannot replace them. Maintained by Google; newest stable
releases on Google Maven, 22 November 2026.

### WorkManager 2.12.0

- **Provides:** background work that survives process death, reboots and (per the Plan soak, 9.1 and 6.1) app updates, with a network constraint; used for the weather refresh.
- **Why the platform is not enough:** JobScheduler alone would need hand written persistence, constraint handling and rescheduling that WorkManager already does; the specification names it as guidance (2.3).
- **Maintenance:** Google, AndroidX; 2.12.0 is the latest stable, 23 September 2026.
- **Licence:** Apache 2.0.
- **Platform support:** API 23 and later; uses JobScheduler on this app's minimum API 34.
- **Security:** no network access of its own; runs only this app's workers.
- **Test strategy:** the Plan soak on the Pixel; scheduling policy tests with a fake time source in CWE6.
- **Replacement cost:** moderate; the worker classes are small.
- **Added early:** CWE5 uses it for a one off refresh when a widget is placed or saved; the 30 minute schedule is CWE6.

### kotlinx-serialization-json 1.13.0

- **Provides:** parsing of Open-Meteo responses into a JSON tree in the pure Kotlin domain module, where the size bound, required fields, types and ranges are then checked (datum 0.10).
- **Why the platform is not enough:** Android's `org.json` exists only on the device, so a parser using it could not be tested and mutation tested on the JVM in the domain module (specification 2.1). Writing a JSON parser by hand would be riskier than a widely reviewed one.
- **How it is used:** only `Json.parseToJsonElement` and the tree types; no compiler plugin and no reflection.
- **Maintenance:** JetBrains; 1.13.0 released 11 November 2026, built on Kotlin 2.4.10, compatible with the project's Kotlin 2.4.21.
- **Licence:** Apache 2.0.
- **Size:** json 300,818 bytes and core 416,403 bytes as jars, before shrinking.
- **Security:** parses untrusted network text; the body is bounded to 64 KiB before parsing, and the seeded mutation test, including 20,000 deep nesting, runs on every push.
- **Test strategy:** `OpenMeteoForecastTest` and `OpenMeteoForecastMutationTest`.
- **Replacement cost:** low; one file uses it.

## Material derived from other projects

| Material | Source | Licence | Notes |
|---|---|---|---|
| Weather icons | Meteocons 3.0.0-next.10, `@meteocons/svg-static`, monochrome style; 23 icons vendored verbatim in `third_party/meteocons/` with per file digests in its PROVENANCE.md | MIT, Copyright (c) 2020-present Bas Milius | Converted to `app/src/main/res/drawable/weather_*.xml` by the icon conversion tool below (CWD22, CWD23, CWD24) |
| App and widget icon | Thyme `internal/render/appicon.go` and `internal/theme/theme.go` at commit 01823cb9e7a296d501131b3958fae30871fc4b8c | MIT, Copyright (c) 2026 TheScottBot | Geometry and colours redrawn as an Android vector drawable (CWD33); no file copied |

## Build and test tools

These run on the developer's Mac and in CI. None ships in the app.

### Android Gradle Plugin 9.5.1

- **Provides:** the Android build, resource processing, built in Kotlin compilation and Android lint.
- **Why:** the only supported way to build an Android app.
- **Maintenance:** Google; 9.5.1 is the newest patch listed in the release notes on 22 November 2026.
- **Licence:** Apache 2.0.
- **Platform support:** compileSdk up to 37; needs Gradle 9.6.0 or later and JDK 17 or later.
- **Security:** build time only; resolved from Google's Maven repository.
- **Test strategy:** every build, locally and in CI.
- **Replacement cost:** none realistic.

### Kotlin Gradle plugin 2.4.21

- **Provides:** compilation of the pure JVM modules (`domain`, `tools/typography-check`).
- **Why:** the domain must build without Android (specification 2.1).
- **Maintenance:** JetBrains; newest stable release, 8 October 2026.
- **Licence:** Apache 2.0.
- **Security:** build time only, from the Gradle Plugin Portal.
- **Test strategy:** every build.
- **Replacement cost:** none realistic.

### Gradle 9.9.2 (wrapper)

- **Provides:** the build tool, downloaded by `gradlew` and checked against the pinned `distributionSha256Sum`.
- **Licence:** Apache 2.0.
- **Security:** the distribution checksum is pinned; CI also validates the wrapper jar.
- **Replacement cost:** none realistic.

### JUnit 6.1.3

- **Provides:** the unit test framework for the JVM modules.
- **Why:** Kotlin and the JDK have no test runner.
- **Maintenance:** the JUnit team; newest stable release on Maven Central.
- **Licence:** Eclipse Public License 2.0. Test scope only, never distributed.
- **Replacement cost:** low; the tests use plain assertions.

### ktlint 1.8.0

- **Provides:** the Kotlin formatter and style check required by datum 0.5 (CWD26).
- **Why:** neither Kotlin nor AGP enforces a formatting standard.
- **How it runs:** its self contained command line jar (`ktlint-cli`, shadowed variant, 71,470,431 bytes) through the root `ktlintCheck` and `ktlintFormat` tasks, so no Gradle plugin has to keep pace with AGP.
- **Maintenance:** Pinterest and contributors; newest release on Maven Central.
- **Licence:** MIT.
- **Security:** build time only.
- **Replacement cost:** low; ktfmt is the alternative.

## One time conversion tool

`tools/icon-conversion`, run with `./gradlew :icon-conversion:convertWeatherIcons`.
It ships in nothing built (datum 0.13). It uses Android's own SVG converter,
`Svg2Vector` from `com.android.tools:sdk-common` 32.5.1 (Apache 2.0), the
release AGP 9.5.1 depends on. Its tests run in `check` and fail if the committed
drawables or `WeatherIconDrawables.kt` differ from a fresh conversion.

Two exact equivalences are applied to each SVG on the way in, never to the
vendored files: `currentColor` becomes white, so the widget can tint icons to
the text colour; and an alpha mask whose shapes are all opaque black is
rewritten as a solid white mask, which `Svg2Vector` accepts as a clip path. Any
warning from the converter stops the run.

## Continuous integration actions

Each is pinned to the full commit hash of its release.

| Action | Release | Commit | Licence | Use |
|---|---|---|---|---|
| actions/checkout | v7.0.1 | 3d3c42e5aac5ba805825da76410c181273ba90b1 | MIT | Check out without keeping credentials |
| actions/setup-java | v6.3.0 | d1374f8ac74474200d16ef8ecee22c7ae7a51880 | MIT | Temurin JDK 25 |
| gradle/actions (setup-gradle) | v6.4.1 | 8da46b539cd8fe6ab27ca75128919a18d757604c | MIT, with a proprietary caching component | Wrapper validation and caching |
| actions/upload-artifact | v7.0.2 | cf430e030ddbb5b0abf93d22962f4752f3646cd9 | MIT | Keep the debug APK |

setup-gradle's default `enhanced` cache is `gradle-actions-caching`, which its
repository marks as proprietary. The workflow sets `cache-provider: basic`,
the MIT licensed implementation, so no proprietary component runs.

## Android SDK packages

Installed by the Android Studio setup wizard on the Mac and by `sdkmanager` in
CI: SDK Platform 37 (`platforms;android-37.0`), build tools 36.0.0. Licensed
under the Android Software Development Kit License Agreement.

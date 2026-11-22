# Thymian: Plan stage record

This is the Plan stage record required by ENGINEERING_DATUM.md (version 2, dated
20 September 2026), Part 1, Stage one. The project specification calls this file
PLAN_FINDINGS.md; the datum path is used here and the mismatch is raised as
CWD14.

Record opened 22 November 2026. Every entry says whether it was read from
source, run for real, or is still open. Nothing in this record was observed on
either target phone yet: no phone was connected and no Android SDK was installed
on the development Mac when it was written (see section 1).

Status of the Plan stage: **open**. CWP1, CWP4 (source half) and CWP6 have
evidence. CWP2, CWP3, CWP5 and CWP7 need the two phones and are blocked on the
tooling gaps in section 1. Questions for the author are batched in section 10.

(Status update, 22 November 2026: every decision is answered, see 10.0. CWP2 and
CWP5 have emulator and Pixel evidence. Still open: the CWP7 Pixel soak results
(9.1), a light wallpaper check on the Pixel for CWP3, and the CWP4 size bucket
thresholds. Every Xiaomi item is not available before delivery, per CWD30.)

## 1. Development environment

Read on 22 November 2026 by running commands on the development Mac.

| Item | Observed | Status |
|---|---|---|
| Machine | macOS 27.2.1 (build 26B7131), arm64 | ok |
| Android Studio | 2026.2 (build AI-262.10968.63.2622.16975389), Homebrew cask `android-studio` | installed |
| JDK | OpenJDK 25.0.4, the JetBrains runtime bundled with Android Studio; `JAVA_HOME` points at it | ok, see note A |
| `/usr/libexec/java_home` | finds no registered JDK | harmless while `JAVA_HOME` is set |
| Android SDK | `ANDROID_HOME=/Users/wash/Library/Android/sdk`, but that directory does not exist; no `sdkmanager`, platforms or build tools anywhere | **gap**; closed later the same day, see 3.4, except `sdkmanager` |
| adb | 1.0.41, version 37.0.1, Homebrew cask `android-platform-tools` | ok |
| Connected devices | `adb devices -l` lists none | **gap**; Pixel connected later the same day, see 3.3. The Xiaomi cannot be connected, see CWD30 |
| Gradle | not installed; the project will carry the Gradle wrapper | **gap for bootstrap only**, see note B |
| `~/.gradle/gradle.properties` | absent; `~/.gradle` does not exist | **gap** for release signing (CWE1) |
| Kotlin formatter or static analysis | none installed | needs a decision, CWD26 |
| GitHub CLI | logged in as TheScottBot with `repo` and `workflow` scopes | ok |
| Repository | `origin` is https://github.com/TheScottBot/Thymian.git, public, default branch main, Actions enabled | ok |

Note A. Gradle runs on Java 25 from Gradle 9.1.0 onwards (read from
https://docs.gradle.org/current/userguide/compatibility.html, 22 November 2026).
AGP 9.5 needs JDK 17 or later. JDK 25 is therefore acceptable for both, subject
to the CWE1 build actually passing.

Note B, update 22 November 2026: with the author's approval, Gradle 9.9.2 was
installed with `brew install gradle`, used only to generate the wrapper. Homebrew
also installed OpenJDK 27.0.1.1 as a dependency. `gradle --version` reports the
launcher and daemon JVM as the Android Studio JetBrains runtime 25.0.4, because
`JAVA_HOME` takes precedence, so the Homebrew JDK is not used by builds.

Note B. Generating the wrapper needs a Gradle installation once (or Android
Studio's new project flow). Current Gradle is 9.9.2, built 19 November 2026
(read from https://services.gradle.org/versions/current).

## 2. Reference implementations

Datum requirement: read the three named repositories and record what was taken.
Each was confirmed to exist at the exact path with `git ls-remote` and `gh api`
before cloning, then shallow cloned into the session scratch directory.

| Repository | Commit read | Read on |
|---|---|---|
| https://github.com/FtrOnOff/FtrIO | 7e214d5b3d62d31333031413b43f69d9c7e65a7d | 22 November 2026 |
| https://github.com/FtrOnOff/ftrio-python | b95ec4e1feaf1b3c74a30a43edd5e03c90f7ad68 | 22 November 2026 |
| https://github.com/FtrOnOff/ftrio-rust | e814d40758eb3524d64e9e40688bb8c59de65989 | 22 November 2026 |

Taken from them for Thymian:

- Names spelled out in full; acronyms written as one word (`HttpWeatherSource`,
  `OpenMeteoWeatherSource`), as ftrio-rust enforces; a renamed type says what it
  reads.
- Test names are behaviour sentences. In Kotlin these become backtick names such
  as `` `stale when last success is older than twice the interval` ``. Unit tests
  and on-device tests are kept apart.
- Test doubles are named by role plus interface (`FakeWeatherSource`,
  `RecordingWidgetSettingsStore`), shared in one place, each with a comment
  saying what it records.
- Global state gets an explicit reset hook used by tests.
- Comments explain why and what is load-bearing.
- Errors: a sealed hierarchy in the Rust style rather than exceptions on the
  expected failure paths.
- Fail safe by serving the last good state, logging the consequence.
- CHANGELOG.md in Keep a Changelog form; README section order: what it is, build,
  development gate commands, releasing.
- CI runs format check, lint with warnings as errors, then tests.

Not present in any of the three, so not taken from them: TESTING.md,
IMPLEMENTATION_DEVIATIONS.md, an evaluation record, a dash scan, checks proven
against planted violations, structured logging. Those come from the datum
directly. FtrIO's own README and tests contain em dashes, so FtrIO is not a
typography reference.

## 3. CWP1: datum reconciliation and device facts

### 3.1 Mismatches between the specification and the datum

Each is raised as a question in section 10.

| Number | Specification says | Datum says | Question |
|---|---|---|---|
| M1 | Plan record is PLAN_FINDINGS.md | `docs/evaluation/ACTUAL_CONTRACT_EVALUATION.md` | CWD14 |
| M2 | Opening does not name the datum version or the Part 4 modules applied | A specification must state both | CWD15 |
| M3 | Part 0 restates datum rules | A specification must not restate a datum rule | CWD15 |
| M4 | Required documents: README, LICENCE, NOTICE, DEPENDENCIES.md, PLAN_FINDINGS.md | Always: README.md, TESTING.md, IMPLEMENTATION_DEVIATIONS.md, CHANGELOG.md, LICENSE, the evaluation record. When applicable: PRIVACY.md (personal data: location), HARDWARE_COMPATIBILITY.md (hardware gates), SECURITY.md (trust boundary: network responses) | CWD16 |
| M5 | Bans the em dash only | Bans em dash, en dash and double or triple hyphen runs in prose, and quotation marks used for emphasis; the scan excludes vendored paths by explicit list | CWD17 |
| M6 | The agent must run every Done when check on both phones and record the result | Device checks are author gates; the agent must not claim or infer them | CWD18 |
| M7 | No input bounds for provider responses | 0.10: bound every read, reject unknown fields, run under memory analysis, fuzz guidance | CWD19, CWD20 |
| M8 | CWD3: present options, do not choose | 0.3: every question carries a labelled recommendation | resolved in CWD3 by giving a recommendation labelled as such, without choosing |
| M9 | Identifier prefixes CWP, CWE, CWV, CWD (three letters) | First letter names the owning document, second the kind of item | compatible in practice: no phase prefix is a prefix of a decision prefix. Recorded, not asked |
| M10 | Diagnostics show the location source; logging must be enough to diagnose from logcat | 0.15: never log personal data | compatible if logs carry the source category and never coordinates or place names. Recorded as a design rule, not asked |
| M11 | Stale indicator described as subtle | 0.16: never convey status by colour alone | compatible if the indicator is a glyph, not a colour change. Recorded as a design rule |
| M12 | Spec does not say where it lives | The project specification is the governing document for the work | CWD29 |

### 3.2 Toolchain and target levels

Read from source on 22 November 2026.

| Fact | Value | Source |
|---|---|---|
| Newest stable Android Gradle Plugin | 9.5.1 | https://developer.android.com/build/releases/gradle-plugin |
| Highest API level AGP 9.5 supports | 37 | same page |
| AGP 9.5 needs | Gradle 9.6.0 minimum, Build Tools 36.0.0, JDK 17 minimum | same page |
| Newest stable Kotlin | 2.4.21, 8 October 2026 | https://kotlinlang.org/docs/releases.html |
| Kotlin 2.4 needs | AGP 8.5.2 or later | https://developer.android.com/build/kotlin-support |
| Newest stable Android | Android 17, API 37, released 16 June 2026 | https://android-developers.googleblog.com/2026/06/Android-17.html |

Conclusion proposed for 1.5: **targetSdk 37 and compileSdk 37**, minSdk 34.
Assumption to verify in CWE1: AGP 9.5.1 builds cleanly with Gradle 9.9.2 on
JDK 25; if not, fall back to Gradle 9.6.0.

Consequences of targetSdk 37 already found: edge to edge is enforced on the
settings screen and the opt out is disabled from targetSdk 36
(https://developer.android.com/about/versions/16/behavior-changes-16); Android 17
caps widget bitmap memory per widget
(https://developer.android.com/develop/ui/views/appwidgets). The Xiaomi runs
Android 14, so these behaviours apply only on the Pixel.

### 3.3 Pixel 10 Pro

Read from the phone over adb on 22 November 2026 (serial 61310DLCH000ZB,
product blazer). This replaces the earlier unconfirmed launch version note.

| Property | Value |
|---|---|
| `ro.build.version.release` | 17 |
| `ro.build.version.sdk` | 37 |
| `ro.build.version.sdk_full` | 37.1 (a minor SDK release on top of API 37) |
| `ro.build.display.id` | CP3A.261005.005 |
| `ro.build.version.incremental` | 16271449 |
| Security patch | 5 October 2026 |
| Locale | en-GB |
| `time_12_24` setting | 24 |
| Home launcher | `com.google.android.apps.nexuslauncher`, versionName 17, versionCode 907 |
| Display | 1080 by 2410 pixels at density 420, as currently configured |

Appendix A row "The Pixel 10 Pro runs Android 16 or later" is confirmed: it runs
Android 17, API 37.

### 3.4 Android SDK as installed

Installed by the author through the Android Studio setup wizard; versions read
from each component's `source.properties` on 22 November 2026.

| Component | Version |
|---|---|
| SDK Platform 17 (`platforms/android-37.0`) | API 37.0, revision 2, extension level 22 |
| Build tools | 36.0.0 |
| Platform tools (SDK copy) | 37.0.3 |
| Emulator | 37.3.10 |
| System image | API 37.0, Google Play, arm64-v8a, revision 7 |
| Command line tools | 23.0.0 (installed later the same day) |
| SDK Platform 34 and its sources | revision 3; sources revision 2 |
| API 34 system image (for CWD30 option C) | Google APIs, arm64-v8a, revision 14 |

Update, 22 November 2026, read from `sdkmanager --list_installed` and the
tools' own help text. Command line tools 23.0.0 report `sdkmanager` as
deprecated and replaced by the Android CLI: the `android` binary in the same
directory, version 1.1.16919734, with `android sdk install`, `list`, `update`
and `remove`, and `android emulator create`, `list`, `start` and `stop`.
`android emulator create` takes only a device profile, not a system image, so
the API 34 emulator was made with `avdmanager`, which is still shipped:

`avdmanager create avd --name Thymian_API_34 --package "system-images;android-34;google_apis;arm64-v8a" --device medium_phone`

The emulator booted in about 30 seconds and reported Android 14, API 34, build
`sdk_gphone64_arm64-userdebug 14 UE1A.230829.050 12077443 dev-keys`, security
patch 5 September 2023. The Xiaomi runs HyperOS 1.0.27.0 on Android 14, so the
emulator matches its API level but not its launcher, vendor changes or patch
level.

Android 17's codename is CinnamonBun, as shown in the Android Studio SDK
Manager on 22 November 2026; this settles the codename left unconfirmed in 3.2.

The Homebrew `adb` on the PATH is 37.0.1 while the SDK copy is 37.0.3. Two adb
binaries of different versions restart each other's server; the project
documentation will name the SDK copy.

## 4. CWP2: font and clock

### 4.1 Read from source

AOSP `frameworks/base`, branch main, read 22 November 2026.

- RemoteViews has no typeface setter, and `TextView.setTypeface` carries no
  `@RemotableViewMethod`, so it cannot be called through reflection setters
  either. RemoteViews rejects any method without that annotation.
- When a widget is inflated in another package (the launcher), both
  `RemoteViews.java` (line 8484) and `AppWidgetHostView.java` (line 761) build
  the inflation context with `Context.CONTEXT_RESTRICTED`.
- `TextView.java` line 4385 loads a font resource only when
  `!context.isRestricted() && context.canLoadUnsafeResources()`. In
  `ContextImpl.java` line 3161, `canLoadUnsafeResources` is false for a context
  of another package unless `CONTEXT_IGNORE_SECURITY` is set.
- Reading of the above (inferred, strong): **on stock Android a bundled
  `res/font` JetBrains Mono set in the widget layout will not render on the home
  screen**. A system family name such as `monospace` or `sans-serif-light` does
  apply. The settings screen preview runs in the app's own process, so it would
  show JetBrains Mono and disagree with the real widget.
- HyperOS may have changed this code. Only the spike can say.

### 4.2 Clock behaviour read from source

- TextClock schedules its own tick every minute aligned to the minute boundary,
  in the host process, while visible. It listens for time set and time zone
  changes. There is no midnight handler, but every tick formats from the current
  time, so the date rolls over on the first tick after midnight. Inferred from
  `TextClock.java`, needs the CWE2 device check.
- `TextClock.setFormat12Hour`, `setFormat24Hour` and `setTimeZone` are remotable.
- `DateFormat.format` builds a `SpannableStringBuilder` from the format, so a
  format carrying a size span over the am or pm character may style the suffix
  inside one TextClock. Whether the span survives parcelling through RemoteViews
  is unconfirmed. The fallback is a second TextClock with a format of `a`, which
  is certain to work.
- `TextView.setShadowLayer` is not remotable, so the shadow must come from the
  layout or a style, as the specification already intends.

### 4.3 Glance

Glance 1.2.0 (26 August 2026) has only `Text` in `androidx.glance.text` and no
clock composable
(https://developer.android.com/reference/kotlin/androidx/glance/text/package-summary).
The specification's guidance against Glance is confirmed.

### 4.4 Spike

Source: `spikes/plan-font-clock-transparency` (throwaway, application ID
`fyi.punkdev.thymian.spike`). Built 22 November 2026 with AGP 9.5.1, Gradle
9.9.2 (wrapper), JDK 25.0.4, compileSdk and targetSdk 37, minSdk 34. AGP 9
compiles Kotlin itself (built in Kotlin, enabled by default; read from
https://developer.android.com/build/migrate-to-built-in-kotlin), so no Kotlin
plugin is applied.

Font: JetBrains Mono Light, `fonts/ttf/JetBrainsMono-Light.ttf` at tag v2.304
(tag commit cd5227bd1f61dff3bbd6c814ceaf7ffd95e947d9), 276452 bytes, SHA-256
60c18d7dd58d81b3bbd12e8ce32744a8771bfe2b5280574082b0eaed46c60d24.

The widget layout has seven numbered rows: (1) bundled font set by attribute,
no shadow; (2) bundled font and shadow set by a shared style; (3) system
`monospace`; (4) system `sans-serif-light`; (5) a TextClock whose format carries
a `RelativeSizeSpan(0.5f)` over the am or pm, pushed with `setCharSequence`;
(6) two TextClocks, digits and a separate smaller `a`; (7) a date TextClock.
The root background is transparent. The app's launcher activity shows device
facts and the same layout inflated inside the app as a reference.

#### Run 1: Android 14 emulator (API 34), 22 November 2026

Run for real. Device as in 3.4; launcher `com.google.android.apps.nexuslauncher`
version 14; locale en-US; 12 hour. The widget was placed with adb input (long
press, Widgets, search, drag). Evidence:
`docs/evaluation/evidence/cwp2/emulator-api34-summary.png` and
`docs/evaluation/evidence/cwp2/emulator-api34-home-widget.png`.

| Row | In app reference | Home screen widget |
|---|---|---|
| 1 bundled font, attribute | JetBrains Mono | **Roboto (fallback)** |
| 2 bundled font, style | JetBrains Mono | **Roboto (fallback)** |
| 3 system monospace | monospace | monospace |
| 4 sans-serif-light | light | light |
| 5 spanned am or pm | half size | **half size: the span survives RemoteViews** |
| 6 two clocks | correct | correct |
| 7 date | Sun 22 November | Sun 22 November |
| Shadow (row 2 against row 1) | visible | **visible**: row 2 is darker edged and more legible over the light part of the wallpaper |
| Background | n/a | fully transparent, no launcher backdrop or corner clipping seen |

The picker preview, also drawn by the launcher, showed the same font fallback
and, before `onUpdate` had run, the unspanned XML format on row 5.

Conclusions accepted from run 1, for stock Android 14 only: a bundled font does
**not** reach the launcher by attribute or by style, which matches the source
reading in 4.1; a style shadow does render; a spanned TextClock format does
style the am or pm; transparency is clean. HyperOS and the Pixel launcher on
Android 17 remain to be run.

#### Run 2: Pixel 10 Pro, 22 November 2026

Run for real, after the phone was reconnected (it had dropped off adb during the
first install attempt). Device as in 3.3: Android 17, API 37, build
CP3A.261005.005, launcher `com.google.android.apps.nexuslauncher` version 17,
locale en-GB, 24 hour. Widget placed with adb input on the author's own home
screen, over a dark patterned wallpaper. Evidence:
`docs/evaluation/evidence/cwp2/pixel10pro-api37-summary.png` and
`docs/evaluation/evidence/cwp2/pixel10pro-api37-home-widget.png`.

| Row | In app reference | Home screen widget |
|---|---|---|
| 1 bundled font, attribute | JetBrains Mono | **Roboto (fallback)** |
| 2 bundled font, style | JetBrains Mono | **Roboto (fallback)** |
| 3 system monospace | monospace | monospace |
| 4 sans-serif-light | light | light |
| 5 spanned am or pm | half size | half size |
| 6 two clocks | correct | correct |
| 7 date | Sun 22 November | Sun 22 November |
| Background | n/a | fully transparent, no backdrop or corner clipping seen |

The en-GB locale renders the suffix in lower case (am), where en-US rendered AM.
The shadow could not be judged here because the wallpaper is dark; a light
wallpaper run on the Pixel is still owed for CWP3 and was not done, since it
would change the author's wallpaper.

(Added 22 November 2026: the light wallpaper run was done the same day, after
the wallpaper was changed on the Pixel to a mid teal with a light green pattern.
Evidence: `docs/evaluation/evidence/cwp2/pixel10pro-api37-home-widget-light-wallpaper.png`.
The background stayed fully transparent with no backdrop or corner clipping.
Row 2's shadow gives the text a visibly darker edge against the light green
pattern compared with row 1, which has none. Row 4, `sans-serif-light`, is the
thinnest and least legible line at 15 sp over the busy pattern; the CWD3 font
is fine for the large time digits but small text in it leans hard on the
shadow, which CWE2 should check by eye. The spike was uninstalled afterwards.)

Conclusion accepted from runs 1 and 2: on both stock launchers tested
(Pixel launcher 14 on the API 34 emulator and Pixel launcher 17 on the Pixel 10
Pro), a bundled font never reaches the home screen widget, whether set by
attribute or by style, and the spanned TextClock format works.

#### Run 3: Xiaomi 11T Pro

Not available before delivery (CWD30 as revised). The first evidence will be
the delivered widget in real use, recorded in `FIELD_NOTES.md`.

## 5. CWP3: transparency

Not started; needs both phones. Nothing read from source settles launcher
backdrops or corner clipping.

(Update, 22 November 2026: run on the API 34 emulator over its default light and
dark wallpaper, and on the Pixel 10 Pro over a dark and a light patterned
wallpaper, using the CWP2 spike widget. Fully transparent on both, with no
backdrop, blur or corner clipping added by either launcher. Evidence and the
shadow observations are in 4.4. The Xiaomi is not available before delivery.)

## 6. CWP4: RemoteViews capability check

Read from the RemoteViews reference
(https://developer.android.com/reference/android/widget/RemoteViews) and AOSP
annotations, 22 November 2026. Every customisation in 1.3 has a remotable route,
so **no layout variant per setting is needed**.

| Customisation | Remotable route | API |
|---|---|---|
| Background colour with opacity | `setInt(viewId, "setBackgroundColor", argbColour)` with the alpha in the colour, on a background view | 3 |
| Corner radius | `setViewOutlinePreferredRadius` with `setClipToOutline` in the layout; or a shape drawable background tinted with `setColorStateList(viewId, "setBackgroundTintList", ...)` | 31 |
| Text colour | `setTextColor(viewId, colour)` | 3 |
| Text size | `setTextViewTextSize(viewId, unit, size)` | 16 |
| Clock formats | `setCharSequence(viewId, "setFormat12Hour" or "setFormat24Hour", format)` | 17 |
| Visibility | `setViewVisibility` | 3 |
| Icon tint | `setColorStateList(viewId, "setImageTintList", ...)`: remotability of `ImageView.setImageTintList` is **not yet confirmed** | 31 |
| Shadow | layout or style only; not remotable | n/a |
| Font | layout or style only; see section 4 | n/a |

Size layouts: `RemoteViews(Map<SizeF, RemoteViews>)` is API 31; the launcher
picks the largest layout that fits, with a 1 dp margin, or the smallest if none
fits; at most 16 entries (`MAX_INIT_VIEW_COUNT`). `targetCellWidth` and
`targetCellHeight` replace `minWidth` and `minHeight` on Android 12 and later.
Reconfiguration is `widgetFeatures="reconfigurable"` (API 31), reached by the
user through the launcher's long press menu.

Size bucket thresholds: **open**, settled after the HyperOS cell size is
measured on the Xiaomi during the spike.

### 6.1 Cell sizes and size bucket thresholds, 22 November 2026

Read from source: the Android widget layout guide
(https://developer.android.com/develop/ui/views/appwidgets/layouts) gives, for a
Pixel 4 on a 5 by 4 grid, portrait sizes of (73n minus 16) by (118m minus 16) dp
and landscape sizes of (142n minus 15) by (66m minus 15) dp, and says width
thresholds should come from portrait and height thresholds from landscape. It
also warns that a launcher may not report `OPTION_APPWIDGET_SIZES`.

Run for real: the soak widget (9.1) logs the options the launcher reports. On
the Pixel 10 Pro, a 3 by 1 widget reported `min_dp=276x48`, `max_dp=587x94`, and
exact sizes 276.6 by 94.1 dp (portrait) and 587.8 by 48.4 dp (landscape). So one
Pixel cell is about 92 dp wide and 94 dp tall in portrait, and 48 dp tall in
landscape. The Pixel launcher does report exact sizes.

Not available: HyperOS cell sizes (CWD30). The Xiaomi 11T Pro's screen is about
393 dp wide (1080 pixels at its stock density, which is unconfirmed on the
device), so a 5 column grid gives cells of roughly 70 to 75 dp, and the
reference 5 by 1 widget is roughly 350 to 370 dp wide.

Thresholds proposed, as single named constants in the domain module so tuning
is a one line change, applied to the portrait width and height the launcher
reports:

| Bucket | Rule | Pixel 10 Pro cells | Likely Xiaomi cells |
|---|---|---|---|
| Tall | height at least 150 dp and width below 300 dp | 2 or 3 wide, 2 or more tall | 3 or 4 wide, 2 or more tall |
| Wide | width at least 240 dp, and not Tall | 3 or more wide | 4 or more wide |
| Small | everything else | 1 or 2 wide | 3 or fewer wide |

These are a starting point for CWE4, where text scaling and element hiding make
each bucket fit, judged by eye on the Pixel. Xiaomi tuning comes from field use.
The decision is a domain rule with tests at every boundary (2.4), so changing a
constant later is cheap.

Also observed during this run: reinstalling the soak spike over itself with
`adb install -r` left its WorkManager job registered with JobScheduler (six
matching entries before and after), which is the first evidence for 1.4's
survival across an app update. The next scheduled run confirms it (9.1).

## 7. CWP5: location, permissions and place names

### 7.1 Read from source

- Targeting Android 11 or later, requesting foreground and background location
  together is ignored; background must be requested afterwards, and the user
  grants it on a settings page, not in the dialog
  (https://developer.android.com/develop/sensors-and-location/location/permissions/runtime
  and .../background). The label is read from
  `getBackgroundPermissionOptionLabel()`.
- A WorkManager worker with no visible activity is background, so it needs
  `ACCESS_BACKGROUND_LOCATION` (inferred from the definitions page).
- Approximate location is accurate to about 3 square kilometres. That is enough
  for a forecast and a town name; a district name may be wrong.
- `LocationManager.getCurrentLocation` is API 30 (the LocationRequest overload
  is API 31) and `FUSED_PROVIDER` is API 31, so the platform can supply a fix
  without Google Play services. See CWD25.
- `Geocoder.isPresent()` and the asynchronous `getFromLocation(..., GeocodeListener)`
  (API 33) exist. The documentation does not name the backend; with no backend
  it returns nothing.

### 7.2 Spike

Source: `spikes/plan-location-geocoder` (throwaway, application ID
`fyi.punkdev.thymian.spike.location`), same toolchain as 4.4. It requests
`ACCESS_COARSE_LOCATION` only, then `ACCESS_BACKGROUND_LOCATION` separately,
asks the platform `LocationManager.getCurrentLocation` for one fix (30 second
limit), lists last known fixes per provider, reverse geocodes three fixed
reference points with the asynchronous Geocoder, and runs the same fix from a
platform `JobService` with no activity visible. Coordinates are never logged or
shown; the device's own place name was shown on screen only and is not
recorded here. Run 22 November 2026 on the API 34 emulator and the Pixel 10 Pro
(details in 3.3 and 3.4). Evidence in `docs/evaluation/evidence/cwp5/`.

| Check | API 34 emulator | Pixel 10 Pro |
|---|---|---|
| Foreground dialog | Approximate only; While using the app, Only this time, Don't allow | same three choices |
| Background request | opens the settings page; Allow all the time is the first option | same; the third option reads "Ask every time or allow when you share" |
| `getBackgroundPermissionOptionLabel()` | Allow all the time | Allow all the time |
| Enabled providers once granted | passive, network, fused, gps | passive, network, fused, gps |
| `Geocoder.isPresent()` | true | true |
| Fresh fix, fused, `QUALITY_LOW_POWER` | none within 30 s | none within 30 s |
| Fresh fix, fused, `QUALITY_BALANCED_POWER_ACCURACY` | not run | none within 30 s |
| Fresh fix, network provider, balanced | not run | none within 30 s, foreground and from the background job |
| Last known fix | none (emulator has no network location) | every provider had one, accuracy 2000 m (the approximate location grid), 3 to 4 minutes old; gps 95 minutes old |
| Background job with Allow all the time | ran, no SecurityException, no fix | ran, no SecurityException, no fix |
| Reverse geocode London 51.5074, -0.1278 | locality null, subAdminArea Greater London, adminArea England, GB | identical |
| Reverse geocode Berlin 52.52, 13.405 | locality Berlin, adminArea Berlin, DE | identical |
| Reverse geocode Munich 48.1374, 11.5755 | locality München, subAdminArea Oberbayern, adminArea Bayern, DE | identical |

Conclusions accepted, for these two devices:

- The two step permission flow behaves as documented. The explanation shown
  before the second request is needed because the second step leaves the app for
  a settings page.
- With approximate permission only, the platform `getCurrentLocation` returned
  no fresh fix within 30 seconds on either device, from any provider or quality
  tried, while a last known fix a few minutes old was available on the Pixel.
  The automatic location path therefore has to use the last known fix first and
  treat a fresh fix as a bonus, which the fallback chain in 1.3 already allows.
  This is the most important location finding, and it bears on CWD25.
- The Geocoder works on both devices, but `locality` can be null (London), so the
  place name needs its own fallback order: locality, then subAdminArea, then
  adminArea.
- Place names come back in the local language (München) even with an English
  locale. On the Xiaomi in Germany that means German names, which is probably
  right, but it is worth knowing.

Not established: behaviour on the Xiaomi (not available before delivery);
whether Google Play services' fused location returns fresh approximate fixes
where the platform did not (CWD25); whether a fresh fix arrives with a longer
limit, outdoors, or with Wi-Fi scanning on.

### CWD25, updated 22 November 2026

The spike weakens the case for the platform provider: it gave no fresh
approximate fix on either device. Revised options: platform LocationManager
using the last known fix first and a fresh fix when one comes; or Google Play
services fused location (a dependency, present on the Pixel, and on the Xiaomi
only if its HyperOS build ships Google services, which is likely for a UK
device but unconfirmed). Revised recommendation, labelled as such: platform,
last known first, since a 2 km grid fix minutes old is good enough for weather
and a place name, and no dependency is added. If field use shows stale places,
Play services is the fix.

## 8. CWP6: provider check

Read on 22 November 2026.

### 8.1 Open-Meteo forecast

- Endpoint: `https://api.open-meteo.com/v1/forecast` (https://open-meteo.com/en/docs).
- Parameters to use: `latitude`, `longitude`,
  `current=temperature_2m,weather_code,is_day`, `temperature_unit` (`celsius` or
  `fahrenheit`), `timezone=auto`, `forecast_days=1`.
- `is_day` is 1 in daylight and 0 at night, which drives the day or night icon
  without any sunrise calculation on the device.
- Response, from a real call for Berlin: top level `latitude`, `longitude`,
  `generationtime_ms`, `utc_offset_seconds`, `timezone`,
  `timezone_abbreviation`, `elevation`, `current_units`, and `current` holding
  `time`, `interval`, `temperature_2m`, `weather_code`, `is_day`.
- Errors: HTTP 400 with `{"error": true, "reason": "..."}`, confirmed by a real
  call with an out of range latitude.
- Documented WMO codes: 0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66,
  67, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 97, 99. Codes 96 and 99 come
  only from models with a hail forecast.

### 8.2 Open-Meteo geocoding

- Endpoint: `https://geocoding-api.open-meteo.com/v1/search`
  (https://open-meteo.com/en/docs/geocoding-api); parameters `name`, `count`
  (1 to 100), `language`, `format=json`.
- Searches of one character return nothing; two characters must match exactly.
- Each result has `id`, `name`, `latitude`, `longitude`, `country`, `admin1` and
  more; empty fields are omitted. A search with no match returned HTTP 200 with
  no `results` key at all.
- Location data comes from GeoNames, which is CC BY and needs crediting too.

### 8.3 Terms

- Free tier: no API key, non-commercial only, under 10,000 calls a day, 5,000 an
  hour, 600 a minute, 300,000 a month (https://open-meteo.com/en/terms,
  /en/pricing). One widget at 30 minute intervals is 48 calls a day, plus taps.
- Attribution (https://open-meteo.com/en/licence): data under CC BY 4.0; the
  page asks for a link next to any location where Open-Meteo data is shown, for
  example a link reading Weather data by Open-Meteo.com. The widget shows the
  data, but the specification places attribution on the settings screen only.
  See CWD21.

### 8.4 Meteocons

- MIT, copyright line "Copyright (c) 2020-present Bas Milius". The repository
  moved to https://github.com/basmilius/meteocons.
- Latest stable release v2.0.0 (6 May 2021). Current line 3.0.0-next.10
  (12 April 2026), a pre-release, published as `@meteocons/svg-static`. See CWD22.
- Four styles: fill, flat, line, monochrome. Formats: animated SVG, static SVG,
  Lottie.
- Day and night variants exist for clear, mostly clear, partly cloudy,
  overcast, fog, haze, thunderstorms and extreme. **Drizzle, rain, snow, sleet,
  hail, mist, smoke and cloudy have no plain day or night variant**; day and
  night exist only in compounds such as `partly-cloudy-day-rain`. The
  specification requires distinct day and night icons for every condition. See
  CWD23.
- Conversion: the fill style uses gradients everywhere and masks on fog; the
  flat, line and monochrome styles use masks on most compound icons. Android
  VectorDrawable has clip paths but no masks, so the icons need flattening
  before conversion. See CWD24.

### 8.5 JetBrains Mono

SIL Open Font Licence 1.1, copyright line "Copyright 2020 The JetBrains Mono
Project Authors (https://github.com/JetBrains/JetBrainsMono)". Latest release
v2.304 (14 January 2023) with static TTFs from Thin to ExtraBold. The reference
time digits are light weight, so Light or ExtraLight is the likely match.

## 8.6 App and widget icon

Direction given 22 November 2026: Thymian uses the icon of Thyme
(https://github.com/TheScottBot/Thyme, local copy at
`/Users/wash/Documents/Repo/Thyme`, commit
01823cb9e7a296d501131b3958fae30871fc4b8c, read 22 November 2026).

Read from source: Thyme ships no icon file. `internal/render/appicon.go` draws it
in code: a rounded square tile (82.4 percent of the canvas, corner radius 22
percent of the tile) filled with `PanelFill` `#12131C`, a heavy outline (tile/16)
in `Outline` `#F59EF3`, and a clock face ring (radius 27 percent of the tile,
stroke 8 percent) with a minute hand pointing up and an hour hand pointing to
three o'clock, both in `TimeText` `#F894F8` with rounded ends. Colours from
`internal/theme/theme.go`. Golden renders at 16, 32, 128 and 256 pixels are in
`internal/render/testdata/golden/`. Thyme is MIT licensed, copyright 2026
TheScottBot, the same holder and licence as Thymian.

Plan for CWE7: reproduce the same geometry and colours as an Android vector
drawable rather than copying a raster, so it stays sharp at every density. An
Android adaptive icon is masked by the launcher to its own shape, so the dark
panel becomes the background layer and the ring and hands the foreground layer;
the pink outline is likely lost to the mask and may be drawn as an inner ring
instead. The widget picker shows the app icon unless the widget declares its own,
so one icon covers both. Credit to Thyme goes in NOTICE.

## 9. CWP7: background behaviour

From source: periodic WorkManager work has a 15 minute minimum interval and is
rescheduled across reboots
(https://developer.android.com/reference/androidx/work/PeriodicWorkRequest,
https://developer.android.com/develop/background-work/background-tasks/persistent).
Survival across an app update is not stated by that page. WorkManager 2.12.0 is
the latest stable (23 September 2026). The Xiaomi and Pixel observations are not
started: they need the phones and, on the Xiaomi, at least 24 hours each under
default settings and under Autostart with No restrictions. Those observations
are author gates.

### 9.1 Pixel soak

Source: `spikes/plan-background-refresh` (throwaway, application ID
`fyi.punkdev.thymian.spike.background`), WorkManager 2.12.0 (`work-runtime`,
the latest stable on Google Maven, read 22 November 2026). A
`PeriodicWorkRequest` every 30 minutes with a connected network constraint,
enqueued with `KEEP`, as the real refresh will be. Each run appends one line to a
capped log in app storage: local time, minutes since the previous run, app
standby bucket, power save mode, device idle mode and attempt count. A small
widget shows the latest run, since the real app always has a placed widget and
that may affect its standby bucket.

Started 22 November 2026 at 12:17 on the Pixel 10 Pro (3.3) under default
battery settings: Adaptive Battery on (`adaptive_battery_management_enabled=1`),
Doze enabled, the app not exempted. The first run happened at 12:17, as soon as
the work was enqueued, in the active bucket. WorkManager registered its job with
JobScheduler (`androidx.work.systemjobscheduler`).

Read back after 24 hours, ending no earlier than 23 November 2026 12:17, with:

`adb -s 61310DLCH000ZB shell run-as fyi.punkdev.thymian.spike.background cat files/soak.log`

Results: pending. Reboot survival is not yet tested; it needs the Pixel
rebooted during the soak.

Found while placing the widget: with no `previewLayout` or `previewImage`, the
Pixel widget picker shows a generic tile with the app icon, so the real widget
needs its own preview for the picker (CWE1 or CWE7).

Android 14 rule affecting the alarm tap: a mutable PendingIntent with an implicit
intent throws, so the clock tap uses an immutable PendingIntent.
`ACTION_SHOW_ALARMS` needs no permission and `startActivity` needs no package
visibility declaration.

## 10. Questions for the author

Batched for the Plan stage, in the five part form of datum 0.3. New decisions
continue the specification's numbering from CWD14.

### 10.0 Answers, 22 November 2026

Every recommendation below was accepted on 22 November 2026, with CWD3 taking
the revised recommendation. The questions are kept as asked, as history.

| Decision | Answer |
|---|---|
| CWD3 | System `sans-serif-light` for all widget text; JetBrains Mono kept open as a later field experiment through the shared style |
| CWD14 | Plan record is this file only; the specification is amended to match |
| CWD15 | Specification opening names datum version 2 and no Part 4 modules; Part 0 reduced to a datum reference plus project rules |
| CWD16 | (DEPENDENCIES.md, IMPLEMENTATION_DEVIATIONS.md, HARDWARE_COMPATIBILITY.md and FIELD_NOTES.md live in `docs/`, the rest at the root, as directed 22 November 2026.) README.md, TESTING.md, IMPLEMENTATION_DEVIATIONS.md, CHANGELOG.md, LICENSE, NOTICE, DEPENDENCIES.md, PRIVACY.md, SECURITY.md, HARDWARE_COMPATIBILITY.md, FIELD_NOTES.md (CWD30) and this record |
| CWD17 | Datum typography scope, mechanically checked and proven against planted violations, vendored font and icon paths excluded by explicit list |
| CWD18 | Builds installed and screenshots taken over adb; appearance, TalkBack and soak judgements, and every gate row, belong to the owner of the project |
| CWD19 | Responses over 64 KiB refused before parsing; used fields required and range checked; unknown fields tolerated and counted; recorded in IMPLEMENTATION_DEVIATIONS.md |
| CWD20 | Memory analysis not applicable (Kotlin on the JVM); a seeded mutation test over recorded fixtures in plain JUnit on every push |
| CWD21 | Attribution on the settings screen beside the live preview, crediting Open-Meteo and GeoNames with links, and in the README |
| CWD22 | Meteocons 3.0.0-next.10, pinned by version and per file digests |
| CWD23 | Monochrome style tinted to the text colour; precipitation uses the `partly-cloudy-day-` and `partly-cloudy-night-` compounds |
| CWD24 | Original SVGs vendored verbatim; one time conversion with a pinned tool recorded in DEPENDENCIES.md; a check that every condition has its drawables |
| CWD25 | Platform LocationManager, last known fix first, fresh fix when one arrives |
| CWD26 | ktlint, Android lint and Kotlin warnings as errors |
| CWD27 | Closed: the SDK was installed (3.4) |
| CWD28 | `/Users/wash/Keystores/thymian-release.jks`; passwords read from the environment variables `THYMIAN_KEYSTORE_PASSWORD` and `THYMIAN_KEY_PASSWORD` (answered 22 November 2026); key alias `thymian`, a PrivateKeyEntry created 22 November 2026, as listed by `keytool -list` |
| CWD29 | Specification added as `docs/SPECIFICATION.md` |
| CWD30 | No Xiaomi testing before delivery; emulator added; field use recorded in FIELD_NOTES.md |
| CWD31 | Bounded in app event history and a Copy diagnostics button, added to scope |
| CWD32 | The Xiaomi only receives release builds signed with the release keystore |
| CWD33 | App and widget icon is Thyme's icon (8.6), decided when first raised |
| CWD34 | Date line keeps the locale's punctuation, including the comma on API 34 en-GB (section 13) |
| CWD35 | Corner radius keeps 0 to 32 dp, with a note under the slider that the home screen may round corners further (section 14) |
| CWD36 | One location for the whole app; temperature unit stays per widget (section 16) |

### CWD3. Font rendering approach (still blocked on the CWP2 spike)

1. Blocked: how widget text gets its typeface.
2. Why not settled: AOSP source strongly suggests a bundled font does not apply
   in the launcher, but HyperOS may differ, and the device spike has not run.
3. Options, to be confirmed by the spike:
   A. Use a system family (`monospace`, which is not JetBrains Mono) everywhere.
   B. TextClock in a system family for the time and date; place name and
   temperature drawn into bitmaps with JetBrains Mono at each refresh.
   C. Everything as bitmaps in JetBrains Mono, which needs an update every minute
   and so breaks the rule against alarms and background work for the clock.
   D. Bundled font in XML, if the spike shows HyperOS honours it, accepting a
   different font on the Pixel.
4. Trade offs: A is simplest and consistent but loses the font. B mixes two
   fonts in one widget and adds a rendering path plus bitmap memory limits on
   Android 17. C contradicts 1.3. D makes the Pixel unreliable as evidence.
5. Recommendation (not a choice; the specification reserves this for you): wait
   for the spike; if bundled fonts fail on both launchers, A with
   `sans-serif-light` or `monospace`, whichever looks closer on the Xiaomi.

**Update, 22 November 2026, after spike runs 1 and 2 and the revision of
CWD30.** The bundled font fails on both stock launchers tested, and no HyperOS
result will exist before delivery. Revised options:

   A. System family everywhere (`sans-serif-light` for the time, matching the
   light reference digits, or `monospace`). Identical on every launcher; the
   settings preview matches the widget exactly. Not JetBrains Mono.
   B. TextClock in a system family; place name and temperature drawn as
   JetBrains Mono bitmaps at each refresh. Two typefaces in one widget, an extra
   rendering path to keep DRY, and Android 17 bitmap memory caps.
   D. Declare the bundled font in the shared style anyway. Where a launcher
   honours it (HyperOS, unknown) the widget shows JetBrains Mono; elsewhere it
   falls back to Roboto, as seen on the Pixel. Costs nothing, but the settings
   preview (in app, where the font loads) disagrees with the Pixel widget, and
   the Xiaomi look is unpredictable until seen in use.
   E. A with a later switch to JetBrains Mono if field use ever shows HyperOS
   honours it, by a one line style change.

Revised recommendation, labelled as such: **A with `sans-serif-light`**, so the
delivered build looks the same on every launcher and the preview is truthful;
keep E open as a field experiment. C remains excluded by 1.3.

### CWD14. Name of the Plan record

1. Blocked: whether the Plan record is PLAN_FINDINGS.md or the datum path.
2. Why: the two documents name different files and the datum governs.
3. Options: datum path only; both files; spec name only.
4. Trade offs: two files drift; the spec name departs from the datum.
5. Recommendation: datum path only (this file); amend the specification.

### CWD15. Specification opening and restated rules

1. Blocked: the specification does not name the datum version or Part 4
   modules, and restates datum rules in Part 0.
2. Why: the datum requires the first and forbids the second; only you can amend
   the specification.
3. Options: amend the specification; or leave it and record both as deviations.
4. Trade offs: restated rules already differ from the datum (M5), which is the
   drift the datum warns about.
5. Recommendation: amend the opening to say "applies datum version 2,
   20 September 2026; applies no Part 4 modules" (Kotlin is memory safe and
   there is one product) and replace Part 0 with a reference to the datum,
   keeping only the project rules (CWD8 keystore, UK spelling is already in the
   datum).

### CWD16. Required documents

1. Blocked: which documents the repository carries.
2. Why: the specification lists fewer than the datum, and spells LICENCE.
3. Options: datum set plus the specification's extras; specification set only.
4. Trade offs: the datum set adds files but each has a defined job.
5. Recommendation: README.md, TESTING.md, IMPLEMENTATION_DEVIATIONS.md,
   CHANGELOG.md, LICENSE (the datum spelling, which GitHub also detects), NOTICE,
   DEPENDENCIES.md, PRIVACY.md, SECURITY.md, HARDWARE_COMPATIBILITY.md and this
   record.

### CWD17. Typography check scope

1. Blocked: what the CI typography check enforces.
2. Why: the specification bans em dashes only; the datum also bans en dashes,
   double and triple hyphen runs and quotation marks used for emphasis.
3. Options: datum scope; specification scope.
4. Trade offs: quotation marks for emphasis cannot be detected mechanically;
   that part is review only.
5. Recommendation: datum scope for the mechanical checks, proven against planted
   violations, excluding the vendored font and icon directories by explicit list.

### CWD18. Who runs the device checks

1. Blocked: who performs and records the Done when checks on the phones.
2. Why: the specification gives them to the agent; the datum makes them author
   gates the agent must not claim.
3. Options: you perform them and add HARDWARE_COMPATIBILITY.md rows; the agent
   drives the phones over adb and you confirm by eye.
4. Trade offs: the second is faster, but the gate is still yours to clear.
5. Recommendation: the agent installs builds and captures screenshots over adb;
   you judge appearance, TalkBack and the 24 hour soak, and only you add a row
   saying a gate passed.

### CWD19. Untrusted input rules for Open-Meteo responses

1. Blocked: how strictly the parser treats provider responses.
2. Why: datum 0.10 says reject unknown fields, but Open-Meteo is a third party
   contract that returns metadata fields we do not use and may add more.
3. Options: strict allow list of every field seen today; tolerate unknown fields
   while requiring and bounding the ones used.
4. Trade offs: strict means a harmless provider addition blanks the weather until
   a new build is sideloaded. Tolerant departs from 0.10.
5. Recommendation: refuse any body over 64 KiB before parsing; require and range
   check the fields used; tolerate and count unknown fields, logging their count;
   record the departure in IMPLEMENTATION_DEVIATIONS.md.

### CWD20. Memory analysis and fuzzing

1. Blocked: how 0.10's memory analysis and the fuzzing guidance apply.
2. Why: Kotlin on the JVM is memory safe and has no sanitiser; JVM fuzzing needs
   a dependency such as Jazzer.
3. Options: record memory analysis as not applicable; add Jazzer; or a seeded
   mutation test of the parser in plain JUnit.
4. Trade offs: Jazzer is another dependency and CI cost; a seeded test is weaker
   but free.
5. Recommendation: not applicable for memory analysis, recorded; a seeded
   mutation test over recorded fixtures in plain JUnit, run on every push.

### CWD21. Where the Open-Meteo attribution appears

1. Blocked: placement of the CC BY 4.0 credit.
2. Why: the licence page asks for a link next to where the data is shown; the
   specification puts it on the settings screen only.
3. Options: settings screen only; settings screen plus a small credit on the
   widget; settings screen beside the live preview.
4. Trade offs: anything on the widget breaks the reference look. Settings only
   may fall short of the provider's wording. I cannot give a legal view.
5. Recommendation: settings screen beside the live preview, crediting both
   Open-Meteo and GeoNames with links, plus README; record it as a decision of
   yours.

### CWD22. Meteocons version

1. Blocked: which Meteocons release to vendor.
2. Why: the only stable release is from 2021; the current line is a pre-release.
3. Options: v2.0.0; 3.0.0-next.10 pinned.
4. Trade offs: v2 is stable but older; next.10 has the fuller day and night set
   we inspected. Vendored files never change under us either way.
5. Recommendation: 3.0.0-next.10, pinned by version and file digests.

### CWD23. Meteocons style and day and night coverage

1. Blocked: icon style, and how precipitation gets distinct day and night icons.
2. Why: plain drizzle, rain, snow, sleet and hail have no day or night variant.
3. Options: use `partly-cloudy-day-` and `partly-cloudy-night-` compounds for
   precipitation; use `overcast-day-` and `overcast-night-` compounds; accept a
   single icon for those conditions as a deviation.
4. Trade offs: compounds show a sun or moon in rain, which is less literal;
   single icons break 1.3.
5. Recommendation: monochrome style, tinted to the text colour so icons stay
   white like the reference, with `partly-cloudy-day-` and `-night-` compounds
   for precipitation.

### CWD24. Converting icons to Android drawables

1. Blocked: how SVGs with masks become Android drawables.
2. Why: VectorDrawable has no mask support.
3. Options: a one time conversion with a pinned tool (for example Inkscape on
   the command line to flatten masks, then Android Studio's importer); PNG
   rasters per density.
4. Trade offs: conversion needs a tool on your Mac and a recorded invocation;
   PNGs are simple but cannot be tinted as cleanly and cost memory.
5. Recommendation: vendor the original SVGs verbatim, convert once with a pinned
   tool recorded in DEPENDENCIES.md, and add a check that the drawables list
   matches the condition table.

### CWD25. Location provider

1. Blocked: platform LocationManager or Google Play services Fused Location.
2. Why: both work; the specification asks for minimal dependencies.
3. Options: platform `getCurrentLocation` with `FUSED_PROVIDER`; Play services.
4. Trade offs: Play services is a dependency and is present on both phones;
   platform needs no dependency.
5. Recommendation: platform LocationManager, confirmed in the CWP5 spike.

### CWD26. Kotlin formatting and static analysis

1. Blocked: which formatter and analysers enforce datum 0.5 and 0.10.
2. Why: none is installed; each is a build dependency.
3. Options: ktlint; ktfmt; detekt in addition.
4. Trade offs: ktlint follows the official Kotlin style and is the smallest;
   detekt adds deeper checks and configuration.
5. Recommendation: ktlint plus Android lint and Kotlin warnings as errors.

### CWD27. Installing the Android SDK

1. Blocked: every device spike and every build.
2. Why: no SDK exists at the configured `ANDROID_HOME`.
3. Options: you run Android Studio's setup wizard; I install the command line
   tools with Homebrew and use `sdkmanager`.
4. Trade offs: the wizard is one click for you and puts the SDK where
   `ANDROID_HOME` already points; Homebrew is scriptable but installs software
   on your Mac on my say so.
5. Recommendation: you run the wizard once, then I install platform 37 and
   build tools 36.0.0 with `sdkmanager` and record the versions.

### CWD30. Testing on the Xiaomi without access to it

Raised 22 November 2026. The author reports that the Xiaomi 11T Pro and its user
are in Germany, so only the Pixel 10 Pro can be connected to the development Mac.

1. Blocked: CWP2, CWP3, CWP5 and CWP7 on the Xiaomi, and every Xiaomi Done when
   check, while the specification makes the Xiaomi the acceptance device.
2. Why: nothing can be installed or observed on the Xiaomi over adb, and no
   emulator reproduces the HyperOS launcher.
3. Options:
   A. The phone's user installs each spike or build from a file (the CI debug
   APK, or one the author sends) and returns screenshots and the diagnostics
   section; the author records the results.
   B. Pause every Xiaomi item until the phone is back with the author.
   C. Add a stock Android 14 emulator for API 34 evidence alongside the Pixel,
   while A or B covers HyperOS.
4. Trade offs: A depends on the phone's user installing an APK from outside the
   Play Store and on screenshots instead of adb, and the 24 hour soak becomes
   their observation. B keeps the evidence clean but stalls CWD3, which needs
   both launchers. C is cheap and catches API 34 differences, but says nothing
   about HyperOS.
5. Recommendation: run the Pixel spikes now, add C, and use A for the Xiaomi:
   each spike shows an on screen summary of what it found, so one screenshot
   carries the result. Xiaomi gates stay open until that evidence arrives.

**Answered by the author on 22 November 2026: A and C.** The Xiaomi's user
installs builds from a file and returns screenshots; a stock Android 14 (API 34)
emulator is added for API level evidence. Consequences for this plan: every
spike shows an on screen summary of its findings so one screenshot carries the
result; Xiaomi evidence is recorded as reported by the author, in those words,
per datum Part 3; the emulator never stands in for a HyperOS result.

**Revised by the author later on 22 November 2026: option A is withdrawn.** The
phone's user will not install test builds or send screenshots. The author's
direction is to deliver a build that mostly works and then fix snags found in
real use. Consequences:

- No HyperOS evidence will exist before delivery. Every Xiaomi item in CWP2,
  CWP3, CWP5 and CWP7, and every Xiaomi Done when check, is marked **not
  available before delivery** rather than pending. These author gates stay open;
  none is claimed.
- Option C (the API 34 emulator) stands, and the Pixel stays the development
  device.
- Real use on the Xiaomi becomes the evidence, recorded in `FIELD_NOTES.md`
  (datum Part 2: when there is real world use), dated, in the author's words.
- The design must be defensive where HyperOS is unknown: a font approach that
  looks right whether or not HyperOS honours bundled fonts (CWD3), the
  HyperOS guidance screen, and diagnostics good enough to explain a snag
  without adb (CWD31).
- Each delivered build must install over the previous one without losing
  settings, which constrains signing (CWD32).

Related, to confirm with the phone's user: the device language and region.
Dates follow the device locale (CWD13), so a phone set to German shows a German
date, not the UK form the specification uses as its example. Location will
also be in Germany, which needs nothing special from Open-Meteo.

### CWD31. Diagnosing snags without adb

1. Blocked: how a snag on the Xiaomi gets diagnosed when nobody can read
   logcat from it.
2. Why: CWD30 as revised rules out adb and test builds on the Xiaomi; the
   specification's diagnostics section shows only the latest refresh, error,
   location source and next refresh.
3. Options: specification diagnostics only; add a bounded in app event history
   (for example the last 50 refresh outcomes and fallbacks, each with time and
   reason category, no coordinates or place names) with a button that copies it
   as text for the phone's user to paste into a message.
4. Trade offs: the history is new scope and a small storage cost, but it turns
   "the weather is wrong" into a timeline. It holds no personal data under datum
   0.15. Copying needs the phone's user to tap one button and paste.
5. Recommendation: add the bounded history and the copy button, recorded as an
   addition approved by you.

### CWD32. Signing of delivered builds

1. Blocked: which build is sent to the Xiaomi.
2. Why: Android refuses to install an update signed with a different key. The
   CI debug APK is signed with the CI runner's own debug key, and a local debug
   build with this Mac's, so switching between them, or to release, forces an
   uninstall that deletes the widget's settings.
3. Options: always send release builds signed with
   `/Users/wash/Keystores/thymian-release.jks` from your Mac; send CI debug
   builds only; give CI a fixed debug key.
4. Trade offs: release builds keep updates seamless and match 2.6. CI debug
   builds are convenient but each runner may sign differently. A fixed CI debug
   key is a secret in GitHub, which 2.6 does not cover.
5. Recommendation: the Xiaomi only ever receives release builds from your Mac;
   the CI debug APK is for the Pixel and checking.

### CWD29. Where the specification lives

1. Blocked: the specification is not in the repository.
2. Why: the record and the datum refer to it as the governing document.
3. Options: add it verbatim as `docs/SPECIFICATION.md`; keep it outside.
4. Trade offs: inside, it is versioned with the work.
5. Recommendation: I add it verbatim; you commit it.

### CWD28. Release keystore path

Answered by the author on 22 November 2026: `/Users/wash/Keystores/thymian-release.jks`.
Observed the same day: the file exists, 4330 bytes, mode 600, outside the
repository. Still to ask with the CWE1 batch: the key alias, and the names of
the Gradle properties or environment variables that will hold the passwords.
The author keeps the keystore password in Dashlane and has installed the
Dashlane command line tool. Option for CWE1, to be checked against the Dashlane
CLI documentation before use: the release build reads the passwords from
environment variables, which the author populates from Dashlane at release time,
so no password is written to disk or seen by the agent.

## 11. Appendix A status

| Claim | Status |
|---|---|
| No runtime typeface setter; XML font may or may not render | Confirmed for the setter. XML bundled font: refuted on stock Android, falls back to Roboto on the API 34 emulator and the Pixel (4.4 runs 1 and 2); HyperOS pending |
| Style shadow renders on both launchers | Confirmed on the API 34 emulator and the Pixel (light wallpaper run, 4.4); HyperOS not available before delivery |
| HyperOS adds no backdrop | Not available before delivery (CWD30); first evidence will be field use |
| Pixel clips corners | Refuted for a transparent widget: no clipping seen on the Pixel or the emulator (5); clipping of a coloured background with a corner radius is checked in CWE3 |
| Shadow not changeable at runtime | Confirmed from source |
| TextClock self updates, date at midnight, no app process | Confirmed from source (inferred); device check in CWE2 |
| Responsive layouts and reconfiguration on API 34 | Confirmed: both API 31 |
| Background location needs a separate later request | Confirmed in source and on the emulator and Pixel (7.2) |
| Geocoder present and correct on both phones | Confirmed on the emulator and Pixel, with locality sometimes null (7.2); Xiaomi not available before delivery |
| WorkManager minimum 15 minutes | Confirmed |
| HyperOS needs Autostart and No restrictions | Pending 24 hour observations |
| Open-Meteo free tier terms | Confirmed, with the monthly limit added and the attribution placement raised as CWD21 |
| Meteocons MIT with day and night for every condition | Licence confirmed; coverage refuted for plain precipitation, see CWD23 |
| JetBrains Mono under OFL 1.1 and bundleable | Confirmed |
| HyperOS grid 4 or 5 columns | Pending spike |
| targetSdk 35 or later forces edge to edge | Confirmed, and the opt out is gone from 36 |
| Pixel 10 Pro runs Android 16 or later | Confirmed on the phone: Android 17, API 37 (37.1), build CP3A.261005.005 |
| AlarmClock show alarms works on both phones | API confirmed; device check in CWE6 |

## 12. CWE1 progress notes

22 November 2026. Debug build of the empty widget (batch 3) installed on the
API 34 emulator and the Pixel 10 Pro. On the emulator it was found in the picker
with the Thyme icon, its preview and the description, and placed with adb input
(evidence in `docs/evaluation/evidence/cwe1/`). On the Pixel it is registered
with the widget service (`dumpsys appwidget` lists
`fyi.punkdev.thymian/fyi.punkdev.thymian.widget.ThymianWidgetProvider`).
Placement on the Pixel, in the owner's words: "All fine, placed it." Nothing
else was written down for that gate.

During the Pixel attempt a run of adb taps was sent without a fresh screenshot
after the home screen layout had changed; a long press put the owner's weather
widget into resize mode. The owner confirmed afterwards that nothing had moved.
From then on every adb tap on the Pixel follows a fresh screenshot.

## 13. CWE2 notes

22 November 2026. Clock block built and checked on the API 34 emulator (evidence
in `docs/evaluation/evidence/cwe2/`): it ticks across a minute boundary with no
app code running, follows the device switching to 24 hour time (digits become
`HH:mm` and the suffix empties), and follows a time zone change at once. The
date on en-US reads "Sun, November 22".

Found: Android's en-GB best pattern for `EEEdMMMM` differs by version, with a
comma on API 34 (`EEE, d MMMM`, "Sun, 22 November") and none on the Pixel at
API 37 (`EEE d MMMM`, "Sun 22 November"). The Xiaomi runs API 34, so it will
most likely show the comma, while the specification's example "Tue 11 July" has
none. Raised as CWD34.

Found during the same session, for the CWP7 soak (9.1): at 13:11 the second run
had not happened; JobScheduler showed the connectivity constraint unsatisfied
while the Pixel was on mobile data without Wi-Fi.

### CWD34. Comma in the date line

1. Blocked: whether the date line keeps Android's locale punctuation.
2. Why: CWD13 says follow the locale, and the en-GB locale data on API 34 has a
   comma after the weekday, while the specification's example has none and the
   Pixel's newer data has none.
3. Options: follow the locale exactly (comma on the Xiaomi); remove a comma
   that directly follows the weekday for every locale; follow the locale but
   remove that comma for English locales only.
4. Trade offs: exact locale data is simplest and honest to CWD13 but differs
   between the two phones. Removing the comma everywhere is one small rule in
   the domain, tested, but edits other languages' conventions too. English only
   keeps other locales untouched at the cost of a locale check.
5. Recommendation: follow the locale exactly, as CWD13 decided; the comma is
   the Xiaomi's own convention for British English.

**Answered 22 November 2026: keep the comma.** The date line follows the device
locale exactly, as CWD13 decided, so the Xiaomi on API 34 will most likely read
"Sun, 22 November".

CWE2 Done when, 22 November 2026:

| Check | Pixel 10 Pro | API 34 emulator | Xiaomi 11T Pro |
|---|---|---|---|
| Matches the reference layout | Owner, by eye: "Looks good" | Agent screenshot | Not available before delivery |
| Ticks across a minute boundary | Not separately recorded | Seen, 13:10 to 13:11 | Not available |
| Survives a time zone change | Not run (owner's phone settings left alone) | Seen, London to Berlin | Not available |
| Switches between 12 and 24 hour | Not run on the device; forced modes covered by on device tests | Seen, device setting 12 to 24 | Not available |

(Correction, 22 November 2026: section 12 records the owner's CWE1 words "All
fine, placed it" for the Pixel, but during this check the owner said no
Thymian widget had been placed and placed one then. See
`docs/HARDWARE_COMPATIBILITY.md`.)

## 14. CWE3 notes

22 November 2026, on the API 34 emulator (Pixel launcher 14), evidence in
`docs/evaluation/evidence/cwe3/`:

- Adding the widget opens the settings screen, starting from the defaults.
- Two placed widgets held different settings (blue background at 60 percent
  following the device clock; transparent with pink text in 24 hour time).
- The launcher's reconfigure action (the pencil shown because the widget
  declares `reconfigurable`) reopened the first widget's settings with its saved
  values; hiding its date and saving changed only that widget.
- Found: with corner radius set to 0, the preview (in the app) was square but
  the launcher still drew rounded corners. The Pixel launcher clips widgets to
  its own system radius, so radii below it have no visible effect there.
  Appendix A's claim that "the Pixel launcher clips widget corners to a system
  radius" is confirmed for a coloured background. Raised as CWD35.

CWE3 Done when (two widgets on each phone hold different appearances and
changing one does not affect the other): emulator, seen by the agent; Pixel,
owed to the owner; Xiaomi, not available before delivery.
(Pixel result, 22 November 2026, in the owner's words: "two clock on pixel
works great".)

### CWD35. Corner radius below the launcher's own

1. Blocked: what the corner radius setting promises.
2. Why: the Pixel launcher rounds every widget to its system radius, so a
   setting below that (0 to roughly 16 dp) shows no change on the home screen,
   though the preview does. HyperOS behaviour is unknown.
3. Options: keep 0 to 32 dp and accept that small values only show on
   launchers that do not clip; raise the minimum to the system radius; label the
   setting so it is clear the launcher may round corners anyway.
4. Trade offs: keeping the range is honest on launchers that do not clip,
   possibly HyperOS. Raising the minimum hides an option that may work on the
   Xiaomi. A note in the label costs nothing.
5. Recommendation: keep 0 to 32 dp and add a short line under the slider saying
   the home screen may round corners further.

**Answered 22 November 2026: keep 0 to 32 dp and add a note under the slider.**

## 15. CWE4 notes

22 November 2026. The renderer builds Android's responsive RemoteViews tree,
one layout per size the launcher reports, falling back to the portrait and
landscape bounds when exact sizes are missing. On the API 34 emulator the widget
was resized through 4 by 1, 2 by 1, 2 by 2 and 4 by 2 (evidence in
`docs/evaluation/evidence/cwe4/`): each size re-rendered (`reason=resized` in
the log), the expected layout was used, the clock text scaled with the space,
and nothing clipped or overlapped. The estimated font ratios in
`SizeLayoutPolicy` (marked as estimates in the code) held at every size tried;
they stay as they are until a size shows clipping.

The weather block is in the Wide and Tall layouts but hidden until CWE5 gives it
data, so the Tall layout currently centres the clock alone.

CWE4 Done when (dragging through every size never clips, overlaps or leaves text
unreadably small): emulator, seen by the agent; Pixel, owed to the owner; Xiaomi,
not available before delivery.
(Pixel result, 22 November 2026, in the owner's words: "Looks fine".)

## 16. CWE5 notes

22 November 2026, batch A (domain only): the weather condition table for every
documented WMO code with its Meteocons day and night icons (all 23 icon names
confirmed present in the 3.0.0-next.10 monochrome set), temperature formatting
from one Celsius value per refresh, and the location fallback chain with a
recent last known fix used as it is (CWD25).

### CWD36. One location for all widgets, or one per widget

1. Blocked: whether automatic or fixed location is chosen once for the app or
   separately for each widget.
2. Why: specification 1.3 keeps appearance settings per widget, but says nothing
   about location being per widget, and CWE3 left location settings to CWE5.
3. Options: one location for the whole app, set from any widget's settings
   screen; or a location per widget, so two widgets can show two places.
4. Trade offs: one location means one weather request per refresh for every
   widget and one permission flow. Per widget allows a second city but
   multiplies requests and complicates the fallback and diagnostics.
5. Recommendation: one location for the whole app; temperature unit stays per
   widget, since conversion is local.

**Answered 22 November 2026: one location for the whole app.**

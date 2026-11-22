# Thymian: Transparent Clock and Weather Widget for Android

A specification for handoff to a coding agent.

| | |
|---|---|
| Project name | Thymian |
| App display name | Thymian |
| Application ID | fyi.punkdev.thymian |
| Repository | https://github.com/TheScottBot/Thymian |
| Licence | MIT |

Revision of 22 November 2026, applying the Plan stage decisions CWD3, CWD14,
CWD15, CWD16 and CWD33 recorded in
`docs/evaluation/ACTUAL_CONTRACT_EVALUATION.md` section 10.0: the datum version
and modules are named, Part 0 refers to the datum instead of restating it, the
Plan record path and licence file name follow the datum, the font requirement
follows CWD3, and the decision record is completed.

## Governing standard

This specification applies ENGINEERING_DATUM.md **version 2, dated 20 September
2026**, at /Users/wash/Documents/Repo/ENGINEERING_DATUM.md, as its governing
engineering standard.

It applies **no Part 4 modules**: Kotlin is memory safe, so module A does not
apply, and the repository holds one product, so module B does not apply.

Beyond the datum, its subject requires what Parts 0 to 6 below state.

**MUST** read ENGINEERING_DATUM.md in full before reading the rest of this document.

**MUST** treat ENGINEERING_DATUM.md as authoritative wherever it and this document disagree.

**MUST** list every disagreement between this document and ENGINEERING_DATUM.md in the Plan record, `docs/evaluation/ACTUAL_CONTRACT_EVALUATION.md`, and raise each one as a question to the author. This document was written without the datum file open, so mismatches are expected and are not to be resolved by guessing.

## How to read this document

Read it top to bottom once, then work the stages in order: Plan, Execute, Verify.

| Tag | Meaning |
|---|---|
| **MUST** | Mandatory. Failing it fails the phase. |
| **MUST NOT** | Prohibited. |
| **DECIDE** | A blocking decision belonging to the author. Stop and ask. Never guess. |
| **GUIDANCE** | A recommendation with reasoning. You may depart from it; say so and say why. |
| **MAY** | Permitted, not required. |

Untagged text is context. It explains why a rule exists; it is not itself a rule.

Identifier prefixes: CWP for Plan work, CWE for Execute phases, CWV for Verify, CWD for decisions. Part 6 records each decision and where it is applied.

### What this document is not

No source code of Android, the HyperOS or Pixel launchers, AndroidX, or any Open-Meteo client was read while writing this. Every claim about platform or launcher behaviour is a question for the Plan stage. All such claims are listed in Appendix A and none of them is a contract.

# Part 0: Absolute rules

Every rule in ENGINEERING_DATUM.md Part 0 applies, including its consolidated list in 0.17. They are not restated here. This project adds:

**MUST** keep code DRY and SOLID to a high bar. Duplicated layouts, near-identical classes, or copy-pasted rendering paths fail review regardless of whether they work.

**MUST** record every third-party dependency, font and icon set in DEPENDENCIES.md with its licence and the reason it is needed.

**MUST** log every refresh, failure and fallback with enough context to diagnose it from logcat alone, within the datum's ban on logging personal data.

**MUST NOT** place a signing keystore, keystore password or key password anywhere inside the repository (CWD8).

# Part 1: Product

## 1.1 Problem

A Xiaomi system update removed transparency from the stock HyperOS clock and weather widget. Thymian replaces it with a widget that looks like the old one, is fully transparent by default, and can be customised.

There is one user, the author's partner. The app is sideloaded, never published, and used non-commercially.

## 1.2 Reference design

The reference is the stock Xiaomi Weather and Clock widget at 5 by 1 cells, as it looked before the update:

- Left side: a small date line on top, with a large time below it and a small am or pm suffix after the digits.
- Right side: a small place name on top, right-aligned, with a weather icon and a large temperature below it.
- Background fully transparent; white text.

Appendix B describes the reference in more detail.

## 1.3 Functional requirements

### Clock and date

**MUST** show the current time, updating every minute with no visible lag after the minute changes.

**MUST** show the date directly above the time, matching the reference layout.

**MUST** support three clock modes: 12 hour, 24 hour, and follow device setting. The default is follow device setting.

**MUST** show the am or pm suffix at a smaller size than the digits in 12 hour mode, and hide it in 24 hour mode.

**MUST** format the date from the device locale, using the locale's best pattern for abbreviated weekday, day and month, so a UK device shows a form like Tue 11 July (CWD13).

**MUST** update the date at midnight and when the device time zone or locale changes.

**MUST NOT** run a background service, alarm or wake lock to keep the clock ticking. The clock relies on the platform's self-updating clock view (see CWP2).

### Weather

**MUST** show the current temperature, a condition icon with distinct day and night variants from the Meteocons set (CWD4), and the place name.

**MUST** support Celsius and Fahrenheit. The default is Celsius.

**MUST** refresh the weather every 30 minutes in the background (CWD7).

**MUST** show a subtle stale indicator when the last successful refresh is older than twice the refresh interval, and keep showing the last good data rather than blanking.

**MUST** show the data attribution required by the provider's licence on the settings screen (see 1.4).

### Location

**MUST** use the device location automatically by default.

**MUST** request background location access, the Allow all the time option, so automatic location stays current during unattended refreshes (CWD5). **MUST** explain why before the system prompt appears.

**MUST** allow a fixed location chosen by place-name search, which overrides automatic location until switched back.

**MUST** fall back to the last known location, then to the fixed location if one is set, when the current location cannot be obtained, including when background access is refused. **MUST** record which source was used.

**MUST** request the coarsest location accuracy that gives a correct place name and forecast. Coarse location is expected to be enough.

**MUST** resolve place names from coordinates with the platform Geocoder (CWD6). If CWP5 shows the Geocoder is unavailable or unreliable on either target device, **MUST** stop and ask rather than adding another service.

**MUST NOT** send the location anywhere except Open-Meteo and the platform Geocoder.

### Appearance and customisation

**MUST** default to a fully transparent background: zero opacity.

**MUST** let the user set background colour, background opacity from 0 to 100 percent, corner radius, and text colour.

**MUST** render all widget text in the system `sans-serif-light` family, set once in the shared text style (CWD3). A bundled font does not reach the launcher on the stock launchers tested; JetBrains Mono remains a later field experiment through that one style.

**MUST** let the user show or hide each optional element independently: date, place name, condition icon, temperature.

**MUST** keep settings per widget instance, so two widgets on the home screen can look different.

**MUST** show a live preview of the widget on the settings screen while settings are changed.

**MUST** apply one fixed soft text shadow to all widget text through a single shared text style, to keep text legible on transparent backgrounds (CWD9). **MUST NOT** expose the shadow as a setting.

### Placement and size

**MUST** appear in the system widget picker as a normal Android app widget.

**MUST** be resizable horizontally and vertically on the home screen.

**MUST** rearrange its content to suit its size (see 2.4). A size that cannot fit an element hides that element rather than clipping or overlapping it.

**MUST** scale text with widget size.

### Configuration and interaction

**MUST** open the settings screen when the widget is first added.

**MUST** let the user reopen settings for an existing widget without removing it.

**MUST** open the system alarm screen when the clock area is tapped (CWD12).

**MUST** trigger an immediate weather refresh when the weather area is tapped (CWD12).

**MUST** show a guidance screen for the HyperOS Autostart and battery settings identified in CWP7, on Xiaomi devices only.

**MUST** show a diagnostics section on the settings screen: last refresh time, last error, location source used, and next scheduled refresh.

### App icon

**MUST** use the Thyme app icon (https://github.com/TheScottBot/Thyme) as the app and widget icon (CWD33).

## 1.4 Non-functional requirements

**MUST** keep battery use negligible: the only periodic work is the weather refresh.

**MUST** keep working after a reboot, an app update, and a launcher restart with no user action.

**MUST** handle no network, location denied, location off and provider errors without crashing, logging each case and keeping the last good data on screen.

**MUST** give every view a content description, so TalkBack reads the time, date, temperature, condition and place.

**MUST NOT** include analytics, tracking, advertising, or any network call not named in this document.

**MUST** comply with the Open-Meteo free tier terms: non-commercial use with CC BY 4.0 attribution.

## 1.5 Target devices

| Role | Device | OS |
|---|---|---|
| Primary (the user) | Xiaomi 11T Pro | HyperOS 1.0.27.0.UKDEUXM, Android 14 (API 34) |
| Development and testing | Google Pixel 10 Pro | Stock Android, version confirmed during CWP1 |

**MUST** set minSdk to 34 (CWD2).

**MUST** set targetSdk to the newest stable API level supported by the current stable Android Gradle Plugin, confirmed in CWP1.

**MUST** work correctly on both devices.

**MUST** treat the Xiaomi as the acceptance device. A behaviour that passes on the Pixel and fails on the Xiaomi is a failure.

**GUIDANCE:** do day-to-day development on the Pixel. Launcher behaviour, transparency, fonts in widgets and background work all differ between the two, so a Pixel pass is evidence, never proof.

# Part 2: Architecture

## 2.1 Shape

Four layers, with dependencies pointing inwards only:

1. **Domain.** Pure Kotlin with no Android imports. Holds the value types (appearance settings, weather snapshot, weather condition, location, size bucket) and the pure logic: mapping weather codes to conditions, choosing a size bucket, scaling text, formatting temperatures, deciding staleness.
2. **Ports.** Interfaces the domain and presentation depend on: WeatherSource, LocationSource, PlaceNameSource, WidgetSettingsStore, TimeSource.
3. **Adapters.** Implementations of the ports: Open-Meteo weather, device location, fixed location, platform Geocoder place names, settings persistence.
4. **Presentation.** The widget provider, one renderer, the settings screen, and the refresh scheduler.

**GUIDANCE:** put the domain in its own pure Kotlin JVM Gradle module, so its tests run on the JVM in seconds without an emulator or Robolectric.

## 2.2 DRY and SOLID rules

These are the rules this project will be reviewed against hardest.

**MUST** have exactly one renderer that turns one widget state into one RemoteViews tree. Every size, every setting and every entry point (first add, resize, refresh, reconfigure, boot) goes through it.

**MUST** declare each setting exactly once: key, type, default, bounds, and display label. Persistence, defaults, validation and the settings screen controls are all derived from that single declaration. Adding a setting means adding one declaration plus any rendering it needs, and nothing else.

**MUST** compose size layouts from shared fragments: one clock block and one weather block, included where needed. Size layouts only arrange the blocks. **MUST NOT** duplicate the views inside a block across layout files.

**MUST** define the text shadow and font once in a shared text style used by every widget text view.

**MUST** apply background colour, opacity and corner radius through one function used by both the widget and the settings preview.

**MUST** keep the widget provider thin: it receives system callbacks and delegates. It contains no rendering, networking or settings logic.

**MUST** depend on ports, not adapters, everywhere outside the composition root. Swapping the weather provider means writing one new adapter.

**MUST NOT** create parallel class hierarchies or near-identical classes per size, per setting, or per location source.

## 2.3 Technology

**MUST** use Kotlin.

**MUST** build the widget with RemoteViews and XML layouts.

**GUIDANCE:** do not use Jetpack Glance. The clock relies on the platform's self-updating TextClock view, which RemoteViews supports directly. Confirm during Plan whether Glance has an equivalent before ruling it out finally.

**GUIDANCE:** use WorkManager for the periodic weather refresh.

**GUIDANCE:** keep dependencies minimal. Every dependency needs an entry in DEPENDENCIES.md justifying why the platform alone is not enough.

## 2.4 Size behaviour

Three size buckets, with thresholds settled in CWP4:

- **Small:** clock and date only.
- **Wide:** the reference layout, with the clock block on the left and the weather block on the right.
- **Tall:** clock and date stacked above the weather.

**MUST** choose the bucket in the domain layer from the reported widget dimensions, and unit test the choice.

**MUST** use the platform's responsive widget layouts. minSdk 34 makes them available on every supported device, so no fallback path is built.

## 2.5 Weather provider

Open-Meteo forecast API for current conditions, plus the Open-Meteo geocoding API for fixed-location search. The platform Geocoder handles coordinates to place name.

**MUST** read the Open-Meteo documentation during Plan and record the exact request parameters and response fields used.

**MUST** map the provider's weather codes to a closed set of domain conditions in one table, covered by tests for every code the provider documents. **MUST** map each condition to its Meteocons day and night icons in that same table.

## 2.6 Build, signing and CI

**MUST** sign release builds with a single release keystore stored outside the repository (CWD8). **MUST** ask the author for the keystore path before creating or referencing it.

**MUST** read keystore and key passwords from outside the repository, for example environment variables or the user-level Gradle properties file, and **MUST** fail the release build with a clear message when they are missing.

**MUST** provide a GitHub Actions workflow in the repository that, on every push and pull request, runs the unit tests, lint and the em dash check, and builds a debug APK uploaded as a workflow artefact (CWD10).

**MUST NOT** give the workflow access to the release keystore. Release builds happen on the author's Mac only.

# Part 3: Plan stage

Plan produces evidence and decisions. No production code is written in Plan; throwaway spikes are allowed and are deleted or clearly fenced off afterwards.

**CWP1. Datum reconciliation and device facts.** Read ENGINEERING_DATUM.md and compare it with this document. Record every mismatch in the Plan record and ask the author about each one. Record the Pixel 10 Pro's exact Android version and API level, and the targetSdk chosen under 1.5.

**CWP2. Font and clock spike (highest risk, do first).** RemoteViews cannot set a typeface from code. Build a minimal widget and install it on both devices to establish:
- whether a TextView and a TextClock in a widget layout render JetBrains Mono when the font is set in the layout XML from a bundled font resource;
- whether the answer differs between the Pixel launcher and the HyperOS launcher;
- whether a text shadow defined in a shared style renders in the widget on both devices.

Record screenshots from both devices. The outcome feeds CWD3. If the XML font fails on either device, **MUST** stop and present the options with their costs.

**CWP3. Transparency spike.** Confirm on both devices that a widget with a transparent background renders fully transparent, with no backdrop, blur or forced corner clipping added by the launcher. Record screenshots over a light and a dark wallpaper on each.

**CWP4. RemoteViews capability check.** For each customisation in 1.3, confirm which remotable method or layout technique delivers it on API 34 and later: background colour and alpha, corner radius, text colour, text size, TextClock formats, view visibility. Record the method for each. Flag any that need a layout variant, because a variant per setting would break 2.2. Settle the size bucket thresholds.

**CWP5. Location, permissions and place names.** Establish the foreground then background location permission flow on both devices. Establish whether the platform Geocoder is present and returns correct place names on both devices, using its asynchronous API. Record the results.

**CWP6. Provider check.** Read the Open-Meteo forecast and geocoding documentation and terms. Record the endpoints, parameters, fields, rate limits and attribution wording. Confirm the Meteocons licence, its day and night coverage for every domain condition, and which style is used.

**CWP7. Background behaviour per device.** On the Xiaomi, establish whether a 30 minute periodic WorkManager job survives with default settings, and what changes with Autostart enabled and battery set to No restrictions. On the Pixel, establish behaviour under default battery settings and Adaptive Battery. Record which settings the HyperOS guidance screen must cover.

**CWP8. Remaining decision.** Get an answer to CWD3 once CWP2 has reported.

**Plan is done when:** the Plan record exists with evidence for CWP1 to CWP7, every Appendix A row is marked confirmed, refuted, or replaced, and CWD3 is answered.

# Part 4: Execute stage

Each phase lists Work, Excluded, Assumptions to verify, Tests first, and Done when. A phase is not started until the previous one is done.

**MUST** run every phase's Done-when check on both target devices and record the result for each. A phase is done only when both pass.

## CWE1. Project skeleton, build and CI

**Work:** Gradle project with a version catalogue, application ID fyi.punkdev.thymian; domain module and app module per 2.1; minSdk 34 and targetSdk per 1.5; warnings as errors; lint configured; release signing per 2.6; the GitHub Actions workflow per 2.6; README; LICENSE (MIT); NOTICE crediting JetBrains Mono (SIL Open Font Licence 1.1) if bundled, Meteocons (MIT), Open-Meteo data (CC BY 4.0) and the Thyme icon (MIT); DEPENDENCIES.md; an empty widget registered in the manifest.

**Excluded:** any real rendering, settings or networking.

**Assumptions to verify:** the chosen targetSdk builds cleanly with the current stable Android Gradle Plugin.

**Tests first:** a domain module test that runs and passes; a check that fails the build if any source, resource or document contains an em dash.

**Done when:** the Gradle build passes from a clean checkout locally and in the workflow, a release build installs over a previous release build without losing data, and the empty widget appears in the picker on both phones and can be placed.

## CWE2. Clock and date block

**Work:** the clock block fragment: date line above the time, time with a separate smaller am or pm suffix; clock mode handling for 12 hour, 24 hour and follow device; locale-derived date pattern; font per CWD3; the shared text style with the fixed shadow; transparent background.

**Excluded:** weather, settings screen, resizing.

**Assumptions to verify:** the date view updates itself at midnight with no app code running.

**Tests first:** domain tests for choosing time and date patterns per clock mode and locale, including en-GB and en-US.

**Done when:** on both phones, the clock matches the reference layout, ticks across a minute boundary, survives a time zone change, and switches correctly between 12 and 24 hour.

## CWE3. Settings model and settings screen

**Work:** the single setting declaration set (2.2); the settings store keyed by widget instance; the settings screen generated from the declarations, with a live preview using the shared appearance function; first-add configuration and reconfiguration of an existing widget; the appearance settings from 1.3; edge-to-edge handling of system bar insets.

**Excluded:** weather settings beyond unit selection; location settings until CWE5; diagnostics until CWE6.

**Assumptions to verify:** reconfiguring an existing widget is reachable from both launchers.

**Tests first:** declaration defaults and bounds; round-trip persistence per widget instance; clamping of out-of-range values; deleting a widget removes its settings.

**Done when:** two widgets on each phone hold different appearances, and changing one does not affect the other.

## CWE4. Responsive sizing

**Work:** the three size buckets from 2.4, composed from the shared blocks using responsive layouts; text scaling from widget dimensions; element hiding when space runs out.

**Excluded:** new elements.

**Tests first:** bucket selection and text scale calculation across a table of dimensions, including boundary values.

**Done when:** dragging the widget through every size on both phones never clips, overlaps or leaves text unreadably small.

## CWE5. Weather and location

**Work:** WeatherSource, LocationSource and PlaceNameSource ports with their adapters; the weather code to condition and icon table with Meteocons day and night icons; temperature units; automatic location with the foreground then background permission flow and an explanation before each prompt; the fixed location override with place-name search; the fallback chain from 1.3; platform Geocoder place names.

**Excluded:** periodic scheduling (CWE6).

**Tests first:** weather code mapping for every documented code, including the icon for day and night; response parsing from recorded fixtures, including error and partial responses; the location fallback chain with fake sources, including background access refused; temperature formatting and rounding.

**Done when:** a manual refresh on both phones shows correct weather and place name for automatic location, and for a fixed location in another city.

## CWE6. Refresh, resilience and diagnostics

**Work:** 30 minute periodic refresh with a network constraint and backoff; tap-to-refresh on the weather area; alarm screen on the clock area; refresh after boot and after app update; the stale indicator; the HyperOS guidance screen, shown on Xiaomi devices only; the diagnostics section; structured logging for every refresh outcome.

**Excluded:** new visual features.

**Tests first:** the staleness rule; the decision to keep last good data on each failure type; scheduling policy with a fake time source; manufacturer detection for the guidance screen.

**Done when:** the widget refreshes unattended for 24 hours on the Xiaomi, recovers after airplane mode is turned off, and the diagnostics section explains every failure it hit.

## CWE7. Accessibility, attribution and finish

**Work:** content descriptions for every element; provider attribution on the settings screen; app icon; README sections covering how to build, sign and install over USB, and how to get the debug APK from the workflow.

**Tests first:** a test that every view in every widget layout has a content description or is marked as decorative.

**Done when:** TalkBack reads the widget correctly on both phones, and the README steps work from a clean checkout.

# Part 5: Verify stage

## CWV1. Automated

**MUST** pass locally and in the GitHub Actions workflow: all unit tests, lint with zero warnings, the em dash check, and a clean build.

## CWV2. Device gates

Run the full checklist on the Xiaomi 11T Pro. Run items 1 to 6, 9, 10 and 11 on the Pixel 10 Pro. Items 7, 8 and 12 are about background survival, which only counts on the Xiaomi; record Pixel results too as a comparison.

1. Widget added from the picker, settings screen opens, defaults are fully transparent.
2. Transparency correct over a light, a dark and a busy wallpaper; text legible on all three with the fixed shadow.
3. Resized through every bucket with no clipping.
4. Clock correct across a minute boundary, across midnight, and after a time zone change; date in UK order on a UK locale.
5. 12 hour, 24 hour and follow device modes all correct.
6. Weather correct for automatic location, then a fixed location, then back to automatic; clock tap opens alarms; weather tap refreshes.
7. Airplane mode: last good data stays, stale indicator appears after the threshold, recovers when the network returns.
8. Reboot: the widget comes back with its settings and resumes refreshing.
9. Two instances with different settings stay independent.
10. Reconfigure an existing widget without removing it.
11. TalkBack reads time, date, temperature, condition and place.
12. 24 hours of unattended refreshes visible in diagnostics.

# Part 6: Decision record

| ID | Decision | Answer | Applied in |
|---|---|---|---|
| CWD1 | Name, application ID, repository | Thymian; fyi.punkdev.thymian; https://github.com/TheScottBot/Thymian | Header, CWE1 |
| CWD2 | minSdk and targetSdk | minSdk 34; targetSdk newest stable supported by current AGP | 1.5, 2.4, CWE1 |
| CWD3 | Font rendering approach | System `sans-serif-light` in the shared style; JetBrains Mono a later field experiment | 1.3, CWE2 |
| CWD4 | Weather icon set | Meteocons (MIT) | 1.3, 2.5, CWE5 |
| CWD5 | Background location | Request Allow all the time, with the fallback chain if refused | 1.3, CWE5 |
| CWD6 | Place-name source | Platform Geocoder; stop and ask if it fails on either device | 1.3, CWP5 |
| CWD7 | Refresh interval | 30 minutes | 1.3, CWE6 |
| CWD8 | Signing | Release keystore outside the repository; path supplied by the author | 2.6, CWE1 |
| CWD9 | Legibility | Fixed soft shadow in a shared style; not a setting | 1.3, 2.2, CWE2 |
| CWD10 | Build and distribution | Local builds plus a GitHub Actions workflow producing a debug APK | 2.6, CWE1 |
| CWD11 | Project licence | MIT | Header, CWE1 |
| CWD12 | Tap targets | Clock opens alarms; weather refreshes | 1.3, CWE6 |
| CWD13 | Date format | Follow device locale | 1.3, CWE2 |
| CWD14 to CWD32 | Plan stage decisions | Recorded with their reasoning in `docs/evaluation/ACTUAL_CONTRACT_EVALUATION.md` section 10 | Throughout |
| CWD33 | App and widget icon | The Thyme app icon, redrawn as an Android vector drawable | 1.3, CWE7 |

### Additions confirmed by the author

These came from the spec writer and the author has approved them: the stale indicator, the diagnostics section, the HyperOS guidance screen, and per-widget settings. The em dash build check and the separate domain module also originate with the spec writer; the first enforces an author rule, the second remains GUIDANCE.

# Appendix A: Unverified values and claims

| Claim | Settled by |
|---|---|
| RemoteViews has no runtime typeface setter; a font set in the layout XML may or may not render in a widget | CWP2 |
| A text shadow defined in a layout style renders in a widget on both launchers | CWP2 |
| The HyperOS launcher renders a transparent widget background without adding a backdrop | CWP3 |
| The Pixel launcher clips widget corners to a system radius, which may fight the corner radius setting | CWP3 |
| Text shadow cannot be changed at runtime through RemoteViews | CWP4 |
| TextClock updates itself every minute and its date at midnight inside a widget, with no app process running | CWP2, CWE2 |
| Responsive widget layouts and widget reconfiguration are available on API 34 and later | CWP4 |
| Background location from WorkManager needs the background location permission, requested separately after foreground location | CWP5 |
| The platform Geocoder is present and returns correct place names on both devices | CWP5 |
| WorkManager periodic work has a 15 minute minimum interval | CWP7 |
| HyperOS needs Autostart and No restrictions battery mode for reliable periodic work | CWP7 |
| Open-Meteo free tier: no key, non-commercial, about 10,000 calls a day, CC BY 4.0 attribution | CWP6 |
| Meteocons is MIT licensed and has day and night variants for every condition needed | CWP6 |
| JetBrains Mono is licensed under the SIL Open Font Licence 1.1 and may be bundled | CWE1 |
| The HyperOS home screen grid is 4 or 5 columns, so the reference widget is 4 by 1 or 5 by 1 | CWP4 |
| A targetSdk of 35 or later forces edge-to-edge layout on the settings screen | CWE3 |
| The Pixel 10 Pro runs Android 16 or later | CWP1 |
| AlarmClock show alarms opens an alarm screen on both devices | CWE6 |

# Appendix B: Reference design notes

Taken from a screenshot of the stock widget in the HyperOS widget picker, where it is listed as Weather and Clock, 5 by 1. The picker preview shows a grey rounded background; on the home screen the original was fully transparent.

- Date line: small, top left.
- Time: large, light weight, left aligned under the date, with a small pm suffix baseline-aligned after the digits.
- Place name: small, top right, right aligned.
- Weather: a condition icon, here partly cloudy, followed by a large temperature with a degree sign and no unit letter, right aligned under the place name.
- All text and icons white.
- Generous horizontal padding; the two halves sit at opposite ends of the strip.

Proportions and exact sizes are not specified, because they cannot be measured reliably from the screenshot. Match them by eye on the device during CWE2 and CWE4.

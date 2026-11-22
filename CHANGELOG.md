# Changelog

All notable changes are recorded here, in the form of
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/). Versions follow
[Semantic Versioning](https://semver.org/). Per the engineering datum, each
tag also names the gates cleared at that commit.

## [Unreleased]

Project skeleton (CWE1).

### Added

- Gradle 9.9.2 wrapper with a pinned checksum and a version catalogue
  (AGP 9.5.1, Kotlin 2.4.21, JUnit 6.1.3, ktlint 1.8.0).
- `domain` module in pure Kotlin, with a test that keeps Android imports out,
  and the refresh interval (30 minutes) and staleness threshold (60 minutes).
- Typography check for em dashes, en dashes and hyphen runs, run by `check`.
- `app` module (minimum API 34, target API 37) with an empty transparent
  widget, its picker preview and the Thyme icon.
- Android lint and ktlint with warnings as errors.
- Release signing from `THYMIAN_KEYSTORE_PASSWORD` and `THYMIAN_KEY_PASSWORD`,
  failing early when either is missing.
- GitHub Actions workflow running every check and uploading the debug APK.
- Clock block (CWE2): date above large digits with a smaller am or pm suffix,
  in the shared sans-serif-light text style with the fixed shadow; 12 hour,
  24 hour and follow device modes; the date pattern from the device locale;
  re-rendered when the locale changes; on device tests.
- Widget settings (CWE3): every setting declared once with its default, bounds
  and label; stored per widget in SharedPreferences with clamping, a length
  bound and a schema version; background colour, opacity, corner radius, text
  colour and date visibility rendered; settings deleted with their widget.
- Settings screen (CWE3): opens when a widget is added and from the launcher's
  reconfigure action; one control per declared setting; a live preview of the
  renderer's own output; saves per widget.
- Responsive sizing (CWE4): Small, Wide and Tall layouts composed from the
  clock block and a weather block (hidden until weather arrives); one layout
  per size the launcher reports; clock text scaled to the space, hiding the
  date before the digits get too small; re-rendered on resize.
- Weather rules (CWE5): the weather condition table with Meteocons day and
  night icons, temperature formatting with local unit conversion, the location
  fallback chain, and a bounded Open-Meteo forecast parser with a seeded
  mutation test.
- Weather icons (CWE5): 23 Meteocons monochrome icons vendored verbatim with
  digests, converted to vector drawables by a tested one time tool.
- Weather on the widget (CWE5): HTTPS Open-Meteo source with a bounded read,
  platform location and Geocoder adapters, the last good weather kept between
  refreshes, a one off refresh when a widget is placed or saved, and the weather
  block showing place, tinted icon and temperature in each widget's unit with a
  spoken description.
- Location settings (CWE5): one location for all widgets, following the phone
  through a two step permission flow with an explanation before each prompt, or
  a fixed place from a bounded Open-Meteo place search; Open-Meteo and GeoNames
  credited beside the preview.
- Refresh and taps (CWE6): a 30 minute periodic refresh with a network
  constraint, scheduled from the settings screen and after a reboot or app
  update; tap the weather to refresh, tap the clock to open alarms; every
  refresh outcome kept in a bounded diagnostics history.
- Stale indicator (CWE6): a small clock symbol before the temperature once the
  weather is over an hour old, tinted to the text colour and read out by
  TalkBack; refreshes run offline too, so it appears and the failure is logged.
- Diagnostics (CWE6): last refresh, location source, last error, next
  scheduled refresh and the recent history on the settings screen, with Copy
  diagnostics putting a plain report with no location on the clipboard.
- The typography check also rejects on device test names that dex cannot hold.

### Fixed

- A refresh loop that re-fetched the weather once a second: the widget update
  callback no longer schedules work, and a throttle limits automatic refreshes
  to one per 10 minutes.
- Project documents: README, BUILDING, LICENSE, NOTICE, DEPENDENCIES, TESTING,
  IMPLEMENTATION_DEVIATIONS, PRIVACY, SECURITY, HARDWARE_COMPATIBILITY and
  FIELD_NOTES.

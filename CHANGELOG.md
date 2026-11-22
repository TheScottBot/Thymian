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
- Project documents: README, BUILDING, LICENSE, NOTICE, DEPENDENCIES, TESTING,
  IMPLEMENTATION_DEVIATIONS, PRIVACY, SECURITY, HARDWARE_COMPATIBILITY and
  FIELD_NOTES.

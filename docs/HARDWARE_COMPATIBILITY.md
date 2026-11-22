# Hardware compatibility

What has been run on what, by whom, and when. Only the owner adds a row saying
a gate was cleared. Rows recording observations by the agent say so.

## Devices

| Device | Android | Build | Launcher | Role |
|---|---|---|---|---|
| Google Pixel 10 Pro | 17, API 37 (37.1) | CP3A.261005.005 | Pixel launcher 17 | Development and testing |
| Android Emulator, Google APIs image | 14, API 34 | UE1A.230829.050 | Pixel launcher 14 | API 34 evidence only, never a HyperOS stand in |
| Xiaomi 11T Pro | 14, API 34 | HyperOS 1.0.27.0.UKDEUXM | HyperOS launcher | Acceptance; not available before delivery (CWD30) |

## Runs

| Date | Device | Build | What ran | By | Result |
|---|---|---|---|---|---|
| 2026-11-22 | Emulator API 34 | Plan spike | Font, clock, transparency (CWP2, CWP3) | Agent, over adb | Bundled font falls back to Roboto; shadow, spanned am or pm and transparency render |
| 2026-11-22 | Pixel 10 Pro | Plan spike | Font, clock, transparency over dark and light wallpaper | Agent, over adb | As on the emulator |
| 2026-11-22 | Emulator API 34 and Pixel 10 Pro | Plan spike | Location permissions and Geocoder (CWP5) | Agent, over adb | Flow as documented; no fresh approximate fix within 30 s; last known fix available on the Pixel; Geocoder works |
| 2026-11-22 | Pixel 10 Pro | Plan spike | 30 minute WorkManager soak (CWP7), started 12:17 | Agent, over adb | Running; read after 2026-11-23 12:17 |
| 2026-11-22 | Emulator API 34 | CWE1 debug | Empty widget found in the picker and placed | Agent, over adb | Placed, Thyme icon and preview shown |
| 2026-11-22 | Pixel 10 Pro | CWE1 debug | Empty widget placed | Owner | "All fine, placed it" (see the correction below) |
| 2026-11-22 | Emulator API 34 | CWE2 debug | On device tests, 6 | Agent, Gradle connected run | All pass |
| 2026-11-22 | Pixel 10 Pro | CWE2 debug | On device tests, 6 | Agent, instrumentation runner | All pass |
| 2026-11-22 | Emulator API 34 | CWE2 debug | Clock block placed; minute boundary; device switched to 24 hour; time zone to Berlin | Agent, over adb | Ticked 13:10 to 13:11; showed 13:11 without the suffix; showed 14:11 at once |
| 2026-11-22 | Pixel 10 Pro | CWE2 debug | Clock block placed and judged by eye | Owner | "Looks good. It wasn't placed anywhere so I've placed it and its good" |

Correction, 22 November 2026: during the CWE2 check the owner reported that no
Thymian widget had been on the Pixel's home screen, and placed one then. The
CWE1 placement row above is therefore not evidence of a placed widget on the
Pixel; the CWE2 row is.

## Gates cleared

None yet.

## Owed

- Release build installed over a previous release build without losing data (CWE1).
- Every Xiaomi check: first evidence will come from real use, in FIELD_NOTES.md.

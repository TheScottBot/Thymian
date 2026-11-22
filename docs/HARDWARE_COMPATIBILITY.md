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
| 2026-11-22 | Emulator API 34 | CWE3 batch B debug | On device tests, 13 | Agent, Gradle connected run | All pass |
| 2026-11-22 | Pixel 10 Pro | CWE3 batch B debug | On device tests, 13 | Agent, instrumentation runner | All pass |
| 2026-11-22 | Emulator API 34 | CWE3 debug | On device tests, 21 | Agent, Gradle connected run | All pass |
| 2026-11-22 | Pixel 10 Pro | CWE3 debug | On device tests, 21 | Agent, instrumentation runner | All pass |
| 2026-11-22 | Emulator API 34 | CWE3 debug | First add opens settings; two widgets with different settings; reconfigure one through the launcher's pencil | Agent, over adb | Independent; reconfigure loads saved settings; corner radius 0 still drawn rounded by the launcher |
| 2026-11-22 | Pixel 10 Pro | CWE3 debug | Two widgets with different settings | Owner | "two clock on pixel works great" |
| 2026-11-22 | Emulator API 34 | CWE4 debug | On device tests, 30 | Agent, Gradle connected run | All pass |
| 2026-11-22 | Pixel 10 Pro | CWE4 debug | On device tests, 30 | Agent, instrumentation runner | All pass |
| 2026-11-22 | Emulator API 34 | CWE4 debug | Resized through 4 by 1, 2 by 1, 2 by 2 and 4 by 2 | Agent, over adb | Wide, Small width, Tall and Wide layouts chosen; text scaled; no clipping or overlap |
| 2026-11-22 | Pixel 10 Pro | CWE4 debug | Widget dragged through several sizes, checked for clipping | Owner | "Looks fine" |
| 2026-11-22 | Emulator API 34 | CWE5 D1 debug | On device tests, 39 | Agent, Gradle connected run | All pass |
| 2026-11-22 | Emulator API 34 | CWE5 D1 debug | Refresh after placing a widget, location granted by adb | Agent, over adb | Worker ran; no fix on the emulator, so `location_unavailable reason=NO_FIX` logged and the widget kept rendering |
| 2026-11-22 | Emulator API 34 | CWE5 D2 debug | On device tests, 44 | Agent, Gradle connected run | All pass |
| 2026-11-22 | Pixel 10 Pro | CWE5 D2 debug | On device tests, 44 | Agent, instrumentation runner | All pass |
| 2026-11-22 | Emulator API 34 | CWE5 D2 debug | Live place search for Berlin, fixed place saved, refresh | Agent, over adb | 10 candidates; weather fetched (status 200, 390 bytes); widget shows Berlin, overcast day icon tinted white, 5° |
| 2026-11-22 | Emulator API 34 and Pixel 10 Pro | CWE5 D2 debug | Widget flickering every second, reported by the owner | Owner, then agent over adb | Refresh loop found and fixed (D10); after the fix one refresh per save and no further activity over 45 s |
| 2026-11-22 | Pixel 10 Pro | CWE5 D2 debug with loop fix | Location allowed, weather on the widget | Owner | "Steady, weather shows" |
| 2026-11-22 | Pixel 10 Pro | CWE5 D2 debug with loop fix | Fixed place in another city, weather on the widget | Owner | "Changed correctly" |
| 2026-11-22 | Emulator API 34 and Pixel 10 Pro | CWE6 B debug | On device tests, 46 | Agent, instrumentation runner | All pass on both |
| 2026-11-22 | Emulator API 34 and Pixel 10 Pro | CWE6 B debug | Installed over the previous build | Agent, over adb | Update receiver ran on both (`reason=MY_PACKAGE_REPLACED`); its automatic refresh was throttled, a refresh having just run; a WorkManager job with timing and connectivity constraints is scheduled on both |
| 2026-11-22 | Emulator API 34 | CWE6 B debug | Tap the weather; tap the clock | Agent, over adb | Weather tap refreshed at once (`trigger=USER_REQUESTED`); clock tap opened the Clock app's alarms |
| 2026-11-22 | Emulator API 34 and Pixel 10 Pro | CWE6 C1 debug | On device tests, 49 | Agent, instrumentation runner | All pass on both after CWD38 |
| 2026-11-22 | Emulator API 34 | CWE6 C1 debug | Weather aged 75 minutes, network off, weather tapped | Agent, over adb | `failure=NETWORK` logged; last good weather kept with the stale symbol beside the temperature |
| 2026-11-22 | Emulator API 34 | CWE6 C1 debug | Force stop | Agent, over adb | The launcher fell back to the initial layout (no weather, no tap actions) until the next update; force stop also cancels scheduled work |
| 2026-11-22 | Emulator API 34 and Pixel 10 Pro | CWE6 C2 debug | On device tests, 52 | Agent, instrumentation runner | All pass on both |
| 2026-11-22 | Emulator API 34 | CWE6 C2 debug | Diagnostics section opened | Agent, over adb | Last refresh, fixed place source, the earlier network failure as last error, next refresh 30 minutes on, and the history including throttled attempts |
| 2026-11-22 | Emulator API 34 and Pixel 10 Pro | CWE6 C3 debug | On device tests, 56 | Agent, instrumentation runner | All pass on both |
| 2026-11-22 | Emulator API 34 | CWE6 C3 debug | Settings screen on a phone that is not Xiaomi | Agent, over adb | No battery guidance shown; diagnostics closed behind its heading above Save |
| 2026-11-22 | Emulator API 34 and Pixel 10 Pro | CWE7 debug | On device tests, 58, including widget layout accessibility | Agent, instrumentation runner | All pass on both |
| 2026-11-22 | Pixel 10 Pro | CWE7 debug | TalkBack over the widget | Owner | "Reads correctly" |
| 2026-11-22 | Emulator API 34 | Verify debug | Reboot | Agent, over adb | Widget returned with settings and weather; `refresh_schedule_ensured reason=BOOT_COMPLETED` |
| 2026-11-22 | Emulator API 34 | Verify debug | Clock set by hand to 23:59:30 | Agent, over adb | Date rolled to Mon, November 23; the 12:00 tick was missed, 11:59 shown until 12:01 |

Correction, 22 November 2026: during the CWE2 check the owner reported that no
Thymian widget had been on the Pixel's home screen, and placed one then. The
CWE1 placement row above is therefore not evidence of a placed widget on the
Pixel; the CWE2 row is.

## Gates cleared

None yet.

## Owed

- The first published release, installed on the Xiaomi from the releases page (README, Install).

- Release build installed over a previous release build without losing data (CWE1).
- Every Xiaomi check: first evidence will come from real use, in FIELD_NOTES.md.

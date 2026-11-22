# Field notes

What real use showed, dated. Real use on the Xiaomi 11T Pro is the only
HyperOS evidence this project will have (CWD30), so each snag reported from it
is recorded here with the date, what was seen, any copied diagnostics text, and
what changed in response.

## 22 November 2026: widget missing from the widget list

Seen: 1.0.0 installed from the releases page, but Thymian did not appear in
the HyperOS widget list. No diagnostics text: the settings screen could not be
reached.

Likely cause: HyperOS hides the widgets of an app that has never been opened,
and 1.0.0 had no home screen icon, so it could not be opened.

Changed: the next release adds a home screen icon that opens the settings, or
offers to add the widget when none is placed. Still to confirm on the Xiaomi
that opening the app once makes the widget appear.

## 22 November 2026, 1.0.1: widget appears; touching the time does nothing

Seen: with 1.0.1 the widget appeared in the widget list and was placed, so the
home screen icon fixed the first report. Weather refreshes for a fixed place.
Touching the time did nothing. Diagnostics showed five refreshes, all
`location_source=FIXED_CHOSEN place_named=true`, and no failures.

Likely cause: the clock tap asked only for the alarm list, and the Xiaomi
clock app is thought not to answer that request, so the tap had nowhere to go.

Changed: the clock tap opens the alarm list where the phone can show it, and
otherwise the clock app (`com.android.deskclock` on Xiaomi). Copy diagnostics
now has a `clock_tap target=` line saying which. Every touch on either side
is now logged as `tapped side=clock` or `tapped side=weather`, with
`tap_failed` and a reason when opening fails, so the next report shows
whether touches reach the app at all. Still to confirm on the Xiaomi.

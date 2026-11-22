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

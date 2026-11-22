# Privacy

Thymian handles one piece of personal data: where the phone is. This states
what is collected, where it goes, how long it stays and what Thymian does not
protect against. Parts marked as arriving later describe the design decided in
the specification; the current build collects nothing.

## What is collected

- **Approximate location** (Android's coarse location, about a 3 square
  kilometre area), from CWE5. Precise location is never requested.
- **A fixed place** chosen by searching a place name, if the user picks one.
- **Widget settings** per widget: colours, opacity, corner radius, which
  elements show, clock mode and temperature unit.

Nothing else: no account, no identifiers, no contacts, no usage data.

## Where it goes

- **Open-Meteo** (api.open-meteo.com) receives the latitude and longitude of
  the automatic or fixed location with each weather refresh, every 30 minutes
  and when the weather is tapped. Open-Meteo's own terms govern what it keeps.
- **Open-Meteo geocoding** (geocoding-api.open-meteo.com) receives the text
  typed into the place search.
- **The platform Geocoder** turns coordinates into a place name. On Android it
  is supplied by the phone's system services, typically Google's on a phone
  with Google services; Thymian does not choose or control that backend.

Location is sent nowhere else (specification 1.3). There are no analytics,
tracking or advertising (specification 1.4).

## How long it stays

- The last known location and last weather result stay in app storage on the
  phone so the widget can show something when a refresh fails. They are
  replaced on each refresh and deleted when the app is uninstalled.
- Widget settings are deleted when their widget is removed.
- Nothing is backed up to the cloud or copied in a device transfer
  (`app/src/main/res/xml/data_extraction_rules.xml`).

## Logs and diagnostics

Logs and the diagnostics history record which location source was used and
why a refresh failed, never coordinates or place names (datum 0.15). The Copy
diagnostics button (CWD31) copies that history as text for the user to send if
they choose; it contains no location.

## What Thymian does not protect against

- Open-Meteo, the platform Geocoder backend and the network path to them see
  the location as described above, over HTTPS.
- Anyone with the unlocked phone can open the settings screen and see the
  place name and fixed location.
- Android, the launcher and other apps with location permission are outside
  Thymian's control.

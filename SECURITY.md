# Security

The threat model, the accepted risks, and the limits stated plainly.

## Trust boundaries

1. **Network responses** from Open-Meteo (forecast and geocoding) are
   untrusted input. From CWE5 they are size bounded (64 KiB, refused rather
   than truncated), parsed by a parser that cannot read past its buffer, with
   used fields required and range checked (datum 0.10, CWD19).
2. **The platform Geocoder** returns data from a system service; its results
   are treated as untrusted text and only displayed.
3. **Release signing material** stays outside the repository: the keystore at
   `~/Keystores/thymian-release.jks` and its passwords in the environment of the
   one release command. `.gitignore` refuses `*.jks` and `*.keystore` files.
4. **Continuous integration** has read only repository access and no secrets,
   and cannot build or sign a release. Every action is pinned to a commit hash
   and the Gradle wrapper is validated.

## What Thymian exposes

- No exported activity yet. The widget provider is exported because the
  launcher must reach it; its intent filter lists only the widget update
  action, and any app could still send it an explicit broadcast, which
  `AppWidgetProvider` handles only for the widget actions it knows.
- No network listener, no content provider, no shared files.
- Pending intents for the tap targets (CWE6) are immutable with explicit
  targets, as Android 14 requires.

## Accepted risks

None has been accepted yet. Accepted risks are written here in the owner's own
words when one is decided.

## Limits

- Sideloaded builds rely on the user installing only builds signed with the
  release key; Android refuses an update signed with a different key.
- A compromised Mac could read the keystore and capture the passwords while a
  release is built; that is outside what this repository can defend.

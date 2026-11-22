# Thymian

A transparent clock and weather home screen widget for Android, styled after
the HyperOS Weather and Clock widget before a system update took its
transparency away. Customisable, with weather from Open-Meteo.

## Install

Thymian is not on the Play Store. It is installed by downloading the APK from
GitHub and opening it on the phone, which Android calls sideloading. It needs
Android 14 or later.

### Download and install

1. On the phone, open
   [the latest release](https://github.com/TheScottBot/Thymian/releases/latest)
   and download the file named `thymian-` followed by the version and `.apk`.
2. Open the downloaded file, from the notification or the Downloads app.
3. Android asks whether to allow installing apps from that browser or file
   app. Allow it for that app only, then go back and open the file again.
4. Tap Install. Google Play Protect may warn that the app is unknown; choose
   to install anyway. This is expected for any app that is not from the Play
   Store.

### Add the widget

1. Long press an empty part of the home screen and choose Widgets.
2. Find Thymian and drag it onto the home screen. Its settings open.
3. Under Location, either leave Follow the phone selected and tap Continue to
   allow location, choosing Allow all the time on the second screen, or choose
   Fixed place and search for a town.
4. Change the look if you like, then tap Save. The weather appears within a
   minute.

Tap the clock to open your alarms, or the clock app if the phone has no alarm
list, and tap the weather to refresh it. In the widget's settings, the weather
can also open an app of your choice, such as a weather app. The
Thymian icon in the app list opens the widget's settings, or offers to add the
widget if none is placed.

### On a Xiaomi, Redmi or POCO phone

If Thymian is missing from the widget list, open the Thymian icon in the app
list once, then look again; HyperOS hides widgets from apps that have never
been opened. The icon can also add the widget for you.

HyperOS can also stop apps running in the background, which stops the weather
refreshing. Open the widget's settings and tap Keep the weather refreshing on
Xiaomi for what to change.

### Updating

Download and open the newer APK the same way; it installs over the old one
and keeps every widget's settings. Updates only install over a build from the
same releases page: a build from anywhere else is signed differently, and
Android would refuse it unless the app were removed first, losing the settings.

### If something looks wrong

Open the widget's settings, tap Show diagnostics, then Copy diagnostics, and
paste the text into a message. It says what the app tried and what failed, and
contains no location or weather details.

## Build

```bash
./gradlew check assembleDebug
```

## Documents

- [Building, installing over USB and releasing](docs/BUILDING.md)
- [Testing](TESTING.md)
- [Specification](docs/SPECIFICATION.md)
- [Plan stage evaluation and decisions](docs/evaluation/ACTUAL_CONTRACT_EVALUATION.md)
- [Changelog](CHANGELOG.md)
- [Dependencies](docs/DEPENDENCIES.md)
- [Implementation deviations](docs/IMPLEMENTATION_DEVIATIONS.md)
- [Hardware compatibility](docs/HARDWARE_COMPATIBILITY.md)
- [Field notes](docs/FIELD_NOTES.md)
- [Privacy](PRIVACY.md)
- [Security](SECURITY.md)

## Licence

MIT, see [LICENSE](LICENSE). Third party material is credited in
[NOTICE](NOTICE).

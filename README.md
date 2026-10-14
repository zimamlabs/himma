<p align="center"><img src="docs/icon.svg" width="120" alt="Himma"></p>

<h1 align="center">Himma</h1>

<p align="center">Health and training in plain files, by <a href="https://github.com/zimamlabs">Zimam</a>.</p>

Himma (همّة, drive and determination) keeps your meals, water and fasting in a folder of plain files you own. It works offline, has no account and sends nothing anywhere.

## What is in it

- **Home** first: a greeting, calories eaten and left, water, the fasting state, the next medicine dose, quick actions (log a meal, add a glass of water) and what's new. Each card opens its section.
- **Meals**: name, calories and optional protein, carbs and fat. A daily calorie goal, the day's total and what is left. Tap a meal to edit or delete it; step back through earlier days.
- **Water**: a daily goal (2 L to start), a tap for a 250 ml glass or any amount, undo, and a progress ring.
- **Fasting**: 16:8, 18:6, 20:4 or your own window. Shows eating or fasting now, the time left, and remembers when you start eating.
- **Medicine**: add a medicine with a dose note, times and days. Today's doses show on Home and in the Medicine section with Taken and Skip, and a notification at each dose time has Taken and Skip buttons. Alarms are set again after a restart or a change of time zone. Android asks once for notification permission; if exact alarms are not allowed a reminder can come a few minutes late.
- **Tour and updates**: a short tour on first install (open it again from About or Settings), and a one-time What's new note after each update.
- **Settings**: the calorie and water goals, the data folder and the tour.
- **Plain files**: `meals/<year>/<date>.csv`, `water/<year>.csv`, `medicines/medicines.json`, `medicines/log.csv` and `.himma/settings.json`. Open them in any editor or spreadsheet.

## Your data folder

By default Himma keeps its files in the app's private folder. If you allow *All files access* (the tour and Settings offer it), they live in a visible `Zimam/Himma` folder on the phone, next to the other Zimam apps, and survive a reinstall. On the desktop it is `~/Zimam/Himma`.

Moving there never risks your data: Himma copies the old folder, checks every file by size and SHA-256, writes a marker and only then starts using the new folder. Nothing is deleted or overwritten: a different file with the same name stays and the old one is kept beside it as `name (old copy)`. If anything goes wrong the old folder stays in use and Himma tries again later. The old copy is kept as a backup until you choose *Remove the old copy* in Settings, which asks first and checks again that everything is safe.

## Download

Get the file for your phone from the [latest release](../../releases/latest). Next to every file is a checksum file with the same name plus `.sha256`.

| System | File |
|---|---|
| Android phone (64-bit, almost every phone since 2016) | `Himma_<version>_Android_arm64-v8a.apk` |
| Android, older 32-bit | `Himma_<version>_Android_armeabi-v7a.apk` |
| Android emulator, Chromebook | `Himma_<version>_Android_x86_64.apk` |

```
sha256sum -c Himma_<version>_Android_arm64-v8a.apk.sha256
```

### Verify the Android download

Every APK is signed with Himma's own key. Android installs it without a store because the signature is valid, and shows its usual "unknown source" notice for anything that did not come from Google Play. To check a file is really ours, compare its signing certificate with this fingerprint:

```
SHA-256: A0:72:C4:14:70:6B:43:88:D5:7C:3B:A8:1C:B1:7E:E3:54:EE:81:4C:CA:88:78:20:33:C2:D0:A7:25:0D:31:76
```

```
apksigner verify --print-certs Himma_<version>_Android_arm64-v8a.apk
```

## Build

You need JDK 17 or newer and, for Android, the Android SDK (platform 36).

```
./gradlew :shared:desktopTest            # the tests
./gradlew :composeApp:run                # run it on the desktop (data in ~/Himma)
./gradlew :composeApp:assembleDebug      # Android debug APKs
```

`shared` holds the files and the sums (plain Kotlin, tested without a screen) and `composeApp` holds the screens for both platforms.

## Roadmap

Planned next: gym routines and sessions with streaks, steps and other sports, meal reminders, diet and gym plans, body measurements, backup and export, and a Linux edition after version 1.

## Licence

MIT. See [LICENSE](LICENSE).

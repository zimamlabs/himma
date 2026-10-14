# Changelog

## 0.4.0

- **Arabic and right-to-left.** Himma speaks Arabic (همّة) with a full right-to-left layout: every screen, the tour, the release notes and the medicine notifications. Choose English, العربية or the phone's language in Settings, and optionally show numbers in Arabic-Indic digits. A check in CI fails when an English text has no Arabic one.

## 0.3.1

- **A new look.** Warm near-black and ivory surfaces, the Zimam orange as the one accent, a soft glow behind the hero, large rounded cards with a hairline border and the small diamond accent, on every screen.
- **A new About page.** The mark on a glow, what Himma is, the tour, what's new, a privacy card and links to the repository, releases, issues and licence.
- **A refreshed README** with badges, the icon board and a screenshots section.

## 0.3.0

- **New icon.** The flowing runner shape with the Zimam diamond, on the launcher, the splash screen and the About page.
- **A richer Home.** A progress ring for today's goals, rings for water, calories and steps, a protein, carbs and fat bar, seven-day charts, today's workout and a weekly summary.
- **Gym.** Routines, a workout screen with a rest timer, history, volume and personal bests.
- **Steps.** The phone's step counter with a daily goal and a seven-day chart; Android asks once for the activity permission. Steps on days Himma is never opened are not counted.
- **Weight.** A weight log with the change over four weeks and a trend chart.
- **Meal templates.** Save a meal and log it again in one tap; recent meals are offered as well.
- **Theme.** System, Light or Dark, chosen in the tour or Settings.
- All the new data is in new plain files; nothing from 0.2.0 changes.

## 0.2.0

- **Home.** The app opens on a Home page: greeting, calories, water, fasting, the next medicine dose, quick actions and what's new. Each card opens its section.
- **Medicine reminders.** Add medicines with times and days; see today's doses on Home and in the Medicine section; mark each Taken or Skipped. A notification at each dose time has Taken and Skip buttons. Alarms return after a restart or a time-zone change.
- **One Zimam folder.** With All files access, your files live in `Zimam/Himma` on the phone (`~/Zimam/Himma` on the desktop). The move is a verified copy: every file is checked by size and SHA-256, nothing is deleted or overwritten (a clash keeps both files), the old private folder stays as a backup until you remove it in Settings, and on any problem the old folder stays in use.
- **Tour and updates.** A four-page tour on first install, reopenable from About and Settings, and a one-time What's new note after an update.
- **New splash.** The rings draw in, the dot settles with a bounce, and the name sits clear of the logo.

## 0.1.0

- First release: Today, Meals, Water and Fasting tabs.
- **Meals**: log a meal with name, calories and optional protein, carbs and fat; day total and calories left against your goal; edit, delete and browse earlier days.
- **Water**: a daily goal (2 L by default), +250 ml glass or a custom amount, undo, and a progress ring.
- **Fasting**: 16:8, 18:6, 20:4 or your own window, with the time you start eating saved; shows whether you are eating or fasting now and how long is left, also when the window crosses midnight.
- **Settings**: daily calorie and water goals.
- Everything is stored as plain files in the `Himma` folder (the app's private folder on Android, `~/Himma` on the desktop): `meals/<year>/<date>.csv`, `water/<year>.csv` and `.himma/settings.json`. No account, no network.

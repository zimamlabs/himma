# Changelog

## 0.1.0

- First release: Today, Meals, Water and Fasting tabs.
- **Meals**: log a meal with name, calories and optional protein, carbs and fat; day total and calories left against your goal; edit, delete and browse earlier days.
- **Water**: a daily goal (2 L by default), +250 ml glass or a custom amount, undo, and a progress ring.
- **Fasting**: 16:8, 18:6, 20:4 or your own window, with the time you start eating saved; shows whether you are eating or fasting now and how long is left, also when the window crosses midnight.
- **Settings**: daily calorie and water goals.
- Everything is stored as plain files in the `Himma` folder (the app's private folder on Android, `~/Himma` on the desktop): `meals/<year>/<date>.csv`, `water/<year>.csv` and `.himma/settings.json`. No account, no network.

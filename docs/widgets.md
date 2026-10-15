# Himma widgets (plan, no code yet)

Status: plan. Written 2026-10-15 from section 1.7 of the Zimam master plan. Nothing here is built.

## Technology

- Jetpack Glance app widgets (Compose-style), Android only.
- The widget reads the same plain files as the app through a small read-only module, `widget-data`, that depends only on `shared` (the `Vault` readers). It never writes except for the one action below.
- Updates: the app asks the widgets to redraw after every change it saves (water, meals, doses, steps) and WorkManager refreshes them every 30 minutes and at midnight (a new day resets rings and doses). Exact-time changes (the next dose) use the alarm the medicine reminders already set.
- A tap opens the exact screen (a deep link into the section).
- No network, no background service.

## The three widgets

| Widget | Size | Shows | Tap on the widget | Button |
|---|---|---|---|---|
| Water | 2x2 | ring of today's water against the goal, the amount ("1.25 L") | opens Track on Water | one button "+ 250 ml" (the glass size is configurable) |
| Next medicine dose | 4x1 | name, dose note, time; "All done" when nothing is left today | opens Medicine | Taken and Skip when the dose is due or past |
| Steps and calories rings | 4x2 | two rings (steps, kcal) and the numbers, plus today's water as a third small ring | opens Home | none |

### Water widget behaviour (first to build)

- The "+ 250 ml" button is a Glance `ActionCallback` that adds the amount to today's water file through the same `Vault.saveWater` code path (atomic write), then redraws. It does not open the app.
- The undo is in the app (Water screen); the widget shows the new total for a few seconds as an "Added" state so a mis-tap is noticed.
- If the data file is unreadable (see H-01) the widget shows "Open Himma" and does not write.
- The glass size comes from a configuration screen shown only when the widget is added (choice: 150, 250, 330, 500 ml). Existing widgets keep their choice.

### Next medicine dose widget behaviour

- Names are private: by default the widget shows "Medicine at 08:00" without the name and dose, and shows the real text only if the user turns on "Show medicine names on widgets" (same switch family as the lock-screen setting, default off, in Settings, Notifications and reminders). A widget on a lock screen or a shared home screen must not reveal a condition.
- Taken and Skip write the dose log (`Vault.setDoseMark`) exactly like the notification buttons, then redraw.

### Steps and calories widget behaviour

- Reads the last step state saved by the app. It cannot count steps itself, so it says "as of 14:20" when the number is older than 30 minutes.

## Look and behaviour rules

- Light, dark and the app's colour theme (read `Settings.palette` and `themeMode`; the system dynamic colours are not used so the widget matches the app).
- Arabic and right to left, Arabic-Indic digits when chosen, units from the settings (kg and lb do not appear here; water is ml or L in the chosen language).
- Text size follows the system font scale; the layouts have a compact variant for 130 percent scale.
- Touch targets at least 48 dp; the ring uses the app's ring drawing.
- A configuration screen only where there is a real choice (water glass size, medicine names).
- Accessibility: every ring and number has a content description ("Water, 1.25 of 2 litres").

## Work breakdown (each an issue, each a small PR)

1. `widget-data` module: read today's water, next dose, steps, calories and goals; unit tests on a fake file system (reuse `Vault` tests helpers).
2. Water widget (2x2), with the add action, the configuration screen, light and dark renders.
3. App side: `WidgetUpdater` called from the store after saves; WorkManager periodic refresh.
4. Next medicine dose widget (4x1) with the privacy default.
5. Steps and calories widget (4x2).
6. Arabic and large-text render checks for all three, and a phone checklist entry for the owner.

## Tests

- Pure tests for what each widget shows for given files (water 0, goal passed, no doses, all taken, dose due now, step state from yesterday).
- Glance preview renders into PNG on the desktop test task where Glance allows it; otherwise the layout is built from small Compose functions that the existing offscreen render test can draw.
- Owner phone checks: add each widget, tap the button, restart the phone (widgets refresh), change language, change colour theme.

## Out of scope for the first version

Resizable layouts beyond the three sizes, lock-screen widgets, a widget for meals, anything that needs a network.

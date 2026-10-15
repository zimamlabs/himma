package dev.himma.app

import androidx.compose.runtime.Composable
import dev.himma.app.res.Res
import dev.himma.app.res.note_1
import dev.himma.app.res.note_2
import dev.himma.app.res.note_3
import dev.himma.app.res.note_4
import dev.himma.app.res.note_5
import dev.himma.app.res.note_6
import dev.himma.app.res.note_7
import dev.himma.app.res.note_8
import dev.himma.app.res.note_9
import dev.himma.app.res.note_10
import dev.himma.app.res.note_11
import dev.himma.app.res.note_12
import dev.himma.app.res.note_13
import dev.himma.app.res.note_14
import dev.himma.app.res.note_15
import dev.himma.app.res.note_16
import dev.himma.app.res.note_17
import dev.himma.app.res.note_18
import dev.himma.app.res.note_19
import dev.himma.app.res.note_20
import dev.himma.app.res.note_21
import dev.himma.app.res.greet_morning
import dev.himma.app.res.greet_afternoon
import dev.himma.app.res.greet_evening
import org.jetbrains.compose.resources.StringResource

/** Texts that the shared module writes in English (release notes and greetings) and their translations. */
internal val knownTexts: Map<String, StringResource> = mapOf(
    "A Home page with today at a glance and shortcuts." to Res.string.note_1,
    "Medicine reminders: times, days, and Taken or Skip on the notification." to Res.string.note_2,
    "Your data can live in one visible Zimam/Himma folder (allow All files access)." to Res.string.note_3,
    "A short tour on first install and this note after each update." to Res.string.note_4,
    "A richer Home: rings, a macro bar, 7-day charts and a weekly summary." to Res.string.note_5,
    "Gym: routines, workouts with every set, personal bests and a rest timer." to Res.string.note_6,
    "Steps from the phone's step counter, a weight log with a trend, and meal templates." to Res.string.note_7,
    "Light, Dark or System theme." to Res.string.note_8,
    "A new look: warm surfaces, a soft orange glow, rounded cards and the diamond accent." to Res.string.note_9,
    "A new About page." to Res.string.note_10,
    "Himma now speaks Arabic, with a right-to-left layout. Choose the language in Settings." to Res.string.note_11,
    "Numbers can be shown in Arabic-Indic digits." to Res.string.note_12,
    "Today, Meals, Water and Fasting." to Res.string.note_13,
    "A new app icon: a rising peak with a diamond." to Res.string.note_14,
    "A side menu, and a swipe between sections." to Res.string.note_15,
    "Settings as sections: six colour themes, text size, density and section order." to Res.string.note_16,
    "Reminders that tell you when they cannot work, and missed doses are announced after a restart." to Res.string.note_17,
    "Hold a meal, medicine, routine, workout or weight for a menu: edit, history, duplicate, delete." to Res.string.note_18,
    "Safer data: a damaged file is kept as a copy, export and delete all, and a confirmation before deleting." to Res.string.note_19,
    "Kg or lb, units and numbers in your own language, and correct Arabic plurals." to Res.string.note_20,
    "A water widget for the home screen: today's water and a button that adds a glass." to Res.string.note_21,
    "Good morning" to Res.string.greet_morning,
    "Good afternoon" to Res.string.greet_afternoon,
    "Good evening" to Res.string.greet_evening,
)

/** The translation of a shared English text, or the text itself when it is not one we know. */
@Composable
fun tr(text: String): String = knownTexts[text]?.let { s(it) } ?: text

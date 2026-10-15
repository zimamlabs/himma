package dev.himma.core

/** The changes of one released version, shown once after an update. */
data class ReleaseNote(val version: String, val bullets: List<String>)

/** Short notes per version, newest last; the same text as the CHANGELOG, trimmed to what a user notices. */
val releaseNotes: List<ReleaseNote> = listOf(
    ReleaseNote("0.1.0", listOf("Today, Meals, Water and Fasting.")),
    ReleaseNote("0.2.0", listOf(
        "A Home page with today at a glance and shortcuts.",
        "Medicine reminders: times, days, and Taken or Skip on the notification.",
        "Your data can live in one visible Zimam/Himma folder (allow All files access).",
        "A short tour on first install and this note after each update.",
    )),
    ReleaseNote("0.3.0", listOf(
        "A richer Home: rings, a macro bar, 7-day charts and a weekly summary.",
        "Gym: routines, workouts with every set, personal bests and a rest timer.",
        "Steps from the phone's step counter, a weight log with a trend, and meal templates.",
        "Light, Dark or System theme.",
    )),
    ReleaseNote("0.3.1", listOf(
        "A new look: warm surfaces, a soft orange glow, rounded cards and the diamond accent.",
        "A new About page.",
    )),
    ReleaseNote("0.4.0", listOf(
        "Himma now speaks Arabic, with a right-to-left layout. Choose the language in Settings.",
        "Numbers can be shown in Arabic-Indic digits.",
    )),
    ReleaseNote("0.4.1", listOf(
        "A new app icon: a rising peak with a diamond.",
    )),
    ReleaseNote("0.5.0", listOf(
        "A side menu, and a swipe between sections.",
        "Settings as sections: six colour themes, text size, density and section order.",
        "Reminders that tell you when they cannot work, and missed doses are announced after a restart.",
        "Hold a meal, medicine, routine, workout or weight for a menu: edit, history, duplicate, delete.",
        "Safer data: a damaged file is kept as a copy, export and delete all, and a confirmation before deleting.",
        "Kg or lb, units and numbers in your own language, and correct Arabic plurals.",
    )),
)

/** What the first open should show. */
sealed interface Startup {
    /** First ever run: show the tour. */
    data object Fresh : Startup
    /** Opened after an update: show these notes once. */
    data class Updated(val notes: List<ReleaseNote>) : Startup
    /** Nothing to show. */
    data object Nothing : Startup
}

/** "0.10.1" as comparable numbers; a part that is not a number counts as 0. */
fun versionParts(v: String): List<Int> = v.trim().removePrefix("v").split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }

fun compareVersions(a: String, b: String): Int {
    val x = versionParts(a); val y = versionParts(b)
    for (i in 0 until maxOf(x.size, y.size)) {
        val c = (x.getOrElse(i) { 0 }).compareTo(y.getOrElse(i) { 0 })
        if (c != 0) return c
    }
    return 0
}

/**
 * Decides the first screen. [lastSeen] is the version recorded last time (null if none). When nothing is recorded but
 * [hasData] is true the app was installed before notes existed, so it counts as an update from 0.1.0 rather than a fresh
 * install. A downgrade shows nothing.
 */
fun startupFor(lastSeen: String?, current: String, hasData: Boolean, notes: List<ReleaseNote> = releaseNotes): Startup {
    val seen = lastSeen ?: if (hasData) "0.1.0" else return Startup.Fresh
    if (compareVersions(seen, current) >= 0) return Startup.Nothing
    val fresh = notes.filter { compareVersions(it.version, seen) > 0 && compareVersions(it.version, current) <= 0 }.sortedByDescending { n -> versionParts(n.version).fold(0L) { acc, p -> acc * 1000 + p } }
    return if (fresh.isEmpty()) Startup.Nothing else Startup.Updated(fresh)
}

fun greeting(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

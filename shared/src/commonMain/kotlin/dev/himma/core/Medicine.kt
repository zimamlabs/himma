package dev.himma.core

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

/**
 * A medicine taken at fixed times. [times] are "HH:MM"; [days] are weekdays 1 (Monday) to 7 (Sunday),
 * and an empty set means every day.
 */
@Serializable
data class Medicine(
    val id: String,
    val name: String,
    val dose: String = "",
    val times: List<String>,
    val days: Set<Int> = emptySet(),
)

/** One scheduled dose of a [Medicine] on a [date]. */
data class Dose(val medicine: Medicine, val date: LocalDate, val time: String) {
    val minutes: Int get() = parseClock(time) ?: 0
    val at: LocalDateTime get() = LocalDateTime(date, LocalTime(minutes / 60, minutes % 60))
    val key: String get() = "${medicine.id}|$date|$time"
}

enum class DoseMark { Taken, Skipped }

/** Where one dose stands: marked, still to come, or past with no mark. */
enum class DoseStatus { Taken, Skipped, Upcoming, Missed }

fun Medicine.appliesOn(date: LocalDate): Boolean = days.isEmpty() || date.dayOfWeek.ordinal + 1 in days

/** Every dose on [date], earliest first. Times that are not valid clock times are ignored. */
fun dosesOn(medicines: List<Medicine>, date: LocalDate): List<Dose> =
    medicines.filter { it.appliesOn(date) }
        .flatMap { m -> m.times.mapNotNull { t -> parseClock(t)?.let { Dose(m, date, formatClock(it)) } } }
        .sortedWith(compareBy({ it.minutes }, { it.medicine.name }))

/** The first moment strictly after [after] with a dose, looking up to a week ahead; null when there is none. */
fun nextDoseTime(medicines: List<Medicine>, after: LocalDateTime): LocalDateTime? {
    for (offset in 0..7) {
        val date = after.date.plus(DatePeriod(days = offset))
        dosesOn(medicines, date).firstOrNull { it.at > after }?.let { return it.at }
    }
    return null
}

/** Doses scheduled after [from] up to and including [to], oldest first. Handles a window that crosses midnight. */
fun dosesBetween(medicines: List<Medicine>, from: LocalDateTime, to: LocalDateTime): List<Dose> {
    if (to <= from) return emptyList()
    val out = ArrayList<Dose>()
    var date = from.date
    while (date <= to.date) {
        out += dosesOn(medicines, date).filter { it.at > from && it.at <= to }
        date = date.plus(DatePeriod(days = 1))
    }
    return out
}

fun doseStatus(dose: Dose, marks: Map<String, DoseMark>, now: LocalDateTime): DoseStatus = when (marks[dose.key]) {
    DoseMark.Taken -> DoseStatus.Taken
    DoseMark.Skipped -> DoseStatus.Skipped
    null -> if (dose.at > now) DoseStatus.Upcoming else DoseStatus.Missed
}

/** Counts for the days from [from] to [to] inclusive; doses still to come are not counted. */
data class Adherence(val taken: Int, val skipped: Int, val missed: Int) {
    val due: Int get() = taken + skipped + missed
    /** Share of due doses that were taken, 0 to 1; null when nothing was due. */
    val rate: Float? get() = if (due == 0) null else taken.toFloat() / due
}

fun adherence(medicines: List<Medicine>, marks: Map<String, DoseMark>, from: LocalDate, to: LocalDate, now: LocalDateTime): Adherence {
    var taken = 0; var skipped = 0; var missed = 0
    var date = from
    while (date <= to) {
        for (d in dosesOn(medicines, date)) when (doseStatus(d, marks, now)) {
            DoseStatus.Taken -> taken++
            DoseStatus.Skipped -> skipped++
            DoseStatus.Missed -> missed++
            DoseStatus.Upcoming -> {}
        }
        date = date.plus(DatePeriod(days = 1))
    }
    return Adherence(taken, skipped, missed)
}

/** "Mon, Wed" or "Every day". */
fun formatDays(days: Set<Int>): String {
    if (days.isEmpty() || days.size == 7) return "Every day"
    val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return days.sorted().filter { it in 1..7 }.joinToString(", ") { names[it - 1] }
}

/** Reads "08:00, 20:30" into clean, sorted, unique "HH:MM" times; null when any part is not a time or the list is empty. */
fun parseTimes(text: String): List<String>? {
    val parts = text.split(',', ';', ' ').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) return null
    val minutes = parts.map { parseClock(it) ?: return null }
    return minutes.distinct().sorted().map { formatClock(it) }
}

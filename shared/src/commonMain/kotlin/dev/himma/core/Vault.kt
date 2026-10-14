package dev.himma.core

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

/**
 * A Himma folder of plain files, readable in any editor.
 *
 * ```
 * meals/<year>/<date>.csv    id, time, name, kcal, protein, carbs, fat (one file per day)
 * water/<year>.csv           date, ml
 * .himma/settings.json       goals and the fasting window
 * ```
 */
class Vault(
    val fs: FileSystem,
    val root: Path,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

    init { fs.createDirectories(root) }

    fun now(): LocalDateTime = clock.now().toLocalDateTime(zone)

    fun today(): LocalDate = now().date

    private fun read(path: Path): String? = if (fs.exists(path)) fs.read(path) { readUtf8() } else null

    // Write to a temp file and move it over, so a crash never leaves a half-written file.
    private fun write(path: Path, text: String) {
        path.parent?.let { fs.createDirectories(it) }
        val tmp = path.parent!! / (path.name + ".tmp")
        fs.write(tmp) { writeUtf8(text) }
        fs.atomicMove(tmp, path)
    }

    private val settingsPath get() = root / ".himma" / "settings.json"

    fun settings(): Settings = read(settingsPath)?.let { runCatching { json.decodeFromString(Settings.serializer(), it) }.getOrNull() } ?: Settings()

    fun saveSettings(settings: Settings) = write(settingsPath, json.encodeToString(Settings.serializer(), settings))

    private fun mealPath(date: LocalDate) = root / "meals" / date.year.toString() / "$date.csv"

    fun meals(date: LocalDate): List<Meal> = Csv.parse(read(mealPath(date)) ?: "").drop(1).mapNotNull { r ->
        runCatching { Meal(r[0], r[1], r[2], r[3].toInt(), r.getOrNull(4)?.toIntOrNull(), r.getOrNull(5)?.toIntOrNull(), r.getOrNull(6)?.toIntOrNull()) }.getOrNull()
    }

    /** Adds the meal, or replaces the one with the same id. */
    fun saveMeal(date: LocalDate, meal: Meal): Meal {
        val saved = if (meal.id.isEmpty()) meal.copy(id = newId()) else meal
        writeMeals(date, meals(date).filter { it.id != saved.id } + saved)
        return saved
    }

    fun deleteMeal(date: LocalDate, id: String) = writeMeals(date, meals(date).filter { it.id != id })

    private fun writeMeals(date: LocalDate, list: List<Meal>) {
        val path = mealPath(date)
        if (list.isEmpty()) { fs.delete(path, mustExist = false); return }
        val rows = listOf(listOf("id", "time", "name", "kcal", "protein", "carbs", "fat")) +
            list.sortedBy { it.time }.map { listOf(it.id, it.time, it.name, it.kcal.toString(), it.protein?.toString().orEmpty(), it.carbs?.toString().orEmpty(), it.fat?.toString().orEmpty()) }
        write(path, Csv.format(rows))
    }

    private fun waterPath(year: Int) = root / "water" / "$year.csv"

    private fun waterYear(year: Int): Map<LocalDate, Int> = Csv.parse(read(waterPath(year)) ?: "").drop(1).mapNotNull { r ->
        runCatching { LocalDate.parse(r[0]) to r[1].toInt() }.getOrNull()
    }.toMap()

    fun water(date: LocalDate): Int = waterYear(date.year)[date] ?: 0

    fun saveWater(date: LocalDate, ml: Int) {
        val days = waterYear(date.year).toMutableMap()
        if (ml <= 0) days.remove(date) else days[date] = ml
        write(waterPath(date.year), Csv.format(listOf(listOf("date", "ml")) + days.toSortedMap().map { listOf(it.key.toString(), it.value.toString()) }))
    }

    /** True when the folder already holds any of Himma's files (an install from before notes were recorded). */
    fun hasData(): Boolean = listOf("meals", "water", "medicines").any { fs.exists(root / it) } || fs.exists(settingsPath)

    private val medicinesPath get() = root / "medicines" / "medicines.json"

    fun medicines(): List<Medicine> = read(medicinesPath)?.let { runCatching { json.decodeFromString(ListSerializer(Medicine.serializer()), it) }.getOrNull() } ?: emptyList()

    fun saveMedicines(list: List<Medicine>) = write(medicinesPath, json.encodeToString(ListSerializer(Medicine.serializer()), list))

    private val doseLogPath get() = root / "medicines" / "log.csv"

    /** Marks by [Dose.key]. */
    fun doseMarks(): Map<String, DoseMark> = Csv.parse(read(doseLogPath) ?: "").drop(1).mapNotNull { r ->
        runCatching { "${r[1]}|${r[0]}|${r[2]}" to DoseMark.valueOf(r[3]) }.getOrNull()
    }.toMap()

    /** Records [mark] for a dose, or clears it when null. */
    fun setDoseMark(dose: Dose, mark: DoseMark?) {
        val marks = doseMarks().toMutableMap()
        if (mark == null) marks.remove(dose.key) else marks[dose.key] = mark
        val rows = marks.entries.sortedBy { it.key }.map { (k, v) -> k.split('|').let { listOf(it[1], it[0], it[2], v.name) } }
        write(doseLogPath, Csv.format(listOf(listOf("date", "medicine", "time", "mark")) + rows))
    }
}

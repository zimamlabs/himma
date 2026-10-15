package dev.himma.core

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
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
 * .himma/settings.json       goals, theme and the fasting window
 * .himma/steps.json          where the phone's step counter stood last
 * meals/templates.json       meals saved to log again
 * gym/routines.json          routines (exercises, sets, reps, weight)
 * gym/<year>.json            logged workouts
 * steps/<year>.csv           date, steps
 * body/weight.csv            date, kg
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

    private val found = ArrayList<DataProblem>()

    /** Files read in this session that could not be understood; each was copied to [DataProblem.copy] first. */
    fun problems(): List<DataProblem> = found.toList()

    fun clearProblems() = found.clear()

    /** Saves the unreadable [text] beside [path] as `name.corrupt-<stamp>` (once per distinct content) and records the problem. */
    private fun keepCopy(path: Path, text: String) {
        val dir = path.parent ?: return
        val prefix = path.name + ".corrupt-"
        val same = fs.listOrNull(dir).orEmpty().firstOrNull { it.name.startsWith(prefix) && read(it) == text }
        val copy = same ?: newCopyPath(dir, prefix).also { fs.write(it) { writeUtf8(text) } }
        val rel = path.relativeTo(root).toString()
        if (found.none { it.file == rel }) found += DataProblem(rel, copy.name)
    }

    private fun newCopyPath(dir: Path, prefix: String): Path {
        val stamp = now().toString().substringBefore('.').replace("-", "").replace(":", "").replace("T", "-")
        var candidate = dir / (prefix + stamp)
        var n = 1
        while (fs.exists(candidate)) candidate = dir / "$prefix$stamp-${n++}"
        return candidate
    }

    private fun <T> readJson(path: Path, serializer: KSerializer<T>, default: T): T {
        val text = read(path)?.takeIf { it.isNotBlank() } ?: return default
        return runCatching { json.decodeFromString(serializer, text) }.getOrElse { keepCopy(path, text); default }
    }

    private fun <T : Any> readCsv(path: Path, parse: (List<String>) -> T): List<T> {
        val text = read(path) ?: return emptyList()
        var bad = false
        val out = Csv.parse(text).drop(1).mapNotNull { r -> runCatching { parse(r) }.getOrElse { bad = true; null } }
        if (bad) keepCopy(path, text)
        return out
    }

    private val settingsPath get() = root / ".himma" / "settings.json"

    fun settings(): Settings = readJson(settingsPath, Settings.serializer(), Settings())

    fun saveSettings(settings: Settings) = write(settingsPath, json.encodeToString(Settings.serializer(), settings))

    private fun mealPath(date: LocalDate) = root / "meals" / date.year.toString() / "$date.csv"

    fun meals(date: LocalDate): List<Meal> = readCsv(mealPath(date)) { r ->
        Meal(r[0], r[1], r[2], r[3].toInt(), r.getOrNull(4)?.toIntOrNull(), r.getOrNull(5)?.toIntOrNull(), r.getOrNull(6)?.toIntOrNull())
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

    private fun waterYear(year: Int): Map<LocalDate, Int> = readCsv(waterPath(year)) { r -> LocalDate.parse(r[0]) to r[1].toInt() }.toMap()

    fun water(date: LocalDate): Int = waterYear(date.year)[date] ?: 0

    fun saveWater(date: LocalDate, ml: Int) {
        val days = waterYear(date.year).toMutableMap()
        if (ml <= 0) days.remove(date) else days[date] = ml
        write(waterPath(date.year), Csv.format(listOf(listOf("date", "ml")) + days.toSortedMap().map { listOf(it.key.toString(), it.value.toString()) }))
    }

    /** True when the folder already holds any of Himma's files (an install from before notes were recorded). */
    fun hasData(): Boolean = listOf("meals", "water", "medicines", "gym", "steps", "body").any { fs.exists(root / it) } || fs.exists(settingsPath)

    private val medicinesPath get() = root / "medicines" / "medicines.json"

    fun medicines(): List<Medicine> = readJson(medicinesPath, ListSerializer(Medicine.serializer()), emptyList())

    fun saveMedicines(list: List<Medicine>) = write(medicinesPath, json.encodeToString(ListSerializer(Medicine.serializer()), list))

    private val doseLogPath get() = root / "medicines" / "log.csv"

    /** Marks by [Dose.key]. */
    fun doseMarks(): Map<String, DoseMark> = readCsv(doseLogPath) { r -> "${r[1]}|${r[0]}|${r[2]}" to DoseMark.valueOf(r[3]) }.toMap()

    /** Records [mark] for a dose, or clears it when null. */
    fun setDoseMark(dose: Dose, mark: DoseMark?) {
        val marks = doseMarks().toMutableMap()
        if (mark == null) marks.remove(dose.key) else marks[dose.key] = mark
        val rows = marks.entries.sortedBy { it.key }.map { (k, v) -> k.split('|').let { listOf(it[1], it[0], it[2], v.name) } }
        write(doseLogPath, Csv.format(listOf(listOf("date", "medicine", "time", "mark")) + rows))
    }

    private val templatesPath get() = root / "meals" / "templates.json"

    fun templates(): List<MealTemplate> = readJson(templatesPath, ListSerializer(MealTemplate.serializer()), emptyList())

    fun saveTemplates(list: List<MealTemplate>) = write(templatesPath, json.encodeToString(ListSerializer(MealTemplate.serializer()), list))

    private val routinesPath get() = root / "gym" / "routines.json"

    fun routines(): List<Routine> = readJson(routinesPath, ListSerializer(Routine.serializer()), emptyList())

    fun saveRoutines(list: List<Routine>) = write(routinesPath, json.encodeToString(ListSerializer(Routine.serializer()), list))

    private fun workoutsPath(year: Int) = root / "gym" / "$year.json"

    fun workouts(year: Int): List<Workout> = readJson(workoutsPath(year), ListSerializer(Workout.serializer()), emptyList())

    /** Every logged workout, oldest first. */
    fun allWorkouts(): List<Workout> = (fs.listOrNull(root / "gym").orEmpty().mapNotNull { it.name.removeSuffix(".json").toIntOrNull() }).sorted().flatMap { workouts(it) }.sortedBy { it.date }

    /** Adds the workout, or replaces the one with the same id. */
    fun saveWorkout(w: Workout): Workout {
        val saved = if (w.id.isEmpty()) w.copy(id = newId()) else w
        val year = saved.date.take(4).toInt()
        write(workoutsPath(year), json.encodeToString(ListSerializer(Workout.serializer()), workouts(year).filter { it.id != saved.id } + saved))
        return saved
    }

    fun deleteWorkout(w: Workout) {
        val year = w.date.take(4).toInt()
        write(workoutsPath(year), json.encodeToString(ListSerializer(Workout.serializer()), workouts(year).filter { it.id != w.id }))
    }

    private fun stepsPath(year: Int) = root / "steps" / "$year.csv"

    private fun stepsYear(year: Int): Map<LocalDate, Int> = readCsv(stepsPath(year)) { r -> LocalDate.parse(r[0]) to r[1].toInt() }.toMap()

    fun steps(date: LocalDate): Int = stepsYear(date.year)[date] ?: 0

    fun saveSteps(date: LocalDate, steps: Int) {
        val days = stepsYear(date.year).toMutableMap()
        if (steps <= 0) days.remove(date) else days[date] = steps
        write(stepsPath(date.year), Csv.format(listOf(listOf("date", "steps")) + days.toSortedMap().map { listOf(it.key.toString(), it.value.toString()) }))
    }

    private val stepStatePath get() = root / ".himma" / "steps.json"

    fun stepState(): StepState = readJson(stepStatePath, StepState.serializer(), StepState())

    fun saveStepState(s: StepState) = write(stepStatePath, json.encodeToString(StepState.serializer(), s))

    private val weightPath get() = root / "body" / "weight.csv"

    fun weights(): List<WeightEntry> = readCsv(weightPath) { r -> WeightEntry(LocalDate.parse(r[0]), r[1].toDouble()) }.sortedBy { it.date }

    /** One weight per day: logging again on the same day replaces it. [kg] of zero or less removes the day. */
    fun saveWeight(date: LocalDate, kg: Double) {
        val rest = weights().filter { it.date != date }
        val all = if (kg <= 0) rest else (rest + WeightEntry(date, kg)).sortedBy { it.date }
        write(weightPath, Csv.format(listOf(listOf("date", "kg")) + all.map { listOf(it.date.toString(), it.kg.toString()) }))
    }
}

/** A data file that could not be read; [copy] is the file name its content was kept under, next to the original. */
data class DataProblem(val file: String, val copy: String)

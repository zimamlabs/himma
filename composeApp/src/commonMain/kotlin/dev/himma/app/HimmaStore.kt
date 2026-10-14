package dev.himma.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.himma.core.DayRecord
import dev.himma.core.Exercise
import dev.himma.core.MealTemplate
import dev.himma.core.Routine
import dev.himma.core.SetEntry
import dev.himma.core.StepState
import dev.himma.core.Workout
import dev.himma.core.WeightEntry
import dev.himma.core.lastDays
import dev.himma.core.stepUpdate
import dev.himma.core.volume
import dev.himma.core.Dose
import dev.himma.core.DoseMark
import dev.himma.core.Meal
import dev.himma.core.Medicine
import dev.himma.core.dosesOn
import dev.himma.core.newId
import dev.himma.core.Settings
import dev.himma.core.Startup
import dev.himma.core.Vault
import dev.himma.core.startupFor
import dev.himma.core.addWater
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus

class HimmaStore(vault: Vault, val platform: PlatformActions = NoPlatform()) {
    private var vault: Vault = vault

    var now by mutableStateOf(vault.now())
        private set
    var settings by mutableStateOf(vault.settings())
        private set
    /** The day shown in the Meals tab; Today and Water always use [today]. */
    var mealDay by mutableStateOf(vault.today())
        private set
    var meals by mutableStateOf(emptyList<Meal>())
        private set
    var todayMeals by mutableStateOf(emptyList<Meal>())
        private set
    var waterMl by mutableStateOf(0)
        private set
    var medicines by mutableStateOf(emptyList<Medicine>())
        private set
    var doseMarks by mutableStateOf(emptyMap<String, DoseMark>())
        private set
    var routines by mutableStateOf(emptyList<Routine>())
        private set
    var workouts by mutableStateOf(emptyList<Workout>())
        private set
    var templates by mutableStateOf(emptyList<MealTemplate>())
        private set
    var recent by mutableStateOf(emptyList<dev.himma.core.Meal>())
        private set
    var weights by mutableStateOf(emptyList<WeightEntry>())
        private set
    var stepsToday by mutableStateOf(0)
        private set
    /** The last seven days, oldest first, for the Home charts and the weekly summary. */
    var week by mutableStateOf(emptyList<DayRecord>())
        private set
    private var stepState = StepState()
    private var stepSavedAt = 0L

    /** Called after the medicine list changes, so the phone can set its alarms again. */
    var onMedicinesChanged: () -> Unit = {}
    /** What the first screen shows: the tour on a fresh install, the notes after an update, or nothing. Decided once, before anything is saved. */
    var startup by mutableStateOf<Startup>(Startup.Nothing)
        private set
    var folderPath by mutableStateOf(platform.folderPath)
        private set
    var sharedFolder by mutableStateOf(platform.sharedFolder)
        private set
    var canRemoveOld by mutableStateOf(false)
        private set
    private var lastToday = vault.today()
    // Amounts added since the app opened, newest last, so Undo takes back exactly what was tapped.
    private val added = ArrayList<Int>()

    val today: LocalDate get() = now.date
    val nowMinutes: Int get() = now.hour * 60 + now.minute

    init {
        startup = startupFor(vault.settings().lastSeenVersion, APP_VERSION, vault.hasData())
        reload()
        refreshFolderInfo()
    }

    /** Points the app at another data folder (after the phone allowed All files access). */
    fun replaceVault(next: Vault) { vault = next; lastToday = next.today(); added.clear(); reload(); refreshFolderInfo() }

    fun refreshFolderInfo() {
        folderPath = platform.folderPath; sharedFolder = platform.sharedFolder; canRemoveOld = platform.canRemoveOldCopy()
    }

    fun removeOldCopy(): Boolean = platform.removeOldCopy().also { refreshFolderInfo() }

    /** Records that the tour and this version's notes were seen. */
    fun markSeen() { saveSettings(settings.copy(lastSeenVersion = APP_VERSION, tourDone = true)); startup = Startup.Nothing }

    /** Re-reads the files and the clock; moves the Meals tab along when the day rolls over. */
    fun reload() {
        now = vault.now()
        if (mealDay == lastToday) mealDay = today
        if (today != lastToday) added.clear()
        lastToday = today
        settings = vault.settings()
        meals = vault.meals(mealDay)
        todayMeals = vault.meals(today)
        waterMl = vault.water(today)
        medicines = vault.medicines()
        doseMarks = vault.doseMarks()
        routines = vault.routines()
        workouts = vault.allWorkouts()
        templates = vault.templates()
        weights = vault.weights()
        stepState = vault.stepState()
        stepsToday = if (stepState.date == today.toString()) stepState.steps else vault.steps(today)
        recent = recentMealsFor()
        rebuildWeek()
    }

    private fun recentMealsFor(): List<dev.himma.core.Meal> =
        dev.himma.core.recentMeals(lastDays(today, 14).reversed().flatMap { vault.meals(it).sortedByDescending { m -> m.time } }, 8)

    private fun rebuildWeek() {
        week = lastDays(today, 7).map { d ->
            val ws = workouts.filter { it.date == d.toString() }
            DayRecord(d, kcal = vault.meals(d).sumOf { it.kcal }, waterMl = vault.water(d), steps = if (d == today) stepsToday else vault.steps(d),
                workouts = ws.size, volumeKg = ws.sumOf { volume(it.sets) })
        }
    }

    fun tick() { if (vault.today() != lastToday) reload() else now = vault.now() }

    fun saveSettings(s: Settings) { vault.saveSettings(s); settings = s }

    fun shiftMealDay(days: Int) { mealDay = mealDay.plus(DatePeriod(days = days)); meals = vault.meals(mealDay) }

    fun saveMeal(meal: Meal) { vault.saveMeal(mealDay, meal); refreshMeals() }

    fun deleteMeal(id: String) { vault.deleteMeal(mealDay, id); refreshMeals() }

    private fun refreshMeals() { meals = vault.meals(mealDay); todayMeals = vault.meals(today); recent = recentMealsFor(); rebuildWeek() }

    val canUndoWater: Boolean get() = added.isNotEmpty()

    fun addWater(ml: Int) {
        waterMl = addWater(waterMl, ml)
        vault.saveWater(today, waterMl)
        added += ml
        rebuildWeek()
    }

    fun undoWater() {
        val last = added.removeLastOrNull() ?: return
        waterMl = addWater(waterMl, -last)
        vault.saveWater(today, waterMl)
        rebuildWeek()
    }

    val todayDoses: List<Dose> get() = dosesOn(medicines, today)

    fun saveMedicine(m: Medicine) {
        val saved = if (m.id.isEmpty()) m.copy(id = newId()) else m
        vault.saveMedicines(medicines.filter { it.id != saved.id } + saved)
        medicines = vault.medicines()
        onMedicinesChanged()
    }

    fun deleteMedicine(id: String) {
        vault.saveMedicines(medicines.filter { it.id != id })
        medicines = vault.medicines()
        onMedicinesChanged()
    }

    /** Marks a dose Taken or Skipped, or clears the mark with null. */
    fun markDose(dose: Dose, mark: DoseMark?) { vault.setDoseMark(dose, mark); doseMarks = vault.doseMarks() }

    fun nowTime(): String = dev.himma.core.formatClock(nowMinutes)

    // Meal templates: tap to log again.
    fun saveTemplate(t: MealTemplate) {
        val saved = if (t.id.isEmpty()) t.copy(id = newId()) else t
        vault.saveTemplates(templates.filter { it.id != saved.id && !(it.name.equals(saved.name, true) && it.kcal == saved.kcal) } + saved); templates = vault.templates()
    }

    fun deleteTemplate(id: String) { vault.saveTemplates(templates.filter { it.id != id }); templates = vault.templates() }

    /** Logs the same meal again, today, at the current time. */
    fun logAgain(name: String, kcal: Int, protein: Int?, carbs: Int?, fat: Int?) {
        vault.saveMeal(today, dev.himma.core.Meal("", nowTime(), name, kcal, protein, carbs, fat))
        refreshMeals()
    }

    // Gym.
    fun saveRoutine(r: Routine) {
        val saved = if (r.id.isEmpty()) r.copy(id = newId()) else r
        vault.saveRoutines(routines.filter { it.id != saved.id } + saved); routines = vault.routines()
    }

    fun deleteRoutine(id: String) { vault.saveRoutines(routines.filter { it.id != id }); routines = vault.routines() }

    fun saveWorkout(w: Workout) { vault.saveWorkout(w); workouts = vault.allWorkouts(); rebuildWeek() }

    fun deleteWorkout(w: Workout) { vault.deleteWorkout(w); workouts = vault.allWorkouts(); rebuildWeek() }

    // Weight.
    fun saveWeight(kg: Double, date: LocalDate = today) { vault.saveWeight(date, kg); weights = vault.weights() }

    // Steps: the phone sends the counter; it is saved at most every 15 seconds and when the app goes to the background.
    fun onStepCounter(counter: Long) {
        val todayDate = vault.today()
        if (stepState.date.isNotEmpty() && stepState.date != todayDate.toString()) vault.saveSteps(LocalDate.parse(stepState.date), stepState.steps)
        stepState = stepUpdate(stepState, todayDate, counter)
        stepsToday = stepState.steps
        val nowMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        if (nowMs - stepSavedAt > 15_000) flushSteps(nowMs)
    }

    fun flushSteps(nowMs: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()) {
        if (stepState.date.isEmpty()) return
        vault.saveStepState(stepState); vault.saveSteps(LocalDate.parse(stepState.date), stepState.steps); stepSavedAt = nowMs
        rebuildWeek()
    }
}

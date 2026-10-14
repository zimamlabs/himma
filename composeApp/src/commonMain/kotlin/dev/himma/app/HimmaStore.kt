package dev.himma.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    }

    fun tick() { if (vault.today() != lastToday) reload() else now = vault.now() }

    fun saveSettings(s: Settings) { vault.saveSettings(s); settings = s }

    fun shiftMealDay(days: Int) { mealDay = mealDay.plus(DatePeriod(days = days)); meals = vault.meals(mealDay) }

    fun saveMeal(meal: Meal) { vault.saveMeal(mealDay, meal); refreshMeals() }

    fun deleteMeal(id: String) { vault.deleteMeal(mealDay, id); refreshMeals() }

    private fun refreshMeals() { meals = vault.meals(mealDay); todayMeals = vault.meals(today) }

    val canUndoWater: Boolean get() = added.isNotEmpty()

    fun addWater(ml: Int) {
        waterMl = addWater(waterMl, ml)
        vault.saveWater(today, waterMl)
        added += ml
    }

    fun undoWater() {
        val last = added.removeLastOrNull() ?: return
        waterMl = addWater(waterMl, -last)
        vault.saveWater(today, waterMl)
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
}

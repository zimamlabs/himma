package dev.himma.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.himma.core.Meal
import dev.himma.core.Settings
import dev.himma.core.Vault
import dev.himma.core.addWater
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus

class HimmaStore(val vault: Vault) {
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
    private var lastToday = vault.today()
    // Amounts added since the app opened, newest last, so Undo takes back exactly what was tapped.
    private val added = ArrayList<Int>()

    val today: LocalDate get() = now.date
    val nowMinutes: Int get() = now.hour * 60 + now.minute

    init { reload() }

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

    fun nowTime(): String = dev.himma.core.formatClock(nowMinutes)
}

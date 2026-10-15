package dev.himma.app

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import android.content.Intent
import dev.himma.core.Settings
import dev.himma.core.Vault
import dev.himma.core.addWater
import dev.himma.core.waterAmount
import dev.himma.core.waterProgress
import dev.himma.health.R
import okio.FileSystem
import okio.Path.Companion.toOkioPath

/** What the water widget shows; read from the plain files each time it draws. */
internal data class WaterWidgetState(val ml: Int, val goalMl: Int, val settings: Settings)

internal object WaterWidgetData {
    const val GLASS_ML = 250

    private fun vault(context: Context) = Vault(FileSystem.SYSTEM, Storage(context).currentRoot().also { it.mkdirs() }.toOkioPath())

    fun read(context: Context): WaterWidgetState {
        val v = vault(context)
        val settings = v.settings()
        return WaterWidgetState(v.water(v.today()), settings.waterGoalMl, settings)
    }

    /** Adds one glass to today's file with the same atomic write as the app; false when the file could not be read. */
    fun addGlass(context: Context): Boolean {
        val v = vault(context)
        if (v.problems().isNotEmpty()) return false
        v.saveWater(v.today(), addWater(v.water(v.today()), GLASS_ML))
        return true
    }
}

/** The 2x2 water widget: today's amount against the goal and a button that adds a glass without opening the app. */
class WaterWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WaterWidgetData.read(context)
        provideContent { WaterContent(context, state) }
    }
}

private fun pair(light: Color, dark: Color) = ColorProvider(day = light, night = dark)

@androidx.compose.runtime.Composable
private fun WaterContent(context: Context, state: WaterWidgetState) {
    val accent = accents[state.settings.palette] ?: accents.getValue("zimam")
    val amount = waterAmount(state.ml).let { context.getString(if (it.litres) R.string.widget_litres else R.string.widget_ml, it.value) }
    val goal = waterAmount(state.goalMl).let { context.getString(if (it.litres) R.string.widget_litres else R.string.widget_ml, it.value) }
    Column(
        GlanceModifier.fillMaxSize().cornerRadius(24.dp).background(pair(Color(0xFFFFFFFF), Color(0xFF1A1613))).padding(12.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.Vertical.CenterVertically, horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Text(context.getString(R.string.widget_water_title), style = TextStyle(color = pair(Color(0xFF6B6157), Color(0xFFA89E92)), fontSize = 12.sp))
        Text(amount, style = TextStyle(color = pair(Color(0xFF1A1613), Color(0xFFF5EFE6)), fontSize = 22.sp, fontWeight = FontWeight.Bold))
        Text(context.getString(R.string.widget_of_goal, goal), style = TextStyle(color = pair(Color(0xFF6B6157), Color(0xFFA89E92)), fontSize = 12.sp))
        LinearProgressIndicator(waterProgress(state.ml, state.goalMl), GlanceModifier.fillMaxWidth().padding(vertical = 6.dp), color = pair(accent.light, accent.dark), backgroundColor = pair(Color(0xFFF1E7DA), Color(0xFF2A241F)))
        Button(
            context.getString(R.string.widget_add_glass), actionRunCallback<AddGlassAction>(), GlanceModifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = pair(accent.light, accent.dark), contentColor = pair(accent.onLight, accent.onDark)),
        )
    }
}

class WaterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WaterWidget()
}

/** The widget button: adds a glass, then redraws every water widget. */
class AddGlassAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WaterWidgetData.addGlass(context)
        WaterWidget().updateAll(context)
    }
}

/** Called by the app after it changes water, so the widget never lags behind. */
suspend fun refreshWidgets(context: Context) { WaterWidget().updateAll(context) }

package dev.himma.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.himma.app.res.Res
import dev.himma.app.res.about
import dev.himma.app.res.add_a_medicine
import dev.himma.app.res.fasting_label
import dev.himma.app.res.gym
import dev.himma.app.res.himma
import dev.himma.app.res.home
import dev.himma.app.res.kcal
import dev.himma.app.res.log_a_meal
import dev.himma.app.res.meals
import dev.himma.app.res.medicine
import dev.himma.app.res.menu
import dev.himma.app.res.name
import dev.himma.app.res.settings
import dev.himma.app.res.steps
import dev.himma.app.res.track
import dev.himma.app.res.water
import dev.himma.app.res.weight
import dev.himma.core.Meal
import org.jetbrains.compose.resources.StringResource

internal enum class Tab(val label: StringResource, val icon: ImageVector) {
    Home(Res.string.home, Icons.Rounded.Home),
    Meals(Res.string.meals, Icons.Rounded.Restaurant),
    Gym(Res.string.gym, Icons.Rounded.FitnessCenter),
    Track(Res.string.track, Icons.Rounded.MonitorHeart),
    Medicine(Res.string.medicine, Icons.Rounded.Medication),
}

/** The pages inside the Track tab. */
internal enum class TrackPage(val label: StringResource) { Water(Res.string.water), Fasting(Res.string.fasting_label), Steps(Res.string.steps), Weight(Res.string.weight) }

/** A workout about to start: from a routine, or empty. */
class WorkoutStart(val routine: dev.himma.core.Routine?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(store: HimmaStore, onAbout: () -> Unit, onTour: () -> Unit, initialTab: Tab = Tab.Home, initialTrack: TrackPage = TrackPage.Water) {
    var tab by remember { mutableStateOf(initialTab) }
    var track by remember { mutableStateOf(initialTrack) }
    var workout by remember { mutableStateOf<WorkoutStart?>(null) }
    var menu by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Meal?>(null) }
    var adding by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<dev.himma.core.Medicine?>(null) }
    var addingMedicine by remember { mutableStateOf(false) }
    var templatesDialog by remember { mutableStateOf(false) }
    workout?.let { w -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) { WorkoutScreen(store, w.routine) { workout = null } }; return }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(s(Res.string.himma), fontWeight = FontWeight.SemiBold) }, colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background), actions = {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, s(Res.string.menu)) }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text(s(Res.string.settings)) }, onClick = { menu = false; settings = true })
                    DropdownMenuItem(text = { Text(s(Res.string.about)) }, onClick = { menu = false; onAbout() })
                }
            })
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
                Tab.entries.forEach { t -> NavigationBarItem(tab == t, { tab = t }, { Icon(t.icon, s(t.label)) }, label = { Text(s(t.label)) }) }
            }
        },
        floatingActionButton = {
            if (tab == Tab.Meals) FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Rounded.Add, s(Res.string.log_a_meal)) }
            if (tab == Tab.Medicine) FloatingActionButton(onClick = { addingMedicine = true }) { Icon(Icons.Rounded.Add, s(Res.string.add_a_medicine)) }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.Home -> HomeTab(store, Nav(go = { t, p -> tab = t; if (p != null) track = p }, logMeal = { tab = Tab.Meals; adding = true }, startWorkout = { workout = WorkoutStart(it) }))
                Tab.Meals -> MealsTab(store, onTemplates = { templatesDialog = true }) { editing = it }
                Tab.Gym -> GymTab(store) { workout = WorkoutStart(it) }
                Tab.Track -> TrackTab(store, track) { track = it }
                Tab.Medicine -> MedicineTab(store) { editingMedicine = it }
            }
        }
    }
    if (addingMedicine || editingMedicine != null) MedicineDialog(editingMedicine, onDismiss = { addingMedicine = false; editingMedicine = null },
        onDelete = editingMedicine?.let { m -> { store.deleteMedicine(m.id); editingMedicine = null } }) { store.saveMedicine(it); addingMedicine = false; editingMedicine = null }
    if (templatesDialog) TemplatesDialog(store) { templatesDialog = false }
    if (settings) SettingsDialog(store, { settings = false }, onTour = { settings = false; onTour() }) { store.saveSettings(it); settings = false }
    if (adding || editing != null) MealDialog(editing, store.nowTime(), onDismiss = { adding = false; editing = null },
        onDelete = editing?.let { m -> { store.deleteMeal(m.id); editing = null } }, onTemplate = { store.saveTemplate(dev.himma.core.MealTemplate("", it.name, it.kcal, it.protein, it.carbs, it.fat)) }) { store.saveMeal(it); adding = false; editing = null }
}

internal val tabPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)

/** A ring that fills as [progress] goes 0 to 1, with [content] in the middle. */
@Composable
internal fun Ring(progress: Float, modifier: Modifier = Modifier.size(180.dp), content: @Composable () -> Unit) {
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val fill = MaterialTheme.colorScheme.primary
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.minDimension * 0.09f
            val inset = w / 2
            val box = Size(size.width - w, size.height - w)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), box, style = Stroke(w))
            if (progress > 0f) drawArc(fill, -90f, 360f * progress, false, Offset(inset, inset), box, style = Stroke(w, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}

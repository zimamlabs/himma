package dev.himma.app

import dev.himma.app.res.this_week
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import dev.himma.app.res.added_n
import dev.himma.core.Sections
import dev.himma.core.Settings
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.rounded.Restaurant
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

internal enum class Tab(val id: String, val label: StringResource, val icon: ImageVector) {
    Home("home", Res.string.home, Icons.Rounded.Home),
    Meals("meals", Res.string.meals, Icons.Rounded.Restaurant),
    Gym("gym", Res.string.gym, Icons.Rounded.FitnessCenter),
    Track("track", Res.string.track, Icons.Rounded.MonitorHeart),
    Medicine("medicine", Res.string.medicine, Icons.Rounded.Medication);

    companion object { fun byId(id: String): Tab = entries.firstOrNull { it.id == id } ?: Home }
}

/** The sections the user chose to show, in their order. */
internal fun visibleTabs(settings: Settings): List<Tab> = Sections.visible(settings.sectionOrder, settings.hiddenSections).map(Tab::byId)

/** The pages inside the Track tab. */
internal enum class TrackPage(val label: StringResource) { Water(Res.string.water), Fasting(Res.string.fasting_label), Steps(Res.string.steps), Weight(Res.string.weight), Week(Res.string.this_week) }

/** A workout about to start: from a routine, or empty. */
class WorkoutStart(val routine: dev.himma.core.Routine?)

/**
 * The app shell: a side menu, a pager that swipes between the sections (and follows the reading direction, so it
 * mirrors in Arabic), and the bottom bar. A section that was hidden in Settings is still opened as an extra last
 * page when something on Home leads to it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    store: HimmaStore, onAbout: () -> Unit, onTour: () -> Unit, onSettings: () -> Unit = {},
    initialTab: Tab? = null, initialTrack: TrackPage = TrackPage.Water,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    onPageChanged: (Tab) -> Unit = {},
) {
    val shown = visibleTabs(store.settings)
    var extra by remember { mutableStateOf<Tab?>(null) }
    val tabs = shown + listOfNotNull(extra?.takeIf { it !in shown })
    val scope = rememberCoroutineScope()
    val pager = rememberPagerState(initialPage = tabs.indexOf(initialTab ?: Tab.byId(store.settings.startSection)).coerceAtLeast(0)) { tabs.size }
    val current = tabs[pager.currentPage.coerceIn(0, tabs.lastIndex)]
    var track by remember { mutableStateOf(initialTrack) }
    var workout by remember { mutableStateOf<WorkoutStart?>(null) }
    var showNew by remember { mutableStateOf(false) }
    var quickAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Meal?>(null) }
    var adding by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<dev.himma.core.Medicine?>(null) }
    var addingMedicine by remember { mutableStateOf(false) }
    var templatesDialog by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val glass = waterText(250)
    val addedText = s(Res.string.added_n, glass)

    fun goTo(tab: Tab, page: TrackPage? = null) {
        if (page != null) track = page
        if (tab !in tabs) extra = tab
        val index = (visibleTabs(store.settings) + listOfNotNull(extra)).indexOf(tab).coerceAtLeast(0)
        scope.launch { if (store.settings.reduceMotion) pager.scrollToPage(index) else pager.animateScrollToPage(index) }
    }
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { onPageChanged(tabs[it.coerceIn(0, tabs.lastIndex)]) } }
    // An extra page goes away once the user leaves it.
    LaunchedEffect(pager.currentPage, extra) { if (extra != null && current != extra && !pager.isScrollInProgress) extra = null }
    PlatformBackHandler(drawerState.isOpen) { scope.launch { drawerState.close() } }
    PlatformBackHandler(!drawerState.isOpen && current != Tab.Home && workout == null) { goTo(Tab.Home) }

    workout?.let { w -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) { WorkoutScreen(store, w.routine) { workout = null } }; return }
    fun closeDrawerThen(action: () -> Unit) { scope.launch { drawerState.close() }; action() }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                tabs = shown, current = current,
                onSection = { t -> closeDrawerThen { goTo(t) } },
                onQuickAdd = { closeDrawerThen { quickAdd = true } },
                onSettings = { closeDrawerThen(onSettings) },
                onAbout = { closeDrawerThen(onAbout) },
                onWhatsNew = { closeDrawerThen { showNew = true } },
                onTour = { closeDrawerThen(onTour) },
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = { Text(s(Res.string.himma), fontWeight = FontWeight.SemiBold) },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Rounded.Menu, s(Res.string.menu)) } },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
                    shown.forEach { t -> NavigationBarItem(current == t, { goTo(t) }, { Icon(t.icon, s(t.label)) }, label = { Text(s(t.label)) }) }
                }
            },
            floatingActionButton = {
                if (current == Tab.Meals) FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Rounded.Add, s(Res.string.log_a_meal)) }
                if (current == Tab.Medicine) FloatingActionButton(onClick = { addingMedicine = true }) { Icon(Icons.Rounded.Add, s(Res.string.add_a_medicine)) }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
            HorizontalPager(pager, Modifier.fillMaxSize(), beyondViewportPageCount = 1, key = { tabs[it.coerceIn(0, tabs.lastIndex)].id }) { index ->
                when (tabs[index.coerceIn(0, tabs.lastIndex)]) {
                    Tab.Home -> HomeTab(store, Nav(go = { t, p -> goTo(t, p) }, logMeal = { goTo(Tab.Meals); adding = true }, startWorkout = { workout = WorkoutStart(it) }))
                    Tab.Meals -> MealsTab(store, onTemplates = { templatesDialog = true }) { editing = it }
                    Tab.Gym -> GymTab(store) { workout = WorkoutStart(it) }
                    Tab.Track -> TrackTab(store, track) { track = it }
                    Tab.Medicine -> MedicineTab(store) { editingMedicine = it }
                }
            }
            if (pager.currentPage == 0) DrawerEdge { scope.launch { drawerState.open() } }
            }
        }
    }
    if (quickAdd) QuickAddDialog(
        onDismiss = { quickAdd = false },
        onLogMeal = { quickAdd = false; goTo(Tab.Meals); adding = true },
        onAddWater = { quickAdd = false; store.addWater(250); scope.launch { snackbar.showSnackbar(addedText) } },
        onAddMedicine = { quickAdd = false; goTo(Tab.Medicine); addingMedicine = true },
        onLogWeight = { quickAdd = false; goTo(Tab.Track, TrackPage.Weight) },
    )
    if (showNew) dev.himma.core.releaseNotes.lastOrNull()?.let { WhatsNewDialog(listOf(it)) { showNew = false } }
    if (addingMedicine || editingMedicine != null) MedicineDialog(editingMedicine, onDismiss = { addingMedicine = false; editingMedicine = null },
        onDelete = editingMedicine?.let { m -> { store.deleteMedicine(m.id); editingMedicine = null } }) { store.saveMedicine(it); addingMedicine = false; editingMedicine = null }
    if (templatesDialog) TemplatesDialog(store) { templatesDialog = false }
    if (adding || editing != null) MealDialog(editing, store.nowTime(), onDismiss = { adding = false; editing = null },
        onDelete = editing?.let { m -> { store.deleteMeal(m.id); editing = null } }, onTemplate = { store.saveTemplate(dev.himma.core.MealTemplate("", it.name, it.kcal, it.protein, it.carbs, it.fat)) }) { store.saveMeal(it); adding = false; editing = null }
}

/** Comfortable by default (12 dp between cards, 16 dp gutters); the Compact density setting tightens both. */
internal val LocalCompact = androidx.compose.runtime.compositionLocalOf { false }
internal val rowGap: androidx.compose.ui.unit.Dp @Composable get() = if (LocalCompact.current) 8.dp else 12.dp
@Composable internal fun tabPadding() = PaddingValues(start = if (LocalCompact.current) 12.dp else 16.dp, end = if (LocalCompact.current) 12.dp else 16.dp, top = 8.dp, bottom = 96.dp)

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

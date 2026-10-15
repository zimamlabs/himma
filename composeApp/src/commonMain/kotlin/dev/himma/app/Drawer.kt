package dev.himma.app

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.size
import dev.himma.app.res.by_zimam_labs
import dev.himma.app.res.quick_add
import dev.himma.app.res.zimam_labs
import dev.himma.app.res.version
import dev.himma.app.res.cancel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tour
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.himma.app.res.Res
import dev.himma.app.res.about
import dev.himma.app.res.add_a_medicine
import dev.himma.app.res.add_a_glass
import dev.himma.app.res.himma
import dev.himma.app.res.log_a_meal
import dev.himma.app.res.log_weight
import dev.himma.app.res.settings
import dev.himma.app.res.show_the_tour_again
import dev.himma.app.res.what_s_new

/**
 * The side menu, in the order fixed by ui-spec-shared.md: header (mark tile, name, tagline, version), the sections,
 * a divider, then Quick add, Settings, What's new, Tour, About, and a small footer. Items are 56 dp high with 16 dp
 * side padding. Himma has nothing to search yet, so the spec's Search item is left out.
 */
@Composable
internal fun AppDrawer(
    tabs: List<Tab>, current: Tab,
    onSection: (Tab) -> Unit, onQuickAdd: () -> Unit,
    onSettings: () -> Unit, onAbout: () -> Unit, onWhatsNew: () -> Unit, onTour: () -> Unit,
) {
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp, start = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { BrandMark(Modifier.size(40.dp)) }
                Column {
                    Text(s(Res.string.himma), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(s(Res.string.by_zimam_labs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(s(Res.string.version, APP_VERSION), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                tabs.forEach { t -> Item(s(t.label), { Icon(t.icon, null) }, current == t) { onSection(t) } }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Item(s(Res.string.quick_add), { Icon(Icons.Rounded.Add, null) }, false, onQuickAdd)
                Item(s(Res.string.settings), { Icon(Icons.Rounded.Settings, null) }, false, onSettings)
                Item(s(Res.string.what_s_new), { Icon(Icons.Rounded.AutoAwesome, null) }, false, onWhatsNew)
                Item(s(Res.string.show_the_tour_again), { Icon(Icons.Rounded.Tour, null) }, false, onTour)
                Item(s(Res.string.about), { Icon(Icons.Rounded.Info, null) }, false, onAbout)
            }
            Text(s(Res.string.zimam_labs), Modifier.fillMaxWidth().padding(vertical = 16.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

/** The small form behind Quick add: the four things people add most often. */
@Composable
internal fun QuickAddDialog(onDismiss: () -> Unit, onLogMeal: () -> Unit, onAddWater: () -> Unit, onAddMedicine: () -> Unit, onLogWeight: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s(Res.string.quick_add)) },
        text = {
            Column {
                Item(s(Res.string.log_a_meal), { Icon(Icons.Rounded.Add, null) }, false, onLogMeal)
                Item(s(Res.string.add_a_glass), { Icon(Icons.Rounded.WaterDrop, null) }, false, onAddWater)
                Item(s(Res.string.add_a_medicine), { Icon(Icons.Rounded.Add, null) }, false, onAddMedicine)
                Item(s(Res.string.log_weight), { Icon(Icons.Rounded.MonitorWeight, null) }, false, onLogWeight)
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(s(Res.string.cancel)) } },
    )
}

@Composable
private fun Item(label: String, icon: @Composable () -> Unit, selected: Boolean, onClick: () -> Unit) =
    NavigationDrawerItem(label = { Text(label) }, icon = icon, selected = selected, onClick = onClick, modifier = Modifier.heightIn(min = 56.dp))

/**
 * The pager takes every horizontal swipe, so on the first section a thin strip at the start edge opens the side menu
 * (the menu button always works). The strip sits on the side the reading direction starts from.
 */
@Composable
internal fun BoxScope.DrawerEdge(onOpen: () -> Unit) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val threshold = with(LocalDensity.current) { 40.dp.toPx() }
    Box(
        Modifier.align(Alignment.CenterStart).width(24.dp).fillMaxHeight().pointerInput(rtl) {
            var dragged = 0f
            detectHorizontalDragGestures(onDragStart = { dragged = 0f }, onDragEnd = { dragged = 0f }) { change, amount ->
                change.consume()
                dragged += if (rtl) -amount else amount
                if (dragged > threshold) { dragged = Float.NEGATIVE_INFINITY; onOpen() }
            }
        },
    )
}

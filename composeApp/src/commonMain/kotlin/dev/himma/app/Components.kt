package dev.himma.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** The card every screen uses: warm surface, big corners, a hairline border and the small orange diamond in the corner. */
@Composable
fun Panel(modifier: Modifier = Modifier, diamond: Boolean = true, content: @Composable () -> Unit) {
    val border = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Box {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
            if (diamond) Diamond(Modifier.align(Alignment.TopEnd).padding(12.dp).size(8.dp))
        }
    }
}

/** The signature accent: a small rounded square turned on its corner, in the Zimam orange. */
@Composable
fun Diamond(modifier: Modifier = Modifier) {
    Box(modifier.rotate(45f).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
}

/** A soft orange glow behind [content], for hero areas. */
@Composable
fun Glow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val glow = MaterialTheme.colorScheme.primary
    Box(modifier.drawBehind {
        drawRect(Brush.radialGradient(listOf(glow.copy(alpha = 0.26f), Color.Transparent), center = Offset(size.width * 0.5f, size.height * 0.35f), radius = size.maxDimension * 0.7f))
    }) { content() }
}

@Composable
fun EmptyHint(title: String, hint: String) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun prettyDate(d: LocalDate): String = "${dayName(d.dayOfWeek)} ${d.dayOfMonth} ${monthName(d.monthNumber)}"

fun dayName(d: DayOfWeek): String = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[d.ordinal]

fun monthName(n: Int): String = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[(n - 1).coerceIn(0, 11)]

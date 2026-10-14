package dev.himma.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate

/** Seven (or any number of) bars with a dashed goal line and day letters underneath. Today's bar is the strong one. */
@Composable
fun BarChart(values: List<Float>, dates: List<LocalDate>, goal: Float?, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val line = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(Modifier.fillMaxWidth().height(96.dp)) {
            val top = maxOf(values.maxOrNull() ?: 0f, goal ?: 0f, 1f)
            val n = values.size.coerceAtLeast(1)
            val slot = size.width / n
            val bar = slot * 0.56f
            values.forEachIndexed { i, v ->
                val x = i * slot + (slot - bar) / 2
                drawRoundRect(track, Offset(x, 0f), Size(bar, size.height), CornerRadius(bar / 3))
                val h = size.height * (v / top).coerceIn(0f, 1f)
                if (h > 0f) drawRoundRect(if (i == n - 1) color else color.copy(alpha = 0.55f), Offset(x, size.height - h), Size(bar, h), CornerRadius(bar / 3))
            }
            if (goal != null && goal > 0f) {
                val y = size.height * (1f - (goal / top).coerceIn(0f, 1f))
                drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            dates.forEach { d -> Text(dayInitial(d.dayOfWeek), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
        }
    }
}

/** A line over [values] (oldest first) with the points marked; [smooth] is drawn over it as a stronger line when given. */
@Composable
fun LineChart(values: List<Float>, modifier: Modifier = Modifier, smooth: List<Float>? = null, color: Color = MaterialTheme.colorScheme.primary) {
    val faint = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.fillMaxWidth().height(120.dp)) {
        val all = values + (smooth ?: emptyList())
        if (all.size < 2) return@Canvas
        val lo = all.min(); val hi = all.max()
        val span = (hi - lo).coerceAtLeast(0.5f)
        val pad = 8f
        fun x(i: Int, n: Int) = pad + (size.width - 2 * pad) * (if (n <= 1) 0.5f else i / (n - 1f))
        fun y(v: Float) = pad + (size.height - 2 * pad) * (1f - (v - lo) / span)
        drawLine(faint, Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), strokeWidth = 2f)
        fun path(vs: List<Float>) = Path().apply { vs.forEachIndexed { i, v -> if (i == 0) moveTo(x(i, vs.size), y(v)) else lineTo(x(i, vs.size), y(v)) } }
        drawPath(path(values), color.copy(alpha = if (smooth == null) 1f else 0.35f), style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        values.forEachIndexed { i, v -> drawCircle(color.copy(alpha = if (smooth == null) 1f else 0.5f), 5f, Offset(x(i, values.size), y(v))) }
        smooth?.let { drawPath(path(it), color, style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
    }
}

/** Protein, carbs and fat as one bar split by their calories (4, 4 and 9 per gram). */
@Composable
fun MacroBar(protein: Int, carbs: Int, fat: Int, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val parts = listOf(protein * 4f to cs.primary, carbs * 4f to cs.secondary, fat * 9f to cs.tertiary)
    val total = parts.sumOf { it.first.toDouble() }.toFloat()
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        drawRoundRect(cs.surfaceContainerHigh, Offset.Zero, size, CornerRadius(size.height / 2))
        if (total <= 0f) return@Canvas
        var x = 0f
        parts.forEach { (kcal, c) ->
            val w = size.width * kcal / total
            if (w > 0f) drawRect(c, Offset(x, 0f), Size(w, size.height))
            x += w
        }
    }
}

@Composable
fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        androidx.compose.foundation.layout.Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

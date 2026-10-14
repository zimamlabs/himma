package dev.himma.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Ink = Color(0xFF121214)
private val Cream = Color(0xFFF5F1E8)
private val Orange = Color(0xFFFF5A1F)

// The two rings of the icon, in its 512 unit square.
private val outerRing: Path by lazy { PathParser().parsePathString("M256.0 96.0A160 160 0 1 1 153.2 133.4").toPath() }
private val innerRing: Path by lazy { PathParser().parsePathString("M256.0 164.0A92 92 0 1 1 210.0 335.7").toPath() }

/** Draws the first [fraction] of [path] along its length, so a ring looks drawn by hand. */
private fun DrawScope.drawGrowing(path: Path, fraction: Float, color: Color) {
    if (fraction <= 0f) return
    val measure = PathMeasure().apply { setPath(path, false) }
    val part = Path()
    measure.getSegment(0f, measure.length * fraction.coerceAtMost(1f), part, true)
    drawPath(part, color, style = Stroke(34f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/**
 * The opening screen, about 1.4 seconds: the outer ring is drawn, then the inner one, the Zimam dot settles in the middle
 * with a small bounce, and the name slides up and fades in with "by Zimam" below it, well clear of the logo.
 */
@Composable
fun Splash(onDone: () -> Unit) {
    val outer = remember { Animatable(0f) }
    val inner = remember { Animatable(0f) }
    val dot = remember { Animatable(0f) }
    val name = remember { Animatable(0f) }
    val by = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { outer.animateTo(1f, tween(560, easing = FastOutSlowInEasing)) }
        launch { delay(180); inner.animateTo(1f, tween(480, easing = FastOutSlowInEasing)) }
        launch { delay(520); dot.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)) }
        launch { delay(640); name.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
        launch { delay(820); by.animateTo(1f, tween(340)) }
        delay(1400)
        onDone()
    }
    Column(Modifier.fillMaxSize().background(Ink), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Canvas(Modifier.size(150.dp)) {
            val u = size.width / 512f
            withTransform({ scale(u, u, Offset.Zero) }) {
                drawGrowing(outerRing, outer.value, Cream)
                drawGrowing(innerRing, inner.value, Cream)
            }
            zimamDot(Offset(256f * u, 256f * u), 40f * u * dot.value, Orange)
        }
        Spacer(Modifier.height(28.dp))
        Text("Himma", Modifier.alpha(name.value).graphicsLayer { translationY = (1f - name.value) * 14.dp.toPx() }, color = Cream, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("by Zimam", Modifier.alpha(by.value), color = Orange, fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    }
}

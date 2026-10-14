package dev.himma.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val REPO = "https://github.com/zimamlabs/himma"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(store: HimmaStore, onBack: () -> Unit, onTour: () -> Unit) {
    val uri = LocalUriHandler.current
    val note = dev.himma.core.releaseNotes.lastOrNull()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Glow(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))) {
                Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BrandMark(Modifier.size(112.dp))
                    Text("Himma", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    Text("همّة", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("Health and training, kept on your phone", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Text("Version $APP_VERSION", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("Himma tracks meals and calories, water, a fasting window, medicine doses, gym workouts, steps and your weight in plain files you own. It works offline and sends nothing anywhere.", style = MaterialTheme.typography.bodyLarge)
            Panel {
                Text("Show the tour again", style = MaterialTheme.typography.titleSmall)
                Text("A quick walk through what Himma does and where your data lives.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onTour) { Text("Start the tour") }
            }
            if (note != null) Panel {
                Text("What's new in ${note.version}", style = MaterialTheme.typography.titleSmall)
                note.bullets.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
            }
            Panel {
                Text("Private by design", style = MaterialTheme.typography.titleSmall)
                Text("No account, no tracking. Your data stays in ${store.folderPath}.", style = MaterialTheme.typography.bodyMedium)
            }
            Panel {
                Text("Links", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { uri.openUri(REPO) }) { Text("Repository") }
                    TextButton(onClick = { uri.openUri("$REPO/releases") }) { Text("Releases") }
                    TextButton(onClick = { uri.openUri("$REPO/issues") }) { Text("Issues") }
                    TextButton(onClick = { uri.openUri("$REPO/blob/main/LICENSE") }) { Text("Licence") }
                }
            }
            Text("by Zimam Labs  ·  MIT licence", Modifier.fillMaxWidth().padding(vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

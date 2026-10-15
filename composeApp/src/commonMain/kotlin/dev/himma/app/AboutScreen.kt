package dev.himma.app

import dev.himma.app.res.back

import dev.himma.app.res.Res
import dev.himma.app.res.not_medical_advice
import dev.himma.app.res.not_medical_advice_title
import dev.himma.app.res.a_quick_walk_through_what
import dev.himma.app.res.about
import dev.himma.app.res.by_zimam_labs_mit_licence
import dev.himma.app.res.health_and_training_kept_on
import dev.himma.app.res.himma
import dev.himma.app.res.himma_tracks_meals_and_calories
import dev.himma.app.res.issues
import dev.himma.app.res.licence
import dev.himma.app.res.links
import dev.himma.app.res.no_account_no_tracking_your
import dev.himma.app.res.private_by_design
import dev.himma.app.res.releases
import dev.himma.app.res.repository
import dev.himma.app.res.show_the_tour_again
import dev.himma.app.res.start_the_tour
import dev.himma.app.res.version
import dev.himma.app.res.what_s_new_in

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
                title = { Text(s(Res.string.about), fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, s(Res.string.back)) } },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Glow(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))) {
                Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BrandMark(Modifier.size(112.dp))
                    Text(s(Res.string.himma), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    Text("همّة", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text(s(Res.string.health_and_training_kept_on), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Text(s(Res.string.version, APP_VERSION), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(s(Res.string.himma_tracks_meals_and_calories), style = MaterialTheme.typography.bodyLarge)
            Panel {
                Text(s(Res.string.show_the_tour_again), style = MaterialTheme.typography.titleSmall)
                Text(s(Res.string.a_quick_walk_through_what), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onTour) { Text(s(Res.string.start_the_tour)) }
            }
            if (note != null) Panel {
                Text(s(Res.string.what_s_new_in, note.version), style = MaterialTheme.typography.titleSmall)
                note.bullets.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
            }
            Panel {
                Text(s(Res.string.not_medical_advice_title), style = MaterialTheme.typography.titleSmall)
                Text(s(Res.string.not_medical_advice), style = MaterialTheme.typography.bodyMedium)
            }
            Panel {
                Text(s(Res.string.private_by_design), style = MaterialTheme.typography.titleSmall)
                Text(s(Res.string.no_account_no_tracking_your, store.folderPath), style = MaterialTheme.typography.bodyMedium)
            }
            Panel {
                Text(s(Res.string.links), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { uri.openUri(REPO) }) { Text(s(Res.string.repository)) }
                    TextButton(onClick = { uri.openUri("$REPO/releases") }) { Text(s(Res.string.releases)) }
                    TextButton(onClick = { uri.openUri("$REPO/issues") }) { Text(s(Res.string.issues)) }
                    TextButton(onClick = { uri.openUri("$REPO/blob/main/LICENSE") }) { Text(s(Res.string.licence)) }
                }
            }
            Text(s(Res.string.by_zimam_labs_mit_licence), Modifier.fillMaxWidth().padding(vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

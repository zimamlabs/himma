package dev.himma.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.himma.app.res.Res
import dev.himma.app.res.data_file_unreadable
import dev.himma.app.res.data_file_unreadable_body
import dev.himma.app.res.got_it
import dev.himma.core.DataProblem

/** Says which files could not be read and where the untouched copy is, so nothing is lost silently. */
@Composable
fun DataProblemsCard(problems: List<DataProblem>, onDismiss: () -> Unit) {
    Panel {
        Text(s(Res.string.data_file_unreadable), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
        problems.forEach { Text(s(Res.string.data_file_unreadable_body, it.file, it.copy), style = MaterialTheme.typography.bodyMedium) }
        OutlinedButton(onClick = onDismiss) { Text(s(Res.string.got_it)) }
    }
}

package dev.himma.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Himma's mark (the flowing runner with the diamond), the same picture on every screen that shows it. */
@Composable
expect fun BrandMark(modifier: Modifier = Modifier)

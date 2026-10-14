package dev.himma.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.himma.core.Vault
import okio.FileSystem
import okio.Path.Companion.toPath

fun main() = application {
    val store = HimmaStore(Vault(FileSystem.SYSTEM, (System.getProperty("user.home") + "/Himma").toPath()))
    Window(onCloseRequest = ::exitApplication, title = "Himma", state = rememberWindowState(width = 460.dp, height = 820.dp)) { HimmaApp(store) }
}

package dev.himma.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.himma.core.Migration
import dev.himma.core.Vault
import dev.himma.core.isMigrated
import dev.himma.core.migrateFolder
import okio.FileSystem
import okio.Path.Companion.toPath

fun main() = application {
    val home = System.getProperty("user.home")
    val fs = FileSystem.SYSTEM
    val old = "$home/Himma".toPath()
    val folder = "$home/Zimam/Himma".toPath()
    // The old ~/Himma is copied and checked, never deleted; if the copy fails it stays in use.
    val root = if (isMigrated(fs, old, folder) || migrateFolder(fs, old, folder) !is Migration.Failed) folder else old
    val store = HimmaStore(Vault(fs, root), DesktopPlatform(root.toString()))
    Window(onCloseRequest = ::exitApplication, title = "Himma", state = rememberWindowState(width = 460.dp, height = 820.dp)) { HimmaApp(store) }
}

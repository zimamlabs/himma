package dev.himma.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.himma.core.Vault
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class MainActivity : ComponentActivity() {
    private lateinit var store: HimmaStore

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        store = HimmaStore(Vault(FileSystem.SYSTEM, filesDir.resolve("Himma").also { it.mkdirs() }.toOkioPath()))
        setContent { HimmaApp(store) }
    }

    override fun onResume() {
        super.onResume()
        // The day may have rolled over, or the files changed under us.
        if (::store.isInitialized) store.reload()
    }
}

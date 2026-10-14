package dev.himma.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import dev.himma.core.Vault
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class MainActivity : ComponentActivity() {
    private lateinit var store: HimmaStore
    private lateinit var storage: Storage
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val actions = object : PlatformActions {
        override val folderPath: String get() = storage.root.absolutePath
        override val sharedFolder: Boolean get() = storage.shared
        override val canAskAllFiles: Boolean get() = Build.VERSION.SDK_INT >= 30
        override fun allowAllFiles() {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"))
            runCatching { startActivity(intent) }.onFailure { runCatching { startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)) } }
        }
        override fun allowNotifications() {
            if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        override fun canRemoveOldCopy(): Boolean = storage.canRemoveOld()
        override fun removeOldCopy(): Boolean = storage.removeOld()
    }

    private fun vault() = Vault(FileSystem.SYSTEM, storage.root.also { it.mkdirs() }.toOkioPath())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        storage = Storage(this)
        storage.refresh()
        store = HimmaStore(vault(), actions)
        setContent { HimmaApp(store) }
    }

    override fun onResume() {
        super.onResume()
        if (!::store.isInitialized) return
        // All files access may have just been granted; otherwise the day may have rolled over or the files changed under us.
        if (storage.refresh()) store.replaceVault(vault()) else { store.reload(); store.refreshFolderInfo() }
    }
}

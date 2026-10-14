package dev.himma.app

import android.content.Context
import android.os.Build
import android.os.Environment
import dev.himma.core.Migration
import dev.himma.core.canRemoveOldCopy
import dev.himma.core.isMigrated
import dev.himma.core.migrateFolder
import dev.himma.core.removeOldCopy
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

/**
 * Where Himma keeps its files: the visible `Zimam/Himma` folder when the phone has granted All files access, otherwise the
 * app's private folder. Moving to the visible folder is a verified copy (see `migrateFolder`): nothing is deleted or
 * overwritten, the old folder stays as a backup until the user removes it, and on any problem the old folder stays in use.
 */
class Storage(private val context: Context) {
    private val fs = FileSystem.SYSTEM
    val privateRoot: File get() = File(context.filesDir, "Himma")
    val sharedRoot: File get() = File(File(Environment.getExternalStorageDirectory(), "Zimam"), "Himma")

    @Volatile
    var root: File = privateRoot
        private set

    val shared: Boolean get() = root != privateRoot

    fun canUseShared(): Boolean = Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager()

    /** Picks the folder to use now; true when it changed so callers reload. Safe to call again and again. */
    fun refresh(): Boolean {
        val target = pick()
        if (target == root) return false
        root = target
        return true
    }

    private fun pick(): File {
        privateRoot.mkdirs()
        if (!canUseShared()) return privateRoot
        if (!runCatching { sharedRoot.mkdirs() }.getOrDefault(false) && !sharedRoot.isDirectory) return privateRoot
        if (!sharedRoot.canWrite()) return privateRoot
        val from = privateRoot.toOkioPath(); val to = sharedRoot.toOkioPath()
        if (isMigrated(fs, from, to)) return sharedRoot
        return if (migrateFolder(fs, from, to) is Migration.Failed) privateRoot else sharedRoot
    }

    private fun privateHasFiles(): Boolean = privateRoot.walkTopDown().any { it.isFile }

    fun canRemoveOld(): Boolean = shared && privateHasFiles() && canRemoveOldCopy(fs, privateRoot.toOkioPath(), sharedRoot.toOkioPath())

    fun removeOld(): Boolean = shared && removeOldCopy(fs, privateRoot.toOkioPath(), sharedRoot.toOkioPath()).also { privateRoot.mkdirs() }
}

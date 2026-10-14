package dev.himma.app

/** What the screens need from the system; the desktop build uses the defaults. */
interface PlatformActions {
    /** Where the data lives now, shown to the user. */
    val folderPath: String
    /** True when the data is in the visible Zimam/Himma folder. */
    val sharedFolder: Boolean get() = true
    /** The phone can be asked for All files access (Android). */
    val canAskAllFiles: Boolean get() = false
    fun allowAllFiles() {}
    fun allowNotifications() {}
    /** An old private copy is still there and every file in it is safely in the new folder. */
    fun canRemoveOldCopy(): Boolean = false
    fun removeOldCopy(): Boolean = false
}

class NoPlatform(override val folderPath: String = "") : PlatformActions

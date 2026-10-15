package dev.himma.app

/** Whether the phone can count steps for Himma. */
enum class StepSupport { Ready, NeedsPermission, NoSensor, Unavailable }

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
    /** The phone can hand the user a file (export). */
    val canExport: Boolean get() = false
    /** Asks where to save, then writes [bytes] there as [fileName]. */
    fun exportData(fileName: String, bytes: () -> ByteArray) {}
    /** Medicine notifications can appear (permission given, app and channel not switched off). */
    val notificationsOn: Boolean get() = true
    /** Alarms can ring at the exact minute. */
    val exactAlarmsOn: Boolean get() = true
    fun openNotificationSettings() {}
    fun openExactAlarmSettings() {}
    /** The language of the app: "system", "en" or "ar". */
    val language: String get() = "system"
    fun setLanguage(code: String) {}
    /** Show numbers in Arabic-Indic digits. */
    val arabicDigits: Boolean get() = false
    fun setArabicDigits(on: Boolean) {}
    val stepSupport: StepSupport get() = StepSupport.Unavailable
    fun allowStepCounting() {}
    /** An old private copy is still there and every file in it is safely in the new folder. */
    fun canRemoveOldCopy(): Boolean = false
    fun removeOldCopy(): Boolean = false
}

class NoPlatform(override val folderPath: String = "") : PlatformActions

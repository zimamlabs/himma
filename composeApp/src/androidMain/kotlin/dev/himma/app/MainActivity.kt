package dev.himma.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (::store.isInitialized) store.refreshReminders() }
    private val activityPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { startSteps() }
    private var stepSupportState by mutableStateOf(StepSupport.Unavailable)
    private val sensors by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private val stepSensor by lazy { sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }
    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) { if (::store.isInitialized) store.onStepCounter(event.values[0].toLong(), Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT, 0)) }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /** Registers for the step counter when the phone has one and the permission is given. */
    private fun startSteps() {
        stepSupportState = when {
            stepSensor == null -> StepSupport.NoSensor
            Build.VERSION.SDK_INT >= 29 && checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED -> StepSupport.NeedsPermission
            else -> StepSupport.Ready
        }
        sensors.unregisterListener(stepListener)
        if (stepSupportState == StepSupport.Ready) sensors.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

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
        override val notificationsOn: Boolean get() = MedicineAlarms.notificationsOn(this@MainActivity)
        override val exactAlarmsOn: Boolean get() = MedicineAlarms.exactAlarmsOn(this@MainActivity)
        override fun openNotificationSettings() {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            runCatching { startActivity(intent) }
        }
        override fun openExactAlarmSettings() {
            if (Build.VERSION.SDK_INT >= 31) runCatching { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) }
        }
        override val language: String get() = AppLanguagePrefs.load(this@MainActivity).code
        override fun setLanguage(code: String) { AppLanguagePrefs.save(this@MainActivity, code); recreate() }
        override val arabicDigits: Boolean get() = UiText.arabicDigits
        override fun setArabicDigits(on: Boolean) { AppLanguagePrefs.saveArabicDigits(this@MainActivity, on); UiText.arabicDigits = on; recreate() }
        override val stepSupport: StepSupport get() = stepSupportState
        override fun allowStepCounting() { if (Build.VERSION.SDK_INT >= 29) activityPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION) }
        override fun canRemoveOldCopy(): Boolean = storage.canRemoveOld()
        override fun removeOldCopy(): Boolean = storage.removeOld()
    }

    private fun vault() = Vault(FileSystem.SYSTEM, storage.root.also { it.mkdirs() }.toOkioPath())

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(AppLanguagePrefs.load(base).wrap(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        UiText.arabicDigits = AppLanguagePrefs.arabicDigits(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        storage = Storage(this)
        storage.refresh()
        store = HimmaStore(vault(), actions)
        store.onMedicinesChanged = {
            MedicineAlarms.medicinesChanged(this)
            if (store.medicines.isNotEmpty()) actions.allowNotifications()
        }
        setContent { HimmaApp(store) }
    }

    override fun onResume() {
        super.onResume()
        if (!::store.isInitialized) return
        // All files access may have just been granted; otherwise the day may have rolled over or the files changed under us.
        if (storage.refresh()) store.replaceVault(vault()) else { store.reload(); store.refreshFolderInfo() }
        MedicineAlarms.schedule(this)
        startSteps()
    }

    override fun onPause() {
        super.onPause()
        sensors.unregisterListener(stepListener)
        if (::store.isInitialized) store.flushSteps()
    }
}

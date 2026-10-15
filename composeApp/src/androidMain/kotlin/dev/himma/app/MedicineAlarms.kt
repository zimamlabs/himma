package dev.himma.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.himma.core.Dose
import dev.himma.core.DoseMark
import dev.himma.core.Medicine
import dev.himma.core.Vault
import dev.himma.core.dosesToRemind
import dev.himma.core.nextDoseTime
import dev.himma.health.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.Path.Companion.toOkioPath

private const val CHANNEL = "medicine"
private const val PREFS = "himma"
private const val CHECKED = "med.checked"

/** Sets one alarm for the next dose, and shows the notifications for doses that are due. */
object MedicineAlarms {
    private fun vault(context: Context) = Vault(FileSystem.SYSTEM, Storage(context).currentRoot().also { it.mkdirs() }.toOkioPath())

    private fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    /** Sets (again) the alarm for the next dose after now. Cheap; call on open, after edits, and after a restart. */
    fun schedule(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = PendingIntent.getBroadcast(context, 0, Intent(context, DoseReceiver::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.cancel(intent)
        val zone = TimeZone.currentSystemDefault()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(CHECKED)) prefs.edit().putLong(CHECKED, System.currentTimeMillis()).apply()
        val next = nextDoseTime(vault(context).medicines(), now()) ?: return
        val at = next.toInstant(zone).toEpochMilliseconds()
        val exact = exactAlarmsOn(context)
        if (exact) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
    }

    /** Notifications can appear: allowed for the app and the medicine channel not switched off. */
    fun notificationsOn(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        val channel = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).getNotificationChannel(CHANNEL)
        return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun exactAlarmsOn(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

    /** After the list changes: a dose that was just added for a time already past must not ring now. */
    fun medicinesChanged(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(CHECKED, System.currentTimeMillis()).apply()
        schedule(context)
    }

    /** Notifies for every unmarked dose due since the last check (never reaching back more than three hours), then sets the next alarm. */
    fun fire(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val zone = TimeZone.currentSystemDefault()
        val vault = vault(context)
        val last = prefs.getLong(CHECKED, 0L).takeIf { it > 0 }?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(zone) }
        val due = dosesToRemind(vault.medicines(), vault.doseMarks(), last, now())
        prefs.edit().putLong(CHECKED, System.currentTimeMillis()).apply()
        due.forEach { notify(context, it) }
        schedule(context)
    }

    private fun action(context: Context, dose: Dose, mark: DoseMark): PendingIntent {
        val intent = Intent(context, DoseActionReceiver::class.java)
            .setData(Uri.parse("himma://dose/${dose.key}/${mark.name}"))
            .putExtra("medicine", dose.medicine.id).putExtra("date", dose.date.toString()).putExtra("time", dose.time).putExtra("mark", mark.name)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun notify(base: Context, dose: Dose) {
        // The notification speaks the language chosen in the app, not only the phone's.
        val context = AppLanguagePrefs.load(base).wrap(base)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(CHANNEL, context.getString(R.string.channel_medicine), NotificationManager.IMPORTANCE_HIGH))
        val open = PendingIntent.getActivity(context, 1, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val text = if (dose.medicine.dose.isNotBlank()) context.getString(R.string.notify_dose_at, dose.medicine.dose, dose.time) else context.getString(R.string.notify_it_is, dose.time)
        val showNames = vault(context).settings().lockScreenNames
        // A locked screen shows only the generic version unless the user allowed names; the full text appears after unlocking.
        val generic = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.notify_public_title))
            .build()
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setVisibility(if (showNames) NotificationCompat.VISIBILITY_PUBLIC else NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(if (showNames) null else generic)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.notify_title, dose.medicine.name))
            .setContentText(text)
            .setAutoCancel(true).setContentIntent(open).setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(0, context.getString(R.string.notify_taken), action(context, dose, DoseMark.Taken))
            .addAction(0, context.getString(R.string.notify_skip), action(context, dose, DoseMark.Skipped))
            .build()
        manager.notify(dose.key.hashCode(), n)
    }

    fun mark(context: Context, intent: Intent) {
        val id = intent.getStringExtra("medicine") ?: return
        val date = intent.getStringExtra("date")?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return
        val time = intent.getStringExtra("time") ?: return
        val mark = intent.getStringExtra("mark")?.let { runCatching { DoseMark.valueOf(it) }.getOrNull() } ?: return
        val dose = Dose(Medicine(id, "", times = emptyList()), date, time)
        vault(context).setDoseMark(dose, mark)
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(dose.key.hashCode())
    }
}

/** Fires at a dose time. */
class DoseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { MedicineAlarms.fire(context) }
}

/** The Taken and Skip buttons on a notification. */
class DoseActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { MedicineAlarms.mark(context, intent) }
}

/**
 * After a restart, an app update or a change of time or time zone the alarm is gone. [MedicineAlarms.fire] first
 * notifies for doses that fell in the gap (up to three hours back) and then sets the next alarm.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { MedicineAlarms.fire(context) }
}

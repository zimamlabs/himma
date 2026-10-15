package dev.himma.core

/** Something on the phone that can stop a medicine reminder from appearing or from appearing on time. */
enum class ReminderProblem { NotificationsOff, ExactAlarmsOff }

/** What to warn about; nothing when there is no medicine to remind about. */
fun reminderProblems(medicines: List<Medicine>, notificationsOn: Boolean, exactAlarmsOn: Boolean): List<ReminderProblem> {
    if (medicines.isEmpty()) return emptyList()
    return buildList {
        if (!notificationsOn) add(ReminderProblem.NotificationsOff)
        if (!exactAlarmsOn) add(ReminderProblem.ExactAlarmsOff)
    }
}

package dev.himma.core

import kotlin.test.Test
import kotlin.test.assertEquals

class RemindersTest {
    private val med = Medicine("a", "A", times = listOf("08:00"))

    @Test fun nothingToWarnAboutWhenAllowed() {
        assertEquals(emptyList(), reminderProblems(listOf(med), notificationsOn = true, exactAlarmsOn = true))
    }

    @Test fun noMedicinesMeansNoWarning() {
        assertEquals(emptyList(), reminderProblems(emptyList(), notificationsOn = false, exactAlarmsOn = false))
    }

    @Test fun eachProblemIsReportedOnItsOwn() {
        assertEquals(listOf(ReminderProblem.NotificationsOff), reminderProblems(listOf(med), notificationsOn = false, exactAlarmsOn = true))
        assertEquals(listOf(ReminderProblem.ExactAlarmsOff), reminderProblems(listOf(med), notificationsOn = true, exactAlarmsOn = false))
    }

    @Test fun bothProblemsComeNotificationsFirst() {
        assertEquals(listOf(ReminderProblem.NotificationsOff, ReminderProblem.ExactAlarmsOff), reminderProblems(listOf(med), false, false))
    }
}

package dev.himma.core

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Where the phone's step counter stood. The sensor counts steps since the phone started, so a day's steps are the
 * counter now minus the counter at the start of the day, plus whatever was counted before a restart reset it to zero.
 */
@Serializable
data class StepState(val date: String = "", val baseline: Long = 0, val last: Long = 0, val carried: Long = 0, val boot: Int = 0) {
    val steps: Int get() = (carried + (last - baseline)).coerceAtLeast(0).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

/**
 * Takes a new counter reading. A new day starts from this reading (steps taken on days Himma was not opened cannot be
 * split by day, so they are not counted). A different boot count (or, without one, a reading below the last one) means the phone restarted: the steps so far are
 * kept and counting goes on from zero.
 */
fun stepUpdate(state: StepState, today: LocalDate, counter: Long, boot: Int = 0): StepState {
    if (state.date != today.toString()) return StepState(today.toString(), counter, counter, 0, boot)
    // The phone's boot count tells a restart exactly; without it (0) fall back to the counter going down.
    val restarted = if (state.boot != 0 && boot != 0) boot != state.boot else counter < state.last
    if (restarted) return state.copy(carried = state.carried + (state.last - state.baseline), baseline = 0, last = counter, boot = boot)
    return state.copy(last = counter, boot = if (boot != 0) boot else state.boot)
}

/** Steps as a share of the goal, 0 to 1. */
fun stepProgress(steps: Int, goal: Int): Float = if (goal <= 0) 0f else (steps.toFloat() / goal).coerceIn(0f, 1f)

package dev.himma.core

import kotlinx.serialization.Serializable

/** One exercise in a routine: how many sets of how many reps, and the usual weight. */
@Serializable
data class Exercise(val name: String, val sets: Int = 3, val reps: Int = 10, val weightKg: Double = 0.0)

@Serializable
data class Routine(val id: String, val name: String, val exercises: List<Exercise>)

/** One set that was done. */
@Serializable
data class SetEntry(val exercise: String, val reps: Int, val weightKg: Double)

/** A logged workout; [date] is ISO (2026-10-14). */
@Serializable
data class Workout(val id: String, val date: String, val name: String, val sets: List<SetEntry>, val minutes: Int = 0)

fun setVolume(s: SetEntry): Double = s.reps * s.weightKg

fun volume(sets: List<SetEntry>): Double = sets.sumOf(::setVolume)

/** Epley estimate of the heaviest single lift; a single rep is the weight itself. */
fun oneRepMax(weightKg: Double, reps: Int): Double = if (reps <= 1) weightKg else weightKg * (1 + reps / 30.0)

/** The best a lift has done: heaviest weight, best estimated one-rep max, and the biggest set (reps times weight). */
data class Best(val maxWeightKg: Double, val oneRepMaxKg: Double, val maxSetVolume: Double, val date: String)

/** Personal bests per exercise (names compared case-insensitively) over [workouts]. */
fun personalBests(workouts: List<Workout>): Map<String, Best> {
    val out = HashMap<String, Best>()
    for (w in workouts.sortedBy { it.date }) for (s in w.sets) {
        if (s.reps <= 0 || s.weightKg < 0) continue
        val key = s.exercise.trim().lowercase()
        val prev = out[key]
        out[key] = Best(
            maxWeightKg = maxOf(prev?.maxWeightKg ?: 0.0, s.weightKg),
            oneRepMaxKg = maxOf(prev?.oneRepMaxKg ?: 0.0, oneRepMax(s.weightKg, s.reps)),
            maxSetVolume = maxOf(prev?.maxSetVolume ?: 0.0, setVolume(s)),
            date = if (prev == null || s.weightKg > prev.maxWeightKg || oneRepMax(s.weightKg, s.reps) > prev.oneRepMaxKg) w.date else prev.date,
        )
    }
    return out
}

/** True when [set] beats everything [before] says about that exercise (heavier, or a higher one-rep estimate). */
fun beatsBest(before: Map<String, Best>, set: SetEntry): Boolean {
    if (set.reps <= 0 || set.weightKg <= 0) return false
    val b = before[set.exercise.trim().lowercase()] ?: return true
    return set.weightKg > b.maxWeightKg || oneRepMax(set.weightKg, set.reps) > b.oneRepMaxKg
}

/** The sets a routine plans, one entry per set, in order. */
fun plannedSets(routine: Routine): List<SetEntry> =
    routine.exercises.flatMap { e -> List(e.sets.coerceAtLeast(0)) { SetEntry(e.name, e.reps, e.weightKg) } }

/** "62.5 kg" or "60 kg". */
fun formatKg(kg: Double): String = (kotlin.math.round(kg * 10) / 10).toString().removeSuffix(".0") + " kg"

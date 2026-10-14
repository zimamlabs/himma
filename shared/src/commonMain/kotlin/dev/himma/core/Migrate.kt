package dev.himma.core

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/** How a copy to a new data folder ended. */
sealed interface Migration {
    /** Everything is in the new folder and checked: [copied] files copied now, [clashes] kept beside a different file already there. */
    data class Done(val copied: Int, val clashes: Int) : Migration
    /** There was no old folder (or it is the new one). */
    data object Nothing : Migration
    /** Something went wrong; the old folder is untouched and stays in use. Try again later. */
    data class Failed(val reason: String) : Migration
}

private const val MARKER = ".himma/migrated-from.txt"
private const val PART = ".part"

private fun filesIn(fs: FileSystem, dir: Path): List<Path> =
    fs.listRecursively(dir).filter { fs.metadataOrNull(it)?.isRegularFile == true && !it.name.endsWith(PART) }.toList()

private fun sameFile(fs: FileSystem, a: Path, b: Path): Boolean {
    val ma = fs.metadataOrNull(a); val mb = fs.metadataOrNull(b)
    if (ma?.size == null || ma.size != mb?.size) return false
    return fs.read(a) { readByteString().sha256() } == fs.read(b) { readByteString().sha256() }
}

/** "2026-10-14 (old copy).csv", then "(old copy 2)" and so on, until a name is free or already holds the same bytes. */
private fun clashTarget(fs: FileSystem, target: Path, source: Path): Path {
    val dot = target.name.lastIndexOf('.')
    val stem = if (dot > 0) target.name.substring(0, dot) else target.name
    val ext = if (dot > 0) target.name.substring(dot) else ""
    var n = 1
    while (true) {
        val candidate = target.parent!! / "$stem (old copy${if (n > 1) " $n" else ""})$ext"
        if (!fs.exists(candidate) || sameFile(fs, source, candidate)) return candidate
        n++
    }
}

/** Where each source file should be in [to]: the same relative path, or its clash name when a different file sits there. */
private fun destinations(fs: FileSystem, from: Path, to: Path): List<Pair<Path, Path>> = filesIn(fs, from).map { file ->
    val target = to / file.relativeTo(from)
    file to if (fs.exists(target) && !sameFile(fs, file, target)) clashTarget(fs, target, file) else target
}

/** True when every file in [from] has a byte-identical file at its destination in [to] (same count, size and SHA-256). */
fun verifyCopy(fs: FileSystem, from: Path, to: Path): Boolean =
    fs.exists(from) && destinations(fs, from, to).all { (src, dst) -> fs.exists(dst) && sameFile(fs, src, dst) }

/**
 * Copies the old data folder [from] into [to] and checks the copy. Nothing is ever deleted and no file in [to] is
 * overwritten: a different file with the same name keeps its place and the old one lands beside it as "(old copy)".
 * Each file goes to a temporary name first, so an interrupted run leaves no half-written file; running again finishes
 * the job. On success a marker is written in [to]. On any problem the result is [Migration.Failed] and the caller keeps
 * using [from].
 */
fun migrateFolder(fs: FileSystem, from: Path, to: Path): Migration {
    if (from == to || fs.metadataOrNull(from)?.isDirectory != true) return Migration.Nothing
    var copied = 0; var clashes = 0
    try {
        fs.createDirectories(to)
        for ((src, dst) in destinations(fs, from, to)) {
            if (fs.exists(dst)) continue
            dst.parent?.let { fs.createDirectories(it) }
            val part = dst.parent!! / (dst.name + PART)
            fs.copy(src, part)
            fs.atomicMove(part, dst)
            copied++
            if (dst != to / src.relativeTo(from)) clashes++
        }
    } catch (e: Exception) {
        return Migration.Failed("copy stopped: ${e.message}")
    }
    if (!verifyCopy(fs, from, to)) return Migration.Failed("the copy did not match the original")
    return try {
        val marker = to / MARKER.toPath()
        marker.parent?.let { fs.createDirectories(it) }
        fs.write(marker) { writeUtf8("$from\n") }
        Migration.Done(copied, clashes)
    } catch (e: Exception) {
        Migration.Failed("could not write the marker: ${e.message}")
    }
}

/** True when [to] holds the marker of a finished, verified copy from [from]. */
fun isMigrated(fs: FileSystem, from: Path, to: Path): Boolean {
    val marker = to / MARKER.toPath()
    return fs.exists(marker) && fs.read(marker) { readUtf8() }.trim() == from.toString() && verifyCopy(fs, from, to)
}

/** Deletes the old folder, but only when the copy in [to] checks out again right now. Returns whether it was removed. */
fun removeOldCopy(fs: FileSystem, from: Path, to: Path): Boolean {
    if (from == to || !fs.exists(from) || !isMigrated(fs, from, to)) return false
    return try { fs.deleteRecursively(from); true } catch (e: Exception) { false }
}

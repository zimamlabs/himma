package dev.himma.core

/** A minimal ZIP writer: every file stored without compression (the data files are small text), names in UTF-8. */
object Zip {
    private val table = IntArray(256) { n -> var c = n; repeat(8) { c = if (c and 1 != 0) (c ushr 1) xor 0xEDB88320.toInt() else c ushr 1 }; c }

    fun crc32(data: ByteArray): Int {
        var c = -1
        for (b in data) c = table[(c xor b.toInt()) and 0xFF] xor (c ushr 8)
        return c.inv()
    }

    fun write(files: List<Pair<String, ByteArray>>): ByteArray {
        val out = ArrayList<Byte>()
        val central = ArrayList<Byte>()
        fun ArrayList<Byte>.u16(v: Int) { add(v.toByte()); add((v ushr 8).toByte()) }
        fun ArrayList<Byte>.u32(v: Int) { u16(v and 0xFFFF); u16(v ushr 16) }
        for ((name, data) in files) {
            val nameBytes = name.encodeToByteArray()
            val crc = crc32(data)
            val offset = out.size
            // Local header: signature, version 20, flags bit 11 (UTF-8 names), method 0, time, date 1980-01-01, crc, sizes, lengths.
            out.u32(0x04034b50); out.u16(20); out.u16(0x0800); out.u16(0); out.u16(0); out.u16(0x21)
            out.u32(crc); out.u32(data.size); out.u32(data.size); out.u16(nameBytes.size); out.u16(0)
            out.addAll(nameBytes.toList()); out.addAll(data.toList())
            central.u32(0x02014b50); central.u16(20); central.u16(20); central.u16(0x0800); central.u16(0); central.u16(0); central.u16(0x21)
            central.u32(crc); central.u32(data.size); central.u32(data.size); central.u16(nameBytes.size); central.u16(0); central.u16(0)
            central.u16(0); central.u16(0); central.u32(0); central.u32(offset)
            central.addAll(nameBytes.toList())
        }
        val start = out.size
        out.addAll(central)
        out.u32(0x06054b50); out.u16(0); out.u16(0); out.u16(files.size); out.u16(files.size); out.u32(central.size); out.u32(start); out.u16(0)
        return out.toByteArray()
    }
}

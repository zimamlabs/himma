package dev.himma.core

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class ZipJvmTest {
    @Test fun aRealZipReaderOpensWhatWeWrite() {
        val files = listOf("a/one.txt" to "hello".encodeToByteArray(), "b.csv" to "id,نص\n1,ملاحظة\n".encodeToByteArray(), "empty" to ByteArray(0))
        val read = ArrayList<Pair<String, String>>()
        ZipInputStream(ByteArrayInputStream(Zip.write(files))).use { z ->
            generateSequence { z.nextEntry }.forEach { read += it.name to z.readBytes().decodeToString() }
        }
        assertEquals(files.map { it.first to it.second.decodeToString() }, read)
    }
}

package com.darkraha.backend.extensions

import com.darkraha.backend.helpers.Units
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class FilesKtTest {

    @Rule
    @JvmField
    var folder = TemporaryFolder()

    @Test
    fun testSizedir() {

        val root = folder.newFolder("root")



        for (it in 0..9) {
            File(root, "data_$it.bin").writeBytes(ByteArray(10000))
        }

        assertTrue(root.sizeDir() == 100000L)


        val subdir = File(root, "subdir")
        subdir.mkdirs()

        for (it in 0..4) {
            File(subdir, "data_$it.bin").writeBytes(ByteArray(10000))
        }

        assertTrue(root.sizeDir() == 150000L)
    }

    @Test
    fun testCleanupOld() {
        val root = folder.newFolder("root")
        val curTime = System.currentTimeMillis()



        for (it in 5..9) {
            val file = File(root, "data_$it.bin")
            file.writeBytes(ByteArray(10000))
            file.setLastModified(curTime - 5 * Units.DAY_MS)
        }

        for (it in 10..14) {
            val file = File(root, "data_$it.bin")
            file.writeBytes(ByteArray(10000))
            file.setLastModified(curTime - 2 * Units.DAY_MS)
        }

        val subdir = File(root, "subdir")
        subdir.mkdirs()

        for (it in 0..4) {
            val file = File(subdir, "data_$it.bin")
            file.writeBytes(ByteArray(10000))
            file.setLastModified(curTime - 3 * Units.DAY_MS)
        }

        for (it in 5..9) {
            val file = File(subdir, "data_$it.bin")
            file.writeBytes(ByteArray(10000))
            file.setLastModified(curTime - 5 * Units.DAY_MS)
        }

        println(" 1: " + root.sizeDir())
        assertTrue(root.sizeDir() == 200000L)
        root.cleanup(0, Units.DAY_MS * 4L, Units.DAY_MS * 4L)
        println(" 2: " + root.sizeDir())
        assertTrue(root.sizeDir() == 100000L)


        for (it in 5..9) {
            val file = File(subdir, "data_$it.bin")
            file.writeBytes(ByteArray(10000))
            file.setLastModified(curTime - 7 * Units.DAY_MS)
        }

        root.cleanup(60000, 2L * Units.DAY_MS, 10L * Units.DAY_MS)
        assertTrue(root.sizeDir() == 50000L)
    }

}
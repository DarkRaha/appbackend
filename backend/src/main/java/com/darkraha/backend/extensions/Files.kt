package com.darkraha.backend.extensions

import com.darkraha.backend.helpers.Units
import java.io.File

fun File.sizeDir(): Long {
    var size = 0L

    this.walk().forEach {
        if (it.isFile) {
            size += it.length()
        }
    }

    return size
}

/**
 * Delete old files.
 * @param oldTimeMs ignore values <=0
 */
fun File.cleanup(oldTimeMs: Long) {
    if (oldTimeMs <= 0) {
        return
    }
    val curTime = System.currentTimeMillis()

    this.walk().forEach {
        if (it.isFile) {
            if (curTime - it.lastModified() > oldTimeMs) {
                it.delete()
            }
        }
    }
}


fun File.cleanup(toSize: Long, oldTimeMin: Long, oldTimeMax: Long) {
    var time = oldTimeMax

    cleanup(oldTimeMax)

    if (toSize > 0) {
        val hour = Units.HOUR_MS
        val day = Units.DAY_MS
        var it = 0

        time = time + (if (time > day) -day else -hour)

        while (sizeDir() > toSize && time > oldTimeMin && it < 20) {
            cleanup(time);
            time = time + (if (time > day) -day else -hour)
            ++it
        }
    }
}



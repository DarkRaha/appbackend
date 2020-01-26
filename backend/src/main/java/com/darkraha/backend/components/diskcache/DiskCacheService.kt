package com.darkraha.backend.components.diskcache

import com.darkraha.backend.*
import com.darkraha.backend.cache.LRUCache
import com.darkraha.backend.extensions.cleanup
import com.darkraha.backend.extensions.cleanupIgnoreTmp
import com.darkraha.backend.extensions.extractFileExtension
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.math.BigInteger
import java.security.MessageDigest

/**
 * @author Verma Rahul
 */
interface DiskCacheService : Service {


    fun getWorkdir(): File

    fun subDiskCache(subdir: String): DiskCacheService

    fun cleanup(toSize: Long, timeOldMin: Long, timeOldMax: Long) = getWorkdir().cleanupIgnoreTmp(toSize, timeOldMin, timeOldMax)

    fun clear() {
        val workdir = getWorkdir()
        workdir.deleteRecursively()
        workdir.mkdir()
    }

    fun genFilename(keyUrl: String, suffix: String = ""): String

    fun genFile(keyUrl: String, suffix: String = ""): File {
        println("DiskCacheService genFile workdir=${getWorkdir()} gen name=${genFilename(keyUrl, suffix)}")
        return File(getWorkdir(), genFilename(keyUrl, suffix))
    }

    fun getFile(keyUrl: String, suffix: String = ""): File? {
        val file = genFile(keyUrl, suffix)

        return if (file.exists()) {
            file
        } else {
            null
        }
    }

    fun putFile(keyUrl: String?, file: File?) {
        if (file == null) {
            throw IllegalArgumentException("File is null")
        }

        if (keyUrl == null) {
            throw IllegalArgumentException("Key is null")
        }

        file.copyTo(genFile(keyUrl))
    }


    /**
     * Put modification of file like thumbnail of image.
     */
    fun putFileMod(keyUrl: String?, suffix: String, file: File?) {
        if (file == null) {
            throw IllegalArgumentException("File is null")
        }

        if (keyUrl == null) {
            throw IllegalArgumentException("Key is null")
        }

        val dstFile = genFile(keyUrl, suffix)
        file.copyTo(genFile(keyUrl, suffix))
    }

    fun remove(keyUrl: String) {
        val file = genFile(keyUrl)
        val name = file.nameWithoutExtension
        val name_mod = name + "_"

        getWorkdir().listFiles { p ->
            p.name.startsWith(name) || p.name.startsWith(name_mod)
        }.forEach {
            it.delete()
        }
    }

}

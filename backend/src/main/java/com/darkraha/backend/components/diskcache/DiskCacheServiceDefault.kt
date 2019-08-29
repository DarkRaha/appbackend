package com.darkraha.backend.components.diskcache

import com.darkraha.backend.infos.Param
import com.darkraha.backend.UserQuery
import com.darkraha.backend.ClientQueryEditor
import com.darkraha.backend.ServiceWorkflowHelper
import com.darkraha.backend.cache.LRUCache
import com.darkraha.backend.extensions.encodeMd5
import com.darkraha.backend.extensions.extractFileExtension
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

/**
 * @author Verma Rahul
 */
open class DiskCacheServiceDefault(wd: File = File("diskcache")) : DiskCacheService {
    override fun subDiskCache(subdir: String): DiskCacheService {
        return SubDiskCacheService(this, subdir)
    }

    val workDir = wd
    val cacheFiles = LRUCache()

    override fun getWorkdir(): File {
        return workDir
    }

    override fun genFilename(keyUrl: String, suffix: String): String {
        return if (suffix == "") {
            "${keyUrl.encodeMd5()}.${keyUrl.extractFileExtension()}"
        } else {
            "${keyUrl.encodeMd5()}_$suffix.${keyUrl.extractFileExtension()}"
        }
    }

    override fun genFile(keyUrl: String, suffix: String): File {
        val noSuffix = "" == suffix

        if (noSuffix) {
            val file: File? = cacheFiles[keyUrl] as File?
            if (file != null) {
                return file
            }
        }

        val ret = super.genFile(keyUrl, suffix)

        if (noSuffix) {
            cacheFiles[keyUrl] = ret
        }

        return ret
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun handle(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {
        val command = q.getCommand()

        with(DiskCacheConsts) {
            when (command) {
                CMD_CLEAR -> clear()
                CMD_GET -> handleGet(q, swh, response)
                CMD_PUT -> handlePut(q, swh, response)
                CMD_GEN_NAME -> handleGenName(q, swh, response)
                CMD_GEN_FILE -> handleGenFile(q, swh, response)
                CMD_CLEANUP -> handleCleanup(q, swh, response)
                else -> throw IllegalStateException("Unknown/unsupported command " + command)
            }
        }

    }


    fun handleGet(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {
        val specParams = q.specialParams()

        response.setResult(
            getFile(
                specParams.get(Param.PARAM_KEY_URL) as String,
                specParams.getOrDefault(Param.PARAM_SUFFIX, "") as String
            )
        )
    }

    fun handlePut(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {
        val specParams = q.specialParams()
        val file = q.fileSource()
        val key = specParams.get(Param.PARAM_KEY_URL) as? String

        if (file == null) {
            throw IllegalArgumentException("File is null")
        }

        if (key == null) {
            throw IllegalArgumentException("Key is null")
        }

        putFileMod(
            key, specParams.getOrDefault(Param.PARAM_SUFFIX, "") as String,
            file
        )
    }

    fun handleGenName(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {
        val specParams = q.specialParams()
        val file = q.fileSource()
        val key = specParams.get(Param.PARAM_KEY_URL) as? String

        if (file == null) {
            throw IllegalArgumentException("File is null")
        }

        if (key == null) {
            throw IllegalArgumentException("Key is null")
        }

        response.setResult(genFilename(key, specParams.getOrDefault(Param.PARAM_SUFFIX, "") as String))
    }

    fun handleGenFile(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {
        val specParams = q.specialParams()
        val file = q.fileSource()
        val key = specParams.get(Param.PARAM_KEY_URL) as? String

        if (file == null) {
            throw IllegalArgumentException("File is null")
        }

        if (key == null) {
            throw IllegalArgumentException("Key is null")
        }

        response.setResult(genFile(key, specParams.getOrDefault(Param.PARAM_SUFFIX, "") as String))
    }

    fun handleCleanup(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {
        val specParams = q.specialParams()
        val paramToSize = specParams.getOrDefault(Param.PARAM_TO_SIZE, 0L) as Long
        val paramTimeMin = specParams.getOrDefault(Param.PARAM_OLD_TIME_MIN, 0L) as Long
        val paramTimeMax = specParams.getOrDefault(Param.PARAM_OLD_TIME_MAX, 0L) as Long
        cleanup(paramToSize, paramTimeMin, paramTimeMax)
    }


}


class SubDiskCacheService(src: DiskCacheService, subdir: String) : DiskCacheService by src {
    val subdir = File(src.getWorkdir(), subdir)

    override fun getWorkdir(): File {
        return subdir
    }
}

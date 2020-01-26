package com.darkraha.backend.components.diskcache

import com.darkraha.backend.*
import com.darkraha.backend.client.BackendClientBase
import com.darkraha.backend.infos.Param
import java.io.File

/**
 *
 *
 * @author Verma Rahul
 */
open class DiskCacheClient protected constructor() : BackendClientBase() {


    open fun subClient(subdir: String): DiskCacheClient {
        val builder = Builder()

        builder.service(srv().subDiskCache(subdir))
            .backend(backend)
            .mainThread(mainThread!!)
            .executorService(executorService!!)
            .allowChange(true)

        return builder.build()

    }

    protected var allowChange = false

    protected inline fun srv(): DiskCacheService = service as DiskCacheService


    /**
     * Generates file for disk cache
     * @param suffix modification
     */
    open fun genFile(urlKey: String, suffix: String = ""): File {
        return srv().genFile(urlKey, suffix)
    }


    /**
     * Gets file from disk cache.
     * @return null, if file not exist
     */
    open fun getFile(urlKey: String, suffix: String = ""): File? {
        return srv().getFile(urlKey, suffix)
    }

    /**
     * Cleans old data in disk cache asynchronously.
     */
    open fun clean(
        maxTime: Long,
        minTime: Long = maxTime,
        toSize: Long = 200 * DiskCacheConsts.MB_BYTES,
        cb: Callback<UserQuery>? = null
    ): UserQuery {
        return buildClean(maxTime, minTime, toSize).addCallback(cb).exeAsync()
    }

    /**
     * Clears all data in dis cache.
     */
    open fun clear(cb: Callback<UserQuery>?): UserQuery {
        return buildClear().addCallback(cb).exeAsync()
    }


    open fun putFile(keyUrl: String, file: File, cb: Callback<UserQuery>?): UserQuery {
        return buildPutFile(keyUrl, file).exeAsync()
    }


    //---------------------------------------------------------------------------------------------------------------

    open fun buildClean(
        maxTime: Long,
        minTime: Long,
        toSize: Long
    ): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().command(DiskCacheConsts.CMD_CLEANUP)
            .addNamedSpecialParam(Param.PARAM_OLD_TIME_MAX, maxTime)
            .addNamedSpecialParam(Param.PARAM_OLD_TIME_MIN, minTime)
            .addNamedSpecialParam(Param.PARAM_TO_SIZE, toSize)
    }

    open fun buildClear(): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().command(DiskCacheConsts.CMD_CLEAR)
    }

    open fun buildPutFile(keyUrl: String, file: File): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().command(DiskCacheConsts.CMD_PUT)
            .addNamedSpecialParam(Param.PARAM_FILE_SOURCE, file)
            .addNamedSpecialParam(Param.PARAM_KEY_URL, keyUrl)
    }


    //-------------------------------------------------------------------------------------------------
    class Builder : ClientBuilder<DiskCacheClient, DiskCacheService, Builder>() {


        private var _allowChange = false


        override fun newResult(): DiskCacheClient = DiskCacheClient()

        override fun checkService() {
            if (_service == null) {
                _service = DiskCacheServiceDefault(_workdir ?: File("diskcache"))
            }
        }

        override fun checkWorkdir() {
            if (_workdir == null) {
                _workdir = _service?.getWorkdir() ?: _backend?.run {
                    File(this.workdir, "diskcache")
                } ?: File("diskcache")
            }

            _workdir!!.mkdirs()
        }


        fun allowChange(v: Boolean): Builder {
            _allowChange = v
            return this
        }

        override fun build(): DiskCacheClient {
            super.build()
            result.allowChange = _allowChange
            return result
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(): DiskCacheClient {
            return Builder().build()
        }
    }
}

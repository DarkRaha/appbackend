package com.darkraha.backend.components.diskcache

import com.darkraha.backend.*
import com.darkraha.backend.client.ClientBase
import com.darkraha.backend.infos.Param
import java.io.File

/**
 * todo lock changing disk cache for top client (allowChange) or add param which subdirs process or not
 *
 * @author Verma Rahul
 */
open class DiskCacheClientDefault protected constructor() : ClientBase(), DiskCacheClient {
    override fun getWorkDir(): File {
        return workdir
    }

    override fun subClient(subdir: String): DiskCacheClient {
        val builder = Builder()

        builder.service(srv().subDiskCache(subdir))
            .mainThread(mainThread!!)
            .executorService(executorService!!)
            .queryManager(queryManager!!)
            .allowChange(true)

        return builder.build()

    }

    protected var allowChange = false

    protected inline fun srv(): DiskCacheService = service as DiskCacheService


    /**
     * Generates file for disk cache
     * @param suffix modification
     */
    override fun genFile(urlKey: String, suffix: String): File {
        return srv().genFile(urlKey, suffix)
    }


    /**
     * Gets file from disk cache.
     * @return null, if file not exist
     */
    override fun getFile(urlKey: String, suffix: String): File? {
        return srv().getFile(urlKey, suffix)
    }

    /**
     * Cleans old data in disk cache asynchronously.
     */
    override fun clean(maxTime: Long, minTime: Long, toSize: Long, cb: Callback<UserQuery>?): UserQuery {
        return buildClean(maxTime, minTime, toSize).addCallback(cb).exeAsync()
    }

    /**
     * Clears all data in dis cache.
     */
    override fun clear(cb: Callback<UserQuery>?): UserQuery {
        return buildClear().addCallback(cb).exeAsync()
    }


    override fun putFile(keyUrl: String, file: File, cb: Callback<UserQuery>?): UserQuery {
        return buildPutFile(keyUrl, file).exeAsync()
    }


    //---------------------------------------------------------------------------------------------------------------

    override fun buildClean(maxTime: Long, minTime: Long, toSize: Long): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().command(DiskCacheConsts.CMD_CLEANUP)
            .addNamedSpecialParam(Param.PARAM_OLD_TIME_MAX, maxTime)
            .addNamedSpecialParam(Param.PARAM_OLD_TIME_MIN, minTime)
            .addNamedSpecialParam(Param.PARAM_TO_SIZE, toSize)
    }

    override fun buildClear(): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().command(DiskCacheConsts.CMD_CLEAR)
    }

    override fun buildPutFile(keyUrl: String, file: File): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().command(DiskCacheConsts.CMD_PUT)
            .addNamedSpecialParam(Param.PARAM_FILE_SOURCE, file)
            .addNamedSpecialParam(Param.PARAM_KEY_URL, keyUrl)
    }


    //-------------------------------------------------------------------------------------------------
    class Builder : ClientBuilder<DiskCacheClientDefault, DiskCacheService, Builder>() {


        private var _allowChange = false


        override fun newResult(): DiskCacheClientDefault = DiskCacheClientDefault()

        override fun defaultService(): Service {
            return DiskCacheServiceDefault(_workdir ?: File("diskcache"))
        }

        override fun defaultWorkdir(): File {
            return result.srv().getWorkdir()
        }

        fun allowChange(v: Boolean): Builder {
            _allowChange = v
            return this
        }

        override fun build(): DiskCacheClientDefault {
            super.build()
            result.allowChange = _allowChange
            return result
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(): DiskCacheClientDefault {
            return Builder().build()
        }
    }
}
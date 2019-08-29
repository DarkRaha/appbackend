package com.darkraha.backend.components.diskcache

import com.darkraha.backend.Callback
import com.darkraha.backend.QueryBuilder
import com.darkraha.backend.UserQuery
import com.darkraha.backend.WorkflowBuilder1
import com.darkraha.backend.client.Client
import java.io.File

interface DiskCacheClient : Client {

    /**
     * Creates new disk cache based on this client. As example image manager can make folder images and work within it.
     */
    fun subClient(subdir: String): DiskCacheClient


    fun getWorkDir(): File

    /**
     * Generates file for disk cache
     * @param suffix modification
     */
    fun genFile(urlKey: String, suffix: String = ""): File

    /**
     * Gets file from disk cache.
     * @return null, if file not exist
     */
    fun getFile(urlKey: String, suffix: String = ""): File?

    /**
     * Cleans old data in disk cache asynchronously.
     */
    fun clean(maxTime: Long, minTime: Long = maxTime, toSize: Long = 0, cb: Callback<UserQuery>? = null): UserQuery

    fun clear(cb: Callback<UserQuery>? = null): UserQuery

    /**
     * Adds file to cache disk.
     */
    fun putFile(keyUrl: String, file: File, cb: Callback<UserQuery>? = null): UserQuery

    //------------------------------------------------------------------------------------
    fun buildClean(maxTime: Long, minTime: Long = maxTime, toSize: Long = 0): QueryBuilder<WorkflowBuilder1>

    fun buildClear(): QueryBuilder<WorkflowBuilder1>

    fun buildPutFile(keyUrl: String, file: File): QueryBuilder<WorkflowBuilder1>

}
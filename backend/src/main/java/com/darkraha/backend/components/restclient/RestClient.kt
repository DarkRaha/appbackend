package com.darkraha.backend.components.restclient

import com.darkraha.backend.QueryBuilder
import com.darkraha.backend.QueryCallback
import com.darkraha.backend.WorkflowBuilder1
import com.darkraha.backend.client.Client
import kotlin.reflect.KClass

interface RestClient : Client {

    fun buildCommandQuery(
        urlPath: String,
        cmd: String?,
        id: String?,
        clsResult: KClass<*>? = null,
        cb: QueryCallback?=null
    ): QueryBuilder<WorkflowBuilder1>

}
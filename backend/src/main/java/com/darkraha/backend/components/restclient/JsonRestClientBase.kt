package com.darkraha.backend.components.restclient

import com.darkraha.backend.*
import com.darkraha.backend.client.ClientBase
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.components.http.HttpClientDefault
import com.darkraha.backend.components.json.JsonManager
import com.darkraha.backend.components.json.JsonManagerDefault
import kotlin.reflect.KClass

/**
 * @author Verma Rahul
 */
open class JsonRestClientBase protected constructor() : RestClient, ClientBase() {

    lateinit var baseUrl: String
        protected set

    protected lateinit var httpClient: HttpClient
    protected lateinit var jsonManager: JsonManager

    protected val _runningCommands = HashMap<String, UserQuery>()

    val runningCommands get() = _runningCommands as Map<String, UserQuery>


    protected val clientCallback = object : QueryCallback {

        override fun onFinish(query: UserQuery) {
            val cmdid = query.getCmdQueryId()
            if (cmdid != null) {
                _runningCommands.remove(cmdid)
            }
        }

    }

    override fun prepareQuery(): Query {
        val ret = httpClient.prepareQuery()
        ret.urlBase(baseUrl)
            .client(this)
        return ret
    }

    override fun buildCommandQuery(
        urlPath: String,
        cmd: String?,
        id: String?,
        clsResult: KClass<*>?,
        cb: QueryCallback?
    ): QueryBuilder<WorkflowBuilder1> {
        val ret = prepareQuery()
        ret.addUrlPath(urlPath)
            .command(cmd)
            .queryId(id)
            .addCallback(cb)
            .addPrepareProcessor(this::onPrepare)
            .addPostProcessor(this::onPostprocessor)
            .callbackFirst(clientCallback)
            .destinationClass(clsResult)

        return ret
    }


    open protected fun onPrepare(q: UserQuery, response: ClientQueryEditor) {
        val cmdid = q.getCmdQueryId()
        if (cmdid != null) {
            if (cmdid !in _runningCommands) {
                _runningCommands[cmdid] = q
            } else {
                response.cancel(-2, "Canceled by client, query already executed.")
            }
        }
    }


    open protected fun onPostprocessor(q: UserQuery, response: ClientQueryEditor) {

        val json = q.rawString()!!
        val clsDest = q.getDestinationClass()
        if (clsDest != null) {
            val fromJson = jsonManager.fromJson(json, clsDest)
            if (checkSuccess(q, response, fromJson)) {
                response.responseInfo().result = jsonManager.fromJson(json, clsDest)
            }
        }
    }

    open protected fun checkSuccess(
        q: UserQuery,
        response: ClientQueryEditor,
        fromJson: Any
    ): Boolean {
        return true
    }


    abstract class RestClientBaseBuilder
    <TClient : JsonRestClientBase,
            TService : Service,
            Builder : RestClientBaseBuilder<TClient, TService, Builder>>

        : ClientBase.ClientBuilder<TClient, TService, Builder>() {


        protected var _urlBase: String? = null
        protected var _httpClient: HttpClient? = null
        protected var _jsonManager: JsonManager? = null


        open fun checkUrlBase() {
            if (_urlBase == null) {
                throw IllegalStateException("Url base not assigned.")
            }
        }

        open fun urlBase(urlBase: String): Builder {
            _urlBase = urlBase
            return builder
        }

        open fun checkHttpClient() {
            if (_httpClient == null) {
                _httpClient = _backend?.httpClient ?: HttpClientDefault.newInstance()
            }
        }

        open fun httpClient(httpClient: HttpClient): Builder {
            _httpClient = httpClient
            return builder
        }

        open fun checkJsonManager() {
            if (_jsonManager == null) {
                _jsonManager = _backend?.jsonManager ?: JsonManagerDefault()
            }
        }

        open fun jsonManager(jsonManager: JsonManager): Builder {
            _jsonManager = jsonManager
            return builder
        }


        override fun build(): TClient {
            super.build()
            checkUrlBase()
            checkHttpClient()
            checkJsonManager()

            result.baseUrl = _urlBase!!
            result.httpClient = _httpClient!!
            result.jsonManager = _jsonManager!!

            return result
        }


    }


}
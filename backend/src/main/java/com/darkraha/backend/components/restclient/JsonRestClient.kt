package com.darkraha.backend.components.restclient

import com.darkraha.backend.*
import com.darkraha.backend.client.BackendClientA
import com.darkraha.backend.client.BackendClientBase
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.components.json.JsonManager
import com.darkraha.backend.components.json.JsonManagerDefault
import kotlin.reflect.KClass

/**
 * @author Verma Rahul
 */
open class JsonRestClient protected constructor() : BackendClientBase() {

    lateinit var baseUrl: String
        protected set

    protected lateinit var httpClient: HttpClient
    protected lateinit var jsonManager: JsonManager


    protected val clientCallback = object : QueryCallback {

        override fun onFinish(query: UserQuery) {

            println("JsonRestClientBase onFinish " + query.getCmdQueryId() + " isOk=" + query.isSuccess()
                    + " isCanceled " + query.isCanceled()
                    + " isError " + query.isError() + " url=" + query.url())

            if (query.isError()) {
                println("JsonRestClientBase onFinish err=" + query.errorMessage())
            }

            if (query.isCanceled()) {
                println("JsonRestClientBase onFinish canceled=" + query.cancelInfo().message)
            }
        }

    }


    override fun prepareQuery(srcQuery: Query?): Query {
        return super.prepareQuery(httpClient.prepareQuery(srcQuery)).apply {
            urlBase(baseUrl).optAsString().callbackFirst(clientCallback)
        }
    }

    open fun buildApiQuery(
            urlPath: String,
            cmd: String?,
            id: String?,
            clsResult: KClass<*>?,
            cb: QueryCallback?
    ): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery()
                .addUrlPath(urlPath)
                .command(cmd)
                .queryId(id)
                .addCallback(cb)
                .destinationClass(clsResult)
    }


    override fun onPostProcessor(q: UserQuery, response: ClientQueryEditor) {

        println("JsonRestClientBase onPostProcessor 1")
        val json = q.rawString()!!
        println("JsonRestClientBase onPostProcessor 2 " + json)
        val clsDest = q.getDestinationClass()
        println("JsonRestClientBase onPostProcessor 3")
        if (clsDest != null) {
            println("JsonRestClientBase onPostProcessor 4")
            response.responseInfo().result = jsonManager.fromJsonString(json, clsDest.java)
            println("JsonRestClientBase onPostProcessor 4a")
        }
        println("JsonRestClientBase onPostProcessor 5")
    }

    open protected fun checkSuccess(
            q: UserQuery,
            response: ClientQueryEditor,
            fromJson: Any
    ): Boolean {
        return true
    }


    abstract class RestClientBaseBuilder
    <TClient : JsonRestClient,
            TService : Service,
            Builder : RestClientBaseBuilder<TClient, TService, Builder>>

        : BackendClientA.ClientBuilder<TClient, TService, Builder>() {


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
                _httpClient = _backend?.httpClient ?: HttpClient.newInstance()
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
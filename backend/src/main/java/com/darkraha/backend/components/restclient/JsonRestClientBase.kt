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


    protected val clientCallback = object : QueryCallback {

        override fun onFinish(query: UserQuery) {

            println("JsonRestClientBase onFinish " + query.getCmdQueryId() + " isOk=" + query.isSuccess()
                    + " isCanceled " + query.isCanceled()
                    + " isError " + query.isError()+" url="+query.url())

            if (query.isError()) {
                println("JsonRestClientBase onFinish err=" + query.errorMessage())
            }

            if (query.isCanceled()) {
                println("JsonRestClientBase onFinish canceled=" + query.cancelInfo().message)
            }
        }

    }

    override fun prepareDefaultQuery(): Query {
        val ret = prepareQuery(httpClient as ClientBase)
        ret.urlBase(baseUrl)
                .optAsString()
                .callbackFirst(clientCallback)
        return ret
    }

    override fun buildCommandQuery(
            urlPath: String,
            cmd: String?,
            id: String?,
            clsResult: KClass<*>?,
            cb: QueryCallback?
    ): QueryBuilder<WorkflowBuilder1> {
        val ret = prepareDefaultQuery()
        ret.addUrlPath(urlPath)
                .command(cmd)
                .queryId(id)
                .addCallback(cb)
                .destinationClass(clsResult)

        return ret
    }



    override fun onPostProcessor(q: UserQuery, response: ClientQueryEditor) {

        println("JsonRestClientBase onPostProcessor 1" )
        val json = q.rawString()!!
        println("JsonRestClientBase onPostProcessor 2 "+json )
        val clsDest = q.getDestinationClass()
        println("JsonRestClientBase onPostProcessor 3" )
        if (clsDest != null) {
            println("JsonRestClientBase onPostProcessor 4" )
            response.responseInfo().result = jsonManager.fromJsonString(json, clsDest.java)
            println("JsonRestClientBase onPostProcessor 4a" )
        }
        println("JsonRestClientBase onPostProcessor 5" )
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
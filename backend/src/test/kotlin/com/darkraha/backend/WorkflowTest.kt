package com.darkraha.backend

import com.darkraha.backend.components.mock.MockClient
import com.darkraha.backend.components.mock.MockService
//import com.darkraha.backend.injection.DaggerBackendComponentTest
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Executors

class WorkflowTest {

  //  val components = DaggerBackendComponentTest.create()
    val ok = "Ok"
    val err = "Mock error"

    val backend = Backend.newInstance()
    val mockClient = MockClient()

    init {
        //components.inject(Backend.sharedInstance)
    mockClient.onAddToBackend(backend)
    }

    @Test
    fun testProcessors() {
        val queryManager = backend.queryManager
        var query = queryManager.newQuery()
        val executor = Executors.newFixedThreadPool(1)
        val mockService = MockService()
        mockService.state=4
        mockService.result = ok

        var prepare = 0
        var pre = 0
        var post = 0


        with(query) {
            service(mockService)
            mainThread(backend.mainThread)
            executor(executor).addPrepareProcessor { q, r ->
                prepare++
            }.addPreProcessor { q, r ->
                pre++

            }.addPostProcessor { q, r -> post++ }.addPostProcessor { q, r -> post++ }.exeAsync().waitFinish()

        }

        assertTrue(prepare==1)
        assertTrue(pre==1)
        assertTrue(post==2)
    }

    @Test
    fun testWorkflowListener() {
        val queryManager = backend.queryManager
        var query = queryManager.newQuery()
        val executor = Executors.newFixedThreadPool(1)
        val mockService = MockService()
        mockService.result = ok

        query.service(mockService)
        query.mainThread(backend.mainThread)
        query.executor(executor)


        val steps = mutableListOf<Int>()

        val w = listOf(1, 2, 3, 4, 5, 6, 9, 10)


        query.addWorkflowListener {
            steps.add(it.workflowStep())
        }

        query.exeAsync()
        query.waitFinish()

        steps.forEachIndexed { index, i ->
            assertTrue(w[index] == steps[index])
        }


        steps.clear()
        query = queryManager.newQuery()
        query.service(mockService)
        query.mainThread(backend.mainThread)
        query.executor(executor).addOnSuccess { }

        query.addWorkflowListener {
            steps.add(it.workflowStep())
        }

        query.exeAsync()
        query.waitFinish()

        val w2 = listOf(1, 2, 3, 4, 5, 7, 8, 6, 9, 10)
        steps.forEachIndexed { index, i -> assertTrue(w2[index] == steps[index]) }
    }

    @Test
    fun testEmptyWorkflowOk() {
        val queryManager = backend.queryManager
        val query = queryManager.newQuery()
        val executor = Executors.newFixedThreadPool(1)
        val mockService = MockService()
        mockService.result = ok

        query.service(mockService)
        query.mainThread(backend.mainThread)
        query.executor(executor)

        query.exeAsync().waitFinish()

        assertTrue(query.result() == "Ok")
        executor.shutdown()
    }


    @Test
    fun testEmptyWorkflowError() {
        val queryManager = backend.queryManager
        val query = queryManager.newQuery()
        val executor = Executors.newFixedThreadPool(1)
        val mockService = MockService()
        mockService.state = 1
        mockService.errorMessage = err

        query.service(mockService)
        query.mainThread(backend.mainThread)
        query.executor(executor)

        query.exeAsync().waitFinish()

        executor.shutdown()

        assertTrue(query.isError())
        assertTrue(query.errorMessage() == err)
        assertTrue(query.errorException() is IllegalStateException)
    }


    @Test
    fun testEmptyWorkflowCancel() {
        val queryManager = backend.queryManager
        val query = queryManager.newQuery()
        val executor = Executors.newFixedThreadPool(1)
        val mockService = MockService()
        mockService.state = 2

        query.service(mockService)
        query.mainThread(backend.mainThread)
        query.executor(executor)

        query.exeAsync().waitFinish()

        assertTrue(query.isCanceled())
        executor.shutdown()
    }

    @Test
    fun testProgressListener() {
        val queryManager = backend.queryManager
        val query = queryManager.newQuery()
        val executor = Executors.newFixedThreadPool(1)
        val mockService = MockService()
        var progress = 0f
        mockService.result = ok

        query.service(mockService)
        query.mainThread(backend.mainThread)
        query.executor(executor)

        query.progressListener { current, total ->
            progress = current
        }

        query.exeAsync().waitFinish()

        assertTrue(query.result() == "Ok")
        assertTrue(progress == 9f)
        executor.shutdown()
    }

    @Test
    fun testCallbacks() {
        val queryManager = backend.queryManager
        val query = queryManager.newQuery()
        val executor = Executors.newFixedThreadPool(1)
        val mockService = MockService()
        mockService.result = ok


        var cbOnsuccess = 0

        var cbSuccess = 0
        var cbPrepare = 0
        var cbComplete = 0
        var cbFinish = 0

        query.service(mockService)
        query.mainThread(backend.mainThread)


        val cb = object : Callback<UserQuery> {
            override fun onSuccess(query: UserQuery) {
                cbSuccess++
            }

            override fun onPrepare(query: UserQuery) {
                cbPrepare++
            }

            override fun onComplete(query: UserQuery) {
                cbComplete++
            }

            override fun onFinish(query: UserQuery) {
                cbFinish++
            }

        }

        query.executor(executor).addOnSuccess { cbOnsuccess++ }.callbackFirst(cb).callbackLast(cb).addCallback(cb)

        query.asQueryBuilder().addOnSuccess { cbOnsuccess++ }.exeAsync()
        query.waitFinish()

        assertTrue(cbOnsuccess == 2)
        assertTrue(cbSuccess == 3 && cbPrepare == 3 && cbComplete == 3 && cbFinish == 3)
    }

}
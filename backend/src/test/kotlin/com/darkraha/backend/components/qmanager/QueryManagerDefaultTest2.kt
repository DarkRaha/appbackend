package com.darkraha.backend.components.qmanager

import com.darkraha.backend.Backend
//import com.darkraha.backend.injection.DaggerBackendComponentTest
import org.junit.Test

class QueryManagerDefaultTest2 {


    val url = "http://example.com/"
  //  val components = DaggerBackendComponentTest.create()

    val backend = Backend.newInstance()


    @Test
    fun testWorkflow() {
//
//        val queryManager = backend.queryManager
//        queryManager.clear()
//
//        val query = queryManager.newQuery()
//
//        query.workflow.workflowStep = Workflow.WORKFLOW_PREPARE_START
//        queryManager.onWorkflowStep(query)
//        assertTrue(queryManager.sizeOfExecuted()==1)
//
//        query.workflow.workflowStep = Workflow.WORKFLOW_FINISH_END
//        queryManager.onWorkflowStep(query)
//        assertTrue(queryManager.sizeOfExecuted()==0)
//        assertTrue(queryManager.sizeOfPool()==0)
//
//        query.workflow.workflowStep = Workflow.WORKFLOW_ON_FREE
//        queryManager.onWorkflowStep(query)
//        assertTrue(queryManager.sizeOfExecuted()==0)
//        assertTrue(queryManager.sizeOfPool()==1)
    }


    @Test
    fun testFindSelect() {
//        val queryManager = backend.queryManager
//        queryManager.clear()
//
//        for (it in 0..9) {
//            val query = Query()
//            query.url(url + it)
//            queryManager.addQuery(query)
//        }
//
//        assertTrue(queryManager.findFirstOrNull { it.url()?.endsWith("9") ?: false } != null)
//        assertTrue(queryManager.findLastOrNull { it.url()?.endsWith("0") ?: false } != null)
//        assertTrue(queryManager.findFirstOrNull { it.url()?.endsWith("19") ?: false } == null)
//
//        val sizeSelect = queryManager.select {
//            val url = it.url()
//            if (url != null) {
//                val num = url.last().toInt() - '0'.toInt()
//                return@select num in 2..5
//            }
//            false
//
//        }.size
//
//        assertTrue(sizeSelect == 4)

    }


}
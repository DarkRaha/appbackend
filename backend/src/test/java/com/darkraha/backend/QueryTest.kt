package com.darkraha.backend

import com.darkraha.backend.components.mainthread.MainThreadDefault
import com.darkraha.backend.components.mock.MockService
import com.darkraha.backend.infos.Param
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.concurrent.Executors


class QueryTest {

    @Test
    fun testFileParam(){

        val srcFile= File("1")
        val objFile = File("2")
        val mapFile = File("3")

        val q = Query()

        q.source(srcFile)

        assertTrue(q.fileSource()==srcFile)
        q.reset()
        assertTrue(q.fileSource()==null)

        q.objectParam(objFile)
        assertTrue(q.fileSource()==objFile)
        q.reset()

        q.addNamedSpecialParam(Param.PARAM_FILE_SOURCE, mapFile)
        assertTrue(q.fileSource()==mapFile)

    }


    @Test
    fun testWorkflowBuilder() {
        val q = Query()
        val builder = q.asWorkflowBuilder()
        val reader = q.asQueryReader()

        builder.service(MockService())
        assertTrue(reader.service() != null)

        val srv: MockService? = reader.getServiceAs()

        assertTrue(srv != null)

        builder.executor(Executors.newCachedThreadPool())
        assertTrue(q.workflow.executor != null)

        builder.mainThread(MainThreadDefault())
        assertTrue(q.workflow.mainThread != null)

    }

    @Test
    fun testBuilder() {

        val q = Query()

        val builder = q.asQueryBuilder()
        val reader = q.asQueryReader()

        builder.url("http://example.com")
        assertTrue(reader.url() == "http://example.com")

        builder.addCookie("key", "cookie txt")
        assertTrue(reader.inputCookies().get("key") == "cookie txt")

        builder.addNamedParam("key2", "txt")
        assertTrue(reader.namedParams().get("key2") == "txt")

        builder.addNamedSpecialParam("key3", 35)
        assertTrue(reader.specialParams().get("key3") == 35)

        builder.command("cmd")
        assertTrue(reader.getCommand() == "cmd")

        builder.queryId("2345")
        assertTrue(reader.getQueryId() == "2345")

        builder.post()
        assertTrue(reader.method() == "POST")

        builder.destination("dst", null,true)
        assertTrue(reader.getDestinationValue() == "dst" && reader.isOptionAutoCloseDestination())

        builder.source("src", null,true)
        assertTrue(reader.getSourceValue() == "src" && reader.isOptionAutoCloseSource())

        builder.weakUiParam("ui")
        assertTrue(reader.uiParam() == "ui")

        val workflowListener: WorkflowListener = object : WorkflowListener {
            override fun onWorkflowStep(q: UserQuery) {

            }
        }

        builder.addWorkflowListener(workflowListener)
        assertTrue(q.workflow.workflowListener.size == 1)
        assertTrue(q.workflow.workflowListener[0] == workflowListener)

        val cb = object : Callback<UserQuery> {}

        builder.addCallback(cb)
        assertTrue(q.workflow.callbacks.size == 1)
        assertTrue(q.workflow.callbacks[0] == cb)

        builder.resSync("fg")
        assertTrue(q.workflow.syncResource == "fg")

    }

}
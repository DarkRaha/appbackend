package com.darkraha.backend.client

import com.darkraha.backend.Query
import org.junit.Assert.*
import org.junit.Test

class BackendClientBaseTest{
    val client = BackendClientBase()

    @Test
    fun test(){
        client.addCommandForTracking("cmd1")
        assertTrue("cmd1" in client.commandsForTracking)

        val q = Query().apply {  command("cmd1").queryId("1")}
        q.workflow.setUsed(true)
        val q2 = Query().apply {  command("cmd1").queryId("2")}
        q2.workflow.setUsed(true)
        val q3 = Query().apply {  command("cmd1").queryId("1")}
        q3.workflow.setUsed(true)

        assertTrue(!client.isRunning("cmd1","1"))
        client.onQueryStart(q)
        assertTrue(client.isRunning("cmd1","1"))
        client.onQueryEnd(q)
        assertTrue(!client.isRunning(null,null))

        //---------------------
        client.onQueryStart(q)
        client.onQueryStart(q2)
        client.onQueryStart(q3)
        assertTrue(q3.isCanceled())


    }
}
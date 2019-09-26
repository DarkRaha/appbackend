package com.darkraha.backend.components.mock

import com.darkraha.backend.*
import com.darkraha.backend.client.ClientBase
import java.io.File

/**
 * @author Verma Rahul
 */
class MockClient : ClientBase() {

    override fun prepareDefaultQuery(): Query {
        val q = super.prepareDefaultQuery()
        q.command("MockCommand")
        return q
    }

}


/**
 * @param retState 0 - success, 1 - error,  2 - cancel, 4 - don't finish so post processors will be run
 */
class MockService : Service {
    var state: Int = 0
    var timeout: Long = 2000
    var timeoutProgress: Long = 100
    var progressCount: Int = 9
    var result: Any? = null
    var resultFile: File? = null
    var rawStringResult: String? = null
    var rawBytesResult: ByteArray? = null
    var errorMessage: String? = null


    override fun isAvailable(): Boolean {
        return true
    }

    override fun handle(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {

        if (timeout > 0) {
            try {
                Thread.sleep(timeout)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        if (swh.isProgressListenerPossible()) {
            for (it in 0..progressCount) {
                swh.dispatchProgress(it.toFloat(), progressCount.toFloat())
                try {
                    Thread.sleep(timeoutProgress)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        when {
            state == 0 -> {
                response.success()
                response.setResult(result)
                if (q.isOptionSaveFile()) {
                    response.setResultFile(resultFile)
                }

                if (q.isOptionSaveBytes()) {
                    response.setRawBytes(rawBytesResult)
                }

                if (q.isOptionSaveString()) {
                    response.setRawString(rawStringResult)
                }

                if (q.isOptionSaveObject()) {

                }
            }

            state == 1 -> {
                throw IllegalStateException(errorMessage)
            }

            state == 2 -> {
                response.cancel()
            }
        }
    }
}
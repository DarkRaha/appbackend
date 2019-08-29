package com.darkraha.backend.components.endecode

import com.darkraha.backend.Backend
import com.darkraha.backend.Callback
import com.darkraha.backend.UserQuery
import com.darkraha.backend.getResultAs
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class EndecodeClientDefaultTest {

    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    val endecodeClient = EndecodeClientDefault.newInstance()


    @Test
    fun testDecode() {

        //val c = backend.endecodeClient

        val src = ByteArray(100) { 41 }
        val file = File(tmpFolder.root, "aaa.bin")
        file.writeBytes(src)

        endecodeClient.addDecoder(BinaryFileDecoder())
        endecodeClient.addDecoder(TextFileDecoder())


        var bool: Boolean = false

        endecodeClient.decode(file, null, ByteArray::class, null, object : Callback<UserQuery> {
            override fun onSuccess(query: UserQuery) {
                val result: ByteArray? = query.getResultAs()
                bool = result != null && result.size == 100 && result[10].toInt() == 41
            }

            override fun onError(query: UserQuery) {
                println(query.errorMessage())
            }
        }).waitFinish()

        assertTrue(bool)

        bool = false

        endecodeClient.decode(file, null, String::class, null, object : Callback<UserQuery> {
            override fun onSuccess(query: UserQuery) {
                val result: String? = query.getResultAs()
                bool = result != null && result.length == 100 && result[10].toInt() == 41
            }

            override fun onError(query: UserQuery) {
                println(query.errorMessage())
            }
        }).waitFinish()

        assertTrue(bool)
    }

}
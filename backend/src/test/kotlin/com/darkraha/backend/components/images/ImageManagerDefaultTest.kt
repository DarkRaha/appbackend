package com.darkraha.backend.components.images

import com.darkraha.backend.*
import com.darkraha.backend.components.endecode.BinaryFileDecoder
import com.darkraha.backend.components.endecode.FileDecoder
import com.darkraha.backend.components.endecode.FileEncoder
import com.darkraha.backend.extraparams.ImageLoadEP
import org.awaitility.kotlin.await
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ImageManagerDefaultTest {

    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    val imageManager = ImageManager.Builder().imagePlatformHelper(ImagePlatformHelperTest()).build()


    var isSuccess = AtomicBoolean(false)

    @Volatile
    var isCanceled = false

    @Volatile
    var isFinished = false


    @Test
    fun testCancelLoad() {
        imageManager.diskCacheClient.buildClear().exeSync()
        isCanceled = false
        isSuccess.set(false)

        val cb = object : QueryCallback {

            override fun onCancel(query: UserQuery) {
                isCanceled = true
            }

            override fun onSuccess(query: UserQuery) {
                isSuccess.set(true)
            }

            override fun onError(query: UserQuery) {
                println(query.errorMessage())
                query.errorException()?.printStackTrace()
            }


            override fun onFinish(query: UserQuery) {
                isFinished = true

            }
        }

        val url = "https://pm1.narvii.com/6652/96cfbc896f4f277f98f09d049bd835baed62a0bf_hq.jpg"
        val imageUi = ImageUi()

        var query = imageManager.buildLoad(url, imageUi, cb, null, null)
            .addWorkflowListener() {

                if (it.workflowStep() == 3) {
                    await.atLeast(5000L, TimeUnit.MILLISECONDS)
                }
            }
            .exeAsync()

        await.atLeast(2000L, TimeUnit.MILLISECONDS)
        imageManager.cancelLoad(imageUi)

        await.atMost(10000, TimeUnit.MILLISECONDS).until {
            isFinished
        }

        assert(isCanceled)
        isCanceled = false
        isFinished = false


        imageManager.diskCacheClient.buildClear().exeSync()
        query = imageManager.buildLoad(url, null, cb, null, null)
            .addWorkflowListener() {
                if (it.workflowStep() == 3) {
                    await.atLeast(5000L, TimeUnit.MILLISECONDS)
                }
            }
            .exeAsync()

        await.atLeast(2000L, TimeUnit.MILLISECONDS)
        imageManager.cancelLoad(imageUi)

        await.atMost(10000, TimeUnit.MILLISECONDS).until {
            isFinished
        }

        assert(!isCanceled)
        assert(isSuccess.get())
        imageManager.diskCacheClient.buildClear().exeSync()
        imageManager.diskCacheClient.workdir.delete()
    }


    @Test
    fun testLoadFile() {

        assertTrue(imageManager.endecoder.getDecodersList().size == 2)

        imageManager.diskCacheClient.buildClear().exeSync()


        val dirImages = imageManager.diskCacheClient.workdir
        assertTrue(dirImages.isDirectory)
        assertTrue(dirImages.listFiles().size == 0)


        val url = "https://pm1.narvii.com/6652/96cfbc896f4f277f98f09d049bd835baed62a0bf_hq.jpg"
        val imageUi = ImageUi()


        val cb = object : QueryCallback {


            override fun onSuccess(query: UserQuery) {
                println("1")
                isSuccess.set(true)
            }

            override fun onError(query: UserQuery) {
                println(query.errorMessage())
                query.errorException()?.printStackTrace()
            }
        }


        imageManager.buildLoad(url, imageUi, cb, null, null).exeAsync().waitFinish()


        assertTrue(isSuccess.get())
        isSuccess.set(true)

        assertTrue(imageUi.image != null)
        assertTrue(imageUi.image?.data != null ?: false)
        assertTrue(imageUi.image?.type == "jpg" ?: false)


        imageUi.image = null

        imageManager.loadFromMemory(url, imageUi)
        assertTrue(imageUi.image != null)
        assertTrue(imageUi.image?.data != null ?: false)
        assertTrue(imageUi.image?.type == "jpg")

        imageUi.image = null
        imageManager.removeFromMemory(url)
        imageManager.loadFromMemory(url, imageUi)
        assertTrue(imageUi.image == null)


        val cb2 = object : QueryCallback {


            override fun onSuccess(query: UserQuery) {
                isSuccess.set(true)
                assertTrue(imageUi.image != null)
                assertTrue(imageUi.image?.data != null ?: false)
                assertTrue(imageUi.image?.type == "jpg")
            }

            override fun onError(query: UserQuery) {
                println(query.errorMessage())
                query.errorException()?.printStackTrace()
            }
        }
        val s = imageManager.buildDecode(url, imageManager.imageFile(url), imageUi).exeAsync()
            .waitFinish()


        assertTrue(imageUi.image != null)
        assertTrue(imageUi.image?.data != null ?: false)
        assertTrue(imageUi.image?.type == "jpg")



        imageManager.diskCacheClient.buildClear().exeSync()
        imageManager.diskCacheClient.workdir.delete()


    }


    @Test
    fun testLoadAppend() {
        imageManager.diskCacheClient.buildClear().exeSync()
        val url = "https://pm1.narvii.com/6652/96cfbc896f4f277f98f09d049bd835baed62a0bf_hq.jpg"
        val imageUi1 = ImageUi()
        val imageUi2 = ImageUi()
        val q = imageManager.buildLoad(url, imageUi1, null, null, null).exeAsync()


        val result = imageManager.buildLoad(url, imageUi2, null, null, null).exeAsync().waitFinish()
            .isCanceled()
        assert(result)

        q.waitFinish()
        await.atMost(4000, TimeUnit.MILLISECONDS).until{
            true
        }

        assert(imageUi1.image!=null)
        assert(imageUi2.image!=null)
        assert(imageUi1.image==imageUi2.image)

    }


    class ImageUi {
        var image: ImageTest? = null

    }

    class ImageTest {
        var data: ByteArray? = null
        var animated = false
        var type = ""
    }

    class ImagePlatformHelperTest : ImagePlatformHelperBase() {

        init {
            backendImages = mutableListOf(
                BackendImageTest(JpegTstDecoder(), null),
                BackendImageTest(PngTstDecoder(), null)
            )
        }


        override fun assignImage(img: Any?, ui: Any) {
            if (ui is ImageUi && img is ImageTest) {
                ui.image = img
            } else {
            }
        }
    }


    class BackendImageTest() : BackendImage() {
        constructor(decoder: FileDecoder?, encoder: FileEncoder?) : this() {
            fileDecoder = decoder
            fileEncoder = encoder
        }

        init {
            srcImageClass = ImageTest::class.java
        }

        override fun getMemoryUsageOf(obj: Any): Int {
            return (obj as ImageTest).data?.size ?: 0
        }

//        override fun assignTo(view: Any?) {
//            if (ui is ImageUi && img is ImageTest) {
//                ui.image = img
//            } else {
//            }
//        }
    }


    class JpegTstDecoder :
        BinaryFileDecoder(
            "image/jpeg",
            arrayOf("jpeg", "jpg"),
            ImageTest::class,
            ImageLoadEP::class
        ) {


        override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {

            val ret = ImageTest()
            ret.data = super.onDecoding(inStream, dst, extraParam) as ByteArray
            ret.type = "jpg"
            if (extraParam is ImageLoadEP) {

            }

            return ret
        }


    }


    class PngTstDecoder :
        BinaryFileDecoder("image/png", arrayOf("png"), ImageTest::class, ImageLoadEP::class) {


        override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {


            val data = super.onDecoding(inStream, dst, extraParam)
            val ret = ImageTest()
            ret.data = data as ByteArray
            ret.type = "png"
            if (extraParam is ImageLoadEP) {

            }

            return ret
        }


    }


}
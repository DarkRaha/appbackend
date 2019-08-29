package com.darkraha.backend.components.images

import com.darkraha.backend.*
import com.darkraha.backend.components.endecode.BinaryFileDecoder
import com.darkraha.backend.extraparams.ImageLoadEP
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.InputStream

class ImageManagerDefaultTest {

    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    val imageManager = ImageManagerClientDefault.newInstance()
    var isSuccess = false


    @Test
    fun testLoadFile() {
        imageManager.attachImagePlatformHelper(ImagePlatformHelperTest())
        assertTrue(imageManager.endecoder.getDecodersList().size == 2)

        imageManager.getDiskcache().buildClear().exeSync()


        val dirImages = imageManager.getDiskcache().getWorkDir()
        assertTrue(dirImages.isDirectory)
        assertTrue(dirImages.listFiles().size == 0)


        val url = "https://pm1.narvii.com/6652/96cfbc896f4f277f98f09d049bd835baed62a0bf_hq.jpg"
        val imageUi = ImageUi()


        val cb = object : QueryCallback {


            override fun onSuccess(query: UserQuery) {
                isSuccess = true
            }

            override fun onError(query: UserQuery) {
                println(query.errorMessage())
                query.errorException()?.printStackTrace()
            }
        }


        imageManager.buildLoad(url, imageUi, cb, null, null).exeAsync()
        imageManager.queryManager.waitEmpty()

        assertTrue(isSuccess)
        isSuccess = false

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
                isSuccess = true
                assertTrue(imageUi.image != null)
                assertTrue(imageUi.image?.data != null ?: false)
                assertTrue(imageUi.image?.type == "jpg")
            }

            override fun onError(query: UserQuery) {
                println(query.errorMessage())
                query.errorException()?.printStackTrace()
            }
        }
        val s = imageManager.buildDecode(url, imageManager.imageFile(url), imageUi).exeSync()


        assertTrue(imageUi.image != null)
        assertTrue(imageUi.image?.data != null ?: false)
        assertTrue(imageUi.image?.type == "jpg")



        imageManager.getDiskcache().buildClear().exeSync()
        imageManager.getDiskcache().getWorkDir().delete()


    }


    class ImageUi {
        var image: ImageTest? = null

    }

    class ImageTest {
        var data: ByteArray? = null
        var animated = false
        var type = ""
    }

    class ImagePlatformHelperTest : ImagePlatformHelper {
        override fun onAttach(imageManager: ImageManagerClient) {
            imageManager.addImageDecoder(JpegTstDecoder())
            imageManager.addImageDecoder(PngTstDecoder())
            imageManager.addImageSizeCalculator(ImageTest::class) {
                (it as ImageTest)?.data?.size ?: 0
            }
        }

        override fun assignImage(img: Any?, ui: Any) {
println("test assign 1")
            if (ui is ImageUi && img is ImageTest) {
                println("test assign 2 "+img)
                ui.image = img
            }else{
                println("ui "+ui.javaClass+ " img "+img?.javaClass)
            }
        }

        override fun startAnimation(ui: Any?) {
            if (ui is ImageUi) {
                ui.image?.animated = true
            }
        }

        override fun stopAnimation(ui: Any?) {
            if (ui is ImageUi) {
                ui.image?.animated = false
            }
        }
    }


    class JpegTstDecoder :
        BinaryFileDecoder("image/jpeg", arrayOf("jpeg", "jpg"), ImageTest::class, ImageLoadEP::class) {


        override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {

            val ret = ImageTest()
            ret.data = super.onDecoding(inStream, dst, extraParam) as ByteArray
            ret.type = "jpg"
            if (extraParam is ImageLoadEP) {

            }

            return ret
        }


    }


    class PngTstDecoder : BinaryFileDecoder("image/png", arrayOf("png"), ImageTest::class, ImageLoadEP::class) {


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
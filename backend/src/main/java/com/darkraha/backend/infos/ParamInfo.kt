package com.darkraha.backend.infos

import com.darkraha.backend.ChainType
import com.darkraha.backend.helpers.SimpleUrlBuilder
import java.io.File
import java.lang.ref.WeakReference


/**
 * Common parameters name.
 *
 * @author Verma Rahul
 */
object Param {

    val PARAM_KEY_URL = "paramKeyUrl"
    val PARAM_FILE_SOURCE = "paramFileSource"
    val PARAM_FILE_DST = " paramFileDestination"

    // for disk caches
    val PARAM_TO_SIZE = "paramToSize"
    val PARAM_OLD_TIME_MIN = "paramOldTimeMin"
    val PARAM_OLD_TIME_MAX = "paramOldTimeMax"
    val PARAM_SUFFIX = "paramSuffix"

}


interface ParamReader {

    /**
     * url without named parameters, if it's exist
     */
    fun url(): String?

    /**
     * List of unnamed parameters
     */
    fun params(): List<Any>

    /**
     * Get named paramters that can be used as part of url.
     */
    fun namedParams(): Map<String, String>

    /**
     * Get special parameters.
     */
    fun specialParams(): Map<Any, Any?>

    fun source(): DataInfo

    fun destination(): DataInfo


    fun isOptionAutoCloseSource(): Boolean

    fun isOptionAutoCloseDestination(): Boolean

    fun extraParam(): Any?

    fun uiParam(): Any?

    fun objectParam(): Any?

    fun fileSource(): File? {
        when {
            source().value is File -> return source().value as File

            specialParams().get(Param.PARAM_FILE_SOURCE) is File ->
                return specialParams().get(Param.PARAM_FILE_SOURCE) as File

            objectParam() is File -> return objectParam() as File
            else -> return null
        }
    }

    fun fileDestination(): File? {
        when {
            destination().value is File -> return destination().value as File

            specialParams().get(Param.PARAM_FILE_DST) is File ->
                return specialParams().get(Param.PARAM_FILE_DST) as File

            objectParam() is File -> return objectParam() as File

            else -> return null
        }
    }

    fun chainTypeCreate(): ChainType

}


/**
 * @author Verma Rahul
 */
class ParamInfo : ParamReader {

    val urlBuilder = SimpleUrlBuilder()

    /**
     * List of unnamed paramters of query.
     */
    val param = mutableListOf<Any>()
    /**
     * Named parameters for url and etc.
     */
    val namedParams = mutableMapOf<String, String>()

    /**
     * Special named parameters that can be used by service.
     */
    val namedSpecialParams = mutableMapOf<Any, Any?>()

    val source = DataInfo()

    val destination = DataInfo()

    /**
     * Ui object related with query.
     */
    var uiObject: Any? = null
    var extraParam: Any? = null
    var objectParam: Any? = null

    var comment: String? = null
    var options = 0
    var chainTypeCreate: ChainType = ChainType.STAND_ALONE


    fun autoCloseSource() {
        options = options.or(OPT_AUTOCLOSE_SOURCE)
    }

    fun autoCloseDestination() {
        options = options.or(OPT_AUTOCLOSE_DESTINATION)
    }

    fun assignFrom(src: ParamInfo) {
        urlBuilder.assignFrom(src.urlBuilder)
        param.addAll(src.param)
        namedParams.putAll(src.namedParams)
        namedSpecialParams.putAll(src.namedSpecialParams)
        source.set(src.source)
        destination.set(src.destination)
        uiObject = src.uiObject
        extraParam = src.extraParam
        objectParam = src.objectParam
        options = src.options
        comment = src.comment
        chainTypeCreate = src.chainTypeCreate
    }

    fun clear() {
        urlBuilder.clear()
        param.clear()
        namedParams.clear()
        namedSpecialParams.clear()
        chainTypeCreate = ChainType.STAND_ALONE
        source.clear()
        destination.clear()
        uiObject = null
        extraParam = null
        objectParam = null
        comment = null
        options = 0
    }


    //----------------------------------------------------------------
    override fun destination(): DataInfo = destination

    override fun isOptionAutoCloseDestination() = options.and(OPT_AUTOCLOSE_DESTINATION) > 0
    override fun source(): DataInfo = source
    override fun isOptionAutoCloseSource(): Boolean = options.and(OPT_AUTOCLOSE_SOURCE) > 0
    override fun extraParam(): Any? = extraParam
    override fun namedParams(): Map<String, String> = namedParams
    override fun specialParams(): Map<Any, Any?> = namedSpecialParams
    override fun params(): List<Any> = params()
    override fun url(): String? = urlBuilder.url
    override fun chainTypeCreate(): ChainType = chainTypeCreate

    override fun uiParam(): Any? {
        if (uiObject != null) {
            if (uiObject is WeakReference<*>) {
                return (uiObject as WeakReference<*>).get()
            } else {
                return uiObject
            }
        }
        return null
    }


    override fun objectParam(): Any? {
        if (objectParam != null) {
            if (objectParam is WeakReference<*>) {
                return (objectParam as WeakReference<*>).get()
            } else {
                return objectParam
            }
        }
        return null
    }

    //------------------------------------------------------------------


    companion object {
        @JvmStatic
        val OPT_AUTOCLOSE_SOURCE = 1
        @JvmStatic
        val OPT_AUTOCLOSE_DESTINATION = 2


    }


}

package com.darkraha.backend.infos

import java.io.File

/**
 *
 */
class EndecodeItem(
    src: DataInfo,
    dst: List<DataInfo>,
    fileExtensionsSrc: Array<String>? = null,
    fileExtensionsDst: Array<String>? = null
) {

    val source: DataInfo = src;
    val destination: List<DataInfo> = dst

    /**
     * Possible file srcFileExtensions for file source.
     */
    val extSource: Array<String>? = fileExtensionsSrc

    /**
     * Possible file srcFileExtensions for file destination.
     */
    val extDestination: Array<String>? = fileExtensionsDst

    fun isSupportSource(src: DataInfo): Boolean {
        val value = src.value

        if (value != null) {
            if (value is File) {
                return isSupportFile(value, extSource)
            } else if (source.cls?.isInstance(value) ?: false) {
                return true
            }
        }

        return source.cls == src.cls || isMatchMimetype(source.mimetype, src.mimetype)
    }




    protected fun isMatchMimetype(mainmt: String?, matchmt: String?): Boolean {
        if (mainmt != null && matchmt != null) {
            if (mainmt.endsWith("/*")) {
                return matchmt.startsWith(mainmt.substringBefore("/"))
            } else return mainmt == matchmt
        }
        return false
    }


    /**
     * @param fileExtensions if null, accept all files
     */
    protected fun isSupportFile(file: File, fileExtensions: Array<String>?): Boolean {
        if (fileExtensions != null) {
            fileExtensions?.forEach { if (it == file.extension) return true }
            return false
        }

        return true
    }


}
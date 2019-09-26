package com.darkraha.backend.infos

import java.io.File
import kotlin.reflect.KClass

/**
 * @author Verma Rahul
 */
class DataInfo(value: Any? = null, mimetype: String? = null, cls: KClass<*>? = null, clsItem: KClass<*>? = null) {

    constructor(src: DataInfo?) : this(src?.value, src?.mimetype, src?.cls, src?.clsItem)

    var value: Any? = value
        internal set

    var cls: KClass<*>? = cls
        internal set

    var clsItem: KClass<*>? = clsItem
        internal set

    var mimetype: String? = mimetype
        internal set

    fun clear() {
        value = null
        cls = null
        clsItem = null
        mimetype = null
    }

    fun set(src: DataInfo) {
        value = src.value
        cls = src.cls
        clsItem = src.clsItem
        mimetype = src.mimetype
    }

    override fun toString(): String {
        return "DataInfo{value=${value} cls=${cls} clsItem=${clsItem} mimetype=${mimetype}}"
    }

    fun isSupportSource(v: DataInfo, extensions: Array<String>?) {
        val value = v.value
        var result = true

        if (value is File) {

            val ext = value.extension.toLowerCase();
            extensions?.forEach {
                if (it == ext) {

                }
            }

        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataInfo

        if (cls != other.cls) return false
        if (clsItem != other.clsItem) return false
        if (mimetype != other.mimetype) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cls?.hashCode() ?: 0
        result = 31 * result + (clsItem?.hashCode() ?: 0)
        result = 31 * result + (mimetype?.hashCode() ?: 0)
        return result
    }

}
package com.darkraha.backend.components.json

import java.lang.reflect.Type

interface JsonManager {
    fun <T> fromJsonString(src: String, clsDst: Class<T>): T

    /**
     * @param src
     * @param clsDst
     * @param <SRC> some json object, depend from used library
     * @param <T>
     * @return
    </T></SRC> */
    fun <SRC : Any, T > fromJson(src: SRC, clsDst: Class<T>): T

    fun toJsonString(obj: Any): String

    fun addTypeAdapter(type: Type, typeAdapter: Any)
}
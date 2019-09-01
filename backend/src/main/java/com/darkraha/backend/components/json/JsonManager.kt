package com.darkraha.backend.components.json

import java.lang.reflect.Type
import kotlin.reflect.KClass

interface JsonManager {
    fun <T : Any> fromJson(src: String, clsDst: KClass<T>): T

    /**
     * @param src
     * @param clsDst
     * @param <SRC>  depend from used library
     * @param <T>
     * @return
    </T></SRC> */
    fun <SRC : Any, T : Any> fromJson(src: SRC, clsDst: KClass<T>): T

    fun toJson(obj: Any): String

    fun addTypeAdapter(type: Type, typeAdapter: Any)
}




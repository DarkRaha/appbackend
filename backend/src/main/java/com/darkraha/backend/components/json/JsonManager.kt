package com.darkraha.backend.components.json

import java.lang.reflect.Type

interface JsonManager {
     fun <T> fromJson(src: String, clsDst: Class<T>): T

    /**
     * @param src
     * @param clsDst
     * @param <SRC>  depend from used library
     * @param <T>
     * @return
    </T></SRC> */
     fun <SRC:Any, T> fromJson(src: SRC, clsDst: Class<T>): T

     fun toJson(obj: Any): String

     fun addTypeAdapter(type: Type, typeAdapter: Any)
}
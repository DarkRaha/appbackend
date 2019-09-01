package com.darkraha.backend.components.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import java.lang.reflect.Type
import java.util.HashMap
import kotlin.reflect.KClass

class JsonManagerDefault : JsonManager {

    protected lateinit var gson: Gson
    protected var adapters: HashMap<Type, Any>? = HashMap()
    protected var applied = false

    override fun <T: Any> fromJson(src: String, clsDst: KClass<T>): T {
        apply()
        return gson!!.fromJson(src, clsDst.java)
    }


    override fun <SRC:Any, T: Any> fromJson(src: SRC, clsDst: KClass<T>): T {
        apply()


        return when {
            src == null -> throw IllegalArgumentException("src can not be null")
            src is JsonElement -> gson.fromJson(src as JsonElement, clsDst.java)
            src is JsonReader -> gson.fromJson(src as JsonReader, clsDst.java)
            else -> {
                throw IllegalArgumentException("Class " + src::class + " not supported by JsonManager")
            }
        }
    }

    override fun toJson(obj: Any): String {
        apply()
        return gson.toJson(obj)
    }

    override fun addTypeAdapter(type: Type, typeAdapter: Any) {
        adapters!![type] = typeAdapter
        applied = false
    }


    /**
     * Apply changing.
     */
    protected fun apply() {
        if (!applied || gson == null) {
            if (adapters != null && adapters!!.size > 0) {
                val builder = GsonBuilder()

                for ((key, value) in adapters!!) {
                    builder.registerTypeAdapter(key, value)
                }

                gson = builder.create()
            } else {
                gson = Gson()
            }

            applied = true
        }
    }
}

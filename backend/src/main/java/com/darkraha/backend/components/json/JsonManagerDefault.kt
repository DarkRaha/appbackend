package com.darkraha.backend.components.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import java.lang.reflect.Type
import java.util.HashMap

class JsonManagerDefault : JsonManager {

    protected lateinit var gson: Gson
    protected var adapters: HashMap<Type, Any>? = HashMap()
    protected var applied = false

    override fun <T> fromJsonString(src: String, clsDst: Class<T>): T {
        apply()
        return gson!!.fromJson(src, clsDst)
    }


    override fun <SRC : Any, T> fromJson(src: SRC, clsDst: Class<T>): T {
        apply()

        return when {
            src == null -> throw IllegalArgumentException("src can not be null")
            src is JsonElement -> gson.fromJson(src as JsonElement, clsDst)
            src is JsonReader -> gson.fromJson(src as JsonReader, clsDst)
            else -> {
                throw IllegalArgumentException("Class " + src::class + " not supported by JsonManager")
            }
        }
    }

    override fun toJsonString(obj: Any): String {
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

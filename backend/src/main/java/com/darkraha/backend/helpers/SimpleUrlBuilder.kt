package com.darkraha.backend.helpers

class SimpleUrlBuilder {


    val addpathes = mutableListOf<String>()
    val replacement = mutableMapOf<String, String>()
    var url: String? = null
    var base: String? = null


    inline fun addReplacement(key: String, value: String) {
        replacement.put(if (key[0] == '{') key else "{" + key + "}", value)
    }



    fun assignFrom(src: SimpleUrlBuilder) {
        addpathes.addAll(src.addpathes)
        replacement.putAll(src.replacement)
        url = src.url
        base = src.base
    }

    fun build() {
        if (base != null) {
            val sb = StringBuilder()
            sb.append(base)


            replacement.forEach {
                val indexOf = sb.indexOf(it.key)

                if (indexOf >= 0) {
                    sb.replace(indexOf, it.key.length, it.value)
                }
            }

            addpathes.forEach {
                sb.append(it)
            }

            url = sb.toString()
        }
    }

    fun clear() {
        url = null;
        base = null
        replacement.clear()
        addpathes.clear()
    }

}
package com.darkraha.backend.components.images

object ImageManagerConsts {

    /**
     * Command for downloading image file and save it in disk cache.
     */
    val CMD_DOWNLOAD = "cmd:im.download"

    /**
     * First check memory cache, then disk cache, then download image file.
     * Save image file in disk cache, in memory cache, assign from image to ui object.
     */
    val CMD_LOAD = "cmd:im.load"

    /**
     * Like CMD_LOAD, but don't download file.
     */
    val CMD_LOAD_FROM_DISKCACHE = "cmd:im.load_from_diskcache"

    /**
     * Command for loading image only from memory.
     */
    val CMD_LOAD_FROM_MEMORY = "cmd:im.load_from_memory"

    val CMD_GENERATE_THUMBS = "cmd:im.gen_thumbs"

}
package com.darkraha.backend.components.diskcache

object DiskCacheConsts {

    /**
     * Command for putting file to disk cache.
     */
    val CMD_PUT = "cmd:dc.put"

    /**
     * Command for retrieve file from disk cache.
     */
    val CMD_GET = "cmd:dc.get"

    /**
     * Command for delete all data from disk cache.
     */
    val CMD_CLEAR = "cmd:dc.clear"
    /**
     * Command for deleting exceeded data id disk cache.
     */
    val CMD_CLEANUP = "cmd:dc.cleanup"
    /**
     * Command for generating file name from key(like url).
     */
    val CMD_GEN_NAME = "cmd:dc.gen_name"
    /**
     * Command for generating file from key(like url).
     */
    val CMD_GEN_FILE = "cmd:dc.gen_file"


}
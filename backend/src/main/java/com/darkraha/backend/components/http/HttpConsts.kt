package com.darkraha.backend.components.http

object HttpConsts {
    /**
     * Command for loading text.
     */
    val CMD_LOAD_TEXT = "cmd:http.load_text"
    /**
     * Command for loading file.
     */
    val CMD_LOAD_FILE = "cmd:http.load_file"
    /**
     * Command for checking is link valid.
     */
    val CMD_CHECK_LINK = "cmd:http.check_link"
    /**
     * Command for posting web form.
     */
    val CMD_POST_FORM = "cmd:http.post_form"
    /**
     * Command for uploading one single file.
     */
    val CMD_UPLOAD_FILE = "cmd:http.upload_file"

    inline fun listOfCommands() = listOf(CMD_LOAD_TEXT, CMD_LOAD_FILE, CMD_CHECK_LINK, CMD_POST_FORM, CMD_UPLOAD_FILE)

    inline fun listOfSchemes() = listOf("http", "https")



}
package com.darkraha.backend.infos

class CancelInfo {
    var code = 0
        private set
    var message: String? = null
        private set


    fun set(argCode: Int, argMsg: String?) {
        code = argCode
        message = argMsg
    }


    fun clear() {
        code = 0
        message = null
    }


    companion object {

        @JvmStatic
        val CANCEL_BY_USER = 1

        /**
         * Query unnecessary for user.
         */
        @JvmStatic
        val CANCEL_UNNECESSARY = 2

        /**
         * Query was canceled, because it was appended to other query.
         */
        @JvmStatic
        val CANCEL_APPEND = 3

        /**
         * Query was canceled, because service not available.
         */
        @JvmStatic
        val CANCEL_SRV_NOT_AVAILABLE = 4

        /**
         * Query canceled by client. As example new query appears with more priority or need merge.
         */
        @JvmStatic
        val CANCEL_BY_CLIENT = 5


    }

}
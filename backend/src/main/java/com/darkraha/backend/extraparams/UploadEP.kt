package com.darkraha.backend.extraparams

import java.io.File

/**
 * @author Verma Rahul
 */
class UploadEP(name: String?) {
    /**
     *
     */
    var name: String? = name

    var file: File? = null

    var bytes: ByteArray? = null

    var mimetype: String? = "image/jpeg"

    var srvFilename: String? = null


    fun srvFilename(v: String?): UploadEP {
        srvFilename = v
        return this
    }

    fun mimetype(v: String?): UploadEP {
        mimetype = v
        return this
    }

    fun bytes(v: ByteArray?): UploadEP {
        bytes = v
        return this
    }

    fun name(v: String): UploadEP {
        name = v
        return this
    }

    fun file(f: File?): UploadEP {
        file = f
        if(srvFilename==null){
            srvFilename=file?.name
        }
        return this
    }


}


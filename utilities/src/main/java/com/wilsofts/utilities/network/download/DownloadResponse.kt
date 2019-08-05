package com.wilsofts.utilities.network.download

interface DownloadResponse {
    fun progress(progress: Long, fileSize: Long)

    fun response(code: Int)

    fun error(throwable: Throwable)
}

package com.wilsofts.utilities.network.progressClient

import com.wilsofts.utilities.LibUtils
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

class ProgressUpdater {

    class ProgressResponseBody internal constructor(private val responseBody: ResponseBody, private val progressListener: ProgressListener)
        : ResponseBody() {
        private var bufferedSource: BufferedSource? = null

        override fun contentType(): MediaType? {
            return this.responseBody.contentType()
        }

        override fun contentLength(): Long {
            LibUtils.logE("Content length = " + this.responseBody.contentLength())
            return this.responseBody.contentLength()
        }

        override fun source(): BufferedSource {
            if (this.bufferedSource == null) {
                this.bufferedSource = Okio.buffer(this.source(this.responseBody.source()))
            }
            return this.bufferedSource!!
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L

                @Throws(IOException::class)
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    this.totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                    this@ProgressResponseBody.progressListener.update(this.totalBytesRead,
                            this@ProgressResponseBody.responseBody.contentLength(), bytesRead == -1L)
                    return bytesRead
                }
            }
        }
    }

    interface ProgressListener {
        fun update(bytesRead: Long, contentLength: Long, done: Boolean)
    }
}

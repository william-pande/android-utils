package com.wilsofts.utilities.network.download

import android.os.AsyncTask
import com.wilsofts.utilities.LibUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class RetrofitDownloader(call: Call<ResponseBody>, private val downloadResponse: DownloadResponse, private val target_path: File)
    : AsyncTask<ResponseBody, RetrofitDownloader.Progress, String>() {

    constructor(call: Call<ResponseBody>, target_path: File, downloadResponse: DownloadResponse) : this(call, downloadResponse, target_path)

    init {
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    this@RetrofitDownloader.execute(response.body())
                } else {
                    this@RetrofitDownloader.downloadResponse.response(response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                this@RetrofitDownloader.downloadResponse.error(throwable)
            }
        })
    }

    override fun doInBackground(vararg urls: ResponseBody): String? {
        //Copy you logic to calculate progress and call
        this.saveToDisk(urls[0])
        return null
    }

    override fun onProgressUpdate(vararg values: Progress?) {
        super.onProgressUpdate(*values)
        this.downloadResponse.progress(values[0]!!.progress, values[0]!!.file_size)
    }

    override fun onPostExecute(result: String) {

    }

    private fun saveToDisk(body: ResponseBody) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            inputStream = body.byteStream()
            outputStream = FileOutputStream(this.target_path)
            val data = ByteArray(4096)
            var count: Int = inputStream!!.read(data)
            var progress: Long = 0
            val fileSize = body.contentLength()

            while ((count) != -1) {
                outputStream.write(data, 0, count)
                progress += count.toLong()
                this.publishProgress(Progress(progress, fileSize))
                count = inputStream.read(data)
            }

            outputStream.flush()
            this.publishProgress(Progress(fileSize, fileSize))

        } catch (error: IOException) {
            this.publishProgress(Progress(-1L, -1L))
            this.downloadResponse.error(error)

        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (error: IOException) {
                LibUtils.logE(error)
            }
        }
    }

    class Progress(val progress: Long, val file_size: Long)
}

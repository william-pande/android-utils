package com.wilsofts.utilities.network.request

import android.content.Intent
import android.util.Log
import com.google.gson.GsonBuilder
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import com.wilsofts.utilities.LibUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

@Suppress("unused", "MemberVisibilityCanBePrivate")
class RetrofitClient(call: Call<String>, serverResponse: ServerResponse? = null) {

    init {
        ResponseManager(call = call, serverResponse = serverResponse)
    }

    companion object {
        fun getRetrofit(headers: Intent, url: String, listener: ProgressListener? = null): Retrofit {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val builder = OkHttpClient.Builder()
                    .connectTimeout(LibUtils.CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .writeTimeout(LibUtils.WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .readTimeout(LibUtils.READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .addInterceptor { chain: Interceptor.Chain ->
                        val builder = chain.request().newBuilder()

                        val bundle = headers.extras
                        if (bundle != null) {
                            for (name in bundle.keySet()) {
                                builder.addHeader(name, bundle.getString(name)!!)
                            }
                        }
                        chain.proceed(builder.build())
                    }

            if (listener != null) {
                builder.addNetworkInterceptor { chain ->
                    val originalResponse = chain.proceed(chain.request())
                    originalResponse.newBuilder()
                            .body(ProgressUpdater.ProgressResponseBody(originalResponse.body!!, listener))
                            .build()
                }
            }

            if (LibUtils.SHOW_LOG) {
                builder.addInterceptor(OkHttpProfilerInterceptor())
            }

            val client = builder.build()
            return Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build()
        }

        /*retrofit with progress listener*/
        fun getRetrofit(headers: Intent, listener: ProgressListener): Retrofit {
            return getRetrofit(headers, LibUtils.URL_LINK, listener)
        }

        fun getRetrofit(url: String, listener: ProgressListener): Retrofit {
            return getRetrofit(Intent(), url, listener)
        }

        /*retrofit without progress dialog*/
        fun getRetrofit(headers: Intent): Retrofit {
            return getRetrofit(headers = headers, url = LibUtils.URL_LINK)
        }

        fun getRetrofit(url: String): Retrofit {
            return getRetrofit(headers = Intent(), url = url)
        }

        val retrofit: Retrofit
            get() = getRetrofit(headers = Intent(), url = LibUtils.URL_LINK)

        fun getBody(parameters: Map<String, Any>): RequestBody {
            return JSONObject(parameters).toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }

        fun logE(s: String) {
            if (LibUtils.NET_LOG) {
                Log.e("Network", s)
            }
        }

        fun logE(throwable: Throwable) {
            if (LibUtils.NET_LOG) {
                Log.e("Network", throwable.message, throwable)
            }
            throwable.printStackTrace()
        }
    }

    class ProgressUpdater {
        class ProgressResponseBody(private val responseBody: ResponseBody, private val progressListener: ProgressListener) : ResponseBody() {
            private var bufferedSource: BufferedSource? = null

            override fun contentType(): MediaType? {
                return this.responseBody.contentType()
            }

            override fun contentLength(): Long {
                //val size = responseBody.bytes().size.toLong()
                val size = this.responseBody.contentLength()
                //logE(responseBody.toString())
                logE("Size = $size")
                //logE("${responseBody.toString().length}")
                return size
            }

            override fun source(): BufferedSource {
                if (this.bufferedSource == null) {
                    this.bufferedSource = this.source(this.responseBody.source()).buffer()
                }
                return this.bufferedSource!!
            }

            private fun source(source: Source): Source {
                return object : ForwardingSource(source) {
                    var totalBytesRead = 0L

                    override fun read(sink: Buffer, byteCount: Long): Long {
                        val bytesRead = super.read(sink, byteCount)
                        val contentLength = this@ProgressResponseBody.responseBody.contentLength()

                        // read() returns the number of bytes read, or -1 if this source is exhausted.
                        this.totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                        var percentage = 0.0
                        if (contentLength != -1L) {
                            percentage = (DecimalFormat("#.##").format((100 * bytesRead) / contentLength)).toDouble()
                        }

                        this@ProgressResponseBody.progressListener.update(
                                bytesRead = this.totalBytesRead,
                                contentLength = contentLength,
                                done = bytesRead == -1L,
                                percentage = percentage
                        )
                        return bytesRead
                    }
                }
            }
        }
    }

    class RetroProgress {

    }

    interface ServerResponse {
        fun response(status: Int, response: JSONObject, throwable: Throwable?)
    }

    interface ProgressListener {
        fun update(bytesRead: Long, contentLength: Long, done: Boolean, percentage: Double)
    }
}
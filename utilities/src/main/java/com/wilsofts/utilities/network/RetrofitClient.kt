package com.wilsofts.utilities.network

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.google.gson.GsonBuilder
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor
import com.wilsofts.utilities.LibUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class RetrofitClient(private val activity: FragmentActivity?, private val call: Call<String>, title: String) {
    private var show_progress: Boolean = false
    private var dialog: DialogProgress? = null

    init {
        this.show_progress = true
        if (this.activity != null) {
            this.dialog = DialogProgress.newInstance(title)
        }
    }

    fun initProgress(): RetrofitClient {
        this.show_progress = false
        return this
    }

    private fun hideDialog() {
        if (this.activity != null && this.dialog!!.isVisible) {
            DialogProgress.hideProgress(this.activity)
        }
    }

    fun initRequest(networkResponse: NetworkResponse) {
        if (this.activity != null && LibUtils.noInternetConnection(this.activity)) {
            networkResponse.error(true, null)
            return
        }

        if (this.show_progress && this.activity != null && this.dialog != null) {
            DialogProgress.showDialog(this.activity, this.dialog!!)
        }

        this.call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                try {
                    if (response.isSuccessful) {
                        this@RetrofitClient.hideDialog()
                        LibUtils.logE("code = " + response.code() + " and message = " + response.body() + " ")
                        networkResponse.success(response.code(), response.body()!!)

                    } else {
                        val error_body = response.errorBody()
                        if (error_body != null) {
                            val stream = error_body.byteStream()
                            val reader = BufferedReader(InputStreamReader(stream))
                            val builder = StringBuilder()
                            var line: String? = reader.readLine()
                            while (line != null) {
                                builder.append(line).append("\n")
                                line = reader.readLine()
                            }
                            this@RetrofitClient.hideDialog()
                            LibUtils.logE("code = " + response.code() + " and message = " + builder.toString())
                            networkResponse.success(response.code(), builder.toString())

                        } else {
                            this@RetrofitClient.hideDialog()
                            networkResponse.success(response.code(), "")
                            LibUtils.logE("code = " + response.code())
                        }
                    }
                } catch (error: Exception) {
                    this@RetrofitClient.hideDialog()
                    LibUtils.logE(error)
                    networkResponse.error(false, null)
                    LibUtils.logE("code = " + response.code())
                }

            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                this@RetrofitClient.hideDialog()
                if (throwable is SocketTimeoutException) {
                    networkResponse.error(true, throwable)
                } else {
                    networkResponse.error(false, throwable)
                }
            }
        })
    }

    companion object {
        fun getRetrofitInstance(headers: Intent, url: String): Retrofit {
            LibUtils.logE(url)
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
            if (LibUtils.SHOW_LOG) {
                builder.addInterceptor(OkHttpProfilerInterceptor() )
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

        fun getRetrofitInstance(headers: Intent): Retrofit {
            return getRetrofitInstance(headers, LibUtils.URL_LINK)
        }

        fun getRetrofitInstance(url: String): Retrofit {
            return getRetrofitInstance(Intent(), url)
        }

        val retrofitInstance: Retrofit
            get() = getRetrofitInstance(Intent(), LibUtils.URL_LINK)

        fun getBody(parameters: Map<String, Any>): RequestBody {
            return RequestBody
                    .create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                            JSONObject(parameters).toString())
        }
    }
}

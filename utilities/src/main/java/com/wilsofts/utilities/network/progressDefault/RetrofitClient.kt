package com.wilsofts.utilities.network.progressDefault

import android.content.Intent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.gson.GsonBuilder
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.network.misc.Response
import com.wilsofts.utilities.network.misc.ServerResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate", "unused")
class RetrofitClient(
        private val call: Call<String>,
        title: String = "",
        private val activity: FragmentActivity? = null,
        dialog: DialogFragment? = null,
        private val server_response: ServerResponse? = null,
        private val response: Response? = null) {

    private var dialog: DialogFragment

    init {
        if (dialog == null) {
            this.dialog = DialogProgress.newInstance(title)
        } else {
            this.dialog = dialog
        }
        this.process()
    }

    private fun process() {
        if (this.activity != null) {
            this.dialog.show(activity.supportFragmentManager, "network_dialog")
        }

        fun hide_dialog() {
            if (this.activity != null && this.dialog.isVisible) {
                this.dialog.dismiss()
            }
        }

        this.call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
                LibUtils.logE("${response.code()}", "Response")
                hide_dialog()

                try {
                    if (response.isSuccessful) {
                        LibUtils.logE(response.body() ?: "Response must not be null, but otherwise is gotten", "Response")
                        val json = JSONObject(response.body()!!)
                        LibUtils.logE(json.toString(2), "Response")
                        this@RetrofitClient.server_response?.success(status = response.code(), response = json)
                        this@RetrofitClient.response?.response(status = response.code(), response = json)

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
                            val message = builder.toString()
                            val json = if (message.isNotEmpty()) JSONObject(message) else JSONObject()
                            LibUtils.logE(json.toString(2), "Response")
                            this@RetrofitClient.server_response?.success(status = response.code(), response = json)
                            this@RetrofitClient.response?.response(status = response.code(), response = json)

                        } else {
                            this@RetrofitClient.server_response?.success(status = response.code(), response = JSONObject())
                            this@RetrofitClient.response?.response(status = response.code(), response = JSONObject())
                        }
                    }
                } catch (error: Exception) {
                    LibUtils.logE(error, "Response")
                    this@RetrofitClient.server_response?.error(throwable = error, network = false)
                    this@RetrofitClient.response?.response(
                            status = response.code(), response = JSONObject().put("error", error.localizedMessage))
                }
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                hide_dialog()
                this@RetrofitClient.server_response?.error(throwable = throwable, network = throwable is IOException)
                this@RetrofitClient.response?.response(
                        status = 500,
                        response = JSONObject()
                                .put("error", throwable.localizedMessage)
                                .put("network", throwable is IOException)
                )
            }
        })
    }

    companion object {
        val retrofit: Retrofit
            get() = getRetrofit(Intent(), LibUtils.URL_LINK)

        fun getRetrofit(headers: Intent, url: String): Retrofit {
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

        fun getRetrofit(headers: Intent): Retrofit {
            return getRetrofit(headers, LibUtils.URL_LINK)
        }

        fun getRetrofit(url: String): Retrofit {
            return getRetrofit(Intent(), url)
        }

        fun getBody(parameters: Map<String, Any>): RequestBody {
            return JSONObject(parameters).toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
    }
}

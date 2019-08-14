package com.wilsofts.utilities.network

import android.content.Intent
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.gson.GsonBuilder
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.network.progressClient.ResponseManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(private val activity: FragmentActivity?, private val call: Call<String>, title: String) {
    private var show_progress: Boolean = false
    private var dialog: DialogProgress? = null

    init {
        this.show_progress = true
        if (this.activity != null) {
            this.dialog = DialogProgress.newInstance(title)

            dialog!!.progress_circular?.visibility = View.VISIBLE
            dialog!!.horizontal_progress?.visibility = View.GONE
            dialog!!.progress_text?.visibility = View.GONE
        }
    }

    fun initProgress(): RetrofitClient {
        this.show_progress = false
        return this
    }

    fun initRequest(networkResponse: NetworkResponse) {
        ResponseManager(call = call, networkResponse = networkResponse, dialog = dialog, activity = activity, show_progress = show_progress)
    }

    companion object {
        fun getRetrofit(headers: Intent, url: String): Retrofit {
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

        val retrofit: Retrofit
            get() = getRetrofit(Intent(), LibUtils.URL_LINK)

        fun getBody(parameters: Map<String, Any>): RequestBody {
            return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), JSONObject(parameters).toString())
        }
    }
}

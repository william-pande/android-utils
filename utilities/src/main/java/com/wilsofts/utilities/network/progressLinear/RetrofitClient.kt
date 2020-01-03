package com.wilsofts.utilities.network.progressLinear

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.google.gson.GsonBuilder
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.network.misc.NetworkResponse
import com.wilsofts.utilities.network.misc.ResponseManager
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

@Suppress("unused", "MemberVisibilityCanBePrivate")
class RetrofitClient(activity: FragmentActivity?, call: Call<String>, var dialog: DialogProgressLinear?, networkResponse: NetworkResponse) {

    init {
        ResponseManager(call = call, networkResponse = networkResponse, dialog = dialog, activity = activity, show_progress = true)
    }

    companion object {
        fun getRetrofit(headers: Intent, url: String, listener: ProgressUpdater.ProgressListener): Retrofit {
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
                    .addNetworkInterceptor { chain ->
                        val originalResponse = chain.proceed(chain.request())
                        originalResponse.newBuilder()
                                .body(ProgressUpdater.ProgressResponseBody(originalResponse.body()!!, listener))
                                .build()
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

        fun getRetrofit(headers: Intent, listener: ProgressUpdater.ProgressListener): Retrofit {
            return getRetrofit(headers, LibUtils.URL_LINK, listener)
        }

        fun getRetrofit(url: String, listener: ProgressUpdater.ProgressListener): Retrofit {
            return getRetrofit(Intent(), url, listener)
        }

        fun getBody(parameters: Map<String, Any>): RequestBody {
            return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), JSONObject(parameters).toString())
        }
    }
}
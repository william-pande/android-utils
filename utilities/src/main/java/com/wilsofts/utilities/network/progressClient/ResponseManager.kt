package com.wilsofts.utilities.network.progressClient

import androidx.fragment.app.FragmentActivity
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.network.DialogProgress
import com.wilsofts.utilities.network.NetworkResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.SocketTimeoutException

class ResponseManager(
        private val call: Call<String>,
        val networkResponse: NetworkResponse,
        val dialog: DialogProgress?,
        val activity: FragmentActivity?,
        val show_progress: Boolean) {
    init {
        this.response()
    }

    private fun response() {
        if (this.activity != null && LibUtils.noInternetConnection(this.activity)) {
            networkResponse.error(true, null)
            return
        }

        if (this.show_progress && this.activity != null && this.dialog != null) {
            DialogProgress.showDialog(this.activity, this.dialog)
        }

        this.call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                try {
                    if (response.isSuccessful) {
                        this@ResponseManager.hideDialog()
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
                            this@ResponseManager.hideDialog()
                            LibUtils.logE("code = " + response.code() + " and message = " + builder.toString())
                            networkResponse.success(response.code(), builder.toString())

                        } else {
                            this@ResponseManager.hideDialog()
                            networkResponse.success(response.code(), "")
                            LibUtils.logE("code = " + response.code())
                        }
                    }
                } catch (error: Exception) {
                    this@ResponseManager.hideDialog()
                    LibUtils.logE(error)
                    networkResponse.error(false, null)
                    LibUtils.logE("code = ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                this@ResponseManager.hideDialog()
                LibUtils.logE("Error = $throwable")
                if (throwable is SocketTimeoutException) {
                    networkResponse.error(true, throwable)
                } else {
                    networkResponse.error(false, throwable)
                }
            }
        })
    }

    fun hideDialog() {
        if (this.activity != null && this.dialog!!.isVisible) {
            DialogProgress.hideProgress(this.activity)
        }
    }
}
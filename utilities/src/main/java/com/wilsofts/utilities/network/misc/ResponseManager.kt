package com.wilsofts.utilities.network.misc

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.wilsofts.utilities.LibUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.SocketTimeoutException

class ResponseManager(private val call: Call<String>, val response: ServerResponse, val dialog: DialogFragment?,
                      val activity: FragmentActivity?, val show_progress: Boolean) {
    init {
        this.process()
    }

    private fun process() {
        if (this.show_progress && this.activity != null && this.dialog != null) {
            this.showDialog()
        }

        this.call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                LibUtils.logE("${response.code()}")
                this@ResponseManager.hideDialog()
                try {
                    if (response.isSuccessful) {
                        LibUtils.logE(JSONObject(response.body()!!).toString(2))
                        this@ResponseManager.response.success(status = response.code(), response = JSONObject(response.body()!!))
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
                            LibUtils.logE(json.toString(2))
                            this@ResponseManager.response.success(status = response.code(), response = json)
                        } else {
                            this@ResponseManager.response.success(status = response.code(), response = JSONObject())
                        }
                    }
                } catch (error: Exception) {
                    this@ResponseManager.hideDialog()
                    LibUtils.logE(error)
                    this@ResponseManager.response.error(                            throwable = error, network = false)
                }
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                this@ResponseManager.hideDialog()
                this@ResponseManager.response.error(throwable = throwable, network = throwable is IOException)
            }
        })
    }

    private fun showDialog() {
        if (dialog != null && activity != null) {
            val manager = activity.supportFragmentManager
            manager.beginTransaction()
                    .add(dialog, "dialog_fragment")
                    .commitAllowingStateLoss()
        }
    }

    private fun hideDialog() {
        if (this.activity != null && this.dialog!!.isVisible) {
            val manager = activity.supportFragmentManager
            val fragment = manager.findFragmentByTag("dialog_fragment")
            if (fragment != null) {
                manager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
        }
    }
}
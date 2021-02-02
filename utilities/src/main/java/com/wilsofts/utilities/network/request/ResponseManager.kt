package com.wilsofts.utilities.network.request

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class ResponseManager(private val call: Call<String>, val serverResponse: RetrofitClient.ServerResponse?) {
    init {
        this.process()
    }

    private fun process() {
        this.call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                try {
                    if (response.isSuccessful) {
                        this@ResponseManager.serverResponse?.response(status = response.code(), response = JSONObject(), throwable = null)

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
                            this@ResponseManager.serverResponse?.response(status = response.code(), response = json, throwable = null)
                        } else {
                            this@ResponseManager.serverResponse?.response(status = response.code(),
                                    response = JSONObject(), throwable = null)
                        }
                    }
                } catch (throwable: Exception) {
                    this@ResponseManager.serverResponse?.response(
                            status = response.code(), throwable = throwable,
                            response = JSONObject().put("error", throwable.localizedMessage)
                    )
                }
            }

            override fun onFailure(call: Call<String>, throwable: Throwable) {
                this@ResponseManager.serverResponse?.response(
                        status = 500, throwable = throwable,
                        response = JSONObject()
                                .put("error", throwable.localizedMessage)
                                .put("network", throwable is IOException)
                )
            }
        })
    }
}
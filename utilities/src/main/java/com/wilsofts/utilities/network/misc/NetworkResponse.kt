package com.wilsofts.utilities.network.misc

import org.json.JSONException

interface NetworkResponse {
    @Throws(JSONException::class)
    fun success(code: Int, message: String)

    fun error(timeout: Boolean, throwable: Throwable?)
}

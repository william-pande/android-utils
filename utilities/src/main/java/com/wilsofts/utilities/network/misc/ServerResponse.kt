package com.wilsofts.utilities.network.misc

import org.json.JSONException
import org.json.JSONObject

interface ServerResponse {
    @Throws(JSONException::class)
    fun send(status: Int, response: JSONObject, throwable: Throwable?, network: Boolean)
}

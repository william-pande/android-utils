package com.wilsofts.utilities.network.misc

import org.json.JSONException
import org.json.JSONObject

interface ServerResponse {
    @Throws(JSONException::class)
    fun success(status: Int, response: JSONObject)

    fun error(throwable: Throwable, network: Boolean)
}

interface Response{
    fun response(status: Int, response: JSONObject)
}

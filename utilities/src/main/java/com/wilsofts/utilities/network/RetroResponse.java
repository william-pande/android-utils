package com.wilsofts.utilities.network;

import org.json.JSONException;

public interface RetroResponse {
    void success(int code, String message) throws JSONException;
}

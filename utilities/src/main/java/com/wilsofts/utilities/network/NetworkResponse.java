package com.wilsofts.utilities.network;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

public interface NetworkResponse {
    void success(int code, String message) throws JSONException;

    void error(boolean timeout, @Nullable Throwable throwable);
}

package com.wilsofts.utilities.network;

import org.jetbrains.annotations.Nullable;

public interface NetworkResponse {
    void success(int code, String message);

    void error(boolean timeout, @Nullable Throwable throwable);
}

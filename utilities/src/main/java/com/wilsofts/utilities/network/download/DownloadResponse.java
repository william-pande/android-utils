package com.wilsofts.utilities.network.download;

public interface DownloadResponse {
    void progress(long progress, long fileSize);

    void response(int code);

    void error(Throwable throwable);
}

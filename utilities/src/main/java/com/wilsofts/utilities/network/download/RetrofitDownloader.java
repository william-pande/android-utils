package com.wilsofts.utilities.network.download;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitDownloader extends AsyncTask<ResponseBody, Long, String> {
    private final DownloadResponse downloadResponse;
    private final File target_path;

    public RetrofitDownloader(Call<ResponseBody> call, File target_path, DownloadResponse downloadResponse) {
        this(call, downloadResponse, target_path);
    }

    public RetrofitDownloader(Call<ResponseBody> call, DownloadResponse downloadResponse, File target_path) {
        this.downloadResponse = downloadResponse;
        this.target_path = target_path;

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    RetrofitDownloader.this.execute(response.body());
                } else {
                    RetrofitDownloader.this.downloadResponse.response(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                RetrofitDownloader.this.downloadResponse.error(throwable);
            }
        });
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(ResponseBody... urls) {
        //Copy you logic to calculate progress and call
        this.saveToDisk(urls[0]);
        return null;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        super.onProgressUpdate(progress);
        this.downloadResponse.progress(progress[0], progress[1]);
    }

    @Override
    protected void onPostExecute(String result) {

    }

    private void saveToDisk(ResponseBody body) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = body.byteStream();
            outputStream = new FileOutputStream(this.target_path);
            byte[] data = new byte[4096];
            int count;
            long progress = 0;
            long fileSize = body.contentLength();

            while ((count = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, count);
                progress += count;
                this.publishProgress(progress, fileSize);
            }

            outputStream.flush();
            this.publishProgress(fileSize, fileSize);

        } catch (IOException error) {
            this.publishProgress(-1L, -1L);
            this.downloadResponse.error(error);

        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }
    }
}

package com.wilsofts.utilities.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.GsonBuilder;
import com.wilsofts.utilities.LibUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private boolean show_progress;
    private final FragmentActivity activity;
    private final CoordinatorLayout coordinatorLayout;
    private DialogProgress dialog = null;
    private final Call<String> call;

    public RetrofitClient(@Nullable FragmentActivity activity, @Nullable CoordinatorLayout coordinatorLayout, @NonNull Call<String> call, String title) {
        this.show_progress = true;
        this.activity = activity;
        this.coordinatorLayout = coordinatorLayout;
        if (this.activity != null) {
            this.dialog = DialogProgress.newInstance(title);
        }
        this.call = call;
    }

    public RetrofitClient initProgress() {
        this.show_progress = false;
        return this;
    }

    private void hideDialog() {
        if (this.activity != null && this.dialog.isVisible()) {
            DialogProgress.hideProgress(this.activity);
        }
    }

    public void initRequest(RetroResponse retroResponse) {
        try {
            if (this.coordinatorLayout != null && this.activity != null && LibUtils.noInternetConnection(
                    this.activity, this.coordinatorLayout)) {
                retroResponse.success(10, "");
                return;
            }
        } catch (JSONException e) {
            LibUtils.logE(e);
            if (RetrofitClient.this.coordinatorLayout != null) {
                LibUtils.showError(RetrofitClient.this.coordinatorLayout, e.getMessage());
            }
        }

        if (this.show_progress && this.activity != null && this.dialog != null) {
            DialogProgress.showDialog(this.activity, this.dialog);
        }

        this.call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                try {
                    RetrofitClient.this.hideDialog();
                    LibUtils.logE(response.code() + " = " + response.body());
                    retroResponse.success(response.code(), response.body());
                } catch (JSONException e) {
                    LibUtils.logE(e);
                    if (RetrofitClient.this.coordinatorLayout != null) {
                        LibUtils.showError(RetrofitClient.this.coordinatorLayout, e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                try {
                    RetrofitClient.this.hideDialog();
                    if (throwable instanceof SocketTimeoutException) {
                        if (RetrofitClient.this.coordinatorLayout != null) {
                            LibUtils.showError(RetrofitClient.this.coordinatorLayout,
                                    "Network response error. Please check your network connectivity");
                        }
                        retroResponse.success(10, "");
                    } else {
                        retroResponse.success(0, "");
                    }
                } catch (JSONException e) {
                    LibUtils.logE(e);
                    if (RetrofitClient.this.coordinatorLayout != null) {
                        LibUtils.showError(RetrofitClient.this.coordinatorLayout, e.getMessage());
                    }
                }
            }
        });
    }

    public static Retrofit getRetrofitInstance() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(LibUtils.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(LibUtils.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(LibUtils.READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + LibUtils.AUTHORIZATION_BEARER).build();
                    return chain.proceed(request);
                }).build();

        return new Retrofit.Builder()
                .baseUrl(LibUtils.URL_LINK)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
    }

    public static RequestBody getBody(Map<String, Object> parameters) {
        return RequestBody
                .create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                        (new JSONObject(parameters)).toString());
    }
}

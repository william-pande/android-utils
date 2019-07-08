package com.wilsofts.libraries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;

import com.wilsofts.libraries.databinding.ActivityMainBinding;
import com.wilsofts.utilities.LibUtils;
import com.wilsofts.utilities.network.NetworkResponse;
import com.wilsofts.utilities.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {
   private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding =  DataBindingUtil.setContentView(this, R.layout.activity_main);

        this.networkTest();
    }

    private void networkTest() {
        LibUtils.SHOW_LOG = true;

        Call<String> call = RetrofitClient.getRetrofitInstance().create(Api.class).login_user("pande2@gmail.com", "123456s789");
        new RetrofitClient(this, this.binding.coordinator, call, "Testing please wait")
                .initRequest(new NetworkResponse() {
                    @Override
                    public void success(int code, String response) {

                    }

                    @Override
                    public void error(boolean timeout, @Nullable Throwable throwable) {

                    }
                });

    }

    interface Api {
        @FormUrlEncoded
        @POST("api/auth/login")
        Call<String> login_user(@Field("email") String username, @Field("password") String password);
    }
}

package com.wilsofts.libraries;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.wilsofts.libraries.databinding.ActivityMainBinding;
import com.wilsofts.utilities.LibUtils;
import com.wilsofts.utilities.dialogs.ReturnResponse;
import com.wilsofts.utilities.network.NetworkResponse;
import com.wilsofts.utilities.network.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //this.networkTest();
        //this.confirmAlert();

        this.binding.cameraImageText.setOnClickListener(listener -> this.startActivity(
                new Intent(this, CameraImageActivity.class)));
    }

    private void confirmAlert() {
        LibUtils.ConfirmationDialog dialog =
                LibUtils.ConfirmationDialog.Companion.newInstance(this, "Hello", "This is my message now.\n<b>This is now formatted</b>",
                        (ReturnResponse) proceed -> LibUtils.INSTANCE.logE(proceed + ""));
        dialog.show(this.getSupportFragmentManager(), "missiles");
    }

    private void networkTest() {
        LibUtils.INSTANCE.setSHOW_LOG(true);

        Call<String> call = RetrofitClient.Companion.getRetrofitInstance().create(Api.class)
                .login_user("pande2@gmail.com", "123456s789");
        new RetrofitClient(this, call, "Testing please wait")
                .initRequest(new NetworkResponse() {
                    @Override
                    public void success(int code, String message) throws JSONException {
                        JSONObject response = new JSONObject(message);
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

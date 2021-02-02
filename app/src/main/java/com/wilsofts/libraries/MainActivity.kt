package com.wilsofts.libraries

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wilsofts.libraries.databinding.ActivityMainBinding
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.network.request.RetrofitClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        networkTest()
    }


    private fun networkTest() {
        RetrofitClient(
                call = RetrofitClient.getRetrofit("https://api.wooowshoes.com/")
                        .create(Api::class.java).get_admin_items(),
                serverResponse = object : RetrofitClient.ServerResponse {
                    override fun response(status: Int, response: JSONObject, throwable: Throwable?) {
                        LibUtils.logE(response.toString(2))
                    }
                }
        )
    }

    private fun post_test() {
        RetrofitClient(
                call = RetrofitClient.getRetrofit("https://api.ichuzz2work.com/").create(Api::class.java)
                        .login_user("pandewilliam100@gmail.com", "123456s789"),
                serverResponse = object : RetrofitClient.ServerResponse {
                    override fun response(status: Int, response: JSONObject, throwable: Throwable?) {
                        LibUtils.logE(response.toString(2))
                    }
                }
        )
    }

    internal interface Api {
        @FormUrlEncoded
        @POST("api/auth/login")
        fun login_user(@Field("email") username: String, @Field("password") password: String): Call<String>

        @GET("admin/items")
        fun get_admin_items(): Call<String>
    }
}

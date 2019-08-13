package com.wilsofts.libraries

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

import com.wilsofts.libraries.databinding.ActivityMainBinding
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.dialogs.ReturnResponse
import com.wilsofts.utilities.image.ImageUtils
import com.wilsofts.utilities.network.NetworkResponse
import com.wilsofts.utilities.network.RetrofitClient

import org.json.JSONException
import org.json.JSONObject

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.io.File

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //this.networkTest();
        //this.confirmAlert();

        this.binding!!.cameraImageText.setOnClickListener { listener ->
            this.startActivity(
                    Intent(this, CameraImageActivity::class.java))
        }
    }

    private fun confirmAlert() {
        val dialog = LibUtils.ConfirmationDialog.newInstance(this,
                "Hello", "This is my message now.\n<b>This is now formatted</b>",
                object : ReturnResponse {
                    override fun response(proceed: Boolean, ignored: Boolean) {
                        LibUtils.logE(proceed.toString() + "")
                    }
                })
        dialog.show(this.supportFragmentManager, "missiles")
    }

    private fun compress() {
        val compressed: File? = ImageUtils.CompressImage(this, "")
                .formatJPEG()
                .height(1200)
                .compressImage()
    }

    private fun networkTest() {
        LibUtils.SHOW_LOG = true

        val call = RetrofitClient.retrofit.create(Api::class.java)
                .login_user("pande2@gmail.com", "123456s789")
        RetrofitClient(this, call, "Testing please wait")
                .initRequest(object : NetworkResponse {
                    @Throws(JSONException::class)
                    override fun success(code: Int, message: String) {
                        val response = JSONObject(message)
                    }

                    override fun error(timeout: Boolean, throwable: Throwable?) {

                    }
                })

    }

    internal interface Api {
        @FormUrlEncoded
        @POST("api/auth/login")
        fun login_user(@Field("email") username: String, @Field("password") password: String): Call<String>
    }
}

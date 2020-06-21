package com.wilsofts.libraries

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wilsofts.libraries.databinding.ActivityMainBinding
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.dialogs.ReturnResponse
import com.wilsofts.utilities.image.ImageUtils
import com.wilsofts.utilities.network.misc.ServerResponse
import com.wilsofts.utilities.network.progressDefault.RetrofitClient
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

        this.networkTest();
        //this.confirmAlert();

        this.binding!!.cameraImageText.setOnClickListener {
            this.startActivity(Intent(this, CameraImageActivity::class.java))
        }
    }

    private fun confirmAlert() {
        LibUtils.ConfirmationDialog.newInstance(
                activity = this,
                title = "Hello",
                message = "This is my message now.\n<b>This is now formatted</b>",
                response = object : ReturnResponse {
                    override fun response(proceed: Boolean, ignored: Boolean) {
                        LibUtils.logE(proceed.toString() + "")
                    }
                })
    }

    private fun compress() {
        val compressed: File? = ImageUtils.CompressImage(this, "")
                .formatJPEG()
                .height(1200)
                .compressImage()
    }

    private fun networkTest() {
        LibUtils.SHOW_LOG = true

        val call = RetrofitClient.getRetrofit("https://api.ichuzz2work.com/").create(Api::class.java)
                .login_user("pande2@gmail.com", "123456s789")
        RetrofitClient(
                activity = null, call = call,
                title = "Testing please wait",
                server_response = object : ServerResponse {
                    override fun success(status: Int, response: JSONObject) {

                    }

                    override fun error(throwable: Throwable, network: Boolean) {

                    }
                })

    }

    internal interface Api {
        @FormUrlEncoded
        @POST("api/auth/login")
        fun login_user(@Field("email") username: String, @Field("password") password: String): Call<String>
    }
}

package com.wilsofts.libraries

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wilsofts.libraries.databinding.ActivityMainBinding
import com.wilsofts.libraries.scoped.ScopedStorageActivity
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.dialogs.MultiSelectOptions
import com.wilsofts.utilities.network.request.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*
import java.io.File
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(this.layoutInflater)
        this.setContentView(this.binding.root)


        this.binding.performTask.setOnClickListener {
            startActivity(Intent(this, ScopedStorageActivity::class.java))
        }
    }

    private fun select() {
        val items = arrayListOf<MultiSelectOptions.MultiSelect>()
        for (index in 1 until 100) {
            items.add(MultiSelectOptions.MultiSelect(item_checked = false, item_id = index.toString(), item_text = "Item $index"))
        }
        MultiSelectOptions.newInstance(
                select_items = items, manager = this.supportFragmentManager,
                multi_receiver = object : MultiSelectOptions.MultiReceiver {
                    override fun receiver(select_items: ArrayList<MultiSelectOptions.MultiSelect>) {
                        TODO("Not yet implemented")
                    }

                }
        )
    }

    private fun networkTest() {
        RetrofitClient(
                call = RetrofitClient
                        .getRetrofit(url = "https://api.wooowshoes.com/")
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
                call = RetrofitClient.getRetrofit(url = "https://api.ichuzz2work.com/")
                        .create(Api::class.java)
                        .login_user("pandewilliam100@gmail.com", "123456s789"),
                serverResponse = object : RetrofitClient.ServerResponse {
                    override fun response(status: Int, response: JSONObject, throwable: Throwable?) {
                        LibUtils.logE(response.toString(2))
                    }
                }
        )
    }

    private fun upload_test() {
        val item_images = mutableListOf<MultipartBody.Part>()
        val images = mutableListOf(
                "/storage/emulated/0/DCIM/100PINT/Pins/7408722fde35c88ed0d41fd5e4c81e31.jpg",
                "/storage/emulated/0/Shoe 3/DSC_0784.jpg",
                "/storage/emulated/0/DCIM/100PINT/Pins/74a36d0e4cd7c00e698da774b2e76d23.jpg",
                "/storage/emulated/0/Shoe 3/DSC_0780.JPG"
        )
        for (index in 0 until images.size) {
            val file = File(images[index])
            if (file.exists()) {
                val body: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                item_images.add(MultipartBody.Part.createFormData("image[${item_images.size}]", file.name, body))
            }
        }

        RetrofitClient(
                call = RetrofitClient.getRetrofit(
                        url = "https://api.wooowshoes.com/",
                        listener = object : RetrofitClient.ProgressListener {
                            override fun update(bytesRead: Long, contentLength: Long, done: Boolean, percentage: Double) {
                                RetrofitClient.logE("$bytesRead, $contentLength, $percentage")
                            }
                        }
                ).create(Api::class.java).save_bank_withdrawal(item_images),
                serverResponse = object : RetrofitClient.ServerResponse {
                    override fun response(status: Int, response: JSONObject, throwable: Throwable?) {
                        LibUtils.logE(response.toString(2))
                        binding.performTask.isEnabled = true
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

        @Multipart
        @POST("admin/items/save")
        fun save_bank_withdrawal(@Part securities: MutableList<MultipartBody.Part>): Call<String>
    }
}

package com.wilsofts.libraries

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wilsofts.libraries.databinding.ActivityCameraImageBinding
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.image.FilePath
import com.wilsofts.utilities.image.ImageUtils
import java.io.File
import java.io.IOException

class CameraImageActivity : AppCompatActivity() {
    private var binding: ActivityCameraImageBinding? = null
    //for storing captured image path
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_image)

        this.binding!!.photoCamera.setOnClickListener {
            try {
                val image_file = ImageUtils.createImageFile("LibTest")
                if (image_file != null) {
                    this.currentPhotoPath = image_file.absolutePath
                    ImageUtils.captureImage(this, image_file, "com.wilsofts.utilities.fileprovider")
                } else {
                    LibUtils.showToast(this, "Failed to initialise file storage")
                }
            } catch (e: IOException) {
                LibUtils.logE(e)
                LibUtils.showToast(this, e.message!!)

                LibUtils.restart(Intent(), this)
            }
        }

        this.binding!!.photoLibrary.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            this.startActivityForResult(intent, ImageUtils.IMAGE_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == ImageUtils.IMAGE_CAMERA) {
                val file = File(this.currentPhotoPath)
                if (file.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                    this.binding!!.imageView.setImageBitmap(myBitmap)

                    try {
                        val compressed = ImageUtils.fileToBase64(this, file, 500, 500)
                        LibUtils.logE(compressed)
                    } catch (e: IOException) {
                        LibUtils.logE(e)
                        LibUtils.showToast(this, e.message!!)
                    }

                }
            } else if (requestCode == ImageUtils.IMAGE_GALLERY) {
                LibUtils.logE(FilePath.getPath(this, data.data!!)!! + " ")
            }
        }
    }
}

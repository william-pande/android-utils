package com.wilsofts.utilities.image

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
object ImageUtils {
    const val IMAGE_GALLERY = 101
    const val IMAGE_CAMERA = 100

    fun createImageFile(activity: FragmentActivity): File {
        @SuppressLint("SimpleDateFormat")
        var time = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        time = "JPEG_" + time + "_"

        val storage_dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(time, ".jpg", storage_dir)
    }

    fun createImageFile(activity: FragmentActivity, parent: String): File? {
        @SuppressLint("SimpleDateFormat")
        var time = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        time = "JPEG_" + time + "_"

        val storage_dir = File(activity.getExternalFilesDir(Environment.DIRECTORY_DCIM), parent)
        if (!storage_dir.exists()) {
            if (!storage_dir.mkdirs()) {
                return null
            }
        }
        return File.createTempFile(time, ".jpg", storage_dir)
    }

    fun captureImage(activity: FragmentActivity, photo_file: File, authority: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            val photo_ui = FileProvider.getUriForFile(activity, authority, photo_file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_ui)
            activity.startActivityForResult(intent, IMAGE_CAMERA)
        }
    }

    fun captureImage(fragment: Fragment, photo_file: File, authority: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(fragment.requireContext().packageManager) != null) {
            val photo_ui = FileProvider.getUriForFile(fragment.requireContext(), authority, photo_file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_ui)
            fragment.startActivityForResult(intent, IMAGE_CAMERA)
        }
    }

    fun fileToBase64(file: File): String {
        val size = file.length().toInt()
        val bytes = ByteArray(size)

        val bufferedInputStream = BufferedInputStream(FileInputStream(file))

        bufferedInputStream.read(bytes, 0, bytes.size)
        bufferedInputStream.close()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }


    fun fileToBase64(context: Context, original_file: File, max_width: Int, max_height: Int, quality: Int): String? {
        val compressed = CompressImage(
                context = context, path = original_file.path, image_quality = quality,
                image_height = max_height, image_width = max_width
        )
                .formatJPEG()
                .destination(getCacheDir(context).absolutePath + File.separator + "Compressed")
                .compressImage() ?: return null

        return fileToBase64(compressed)
    }

    fun getCacheDir(context: Context): File {
        var cache_directory = context.externalCacheDir
        if (cache_directory == null) {
            //fall back
            cache_directory = context.cacheDir
        }
        return cache_directory!!
    }

    fun generate_image_name(suffix: String): String {
        //SDF to generate a unique name for our compress file.
        val SDF = SimpleDateFormat("yyyy_mm_dd_hh_mm_ss", Locale.getDefault())
        return SDF.format(Date()) + if (suffix == "") "" else "_${suffix}"
    }

    class CompressImage(
            private val context: Context,
            private val path: String,
            private var image_width: Int,
            private var image_height: Int,
            private var image_quality: Int = 80,
            private val image_name: String = generate_image_name("")
    ) {

        private lateinit var format: Bitmap.CompressFormat
        private lateinit var extension: String
        private var destination: File? = null

        init {
            this.formatJPEG()
            this.original_destination(File.separator + "Compressed")
        }

        fun formatPNG(): CompressImage {
            this.format = Bitmap.CompressFormat.PNG
            this.extension = ".png"
            return this
        }

        fun formatJPEG(): CompressImage {
            this.format = Bitmap.CompressFormat.JPEG
            this.extension = ".jpg"
            return this
        }

        fun formatWEBP(): CompressImage {
            this.format = Bitmap.CompressFormat.WEBP
            this.extension = ".png"
            return this
        }

        private fun original_destination(directory: String): CompressImage {
            //getting device external cache directory, might not be available on some devices,
            // so our code fall back to internal storage cache directory, which is always available but in smaller quantity
            val cache_directory = getCacheDir(this.context)

            val root_directory = cache_directory.absolutePath + directory
            this.destination = File(root_directory)

            //Create ImageCompressor folder if it does not already exists.
            if (!this.destination!!.exists()) {
                if (!this.destination!!.mkdirs()) {
                    this.destination = null
                }
            }
            return this
        }

        fun destination(root_directory: String): CompressImage {
            this.destination = File(root_directory)

            //Create ImageCompressor folder if it does not already exists.
            if (!this.destination!!.exists()) {
                if (!this.destination!!.mkdirs()) {
                    return this.original_destination("CompressedDefaulted")
                }
            }
            return this
        }

        fun compressImage(): File? {
            if (this.destination == null) {
                return null
            }

            val original_bitmap = BitmapFactory.decodeFile(this.path)
            if (this.image_height > original_bitmap.height || this.image_width > original_bitmap.width) {
                this.image_width = original_bitmap.width
                this.image_height = original_bitmap.height
            } else if (this.image_width > 0) {
                this.image_height = (this.image_width / original_bitmap.width) * original_bitmap.height
            } else if (this.image_height > 0) {
                this.image_width = (this.image_height / original_bitmap.height) * original_bitmap.width
            }

            //decode and resize the original bitmap from @param path.
            val bitmap = decodeImageFromFiles(this.path, this.image_width, this.image_height)

            //create placeholder for the compressed image file // image_name
            val compressed = File(this.destination, this.image_name + this.extension)
            //convert the decoded bitmap to stream
            val byteArrayOutputStream = ByteArrayOutputStream()
            /*compress bitmap into byteArrayOutputStream
                Bitmap.compress(Format, Quality, OutputStream)
                Where Quality ranges from 1 - 100.
             */
            bitmap.compress(this.format, image_quality, byteArrayOutputStream)
            /*
            Right now, we have our bitmap inside byteArrayOutputStream Object, all we need next is to write it to the compressed file we created earlier,
            java.io.FileOutputStream can help us do just That!
             */
            val fileOutputStream = FileOutputStream(compressed)
            fileOutputStream.write(byteArrayOutputStream.toByteArray())
            fileOutputStream.flush()

            fileOutputStream.close()

            //File written, return to the caller. Done!
            return compressed
        }

        private fun decodeImageFromFiles(path: String, width: Int, height: Int): Bitmap {
            val scaleOptions = BitmapFactory.Options()
            scaleOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, scaleOptions)
            var scale = 1
            while (scaleOptions.outWidth / scale / 2 >= width && scaleOptions.outHeight / scale / 2 >= height) {
                scale *= 2
            }
            // decode with the sample size
            val outOptions = BitmapFactory.Options()
            outOptions.inSampleSize = scale
            return BitmapFactory.decodeFile(path, outOptions)
        }
    }
}

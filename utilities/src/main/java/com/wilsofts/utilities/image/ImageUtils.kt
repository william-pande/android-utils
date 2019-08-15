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
import androidx.fragment.app.FragmentActivity
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    const val IMAGE_GALLERY = 101
    const val IMAGE_CAMERA = 100

    @Throws(IOException::class)
    fun createImageFile(activity: FragmentActivity): File {
        @SuppressLint("SimpleDateFormat")
        var time = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        time = "JPEG_" + time + "_"

        val storage_dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(time, ".jpg", storage_dir)
    }

    @Throws(IOException::class)
    fun createImageFile(parent: String): File? {
        @SuppressLint("SimpleDateFormat")
        var time = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        time = "JPEG_" + time + "_"

        val storage_dir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), parent)
        if (!storage_dir.exists()) {
            if (!storage_dir.mkdirs()) {
                return null
            }
        }
        return File.createTempFile(time, ".jpg", storage_dir)
    }

    fun captureImage(context: FragmentActivity, photo_file: File, authority: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            val photo_ui = FileProvider.getUriForFile(context, authority, photo_file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_ui)
            context.startActivityForResult(intent, IMAGE_CAMERA)
        }
    }

    @Throws(IOException::class)
    fun fileToBase64(file: File): String {
        val size = file.length().toInt()
        val bytes = ByteArray(size)

        val bufferedInputStream = BufferedInputStream(FileInputStream(file))

        bufferedInputStream.read(bytes, 0, bytes.size)
        bufferedInputStream.close()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    @Throws(IOException::class)
    fun fileToBase64(context: Context, original_file: File, max_width: Int, max_height: Int, quality: Int): String? {
        val compressed = CompressImage(context, original_file.path)
                .formatJPEG()
                .quality(quality)
                .height(max_height)
                .width(max_width)
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

    class CompressImage(private val context: Context, private val path: String) {
        private var image_width: Int
        private var image_height: Int
        private var image_quality: Int
        private lateinit var format: Bitmap.CompressFormat
        private lateinit var extension: String
        private var destination: File? = null

        init {
            this.image_width = 1200
            this.image_height = 900
            this.image_quality = 80
            this.formatJPEG()
            this.original_destination(File.separator + "Compressed")
        }

        fun height(image_height: Int): CompressImage {
            this.image_height = image_height
            return this
        }

        fun width(image_width: Int): CompressImage {
            this.image_width = image_width
            return this
        }

        fun quality(image_quality: Int): CompressImage {
            this.image_quality = image_quality
            return this
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

        @Throws(IOException::class)
        fun compressImage(): File? {
            if (this.destination == null) {
                return null
            }
            //SDF to generate a unique name for our compress file.
            val SDF = SimpleDateFormat("yyyy_mm_dd_hh_mm_ss", Locale.getDefault())

            //decode and resize the original bitmap from @param path.
            val bitmap = decodeImageFromFiles(this.path, this.image_width, this.image_height)

            //create placeholder for the compressed image file
            val compressed = File(this.destination, SDF.format(Date()) + this.extension)
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

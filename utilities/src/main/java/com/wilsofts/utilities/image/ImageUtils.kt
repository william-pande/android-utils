package com.wilsofts.utilities.image

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import id.zelory.compressor.Compressor
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object ImageUtils {
    const val IMAGE_GALLERY = 101
    const val IMAGE_CAMERA = 100

    @Throws(IOException::class)
    fun createImageFile(activity: FragmentActivity): File {
        @SuppressLint("SimpleDateFormat")
        var time = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        time = "JPEG_" + time + "_"

        val storage_dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(time, ".jpg", storage_dir)
    }

    @Throws(IOException::class)
    fun createImageFile(parent: String): File? {
        @SuppressLint("SimpleDateFormat")
        var time = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
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
    fun fileToBase64(context: Context, original_file: File, max_width: Int, max_height: Int): String {
        val compressed_file = compressFile(
                context,
                original_file,
                File(original_file.parentFile.absolutePath + File.separator + "compressed"),
                max_width,
                max_height)
        return fileToBase64(compressed_file)
    }

    @Throws(IOException::class)
    fun compressFile(context: Context, original_file: File, destination_file: File, max_width: Int, max_height: Int): File {
        if (!destination_file.exists()) {

            destination_file.mkdirs()
        }

        return Compressor(context)
                .setMaxWidth(max_width)
                .setMaxHeight(max_height)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(destination_file.absolutePath)
                .compressToFile(original_file)
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
}

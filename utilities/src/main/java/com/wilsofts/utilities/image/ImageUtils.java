package com.wilsofts.utilities.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class ImageUtils {
    public static final int IMAGE_GALLERY = 101;
    public static final int IMAGE_CAMERA = 100;

    public static File createImageFile(@NonNull FragmentActivity activity) throws IOException {
        @SuppressLint("SimpleDateFormat")
        String time = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        time = "JPEG_" + time + "_";

        File storage_dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(time, ".jpg", storage_dir);
    }

    public static File createImageFile(String parent) throws IOException {
        @SuppressLint("SimpleDateFormat")
        String time = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        time = "JPEG_" + time + "_";

        File storage_dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), parent);
        if (!storage_dir.exists()) {
            if (!storage_dir.mkdirs()) {
                return null;
            }
        }
        return File.createTempFile(time, ".jpg", storage_dir);
    }

    public static void captureImage(FragmentActivity context, File photo_file, String authority) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            Uri photo_ui = FileProvider.getUriForFile(context, authority, photo_file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_ui);
            context.startActivityForResult(intent, ImageUtils.IMAGE_CAMERA);
        }
    }

    public static String fileToBase64(Context context, @NonNull File original_file, int max_width, int max_height) throws IOException {
        File compressed_file = ImageUtils.compressFile(
                context,
                original_file,
                new File( original_file.getParentFile().getAbsolutePath() + File.separator + "compressed"),
                max_width,
                max_height);
        return ImageUtils.fileToBase64(compressed_file);
    }

    public static File compressFile(Context context, @NonNull File original_file, @NonNull  File destination_file , int max_width, int max_height)
            throws IOException {
        if (!destination_file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            destination_file.mkdirs();
        }

        return new Compressor(context)
                .setMaxWidth(max_width)
                .setMaxHeight(max_height)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(destination_file.getAbsolutePath())
                .compressToFile(original_file);
    }

    public static String fileToBase64(File file) throws IOException {
        int size = (int) file.length();
        byte[] bytes = new byte[size];

        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        //noinspection ResultOfMethodCallIgnored
        bufferedInputStream.read(bytes, 0, bytes.length);
        bufferedInputStream.close();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}

package com.wilsofts.utilities.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

    public static void captureImage(FragmentActivity context, File photo_file) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            Uri photo_ui = FileProvider.getUriForFile(context, "com.jomstelecom.file_provider", photo_file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_ui);
            context.startActivityForResult(intent, ImageUtils.IMAGE_CAMERA);
        }
    }

    public static File createImageFile(@NonNull FragmentActivity activity) throws IOException {
        @SuppressLint("SimpleDateFormat")
        String time = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        time = "JPEG_" + time + "_";

        File storage_dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(time, ".jpg", storage_dir);
    }

    public static String fileToBase64(Context context, @NonNull File original_file, int max_width, int max_height) throws IOException {
        File compressed_file = ImageUtils.compressFile(context, original_file, max_width, max_height);
        return ImageUtils.fileToBase64(compressed_file);
    }

    private static File compressFile(Context context, @NonNull File original_file, int max_width, int max_height) throws IOException {
        File parent_file = original_file.getParentFile();
        File destination = new File(parent_file.getAbsolutePath() + File.separator + "compressed");
        if (!destination.exists()) {
            //noinspection ResultOfMethodCallIgnored
            destination.mkdirs();
        }
        destination = new File(destination.getAbsolutePath() + File.separator + original_file.getName());

        return new Compressor(context)
                .setMaxWidth(max_width)
                .setMaxHeight(max_height)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .compressToFile(destination);
    }

    private static String fileToBase64(File compressed_file) throws IOException {
        int size = (int) compressed_file.length();
        byte[] bytes = new byte[size];

        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(compressed_file));
        //noinspection ResultOfMethodCallIgnored
        bufferedInputStream.read(bytes, 0, bytes.length);
        bufferedInputStream.close();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String pathFromUri(@NonNull FragmentActivity activity, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }
}

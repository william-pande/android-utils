package com.wilsofts.libraries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;

import com.wilsofts.libraries.databinding.ActivityCameraImageBinding;
import com.wilsofts.utilities.LibUtils;
import com.wilsofts.utilities.image.FilePath;
import com.wilsofts.utilities.image.ImageUtils;

import java.io.File;
import java.io.IOException;

public class CameraImageActivity extends AppCompatActivity {
    private ActivityCameraImageBinding binding;


    //for storing captured image path
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_image);

        this.binding.photoCamera.setOnClickListener(listener -> {
            try {
                File image_file = ImageUtils.createImageFile("LibTest");
                if (image_file != null) {
                    this.currentPhotoPath = image_file.getAbsolutePath();
                    ImageUtils.captureImage(this, image_file, "com.wilsofts.utilities.fileprovider");
                } else {
                    LibUtils.showToast(this, "Failed to initialise file storage");
                }
            } catch (IOException e) {
                LibUtils.logE(e);
                LibUtils.showToast(this, e.getMessage());
            }
        });

        this.binding.photoLibrary.setOnClickListener(listener -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            this.startActivityForResult(intent, ImageUtils.IMAGE_GALLERY);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == ImageUtils.IMAGE_CAMERA) {
                File file = new File(this.currentPhotoPath);
                if (file.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    this.binding.imageView.setImageBitmap(myBitmap);

                    try {
                        String compressed = ImageUtils.fileToBase64(this, file, 500, 500);
                        LibUtils.logE(compressed);
                    } catch (IOException e) {
                        LibUtils.logE(e);
                        LibUtils.showToast(this, e.getMessage());
                    }
                }
            } else if (requestCode == ImageUtils.IMAGE_GALLERY) {
                LibUtils.logE(FilePath.getPath(this, data.getData())+" ");
            }
        }
    }
}

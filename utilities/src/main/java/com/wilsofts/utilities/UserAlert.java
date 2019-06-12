package com.wilsofts.utilities;

import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Objects;

public class UserAlert extends DialogFragment {
    public static final int ERROR = 0;
    public static final int INFO = 1;
    public static final int SUCCESS = 2;

    public static void showAlert(FragmentActivity activity, String title, String message, int type) {
        UserAlert alert = new UserAlert();
        Bundle arguments = new Bundle();
        arguments.putString("title", title);
        arguments.putString("message", message);
        arguments.putInt("type", type);
        alert.setArguments(arguments);
        alert.show(activity.getSupportFragmentManager(), "missiles");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this.getActivity()),
                R.style.MyDialogTheme);

        Bundle arguments = this.getArguments();

        builder
                .setMessage(Objects.requireNonNull(arguments).getString("message"))
                .setTitle(Objects.requireNonNull(arguments).getString("title"))
                .setPositiveButton(android.R.string.ok, (dialog, id) -> this.dismiss());
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = this.getDialog().getWindow();
        if (window != null) {
            Point point = new Point();
            Display display = window.getWindowManager().getDefaultDisplay();
            display.getSize(point);
            window.setLayout((int) (point.x * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
    }
}
package com.wilsofts.utilities.network;

import android.app.Dialog;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.wilsofts.utilities.R;

import java.util.Objects;

public class DialogProgress extends DialogFragment {

    public static DialogProgress newInstance(String title) {
        DialogProgress myProgressDialog = new DialogProgress();
        Bundle arguments = new Bundle();
        arguments.putString("title", title);
        myProgressDialog.setArguments(arguments);
        return myProgressDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.progress_dialog, container, false);
        Bundle arguments = this.getArguments();

        ProgressBar progress_circular = view.findViewById(R.id.progress_circular);

        progress_circular.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(Objects.requireNonNull(this.getActivity()), R.color.colorPrimaryDark),
                PorterDuff.Mode.SRC_IN);

        ((TextView) view.findViewById(R.id.dialog_title)).setText(Objects.requireNonNull(arguments).getString("title"));
        return view;
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

    public static void showDialog(FragmentActivity activity, DialogFragment dialog) {
        FragmentManager manager = activity.getSupportFragmentManager();
        manager.beginTransaction()
                .add(dialog, "dialog_fragment")
                .commitAllowingStateLoss();
    }

    public static void hideProgress(FragmentActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag("dialog_fragment");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }
}

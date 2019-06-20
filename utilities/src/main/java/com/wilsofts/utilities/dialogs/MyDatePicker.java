package com.wilsofts.utilities.dialogs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MyDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private FragmentActivity activity;


    public static MyDatePicker newInstance(@Nullable String min_date, @Nullable String max_date) {
        MyDatePicker datePicker = new MyDatePicker();
        Bundle bundle = new Bundle();
        bundle.putString("min_date", min_date);
        bundle.putString("max_date", max_date);
        datePicker.setArguments(bundle);
        return datePicker;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = this.getActivity();
        // Use the current date as the default date in the picker
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(this.activity, this, year, month, day);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey("min_date")) {
                String min_date = bundle.getString("min_date");
                if (min_date != null) {
                    dialog.getDatePicker().setMinDate(Long.parseLong(min_date));
                }
            }

            if (bundle.containsKey("max_date")) {
                String max_date = bundle.getString("max_date");
                if (max_date != null) {
                    dialog.getDatePicker().setMaxDate(Long.parseLong(max_date));
                }
            }
        }
        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("EEE dd MMM yyyy");
        Date date = new Date(calendar.getTimeInMillis());

        Intent intent = new Intent("date_time");
        intent.putExtra("string_date", format.format(date));
        intent.putExtra("epoch_date", calendar.getTimeInMillis());
        LocalBroadcastManager.getInstance(Objects.requireNonNull(this.getActivity())).sendBroadcast(intent);
    }

    public static void showDatePickerDialog(FragmentManager fragmentManager) {
        DialogFragment newFragment = new MyDatePicker();
        newFragment.show(fragmentManager, "date_picker");
    }

    public static void showDatePickerDialog(FragmentManager fragmentManager, MyDatePicker datePicker) {
        datePicker.show(fragmentManager, "date_picker");
    }
}

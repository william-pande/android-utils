package com.wilsofts.utilities.dialogs;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class MyTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private FragmentActivity activity;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = this.getActivity();
        // Use the current time as the default values for the picker
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(this.getActivity(), this, hour, minute,
                false);//DateFormat.is24HourFormat(this.activity)
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Intent intent = new Intent("date_time");
        intent.putExtra("int_time", ((hourOfDay * 60) + (minute)) * 60000);

        String time = hourOfDay < 12 ? "AM" : "PM";
        hourOfDay = hourOfDay > 12 ? (hourOfDay - 12) : hourOfDay;
        String hour_ = hourOfDay < 10 ? "0" + hourOfDay : String.valueOf(hourOfDay);
        String minute_ = minute < 10 ? "0" + minute : String.valueOf(minute);
        intent.putExtra("string_time", hour_ + ":" + minute_ + " " + time);
        LocalBroadcastManager.getInstance(this.activity).sendBroadcast(intent);
    }

    public static void showTimePickerDialog(FragmentManager fragmentManager) {
        DialogFragment newFragment = new MyTimePicker();
        newFragment.show(fragmentManager, "time_picker");
    }
}

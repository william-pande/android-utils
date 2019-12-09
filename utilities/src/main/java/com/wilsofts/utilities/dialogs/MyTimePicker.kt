package com.wilsofts.utilities.dialogs


import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TimePicker

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import java.util.Calendar

class MyTimePicker : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity!!, this, hour, minute, false)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        var hour_of_day = hourOfDay
        val intent = Intent("date_time")
        intent.putExtra("int_time", (hour_of_day * 60 + minute) * 60000)

        val time = if (hour_of_day < 12) "AM" else "PM"
        hour_of_day = if (hour_of_day > 12) hour_of_day - 12 else hour_of_day
        val hour_ = if (hour_of_day < 10) "0$hour_of_day" else hour_of_day.toString()
        val minute_ = if (minute < 10) "0$minute" else minute.toString()
        intent.putExtra("string_time", "$hour_:$minute_ $time")
        LocalBroadcastManager.getInstance(this.activity!!).sendBroadcast(intent)
    }

    companion object {
        fun showTimePickerDialog(fragmentManager: FragmentManager) {
            val newFragment = MyTimePicker()
            newFragment.show(fragmentManager, "time_picker")
        }
    }
}

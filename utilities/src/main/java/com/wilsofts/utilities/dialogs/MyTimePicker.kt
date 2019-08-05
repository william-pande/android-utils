package com.wilsofts.utilities.dialogs


import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
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
        var hourOfDay = hourOfDay
        val intent = Intent("date_time")
        intent.putExtra("int_time", (hourOfDay * 60 + minute) * 60000)

        val time = if (hourOfDay < 12) "AM" else "PM"
        hourOfDay = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
        val hour_ = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
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

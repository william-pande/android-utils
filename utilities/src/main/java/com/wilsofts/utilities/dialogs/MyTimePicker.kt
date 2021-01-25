package com.wilsofts.utilities.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.Serializable
import java.util.*

@Suppress("unused")
class MyTimePicker : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private var hour = 0
    private var minute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.hour = it.getInt("hour")
            this.minute = it.getInt("minute")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(this.activity!!, this, this.hour, this.minute, false)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        var hour_of_day = hourOfDay
        val int_time = (hour_of_day * 60 + minute) * 60000

        val time = if (hour_of_day < 12) "AM" else "PM"
        hour_of_day = if (hour_of_day > 12) hour_of_day - 12 else hour_of_day
        val hour_ = if (hour_of_day < 10) "0$hour_of_day" else hour_of_day.toString()
        val minute_ = if (minute < 10) "0$minute" else minute.toString()
        val string_time = "$hour_:$minute_ $time"

        (this.requireArguments().getSerializable("receiver") as TimeReceiver)
                .receive(string_time = string_time, int_time = int_time)
    }

    companion object {
        fun showTimePickerDialog(activity: FragmentActivity, hour: Int = -1, minute: Int = -1, receiver: TimeReceiver) {
            val calendar = Calendar.getInstance()
            val dialog = MyTimePicker().apply {
                this.arguments = Bundle().apply {
                    this.putInt("hour", if (hour == -1) calendar.get(Calendar.HOUR_OF_DAY) else hour)
                    this.putInt("minute", if (minute == -1) calendar.get(Calendar.MINUTE) else minute)
                    this.putSerializable("receiver", receiver)
                }
            }
            dialog.show(activity.supportFragmentManager, "time_picker")
        }
    }

    interface TimeReceiver : Serializable {
        fun receive(string_time: String, int_time: Int)
    }
}

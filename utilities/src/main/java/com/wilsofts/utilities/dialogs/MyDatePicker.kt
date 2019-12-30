package com.wilsofts.utilities.dialogs

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.text.SimpleDateFormat
import java.util.*

class MyDatePicker : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val calendar = Calendar.getInstance()
        // Create a new instance of DatePickerDialog and return it
        var dialog = DatePickerDialog(this.activity!!, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        val bundle = this.arguments
        if (bundle != null) {
            if (bundle.containsKey("current_date")) {
                val current_date = bundle.getString("current_date")
                if (current_date != null) {
                    calendar.timeInMillis = current_date.toLong()
                    dialog = DatePickerDialog(this.activity!!, this, calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                }
            }

            if (bundle.containsKey("min_date")) {
                val min_date = bundle.getString("min_date")
                if (min_date != null) {
                    dialog.datePicker.minDate = min_date.toLong()
                }
            }

            if (bundle.containsKey("max_date")) {
                val max_date = bundle.getString("max_date")
                if (max_date != null) {
                    dialog.datePicker.maxDate = max_date.toLong()
                }
            }
        }
        return dialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DATE, day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat("EEE dd MMM yyyy")
        val date = Date(calendar.timeInMillis)

        val intent = Intent("date_time")
        intent.putExtra("string_date", format.format(date))
        intent.putExtra("epoch_date", calendar.timeInMillis)
        LocalBroadcastManager.getInstance(activity!!).sendBroadcast(intent)
    }

    companion object {
        fun newInstance(min_date: String?, max_date: String?): MyDatePicker {
            val datePicker = MyDatePicker()
            val bundle = Bundle()
            bundle.putString("min_date", min_date)
            bundle.putString("max_date", max_date)
            datePicker.arguments = bundle
            return datePicker
        }

        fun newInstance(current_date: String?, min_date: String?, max_date: String?): MyDatePicker {
            val datePicker = MyDatePicker()
            val bundle = Bundle()
            bundle.putString("min_date", min_date)
            bundle.putString("max_date", max_date)
            bundle.putString("current_date", current_date)
            datePicker.arguments = bundle
            return datePicker
        }

        fun showDatePickerDialog(fragmentManager: FragmentManager) {
            val newFragment = MyDatePicker()
            newFragment.show(fragmentManager, "date_picker")
        }

        fun showDatePickerDialog(fragmentManager: FragmentManager, datePicker: MyDatePicker) {
            datePicker.show(fragmentManager, "date_picker")
        }
    }
}

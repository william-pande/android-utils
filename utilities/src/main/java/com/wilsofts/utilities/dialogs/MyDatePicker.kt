package com.wilsofts.utilities.dialogs

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
class MyDatePicker : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private var min_date = 0L
    private var max_date = 0L
    private var current_date = 0L
    private var pattern = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.min_date = it.getLong("min_date") * 1000
            this.max_date = it.getLong("max_date") * 1000
            this.current_date = it.getLong("current_date") * 1000
            this.pattern = it.getString("pattern")!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val calendar = Calendar.getInstance()

        // Create a new instance of DatePickerDialog and return it
        val dialog = if (this.current_date != 0L) {
            calendar.timeInMillis = this.current_date
            DatePickerDialog(this.requireContext(), this, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        } else {
            DatePickerDialog(this.requireContext(), this, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        }

        if (this.min_date != 0L) {
            dialog.datePicker.minDate = this.min_date
        }

        if (this.max_date != 0L) {
            dialog.datePicker.maxDate = this.max_date
        }
        return dialog
    }

    @SuppressLint("SimpleDateFormat")
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DATE, day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        (this.requireArguments().getSerializable("receiver")!! as DateReceiver)
                .receive(
                        string_date = SimpleDateFormat(this.pattern).format(Date(calendar.timeInMillis)),
                        epoch_date = (calendar.timeInMillis / 1000),
                        server_date = SimpleDateFormat("yyyy-MM-dd").format(Date(calendar.timeInMillis))
                )
    }

    companion object {
        fun newInstance(activity: FragmentActivity, current_date: Long = 0L, min_date: Long = 0L,
                        max_date: Long = 0L, pattern: String = "EEE dd MMM yyyy", receiver: DateReceiver) {

            val dialog = MyDatePicker().apply {
                this.arguments = Bundle().apply {
                    this.putLong("min_date", min_date)
                    this.putLong("max_date", max_date)
                    this.putLong("current_date", current_date)
                    this.putString("pattern", pattern)
                    this.putSerializable("receiver", receiver)
                }
            }
            dialog.show(activity.supportFragmentManager, "date_picker")
        }
    }

    interface DateReceiver : Serializable {
        fun receive(string_date: String, epoch_date: Long, server_date: String)
    }
}

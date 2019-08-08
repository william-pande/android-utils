package com.wilsofts.utilities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.wilsofts.utilities.dialogs.ReturnResponse
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object LibUtils {
    var TAG = "LIB UTILS"
    var SHOW_LOG = true

    var CONNECT_TIMEOUT = 10
    var READ_TIMEOUT = 10
    var WRITE_TIMEOUT = 10

    var URL_LINK = ""

    fun noInternetConnection(context: Context, coordinatorLayout: CoordinatorLayout): Boolean {
        val noInternet = noInternetConnection(context)
        if (noInternet) {
            showError(coordinatorLayout, "No network connection found")
        }
        return noInternet
    }

    fun noInternetConnection(context: Context): Boolean {
        val connectivityManager: ConnectivityManager? = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var activeNetworkInfo: NetworkInfo? = null
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.activeNetworkInfo
        }
        return activeNetworkInfo == null
    }

    fun showError(coordinatorLayout: CoordinatorLayout, error: String) {
        val snack_bar = Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_LONG)

        val view = snack_bar.view
        view.setBackgroundColor(Color.RED)

        val textView = view.findViewById<TextView>(R.id.snackbar_text)
        textView.gravity = Gravity.CENTER_HORIZONTAL
        textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        textView.setTextColor(Color.WHITE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        } else {
            textView.gravity = Gravity.CENTER_HORIZONTAL
        }

        snack_bar.show()
    }

    fun logE(s: String) {
        if (SHOW_LOG) {
            Log.e(TAG, s)
        }
    }

    fun logE(throwable: Throwable) {
        if (SHOW_LOG) {
            Log.e(TAG, throwable.message, throwable)
        }
    }

    fun showToast(activity: FragmentActivity, toast: String) {
        Toast.makeText(activity, toast, Toast.LENGTH_LONG).show()
    }

    fun showErrorToast(activity: FragmentActivity) {
        showToast(activity, activity.getString(R.string.request_unsuccessful))
    }

    fun restart(intent: Intent, context: Context) {
        // Closing all the Activities from stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Add new Flag to start new Activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        // Staring Login Activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

    fun invalidEmail(email_address: String): Boolean {
        return email_address.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email_address).matches()
    }

    fun invalidPassword(password: String): Boolean {
        val regex = "^[a-zA-Z0-9@\\\\#$%&*()_+\\]\\[';:?.,!^-]{6,}$"
        return !password.matches(regex.toRegex())
    }

    fun invalidName(name: String): Boolean {
        val expression = "^[a-zA-Z\\s]+"
        return !name.matches(expression.toRegex())
    }

    fun formatPhoneNumber(country_code: String, contact: String): String {
        var phone_number = contact.replace("\\D".toRegex(), "")

        if (phone_number.startsWith("0")) {
            phone_number = phone_number.substring(1)
        } else if (phone_number.startsWith(country_code)) {
            phone_number = phone_number.substring(country_code.length)
        }
        return country_code + phone_number
    }

    fun invalidPhoneNumber(number: String): Boolean {
        return number.length < 12 || number.length > 16
    }

    fun invalidDouble(number: String): Boolean {
        val DOUBLE_PATTERN = "^[0-9]+(\\.)?[0-9]*"
        val INTEGER_PATTERN = "\\d+"

        if (number.isEmpty()) {
            return true
        }

        return if (Pattern.matches(INTEGER_PATTERN, number)) {
            false
        } else {
            !Pattern.matches(DOUBLE_PATTERN, number)
        }
    }

    fun dialogWindow(window: Window?) {
        if (window != null) {
            val point = Point()
            val display = window.windowManager.defaultDisplay
            display.getSize(point)
            window.setLayout((point.x * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    fun getDate(timeStamp: Long): String {
        return try {
            @SuppressLint("SimpleDateFormat")
            val format = SimpleDateFormat("dd MMMM, yyyy")
            val netDate = Date(timeStamp * 1000)
            format.format(netDate)
        } catch (ex: Exception) {
            ""
        }
    }

    fun shortDateTime(timestamp: Long): String {
        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat("dd EEE MMM, yyyy hh:mm:ss a")
        val date = Date(timestamp * 1000)
        return format.format(date)
    }

    fun shortDate(timestamp: Long): String {
        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat("dd EEE MMM, yyyy")
        val date = Date(timestamp * 1000)
        return format.format(date)
    }

    fun getTime(timestamp: Long): String {
        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat("hh:mm:ss a")
        val date = Date(timestamp * 1000)
        return format.format(date)
    }

    fun dateLongToMillis(string_date: String?): Long {
        if (string_date == null) {
            return 0
        }

        try {
            @SuppressLint("SimpleDateFormat")
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = simpleDateFormat.parse(string_date)
            return date.time / 1000
        } catch (e: ParseException) {
            logE(e)
        }

        return 0
    }

    fun dateLongToString(date: String): String {
        val timestamp = dateLongToMillis(date)
        return shortDate(timestamp)
    }

    fun dateShortToMillis(string_date: String?): Long {
        if (string_date == null) {
            return 0
        }

        try {
            @SuppressLint("SimpleDateFormat")
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date = simpleDateFormat.parse(string_date)
            return date.time / 1000
        } catch (e: ParseException) {
            logE(e)
        }

        return 0
    }

    fun dateShortToString(date: String): String {
        val timestamp = dateShortToMillis(date)
        return shortDate(timestamp)
    }

    fun formatDouble(number: Double): String {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(number)
    }

    fun formatNumber(number: Double): String {
        @SuppressLint("DefaultLocale")
        val formatted = String.format("%,.2f", number)
        if (formatted.endsWith(".00"))
            return formatted.substring(0, formatted.length - 3)
        else if (formatted.contains(".") && formatted.endsWith("0"))
            return formatted.substring(0, formatted.length - 2)
        return formatted
    }

    fun extractDatabase(context: Context, database_name: String) {
        try {
            val storage_file = Environment.getExternalStorageDirectory()

            if (storage_file.canWrite()) {
                val database_path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    context.filesDir.absolutePath.replace("files", "databases") + File.separator
                else
                    context.filesDir.path + context.packageName + "/databases/"

                val current_database_file = File(database_path, database_name)
                val backup_database_file = File(storage_file, "$database_name.db")

                if (current_database_file.exists()) {
                    val source_file_channel = FileInputStream(current_database_file).channel
                    val destination_file_channel = FileOutputStream(backup_database_file).channel
                    destination_file_channel.transferFrom(source_file_channel, 0, source_file_channel.size())
                    source_file_channel.close()
                    destination_file_channel.close()
                }
            }
        } catch (e: IOException) {
            logE(e)
        }

    }

    fun listBundle(intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                val value = bundle.get(key)
                logE(String.format("%s %s (%s)", key, value!!.toString(), value.javaClass.name))
            }
        }
    }

    fun listIntentData(intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                val value = bundle.get(key)
                logE(String.format("%s %s (%s)", key, value!!.toString(), value.javaClass.name))
            }
        }
    }

    fun writeDBToSD(context: Context, db_name: String) {
        try {
            val sd = Environment.getExternalStorageDirectory()

            if (sd.canWrite()) {
                val DB_PATH: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    context.filesDir.absolutePath.replace("files", "databases") + File.separator
                } else {
                    context.filesDir.path + context.packageName + "/databases/"
                }

                val currentDB = File(DB_PATH, db_name)
                val backupDB = File(sd, db_name)

                if (currentDB.exists()) {
                    val src = FileInputStream(currentDB).channel
                    val dst = FileOutputStream(backupDB).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                }
            }
        } catch (e: IOException) {
            logE(e)
        }

    }

    class RecyclerViewSpacing(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

        constructor(context: Context, @DimenRes itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId))

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(this.mItemOffset, this.mItemOffset, this.mItemOffset, this.mItemOffset)
        }
    }

    class ConfirmationDialog : DialogFragment() {
        private var extras: Bundle? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = this.activity
            this.extras = this.arguments
            assert(this.extras != null)
            assert(activity != null)

            val returnResponse = this.extras!!.getSerializable("returnResponse") as ReturnResponse

            val inflater = this.requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_layout, null)
            (view.findViewById<View>(R.id.message_text) as TextView).text = Html.fromHtml(this.extras!!.getString("message"))

            val check_box = view.findViewById<CheckBox>(R.id.do_not_show)
            val check_text: String = this.extras!!.getString("check")!!
            if (check_text.isEmpty()) {
                check_box.visibility = View.GONE
            } else {
                check_box.text = check_text
            }

            val builder = AlertDialog.Builder(activity!!, R.style.MyDialogTheme)
                    .setView(view)
                    .setTitle(this.extras!!.getString("title"))
                    .setPositiveButton(this.extras!!.getString("ok")) { dialog, _ ->
                        dialog.dismiss()
                        returnResponse.response(true, check_box.isChecked)
                    }
                    .setNegativeButton(this.extras!!.getString("cancel")) { dialog, _ ->
                        dialog.dismiss()
                        returnResponse.response(false, check_box.isChecked)
                    }


            return builder.create()
        }

        override fun onResume() {
            super.onResume()
            val window = this.dialog.window
            if (window != null) {
                val point = Point()
                val display = window.windowManager.defaultDisplay
                display.getSize(point)
                window.setLayout((point.x * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                window.setGravity(this.extras!!.getInt("gravity"))

                val params = window.attributes
                params.x = this.extras!!.getInt("offset_x")
                params.y = this.extras!!.getInt("offset_y")
                window.attributes = params
            }
        }

        companion object {
            fun newInstance(context: Context, title: String, message: String, returnResponse: ReturnResponse, check: String = ""): ConfirmationDialog {
                return newInstance(title, message, context.getString(R.string.proceed), context.getString(R.string.cancel), Gravity.CENTER, 0, 0, returnResponse, check)
            }

            fun newInstance(context: Context, title: String, message: String, gravity: Int, offset_x: Int, offset_y: Int,
                            returnResponse: ReturnResponse, check: String = ""): ConfirmationDialog {
                return newInstance(title, message, context.getString(R.string.proceed), context.getString(R.string.cancel), gravity, offset_x, offset_y, returnResponse, check)
            }

            fun newInstance(title: String, message: String, ok: String, cancel: String, gravity: Int, offset_x: Int,
                            offset_y: Int, returnResponse: ReturnResponse, check: String = ""): ConfirmationDialog {
                val dialog = ConfirmationDialog()

                val extras = Bundle()
                extras.putString("title", title)
                extras.putString("message", message)
                extras.putString("ok", ok)
                extras.putString("cancel", cancel)
                extras.putInt("gravity", gravity)
                extras.putInt("offset_y", offset_y)
                extras.putInt("offset_x", offset_x)
                extras.putString("check", check)
                extras.putSerializable("returnResponse", returnResponse)
                dialog.arguments = extras

                return dialog
            }
        }
    }
}

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
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
    var NET_LOG = true
    var CHECK_NETWORK = true

    var CONNECT_TIMEOUT = 60
    var READ_TIMEOUT = 60
    var WRITE_TIMEOUT = 60

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

    fun uri_to_file(context: Context, uri: Uri, delete: Boolean = true): File? {
        fun get_file_name(): String? {
            val file_name: String?
            logE("Scheme = ${uri.scheme}")
            if (uri.scheme.equals("file")) {
                file_name = uri.lastPathSegment
            } else {
                context.contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DISPLAY_NAME),
                        null, null, null)!!.also {
                    it.moveToFirst()
                    file_name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    //file_name = it.getString(it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
                    it.close()
                }
            }
            return file_name
        }

        var destination: File? = null
        val file_name = get_file_name()
        if (file_name != null) {
            var suffix = ""
            var prefix = file_name
            if (file_name.contains(".")) {
                prefix = file_name.substring(0, file_name.lastIndexOf("."))
                suffix = file_name.substring(file_name.lastIndexOf("."))
            }
            destination = File.createTempFile(prefix, suffix, context.externalCacheDir)
            if (delete) {
                destination.deleteOnExit()
            }

            val input_stream = context.contentResolver.openInputStream(uri)
            val output_stream = FileOutputStream(destination)
            var len = 0
            val buffer = ByteArray(1024)

            do {
                if (len > 0) {
                    output_stream.write(buffer, 0, len)
                }
                len = input_stream?.read(buffer) ?: 0
            } while (len > 0)
            input_stream?.close()
        }
        return destination
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

    fun logE(s: String, tag: String = TAG) {
        if (SHOW_LOG) {
            Log.e(tag, s)
        }
    }

    fun logE(throwable: Throwable, tag: String = TAG) {
        if (SHOW_LOG) {
            Log.e(tag, throwable.message, throwable)
        }
        throwable.printStackTrace()
    }

    fun showToast(context: Context, toast: String) {
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
    }

    fun showErrorToast(context: Context) {
        showToast(context, context.getString(R.string.request_unsuccessful))
    }

    fun restart(context: Context, cls: Class<*>) {
        val intent = Intent(context, cls)
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

    fun invalidNumber(number: String): Boolean {
        val INTEGER_PATTERN = "\\d+"

        if (number.isEmpty()) {
            return true
        }
        return !Pattern.matches(INTEGER_PATTERN, number)
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
            return date!!.time / 1000
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
            return date!!.time / 1000
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
        if (formatted.endsWith(".00")) {
            return formatted.substring(0, formatted.length - 3)
        } else if (formatted.contains(".") && formatted.endsWith("0")) {
            return formatted.substring(0, formatted.length - 2)
        }
        return formatted
    }

    fun extractDatabase(context: Context, database_name: String) {
        try {
            val storage_file = context.getExternalFilesDir(null)

            if (storage_file != null && storage_file.canWrite()) {
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
            val file = context.getExternalFilesDir(null)

            if (file != null && file.canWrite()) {
                val DB_PATH: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    context.filesDir.absolutePath.replace("files", "databases") + File.separator
                } else {
                    context.filesDir.path + context.packageName + "/databases/"
                }

                val currentDB = File(DB_PATH, db_name)
                val backupDB = File(file, db_name)

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

    fun shareContent(context: Context, subject: String, url_text: String, title: String) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        } else {
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        }
        share.putExtra(Intent.EXTRA_SUBJECT, subject)
        share.putExtra(Intent.EXTRA_TEXT, url_text)

        context.startActivity(Intent.createChooser(share, title))
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
            this.extras = this.arguments!!

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
            val window = this.dialog?.window
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
            fun newInstance(title: String, message: String, ok: String = "Proceed", cancel: String = "Cancel",
                            gravity: Int = Gravity.CENTER, offset_x: Int = 0, offset_y: Int = 0, response: ReturnResponse,
                            check: String = "", activity: FragmentActivity) {
                val dialog = ConfirmationDialog().apply {
                    this.arguments = Bundle().apply {
                        this.putString("title", title)
                        this.putString("message", message)
                        this.putString("ok", ok)
                        this.putString("cancel", cancel)
                        this.putInt("gravity", gravity)
                        this.putInt("offset_y", offset_y)
                        this.putInt("offset_x", offset_x)
                        this.putString("check", check)
                        this.putSerializable("returnResponse", response)
                    }
                }
                dialog.show(activity.supportFragmentManager, "user_confirmation")
            }
        }
    }
}

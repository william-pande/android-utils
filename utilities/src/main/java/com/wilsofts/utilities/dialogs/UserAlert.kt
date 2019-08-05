package com.wilsofts.utilities.dialogs

import android.app.Dialog
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.wilsofts.utilities.R
import java.util.*

class UserAlert : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.MyDialogTheme)

        val arguments = this.arguments
        builder
                .setMessage(arguments!!.getString("message"))
                .setTitle(arguments.getString("title"))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    this.dismiss()
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
            window.setLayout((point.x * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    companion object {
        const val ERROR = 0
        const val INFO = 1
        const val SUCCESS = 2

        fun showAlert(activity: FragmentActivity, title: String, message: String, type: Int) {
            val alert = UserAlert()
            val arguments = Bundle()
            arguments.putString("title", title)
            arguments.putString("message", message)
            arguments.putInt("type", type)
            alert.arguments = arguments
            alert.show(activity.supportFragmentManager, "missiles")
        }

        fun showAlert(activity: FragmentActivity, message: String, type: Int) {
            val alert = UserAlert()
            val arguments = Bundle()
            var title = "Alert"
            when (type) {
                ERROR -> title = activity.getString(R.string.error)
                SUCCESS -> title = activity.getString(R.string.success)
                INFO -> title = activity.getString(R.string.info)
            }
            arguments.putString("title", title)
            arguments.putString("message", message)
            arguments.putInt("type", type)
            alert.arguments = arguments
            alert.show(activity.supportFragmentManager, "missiles")
        }
    }
}
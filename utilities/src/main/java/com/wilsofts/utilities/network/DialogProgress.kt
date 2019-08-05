package com.wilsofts.utilities.network

import android.app.Dialog
import android.graphics.Point
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.wilsofts.utilities.R

class DialogProgress : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.progress_dialog, container, false)
        val arguments = this.arguments

        val progress_circular = view.findViewById<ProgressBar>(R.id.progress_circular)

        progress_circular.indeterminateDrawable.setColorFilter(
                ContextCompat.getColor(activity!!, R.color.alert_dialog_text_color),
                PorterDuff.Mode.SRC_IN)
        (view.findViewById<View>(R.id.dialog_title) as TextView).text = arguments!!.getString("title")
        return view
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

            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    companion object {
        fun newInstance(title: String): DialogProgress {
            val myProgressDialog = DialogProgress()
            val arguments = Bundle()
            arguments.putString("title", title)
            myProgressDialog.arguments = arguments
            return myProgressDialog
        }

        fun showDialog(activity: FragmentActivity, dialog: DialogFragment) {
            val manager = activity.supportFragmentManager
            manager.beginTransaction()
                    .add(dialog, "dialog_fragment")
                    .commitAllowingStateLoss()
        }

        fun hideProgress(activity: FragmentActivity) {
            val manager = activity.supportFragmentManager
            val fragment = manager.findFragmentByTag("dialog_fragment")
            if (fragment != null) {
                manager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
        }
    }
}

package com.wilsofts.utilities.network

import android.annotation.SuppressLint
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
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.R
import com.wilsofts.utilities.network.progressClient.ProgressUpdater
import java.text.DecimalFormat

class DialogProgress : DialogFragment(), ProgressUpdater.ProgressListener {
    var progress_circular: ProgressBar? = null
    var horizontal_progress: ProgressBar? = null
    var progress_text: TextView? = null
    private var first_update: Boolean = false

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

        this.progress_circular = view.findViewById(R.id.progress_circular)
        this.progress_circular?.indeterminateDrawable?.setColorFilter(
                ContextCompat.getColor(activity!!, R.color.alert_dialog_text_color), PorterDuff.Mode.SRC_IN)

        this.horizontal_progress = view.findViewById(R.id.horizontal_progress)
        this.horizontal_progress?.indeterminateDrawable?.setColorFilter(
                ContextCompat.getColor(activity!!, R.color.alert_dialog_text_color), PorterDuff.Mode.SRC_IN)
        horizontal_progress?.progressDrawable?.setColorFilter(
                ContextCompat.getColor(activity!!, R.color.alert_dialog_text_color), PorterDuff.Mode.SRC_IN)


        /* this.horizontal_progress.secondaryProgressTintMode.setColorFilter(
                 ContextCompat.getColor(activity!!, R.color.alert_dialog_text_color), PorterDuff.Mode.SRC_IN)
         this.horizontal_progress.indeterminateDrawable.setColorFilter(
                 ContextCompat.getColor(activity!!, R.color.alert_dialog_text_color), PorterDuff.Mode.SRC_IN)*/

        this.progress_text = view.findViewById(R.id.progress_text)


        (view.findViewById<View>(R.id.dialog_title) as TextView).text = arguments!!.getString("title")
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        LibUtils.logE("Read = $bytesRead, total = $contentLength")
        if (done) {
            activity?.runOnUiThread {
                this.horizontal_progress?.progress = 100
                this.progress_circular?.visibility = View.VISIBLE
                this.horizontal_progress?.visibility = View.GONE
                this.progress_text?.visibility = View.GONE
            }

        } else {
            if (first_update) {
                first_update = false
                activity?.runOnUiThread {
                    horizontal_progress?.isIndeterminate = contentLength == -1L
                }
                LibUtils.logE("Complete")
            }

            if (contentLength != -1L) {
                val percentage: Long = (100 * bytesRead) / contentLength
                val formatted = DecimalFormat("#.##").format(percentage)

                val bytes_read = DecimalFormat("#").format(bytesRead / 1000)
                val content_length = DecimalFormat("#").format(contentLength / 1000)

                activity?.runOnUiThread {
                    this.progress_text?.text = "${bytes_read}kbs of $content_length ($formatted% complete)"
                    LibUtils.logE("${bytes_read}kbs of $content_length ($formatted% complete)")
                }
            }
        }
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

package com.wilsofts.utilities.network.progressLinear

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.R
import java.text.DecimalFormat

class DialogProgressLinear : DialogFragment(), ProgressUpdater.ProgressListener {
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
        val view = inflater.inflate(R.layout.progress_dialog_linear, container, false)
        val arguments = this.arguments

        this.horizontal_progress = view.findViewById(R.id.horizontal_progress)
        this.horizontal_progress?.indeterminateDrawable?.setColorFilter(
                ContextCompat.getColor(activity!!, R.color.alert_dialog_text_color), PorterDuff.Mode.SRC_IN)

        val progress_bar_drawable: LayerDrawable? = this.horizontal_progress?.progressDrawable as LayerDrawable
        progress_bar_drawable?.getDrawable(0)?.setColorFilter(
                ContextCompat.getColor(activity!!, R.color.alert_dialog_text_color), PorterDuff.Mode.SRC_IN)
        progress_bar_drawable?.getDrawable(1)?.setColorFilter(
                ContextCompat.getColor(activity!!, R.color.color_progress_cream), PorterDuff.Mode.SRC_IN)

        this.progress_text = view.findViewById(R.id.progress_text)


        (view.findViewById<View>(R.id.dialog_title) as TextView).text = arguments!!.getString("title")

        this.horizontal_progress?.visibility = View.VISIBLE
        this.progress_text?.visibility = View.VISIBLE
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        LibUtils.logE("Read = $bytesRead, total = $contentLength")
        if (done) {
            activity?.runOnUiThread {
                this.horizontal_progress?.progress = 100
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

                val bytes_read = DecimalFormat("#").format(bytesRead)
                val content_length = DecimalFormat("#").format(contentLength)

                activity?.runOnUiThread {
                    this.progress_text?.text = "${bytes_read}kbs of $content_length ($formatted% complete)"
                    LibUtils.logE("${bytes_read}kbs of $content_length ($formatted% complete)")
                }
            } else {
                activity?.runOnUiThread {
                    this.progress_text?.text = "${bytesRead}kbs complete)"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val window = this.dialog?.window
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
        fun newInstance(title: String): DialogProgressLinear {
            val myProgressDialog = DialogProgressLinear()
            val arguments = Bundle()
            arguments.putString("title", title)
            myProgressDialog.arguments = arguments
            return myProgressDialog
        }
    }
}

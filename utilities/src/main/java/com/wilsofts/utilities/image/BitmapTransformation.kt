package com.wilsofts.utilities.image

import android.graphics.Bitmap
import com.squareup.picasso.Transformation

class BitmapTransformation(private val max_width: Int, private val max_height: Int) : Transformation {

    override fun transform(source: Bitmap): Bitmap {
        val target_width: Int
        val target_height: Int
        val aspect_ratio: Double

        if (source.width > source.height) {
            target_width = this.max_width
            aspect_ratio = source.height.toDouble() / source.width.toDouble()
            target_height = (target_width * aspect_ratio).toInt()
        } else {
            target_height = this.max_height
            aspect_ratio = source.width.toDouble() / source.height.toDouble()
            target_width = (target_height * aspect_ratio).toInt()
        }

        val result = Bitmap.createScaledBitmap(source, target_width, target_height, false)
        if (result != source) {
            source.recycle()
        }
        return result
    }

    override fun key(): String {
        return this.max_width.toString() + "x" + this.max_height
    }
}

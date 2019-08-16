package com.wilsofts.utilities.image

import android.graphics.*

import com.squareup.picasso.Transformation
import kotlin.math.min

class CircularTransformation : Transformation {
    private val border_color: Int
    private val border_width: Int

    constructor() {
        this.border_color = android.R.color.transparent
        this.border_width = 0
    }

    constructor(border_color: Int, border_width: Int) {
        this.border_color = border_color
        this.border_width = border_width
    }

    override fun transform(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)

        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) {
            source.recycle()
        }

        val bitmap = Bitmap.createBitmap(size, size, source.config)

        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true

        //border code
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.color = this.border_color
        paint.strokeWidth = this.border_width.toFloat()

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        squaredBitmap.recycle()
        return bitmap
    }

    override fun key(): String {
        return "circle"
    }
}

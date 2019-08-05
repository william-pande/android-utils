package com.wilsofts.utilities.image

import android.graphics.*

class RoundedTransformation// radius is corner radii in dp
// margin is the board in dp
(private val radius: Int, private val margin: Int) : com.squareup.picasso.Transformation {

    override fun transform(source: Bitmap): Bitmap {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawCircle((source.width - this.margin) / 2f, (source.height - this.margin) / 2f, (this.radius - 2).toFloat(), paint)

        if (source != output) {
            source.recycle()
        }

        val paint1 = Paint()
        paint1.color = Color.RED
        paint1.style = Paint.Style.STROKE
        paint1.isAntiAlias = true
        paint1.strokeWidth = 2f
        canvas.drawCircle((source.width - this.margin) / 2f, (source.height - this.margin) / 2f, (this.radius - 2).toFloat(), paint1)


        return output
    }

    override fun key(): String {
        return "rounded"
    }
}
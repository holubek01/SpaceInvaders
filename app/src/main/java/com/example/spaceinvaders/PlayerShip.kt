package com.example.spaceinvaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF

class PlayerShip(
    context: Context,
    private val x: Int,
    y: Int
) {

    val bitmap: Bitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.playership
    ).let { scaledBitmap(it, x / 15f, y / 20f) }

    private val speed = 450f
    val width = bitmap.width.toFloat()

    val position = RectF(
        x / 2f,
        y - bitmap.height.toFloat(),
        x / 2 + width,
        y.toFloat()
    )

    var moving = stopped

    companion object {
        const val stopped = 0
        const val left = 1
        const val right = 2
    }

    fun update(fps: Long) {
        if (moving == left && position.left > 0) {
            position.left -= speed / fps
        } else if (moving == right && position.left < x - width) {
            position.left += speed / fps
        }
        position.right = position.left + width
    }

    private fun scaledBitmap(bitmap: Bitmap, width: Float, height: Float): Bitmap {
        return Bitmap.createScaledBitmap(
            bitmap,
            width.toInt(),
            height.toInt(),
            false
        )
    }
}

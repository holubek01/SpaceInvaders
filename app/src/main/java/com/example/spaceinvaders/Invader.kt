package com.example.spaceinvaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import java.util.*

class Invader(context: Context, row: Int, column: Int, x: Int, y: Int, size:Float) {
    var width = x / size
    private var height = y / size
    private val padding = x / 45

    var position = RectF().apply {
        left = column * (width + padding)
        top = 100 + row * (width + padding / 4)
        right = left + width
        bottom = top + height
    }

    private var speed = 40f

    private val left = 1
    private val right = 2

    private var shipMoving = right

    var isVisible = true

    companion object {
        var bitmap1: Bitmap? = null
        var bitmap2: Bitmap? = null

        var numberOfInvaders = 0
    }

    init {
        bitmap1 = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.invader1)

        bitmap2 = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.invader2)


        bitmap1 = bitmap1?.let {
            Bitmap.createScaledBitmap(
                it,
                width.toInt(),
                height.toInt(),
                false)
        }

        bitmap2 = bitmap2?.let {
            Bitmap.createScaledBitmap(
                it,
                width.toInt(),
                height.toInt(),
                false)
        }

        numberOfInvaders ++
    }

    fun update(fps: Long) {
        position.left += speed / fps * (shipMoving - 1)
        position.right = position.left + width
    }

    fun dropDownAndReverse(waveNumber: Int) {
        shipMoving = if (shipMoving == left) {
            right
        } else {
            left
        }

        position.top += height
        position.bottom += height

        // Im większa fala tym szybciej poruszją się obcy
        speed *=  (1.1f + (waveNumber.toFloat() / 20))
    }

    fun takeAim(playerShipX: Float,
                playerShipLength: Float,
                waves: Int)
            : Boolean {

        val generator = Random()
        var randomNumber: Int

        if (playerShipX + playerShipLength > position.left &&
            playerShipX + playerShipLength < position.left + width ||
            playerShipX > position.left && playerShipX < position.left + width) {

            randomNumber = generator.nextInt(100 * numberOfInvaders) / waves
            if (randomNumber == 0) {
                return true
            }

        }

        randomNumber = generator.nextInt(150 * numberOfInvaders)
        return randomNumber == 0

    }
}
package com.example.spaceinvaders

import android.graphics.RectF

class Brick(
    private val row: Int,
    private val column: Int,
    private val shelterNumber: Int,
    private val x: Int,
    private val y: Int
) {
    var isVisible = true

    private val width = x / 180
    private val height = y / 80
    private val spaceBetweenShelters = x / 9f
    private val startHeight = y - (y / 10f * 2f)

    val position = RectF(
        calculateLeft(),
        calculateTop(),
        calculateRight(),
        calculateBottom()
    )

    private fun calculateLeft(): Float {
        return (column * width + 1 +
                spaceBetweenShelters * shelterNumber +
                spaceBetweenShelters + spaceBetweenShelters * shelterNumber)
    }

    private fun calculateTop(): Float {
        return (row * height + 1 + startHeight)
    }

    private fun calculateRight(): Float {
        return (column * width + width - 1 +
                spaceBetweenShelters * shelterNumber +
                spaceBetweenShelters + spaceBetweenShelters * shelterNumber)
    }

    private fun calculateBottom(): Float {
        return (row * height + height - 1 + startHeight)
    }
}

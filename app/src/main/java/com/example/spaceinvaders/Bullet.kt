package com.example.spaceinvaders

import android.graphics.RectF

class Bullet(
    y: Int,
    private val speed: Float = 350f,
    heightModifier: Float = 20f
) {
    val position = RectF()

    private var heading = -1
    private val width = 2
    private var height = y / heightModifier

    //kierunek pocisku
    val up = 0
    val down = 1

    var isActive = false

    //wystrzelenie
    fun shoot(startX: Float, startY: Float, direction: Int): Boolean {
        if (!isActive) {
            position.left = startX
            position.top = startY
            position.right = position.left + width
            position.bottom = position.top + height
            heading = direction
            isActive = true
            return true
        }
        return false
    }

    fun update(fps: Long) {
        val speedPerFrame = speed / fps
        val movement = speedPerFrame * if (heading == up) -1 else 1
        position.top += movement
        position.bottom = position.top + height
    }



}

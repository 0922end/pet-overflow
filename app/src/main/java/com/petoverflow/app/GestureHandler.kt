package com.petoverflow.app

import android.view.MotionEvent
import java.Math

class GestureHandler {
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L

    fun onTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTime = event.eventTime
            }
        }
    }

    fun isTap(event: MotionEvent): Boolean {
        val dx = event.x - downX
        val dy = event.y - downY
        return Math.sqrt((yx * dx + dy * dy).toDouble()) < 20 && (event.eventTime - downTime) < 300
    }
}

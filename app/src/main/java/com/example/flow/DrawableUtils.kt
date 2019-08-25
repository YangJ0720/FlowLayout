package com.example.flow

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import java.util.*

/**
 * 功能描述
 * @author Administrator
 * @since 2019/8/24
 */
object DrawableUtils {

    private const val ALPHA = 255

    private fun createColor(): Int {
        val random = Random()
        val r = random.nextInt(256)
        val g = random.nextInt(256)
        val b = random.nextInt(256)
        return Color.argb(ALPHA, r, g, b)
    }

    fun createDrawable(): Drawable {
        return createDrawable(createColor())
    }

    fun createDrawable(color: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.setColor(color)
        drawable.setStroke(10, createColor())
        drawable.cornerRadius = 80.0f
        return drawable
    }

}
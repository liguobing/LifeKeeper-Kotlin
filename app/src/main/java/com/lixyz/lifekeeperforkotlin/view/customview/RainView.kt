package com.lixyz.lifekeeperforkotlin.view.customview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import java.lang.Math.toRadians

internal class RainView(private val params: Params) {
    private var size: Int = 0
    private var alpha: Int = 255
    private var bitmap: Bitmap? = null
    private var speedX: Double = 0.0
    private var speedY: Double = 0.0
    private var positionX: Double = 0.0
    private var positionY: Double = 0.0

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 255, 255)
            style = Style.FILL
        }
    }
    private val random by lazy { Randomizer() }

    var shouldRecycleFalling = true
    private var stopped = false

    init {
        reset()
    }

    internal fun reset(positionY: Double? = null) {
        shouldRecycleFalling = true
        size = random.randomInt(params.sizeMinInPx, params.sizeMaxInPx, gaussian = true)
        if (params.image != null) {
            bitmap = Bitmap.createScaledBitmap(params.image, size, size, false)
        }

        val speed =
            ((size - params.sizeMinInPx).toFloat() / (params.sizeMaxInPx - params.sizeMinInPx) *
                    (params.speedMax - params.speedMin) + params.speedMin)
        val angle = toRadians(random.randomDouble(params.angleMax) * random.randomSignum())
        speedX = speed * kotlin.math.sin(angle)
        speedY = speed * kotlin.math.cos(angle)

        alpha = random.randomInt(params.alphaMin, params.alphaMax)
        paint.alpha = alpha

        positionX = random.randomDouble(params.parentWidth)
        if (positionY != null) {
            this.positionY = positionY
        } else {
            this.positionY = random.randomDouble(params.parentHeight)
        }
    }

    fun isStillFalling(): Boolean {
        return (shouldRecycleFalling || (positionY > 0 && positionY < params.parentHeight))
    }

    fun update() {
        positionX += speedX
        positionY += speedY
        if (positionY > params.parentHeight) {
            if (shouldRecycleFalling) {
                if (stopped) {
                    stopped = false
                    reset()
                } else {
                    reset(positionY = -size.toDouble())
                }
            } else {
                positionY = params.parentHeight + size.toDouble()
                stopped = true
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, positionX.toFloat(), positionY.toFloat(), paint)
        } else {
            canvas.drawCircle(positionX.toFloat(), positionY.toFloat(), size.toFloat(), paint)
        }
    }

    data class Params(
        val parentWidth: Int,
        val parentHeight: Int,
        val image: Bitmap?,
        val alphaMin: Int,
        val alphaMax: Int,
        val angleMax: Int,
        val sizeMinInPx: Int,
        val sizeMaxInPx: Int,
        val speedMin: Int,
        val speedMax: Int
    )
}
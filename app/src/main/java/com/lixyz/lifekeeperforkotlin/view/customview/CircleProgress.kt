package com.lixyz.lifekeeperforkotlin.view.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.lixyz.lifekeeperforkotlin.R

class CircleProgress : View {

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs!!)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs!!)
    }

    /**
     * 背景色
     */
    private var backgroundColor: Int? = null

    /**
     * 前景色
     */
    private var foregroundColor: Int? = null

    /**
     * 进度值
     */
    var progress: Float? = null

    /**
     * 半径
     */
    private var radius: Float? = null

    /**
     * 画笔
     */
    private var paint: Paint? = null

    private fun init(context: Context, attrs: AttributeSet?) {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.CircleProgress)
        backgroundColor =
            attributes.getColor(R.styleable.CircleProgress_progress_background_color, Color.BLACK)
        foregroundColor =
            attributes.getColor(R.styleable.CircleProgress_progress_foreground_color, Color.GRAY)
        progress = attributes.getFloat(R.styleable.CircleProgress_progress, 0f)
        radius = attributes.getDimension(R.styleable.CircleProgress_progress_radius, 100f)
        attributes.recycle()

        paint = Paint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(radius!!.toInt() * 2, radius!!.toInt() * 2)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint!!.color = backgroundColor!!
        canvas!!.drawCircle(measuredWidth / 2f, measuredHeight / 2f, radius!!, paint!!)
        paint!!.color = foregroundColor!!
        canvas.drawArc(
            getRect(),
            0f, progress!!, true, paint!!
        )
    }

    private fun getRect(): RectF {
        return RectF(
            measuredWidth / 2 - radius!!,
            measuredHeight / 2 - radius!!,
            measuredWidth / 2 + radius!!,
            measuredHeight / 2 + radius!!
        )
    }
}
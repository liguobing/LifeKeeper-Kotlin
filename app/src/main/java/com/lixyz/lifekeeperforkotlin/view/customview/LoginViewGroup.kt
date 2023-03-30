package com.lixyz.lifekeeperforkotlin.view.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.RelativeLayout


/**
 * @author LGB
 * 登录页面根布局
 * 主要是为了画底部的圆形
 */
class LoginViewGroup : RelativeLayout {
    private lateinit var bottomCirclePaint: Paint
    private var mWidth = 0
    private var mHeight = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(
            mWidth.toFloat() / 2,
            mHeight.toFloat() + mWidth.toFloat() / 4,
            mWidth.toFloat() * 3 / 4,
            bottomCirclePaint
        )
    }

    private fun init() {
        setWillNotDraw(false)
        bottomCirclePaint = Paint()
        bottomCirclePaint.style = Paint.Style.FILL
        bottomCirclePaint.color = Color.WHITE
    }
}
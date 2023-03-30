package com.lixyz.lifekeeperforkotlin.view.customview

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView
import com.lixyz.lifekeeperforkotlin.R


/**
 * 首页走马灯文字
 *
 * @author LGB
 */
class IndexRotateText : AppCompatTextView {
    /**
     * Context
     */
    private var mContext: Context? = null

    /**
     * AttributeSet
     */
    private var attrs: AttributeSet? = null

    /**
     * 需要绘制的文字
     */
    private var drawText: String? = null

    /**
     * 前半段字符串
     */
    private var preStr = ""

    /**
     * 前半段画笔
     */
    private var prePaint: Paint? = null

    /**
     * 前半段绘制文字 Rect
     */
    private var preRect: Rect? = null

    /**
     * 动画
     */
    private var animator: ValueAnimator? = null

    constructor(context: Context) : super(context) {
        this.mContext = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.mContext = context
        this.attrs = attrs
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.mContext = context
        this.attrs = attrs
        init()
    }

    private fun init() {
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.IndexRotateText)
        val textSize =
            array.getDimension(R.styleable.IndexRotateText_android_textSize, 10f)
        drawText = array.getString(R.styleable.IndexRotateText_android_text)
        array.recycle()
        prePaint = Paint()
        prePaint!!.textSize = textSize
        prePaint!!.color = Color.BLACK
        preRect = Rect()
        animator = ValueAnimator.ofInt(0, drawText!!.length)
        animator!!.duration = (drawText!!.length + 1) * 100.toLong()
        animator!!.interpolator = LinearInterpolator()
        animator!!.addUpdateListener(AnimatorUpdateListener { animation ->
            preStr = drawText!!.substring(0, animation.animatedValue as Int)
            prePaint!!.getTextBounds(preStr, 0, preStr.length, preRect)
            invalidate()
        })
        animator!!.repeatCount = -1
    }

    fun start() {
        animator!!.start()
    }

    fun stop() {
        animator!!.cancel()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawText(
            preStr,
            width / 2f - preRect!!.width() / 2f,
            height / 2f + preRect!!.height() / 2f,
            prePaint!!
        )
    }
}
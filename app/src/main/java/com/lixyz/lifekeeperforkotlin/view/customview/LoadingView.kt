package com.lixyz.lifekeeperforkotlin.view.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.lixyz.lifekeeperforkotlin.R

class LoadingView : View {
    constructor(context: Context?) : super(context) {
        init(context!!, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context!!, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context!!, attrs)
    }

    private var backgroundColor: Int? = null
    private var progressWidth: Int? = null
    private var progressColor: Int? = null

    private fun init(context: Context, attrs: AttributeSet?) {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.LoadingView)
        backgroundColor = attributes.getColor(R.styleable.LoadingView_backgroundColor, Color.BLUE)
        progressWidth = attributes.getInt(R.styleable.LoadingView_progressWidth, 30)
        progressColor = attributes.getColor(R.styleable.LoadingView_progressColor, Color.BLUE)
        attributes.recycle()
    }

    private var widthSize = 0F
    private var heightSize = 0F

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthSize = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        heightSize = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    private var paint = Paint()
    private var animation1: ValueAnimator? = null

    fun start() {
        animation1 = ValueAnimator.ofInt(0, 360)
        animation1!!.duration = 1000
        animation1!!.repeatCount = -1
        animation1!!.repeatMode = ValueAnimator.RESTART
        animation1!!.interpolator = LinearInterpolator()
        animation1!!.addUpdateListener { animation ->
            startAngle = animation!!.animatedValue as Int
            invalidate()
        }
        animation1!!.start()
    }

    private var iconStatus = true

    private fun zoomImg(bm: Bitmap): Bitmap {
        // 获得图片的宽高
        val width = bm.width
        val height = bm.height
        // 计算缩放比例
        val scaleWidth = if (widthSize / 2 > width) {
            ((width) / widthSize / 4)
        } else {
            (widthSize / 4) / (width)
        }
        // 取得想要缩放的matrix参数
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleWidth)
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
    }

    fun success(result: Boolean) {
        if (result) {
            bitmap1 = zoomImg(bitmap1)
        } else {
            bitmap2 = zoomImg(bitmap2)
        }

        iconStatus = result
        animation1!!.cancel()
        animation1 = ValueAnimator.ofInt(sweepAngle, 360)
        animation1!!.duration = 1000
        animation1!!.repeatCount = 0
        animation1!!.interpolator = LinearInterpolator()
        animation1!!.addUpdateListener { animation ->
            sweepAngle = animation!!.animatedValue as Int
            invalidate()
            if (sweepAngle >= 360) {
                showIcon()
            }
        }
        animation1!!.start()
    }

    private fun showIcon() {
        animation1 = ValueAnimator.ofInt(progressWidth!!, widthSize.toInt() / 2)
        animation1!!.duration = 1000
        animation1!!.repeatCount = 0
        animation1!!.interpolator = LinearInterpolator()
        animation1!!.addUpdateListener {
            progressWidth = it.animatedValue as Int
            invalidate()
            if (progressWidth!! >= widthSize.toInt() / 2) {
                drawBitmap()
            }
        }
        animation1!!.start()
    }

    private fun drawBitmap() {
        animation1!!.cancel()
        animation1 = ValueAnimator.ofInt(0, 255)
        animation1!!.duration = 1000
        animation1!!.repeatCount = 0
        animation1!!.interpolator = LinearInterpolator()
        animation1!!.addUpdateListener {
            bitmapPaintAlpha = it.animatedValue as Int
            invalidate()
            if (bitmapPaintAlpha >= 255) {
                listener!!.onFinished()
            }
        }
        animation1!!.start()
    }

    private var sweepAngle = 90
    private var startAngle = 0
    private var bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.success)
    private var bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.fail)
    private val bitmapPaint = Paint()
    private var bitmapPaintAlpha = 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val rect = RectF(0f, 0f, widthSize, heightSize)
        paint.color = progressColor!!
        canvas!!.drawArc(rect, startAngle.toFloat(), sweepAngle.toFloat(), true, paint)

        paint.color = backgroundColor!!
        val rect2 = RectF(
            progressWidth!!.toFloat(),
            progressWidth!!.toFloat(),
            widthSize - progressWidth!!,
            heightSize - progressWidth!!
        )
        canvas.drawArc(rect2, 0f, 360f, true, paint)
        bitmapPaint.alpha = bitmapPaintAlpha

        if (iconStatus) {
            canvas.drawBitmap(
                bitmap1,
                widthSize / 2 - bitmap1.width / 2,
                heightSize / 2 - bitmap1.height / 2,
                bitmapPaint
            )
        } else {
            canvas.drawBitmap(
                bitmap2,
                widthSize / 2 - bitmap2.width / 2,
                heightSize / 2 - bitmap2.height / 2,
                bitmapPaint
            )
        }

    }

    var listener: OnProgressFinishedListener? = null

    fun setOnProgressFinishedListener(listener: OnProgressFinishedListener) {
        this.listener = listener
    }

    interface OnProgressFinishedListener {
        fun onFinished()
    }
}
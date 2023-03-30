package com.lixyz.lifekeeperforkotlin.view.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


/**
 * @author LGB
 * 个人中心，自定义布局
 */
class BevelLayout : RelativeLayout {
    private var width = 0f
    private var height = 0f
    private var path: Path? = null
    private var paint: Paint? = null
    private val bitmap: Bitmap? = null
    private var newbm: Bitmap? = null

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("个人中心自定义布局线程池"))

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        path = Path()
        paint = Paint()
        val linearGradient = LinearGradient(
            0f, 0f, 1080f, 1920f, Color.parseColor("#18545A"),
            Color.parseColor("#F1F2B5"), Shader.TileMode.MIRROR
        )
        paint!!.shader = linearGradient
        paint!!.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        val bitmap =
            BitmapFactory.decodeResource(resources, R.drawable.persional_infomation___background)
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        // 计算缩放比例
        val scaleWidth = height / 2 / bitmapHeight.toFloat()
        // 取得想要缩放的matrix参数
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleWidth)
        // 得到新的图片
        newbm = Bitmap.createBitmap(
            bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix,
            true
        )
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(newbm!!, 0f, 0f, paint)
        path!!.moveTo(0f, height / 2)
        path!!.lineTo(0f, height)
        path!!.lineTo(width, height)
        path!!.lineTo(width, height * 3 / 8)
        path!!.close()
        canvas.drawPath(path!!, paint!!)
    }
}
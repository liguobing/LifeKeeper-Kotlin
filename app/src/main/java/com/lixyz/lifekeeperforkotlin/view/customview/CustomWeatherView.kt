package com.lixyz.lifekeeperforkotlin.view.customview

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.animation.ValueAnimator.RESTART
import android.animation.ValueAnimator.REVERSE
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.RelativeLayout
import com.lixyz.lifekeeperforkotlin.R
import java.util.*
import kotlin.collections.ArrayList


class CustomWeatherView : RelativeLayout {

    /**
     * 天气代码
     */
    var weatherCode = 0

    /**
     * 背景渐变色,会根据天气已经时间进行变化
     */
    private var backgroundGradient: LinearGradient? = null

    /**
     * 背景范围，整个屏幕
     */
    private var rect: Rect? = null

    /**
     * 画笔
     */
    private val paint: Paint = Paint()

    /**
     * 雨点 Bitmap
     */
    private val rainBitmap = BitmapFactory.decodeResource(resources, R.drawable.rain)

    /**
     * 雪花 Bitmap
     */
    private val snowBitmap = BitmapFactory.decodeResource(resources, R.drawable.snow)

    /**
     * 云彩 Bitmap
     */
    private val cloudBitmap = BitmapFactory.decodeResource(resources, R.drawable.cloud)

    /**
     * 太阳 Bitmap
     */
    private val sunBitmap = BitmapFactory.decodeResource(resources, R.drawable.sun)

    /**
     * 屏幕宽度
     */
    private var screenWidth: Int = 0

    /**
     * 屏幕高度
     */
    private var screenHeight: Int = 0

    /**
     * 用于产生随机数
     */
    private val random = Random()

    /**
     * 用于存储随机点，这些点用于确定雨滴的位置
     */
    private var rainPointList: ArrayList<Point> = ArrayList()

    /**
     * 雪花片数
     */
    private var snowNum: Int = 0

    /**
     * 雪花图片
     */
    private var snowImage: Bitmap? = null

    /**
     * 雪花最小透明度
     */
    private var snowAlphaMin: Int = 0

    /**
     * 雪花最大透明度
     */
    private var snowAlphaMax: Int = 0

    /**
     * 雪花最大漂移角度
     */
    private var snowAngleMax: Int = 0

    /**
     * 雪花最小尺寸
     */
    private var snowSizeMinInPx: Int = 0

    /**
     * 雪花最大尺寸
     */
    private var snowSizeMaxInPx: Int = 0

    /**
     * 雪花下降最小速度
     */
    private var snowSpeedMin: Int = 0

    /**
     * 雪花下降最大速度
     */
    private var snowSpeedMax: Int = 0

    /**
     * 用于更新雪花的线程
     */
    private var updateSnowflakesThread: UpdateSnowflakesThread? = null

    /**
     * 存储雪花的列表
     */
    private var snowList: Array<SnowView>? = null

    /**
     * 控制雨速
     */
    private var rainTimer: Timer? = null

    /**
     * 雷电动画
     */
    private var thunderAnimator: ValueAnimator? = null

    /**
     * 绘制雷电的画笔
     */
    private val thunderPaint = Paint()

    /**
     * 雷电透明度
     */
    private var thunderAlpha = 0

    /**
     * 绘制的雷电
     */
    private var drawThunderBitmap = BitmapFactory.decodeResource(resources, R.drawable.lightning0)

    /**
     * 替换的雷电
     */
    private val lightning0 = BitmapFactory.decodeResource(resources, R.drawable.lightning0)
    private val lightning1 = BitmapFactory.decodeResource(resources, R.drawable.lightning1)
    private val lightning2 = BitmapFactory.decodeResource(resources, R.drawable.lightning2)
    private val lightning3 = BitmapFactory.decodeResource(resources, R.drawable.lightning3)
    private val lightning4 = BitmapFactory.decodeResource(resources, R.drawable.lightning4)

    /**
     * 雷电替换
     */
    val thunderList = arrayOf(
        lightning0,
        lightning1,
        lightning2,
        lightning3,
        lightning4
    )

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

    private fun init(context: Context, attrs: AttributeSet?) {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.CustomWeatherView)
        weatherCode = attributes.getInt(R.styleable.CustomWeatherView_weather_code, 999)
        attributes.recycle()
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        screenWidth = measuredWidth
        screenHeight = measuredHeight

        initParam(screenWidth, screenHeight)
    }

    /**
     * 初始化参数
     * 之所以放在这里，是因为 Rect 需要在测量了控件宽高之后创建
     */
    private fun initParam(width: Int, height: Int) {
        rect = Rect(0, 0, width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when (weatherCode) {
            //晴 - 白天
            100 -> {
                drawBackground(canvas!!, "#0071D1", "#6DA6E4")
                canvas.drawBitmap(sunBitmap, 0f, 0f, paint)
            }
            //多云
            101 -> {
                drawBackground(canvas!!, "#5C82C1", "#95B1DB")
                canvas.drawBitmap(cloudBitmap, cloudTopPosition.toFloat(), 0f, paint)
                canvas.drawBitmap(
                    cloudBitmap,
                    cloudMiddlePosition.toFloat(),
                    cloudBitmap.height.toFloat(),
                    paint
                )
                canvas.drawBitmap(
                    cloudBitmap,
                    cloudBottomPosition.toFloat(),
                    cloudBitmap.height * 2f,
                    paint
                )
            }
            //少云
            102 -> {
                drawBackground(canvas!!, "#5C82C1", "#95B1DB")
                canvas.drawBitmap(cloudBitmap, cloudTopPosition.toFloat(), 0f, paint)
            }
            //晴间多云 - 白天
            103 -> {
                canvas!!.drawBitmap(sunBitmap, 0f, 0f, paint)
                drawBackground(canvas, "#5C82C1", "#95B1DB")
                canvas.drawBitmap(cloudBitmap, cloudTopPosition.toFloat(), 0f, paint)
                canvas.drawBitmap(
                    cloudBitmap,
                    cloudMiddlePosition.toFloat(),
                    cloudBitmap.height.toFloat(),
                    paint
                )
                canvas.drawBitmap(
                    cloudBitmap,
                    cloudBottomPosition.toFloat(),
                    cloudBitmap.height * 2f,
                    paint
                )
            }
            //阴 - 白天
            104 -> {
                drawBackground(canvas!!, "#8FA3C0", "#8C9FB1")
            }
            //晴 - 黑夜
            150, 151, 152 -> {
                drawBackground(canvas!!, "#061E74", "#275E9A")
                drawStar(canvas)
            }
            //晴间多云 - 黑夜
            153 -> {
                drawBackground(canvas!!, "#2C3A60", "#4B6685")
                canvas.drawBitmap(cloudBitmap, cloudTopPosition.toFloat(), 0f, paint)
                canvas.drawBitmap(
                    cloudBitmap,
                    cloudMiddlePosition.toFloat(),
                    cloudBitmap.height.toFloat(),
                    paint
                )
                canvas.drawBitmap(
                    cloudBitmap,
                    cloudBottomPosition.toFloat(),
                    cloudBitmap.height * 2f,
                    paint
                )
            }
            //阴  - 黑夜
            154 -> {
                drawBackground(canvas!!, "#8FA3C0", "#8C9FB1")
            }
            //阵雨 - 白天
            300 -> {
                drawBackground(canvas!!, "#556782", "#7c8b99")
                drawRain(rainPointList, canvas)
            }
            //	雷阵雨
            302, 303, 310, 311, 312, 316, 317, 318, 351 -> {
                drawBackground(canvas!!, "#3B434E", "#565D66")
                drawRain(rainPointList, canvas)
                drawThunder(canvas)
            }
            //雨
            301, 304, 305, 306, 307, 308, 309, 313, 314, 315, 399, 350 -> {
                drawBackground(canvas!!, "#3A4B65", "#495764")
                drawRain(rainPointList, canvas)
            }
            //	雪
            400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 499, 456, 457 -> {
                drawBackground(canvas!!, "#8595AD", "#95A4BF")
                drawSnow(canvas)
            }
            //薄雾
            500, 501, 509, 514, 515, 510 -> {
                drawBackground(canvas!!, "#A6B3C2", "#737F88")
            }
            //霾
            502, 511, 512, 513 -> {
                drawBackground(canvas!!, "#989898", "#4B4B4B")
            }
            //扬沙
            503, 504, 507, 508 -> {
                drawBackground(canvas!!, "#B99D79", "#6C5635")
            }
            //其他
            else -> {
                drawBackground(canvas!!, "#0071D1", "#6DA6E4")
            }
        }
    }

    /**
     * 绘制背景
     */
    private fun drawBackground(canvas: Canvas, startColor: String, endColor: String) {
        backgroundGradient =
            LinearGradient(
                width / 2f,
                0f,
                width / 2f,
                height.toFloat(),
                Color.parseColor(startColor),
                Color.parseColor(endColor),
                Shader.TileMode.MIRROR
            )
        paint.shader = backgroundGradient
        canvas.drawRect(
            rect!!, paint
        )
    }

    private val starPaint = Paint()

    private fun drawStar(canvas: Canvas) {
        for (index in 1..10) {
            val starX = (0..screenWidth).random()
            val starY = (0..500).random()
            val starWidth = (5..10).random()
            starPaint.strokeWidth = starWidth + 0f
            starPaint.color = Color.WHITE
            starPaint.alpha = (0..255).random()
            starPaint.strokeCap = Paint.Cap.ROUND
            canvas.drawPoint(starX + 0f, starY + 0f, starPaint)
        }
    }


    /**
     * 绘制雪花
     */
    private fun drawSnow(canvas: Canvas) {
        if (isInEditMode) {
            return
        }
        val fallingSnowflakes = snowList?.filter { it.isStillFalling() }
        if (fallingSnowflakes?.isNotEmpty() == true) {
            fallingSnowflakes.forEach { it.draw(canvas) }
            updateSnowflakes()
        } else {
            visibility = GONE
        }
    }

    /**
     * 绘制雨点
     */
    private fun drawRain(pointList: ArrayList<Point>, canvas: Canvas) {
        pointList.clear()
        for (index in 1..50) {
            pointList.add(
                Point(
                    random.nextInt(screenWidth) % (screenWidth - 0 + 1) + 0,
                    random.nextInt(screenHeight) % (screenHeight - 0 + 1) + 0
                )
            )
        }
        pointList.forEach { t: Point ->
            canvas.drawBitmap(rainBitmap, t.x.toFloat(), t.y.toFloat(), paint)
        }
    }


    /**
     * 绘制雷电
     */
    private fun drawThunder(canvas: Canvas) {
        thunderPaint.alpha = thunderAlpha
        canvas.drawBitmap(
            drawThunderBitmap,
            Rect(0, 0, drawThunderBitmap.width, drawThunderBitmap.height),
            Rect(0, 0, screenWidth, drawThunderBitmap.height),
            thunderPaint
        )
    }

    private fun startThunder() {
        thunderAnimator = ValueAnimator.ofInt(0, 255, 0)
        thunderAnimator!!.addUpdateListener {
            thunderAlpha = it.animatedValue as Int
            invalidate()
        }

        thunderAnimator!!.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {
                drawThunderBitmap = thunderList[(0..4).random()]
            }

            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
        })
        thunderAnimator!!.duration = 2000
        thunderAnimator!!.interpolator = BounceInterpolator()
        thunderAnimator!!.repeatCount = -1
        thunderAnimator!!.repeatMode = REVERSE
        thunderAnimator!!.start()
    }

    private var cloudTopPosition = 0
    private var cloudMiddlePosition = 0
    private var cloudBottomPosition = 0

    fun start() {
        Log.d("TTT", "start: $weatherCode")
        when (weatherCode) {
            101, 103, 153 -> {
                val topAnimation = ValueAnimator.ofInt(-cloudBitmap.width, screenWidth)
                topAnimation.duration = 10000
                topAnimation.repeatCount = -1
                topAnimation.repeatMode = RESTART
                topAnimation.addUpdateListener {
                    val value = it.animatedValue as Int
                    cloudTopPosition = value
                    invalidate()
                }

                val middleAnimation = ValueAnimator.ofInt(screenWidth, -cloudBitmap.width)
                middleAnimation.duration = 8000
                middleAnimation.repeatCount = -1
                middleAnimation.repeatMode = RESTART
                middleAnimation.addUpdateListener {
                    val value = it.animatedValue as Int
                    cloudMiddlePosition = value
                    invalidate()
                }

                val bottomAnimation = ValueAnimator.ofInt(-cloudBitmap.width, screenWidth)
                bottomAnimation.duration = 15000
                bottomAnimation.repeatCount = -1
                bottomAnimation.repeatMode = RESTART
                bottomAnimation.addUpdateListener {
                    val value = it.animatedValue as Int
                    cloudBottomPosition = value
                    invalidate()
                }
                topAnimation.start()
                middleAnimation.start()
                bottomAnimation.start()
            }
//            103 -> {
//                val animation = ValueAnimator.ofInt(-cloudBitmap.width, screenWidth)
//                animation.duration = 10000
//                animation.repeatCount = -1
//                animation.repeatMode = RESTART
//                animation.addUpdateListener {
//                    val value = it.animatedValue as Int
//                    cloudTopPosition = value
//                    invalidate()
//                }
//                animation.start()
//            }
            //小雨，雨点30
            300, 305, 309, 314 -> {
                rainPointList.clear()
                for (index in 1..5) {
                    rainPointList.add(
                        Point(
                            random.nextInt(screenWidth) % (screenWidth - 0 + 1) + 0,
                            random.nextInt(screenHeight) % (screenHeight - 0 + 1) + 0
                        )
                    )
                }
                rainTimer = Timer()
                rainTimer!!.scheduleAtFixedRate(object : TimerTask() {
                    //                    var value = 1 // Initial value
                    override fun run() {
                        invalidate()
                    }
                }, 0, 200)
            }
            //中雨，雨点50
            302, 304, 306, 313, 315, 399, 350 -> {
                rainPointList.clear()
                for (index in 1..50) {
                    rainPointList.add(
                        Point(
                            random.nextInt(screenWidth) % (screenWidth - 0 + 1) + 0,
                            random.nextInt(screenHeight) % (screenHeight - 0 + 1) + 0
                        )
                    )
                }
                rainTimer = Timer()
                rainTimer!!.scheduleAtFixedRate(object : TimerTask() {
                    var value = 1 // Initial value
                    override fun run() {
                        invalidate()
                    }
                }, 0, 100)
                startThunder()
            }
            //大雨，雨点80
            301, 303, 307, 308, 310, 311, 312, 316, 317, 318, 351 -> {
                rainPointList.clear()
                for (index in 1..80) {
                    rainPointList.add(
                        Point(
                            random.nextInt(screenWidth) % (screenWidth - 0 + 1) + 0,
                            random.nextInt(screenHeight) % (screenHeight - 0 + 1) + 0
                        )
                    )
                }
                rainTimer = Timer()
                rainTimer!!.scheduleAtFixedRate(object : TimerTask() {
                    var value = 1 // Initial value
                    override fun run() {
                        invalidate()
                    }
                }, 0, 30)
                startThunder()
            }
            //小雪
            400, 408 -> {
                snowNum = 50
                snowImage = snowBitmap
                snowAlphaMin = 150
                snowAlphaMax = 255
                snowAngleMax = 5
                snowSizeMinInPx = (5 * resources.displayMetrics.density).toInt()
                snowSizeMaxInPx = (28 * resources.displayMetrics.density).toInt()
                snowSpeedMin = 2
                snowSpeedMax = 8
                snowList = createSnowflakes()
                snowList?.forEach { it.shouldRecycleFalling = true }
                updateSnowflakesThread = UpdateSnowflakesThread()
                invalidate()
            }
            //中雪
            401, 404, 405, 406, 407, 409, 499, 456, 457 -> {
                snowNum = 100
                snowImage = snowBitmap
                snowAlphaMin = 150
                snowAlphaMax = 255
                snowAngleMax = 5
                snowSizeMinInPx = (8 * resources.displayMetrics.density).toInt()
                snowSizeMaxInPx = (32 * resources.displayMetrics.density).toInt()
                snowSpeedMin = 4
                snowSpeedMax = 12
                snowList = createSnowflakes()
                snowList?.forEach { it.shouldRecycleFalling = true }
                updateSnowflakesThread = UpdateSnowflakesThread()
                invalidate()
            }
            //大雪
            402, 403, 410 -> {
                snowNum = 150
                snowImage = snowBitmap
                snowAlphaMin = 150
                snowAlphaMax = 255
                snowAngleMax = 5
                snowSizeMinInPx = (10 * resources.displayMetrics.density).toInt()
                snowSizeMaxInPx = (35 * resources.displayMetrics.density).toInt()
                snowSpeedMin = 7
                snowSpeedMax = 17
                snowList = createSnowflakes()
                snowList?.forEach { it.shouldRecycleFalling = true }
                updateSnowflakesThread = UpdateSnowflakesThread()
                invalidate()
            }
            else -> {
                invalidate()
            }
        }
    }

    fun stop() {
        when (weatherCode) {
            300, 305, 309, 314, 302, 304, 306, 313, 315, 399, 350, 301, 303, 307, 308, 310, 311, 312, 316, 317, 318, 351 -> {
                rainTimer!!.cancel()
            }
            //雪
            400, 408, 401, 404, 405, 406, 407, 409, 499, 456, 457, 402, 403, 410 -> {
                snowList?.forEach { it.shouldRecycleFalling = false }
            }
        }
        if (thunderAnimator != null && thunderAnimator!!.isStarted) {
            thunderAnimator!!.cancel()
        }
    }


    fun destory() {
        sunBitmap.recycle()
        cloudBitmap.recycle()
        snowBitmap.recycle()
        rainBitmap.recycle()
        drawThunderBitmap.recycle()
        lightning0.recycle()
        lightning1.recycle()
        lightning2.recycle()
        lightning3.recycle()
        lightning4.recycle()
    }

    override fun onDetachedFromWindow() {
        updateSnowflakesThread?.quit()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (changedView === this && visibility == GONE) {
            snowList?.forEach { it.reset() }
        }
    }

    /**
     * 生成雪花
     */
    private fun createSnowflakes(): Array<SnowView> {
        val snowflakeParams = SnowView.Params(
            parentWidth = width,
            parentHeight = height,
            image = snowImage,
            alphaMin = snowAlphaMin,
            alphaMax = snowAlphaMax,
            angleMax = snowAngleMax,
            sizeMinInPx = snowSizeMinInPx,
            sizeMaxInPx = snowSizeMaxInPx,
            speedMin = snowSpeedMin,
            speedMax = snowSpeedMax
        )
        return Array(snowNum) { SnowView(snowflakeParams) }
    }

    /**
     * 更新雪花
     */
    private fun updateSnowflakes() {
        val fallingSnowflakes = snowList?.filter { it.isStillFalling() }
        if (fallingSnowflakes?.isNotEmpty() == true) {
            updateSnowflakesThread!!.handler.post {
                fallingSnowflakes.forEach { it.update() }
                postInvalidateOnAnimation()
            }
        }
    }

    private class UpdateSnowflakesThread : HandlerThread("雪花下降线程") {
        val handler by lazy { Handler(looper) }

        init {
            start()
        }
    }
}
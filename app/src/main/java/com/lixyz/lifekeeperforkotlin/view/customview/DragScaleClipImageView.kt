package com.lixyz.lifekeeperforkotlin.view.customview

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.lixyz.lifekeeperforkotlin.utils.Arith
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs
import kotlin.math.sqrt


/**
 * @author LGB
 */
class DragScaleClipImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {

    private val mContext: Context = context

    /**
     * 源Bitmap
     */
    private var sourceBitmap: Bitmap? = null

    /**
     * 绘制图片的画笔
     */
    private val bitmapPaint: Paint = Paint()

    /**
     * 屏幕宽度
     */
    private var screenWidth = 0f

    /**
     * 屏幕高度
     */
    private var screenHeight = 0f

    /**
     * 绘制在屏幕上的 Bitmap
     */
    private var drawBitmap: Bitmap? = null

    /**
     * 缩放 Matrix
     */
    private val mMatrix: Matrix = Matrix()

    /**
     * 第一根手指的 X Y 坐标
     */
    private var firstPointerDownX = 0
    private var firstPointerDownY = 0

    /**
     * 图像缩放倍数
     */
    private var scaleValue = 1.0f

    /**
     * 第二根手指按下时，两手指之间距离，用于确定是扩大还是缩小
     */
    private var downDiagonal = 0.0

    /**
     * 临时存储手指移动距离，用于反映多次拖动
     */
    private var tempMoveSizeX = 0
    private var tempMoveSizeY = 0

    /**
     * 拖动的距离
     */
    private var dragX = 0
    private var dragY = 0

    /**
     * 是拖动还是缩放
     */
    private var isDrag = true

    /**
     * 遮盖罩圆的半径
     */
    private var mWidthOrRadius = 0

    /**
     * 绘制阴影层的画笔
     */
    private var coverPaint: Paint? = null

    /**
     * 阴影层图像
     */
    private var mCircleBmp: Bitmap? = null
    private var mCanvas: Canvas? = null

    /**
     * 遮盖层的面积
     */
    private var coverRect: Rect? = null

    /**
     * 绘制不透明的实心圆画笔
     */
    private val mPaintCircle: Paint = Paint()
    private val mCirclePaint: Paint = Paint()
    private var file: File? = null
    private var drawBitmapLeft: Float = 0f
    private var drawBitmapTop: Float = 0f
    private var tempDrawBitmapLeft = 0f
    private var tempDrawBitmapTop = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    fun setBitmap(sourceBitmap: Bitmap?) {
        this.sourceBitmap = sourceBitmap
        drawBitmap = sourceBitmap
    }

    fun init() {
        //遮罩层的画笔设置
        coverPaint = Paint()
        coverPaint!!.color = Color.parseColor("#50000000")
        coverPaint!!.style = Paint.Style.FILL
        mCirclePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //计算圆形的半径
        mWidthOrRadius = width / 2
    }

    private var saveBitmap: Bitmap? = null
    private var saveCanvas: Canvas? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(drawBitmap!!, drawBitmapLeft, drawBitmapTop, bitmapPaint)
        //遮罩层的画笔设置
        coverPaint!!.color = Color.parseColor("#50000000")
        coverPaint!!.style = Paint.Style.FILL
        //阴影层图像
        mCircleBmp =
            Bitmap.createBitmap(coverRect!!.right, coverRect!!.bottom, Bitmap.Config.ARGB_4444)
        mCanvas = Canvas(mCircleBmp!!)

        //绘制阴影层（整个屏幕）
        mCanvas!!.drawRect(coverRect!!, coverPaint!!)
        //绘制实心圆,绘制完后，在mCanvas画布中，mPaintRect和mPaintCirle相交部分即被掏空
        mCanvas!!.drawCircle(
            width / 2f, height / 2f,
            width / 2f - 100, mPaintCircle
        )
        mCanvas!!.drawCircle(
            width / 2f, height / 2f,
            width / 2f - 100, mCirclePaint
        )
        //将扣空之后的阴影画布添加到原来的画布canvas中
        canvas.drawBitmap(mCircleBmp!!, 0f, 0f, bitmapPaint)
        saveBitmap =
            Bitmap.createBitmap(screenWidth.toInt(), screenHeight.toInt(), Bitmap.Config.ARGB_8888)
        saveCanvas = Canvas(saveBitmap!!)
        saveCanvas!!.drawBitmap(drawBitmap!!, drawBitmapLeft, drawBitmapTop, bitmapPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerCount = event.pointerCount
        performClick()
        if (pointerCount == POINTER_TWO) {
            //两根手指，缩放图像
            scaleBitmap(event)
        } else if (pointerCount == POINTER_ONE) {
            //一根手指，拖动图像
            dragBitmap(event)
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    /**
     * 保存头像按钮
     */
    fun saveUserIcon(): String {
        try {
            if (!drawBitmap!!.isRecycled) {
                val userId: String = mContext.getSharedPreferences("LoginConfig", MODE_PRIVATE)
                    .getString("UserId", null)!!
                val fileName = "HEAD-$userId.png"
                file = File(mContext.filesDir.toString() + "/" + fileName)
                val fos = FileOutputStream(file)
                val userIconBitmap = Bitmap.createBitmap(
                    saveBitmap!!,
                    100,
                    height / 2 - (width / 2 - 100),
                    (width / 2 - 100) * 2,
                    (width / 2 - 100) * 2
                )
                val bitmap = Bitmap.createScaledBitmap(userIconBitmap, 100, 100, false)
                bitmap.compress(Bitmap.CompressFormat.PNG, ONE_HUNDRED, fos)
                fos.flush()
                fos.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            mCircleBmp!!.recycle()
            drawBitmap!!.recycle()
        }
        return file!!.absolutePath
    }

    /**
     * 拖动图像，并绘制
     *
     * @param event 事件
     */
    private fun dragBitmap(event: MotionEvent) {
        if (isDrag) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    firstPointerDownX = event.x.toInt()
                    firstPointerDownY = event.y.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    //移动时手指的 X Y
                    val firstPointerMoveX = event.x.toInt()
                    val firstPointerMoveY = event.y.toInt()
                    //X Y 方向移动，用于判断手指是向哪个方向移动
                    val moveSizeX = firstPointerMoveX - firstPointerDownX
                    val moveSizeY = firstPointerMoveY - firstPointerDownY

                    //判断向左滑动还是向右滑动
                    if (moveSizeX <= 0) {
                        //向左滑动
                        if (drawBitmapLeft > 0) {
                            drawBitmapLeft = tempDrawBitmapLeft - abs(moveSizeX)
                        } else {
                            dragX = abs(abs(moveSizeX) + tempMoveSizeX)
                        }
                    } else {
                        //向右滑动
                        if (tempMoveSizeX - abs(moveSizeX) < 0) {
                            dragX = 0
                            drawBitmapLeft = tempDrawBitmapLeft + abs(moveSizeX)
                        } else {
                            dragX = tempMoveSizeX - abs(moveSizeX)
                        }
                    }

                    //判断向上滑动还是向下滑动
                    if (moveSizeY <= 0) {
                        //向左滑动
                        if (drawBitmapTop > 0) {
                            drawBitmapTop = tempDrawBitmapTop - abs(moveSizeY)
                        } else {
                            dragY = abs(abs(moveSizeY) + tempMoveSizeY)
                        }
                    } else {
                        //向右滑动
                        if (tempMoveSizeY - abs(moveSizeY) < 0) {
                            dragY = 0
                            drawBitmapTop = tempDrawBitmapTop + abs(moveSizeY)
                        } else {
                            dragY = tempMoveSizeY - abs(moveSizeY)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    tempMoveSizeX = dragX
                    tempMoveSizeY = dragY
                    tempDrawBitmapLeft = drawBitmapLeft
                    tempDrawBitmapTop = drawBitmapTop
                }
                else -> {
                }
            }
            createDrawBitmap()
        }
    }

    /**
     * 缩放图像，并绘制
     *
     * @param event 事件
     */
    private fun scaleBitmap(event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                isDrag = false
                //获取第二根手指按下时的 X Y 坐标
                val secondPointerDownX = event.getX(1)
                val secondPointerDownY = event.getY(1)
                //获取两根手指之间的距离
                downDiagonal = getSqrt(
                    firstPointerDownX.toFloat(),
                    firstPointerDownY.toFloat(),
                    secondPointerDownX,
                    secondPointerDownY
                )
            }
            MotionEvent.ACTION_MOVE -> {
                //获取手指移动时，两根手指之间的距离
                val moveDiagonal =
                    getSqrt(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
                //判断是扩大还是缩小
                scaleValue = if (moveDiagonal - downDiagonal > 0) {
                    //扩大
                    Arith.add(scaleValue, 0.1f)
                } else {
                    //缩小
                    if (scaleValue > ZERO_POINT_ONE) {
                        Arith.sub(scaleValue, 0.1f)
                    } else {
                        0.1f
                    }
                }
                downDiagonal = moveDiagonal
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_POINTER_UP -> isDrag = true
            else -> {
            }
        }
        createDrawBitmap()
    }

    private var x = 0
    private var y = 0
    private fun createDrawBitmap() {
        //绘制图像的参数
        val width: Int
        val height: Int
        //原图片的宽高
        val sourceBitmapWidth = sourceBitmap!!.width
        val sourceBitmapHeight = sourceBitmap!!.height
        //缩放后的图片宽高
        val scaledBitmapWidth = (sourceBitmapWidth * scaleValue).toInt()
        val scaledBitmapHeight = (sourceBitmapHeight * scaleValue).toInt()
        if (scaledBitmapWidth <= screenWidth) {
            x = 0
            width = sourceBitmapWidth
        } else {
            if (dragX + screenWidth >= scaledBitmapWidth) {
                width = (screenWidth / scaledBitmapWidth * sourceBitmapWidth).toInt()
                x = sourceBitmapWidth - width
            } else {
                width = (screenWidth / (dragX + screenWidth) * sourceBitmapWidth).toInt()
                x = sourceBitmapWidth - width
            }
        }
        if (scaledBitmapHeight <= screenHeight) {
            y = 0
            height = sourceBitmapHeight
        } else {
            if (dragY + screenHeight >= scaledBitmapHeight) {
                height = (screenHeight / scaledBitmapHeight * sourceBitmapHeight).toInt()
                y = sourceBitmapHeight - height
            } else {
                height = (screenHeight / (dragY + screenHeight) * sourceBitmapHeight).toInt()
                y = sourceBitmapHeight - height
            }
        }
        mMatrix.setScale(scaleValue, scaleValue)
        drawBitmap = Bitmap.createBitmap(sourceBitmap!!, x, y, width, height, mMatrix, false)
        invalidate()
    }

    /**
     * 获取两个手指之间的距离
     */
    private fun getSqrt(
        firstPointerDownX: Float,
        firstPointerDownY: Float,
        secondPointerDownX: Float,
        secondPointerDownY: Float
    ): Double {
        return sqrt(
            (abs(firstPointerDownX - secondPointerDownX) * abs(firstPointerDownX - secondPointerDownX)
                    +
                    abs(firstPointerDownY - secondPointerDownY) * abs(firstPointerDownY - secondPointerDownY)).toDouble()
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        coverRect = Rect(0, 0, width, height)
    }

    companion object {
        /**
         * 魔法值，2跟手指
         */
        private const val POINTER_ONE = 1
        private const val POINTER_TWO = 2
        private const val ZERO_POINT_ONE = 0.1f
        private const val ONE_HUNDRED = 100
    }

    init {
        init()
    }
}
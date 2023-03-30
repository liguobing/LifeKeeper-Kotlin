package com.lixyz.lifekeeperforkotlin.view.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout

/**
 * 通话录音 View
 */
class CallRecordingView : RelativeLayout {

    private var mContext: Context? = null

    private var mAttributeSet: AttributeSet? = null

    private var progress: Float = 0F

    var isPlaying: Boolean = true

    private var paint = Paint()

    constructor(context: Context) : super(context) {
        this.mContext = context
        initParams()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        this.mContext = context
        this.mAttributeSet = attributeSet
        initParams()
    }

    private fun initParams() {
        setWillNotDraw(false)
    }

    interface OnSeekBarChangeListener {
        fun onProgressChanged(seekBar: CallRecordingView?, progress: Float, fromUser: Boolean)
    }

    var onSeekBarListener: OnSeekBarChangeListener? = null

    fun setOnSeekBarChangeListener(listener: OnSeekBarChangeListener) {
        this.onSeekBarListener = listener
    }

    fun setProgress(progress: Float) {
        if (progress > 1) {
            this.progress = 1f
        } else {
            this.progress = progress
        }
        invalidate()
    }

    fun getProgress(): Float {
        return progress
    }

    private var progressWidth: Float = 0F

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        if (isPlaying) {
//            when (event!!.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    progressWidth = event.x
//                    progress = event.x / measuredWidth
//                    if (onSeekBarListener != null) {
//                        onSeekBarListener!!.onProgressChanged(this, progress, false)
//                    }
//                    invalidate()
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    progressWidth = event.x
//                    progress = event.x / measuredWidth
//                    if (onSeekBarListener != null) {
//                        onSeekBarListener!!.onProgressChanged(this, progress, false)
//                    }
//                    invalidate()
//                }
//                MotionEvent.ACTION_UP -> {
//                    progressWidth = event.x
//                    progress = event.x / measuredWidth
//                    if (onSeekBarListener != null) {
//                        onSeekBarListener!!.onProgressChanged(this, progress, false)
//                    }
//                    invalidate()
//                }
//            }
//            return true
//        } else {
//            return false
//        }
//    }

    private var rect: RectF? = null

    private fun createRect(): RectF {
        if (rect == null) {
            rect = RectF()
        }
        rect!!.left = 0f
        rect!!.top = 0f
        rect!!.bottom = measuredHeight.toFloat()
        rect!!.right = measuredWidth * progress
        return rect!!
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isPlaying) {
            paint.color = Color.GREEN
            canvas!!.drawRect(createRect(), paint)
        }
    }
}
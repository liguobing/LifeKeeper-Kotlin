package com.lixyz.lifekeeperforkotlin.view.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.EditText


/**
 * @author LGB
 */
class CustomEditText : EditText {
    private var mLeftListener: OnDrawableLeftListener? = null
    private var mRightListener: OnDrawableRightListener? = null
    private var drawableListener: OnDrawableListener? = null
    val DRAWABLE_LEFT = 0
    val DRAWABLE_TOP = 1
    val DRAWABLE_RIGHT = 2
    val DRAWABLE_BOTTOM = 3

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?) : super(context!!) {}

    fun setOnDrawableLeftListener(listener: OnDrawableLeftListener?) {
        mLeftListener = listener
    }

    fun setOnDrawableRightListener(listener: OnDrawableRightListener?) {
        mRightListener = listener
    }

    fun setOnDrawableClickListener(listener: OnDrawableListener?) {
        drawableListener = listener
    }

    interface OnDrawableListener {
        fun onDrawableLeftClick(view: View?)
        fun onDrawableRightClick(view: View?)
    }

    interface OnDrawableLeftListener {
        fun onDrawableLeftClick(view: View?)
    }

    interface OnDrawableRightListener {
        fun onDrawableRightClick(view: View?)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (mRightListener != null) {
                    val drawableRight = compoundDrawables[DRAWABLE_RIGHT]
                    if (drawableRight != null && event.rawX >= right - drawableRight.bounds.width()) {
                        mRightListener!!.onDrawableRightClick(this)
                        return true
                    }
                }
                if (drawableListener != null) {
                    val drawableRight = compoundDrawables[DRAWABLE_RIGHT]
                    if (drawableRight != null && event.rawX >= right - drawableRight.bounds.width()) {
                        drawableListener!!.onDrawableRightClick(this)
                        return true
                    }
                    val drawableLeft = compoundDrawables[DRAWABLE_LEFT]
                    if (drawableLeft != null && event.rawX <= left + drawableLeft.bounds.width()) {
                        drawableListener!!.onDrawableLeftClick(this)
                        return true
                    }
                }
                if (mLeftListener != null) {
                    val drawableLeft = compoundDrawables[DRAWABLE_LEFT]
                    if (drawableLeft != null && event.rawX <= left + drawableLeft.bounds.width()) {
                        mLeftListener!!.onDrawableLeftClick(this)
                        return true
                    }
                }
            }
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }
}
package com.lixyz.lifekeeperforkotlin.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager


/**
 * @author LGB
 * 禁止滑动的 ViewPager
 */
class ViewPagerSlide : ViewPager {
    /**
     * 是否可以进行滑动
     */
    private var isSlide = false


    fun setSlide(slide: Boolean) {
        isSlide = slide
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isSlide
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return false
    }
}
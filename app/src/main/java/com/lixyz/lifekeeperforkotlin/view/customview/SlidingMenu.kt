package com.lixyz.lifekeeperforkotlin.view.customview

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.adapter.PlanListPlanRecyclerViewAdapter
import kotlin.math.abs


/**
 * 滑动菜单
 * 用于计划列表 Recycler Item
 *
 * @author LGB
 */
class SlidingMenu(context: Context, attrs: AttributeSet?) :
    HorizontalScrollView(context, attrs) {
    private var mScreenWidth = 0
    private var mMenuWidth = 0
    private var once = true
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (once) {
            //获取包含内容和菜单的跟布局
            val rootLayout = getChildAt(0) as LinearLayout
            //获取内容布局
            val contentLayout = rootLayout.getChildAt(0) as RelativeLayout
            //设置内容布局宽度为全屏 - 两边的 margin
            contentLayout.layoutParams.width =
                MeasureSpec.getSize(widthMeasureSpec) - contentLayout.marginStart - contentLayout.marginEnd
            //获取菜单布局
            val menuLayout = rootLayout.getChildAt(1) as RelativeLayout
            //菜单宽度等于菜单布局宽度 + marginEnd
            val childCount = menuLayout.childCount
            var childrenWidth = 0
            var childrenMarginStart = 0
            var childrenMarginEnd = 0
            for (i in 0 until childCount) {
                childrenWidth += menuLayout.getChildAt(i).layoutParams.width
                childrenMarginStart +=  menuLayout.getChildAt(i).marginStart
                childrenMarginEnd +=  menuLayout.getChildAt(i).marginEnd
            }
            mMenuWidth = childrenWidth + childrenMarginStart + childrenMarginEnd + contentLayout.marginEnd
            once = false
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        performClick()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                closeOpenMenu()
            }
            MotionEvent.ACTION_UP -> {
                val scrollX = scrollX
                if (abs(scrollX) >= mMenuWidth) {
                    smoothScrollTo(mMenuWidth, 0)
                    onOpenMenu()
                } else {
                    smoothScrollTo(0, 0)
                }
                return true
            }
            MotionEvent.ACTION_HOVER_MOVE -> {
            }
            else -> {
            }
        }
        return super.onTouchEvent(ev)
    }

    /**
     * 菜单是否打开
     */
    var isOpen = false

    /**
     * 关闭菜单
     */
    fun closeMenu() {
        smoothScrollTo(0, 0)
        isOpen = false
    }

    /**
     * 当打开菜单时记录此 view ，方便下次关闭
     */
    private fun onOpenMenu() {
        var view: View = this
        do {
            view = view.parent as View
        } while (view !is RecyclerView)
        if (view.adapter is PlanListPlanRecyclerViewAdapter) {
            val adapter = view.adapter as PlanListPlanRecyclerViewAdapter?
            adapter?.holdOpenMenu(this)
        }
        isOpen = true
    }

    /**
     * 当触摸此 item 时，关闭上一次打开的 item
     */
    private fun closeOpenMenu() {
        if (!isOpen) {
            var view: View = this
            do {
                view = view.parent as View
            } while (view !is RecyclerView)
            if (view.adapter is PlanListPlanRecyclerViewAdapter) {
                val adapter = view.adapter as PlanListPlanRecyclerViewAdapter?
                adapter?.closeOpenMenu()
            }
        }
    }

    companion object {
        /**
         * 菜单占屏幕宽度比
         */
        private const val RADIO = 0.5f
    }

    init {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        mScreenWidth = size.x
        mMenuWidth = (mScreenWidth * RADIO).toInt()
        overScrollMode = View.OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
    }
}
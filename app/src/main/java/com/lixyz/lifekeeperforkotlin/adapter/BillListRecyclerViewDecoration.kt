package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import java.text.DecimalFormat


/**
 * Bill ListView Item 吸顶装饰
 *
 * @author LGB
 */
class BillListRecyclerViewDecoration(context: Context) : ItemDecoration() {
    /**
     * Context
     */
    private val mContext: Context

    /**
     * Item Title 高度
     */
    private val mTitleHeight: Int

    /**
     * 绘制 Item Title 的画笔
     */
    private val mPaint: Paint

    /**
     * 绘制 Title 文字的画笔
     */
    private val mTextPaint: Paint

    /**
     * Item 日期文字 Rect
     */
    private val dateRect: Rect

    /**
     * 绘制分割线的画笔
     */
    private val mGrayPaint: Paint

    /**
     * Item 金额文字 Rect
     */
    private val moneyRect: Rect

    /**
     * 这个方法用于给item隔开距离，类似直接给item设padding
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        val adapter = parent.adapter as BillListRecyclerViewAdapter? ?: return
        if (position == 0 || adapter.isItemHeader(position)) {
            outRect.top = mTitleHeight
        } else {
            outRect.top = 1
        }
    }

    /**
     * 这个方法用于给getItemOffsets()隔开的距离填充图形,
     * 在item绘制之前时被调用，将指定的内容绘制到item view内容之下；
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        // 获取当前屏幕可见 item 数量，而不是 RecyclerView 所有的 item 数量
        val childCount = parent.childCount
        val left = parent.paddingLeft.toFloat()
        val right = (parent.width - parent.paddingRight).toFloat()
        val adapter = parent.adapter as BillListRecyclerViewAdapter?
        for (i in 0 until childCount) {
            val view: View = parent.getChildAt(i)
            val params = view
                .layoutParams as RecyclerView.LayoutParams
            val position = params.viewLayoutPosition
            if (adapter == null) {
                return
            }
            if (position == 0 || adapter.isItemHeader(position)) {
                val top: Float = (view.top - mTitleHeight).toFloat()
                val bottom: Float = view.top.toFloat()
                canvas.drawRect(left, top, right, bottom, mPaint)
                val date = adapter.getDate(position)
                mTextPaint.getTextBounds(date, 0, date.length, dateRect)
                val x: Float = (view.paddingLeft + dp2px(mContext, 21f)).toFloat()
                val y: Float = top + (mTitleHeight - dateRect.height()) / 2f + dateRect.height()
                canvas.drawText(date, x, y, mTextPaint)
                val moneyCount = adapter.getMoneyCount(position)
                val format = DecimalFormat("0.00")
                val income = moneyCount[1]
                val expend = moneyCount[-1]
                val incomeStr: String = format.format(income)
                val expendStr: String = format.format(expend)
                val money = "收入：$incomeStr   支出：$expendStr"
                mTextPaint.getTextBounds(money, 0, money.length, moneyRect)
                val i1: Int = view.width - moneyRect.right - dp2px(mContext, 20f)
                canvas.drawText(money, i1.toFloat(), y, mTextPaint)
            } else {
                val top: Float = (view.top - 1).toFloat()
                val bottom: Float = view.top.toFloat()
                canvas.drawRect(left, top, right, bottom, mGrayPaint)
            }
        }
    }


    /**
     * 在item被绘制之后调用，将指定的内容绘制到item view内容之上
     * 这个方法可以将内容覆盖在item上，可用于制作悬停效果，角标等（这里只实现悬停效果）
     */
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val layoutManager = parent.layoutManager
        if (layoutManager != null) {
            val position = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val adapter = parent.adapter as BillListRecyclerViewAdapter?
            if (adapter != null) {
                if (position <= -1 || position >= adapter.itemCount - 1) {
                    // 越界检查
                    return
                }
                val viewHolder = parent.findViewHolderForAdapterPosition(position)
                if (viewHolder != null) {
                    val firstVisibleView: View = viewHolder.itemView
                    val left = parent.paddingLeft
                    val right = parent.width - parent.paddingRight
                    var top = parent.paddingTop
                    var bottom = top + mTitleHeight
                    // 如果当前屏幕上第二个显示的item是下一组的的第一个，并且第一个被title覆盖，则开始移动上个title。
                    // 原理就是不断改变title所在矩形的top与bottom的值。
                    if (adapter.isItemHeader(position + 1) && firstVisibleView.bottom < mTitleHeight) {
                        top = if (mTitleHeight <= firstVisibleView.height) {
                            val d: Int = firstVisibleView.height - mTitleHeight
                            firstVisibleView.top + d
                        } else {
                            val d: Int = mTitleHeight - firstVisibleView.height
                            // 这里有bug,mTitleHeight过高时 滑动有问题
                            firstVisibleView.top - d
                        }
                        bottom = firstVisibleView.bottom
                    }
                    c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
                    val date = adapter.getDate(position)
                    mTextPaint.getTextBounds(date, 0, date.length, dateRect)
                    val x: Float = (left + firstVisibleView.paddingLeft + dp2px(mContext, 21f)).toFloat()
                    val y: Float = top + (mTitleHeight - dateRect.height()) / 2f + dateRect.height()
                    c.drawText(date, x, y, mTextPaint)
                    val moneyCount = adapter.getMoneyCount(position)
                    val income = moneyCount[1]
                    val expend = moneyCount[-1]
                    val format = DecimalFormat("0.00")
                    val incomeStr: String = format.format(income)
                    val expendStr: String = format.format(expend)
                    val money = "收入：$incomeStr   支出：$expendStr"
                    mTextPaint.getTextBounds(money, 0, money.length, moneyRect)
                    val i1: Int =
                        firstVisibleView.width - moneyRect.right - dp2px(mContext, 20f)
                    c.drawText(money, i1.toFloat(), y, mTextPaint)
                }
            }
        }
    }

    /**
     * dp转换成px
     */
    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    init {
        mContext = context
        mTitleHeight = dp2px(context, 30f)
        mTextPaint = Paint()
        mTextPaint.textSize = dp2px(context, 10f).toFloat()
        mTextPaint.isAntiAlias = true
        mTextPaint.color = Color.BLACK
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.color = Color.parseColor("#F5F5F5")
        mGrayPaint = Paint()
        mGrayPaint.isAntiAlias = true
        mGrayPaint.color = Color.GRAY
        dateRect = Rect()
        moneyRect = Rect()
    }
}
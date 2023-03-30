package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanListDateMenuItemBean


/**
 * 计划列表页面，日期菜单 RecyclerView Adapter
 *
 * @author LGB
 */
class PlanListDateMenuRecyclerAdapter(context: Context, list: ArrayList<PlanListDateMenuItemBean>) :
    RecyclerView.Adapter<PlanListDateMenuRecyclerAdapter.MyHolder>() {
    /**
     * 数据 List
     */
    private val list: ArrayList<PlanListDateMenuItemBean>

    /**
     * Context
     */
    private val context: Context

    /**
     * Item 点击监听器
     */
    private var mOnItemClickListener: OnItemClickListener? = null


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view___plan_list___day_menu___item, viewGroup, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(myHolder: MyHolder, i: Int) {
        myHolder.tvDay.text = "${list[i].day}"
        myHolder.tvWeek.text = list[i].week
        if (list[i].isClick) {
            myHolder.itemView.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.plan_list___day_menu___item_click_background,
                    null
                )
        } else {
            myHolder.itemView.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.plan_list___day_menu___item_unclick_background,
                    null
                )
        }

        //判断是否设置了监听器
        if (mOnItemClickListener != null) {
            //为ItemView设置监听器
            myHolder.itemView.setOnClickListener {
                val position = myHolder.layoutPosition // 1
                val arr = IntArray(2)
                myHolder.itemView.getLocationInWindow(arr)
                mOnItemClickListener!!.onItemClick(position, arr[0], arr[1]) // 2
            }
        }
    }

    /**
     * 对外接口，设置 Item Click Listener
     */
    interface OnItemClickListener {
        /**
         * Item Click
         *
         * @param position 下标
         */
        fun onItemClick(position: Int, x: Int, y: Int)
    }

    /**
     * 设置 Item Click Listener
     *
     * @param mOnItemClickListener Listener
     */
    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvWeek: TextView
        var tvDay: TextView

        init {
            tvWeek = itemView.findViewById(R.id.tv_week)
            tvDay = itemView.findViewById(R.id.tv_day)
        }
    }

    init {
        this.context = context
        this.list = list
    }
}
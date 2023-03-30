package com.lixyz.lifekeeperforkotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.billchart.BillChartItemBean
import java.util.*


class BillChartMonthMenuYearRecyclerViewAdapter(private val data: ArrayList<BillChartItemBean>) :
    RecyclerView.Adapter<BillChartMonthMenuYearRecyclerViewAdapter.MyViewHolder>() {
    /**
     * Item 点击监听器
     */
    private var mOnItemClickListener: BillCategoryRecyclerViewAdapter.OnItemClickListener? = null


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view___bill_chart___month_menu_item, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, i: Int) {
        holder.tvYear.text = String.format(Locale.CHINA, "%d年", data[i].itemName)
        if (data[i].isClick) {
            holder.imgIsCheck.setImageResource(R.drawable.bill_chart___month_menu___item_click)
        } else {
            holder.imgIsCheck.setImageResource(0)
        }

        //判断是否设置了点击监听器
        if (mOnItemClickListener != null) {
            //为ItemView设置监听器
            holder.itemView.setOnClickListener {
                val position = holder.layoutPosition // 1
                mOnItemClickListener!!.onItemClick(position) // 2
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
        fun onItemClick(position: Int)
    }

    /**
     * 设置 Item Click Listener
     *
     * @param mOnItemClickListener Listener
     */
    fun setOnItemClickListener(mOnItemClickListener: BillCategoryRecyclerViewAdapter.OnItemClickListener?) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvYear: TextView
        val imgIsCheck: ImageView

        init {
            tvYear = itemView.findViewById(R.id.tv_item_name)
            imgIsCheck = itemView.findViewById(R.id.img_click)
        }
    }
}
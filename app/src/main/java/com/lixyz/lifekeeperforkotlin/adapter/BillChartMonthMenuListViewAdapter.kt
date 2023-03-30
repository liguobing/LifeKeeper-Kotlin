package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.lixyz.lifekeeperforkotlin.R


/**
 * 账单图表，MonthMenu 当中 月份 GridView Adapter
 *
 * @author LGB
 */
class BillChartMonthMenuListViewAdapter(context: Context, list: ArrayList<String>) :
    BaseAdapter() {
    /**
     * Context
     */
    private val context: Context

    /**
     * 数据 List
     */
    private val list: ArrayList<String>
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, v: View?, parent: ViewGroup?): View? {
        var convertView: View? = v
        val holder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.view___bill_chart___month_menu___grid_view_item, parent, false)
            holder = ViewHolder()
            holder.textView = convertView.findViewById(R.id.text)
            convertView.tag = holder
        } else {
            holder = convertView.getTag() as ViewHolder
        }
        holder.textView!!.text = list[position]
        return convertView
    }

    class ViewHolder {
        var textView: TextView? = null
    }

    init {
        this.context = context
        this.list = list
    }
}
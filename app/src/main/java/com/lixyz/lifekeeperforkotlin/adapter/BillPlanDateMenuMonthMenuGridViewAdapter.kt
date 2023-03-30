package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.lixyz.lifekeeperforkotlin.R


/**
 * 计划列表，日期菜单中月份菜单 GridView Adapter
 *
 * @author LGB
 */
class BillPlanDateMenuMonthMenuGridViewAdapter(context: Context, arr: Array<String>) :
    BaseAdapter() {
    /**
     * Context
     */
    private val context: Context

    /**
     * 数据 List
     */
    private val arr: Array<String>
    override fun getCount(): Int {
        return arr.size
    }

    override fun getItem(position: Int): Any {
        return arr[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, v: View?, parent: ViewGroup?): View {
        var convertView: View? = v
        val holder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.view___bill_chart___month_menu___grid_view_item, parent, false)
            holder = ViewHolder()
            holder.textView = convertView.findViewById(R.id.text)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.textView!!.text = arr[position]
        return convertView!!
    }

    class ViewHolder {
        var textView: TextView? = null
    }

    init {
        this.context = context
        this.arr = arr
    }
}
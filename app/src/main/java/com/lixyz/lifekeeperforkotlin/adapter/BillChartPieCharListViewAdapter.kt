package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.utils.StringUtil


/**
 * 账单图表页面，圆形图表 ListView Adapter
 *
 * @author LGB
 */
class BillChartPieCharListViewAdapter(
    /**
     * 账单数据 List
     */
    private val bills: ArrayList<BillBean>, private var context: Context?
) :
    BaseAdapter() {

    override fun getCount(): Int {
        return bills.size
    }

    override fun getItem(position: Int): Any {
        return bills[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, v: View?, parent: ViewGroup?): View {
        var convertView: View? = v
        val holder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.view___bill_chart___pie_chart___list_view___item,
                parent,
                false
            )
            holder = ViewHolder()
            holder.date = convertView.findViewById(R.id.tv_bill_date)
            holder.money = convertView.findViewById(R.id.tv_bill_money)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.date!!.text = StringUtil.milliToString(bills[position].billDate, false)
        holder.money!!.text = String.format("￥%s", bills[position].billMoney)
        return convertView!!
    }

    internal class ViewHolder {
        var money: TextView? = null
        var date: TextView? = null
    }
}
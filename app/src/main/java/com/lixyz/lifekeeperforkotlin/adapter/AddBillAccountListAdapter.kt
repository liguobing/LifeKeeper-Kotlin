package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount

class AddBillAccountListAdapter(
    private var context: Context,
    private var list: ArrayList<BillAccount>
) : BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view___add_bill___account_list_item, parent, false)
        if (position % 2 == 0) {
            view.setBackgroundColor(Color.parseColor("#E4E2E2"))
        }
        val accountName = view.findViewById<TextView>(R.id.tv_account_name)
        accountName.text = list[position].accountName
        return view
    }
}
package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean

class ShowImageDetailMoveImageCategoryListAdapter(private var context: Context, private var list: ArrayList<ImageCategoryBean>) :
    BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val viewHolder: ViewHolder
        val rowView: View?

        if (view == null) {
            rowView = LayoutInflater.from(context)
                .inflate(R.layout.view___show_image_detail___move_image___list_view_item, viewGroup, false)
            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder
        } else {
            rowView = view
            viewHolder = rowView.tag as ViewHolder
        }
        if (position % 2 == 1) {
            viewHolder.itemName.setBackgroundColor(Color.parseColor("#ffffff"))
        } else {
            viewHolder.itemName.setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        viewHolder.itemName.text = list[position].categoryName

        return rowView!!
    }

    class ViewHolder(view: View?) {
        val itemName = view?.findViewById(R.id.text) as TextView
    }
}
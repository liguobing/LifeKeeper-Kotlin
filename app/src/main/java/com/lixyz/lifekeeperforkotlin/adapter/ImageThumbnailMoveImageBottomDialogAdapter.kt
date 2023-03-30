package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean

class ImageThumbnailMoveImageBottomDialogAdapter(
    private var context: Context,
    private var categoryList: ArrayList<ImageCategoryBean>
) : BaseAdapter() {
    override fun getCount(): Int {
        return categoryList.size
    }

    override fun getItem(position: Int): Any {
        return categoryList[position]
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.view___move_image_to_another_category, parent, false)
        val categoryName: TextView = view.findViewById(R.id.tv_category_name)
        categoryName.text = categoryList[position].categoryName
        if(position == categoryList.size - 1){
            categoryName.setTextColor(Color.RED)
        }
        if (categoryList[position].isPrivate > 0) {
            categoryName.setCompoundDrawables(
                null,
                null,
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.image_category_private,
                    null
                ),
                null
            )
        }
        return view
    }
}
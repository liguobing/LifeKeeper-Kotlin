package com.lixyz.lifekeeperforkotlin.adapter

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.SelectFileBean

class ImageCategorySelectFileBottomDialogAdapter(
    private var context: Context,
    private var list: ArrayList<SelectFileBean>
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
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.view___image_category_select_file_botton_dialog_item, parent, false)
        val img: ImageView = view.findViewById(R.id.img)
        val check:CheckBox = view.findViewById(R.id.cb_check)
//        fileName.text = list[position].fileName
        val uri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            list[position].id.toLong()
        )
//        Glide.with(context).load(uri).override(300).into(img)
        Glide.with(context).load(uri).override(300).centerCrop().into(img)
//        img.setImageURI(uri)
        check.isChecked = list[position].checked
        return view
    }
}
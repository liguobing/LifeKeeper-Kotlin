package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.SelectFileBean

class CallRecordSelectFileBottomDialogAdapter(
    private var context: Context,
    private var fileNameList: ArrayList<SelectFileBean>
) : BaseAdapter() {
    override fun getCount(): Int {
        return fileNameList.size
    }

    override fun getItem(position: Int): Any {
        return fileNameList[position]
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.view___call_record_select_file_botton_dialog_item, parent, false)
        val fileName: TextView = view.findViewById(R.id.tv_file_name)
        val check:CheckBox = view.findViewById(R.id.cb_check)
        fileName.text = fileNameList[position].fileName
        check.isChecked = fileNameList[position].checked
        return view
    }
}
package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.phonerecord.RecordRecyclerViewItemBean

interface IPhoneRecordView {

    fun updatePhoneRecordRecyclerView(
        dataList: ArrayList<RecordRecyclerViewItemBean>,
        progressList: ArrayList<Float>
    )

    fun showWaitDialog()

    fun updateWaitDialog(message: String)

    fun hideWaitDialog()

    fun showSnackBar(message: String)

    fun removeDeleteRecord(removeList: ArrayList<RecordRecyclerViewItemBean>)

    fun updateRecyclerViewItem(position: Int)
}
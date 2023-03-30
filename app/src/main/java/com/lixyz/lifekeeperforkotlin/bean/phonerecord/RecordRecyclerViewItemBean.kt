package com.lixyz.lifekeeperforkotlin.bean.phonerecord

import java.io.Serializable

class RecordRecyclerViewItemBean: Serializable {
    var id: String? = null
    var sha1: String? = null
    var contactName: String? = null
    var contactNum: String? = null
    var callTime: Long = 0
    var inOrOut = 0
    var fileName: String? = null
    var createTime: Long = 0
    var checkBoxIsVisibility: Boolean? = null
    var checked: Boolean? = null
    var progress: Float = 0f
}
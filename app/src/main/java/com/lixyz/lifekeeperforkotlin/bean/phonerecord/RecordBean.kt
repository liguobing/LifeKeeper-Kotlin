package com.lixyz.lifekeeperforkotlin.bean.phonerecord

import java.io.Serializable

class RecordBean : Serializable {
    var objectId: String? = null
    var recordId: String? = null
    var sha1: String? = null
    var contactId: String? = null
    var callTime: String ?=null
    var inOrOut = 0
    var sourceFileName: String? = null
    var originalFileName: String? = null
    var recordUser: String? = null
    var recordStatus = 0
    var createTime: Long = 0
}
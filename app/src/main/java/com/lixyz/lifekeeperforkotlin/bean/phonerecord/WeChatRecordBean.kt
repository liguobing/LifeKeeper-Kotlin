package com.lixyz.lifekeeperforkotlin.bean.phonerecord

data class WeChatRecordBean(
    val objectId: String,
    val recordId: String,
    val sha1: String,
    val contactId: String,
    val callTime: String,
    val sourceFileName: String,
    val originalFileName: String,
    val recordUser: String,
    val recordStatus: Int,
    val createTime: Long
)

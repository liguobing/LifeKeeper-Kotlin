package com.lixyz.lifekeeperforkotlin.bean.phonerecord

import java.io.Serializable

data class ContactBean(
    var objectId: String,
    var contactId: String,
    var contactName: String,
    var phoneNumber: String,
    var contactUser: String
): Serializable
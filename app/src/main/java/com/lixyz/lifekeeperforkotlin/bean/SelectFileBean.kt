package com.lixyz.lifekeeperforkotlin.bean

import java.io.Serializable

data class SelectFileBean(var id: String, var fileName: String, var checked: Boolean) :
    Serializable {

}
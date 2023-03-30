package com.lixyz.lifekeeperforkotlin.bean

import java.io.Serializable

class NewResult: Serializable {
    var result = false
    var exceptionMessage: String? = null
    var exceptionObject: Any? = null
    var resultObject: Any? = null
}
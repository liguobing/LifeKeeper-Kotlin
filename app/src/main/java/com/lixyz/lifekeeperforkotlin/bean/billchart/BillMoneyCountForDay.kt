package com.lixyz.lifekeeperforkotlin.bean.billchart

import java.io.Serializable

class BillMoneyCountForDay : Serializable {
    var date: String? = null
    var income = 0.0
    var expend = 0.0
}
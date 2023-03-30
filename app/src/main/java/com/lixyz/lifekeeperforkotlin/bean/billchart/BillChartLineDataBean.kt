package com.lixyz.lifekeeperforkotlin.bean.billchart

import java.io.Serializable

class BillChartLineDataBean: Serializable {
    var type: Int = 0
    var incomeList: ArrayList<Float>? = null
    var expendList: ArrayList<Float>? = null
}
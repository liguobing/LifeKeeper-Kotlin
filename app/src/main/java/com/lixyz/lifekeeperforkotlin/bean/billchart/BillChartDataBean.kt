package com.lixyz.lifekeeperforkotlin.bean.billchart

import java.io.Serializable

class BillChartDataBean: Serializable {
    var start: Long = 0
    var end:Long = 0
    var income:Double = 0.0
    var expend:Double = 0.0
    var incomeCategoryData:ArrayList<BillMoneyCountForCategoryGroup>?=null
    var expendCategoryData:ArrayList<BillMoneyCountForCategoryGroup?>? = null
    var billMoneyCountForDays: ArrayList<BillMoneyCountForDay>? = null
}
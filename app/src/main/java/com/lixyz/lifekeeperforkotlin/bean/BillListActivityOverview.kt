package com.lixyz.lifekeeperforkotlin.bean

import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import java.io.Serializable

/**
 * BillListActivity 上半部分 UI 数据
 *
 * @author LGB
 */
class BillListActivityOverview: Serializable {
    var year: Int? = null
    var month: Int? = null
    var incomeCount: Double? = null
    var expendCount: Double? = null
    var bills: ArrayList<BillBean>? = null
}
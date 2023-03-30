package com.lixyz.lifekeeperforkotlin.bean.billchart

import java.io.Serializable

class BillMoneyCountForCategoryGroup: Serializable {
    var billCategory: String? = null
    var moneyCount = 0.0F
    var isIncome = 0
}
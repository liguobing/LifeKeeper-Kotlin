package com.lixyz.lifekeeperforkotlin.bean

import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount
import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory
import java.io.Serializable


class BillCategoryAndBillAccount: Serializable {
    var billAccounts: ArrayList<BillAccount>? = null
    var allBillCategories: ArrayList<BillCategory>? = null
    var incomeBillCategories: ArrayList<BillCategory>? = null
    var expendBillCategories: ArrayList<BillCategory>? = null
}
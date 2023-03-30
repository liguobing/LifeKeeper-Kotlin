package com.lixyz.lifekeeperforkotlin.bean.overview

import com.lixyz.lifekeeperforkotlin.bean.UserBean
import java.io.Serializable

class Overview : Serializable {
    var userBean: UserBean? = null
    var planCountOfDay: Int = 0
    var planCountOfMonth: Int = 0
    var incomeCount: Double = 0.0
    var expendCount: Double = 0.0
    var fileCount: Int = 0
}
package com.lixyz.lifekeeperforkotlin.bean.billshop

import java.io.Serializable

class ShopResult: Serializable {
    var shopCount: Int = 0
    var offset: Int = 0
    var shopNames: ArrayList<String>? = null
}
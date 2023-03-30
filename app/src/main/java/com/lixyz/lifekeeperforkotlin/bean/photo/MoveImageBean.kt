package com.lixyz.lifekeeperforkotlin.bean.photo

import java.io.Serializable

class MoveImageBean: Serializable {
    var targetCategoryId: String? = null
    var images: ArrayList<String>? = null
}
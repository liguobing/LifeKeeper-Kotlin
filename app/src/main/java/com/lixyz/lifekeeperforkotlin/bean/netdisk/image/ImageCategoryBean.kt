package com.lixyz.lifekeeperforkotlin.bean.netdisk.image

import java.io.Serializable

class ImageCategoryBean : Serializable {
    var objectId: String? = null
    var categoryId: String? = null
    var categoryName: String? = null
    var categoryUser: String? = null
    var isPrivate = 0
    var categoryStatus = 0
    var categoryType = 0
    var password: String? = null
    var createTime: Long = 0
    var updateTime: Long = 0
}
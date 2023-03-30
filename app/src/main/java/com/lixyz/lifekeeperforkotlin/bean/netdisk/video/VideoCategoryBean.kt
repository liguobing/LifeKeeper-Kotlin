package com.lixyz.lifekeeperforkotlin.bean.netdisk.video

import java.io.Serializable

class VideoCategoryBean : Serializable {
    var objectId: String? = null
    var categoryId: String? = null
    var categoryName: String? = null
    var categoryUser: String? = null
    var isPrivate:Int = 0
    var password: String? = null
    var categoryStatus:Int = 0
    var categoryType:Int = 0
    var createTime: Long = 0
    var updateTime: Long = 0


}
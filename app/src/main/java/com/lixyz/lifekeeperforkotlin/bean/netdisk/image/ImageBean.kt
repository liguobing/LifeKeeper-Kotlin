package com.lixyz.lifekeeperforkotlin.bean.netdisk.image

import java.io.Serializable

class ImageBean : Serializable {
    var objectId: String? = null
    var imageId: String? = null
    var sha1: String? = null
    var fileCategory: String? = null
    var originalFileName: String? = null
    var sourceFileName: String? = null
    var thumbnailFileName: String? = null
    var coverFileName: String? = null
    var blurFileName: String? = null
    var createTime: Long = 0
    var imageUser: String? = null
    var imageStatus = 0
    var imageType = 1
    var updateTime: Long = 0
    var meatTime: Long = 0
    var yearMonth: String?=null
}
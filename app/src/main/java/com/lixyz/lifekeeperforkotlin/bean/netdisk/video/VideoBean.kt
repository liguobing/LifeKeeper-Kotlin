package com.lixyz.lifekeeperforkotlin.bean.netdisk.video

import java.io.Serializable

class VideoBean : Serializable {
    var objectId: String? = null
    var videoId: String? = null
    var sha1: String? = null
    var duration: Long = 0
    var fileCategory: String? = null
    var originalFileName: String? = null
    var sourceFileName: String? = null
    var coverFileName: String? = null
    var thumbnailFileName: String? = null
    var blurFileName: String? = null
    var videoUser: String? = null
    var videoStatus = 0
    var createTime: Long = 0
}
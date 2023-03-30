package com.lixyz.lifekeeperforkotlin.bean.netdisk.video

import java.io.Serializable


class MoveVideoBean : Serializable {
    var videos: ArrayList<String>? = null
    var targetCategoryId: String? = null
}
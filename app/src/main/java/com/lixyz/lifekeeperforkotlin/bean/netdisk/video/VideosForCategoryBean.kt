package com.lixyz.lifekeeperforkotlin.bean.netdisk.video

import java.io.Serializable

class VideosForCategoryBean : Serializable {
    var category: VideoCategoryBean? = null

    var videos: ArrayList<VideoBean>? = null
}
package com.lixyz.lifekeeperforkotlin.bean.pass

import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.ShowVideoItemBean
import java.io.Serializable

class VideoThumbnailToShowVideoDetail : Serializable {
    var position: Int? = null
    var dataList: ArrayList<ShowVideoItemBean>? = null
}
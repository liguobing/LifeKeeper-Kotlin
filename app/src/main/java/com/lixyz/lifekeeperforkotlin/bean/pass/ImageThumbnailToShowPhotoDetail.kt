package com.lixyz.lifekeeperforkotlin.bean.pass

import com.lixyz.lifekeeperforkotlin.bean.ShowPhotoItemBean
import java.io.Serializable

class ImageThumbnailToShowPhotoDetail : Serializable {
    var position: Int? = null
    var dataList: ArrayList<ShowPhotoItemBean>? = null
}
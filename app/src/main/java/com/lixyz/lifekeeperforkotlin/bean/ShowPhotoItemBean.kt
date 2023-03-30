package com.lixyz.lifekeeperforkotlin.bean

import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import java.io.Serializable

class ShowPhotoItemBean : Serializable {
    var image: ImageBean? = null
    var checked: Boolean? = null
    var checkViewIsShow: Boolean? = null
}
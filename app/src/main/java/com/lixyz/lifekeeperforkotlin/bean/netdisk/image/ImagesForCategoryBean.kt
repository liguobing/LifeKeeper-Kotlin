package com.lixyz.lifekeeperforkotlin.bean.netdisk.image

import java.io.Serializable

class ImagesForCategoryBean : Serializable {
    var category: ImageCategoryBean? = null

    var images: ArrayList<ImageBean>? = null
}
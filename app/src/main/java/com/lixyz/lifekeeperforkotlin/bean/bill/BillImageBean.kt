package com.lixyz.lifekeeperforkotlin.bean.bill

import java.io.Serializable

class BillImageBean: Serializable {
    var objectId: String? = null
    var imageId: String? = null
    var billId: String? = null
    var imageSourceName: String? = null
    var imageCoverName: String? = null
    var imageThumbnailName: String? = null
    var imageUser: String? = null
}
package com.lixyz.lifekeeperforkotlin.bean.video

import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoBean
import java.io.Serializable


class PageVideoBean : Serializable {
    //分类名称
    var categoryName: String? = null

    //当前页
    var currentPage = 0

    //每页的数量
    var pageSize = 0

    //总记录数
    var videoCount: Long = 0

    //总页数
    var pageCount = 0

    //结果集
    var videos: ArrayList<VideoBean>? = null
}
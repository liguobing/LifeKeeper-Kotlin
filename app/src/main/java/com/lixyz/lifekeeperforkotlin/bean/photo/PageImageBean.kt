package com.lixyz.lifekeeperforkotlin.bean.photo

import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import java.io.Serializable


class PageImageBean: Serializable {
    //分类名称
    var categoryName: String? = null

    //当前页
    var currentPage = 0

    //每页的数量
    var pageSize = 0

    //总记录数
    var ImageCount: Long = 0

    //总页数
    var pageCount = 0

    //结果集
    var images: ArrayList<ImageBean>? = null
}
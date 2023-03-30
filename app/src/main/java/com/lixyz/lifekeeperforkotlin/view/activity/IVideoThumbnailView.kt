package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.ShowVideoItemBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoCategoryBean

interface IVideoThumbnailView {
    /**
     * 显示等待 Dialog
     */
    fun showWaitDialog()

    /**
     *隐藏等待 Dialog
     */
    fun hideWaitDialog()

    /**
     * 更新等待 Dialog
     */
    fun updateWaitDialog(message: String)

    /**
     * 显示 SnackBar
     */
    fun showSnackBar(message: String)

    /**
     * 更新 RecyclerView
     */
    fun updateRecyclerView(list: ArrayList<ShowVideoItemBean>)

    fun updateOtherCategoryList(list: ArrayList<VideoCategoryBean>)

    /**
     * 隐藏载入图片 Dialog
     */
    fun hideLoadVideoDialog()

    /**
     * 更新载入图片 Dialog
     */
    fun updateLoadVideoDialog(progress: Float)

    /**
     * 更新删除结果
     */
    fun updateDeleteFileResult(list: ArrayList<ShowVideoItemBean>)

    /**
     * 更新移动分类结果
     */
    fun updateMoveFileResult(list: ArrayList<ShowVideoItemBean>)

    /**
     * 显示/隐藏删除按钮布局
     */
    fun showOrHideDeleteLayout(isShow: Boolean)
}
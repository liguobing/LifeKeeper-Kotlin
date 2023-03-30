package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.ShowPhotoItemBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean

interface IImageThumbnailView {
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
    fun updateRecyclerView(list: ArrayList<ShowPhotoItemBean>)

    /**
     * 更新其他分类相册
     */
    fun updateOtherCategoryList(list: ArrayList<ImageCategoryBean>)

    /**
     * 隐藏载入图片 Dialog
     */
    fun hideLoadPhotoDialog()

    /**
     * 更新载入图片 Dialog
     */
    fun updateLoadPhotoDialog(progress: Float)

    /**
     * 更新删除结果
     */
    fun updateDeleteFileResult(list: ArrayList<ShowPhotoItemBean>)

    /**
     * 更新移动分类结果
     */
    fun updateMoveFileResult(list: ArrayList<ShowPhotoItemBean>)

    /**
     * 显示/隐藏删除按钮布局
     */
    fun showOrHideDeleteLayout(isShow: Boolean)

    /**
     * 更新相册名
     */
    fun updateCategoryName(categoryName:String)



}
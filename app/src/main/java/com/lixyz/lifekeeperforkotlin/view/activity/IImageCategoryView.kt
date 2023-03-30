package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.SelectFileBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.photo.ImageCategoryCover

interface IImageCategoryView {
    /**
     * 显示等待 Dialog
     */
    fun showWaitDialog()

    /**
     *隐藏等待 Dialog
     */
    fun hideWaitDialog()

    /**
     * 显示 SnackBar
     */
    fun showSnackBar(message: String)

    /**
     * 更新本地界面
     */
    fun updateView(list: ArrayList<ImageCategoryCover>)
    fun startImageActivity(categoryId: String, categoryName: String, password: String)
    fun showEditCategoryDialog(category: ImageCategoryBean)

    fun showDeleteImageCategoryDialog(categoryName: String, categoryId: String, password: String)

    fun showUpload()
    fun hideUpload()
    fun showBottomUploadDialog(list: ArrayList<SelectFileBean>)
    fun hideUploadBottomDialog()
    fun showUploadDialog()
    fun hideUploadDialog()
    fun updateUploadDialog(message: String, progress: Int)
    fun uploadDone()
}
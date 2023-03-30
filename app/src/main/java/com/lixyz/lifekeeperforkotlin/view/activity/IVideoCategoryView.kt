package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.video.VideoCategoryCover

interface IVideoCategoryView {
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
    fun updateView(list: ArrayList<VideoCategoryCover>)

    fun startVideoActivity(categoryId: String, categoryName: String, password: String)

    fun showDeleteVideoCategoryDialog(categoryName: String, categoryId: String, password: String)
    fun showEditCategoryDialog(category: VideoCategoryBean)
    fun showUploadBottomButton()
    fun hideUploadBottomButton()
    fun showUploadDialog()
    fun hideUploadDialog()
    fun updateUploadDialog(message: String, progress: Int)
    fun uploadDone()
}
package com.lixyz.lifekeeperforkotlin.view.activity

interface INetDiskView {
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
     * 更新上传图片按钮状态
     */
    fun updateUploadButtonStatus(status: Int)


    fun updateImageCardData(imageCount: Int)

    fun updatePhoneRecordCardData(phoneRecordCount: Int)

    fun updateVideoCardData(videoCount: Int)

}
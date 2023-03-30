package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import java.io.File

interface IDisplayImageView {
    fun showWaitDialog()

    fun updateWaitDialog(message: String)

    fun hideWaitDialog()

    fun showSnackBar(message: String)

    fun showPhotoLoadingDialog()

    fun updatePhotoLoadingDialog(progress: Float)

    fun hidePhotoLoadingDialog()

    fun updateViewPager(file: File)

    fun deleteFileSuccessUpdateRecyclerView(position: Int)

    fun updateImageCategoryList(categoryList: ArrayList<ImageCategoryBean>)

    fun moveImageSuccess(bean: ImageBean)

    fun updateImages(images: ArrayList<ImageBean>)
    fun showMoveImageBottomDialog(categoryList: ArrayList<ImageCategoryBean>)
}
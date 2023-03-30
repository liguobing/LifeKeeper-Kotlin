package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.ShowPhotoItemBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.photo.MoveImageBean
import com.lixyz.lifekeeperforkotlin.model.ImageThumbnailModel
import com.lixyz.lifekeeperforkotlin.view.activity.IImageThumbnailView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


class ImageThumbnailPresenter(private val thumbnailView: IImageThumbnailView) {

    /**
     * Model
     */
    private val thumbnailModel: ImageThumbnailModel = ImageThumbnailModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("显示照片线程池"))


    fun loadMoreImage(
        context: Context,
        categoryId: String,
        password: String,
        offset: Int,
        rows: Int
    ) {
        thumbnailView.showWaitDialog()
        threadPool.execute {
            val pageImageBean = thumbnailModel.loadMoreImage(context, categoryId,password, offset, rows)
            if (pageImageBean == null) {
                thumbnailView.showSnackBar("出错啦，请稍后重试...")
            } else {
                val list = ArrayList<ShowPhotoItemBean>()
                if (pageImageBean.images!!.size == 0) {
                    thumbnailView.hideWaitDialog()
                    thumbnailView.showSnackBar("没有更多啦...")
                } else {
                    for (bean in pageImageBean.images!!) {
                        val showPhotoItemBean = ShowPhotoItemBean()
                        showPhotoItemBean.image = bean
                        showPhotoItemBean.checked = false
                        showPhotoItemBean.checkViewIsShow = false
                        list.add(showPhotoItemBean)
                    }
                    thumbnailView.updateRecyclerView(list)
                    thumbnailView.updateCategoryName(pageImageBean.categoryName!!)
                }
            }
            thumbnailView.hideWaitDialog()
        }
    }

    /**
     * 删除照片
     */
    fun deleteFile(itemBeanList: ArrayList<ShowPhotoItemBean>, context: Context) {
        if (itemBeanList.size > 0) {
            thumbnailView.showWaitDialog()
            threadPool.execute {
                val list = ArrayList<String>()
                for (itemBean in itemBeanList) {
                    list.add(itemBean.image!!.objectId!!)
                }
                val deleteResult = thumbnailModel.deleteFile(context, list)
                if (deleteResult) {
                    thumbnailView.updateDeleteFileResult(itemBeanList)
                    thumbnailView.showSnackBar("删除成功")
                } else {
                    thumbnailView.showSnackBar("删除出错，请稍后重试...")
                }
                thumbnailView.showOrHideDeleteLayout(false)
                thumbnailView.hideWaitDialog()
            }
        }
    }


    fun moveImage(
        context: Context,
        imageCategoryBean: ImageCategoryBean,
        deleteList: ArrayList<ShowPhotoItemBean>
    ) {
        thumbnailView.showWaitDialog()
        threadPool.execute {
            val objectIdList = ArrayList<String>()
            deleteList.forEach {
                objectIdList.add(it.image!!.objectId!!)
            }
            val moveImageBean = MoveImageBean()

            moveImageBean.targetCategoryId = imageCategoryBean.categoryId
            moveImageBean.images = objectIdList
            val result: Boolean = thumbnailModel.moveImage(context, moveImageBean)
            if (result) {
                thumbnailView.showSnackBar("移动成功")
                thumbnailView.updateMoveFileResult(deleteList)
            } else {
                thumbnailView.showSnackBar("移动出错，请稍后重试")
            }
            thumbnailView.showOrHideDeleteLayout(false)
            thumbnailView.hideWaitDialog()
        }
    }


    fun getOtherCategory(context: Context, categoryId: String) {
        threadPool.execute {
            val otherCategoryData = thumbnailModel.getOtherCategory(context, categoryId)
            if (otherCategoryData != null) {
                thumbnailView.updateOtherCategoryList(otherCategoryData)
            }
        }
    }

    fun moveImageToNewCategory(
        context: Context,
        category: ImageCategoryBean,
        moveImageList: java.util.ArrayList<ShowPhotoItemBean>
    ) {
        thumbnailView.showWaitDialog()
        threadPool.execute {
            val result:Boolean = thumbnailModel.addNewCategory(context,category)
            if(result){
                val objectIdList = ArrayList<String>()
                moveImageList.forEach {
                    objectIdList.add(it.image!!.objectId!!)
                }
                val moveImageBean = MoveImageBean()
                moveImageBean.targetCategoryId = category.categoryId
                moveImageBean.images = objectIdList
                val moveResult = thumbnailModel.moveImage(context, moveImageBean)
                if (moveResult) {
                    thumbnailView.showSnackBar("移动成功")
                    thumbnailView.updateMoveFileResult(moveImageList)
                } else {
                    thumbnailView.showSnackBar("移动出错，请稍后重试")
                }
                thumbnailView.showOrHideDeleteLayout(false)
                thumbnailView.hideWaitDialog()
            }
        }

    }

}
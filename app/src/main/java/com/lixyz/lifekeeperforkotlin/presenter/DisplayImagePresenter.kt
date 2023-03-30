package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.photo.MoveImageBean
import com.lixyz.lifekeeperforkotlin.model.DisplayImageModel
import com.lixyz.lifekeeperforkotlin.view.activity.IDisplayImageView
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class DisplayImagePresenter {


    /**
     * Model
     */
    private val model: DisplayImageModel = DisplayImageModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("显示照片详情线程池"))

    @Throws(Exception::class)
    fun getImages(
        context: Context,
        view: IDisplayImageView,
        categoryId: String,
        password: String
    ) {
        threadPool.execute {
            val pageImages = model.getImages(context, categoryId, password)
            if (pageImages != null) {
                val images = pageImages.images
                view.updateImages(images!!)
            }
        }
    }

    @Throws(Exception::class)
    fun deleteImage(
        context: Context,
        view: IDisplayImageView,
        position: Int,
        deleteList: ArrayList<String>
    ) {
        view.showWaitDialog()
        threadPool.execute {
            val result: Boolean = model.deleteFile(context, deleteList)
            if (result) {
                view.deleteFileSuccessUpdateRecyclerView(position)
                view.hideWaitDialog()
                view.showSnackBar("删除成功")
            } else {
                view.hideWaitDialog()
                view.showSnackBar("删除失败")
            }
        }
    }

    @Throws(Exception::class)
    fun downloadImage(context: Context, view: IDisplayImageView, imageBean: ImageBean) {
        view.showWaitDialog()
        threadPool.execute {
            try {
                val result: Boolean =
                    model.downloadImage(context, imageBean)
                view.hideWaitDialog()
                if (result) {
                    view.showSnackBar("下载成功")
                } else {
                    view.showSnackBar("下载出错")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(Exception::class)
    fun getOtherCategory(
        context: Context,
        view: IDisplayImageView,
        imageBean: ImageBean
    ) {
        view.showWaitDialog()
        threadPool.execute {
            val otherCategoryList = model.getOtherCategory(context, imageBean.fileCategory!!)
            val bean = ImageCategoryBean()
            bean.categoryName = "创建新相册"
            bean.categoryId = "add_new_category_object_id"
            bean.objectId = "add_new_category_category_id"
            otherCategoryList.add(bean)
            view.hideWaitDialog()
            view.showMoveImageBottomDialog(otherCategoryList)
        }
    }

    @Throws(Exception::class)
    fun moveImageToNewCategory(
        context: Context,
        view: IDisplayImageView,
        newCategory: ImageCategoryBean,
        imageBean: ImageBean,
        position: Int
    ) {
        view.showWaitDialog()
        threadPool.execute {
            val result: Boolean = model.addNewCategory(context, newCategory)
            if (result) {
                val objectIdList = ArrayList<String>()
                objectIdList.add(imageBean.objectId!!)
                val moveImageBean = MoveImageBean()
                moveImageBean.targetCategoryId = newCategory.categoryId
                moveImageBean.images = objectIdList
                val moveResult = model.moveImage(context, moveImageBean)
                if (moveResult) {
                    view.showSnackBar("移动成功")
                    view.deleteFileSuccessUpdateRecyclerView(position)
                } else {
                    view.showSnackBar("移动出错，请稍后重试")
                }
                view.hideWaitDialog()
            }
        }
    }

    @Throws(Exception::class)
    fun moveImage(
        context: Context,
        view: IDisplayImageView,
        imageCategoryBean: ImageCategoryBean,
        imageBean: ImageBean,
        position: Int
    ) {
        view.showWaitDialog()
        threadPool.execute {
            val objectIdList = ArrayList<String>()
            objectIdList.add(imageBean.objectId!!)
            val moveImageBean = MoveImageBean()

            moveImageBean.targetCategoryId = imageCategoryBean.categoryId
            moveImageBean.images = objectIdList
            val result: Boolean = model.moveImage(context, moveImageBean)
            if (result) {
                view.showSnackBar("移动成功")
                view.deleteFileSuccessUpdateRecyclerView(position)
            } else {
                view.showSnackBar("移动出错，请稍后重试")
            }
            view.hideWaitDialog()
        }
    }
}
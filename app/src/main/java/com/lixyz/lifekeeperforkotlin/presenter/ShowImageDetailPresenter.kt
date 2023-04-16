package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.photo.MoveImageBean
import com.lixyz.lifekeeperforkotlin.model.DisplayImageModel
import com.lixyz.lifekeeperforkotlin.view.activity.IShowImageDetailView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class ShowImageDetailPresenter(
    private var view: IShowImageDetailView
) {

    /**
     * Model
     */
    private val model: DisplayImageModel = DisplayImageModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("显示照片详情线程池"))

    fun getOtherImageCategory(context: Context, categoryId: String) {
        threadPool.execute {
            view.updateImageCategoryList(
                model.getOtherCategory(
                    context,
                    categoryId
                )
            )
        }
    }

    fun downloadImage(context: Context, imageBean: ImageBean) {
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

    fun deleteFile(context: Context, bean: ImageBean) {
        val list = ArrayList<String>()
        list.add(bean.objectId!!)
        view.showWaitDialog()
        threadPool.execute {
            val result: Boolean = model.deleteFile(context, list)
            if (result) {
                view.deleteFileSuccessUpdateViewPager(bean)
            } else {
                view.showSnackBar("删除出错，请稍后重试...")
            }
            view.hideWaitDialog()
        }
    }

    fun moveImage(context: Context, targetCategory: ImageCategoryBean, bean: ImageBean) {
        view.showWaitDialog()
        threadPool.execute {
            val moveImageBean = MoveImageBean()
            moveImageBean.targetCategoryId = targetCategory.categoryId
            val list = ArrayList<String>()
            list.add(bean.objectId!!)
            moveImageBean.images = list
            val result = model.setImageCategory(context, moveImageBean)
            if (result) {
                view.showSnackBar("移动成功")
                view.moveImageSuccess(bean)
            } else {
                view.showSnackBar("出错啦，请稍后重试...")
            }
            view.hideWaitDialog()
        }
    }

    fun getImages(
        context: Context,
        categoryId: String,
        password: String
    ) {
        threadPool.execute {
            val pageImages = model.getImages(context, categoryId, password)
            if (pageImages != null) {
                val images = pageImages.images
            }
        }
    }
}
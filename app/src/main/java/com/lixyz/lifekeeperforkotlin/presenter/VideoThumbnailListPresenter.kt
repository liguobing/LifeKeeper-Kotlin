package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.MoveVideoBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.ShowVideoItemBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoCategoryBean
import com.lixyz.lifekeeperforkotlin.model.VideoThumbnailListModel
import com.lixyz.lifekeeperforkotlin.view.activity.IVideoThumbnailView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class VideoThumbnailListPresenter(
    private var thumbnailView: IVideoThumbnailView
) {

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("视频缩略图线程池"))

    private val model: VideoThumbnailListModel = VideoThumbnailListModel()

    fun getOtherCategory(context: Context, categoryId: String?) {
        threadPool.execute {
            val otherCategoryData = model.getOtherCategory(context, categoryId)
            if (otherCategoryData != null) {
                thumbnailView.updateOtherCategoryList(otherCategoryData)
            }
        }
    }

    fun loadMoreVideo(
        context: Context,
        categoryId: String,
        password: String,
        offset: Int,
        rows: Int
    ) {
        thumbnailView.showWaitDialog()
        threadPool.execute {
            val pageVoidBean = model.loadMoreVideo(context, categoryId, password, offset, rows)
            if (pageVoidBean == null) {
                thumbnailView.showSnackBar("出错啦，请稍后重试...")
            } else {
                val list = ArrayList<ShowVideoItemBean>()
                if (pageVoidBean.videos!!.size == 0) {
                    thumbnailView.hideWaitDialog()
                    thumbnailView.showSnackBar("没有更多啦...")
                } else {
                    for (bean in pageVoidBean.videos!!) {
                        val showPhotoItemBean = ShowVideoItemBean()
                        showPhotoItemBean.video = bean
                        showPhotoItemBean.checked = false
                        showPhotoItemBean.checkViewIsShow = false
                        list.add(showPhotoItemBean)
                    }
                    thumbnailView.updateRecyclerView(list)
                }
            }
            thumbnailView.hideWaitDialog()
        }
    }

    /**
     * 删除视频
     */
    fun deleteFile(itemBeanList: ArrayList<ShowVideoItemBean>, context: Context) {
        if (itemBeanList.size > 0) {
            thumbnailView.showWaitDialog()
            threadPool.execute {
                val list = ArrayList<String>()
                for (itemBean in itemBeanList) {
                    list.add(itemBean.video!!.objectId!!)
                }
                val deleteResult = model.deleteFile(context, list)
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

    fun changeVideoCategory(
        context: Context,
        videoCategoryBean: VideoCategoryBean,
        deleteList: ArrayList<ShowVideoItemBean>
    ) {
        thumbnailView.showWaitDialog()
        threadPool.execute {
            val objectIdList = ArrayList<String>()
            deleteList.forEach {
                objectIdList.add(it.video!!.objectId!!)
            }
            val moveVideoBean = MoveVideoBean()
            moveVideoBean.targetCategoryId = videoCategoryBean.categoryId
            moveVideoBean.videos = objectIdList
            val result: Boolean = model.changeVideoCategory(context, moveVideoBean)
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
}
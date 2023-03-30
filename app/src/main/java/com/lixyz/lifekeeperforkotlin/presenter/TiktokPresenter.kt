package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.model.TiktokModal
import com.lixyz.lifekeeperforkotlin.view.activity.ITiktokView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class TiktokPresenter(
    private val view: ITiktokView,
    private val context: Context
) {

    private val modal = TiktokModal()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("播放视频线程池"))


    fun getVideos(context: Context, categoryId: String, password: String) {
        view.showWaitDialog()
        threadPool.execute {
            val pageVideo = modal.getVideos(context, categoryId, password)
            if (pageVideo != null) {
                view.updateRecyclerView(pageVideo.videos)
            } else {
                view.updateRecyclerView(ArrayList())
            }
            view.hideWaitDialog()
        }
    }
}
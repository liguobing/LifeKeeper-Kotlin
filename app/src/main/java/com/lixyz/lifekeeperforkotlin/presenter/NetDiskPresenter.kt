package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.model.NetDiskModel
import com.lixyz.lifekeeperforkotlin.view.activity.INetDiskView
import java.io.File
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


class NetDiskPresenter(private var view: INetDiskView) {

    /**
     * Model
     */
    private val model: NetDiskModel = NetDiskModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("相册页面线程"))

    /**
     * 删除缓存文件
     */
    @kotlin.jvm.Throws(Exception::class)
    fun deleteCacheFile(context: Context) {
        threadPool.execute {
            val files: Array<File>? = context.cacheDir.listFiles()
            files!!.forEach {
                if (it.exists()) {
                    it.delete()
                }
            }
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    fun getNetDiskData(context: Context) {
        threadPool.execute {
            val netDiskData = model.getNetDiskData(context)
            if (netDiskData == null) {
                view.showSnackBar("出错啦，请稍后重试...")
            } else {
                view.updateImageCardData(netDiskData.imageCount)
                view.updatePhoneRecordCardData(netDiskData.recordCount)
                view.updateVideoCardData(netDiskData.videoCount)
            }
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    fun loadImageData(context: Context) {
        threadPool.execute {
            val imageCount = model.getImageCount(context)
            if (imageCount >= 0) {
                view.updateImageCardData(imageCount)
            } else {
                view.showSnackBar("图片载入出错，请稍后重试...")
            }
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    fun loadPhoneRecordData(context: Context) {
        threadPool.execute {
            val phoneRecordCount = model.getPhoneRecordCount(context)
            if (phoneRecordCount >= 0) {
                view.updatePhoneRecordCardData(phoneRecordCount)
            } else {
                view.showSnackBar("电话录音载入出错，请稍后重试...")
            }
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    fun loadVideoData(context: Context) {
        threadPool.execute {
            val videoCount = model.getVideoCount(context)
            if (videoCount >= 0) {
                view.updateVideoCardData(videoCount)
            } else {
                view.showSnackBar("视频载入出错，请稍后重试...")
            }
        }
    }
}
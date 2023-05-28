package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.netdisk.NetDiskOverview
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
    fun getNetDiskData(context: Context): NetDiskOverview? {
        return model.getNetDiskData(context)
    }

    @kotlin.jvm.Throws(Exception::class)
    fun loadImageData(context: Context): Int {
        return model.getImageCount(context)
    }

    @kotlin.jvm.Throws(Exception::class)
    fun loadPhoneRecordData(context: Context): Int {
        return model.getPhoneRecordCount(context)
    }
    @kotlin.jvm.Throws(Exception::class)
    fun loadWeChatRecordData(context: Context): Int {
        return model.getWeChatRecordCount(context)
    }

    @kotlin.jvm.Throws(Exception::class)
    fun loadVideoData(context: Context): Int {
        return model.getVideoCount(context)
    }
}
package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.model.SetUserIconModel
import com.lixyz.lifekeeperforkotlin.view.activity.ISetUserIconView
import java.io.File
import java.io.IOException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class SetUserIconPresenter(view: ISetUserIconView) {

    private var mView: ISetUserIconView = view

    private var model: SetUserIconModel = SetUserIconModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("上传用户头像线程池"))

    fun uploadUserIcon(context: Context, filePath: String) {
        mView.showWaitDialog()
        threadPool.execute {
            try {
                val userImageResponse = model.uploadFileToCloud(context, File(filePath))
                if (userImageResponse.resultCode!! > 0) {
                    val result = model.updateUser(context, userImageResponse.imageName!!)
                    if (result) {
                        mView.saveSuccessful("http://104.245.40.124:8080/UserImage/${userImageResponse.imageName}")
                    } else {
                        mView.showSnackBar("保存失败，请稍后重试...")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                mView.showSnackBar("保存失败，请稍后重试...")
            }
        }
    }
}
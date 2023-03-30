package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.model.IndexModel
import com.lixyz.lifekeeperforkotlin.view.activity.IIndexView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


/**
 * IndexActivity Presenter
 *
 * @author LGB
 */
class IndexPresenter(
    private val view: IIndexView
) {

    /**
     * Model
     */
    private val model: IndexModel = IndexModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("首页页面线程"))


    fun checkLogin(context: Context) {
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        if (config.getString("UserId", null) == null) {
            view.updateLogin(false, null)
            view.showSnackBar("还没有登录")
        } else {
            val userName = config.getString("UserName", null)
            val userIconUrl = config.getString("UserIconUrl", null)
            val userBean = UserBean()
            userBean.userName = userName
            userBean.userIconUrl = userIconUrl
            view.updateLogin(true, userBean)
        }
    }

    fun getPlanOverview(context: Context) {
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
            if (userId == null) {
                view.resetPlanContent(0, 0)
            } else {
                val planOverview = model.getPlanContent(userId)
                if (planOverview == null) {
                    view.resetPlanContent(0, 0)
                } else {
                    view.resetPlanContent(
                        planOverview.planCountOfDay,
                        planOverview.planCountOfMonth
                    )
                }
            }
        }
    }

    fun getAccountOverview(context: Context) {
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
            if (userId == null) {
                view.resetAccountContent(0.0, 0.0)
            } else {
                val accountOverview = model.getAccountContent(userId)
                if (accountOverview == null) {
                    view.resetAccountContent(0.0, 0.0)
                } else {
                    view.resetAccountContent(
                        accountOverview.incomeCount,
                        accountOverview.expendCount
                    )
                }
            }
        }
    }

    fun getNetDiskOverview(context: Context) {
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
            if (userId != null) {
                val netDiskOverview = model.getNetDiskContent(userId)
                if (netDiskOverview == null) {
                    view.resetNetDiskContent(0)
                } else {
                    view.resetNetDiskContent(netDiskOverview.imageCount + netDiskOverview.recordCount + netDiskOverview.videoCount)
                }
            } else {
                view.resetNetDiskContent(0)
            }
        }
    }


    fun updateWeather(latitude: Double, longitude: Double) {
        threadPool.execute {
            val weatherCode = model.getWeatherCode(latitude, longitude)
            Log.d("TTT", "updateWeather: $weatherCode")
            view.updateWeather(weatherCode.toInt())
        }
    }

    /**
     * 关闭线程池
     */
    fun shutDownThreadPool() {
        threadPool.shutdown()
    }
}
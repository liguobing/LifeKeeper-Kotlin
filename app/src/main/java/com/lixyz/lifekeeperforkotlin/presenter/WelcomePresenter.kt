package com.lixyz.lifekeeperforkotlin.presenter

import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.view.activity.IWelcomeView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


class WelcomePresenter(private var view: IWelcomeView) {

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("欢迎页面线程池"))

    private var timeDowning = true

    fun startTimeDown() {
        threadPool.execute {
            var i = 5
            do {
                if (timeDowning) {
                    view.timeDown(i)
                    i--
                    Thread.sleep(1000)
                    if (i == 0) {
                        view.startIndexActivity()
                    }
                } else {
                    view.resetButton()
                    break
                }
            } while (i > 0)
        }
    }

    fun stopTimeDown() {
        if (timeDowning) {
            timeDowning = false
        }
    }


    fun activityDestroy() {
        threadPool.shutdown()
    }
}
package com.lixyz.lifekeeperforkotlin.base

import java.util.concurrent.ThreadFactory


/**
 * 线程工厂类，作用是为生成的线程添加名字
 *
 * @author LGB
 */
class BaseThreadFactory(private val threadName: String) : ThreadFactory {
    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r)
        thread.name = threadName + "-" + thread.id
        return thread
    }

}
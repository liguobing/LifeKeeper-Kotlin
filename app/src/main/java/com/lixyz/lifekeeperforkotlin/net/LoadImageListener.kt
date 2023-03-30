package com.lixyz.lifekeeperforkotlin.net

interface LoadImageListener {
    fun startLoad()

    fun loading(currLength: Long, contentLength: Long)

    fun endLoad()

    fun loadError()
}
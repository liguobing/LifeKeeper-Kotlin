package com.lixyz.lifekeeperforkotlin.utils

import android.content.Context
import chuangyuan.ycj.videolibrary.listener.DataSourceListener
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Util
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


class PlayVideoDataSource(private var context: Context) : DataSourceListener {

    override fun getDataSourceFactory(): DataSource.Factory {
        val client: OkHttpClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(StethoInterceptor())
            .build()
        return OkHttpDataSourceFactory(
            client,
            Util.getUserAgent(context, context.applicationContext.packageName),
            DefaultBandwidthMeter()
        )
    }
}
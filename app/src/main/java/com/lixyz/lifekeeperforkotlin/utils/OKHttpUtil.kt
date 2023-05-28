package com.lixyz.lifekeeperforkotlin.utils

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class OKHttpUtil {
    companion object {
        val getInstance by lazy(LazyThreadSafetyMode.NONE) {
//            val factory = HttpsUtil.getSslSocketFactory(
//                arrayOf(context.assets.open(Constant.SERVER_CER)),
//                context.assets.open(Constant.CLIENT_CER),
//                Constant.CLOUD_ADDRESS_CERTIFICATE_PASSWORD
//            )
            OkHttpClient.Builder().addNetworkInterceptor(
                StethoInterceptor()
            )
                .retryOnConnectionFailure(true)
                .connectTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                .writeTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(StethoInterceptor())
                .addNetworkInterceptor(StethoInterceptor())
                //            .sslSocketFactory(
//                factory.sSLSocketFactory!!, factory.trustManager!!
//            )
//            .hostnameVerifier { hostname, _ ->
//                Constant.HOST_NAME == hostname
//            }
                .build()
        }
    }
}
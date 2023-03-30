package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.os.Environment
import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.RecordRecyclerViewItemBean
import com.lixyz.lifekeeperforkotlin.net.CountingRequestBody
import com.lixyz.lifekeeperforkotlin.net.https.HttpsUtil
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.view.activity.IPhoneRecordView
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class CallRecordPresenter(
    private var view: IPhoneRecordView
) {


    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("通话录音线程池"))

    fun upload(context: Context, list: ArrayList<String>) {
        val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
        val dir = File("$parentPath/MIUI/sound_recorder/call_rec/")
        threadPool.execute {
            list.forEachIndexed { index, s ->
                val file = File("$parentPath/MIUI/sound_recorder/call_rec/$s")
                val requestBody = CountingRequestBody(
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "file",
                            file.name,
                            file.asRequestBody("application/octet-stream".toMediaType())
                        )
                        .addFormDataPart(
                            "fileName",
                            s
                        ).build()
                ) { max, value ->
//                    val progress = (value * 100 / max).toInt()
//                    view.updateUploadDialog(
//                        "上传视频，第 $fileIndex 个，共 $fileCount 个",
//                        progress
//                    )
                }
                val sharedPreferences = context.getSharedPreferences(
                    "LoginConfig",
                    Context.MODE_PRIVATE
                )
                val userId = sharedPreferences.getString("UserId", null)
                if (userId != null) {
                    val factory = HttpsUtil.getSslSocketFactory(
                        arrayOf(context.assets.open(Constant.SERVER_CER)),
                        context.assets.open(Constant.CLIENT_CER),
                        Constant.CLOUD_ADDRESS_CERTIFICATE_PASSWORD
                    )
                    val client: OkHttpClient = OkHttpClient.Builder()
                        .retryOnConnectionFailure(false)
                        .connectTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                        .readTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                        .writeTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
//                        .sslSocketFactory(
//                            factory.sSLSocketFactory!!, factory.trustManager!!
//                        )
//                        .hostnameVerifier { hostname, _ ->
//                            Constant.HOST_NAME == hostname
//                        }
                        .addNetworkInterceptor(StethoInterceptor())
                        .build()
                    val request = Request.Builder()
                        .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UploadRecord")
                        .addHeader("Token", getUserId(context)!!)
                        .post(requestBody)
                        .build()
                    val response = client.newCall(request).execute()
                    val gson = Gson()
                    val result = gson.fromJson(
                        response.body!!.string(),
                        NewResult::class.java
                    )
                    if (result.result) {
                    } else {
                    }
                } else {
                }
            }
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
            .getString("UserId", null)
    }
}
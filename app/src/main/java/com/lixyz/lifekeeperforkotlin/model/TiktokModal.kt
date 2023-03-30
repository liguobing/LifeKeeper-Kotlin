package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.video.PageVideoBean
import com.lixyz.lifekeeperforkotlin.net.https.HttpsUtil
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TiktokModal {
    fun getVideos(context: Context, categoryId: String, password: String): PageVideoBean? {
        try {
            val client = OKHttpUtil.getInstance
            val request =
                Request.Builder()
                    .url("https://www.li-xyz.com/LifeKeeper/GetVideos?cp=0&ps=-1")
                    .addHeader("Token", getUserId(context)!!)
                    .addHeader("Password", password)
                    .addHeader("CategoryId", categoryId)
                    .build()
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
            return if (result.result) {
                val jsonString = Gson().toJson(result.resultObject)
                Gson().fromJson(JsonParser.parseString(jsonString), PageVideoBean::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    private fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
            .getString("UserId", null)
    }
}
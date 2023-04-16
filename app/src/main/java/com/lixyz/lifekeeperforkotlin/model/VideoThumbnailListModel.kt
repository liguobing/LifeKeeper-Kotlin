package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.MoveVideoBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.video.PageVideoBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Type

class VideoThumbnailListModel {

    private fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
    }

    fun getOtherCategory(
        context: Context,
        categoryId: String?
    ): ArrayList<VideoCategoryBean>? {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/GetOtherVideoCategory"
            )
            .addHeader("Token", getUserId(context)!!)
            .addHeader("Category", categoryId!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val listType: Type =
                object : TypeToken<ArrayList<VideoCategoryBean>>() {}.type
            val jsonString = Gson().toJson(result.resultObject)
            Gson().fromJson(JsonParser.parseString(jsonString), listType)
        } else {
            null
        }
    }


    fun loadMoreVideo(
        context: Context,
        categoryId: String,
        password: String,
        offset: Int,
        rows: Int
    ): PageVideoBean? {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/GetVideos?cp=$offset&ps=$rows"
            )
            .addHeader("Token", getUserId(context)!!)
            .addHeader("CategoryId", categoryId)
            .addHeader("Password", password)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val str = response.body!!.string()
        val result = Gson().fromJson(str, NewResult::class.java)
        return if (result.result) {
            val jsonString = Gson().toJson(result.resultObject)
            Gson().fromJson(JsonParser.parseString(jsonString), PageVideoBean::class.java)
        } else {
            null
        }
    }


    fun deleteFile(context: Context, objectIdList: ArrayList<String>): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteVideo")
            .addHeader("Token", getUserId(context)!!)
            .post(
                Gson().toJson(objectIdList)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            result.resultObject as Boolean
        } else {
            false
        }
    }


    fun changeVideoCategory(context: Context, moveVideoBean: MoveVideoBean): Boolean {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/MoveVideos"
            )
            .addHeader("Token", getUserId(context)!!)
            .post(
                Gson().toJson(moveVideoBean)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }
}
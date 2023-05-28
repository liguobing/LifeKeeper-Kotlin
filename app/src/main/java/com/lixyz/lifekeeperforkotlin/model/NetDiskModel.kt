package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.netdisk.NetDiskOverview
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.Request


class NetDiskModel {

    @kotlin.jvm.Throws(Exception::class)
    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
    }

    @kotlin.jvm.Throws(Exception::class)
    fun getNetDiskData(context: Context): NetDiskOverview? {
        try {
            val client = OKHttpUtil.getInstance
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetNetDiskData?userId=${getUserId(context)}")
                .build()
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
            return if (result.result) {
                Gson().fromJson(result.resultObject.toString(), NetDiskOverview::class.java)
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    fun getImageCount(context: Context): Int {
        try {
            val client = OKHttpUtil.getInstance
            val request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetImageCount")
                .addHeader("Token", getUserId(context)!!)
                .build()
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(
                response.body!!.string(),
                NewResult::class.java
            )
            return if (result.result) {
                Gson().fromJson(Gson().toJson(result.resultObject), Int::class.java)
            } else {
                -1
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    fun getPhoneRecordCount(context: Context): Int {
        try {
            val client = OKHttpUtil.getInstance
            val request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetRecordCountByUserId")
                .addHeader("Token", getUserId(context)!!)
                .build()
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(
                response.body!!.string(),
                NewResult::class.java
            )
            return if (result.result) {
                Gson().fromJson(Gson().toJson(result.resultObject), Int::class.java)
            } else {
                -1
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    fun getVideoCount(context: Context): Int {
        try {
            val client = OKHttpUtil.getInstance
            val request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetVideoCount")
                .addHeader("Token", getUserId(context)!!)
                .build()
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(
                response.body!!.string(),
                NewResult::class.java
            )
            return if (result.result) {
                Gson().fromJson(Gson().toJson(result.resultObject), Int::class.java)
            } else {
                -1
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    fun getWeChatRecordCount(context: Context): Int {
        try {
            val client = OKHttpUtil.getInstance
            val request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetWeChatRecordCountByUserId")
                .addHeader("Token", getUserId(context)!!)
                .build()
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(
                response.body!!.string(),
                NewResult::class.java
            )
            return if (result.result) {
                Gson().fromJson(Gson().toJson(result.resultObject), Int::class.java)
            } else {
                -1
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }

    }
}
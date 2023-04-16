package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.PlanOverview
import com.lixyz.lifekeeperforkotlin.bean.billaccount.AccountOverview
import com.lixyz.lifekeeperforkotlin.bean.netdisk.NetDiskOverview
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.Request


class IndexModel {
    /**
     * 获取 UserId，以判断用户是否已经登录
     *
     * @param context Context
     * @return 登录用户 UserId
     */
    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
            .getString("UserId", null)
    }

    fun getPlanContent(userId: String): PlanOverview? {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/GetPlanOverview"
        ).addHeader("Token", userId).build()
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val gson = Gson()
            gson.fromJson(gson.toJson(result.resultObject), PlanOverview::class.java)
        } else {
            null
        }
    }


    fun getWeatherCode(latitude: Double, longitude: Double): String {
        val client = OKHttpUtil.getInstance
        val request = Request.Builder()
            .url("${Constant.ADDRESS}/LifeKeeper/GetWeatherByLongitudeAndLatitude?longitude=$longitude&latitude=$latitude")
            .build()
        Log.d("TTT", "getWeatherCode: ${request.url.toString()}")
        val response = client.newCall(request).execute()
        try {
            return if (response.body != null) {
                val gson = Gson()
                val result =
                    gson.fromJson(response.body!!.string(), NewResult::class.java)
                if (result.result) {
                    result.resultObject.toString()
                } else {
                    "999"
                }
            } else {
                "999"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "999"
        }
    }

    fun getAccountContent(userId: String): AccountOverview? {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/GetAccountOverview"
        ).addHeader("Token", userId).build()
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val gson = Gson()
            gson.fromJson(gson.toJson(result.resultObject), AccountOverview::class.java)
        } else {
            null
        }
    }

    fun getNetDiskContent(userId: String): NetDiskOverview? {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/GetNetDiskOverview"
        ).addHeader("Token", userId).build()
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val gson = Gson()
            gson.fromJson(gson.toJson(result.resultObject), NetDiskOverview::class.java)
        } else {
            null
        }
    }
}
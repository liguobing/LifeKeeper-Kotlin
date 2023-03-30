package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.Result
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanBean
import com.lixyz.lifekeeperforkotlin.net.https.HttpsUtil
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit


/**
 * 添加计划 Model
 *
 * @author LGB
 */
class AddPlanModel {
    fun addPlan(list: List<PlanBean>): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/AddPlan")
            .post(
                Gson().toJson(list)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), Result::class.java)
        return if (result.code == 200) {
            result.detail as Boolean
        } else {
            false
        }
    }

    /**
     * 获取用户 ID
     *
     * @param context Context
     * @return 用户 ID
     */
    fun getUserId(context: Context): String {
        return context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
            .getString("UserId", null)!!
    }
}
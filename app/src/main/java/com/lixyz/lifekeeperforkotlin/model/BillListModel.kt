package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.BillListActivityOverview
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.net.https.HttpsUtil
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


/**
 * 账单列表界面模型
 *
 * @author LGB
 */
class BillListModel {
    /**
     * 获取用户 ID
     *
     * @param context Context
     * @return 用户 Id
     */
    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
            .getString("UserId", null)
    }

    /**
     * 获取顶部 UI 数据
     *
     * @param year    年份
     * @param month   月份
     * @return 数据 bean
     */
    @Throws(Exception::class)
    fun getBillData(
        userId: String,
        year: Int,
        month: Int
    ): BillListActivityOverview? {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetBillByMonth?year=$year&month=$month")
            .addHeader("Token", userId)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val str = response.body!!.string()
        val result = Gson().fromJson(str, NewResult::class.java)
        return if (result.result) {
            Gson().fromJson(
                Gson().toJson(result.resultObject),
                BillListActivityOverview::class.java
            )
        } else {
            null
        }
    }

    /**
     * 删除账单
     *
     * @param context Context
     * @param bean    bean
     * @return 是否删除成功
     */
    fun deleteBill(bean: BillBean, userId: String): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteBill?objectId=${bean.objectId}")
            .addHeader("Token", userId)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }
}
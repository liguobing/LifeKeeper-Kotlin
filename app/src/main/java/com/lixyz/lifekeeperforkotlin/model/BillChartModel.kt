package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.bean.billchart.BillChartDataBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.TimeUtil
import okhttp3.Request
import java.lang.reflect.Type


/**
 * 账单图表 Model
 *
 * @author LGB
 */
class BillChartModel {
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

    /**
     * 获取收支概览数据
     *
     * @param context Context
     * @param year    年
     * @param month   月
     * @return 数据
     */
    fun getBillChartData(context: Context, year: Int, month: Int): BillChartDataBean? {
        val startTime = TimeUtil.getMonthStart(year, month)
        val endTime = TimeUtil.getMonthEnd(year, month)

        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetBillChartData?start=$startTime&end=$endTime")
            .addHeader("Token", getUserId(context))
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val str = response.body!!.string()
        Log.d("TTT", str)
        val result = Gson().fromJson(str, NewResult::class.java)
        return if (result.result) {
            Gson().fromJson(
                Gson().toJson(result.resultObject),
                BillChartDataBean::class.java
            )
        } else {
            null
        }
    }

    fun getBillsByCategory(
        context: Context,
        label: String,
        billProperty: Int,
        year: Int,
        month: Int
    ): ArrayList<BillBean> {
        try {

            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetBillByCategory?categoryName=$label&billProperty=$billProperty&year=$year&month=$month")
                .addHeader("Token", getUserId(context))
                .build()
            val client = OKHttpUtil.getInstance
            val response = client.newCall(request).execute()
            val str = response.body!!.string()
            val result = Gson().fromJson(str, NewResult::class.java)
            return if (result.result) {
                val listType: Type =
                    object : TypeToken<ArrayList<BillBean>>() {}.type
                val jsonString = Gson().toJson(result.resultObject)
                Gson().fromJson(JsonParser.parseString(jsonString), listType)
            } else {
                ArrayList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
    }
}

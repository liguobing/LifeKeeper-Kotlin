package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.Result
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.Request
import java.lang.reflect.Type


/**
 * 计划列表模型
 *
 * @author LGB
 */
class PlanListModel {
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

    fun getPlanByMonth(
        context: Context,
        userId: String,
        year: Int,
        month: Int
    ): HashMap<Int, ArrayList<ArrayList<PlanBean>>> {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetPlanByUserIdAndMonth?userId=$userId&year=$year&month=$month")
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val str = response.body!!.string()
        val result = Gson().fromJson(str, Result::class.java)
        return if (result.code == 200) {
            val type: Type =
                object : TypeToken<HashMap<Int, ArrayList<ArrayList<PlanBean>>>>() {}.type
            Gson().fromJson(
                result.detail.toString().replace("planDescription=,", "planDescription='',")
                    .replace("planLocation=,", "planLocation='',"), type
            )
        } else {
            HashMap()
        }
    }


    fun finishPlan(
        context: Context,
        plan: PlanBean,
        newObjectId: String,
        updateTime: Long
    ): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/FinishPlan?objectId=${plan.objectId}&groupId=${plan.groupId}&updateTime=$updateTime&newObjectId=$newObjectId")
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

    fun unFinishPlan(
        context: Context,
        plan: PlanBean,
        newObjectId: String,
        updateTime: Long
    ): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UnFinishPlan?objectId=${plan.objectId}&updateTime=$updateTime&newObjectId=$newObjectId")
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
     * 取消计划提醒，将 AlarmTime 设置为 -1
     *
     * @param context Context
     * @param plan    要取消的计划对象
     * @return 是否取消成功
     */
    fun removeAlarm(
        context: Context,
        plan: PlanBean,
        newObjectId: String,
        updateTime: Long
    ): Boolean {
        try {
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/RemoveAlarm?objectId=${plan.objectId}&newObjectId=$newObjectId&updateTime=$updateTime")
                .build()
            val client = OKHttpUtil.getInstance
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(response.body!!.string(), Result::class.java)
            return if (result.code == 200) {
                result.detail as Boolean
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 添加计划提醒
     *
     * @param context Context
     * @param plan    要取消的计划对象
     * @return 是否取消成功
     */
    fun addAlarm(
        context: Context,
        plan: PlanBean,
        newObjectId: String,
        alarmTime: Int,
        updateTime: Long
    ): Boolean {
        try {
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/AddAlarm?objectId=${plan.objectId}&newObjectId=$newObjectId&updateTime=$updateTime&alarmTime=$alarmTime")
                .build()
            val client = OKHttpUtil.getInstance
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(response.body!!.string(), Result::class.java)
            return if (result.code == 200) {
                result.detail as Boolean
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


    fun deleteSinglePlan(context: Context, planBean: PlanBean): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeletePlanByObjectId?objectId=${planBean.objectId}&groupId=${planBean.groupId}&updateTime=${System.currentTimeMillis()}")
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

    fun deleteGroupPlan(context: Context, planBean: PlanBean): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeletePlanByGroupId?groupId=${planBean.groupId}&updateTime=${System.currentTimeMillis()}")
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
}
package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.BillCategoryAndBillAccount
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * BillCategory Model
 *
 * @author LGB
 */
class BillCategoryModel {
    /**
     * 获取用户 ID
     *
     * @return 用户ID
     */
    private fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
            .getString("UserId", null)
    }


    /**
     * 添加分类
     */
    @Throws(IOException::class)
    fun addCategory(context: Context, pageIndex: Int, categoryName: String?): NewResult {
        val categoryBean = BillCategory()
        categoryBean.objectId = StringUtil.getRandomString()
        categoryBean.categoryId = StringUtil.getRandomString()
        categoryBean.categoryUser = getUserId(context)!!
        categoryBean.categoryName = categoryName
        when (pageIndex) {
            0 -> {
                categoryBean.isIncome = 1
            }
            1 -> {
                categoryBean.isIncome = -1
            }
        }
        categoryBean.categoryStatus = 1
        categoryBean.categoryType = 0
        categoryBean.createTime = System.currentTimeMillis()
        categoryBean.updateTime = 0
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/AddBillCategory")
            .addHeader("Token", getUserId(context)!!)
            .post(
                Gson().toJson(categoryBean)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        return Gson().fromJson(response.body!!.string(), NewResult::class.java)
    }

    /**
     * 更新账单
     */
    @Throws(IOException::class)
    fun updateBillCategory(
        context: Context,
        category: BillCategory,
    ): NewResult {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateBillCategory")
            .addHeader("Token", getUserId(context)!!)
            .post(
                Gson().toJson(category)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        return Gson().fromJson(response.body!!.string(), NewResult::class.java)
    }


    fun getBillCategories(context: Context): BillCategoryAndBillAccount? {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetBillCategoryAndBillAccount")
            .addHeader("Token", getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            Gson().fromJson(
                Gson().toJson(result.resultObject),
                BillCategoryAndBillAccount::class.java
            )
        } else {
            null
        }
    }

    fun deleteBillCategory(
        context: Context,
        list: ArrayList<String>
    ): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteBillCategory")
            .addHeader("Token", getUserId(context)!!)
            .post(
                Gson().toJson(list).toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }

    fun updateBillCategoryOrder(
        context: Context,
        data: java.util.ArrayList<BillCategory>
    ): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateBillCategoryOrder")
            .addHeader("Token", getUserId(context)!!)
            .post(
                Gson().toJson(data).toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result

    }
}
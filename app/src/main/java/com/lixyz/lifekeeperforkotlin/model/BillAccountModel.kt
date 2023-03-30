package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.BillCategoryAndBillAccount
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount
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
 * BillAccount Model
 *
 * @author LGB
 */
class BillAccountModel {
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
     * 获取账单账户数据
     */
    fun getAccounts(context: Context): ArrayList<BillAccount> {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetBillCategoryAndBillAccount")
            .addHeader("Token", getUserId(context))
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val bean = Gson().fromJson(
                Gson().toJson(result.resultObject),
                BillCategoryAndBillAccount::class.java
            )
            bean.billAccounts!!
        } else {
            ArrayList()
        }
    }


    /**
     * 添加账户
     */
    @Throws(IOException::class)
    fun addAccount(context: Context, name: String?): NewResult {
        val categoryBean = BillAccount()
        categoryBean.objectId = StringUtil.getRandomString()
        categoryBean.accountId = StringUtil.getRandomString()
        categoryBean.accountUser = getUserId(context)
        categoryBean.accountName = name!!
        categoryBean.accountStatus = 1
        categoryBean.accountType = 0
        categoryBean.createTime = System.currentTimeMillis()
        categoryBean.updateTime = 0
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/AddBillAccount")
            .addHeader("Token", getUserId(context))
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
     * 删除账户
     */
    @Throws(IOException::class)
    fun deleteBillAccount(context: Context, list: ArrayList<String>): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteBillAccount")
            .addHeader("Token", getUserId(context))
            .post(
                Gson().toJson(list).toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }

    /**
     * 修改账户信息
     */
    @Throws(IOException::class)
    fun updateBillAccount(context: Context, account: BillAccount): NewResult {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateBillAccount")
            .addHeader("Token", getUserId(context))
            .post(
                Gson().toJson(account)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        return Gson().fromJson(response.body!!.string(), NewResult::class.java)
    }


    fun updateBillAccountOrder(context: Context, data: ArrayList<BillAccount>): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateBillAccountOrder")
            .addHeader("Token", getUserId(context))
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

package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.sqlite.SQLiteException
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.BillCategoryAndBillAccount
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.bean.bill.BillImageBean
import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount
import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.security.NoSuchAlgorithmException


/**
 * 添加账单模型
 *
 * @author LGB
 */
class AddBillModel {
    /**
     * 获取用户 ID
     *
     * @param context Context
     * @return 用户 ID
     */
    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
            .getString("UserId", null)
    }


    /**
     * 上传账单图片
     *
     * @param imageLocalPath 图片本地路径
     * @param context        Context
     * @return 上传响应原型
     * @throws IOException IOException
     */
    fun uploadBillImage(
        imageLocalPath: String,
        billId:String,
        context: Context
    ): Boolean {
        try {
            val file = File(imageLocalPath)
            val billImage = BillImageBean()
            billImage.objectId = StringUtil.getRandomString()
            billImage.imageId = StringUtil.getRandomString()
            billImage.billId = billId
            billImage.imageSourceName =
                "${StringUtil.getRandomString()}_${System.currentTimeMillis()}.${
                    getFileSuffix(file.name)
                }"
            billImage.imageCoverName =
                "${StringUtil.getRandomString()}_${System.currentTimeMillis()}.${
                    getFileSuffix(file.name)
                }"
            billImage.imageThumbnailName =
                "${StringUtil.getRandomString()}_${System.currentTimeMillis()}.${
                    getFileSuffix(file.name)
                }"
            billImage.imageUser = getUserId(context)

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "sourceFile",
                    file.name,
                    file.asRequestBody("application/octet-stream".toMediaType())
                )
                .addFormDataPart(
                    "billImage",
                    Gson().toJson(billImage)
                ).build()
            val sharedPreferences = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
            val userId = sharedPreferences.getString("UserId", null)
            return if (userId != null) {
                val client = OKHttpUtil.getInstance
                val request = Request.Builder()
                    .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UploadBillImage")
                    .addHeader("Token", getUserId(context)!!)
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val str = response.body!!.string()
                val result = Gson().fromJson(
                    str,
                    NewResult::class.java
                )
                result.result
            } else {
                false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return false
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            return false
        }
    }


    /**
     * 保存账单
     *
     * @param billBean 账单原型
     * @return 是否保存成功
     */
    fun saveBill(billBean: BillBean, context: Context): Boolean {
        val requestBody =
            Gson().toJson(billBean).toRequestBody("application/json; charset=UTF-8".toMediaType())
        val request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/AddBill")
            .addHeader("Token",getUserId(context)!!)
            .post(requestBody)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }

    @kotlin.jvm.Throws(Exception::class)
    private fun getFileSuffix(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1)
    }


    /**
     * 添加分类
     *
     * @param context      Context
     * @param name         分类名称
     * @param billProperty 收入/支出
     * @return 是否添加成功
     * @throws IOException     IOException
     * @throws SQLiteException SQLiteException
     */
    @Throws(IOException::class, SQLiteException::class)
    fun addCategory(context: Context, name: String?, billProperty: Int): NewResult {
        val bean = BillCategory()
        bean.objectId = StringUtil.getRandomString()
        bean.categoryId = StringUtil.getRandomString()
        bean.categoryUser = getUserId(context)
        bean.categoryName = name
        bean.isIncome = billProperty
        bean.categoryStatus = 1
        bean.categoryType = 0
        bean.createTime = System.currentTimeMillis()
        bean.updateTime = 0
        val requestBody =
            Gson().toJson(bean).toRequestBody("application/json; charset=UTF-8".toMediaType())
        val request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/AddBillCategory")
            .post(requestBody)
            .addHeader("Token", getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        return Gson().fromJson(response.body!!.string(), NewResult::class.java)
    }


    /**
     * 添加账户
     *
     * @param context Context
     * @param name    账户名称
     * @return 是否添加成功
     * @throws IOException     IOException
     * @throws SQLiteException SQLiteException
     */
    @Throws(IOException::class, SQLiteException::class)
    fun addAccount(context: Context, name: String?): NewResult {
        val bean = BillAccount()
        bean.objectId = StringUtil.getRandomString()
        bean.accountId = StringUtil.getRandomString()
        bean.accountUser = getUserId(context)
        bean.accountName = name
        bean.accountStatus = 1
        bean.accountType = 0
        bean.createTime = System.currentTimeMillis()
        bean.updateTime = 0
        val requestBody =
            Gson().toJson(bean).toRequestBody("application/json; charset=UTF-8".toMediaType())
        val request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/AddBillAccount")
            .post(requestBody)
            .addHeader("Token", getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        return Gson().fromJson(response.body!!.string(), NewResult::class.java)
    }

    fun getBillCategoryAndAccount(context: Context): BillCategoryAndBillAccount? {
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
}
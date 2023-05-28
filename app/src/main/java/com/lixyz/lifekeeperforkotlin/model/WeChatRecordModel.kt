package com.lixyz.lifekeeperforkotlin.model

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.WeChatRecordBean
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.WeChatRecordContactBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Type

class WeChatRecordModel {
    fun getRecords(
        userId: String,
        contactId: String
    ): ArrayList<WeChatRecordBean>? {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetWeChatRecordByContactId")
            .addHeader("Token", userId)
            .addHeader("ContactId", contactId)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val listType: Type =
                object : TypeToken<ArrayList<WeChatRecordBean>>() {}.type
            val jsonString = Gson().toJson(result.resultObject)
            Gson().fromJson(JsonParser.parseString(jsonString), listType)
        } else {
            null
        }
    }

    fun getContactNames(userId: String): ArrayList<WeChatRecordContactBean>? {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetWeChatContactName")
            .addHeader("Token", userId)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val listType: Type =
                object : TypeToken<ArrayList<WeChatRecordContactBean>>() {}.type
            val jsonString = Gson().toJson(result.resultObject)
            Gson().fromJson(JsonParser.parseString(jsonString), listType)
        } else {
            null
        }
    }

    fun deleteContact(userId: String, checkList: MutableList<String>): Boolean {
        val requestBody =
            Gson().toJson(checkList).toRequestBody("application/json; charset=UTF-8".toMediaType())
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteWeChatContact")
            .addHeader("Token", userId)
            .post(requestBody)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }
}
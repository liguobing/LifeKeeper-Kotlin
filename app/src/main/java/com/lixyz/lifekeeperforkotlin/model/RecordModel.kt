package com.lixyz.lifekeeperforkotlin.model

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.ContactBean
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.RecordBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class RecordModel {
    fun getRecords(
        userId: String,
        contactId: String
    ): ArrayList<RecordBean>? {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetRecordByContactId")
            .addHeader("Token", userId)
            .addHeader("ContactId", contactId)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val listType: Type =
                object : TypeToken<ArrayList<RecordBean>>() {}.type
            val jsonString = Gson().toJson(result.resultObject)
            Gson().fromJson(JsonParser.parseString(jsonString), listType)
        } else {
            null
        }
    }

    fun getContactNames(userId: String): ArrayList<ContactBean>? {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetContactName")
            .addHeader("Token", userId)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val listType: Type =
                object : TypeToken<ArrayList<ContactBean>>() {}.type
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
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteContact")
            .addHeader("Token", userId)
            .post(requestBody)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }
}
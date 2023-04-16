package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.photo.MoveImageBean
import com.lixyz.lifekeeperforkotlin.bean.photo.PageImageBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Type


class ImageThumbnailModel {

    private fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
    }

    fun deleteFile(context: Context, objectIdList: ArrayList<String>): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteImage")
            .addHeader("Token",getUserId(context)!!)
            .post(
                Gson().toJson(objectIdList)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }

    fun loadMoreImage(
        context: Context,
        categoryId: String,
        password: String,
        offset: Int,
        rows: Int
    ): PageImageBean? {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/GetImages?cp=$offset&ps=$rows"
            )
            .addHeader("Token", getUserId(context)!!)
            .addHeader("CategoryId", categoryId)
            .addHeader("Password", password)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val jsonString = Gson().toJson(result.resultObject)
            Gson().fromJson(JsonParser.parseString(jsonString),PageImageBean::class.java)
        } else {
            null
        }
    }

    fun setCategoryPrivateStatus(context: Context, objectId: String, privateStatus: Int): Boolean {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateCategoryPrivateStatus?objectId=$objectId&privateStatus=$privateStatus"
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            result.resultObject as Boolean
        } else {
            false
        }
    }

    fun moveImage(context: Context, moveImageBean: MoveImageBean): Boolean {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/MoveImages"
            )
            .post(
                Gson().toJson(moveImageBean)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .addHeader("Token",getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val str = response.body!!.string()
        val result = Gson().fromJson(str, NewResult::class.java)
        return if (result.result) {
            result.resultObject as Boolean
        } else {
            false
        }
    }

    fun getImageData(
        context: Context,
        imageCategoryId: String?,
        offset: Int,
        rows: Int
    ): ArrayList<ImageBean>? {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/GetRangeImageByCategoryId?categoryId=$imageCategoryId&userId=${
                    getUserId(
                        context
                    )
                }&offset=$offset&rows=$rows"
            )
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val listType: Type =
                object : TypeToken<ArrayList<ImageBean>>() {}.type
            val jsonString = Gson().toJson(result.resultObject)
            Gson().fromJson(JsonParser.parseString(jsonString), listType)
        } else {
            null
        }
    }

    fun getOtherCategory(
        context: Context,
        categoryId: String
    ): ArrayList<ImageCategoryBean>? {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/GetOtherImageCategory"
            )
            .addHeader("Token",getUserId(context)!!)
            .addHeader("CategoryId",categoryId)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val listType: Type =
                object : TypeToken<ArrayList<ImageCategoryBean>>() {}.type
            val jsonString = Gson().toJson(result.resultObject)
            Gson().fromJson(JsonParser.parseString(jsonString), listType)
        } else {
            null
        }
    }

    fun addNewCategory(context: Context, category: ImageCategoryBean): Boolean {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/AddImageCategory"
            )
            .addHeader("Token",getUserId(context)!!)
            .post(Gson().toJson(category)
                .toRequestBody("application/json; charset=UTF-8".toMediaType()))
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }
}
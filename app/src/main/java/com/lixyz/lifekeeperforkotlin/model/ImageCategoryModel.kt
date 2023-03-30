package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import cn.hutool.core.lang.UUID
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.photo.ImageCategoryCover
import com.lixyz.lifekeeperforkotlin.net.CountingRequestBody
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.activity.IImageCategoryView
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Type
import java.math.BigInteger
import java.net.SocketTimeoutException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit


class ImageCategoryModel {

    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
    }

    fun getImageCategoryCover(context: Context): ArrayList<ImageCategoryCover>? {
        val userId = getUserId(context)
        if (userId == null) {
            return null
        } else {
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetImageCategoryAndCover")
                .addHeader("Token", userId)
                .build()
            val client = OKHttpUtil.getInstance
            try {
                val response = client.newCall(request).execute()
                val str = response.body!!.string()
                val result = Gson().fromJson(str, NewResult::class.java)
                return if (result.result) {
                    val listType: Type =
                        object : TypeToken<ArrayList<ImageCategoryCover>>() {}.type
                    val jsonString = Gson().toJson(result.resultObject)
                    Gson().fromJson(JsonParser.parseString(jsonString), listType)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    fun checkImageCategoryPassword(
        context: Context,
        categoryId: String,
        password: String
    ): Boolean {
        val userId = getUserId(context)
        if (userId == null) {
            return false
        } else {
            try {
                val request: Request = Request.Builder()
                    .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/VerifyImageCategoryPassword")
                    .addHeader("Token", userId)
                    .addHeader("CategoryId", categoryId)
                    .addHeader("Password", password)
                    .build()
                val client = OKHttpUtil.getInstance
                val response = client.newCall(request).execute()
                val str = response.body!!.string()
                val result = Gson().fromJson(str, NewResult::class.java)
                return result.result
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }

    fun deleteCategory(context: Context,categoryId: String): Boolean {
        try {
            val userId = getUserId(context)
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteImageCategory")
                .addHeader("Token", userId!!)
                .addHeader("CategoryId", categoryId)
                .build()
            val client = OKHttpUtil.getInstance
            val response = client.newCall(request).execute()
            val str = response.body!!.string()
            val result = Gson().fromJson(str, NewResult::class.java)
            return result.result
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun updateImageCategory(context: Context,bean: ImageCategoryBean): Boolean {
        try {
            val userId = getUserId(context)
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/EditImageCategory")
                .addHeader("Token", userId!!)
                .post(Gson().toJson(bean).toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .build()
            val client = OKHttpUtil.getInstance
            val response = client.newCall(request).execute()
            val str = response.body!!.string()
            val result = Gson().fromJson(str, NewResult::class.java)
            return result.result
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun uploadLocalImageFileToCloud(
        context: Context,
        cacheFile: File,
        index: Int,
        count: Int,
        createTime: Long,
        view: IImageCategoryView
    ): Int {
        try {
            val imageBean = ImageBean()
            imageBean.objectId = StringUtil.getRandomString()
            imageBean.imageId = StringUtil.getRandomString()
            imageBean.sha1 = getFileSha1(cacheFile)
            imageBean.originalFileName = cacheFile.name
            imageBean.sourceFileName =
                "${StringUtil.getRandomString()}_${System.currentTimeMillis()}.${
                    getFileSuffix(cacheFile.name)
                }"
            imageBean.coverFileName = UUID.randomUUID().toString(true) + ".webp"
            imageBean.thumbnailFileName = UUID.randomUUID().toString(true) + ".webp"
            imageBean.blurFileName = UUID.randomUUID().toString(true) + ".webp"
            imageBean.createTime = createTime
            imageBean.imageUser = getUserId(context)
            imageBean.imageStatus = 1
            imageBean.imageType = 0
            imageBean.updateTime = 0
            val requestBody = CountingRequestBody(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "sourceFile",
                        cacheFile.name,
                        cacheFile.asRequestBody("application/octet-stream".toMediaType())
                    )
                    .addFormDataPart(
                        "imageBean",
                        Gson().toJson(imageBean)
                    ).build()
            ) { max, value ->
                val progress = (value * 100 / max).toInt()
                view.updateUploadDialog(
                    "第 ${index + 1} 个，共 $count 个",
                    progress
                )
            }
            val sharedPreferences = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
            val userId = sharedPreferences.getString("UserId", null)
            return if (userId != null) {
                val client = OKHttpUtil.getInstance
                val request = Request.Builder()
                    .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UploadImage")
                    .addHeader("Token",getUserId(context)!!)
                    .post(requestBody)
                    .build()
                val response = client.newCall(request).execute()
                val str = response.body!!.string()
                val result = Gson().fromJson(
                    str,
                    NewResult::class.java
                )
                if (result.result) {
                    val gson = Gson()
                    gson.fromJson(gson.toJson(result.resultObject), Int::class.java)
                } else {
                    0
                }
            } else {
                0
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return 0
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return 0
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            return 0
        }
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun getFileSha1(file: File?): String {
        val fis = FileInputStream(file)
        val digest: MessageDigest = MessageDigest.getInstance("SHA-1")
        val buffer = ByteArray(1024 * 1024 * 10)
        var len: Int
        while (fis.read(buffer).also { len = it } > 0) {
            digest.update(buffer, 0, len)
        }
        var sha1: String = BigInteger(1, digest.digest()).toString(16)
        val length = 40 - sha1.length
        if (length > 0) {
            for (i in 0 until length) {
                sha1 = "0$sha1"
            }
        }
        fis.close()
        return sha1
    }

    @kotlin.jvm.Throws(Exception::class)
    private fun getFileSuffix(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1)
    }
}
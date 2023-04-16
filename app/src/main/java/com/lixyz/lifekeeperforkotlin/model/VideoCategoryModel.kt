package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.video.VideoCategoryCover
import com.lixyz.lifekeeperforkotlin.net.CountingRequestBody
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.activity.IVideoCategoryView
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Type
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class VideoCategoryModel {

    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
            .getString("UserId", null)
    }

    fun getVideoCategoryCover(context: Context): ArrayList<VideoCategoryCover>? {
        val userId = getUserId(context)
        if (userId == null) {
            return null
        } else {
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/GetVideoCategoryAndCover")
                .addHeader("Token", userId)
                .build()
            val client = OKHttpUtil.getInstance
            try {
                val response = client.newCall(request).execute()
                val str = response.body!!.string()
                val result = Gson().fromJson(str, NewResult::class.java)
                return if (result.result) {
                    val listType: Type =
                        object : TypeToken<ArrayList<VideoCategoryCover>>() {}.type
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

    fun checkVideoCategoryPassword(
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
                    .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/VerifyVideoCategoryPassword")
                    .addHeader("Token", userId)
                    .addHeader("CategoryId", categoryId)
                    .addHeader("Password", StringUtil.string2MD5("", password)!!)
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

    fun deleteCategory(context: Context, categoryId: String): Boolean {
        try {
            val userId = getUserId(context)
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteVideoCategory")
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

    fun updateVideoCategory(context: Context, bean: VideoCategoryBean): Boolean {
        try {
            val userId = getUserId(context)
            val request: Request = Request.Builder()
                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/EditVideoCategory")
                .addHeader("Token", userId!!)
                .post(
                    Gson().toJson(bean)
                        .toRequestBody("application/json; charset=UTF-8".toMediaType())
                )
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

    /**
     * 上传视频
     */
    @kotlin.jvm.Throws(Exception::class)
    fun uploadLocalVideoToCloud(
        context: Context,
        sourceFile: File,
        fileIndex: Int,
        fileCount: Int,
        duration: Long,
        createTime: Long,
        view: IVideoCategoryView
    ): Boolean {
        try {
            val videoBean = VideoBean()
            videoBean.objectId = StringUtil.getRandomString()
            videoBean.videoId = StringUtil.getRandomString()
            videoBean.sha1 = getFileSha1(sourceFile)
            videoBean.duration = duration
            videoBean.originalFileName = sourceFile.name
            videoBean.sourceFileName =
                "${StringUtil.getRandomString()}_${System.currentTimeMillis()}.${
                    getFileSuffix(sourceFile.name)
                }"
            videoBean.coverFileName =
                "${StringUtil.getRandomString()}_${System.currentTimeMillis()}.webp"
            videoBean.thumbnailFileName =
                "${StringUtil.getRandomString()}_${System.currentTimeMillis()}.webp"
            videoBean.blurFileName =
                "${StringUtil.getRandomString()}_${System.currentTimeMillis()}.webp"
            videoBean.videoUser = getUserId(context)
            videoBean.videoStatus = 1
            videoBean.createTime = createTime


            val requestBody = CountingRequestBody(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "sourceFile",
                        sourceFile.name,
                        sourceFile.asRequestBody("application/octet-stream".toMediaType())
                    )
                    .addFormDataPart(
                        "videoBean",
                        Gson().toJson(videoBean)
                    ).build()
            ) { max, value ->
                val progress = (value * 100 / max).toInt()
                view.updateUploadDialog(
                    "第 $fileIndex 个，共 $fileCount 个",
                    progress
                )
            }
            val sharedPreferences = context.getSharedPreferences(
                "LoginConfig",
                Context.MODE_PRIVATE
            )
            val userId = sharedPreferences.getString("UserId", null)
            return if (userId != null) {
                val client = OKHttpUtil.getInstance
                val request = Request.Builder()
                    .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UploadVideo")
                    .addHeader("Token", getUserId(context)!!)
                    .post(requestBody)
                    .build()
                val response = client.newCall(request).execute()
                val gson = Gson()
                val result = gson.fromJson(
                    response.body!!.string(),
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
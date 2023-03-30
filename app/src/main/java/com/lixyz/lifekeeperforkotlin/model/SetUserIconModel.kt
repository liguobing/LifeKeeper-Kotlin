package com.lixyz.lifekeeperforkotlin.model

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.user.UpdateUserBean
import com.lixyz.lifekeeperforkotlin.bean.user.UserImageResponse
import com.lixyz.lifekeeperforkotlin.bean.user.UserResponseBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class SetUserIconModel {

    /**
     * 上传图片到云端
     *
     * @param context Context
     * @param file    图片文件
     * @return 上传响应
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    fun uploadFileToCloud(context: Context, file: File): UserImageResponse {
        val userId =
            context.getSharedPreferences("LoginConfig", Activity.MODE_PRIVATE)
                .getString("UserId", null)!!
        val userImageResponse = UserImageResponse()
        val client = OKHttpUtil.getInstance
        val image = file.asRequestBody("application/octet-stream".toMediaType())
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "-$userId-${file.name}", image)
            .build()
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UploadUserIcon")
            .post(requestBody)
            .build()
        val response: Response = client.newCall(request).execute()
        if (response.body != null) {
            val responseBody: ResponseBody = response.body!!
            return Gson().fromJson<UserImageResponse>(
                responseBody.string(),
                UserImageResponse::class.java
            )
        }
        return userImageResponse
    }

    fun updateUser(context: Context, userIconUrl: String): Boolean {
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val objectId = config.getString("ObjectId", null)
        val newObjectId = StringUtil.getRandomString()
        val currentTime = System.currentTimeMillis()
        val edit = config.edit()
        edit.putString("ObjectId", newObjectId)
        edit.putString("UserIconUrl", "http://104.245.40.124:8080/UserImage/$userIconUrl")
        val client = OKHttpUtil.getInstance
        var request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByObjectId?objectId=$objectId")
            .build()
        var response = client.newCall(request).execute()
        if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            if (userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0) {
                val user = userResponseBean.responseList!![0]
                user.objectId = newObjectId
                user.createTime = currentTime
                user.userIconUrl = "http://104.245.40.124:8080/UserImage/$userIconUrl"
                val updateUserBean = UpdateUserBean()
                updateUserBean.oldUserObjectId = objectId
                updateUserBean.oldUserUpdateTime = currentTime
                updateUserBean.newUser = user
                val requestBody = Gson().toJson(updateUserBean)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                request = Request.Builder().url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateUser")
                    .post(requestBody).build()
                response = client.newCall(request).execute()
                return if (response.body!!.string().toBoolean()) {
                    val result = edit.commit()
                    result
                } else {
                    false
                }
            } else {
                return false
            }
        } else {
            return false
        }
    }
}
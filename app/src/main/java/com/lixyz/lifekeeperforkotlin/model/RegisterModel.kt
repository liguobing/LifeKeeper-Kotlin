package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.EditText
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class RegisterModel {

    @Throws(Exception::class)
    fun saveUserToCloud(
        context: Context,
        etUserName: EditText?,
        etPhone: EditText?,
        etPassword: EditText
    ): Boolean {
        val user = UserBean()
        user.objectId = StringUtil.getRandomString()
        user.userId = StringUtil.getRandomString()
        user.userPhone = etPhone!!.text.toString().trim()
        user.userIconUrl = "user_icon_default.webp"
        user.userName = etUserName!!.text.toString().trim()
        user.userPassword =
            StringUtil.string2MD5(etPhone.text.toString().trim(), etPassword.text.toString().trim())
        user.userStatus = 1
        user.userType = 0
        user.createTime = System.currentTimeMillis()
        user.updateTime = 0
        val loginConfig = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val edit = loginConfig.edit()
        edit.putString("ObjectId", user.objectId)
        edit.putString("UserId", user.userId)
        edit.putString("UserName", user.userName)
        edit.putString("UserPhone", user.userPhone)
        edit.putString("UserIconUrl", user.userIconUrl)
        edit.putString("UserBindWeibo", user.userBindWeibo)
        edit.putString("UserBindWeiboAccessToken", user.userBindWeiboAccessToken)
        edit.putString("UserBindWeiboIcon", user.userBindWeiboIcon)
        edit.putString("UserBindWeiboExpiresTime", user.userBindWeiboExpiresTime)
        edit.putString("UserBindWeiboId", user.userBindWeiboId)
        edit.putString("UserBindQQ", user.userBindQQ)
        edit.putString("UserBindQQOpenId", user.userBindQQOpenId)
        edit.putString("UserBindQQExpiresTime", user.userBindQQExpiresTime)
        edit.putString("UserBindQQAccessToken", user.userBindQQAccessToken)
        edit.putString("UserBindQQIcon", user.userBindQQIcon)
        val client = OKHttpUtil.getInstance
        val requestBody: RequestBody =
            Gson().toJson(user).toRequestBody("application/json; charset=utf-8".toMediaType())
        val addUserRequest: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/AddUser"
        ).post(requestBody).build()
        val resp: Response = client.newCall(addUserRequest).execute()
        val result = Gson().fromJson(resp.body!!.string(), NewResult::class.java)
        return if (result.result) {
            val objectResult = result.resultObject as Boolean
            if (objectResult) {
                edit.apply()
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun requestSMSCode(phone: String, token: String): NewResult {
        val client = OKHttpUtil.getInstance
        val addUserRequest: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/RegisterRequestSMSCode?phone=$phone&token=$token"
        ).build()
        val resp: Response = client.newCall(addUserRequest).execute()
        return Gson().fromJson(resp.body!!.string(), NewResult::class.java)
    }

    fun verifyCode(stamp: String, phone: String, code: String): Boolean {
        val client = OKHttpUtil.getInstance
        val addUserRequest: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/VerifySMSCode?phone=$phone&code=$code&codeStamp=$stamp"
        ).build()
        val resp: Response = client.newCall(addUserRequest).execute()
        return Gson().fromJson(resp.body!!.string(), NewResult::class.java).result
    }
}
package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
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
import java.io.IOException

class VerifyCodeModel {
    /**
     * 检查手机是否已经注册过
     */
    @Throws(IOException::class)
    fun checkPhoneIsRegistered(context: Context, phone: String): Boolean {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/PhoneIsRegistered?phone=$phone"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val result =
                Gson().fromJson(response.body!!.string(), NewResult::class.java)
            return if (result.result) {
                if (result.resultObject != null) {
                    result.resultObject as Boolean
                } else {
                    false
                }
            } else {
                false
            }
        }
        return false
    }

    fun resetPassword(context: Context, phone: String, password: String): Boolean {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/FindPassword?phone=$phone&newPassword=${
                StringUtil.string2MD5(
                    phone,
                    password
                )
            }"
        ).build()
        val resp = client.newCall(request).execute()
        val result = Gson().fromJson(resp.body!!.string(), NewResult::class.java)
        return result.result
    }

    fun requestSMSCode(phone: String, token: String): NewResult {
        val client = OKHttpUtil.getInstance
        val addUserRequest: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/RegisterRequestSMSCode?phone=$phone&token=$token"
        ).build()
        val resp: Response = client.newCall(addUserRequest).execute()
        return Gson().fromJson(resp.body!!.string(), NewResult::class.java)
    }

    fun resetPasswordRequestSMSCode(phone: String, token: String): NewResult {
        val client = OKHttpUtil.getInstance
        val addUserRequest: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/ResetPasswordRequestSMSCode?phone=$phone&token=$token"
        ).build()
        val resp: Response = client.newCall(addUserRequest).execute()
        return Gson().fromJson(resp.body!!.string(), NewResult::class.java)
    }

    fun verifyCode(
        context: Context,
        user: UserBean,
        stamp: String,
        phone: String,
        code: String
    ): Boolean {
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
            "${Constant.ADDRESS}/LifeKeeper/ThirdPartyLoginBindPhone"
        )
            .addHeader("Phone", phone)
            .addHeader("Code", code)
            .addHeader("CodeStamp", stamp)
            .post(requestBody).build()
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

    fun verifyCode(
        stamp: String,
        phone: String,
        code: String
    ): Boolean {
        val client = OKHttpUtil.getInstance
        val addUserRequest: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/VerifySMSCode?phone=$phone&code=$code&codeStamp=$stamp"
        ).build()
        val resp: Response = client.newCall(addUserRequest).execute()
        val result = Gson().fromJson(resp.body!!.string(), NewResult::class.java)
        return result.result
    }

    fun verifyCodeLogin(
        context: Context,
        stamp: String,
        phone: String,
        code: String
    ): NewResult? {
        val client = OKHttpUtil.getInstance
        val addUserRequest: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/VerifyCodeLogin?phone=$phone&code=$code&stamp=$stamp"
        ).build()
        val resp: Response = client.newCall(addUserRequest).execute()
        return Gson().fromJson(resp.body!!.string(), NewResult::class.java)
    }
}
package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import okhttp3.Request
import java.io.IOException


/**
 * 登录/注册 页面模型
 *
 * @author LGB
 */
class LoginModel {

    companion object {
        fun putDouble(
            edit: SharedPreferences.Editor,
            key: String,
            value: Double
        ): SharedPreferences.Editor? {
            return edit.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        }
    }

    /**
     * 根据手机号和密码查找用户
     */
    @Throws(IOException::class)
    fun selectUserByPassword(context: Context, phone: String, password: String): UserBean? {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/Login?phone=$phone&password=${StringUtil.string2MD5(phone,password)}"
        ).build()
        val resp = client.newCall(request).execute()
        val result = Gson().fromJson(resp.body!!.string(), NewResult::class.java)
        try {
            return if (result.result) {
                if (result.resultObject != null) {
                    return Gson().fromJson(Gson().toJson(result.resultObject), UserBean::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 保存用户信息到本地 SP
     */
    fun saveUserToLocal(context: Context, userBean: UserBean?): Boolean {
        val loginConfig = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val edit = loginConfig.edit()
        edit.putString("ObjectId", userBean!!.objectId)
        edit.putString("UserId", userBean.userId)
        edit.putString("UserName", userBean.userName)
        edit.putString("UserPhone", userBean.userPhone)
        edit.putString("UserIconUrl", userBean.userIconUrl)
        edit.putString("UserBindWeibo", userBean.userBindWeibo)
        edit.putString("UserBindWeiboAccessToken", userBean.userBindWeiboAccessToken)
        edit.putString("UserBindWeiboIcon", userBean.userBindWeiboIcon)
        edit.putString("UserBindWeiboExpiresTime", userBean.userBindWeiboExpiresTime)
        edit.putString("UserBindWeiboId", userBean.userBindWeiboId)
        edit.putString("UserBindQQ", userBean.userBindQQ)
        edit.putString("UserBindQQOpenId", userBean.userBindQQOpenId)
        edit.putString("UserBindQQExpiresTime", userBean.userBindQQExpiresTime)
        edit.putString("UserBindQQAccessToken", userBean.userBindQQAccessToken)
        edit.putString("UserBindQQIcon", userBean.userBindQQIcon)
        return edit.commit()
    }



    /**
     * 通过微博 ID 查找用户
     */
    @Throws(IOException::class)
    fun selectUserByWeiboId(context: Context, weiboId: String): UserBean? {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/GetUserByWeiboId?userBindWeiboId=$weiboId"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val result =
                Gson().fromJson(response.body!!.string(), NewResult::class.java)
            return if (result.result) {
                if (result.resultObject != null) {
                    val jsonString = Gson().toJson(result.resultObject)
                    Gson().fromJson(JsonParser.parseString(jsonString), UserBean::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        }
        return null
    }

    /**
     * 通过 QQ ID 查找用户
     */
    @Throws(IOException::class)
    fun selectUserByQQId(context: Context, qqId: String): UserBean? {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.ADDRESS}/LifeKeeper/GetUserByQQId?userBindQQId=$qqId"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val result =
                Gson().fromJson(response.body!!.string(), NewResult::class.java)
            return if (result.result) {
                if (result.resultObject != null) {
                    val jsonString = Gson().toJson(result.resultObject)
                    Gson().fromJson(JsonParser.parseString(jsonString), UserBean::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        }
        return null
    }
}
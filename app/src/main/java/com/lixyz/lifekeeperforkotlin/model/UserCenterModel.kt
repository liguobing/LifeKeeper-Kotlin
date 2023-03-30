package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.sqlite.SQLiteException
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.bean.user.UpdateUserBean
import com.lixyz.lifekeeperforkotlin.bean.user.UserResponseBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * @author LGB
 * 用户中心模型
 */
class UserCenterModel(context: Context) {

    /**
     * 获取用户信息
     */
    fun getUserInfo(context: Context): UserBean {
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val user = UserBean()
        user.userId = config.getString("UserId", null)
        user.userName = config.getString("UserName", null)
        user.userPhone = config.getString("UserPhone", null)
        user.userIconUrl = config.getString("UserIconUrl", null)
        user.userBindWeiboId = config.getString("UserBindWeiboId", null)
        user.userBindQQOpenId = config.getString("UserBindQQOpenId", null)
        return user
    }


    /**
     * 退出登录
     */
    @Throws(SQLiteException::class)
    fun logout(context: Context): Boolean {
        val editor = context.getSharedPreferences("LoginConfig", MODE_PRIVATE).edit()
        editor.clear()
        return editor.commit()
    }


    /**
     * 绑定微博
     */
    @Throws(IOException::class)
    fun bindWeibo(
        context: Context,
        userBindWeibo: String,
        userBindWeiboExpiresTime: String,
        userBindWeiboAccessToken: String,
        userBindWeiboIcon: String,
        userBindWeiboId: String
    ): Boolean {
        var result = false
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val oldObjectId = config.getString("ObjectId", null)!!
        val newObjectId = StringUtil.getRandomString()
        val currentTime = System.currentTimeMillis()
        val edit = config.edit()
        edit.putString("ObjectId", newObjectId)
        edit.putString("UserBindWeibo", userBindWeibo)
        edit.putString("UserBindWeiboAccessToken", userBindWeiboAccessToken)
        edit.putString("UserBindWeiboIcon", userBindWeiboIcon)
        edit.putString("UserBindWeiboExpiresTime", userBindWeiboExpiresTime)
        edit.putString("UserBindWeiboId", userBindWeiboId)
        val client = OKHttpUtil.getInstance
        var request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByObjectId?objectId=$oldObjectId"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            if (userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0) {
                val user = userResponseBean.responseList!![0]
                user.objectId = newObjectId
                user.userBindWeibo = userBindWeibo
                user.userBindWeiboId = userBindWeiboId
                user.userBindWeiboIcon = userBindWeiboIcon
                user.userBindWeiboExpiresTime = userBindWeiboExpiresTime
                user.userBindWeiboAccessToken = userBindWeiboAccessToken
                user.createTime = currentTime
                user.updateTime = 0
                val updateUserBean = UpdateUserBean()
                updateUserBean.newUser = user
                updateUserBean.oldUserObjectId = oldObjectId
                updateUserBean.oldUserUpdateTime = currentTime
                val requestBody = Gson().toJson(updateUserBean)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                request = Request.Builder().url(
                    "${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateUser"
                ).post(requestBody).build()
                val updateResponse = client.newCall(request).execute()
                if (updateResponse.body != null) {
                    result = updateResponse.body!!.string().toBoolean()
                }
            }
        }
        if (result) {
            result = edit.commit()
        }
        return result
    }

    /**
     * 解绑定微博
     */
    fun unBindWeibo(context: Context): Boolean {
        var result = false
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val oldObjectId = config.getString("ObjectId", null)!!
        val newObjectId = StringUtil.getRandomString()
        val currentTime = System.currentTimeMillis()
        val edit = config.edit()
        edit.putString("ObjectId", newObjectId)
        edit.remove("UserBindWeibo")
        edit.remove("UserBindWeiboAccessToken")
        edit.remove("UserBindWeiboIcon")
        edit.remove("UserBindWeiboExpiresTime")
        edit.remove("UserBindWeiboId")
        val client = OKHttpUtil.getInstance
        var request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByObjectId?objectId=$oldObjectId"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            if (userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0) {
                val user = userResponseBean.responseList!![0]
                user.objectId = newObjectId
                user.userBindWeibo = null
                user.userBindWeiboId = null
                user.userBindWeiboIcon = null
                user.userBindWeiboExpiresTime = null
                user.userBindWeiboAccessToken = null
                user.createTime = currentTime
                user.updateTime = 0
                val updateUserBean = UpdateUserBean()
                updateUserBean.newUser = user
                updateUserBean.oldUserObjectId = oldObjectId
                updateUserBean.oldUserUpdateTime = currentTime
                val requestBody = Gson().toJson(updateUserBean)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                request = Request.Builder().url(
                    "${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateUser"
                ).post(requestBody).build()
                val updateResponse = client.newCall(request).execute()
                if (updateResponse.body != null) {
                    result = updateResponse.body!!.string().toBoolean()
                }
            }
        }
        if (result) {
            result = edit.commit()
        }
        return result
    }

    /**
     * 绑定 QQ
     */
    fun bindQQ(
        context: Context,
        userBindQQOpenId: String,
        userBindQQAccessToken: String,
        userBindQQExpiresTime: String,
        userBindQQ: String,
        userBindQQIcon: String
    ): Boolean {
        var result = false
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val oldObjectId = config.getString("ObjectId", null)!!
        val newObjectId = StringUtil.getRandomString()
        val currentTime = System.currentTimeMillis()
        val edit = config.edit()
        edit.putString("ObjectId", newObjectId)
        edit.putString("UserBindQQ", userBindQQ)
        edit.putString("UserBindQQOpenId", userBindQQOpenId)
        edit.putString("UserBindQQExpiresTime", userBindQQExpiresTime)
        edit.putString("UserBindQQAccessToken", userBindQQAccessToken)
        edit.putString("UserBindQQIcon", userBindQQIcon)
        val client = OKHttpUtil.getInstance
        var request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByObjectId?objectId=$oldObjectId"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            if (userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0) {
                val user = userResponseBean.responseList!![0]
                user.objectId = newObjectId
                user.userBindQQ = userBindQQ
                user.userBindQQIcon = userBindQQIcon
                user.userBindQQExpiresTime = userBindQQExpiresTime
                user.userBindQQAccessToken = userBindQQAccessToken
                user.userBindQQOpenId = userBindQQOpenId
                user.createTime = currentTime
                user.updateTime = 0
                val updateUserBean = UpdateUserBean()
                updateUserBean.newUser = user
                updateUserBean.oldUserObjectId = oldObjectId
                updateUserBean.oldUserUpdateTime = currentTime
                val requestBody = Gson().toJson(updateUserBean)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                request = Request.Builder().url(
                    "${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateUser"
                ).post(requestBody).build()
                val updateResponse = client.newCall(request).execute()
                if (updateResponse.body != null) {
                    result = updateResponse.body!!.string().toBoolean()
                }
            }
        }
        if (result) {
            result = edit.commit()
        }
        return result
    }

    /**
     * 解绑 QQ
     */
    fun unBindQQ(context: Context): Boolean {
        var result = false
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val oldObjectId = config.getString("ObjectId", null)!!
        val newObjectId = StringUtil.getRandomString()
        val currentTime = System.currentTimeMillis()
        val edit = config.edit()
        edit.putString("ObjectId", newObjectId)
        edit.remove("UserBindQQ")
        edit.remove("UserBindQQOpenId")
        edit.remove("UserBindQQExpiresTime")
        edit.remove("UserBindQQAccessToken")
        edit.remove("UserBindQQIcon")
        val client = OKHttpUtil.getInstance
        var request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByObjectId?objectId=$oldObjectId"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            if (userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0) {
                val user = userResponseBean.responseList!![0]
                user.objectId = newObjectId
                user.userBindQQ = null
                user.userBindQQOpenId = null
                user.userBindQQAccessToken = null
                user.userBindQQExpiresTime = null
                user.userBindQQIcon = null
                user.createTime = currentTime
                user.updateTime = 0
                val updateUserBean = UpdateUserBean()
                updateUserBean.newUser = user
                updateUserBean.oldUserObjectId = oldObjectId
                updateUserBean.oldUserUpdateTime = currentTime
                val requestBody = Gson().toJson(updateUserBean)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                request = Request.Builder().url(
                    "${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateUser"
                ).post(requestBody).build()
                val updateResponse = client.newCall(request).execute()
                if (updateResponse.body != null) {
                    result = updateResponse.body!!.string().toBoolean()
                }
            }
        }
        if (result) {
            result = edit.commit()
        }
        return result
    }

    /**
     * 检查是否只是第三方登录
     */
    fun isOnlyThirdPartyLogin(context: Context): Boolean {
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val phone = config.getString("UserPhone", null)
        return phone == null
    }

    /**
     * 修改密码
     */
    fun changePassword(context: Context, password: String): Boolean {
        var result = false
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val oldObjectId = config.getString("ObjectId", null)!!
        val phone = config.getString("UserPhone", null)!!
        val newObjectId = StringUtil.getRandomString()
        val currentTime = System.currentTimeMillis()
        val edit = config.edit()
        edit.putString("ObjectId", newObjectId)
        val client = OKHttpUtil.getInstance
        var request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByObjectId?objectId=$oldObjectId"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            if (userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0) {
                val user = userResponseBean.responseList!![0]
                user.objectId = newObjectId
                user.userPassword = StringUtil.string2MD5(phone, password)
                user.createTime = currentTime
                user.updateTime = 0
                val updateUserBean = UpdateUserBean()
                updateUserBean.newUser = user
                updateUserBean.oldUserObjectId = oldObjectId
                updateUserBean.oldUserUpdateTime = currentTime
                val requestBody = Gson().toJson(updateUserBean)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                request = Request.Builder().url(
                    "${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateUser"
                ).post(requestBody).build()
                val updateResponse = client.newCall(request).execute()
                if (updateResponse.body != null) {
                    result = updateResponse.body!!.string().toBoolean()
                }
            }
        }
        if (result) {
            result = edit.commit()
        }
        return result
    }

    /**
     * 修改用户名
     */
    fun changeUserName(context: Context, name: String): Boolean {
        var result = false
        val config = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val oldObjectId = config.getString("ObjectId", null)!!
        val newObjectId = StringUtil.getRandomString()
        val currentTime = System.currentTimeMillis()
        val edit = config.edit()
        edit.putString("ObjectId", newObjectId)
        edit.putString("UserName", name)
        val client = OKHttpUtil.getInstance
        var request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByObjectId?objectId=$oldObjectId"
        ).build()
        val response = client.newCall(request).execute()
        if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            if (userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0) {
                val user = userResponseBean.responseList!![0]
                user.objectId = newObjectId
                user.userName = name
                user.createTime = currentTime
                user.updateTime = 0
                val updateUserBean = UpdateUserBean()
                updateUserBean.newUser = user
                updateUserBean.oldUserObjectId = oldObjectId
                updateUserBean.oldUserUpdateTime = currentTime
                val requestBody = Gson().toJson(updateUserBean)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                request = Request.Builder().url(
                    "${Constant.CLOUD_ADDRESS}/LifeKeeper/UpdateUser"
                ).post(requestBody).build()
                val updateResponse = client.newCall(request).execute()
                if (updateResponse.body != null) {
                    result = updateResponse.body!!.string().toBoolean()
                }
            }
        }
        if (result) {
            result = edit.commit()
        }
        return result
    }

    /**
     * 电话是否注册过
     */
    fun phoneIsRegistered(phone: String): Boolean {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByPhone?phone=$phone"
        ).build()
        val response = client.newCall(request).execute()
        return if (response.body != null) {
            val userResponse =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            userResponse.responseCode > 0 && userResponse.responseList!!.size > 0
        } else {
            false
        }
    }


    /**
     * 检查微博是否已经绑定过其他账号
     */
    fun checkWeiboIsBind(weiboId: String): Boolean {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByWeiboId?userBindWeiboId=$weiboId"
        ).build()
        val response = client.newCall(request).execute()
        return if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0
        } else {
            false
        }
    }

    /**
     * 检查 QQ 是否已经绑定过其他账号
     */
    fun checkQQIsBind(qqId: String): Boolean {
        val client = OKHttpUtil.getInstance
        val request: Request = Request.Builder().url(
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/SelectUserByQQId?userBindQQId=$qqId"
        ).build()
        val response = client.newCall(request).execute()
        return if (response.body != null) {
            val userResponseBean =
                Gson().fromJson(response.body!!.string(), UserResponseBean::class.java)
            userResponseBean.responseCode > 0 && userResponseBean.responseList!!.size > 0
        } else {
            false
        }
    }
}

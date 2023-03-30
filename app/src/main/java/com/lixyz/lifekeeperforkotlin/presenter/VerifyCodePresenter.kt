package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.fastyotest.library.YoTestCaptchaVerify
import com.fastyotest.library.YoTestListener
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.model.VerifyCodeModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.activity.IVerifyCodeView
import com.lixyz.lifekeeperforkotlin.view.activity.VerifyCodeActivity
import com.lixyz.lifekeeperforkotlin.view.customview.SeparatedEditText
import java.io.IOException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class VerifyCodePresenter(
    private var view: IVerifyCodeView
) {

    /**
     * Model
     */
    private val model: VerifyCodeModel = VerifyCodeModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("绑定手机线程池"))

    private var yoTestCaptchaVerify: YoTestCaptchaVerify? = null


    /**
     * 请求验证码
     */
    fun thirdPartyLoginRequestSMSCode(
        context: Context,
        phone: EditText
    ) {
        if (TextUtils.isEmpty(phone.text)) {
            view.startErrorAnimation(phone)
            view.showSnakeBar("手机号不能为空")
            return
        }

        if (!StringUtil.isPhoneNumber(phone.text.toString().trim())) {
            view.startErrorAnimation(phone)
            view.showSnakeBar("手机号不合法")
            return
        }

        view.showWaitDialog()
        threadPool.execute {
            try {
                val checkResult =
                    model.checkPhoneIsRegistered(context, phone.text.toString().trim())
                if (checkResult) {
                    view.hideWaitDialog()
                    view.startErrorAnimation(phone)
                    view.showSnakeBar("该手机号已经注册过，可以直接登录，如需第三方登录，请登录后绑定")
                } else {
                    view.initYoTestCaptchaVerify()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                view.hideWaitDialog()
                view.startErrorAnimation(phone)
                view.showSnakeBar("获取验证码出错，请稍候重试")
            }
        }
    }

    var pageType: Int = 0

    fun initYoTestCaptchaVerify(activity: VerifyCodeActivity, pageType: Int) {
        this.pageType = pageType
        view.hideWaitDialog()
        yoTestCaptchaVerify = YoTestCaptchaVerify(activity, yoTestListener)
        yoTestCaptchaVerify!!.verify()
    }

    fun thirdPartyLoginCheckVerifyCode(
        context: Context,
        userBean: UserBean,
        etPhone: EditText?,
        seVerifyCode: SeparatedEditText?,
        btVerify: Button,
        stamp: String
    ) {
        if (TextUtils.isEmpty(etPhone!!.text) || !StringUtil.isPhoneNumber(
                etPhone.text.toString().trim()
            )
        ) {
            view.showSnakeBar("手机号不合法")
            view.startErrorAnimation(btVerify)
            return
        }

        if (TextUtils.isEmpty(seVerifyCode!!.text)) {
            view.showSnakeBar("验证码没有填写")
            view.startErrorAnimation(btVerify)
            return
        }

        view.showWaitDialog()
        threadPool.execute {
            val result = model.verifyCode(
                context,
                userBean,
                stamp,
                etPhone.text.toString().trim(),
                seVerifyCode.text.toString().trim()
            )
            if (result) {
                view.loginSuccess()
                view.hideWaitDialog()
            } else {
                view.hideWaitDialog()
                view.showSnackBar("微博绑定出错，请检查后重试")
            }
        }
    }

    fun verifyCodeLoginRequestSMSCode(context: Context, phone: EditText) {
        if (TextUtils.isEmpty(phone.text)) {
            view.startErrorAnimation(phone)
            view.showSnakeBar("手机号不能为空")
            return
        }

        if (!StringUtil.isPhoneNumber(phone.text.toString().trim())) {
            view.startErrorAnimation(phone)
            view.showSnakeBar("手机号不合法")
            return
        }

        view.showWaitDialog()
        threadPool.execute {
            try {
                view.initYoTestCaptchaVerify()
            } catch (e: IOException) {
                e.printStackTrace()
                view.hideWaitDialog()
                view.startErrorAnimation(phone)
                view.showSnakeBar("获取验证码出错，请稍候重试")
            }
        }

    }

    fun verifyCodeLoginCheckVerifyCode(
        context: Context,
        etPhone: EditText?,
        seVerifyCode: SeparatedEditText?,
        btVerify: Button,
        stamp: String
    ) {
        if (TextUtils.isEmpty(etPhone!!.text) || !StringUtil.isPhoneNumber(
                etPhone.text.toString().trim()
            )
        ) {
            view.showSnakeBar("手机号不合法")
            view.startErrorAnimation(btVerify)
            return
        }

        if (TextUtils.isEmpty(seVerifyCode!!.text)) {
            view.showSnakeBar("验证码没有填写")
            view.startErrorAnimation(btVerify)
            return
        }
        view.showWaitDialog()
        threadPool.execute {
            val result = model.verifyCodeLogin(
                context,
                stamp,
                etPhone.text.toString().trim(),
                seVerifyCode.text.toString().trim()
            )
            if (result!!.result) {
                val userBean =
                    Gson().fromJson(Gson().toJson(result.resultObject), UserBean::class.java)
                val loginConfig = context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
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
                edit.apply()
                view.loginSuccess()
                view.hideWaitDialog()
            } else {
                view.hideWaitDialog()
                view.showSnackBar("出错了，请检查后重试")
            }
        }

    }

    fun resetPasswordRequestSMSCode(context: Context, phone: EditText) {
        if (TextUtils.isEmpty(phone.text)) {
            view.startErrorAnimation(phone)
            view.showSnakeBar("手机号不能为空")
            return
        }

        if (!StringUtil.isPhoneNumber(phone.text.toString().trim())) {
            view.startErrorAnimation(phone)
            view.showSnakeBar("手机号不合法")
            return
        }

        view.showWaitDialog()
        threadPool.execute {
            try {
                val checkResult =
                    model.checkPhoneIsRegistered(context, phone.text.toString().trim())
                if (!checkResult) {
                    view.hideWaitDialog()
                    view.startErrorAnimation(phone)
                    view.showSnakeBar("该手机号没有注册过")
                } else {
                    view.initYoTestCaptchaVerify()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                view.hideWaitDialog()
                view.startErrorAnimation(phone)
                view.showSnakeBar("获取验证码出错，请稍候重试")
            }
        }
    }

    fun resetPasswordCheckVerifyCode(
        context: Context,
        etPhone: EditText?,
        seVerifyCode: SeparatedEditText?,
        btVerify: Button,
        stamp: String
    ) {
        if (TextUtils.isEmpty(etPhone!!.text) || !StringUtil.isPhoneNumber(
                etPhone.text.toString().trim()
            )
        ) {
            view.showSnakeBar("手机号不合法")
            view.startErrorAnimation(btVerify)
            return
        }

        if (TextUtils.isEmpty(seVerifyCode!!.text)) {
            view.showSnakeBar("验证码没有填写")
            view.startErrorAnimation(btVerify)
            return
        }
        view.showWaitDialog()
        threadPool.execute {
            val result = model.verifyCode(
                stamp,
                etPhone.text.toString().trim(),
                seVerifyCode.text.toString().trim()
            )
            if (result) {
                view.hideWaitDialog()
                view.setViewPagerCurrentPage(2)
            } else {
                view.hideWaitDialog()
                view.showSnackBar("微博绑定出错，请检查后重试")
            }
        }
    }

    fun resetPassword(
        context: Context,
        etPhone: EditText?,
        etPassword: EditText?,
        btResetPassword: Button?
    ) {
        if (TextUtils.isEmpty(etPassword!!.text) || etPassword.text.length < 6
        ) {
            view.showSnakeBar("新密码不合法")
            view.startErrorAnimation(btResetPassword!!)
            return
        }
        view.showWaitDialog()
        threadPool.execute {
            val result = model.resetPassword(
                context,
                etPhone!!.text.toString().trim(),
                etPassword.text.toString().trim()
            )
            if (result) {
                view.hideWaitDialog()
                view.resetPasswordSuccess()
            } else {
                view.hideWaitDialog()
                view.showSnakeBar("密码重置失败，请稍后重试")
                view.setViewPagerCurrentPage(0)
            }
        }
    }

    // 设置监听事件
    private val yoTestListener = object : YoTestListener() {

        override fun onSuccess(token: String, verified: Boolean) {
            val phone = view.getPhone()
            view.showWaitDialog()
            threadPool.execute {
                if (verified) {
                    var result: NewResult? = if (pageType == 3) {
                        model.resetPasswordRequestSMSCode(phone, token)
                    } else {
                        model.requestSMSCode(phone, token)
                    }

                    if (result!!.result) {
                        view.setCodeStamp(result.resultObject as String)
                        view.requestVerifyCodeSuccess()
                        view.hideWaitDialog()
                    } else {
                        view.hideWaitDialog()
                        view.showSnackBar(result.exceptionMessage!!)
                    }
                } else {
                    view.hideWaitDialog()
                    view.showSnackBar("获取验证码出错")
                }
            }
        }

        override fun onError(code: Int, message: String) {
            yoTestCaptchaVerify!!.destroy()
        }

        override fun onClose(data: String?) {
            yoTestCaptchaVerify!!.destroy()
        }
    }
}
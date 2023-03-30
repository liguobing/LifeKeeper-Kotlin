package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.text.TextUtils
import android.widget.EditText
import com.fastyotest.library.YoTestCaptchaVerify
import com.fastyotest.library.YoTestListener
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.model.RegisterModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.activity.IRegisterView
import com.lixyz.lifekeeperforkotlin.view.activity.RegisterActivity
import com.lixyz.lifekeeperforkotlin.view.customview.SeparatedEditText
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class RegisterPresenter(private var view: IRegisterView) {
    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("注册线程池"))

    private var yoTestCaptchaVerify: YoTestCaptchaVerify? = null

    // 设置监听事件
    private val yoTestListener = object : YoTestListener() {
        override fun onReady(data: String?) {
        }

        override fun onSuccess(token: String, verified: Boolean) {
            val phone = view.getPhone()
            view.showWaitDialog()
            threadPool.execute {
                if (verified) {
                    val result = model.requestSMSCode(phone, token)
                    if (result.result) {
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


    private var model = RegisterModel()

    fun requestSMSCode(
        activity: RegisterActivity,
        view: IRegisterView,
        etUserName: EditText?,
        etPhone: EditText?
    ) {
        if (TextUtils.isEmpty(etPhone!!.text) || !StringUtil.isPhoneNumber(
                etPhone.text.toString().trim()
            )
        ) {
            view.showSnackBar("手机号不合法")
            return
        }

        if (TextUtils.isEmpty(etUserName!!.text)) {
            view.showSnackBar("用户名不能为空")
            return
        }
        yoTestCaptchaVerify = YoTestCaptchaVerify(activity, yoTestListener)
        yoTestCaptchaVerify!!.verify()
    }

    fun checkVerifyCode(
        etPhone: EditText,
        seVerifyCode: SeparatedEditText,
        stamp: String
    ) {
        if (TextUtils.isEmpty(seVerifyCode.text)) {
            view.showSnackBar("验证码没有填写")
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
                view.checkVerifyCodeSuccess()
                view.hideWaitDialog()
            } else {
                view.hideWaitDialog()
                view.showSnackBar("验证码错误，请检查后重试")
            }
        }
    }

    fun register(
        context: Context,
        etUserName: EditText?,
        etPhone: EditText?,
        etPassword: EditText?
    ) {
        if (TextUtils.isEmpty(etPassword!!.text)) {
            view.showSnackBar("密码不能为空")
            return
        }

        if (etPassword.text.toString().trim().length < 6) {
            view.showSnackBar("密码太短了")
            return
        }

        view.showWaitDialog()
        threadPool.execute {
            try {
                val result = model.saveUserToCloud(context, etUserName, etPhone, etPassword)
                if (result) {
                    view.hideWaitDialog()
                    view.registerSuccess()
                } else {
                    view.hideWaitDialog()
                    view.showSnackBar("注册出错，请稍候重试。")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
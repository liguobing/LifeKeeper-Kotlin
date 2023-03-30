package com.lixyz.lifekeeperforkotlin.view.activity

import android.view.View
import java.security.MessageDigest

interface IVerifyCodeView {
    fun startErrorAnimation(view: View)
    fun showSnakeBar(msg: String)
    fun showWaitDialog()
    fun hideWaitDialog()
    fun requestVerifyCodeSuccess()
    fun requestVerifyCodeFailured()
    fun loginSuccess()
    fun setViewPagerCurrentPage(index: Int)
    fun resetPasswordSuccess()
    fun getPhone():String
    fun setCodeStamp(stamp:String)
    fun showSnackBar(message:String)
    fun initYoTestCaptchaVerify()

}
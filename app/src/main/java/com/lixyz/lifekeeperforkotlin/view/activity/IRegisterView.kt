package com.lixyz.lifekeeperforkotlin.view.activity

interface IRegisterView {
    fun showWaitDialog()
    fun hideWaitDialog()
    fun showSnackBar(msg:String)
    fun setViewPagerCurrentItem(index:Int)
    fun requestVerifyCodeSuccess()
    fun requestVerifyCodeFailured()
    fun checkVerifyCodeSuccess()
    fun registerSuccess()
    fun setCodeStamp(stamp:String)
    fun getPhone():String
}
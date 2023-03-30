package com.lixyz.lifekeeperforkotlin.view.activity

import android.view.View
import com.lixyz.lifekeeperforkotlin.bean.UserBean

/**
 * @author LGB
 * 登录 View 接口
 */
interface ILoginView {
    /**
     * 显示等待 Dialog
     */
    fun showWaitDialog()

    /**
     * 更新等待 Dialog
     *
     * @param msg 更新内容
     */
    fun updateWaitDialog(msg: String?)

    /**
     * 隐藏等待 Dialog
     */
    fun hideWaitDialog()

    /**
     * 显示 SnakeBar
     *
     * @param msg 显示内容
     */
    fun showSnakeBar(msg: String?)

    /**
     * 更新 ViewPage 页面
     *
     * @param index 页面下标
     */
    fun updateViewPageCurrentPage(index: Int)

    fun closeResetPasswordDialog()

    /**
     * 开始错误动画
     *
     * @param view 显示动画的 View
     */
    fun startErrorAnimation(view: View?)

    /**
     * 获取验证码按钮倒计时
     */
    fun timeCountDown(type: Int)

    /**
     * 登录成功
     */
    fun loginSuccess()

    /**
     * 隐藏软键盘
     *
     * @param view view
     */
    fun hideSoftInput(view: View?)

    /**
     * 绑定的手机号已经注册过
     */
    fun bindPhoneIsRegistered()

    /**
     * 显示绑定手机的 Dialog
     */
    fun showBindPhoneDialog(user: UserBean)

    /**
     * 关闭绑定手机的 Dialog
     */
    fun closeBindPhoneDialog()

    /**
     * QQ登录时，绑定手机
     */
    fun qqLoginBindPhone(user: UserBean)

    /**
     * 微博登录时，绑定手机
     */
    fun weiboLoginBindPhone(user: UserBean)
}
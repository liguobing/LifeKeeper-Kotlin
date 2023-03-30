package com.lixyz.lifekeeperforkotlin.view.activity

import android.view.View
import com.lixyz.lifekeeperforkotlin.bean.UserBean

/**
 * @author LGB
 * UserCenter View 接口
 */
interface IUserCenterView {
    /**
     * 显示等待 Dialog
     */
    fun showWaitDialog()

    /**
     * 隐藏等待 Dialog
     */
    fun hideWaitDialog()

    /**
     * 显示 SnakeBar
     *
     * @param message SnakeBar 展示信息
     */
    fun showSnakeBar(message: String?)

    /**
     * 显示 Toast
     */
    fun showToast(message: String?)

    /**
     * 更新微博绑定状态
     *
     * @param isBind 是否绑定
     */
    fun updateWeiboBindStatus(isBind: Boolean)

    /**
     * 更新QQ绑定状态
     *
     * @param isBind 是否绑定
     */
    fun updateQqBindStatus(isBind: Boolean)

    /**
     * 更新用户名
     *
     * @param username 用户名
     */
    fun updateUsername(username: String?)

    /**
     * 更新手机号
     */
    fun updatePhone(phone: String?)

    /**
     * 更新用户头像
     *
     * @param userIconUrl 用户头像 Uri
     */
    fun updateUserIcon(userIconUrl: String?)

    /**
     * 更新登录状态
     *
     * @param isLogin 是否登录
     */
    fun updateLoginStatus(isLogin: Boolean)

    fun updateUserInfo(user: UserBean)

    /**
     * 开始错误动画
     */
    fun startErrorAnimation(view: View?)

    /**
     * 绑定微博
     */
    fun bindWeibo()

    /**
     * 解绑微博
     */
    fun unBindWeibo()

    /**
     * 绑定 QQ
     */
    fun bindQQ()

    /**
     * 解绑 QQ
     */
    fun unBindQQ()

    /**
     * 绑定手机号按钮倒计时
     */
    fun timeCountDown()

    /**
     * 更新绑定手机 Dialog 提示
     */
    fun updateWarning(message: String)

    /**
     * 隐藏绑定手机 Dialog
     */
    fun dismissBindPhoneDialog()

    /**
     * 显示绑定手机号 Dialog 提示
     */
    fun showBindPhoneDialogWarning(message: String)

    /**
     * 显示绑定手机号 Dialog 提示
     */
    fun hideBindPhoneDialogWarning()

    /**
     * 退出
     */
    fun logout()

    fun hideBindPhoneDialog()
}
package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.UserBean

/**
 * IndexActivity View 接口
 *
 * @author LGB
 */
interface IIndexView {


    /**
     * 显示 SnackBar
     *
     * @param message SnackBar 显示的信息
     */
    fun showSnackBar(message: String?)


    /**
     * 更新登录状态
     */
    fun updateLogin(isLogin: Boolean, userBean: UserBean?)

    /**
     * 更新背景天气
     */
    fun updateWeather(weatherCode: Int)

    /**
     * 重置计划数据
     */
    fun resetPlanContent(planCountOfDay: Int, planCountOfMonth: Int)

    /**
     * 重置账本数据
     */
    fun resetAccountContent(income: Double, expend: Double)

    /**
     * 重置网盘数据
     */
    fun resetNetDiskContent(fileCount: Int)
}
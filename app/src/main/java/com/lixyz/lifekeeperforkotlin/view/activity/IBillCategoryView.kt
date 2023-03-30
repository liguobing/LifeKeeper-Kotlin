package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory


/**
 * BillCategoryActivity View
 *
 * @author LGB
 */
interface IBillCategoryView {
    /**
     * 显示SnackBar
     *
     * @param message 显示内容
     */
    fun showSnackBar(message: String?)

    /**
     * 显示等待 Dialog
     */
    fun showWaitDialog()

    /**
     * 隐藏等待 Dialog
     */
    fun hideWaitDialog()

    /**
     * 更新等待 Dialog
     *
     * @param message 更新内容
     */
    fun updateWaitDialog(message: String?)

    /**
     * 更新收入分类列表
     *
     * @param list 数据列表
     */
    fun updateIncomeList(list: ArrayList<BillCategory>?)

    /**
     * 更新支出分类列表
     *
     * @param list 数据列表
     */
    fun updateExpendList(list: ArrayList<BillCategory>?)
}

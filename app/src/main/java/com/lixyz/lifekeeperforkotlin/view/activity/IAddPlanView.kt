package com.lixyz.lifekeeperforkotlin.view.activity

/**
 * AddPlan View
 *
 * @author LGB
 */
interface IAddPlanView {
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
     * 保存成功
     */
    fun saveSuccessful()
}
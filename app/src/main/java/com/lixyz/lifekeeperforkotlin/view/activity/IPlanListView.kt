package com.lixyz.lifekeeperforkotlin.view.activity

import com.lixyz.lifekeeperforkotlin.bean.plan.PlanBean
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanListDateMenuItemBean


interface IPlanListView {
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
     * 显示 SnakeBar
     *
     * @param message 显示内容
     */
    fun showSnakeBar(message: String?)

    /**
     * 更新计划列表
     *
     */
    fun updatePlanList(list:List<List<PlanBean>>?)

    /**
     * 更新日期
     *
     * @param date 日期
     */
    fun updatePlanDate(date: String?)

    /**
     * 更新日期 RecyclerView
     *
     * @param list 日期 List
     */
    fun updatePlanDateMenu(list: ArrayList<PlanListDateMenuItemBean>?)

    /**
     * 滚动日期列表
     *
     * @param index 滚动的坐标
     */
    fun scrollDateList(index: Int)
}
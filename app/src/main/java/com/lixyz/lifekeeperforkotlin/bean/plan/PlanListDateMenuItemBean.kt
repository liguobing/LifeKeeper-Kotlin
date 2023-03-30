package com.lixyz.lifekeeperforkotlin.bean.plan

import java.io.Serializable

/**
 * 计划列表，日期菜单 RecyclerView Item 原型
 *
 * @author LGB
 */
class PlanListDateMenuItemBean: Serializable {
    /**
     * 日
     */
    var day = 0

    /**
     * 星期
     */
    var week: String? = null

    /**
     * 是否选中
     */
    var isClick = false
}
package com.lixyz.lifekeeperforkotlin.bean.billchart

import java.io.Serializable

/**
 * 账单图表 Activity，Toolbar 日期菜单中 ListView Item 原型
 *
 * @author LGB
 */
class BillChartItemBean(
    /**
     * item 名称
     */
    val itemName: Int,
    /**
     * 是否点击（用于标识年份）
     */
    var isClick: Boolean
) : Serializable {

    init {
        isClick = isClick
    }
}
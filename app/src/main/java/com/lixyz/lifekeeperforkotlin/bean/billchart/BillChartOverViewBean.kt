package com.lixyz.lifekeeperforkotlin.bean.billchart

import java.io.Serializable

/**
 * 账单图表界面，收支概览原型
 *
 * @author LGB
 */
class BillChartOverViewBean: Serializable {
    /**
     * 收入
     */
    var income = 0.0

    /**
     * 结余
     */
    var balance = 0.0

    /**
     * 支出
     */
    var expend = 0.0

    /**
     * 日平均
     */
    var evaluate = 0.0

    constructor() {}
    constructor(income: Double, balance: Double, expend: Double, evaluate: Double) {
        this.income = income
        this.balance = balance
        this.expend = expend
        this.evaluate = evaluate
    }
}
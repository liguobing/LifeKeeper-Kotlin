package com.lixyz.lifekeeperforkotlin.bean.bill

import java.io.Serializable


/**
 * 账单原型
 *
 * @author LGB
 */
class BillBean : Serializable {


    /**
     * ObjectId
     */
    var objectId: String? = null

    /**
     * 账单 ID
     */
    var billId: String? = null

    /**
     * 账单日期
     */
    var billDate: Long = 0

    /**
     * 账单金额
     */
    var billMoney = 0.0

    /**
     * 账单属性：
     * 1 ： 收入；-1 ： 支出
     */
    var billProperty = 0

    /**
     * 账单分类
     */
    var billCategory: String? = null

    /**
     * 账单账户
     */
    var billAccount: String? = null

    /**
     * 账单备注
     */
    var billRemark: String? = null

    /**
     * 账单用过户
     */
    var billUser: String? = null

    /**
     * 账单商家
     */
    var billShop: String? = null

    /**
     * 账单状态
     * 1:正常账单   -1：非正常账单
     */
    var billStatus = 0

    /**
     * 账单类型
     * 0：正常  1：已删除  2：已修改
     */
    var billType = 0

    /**
     * 账单图片
     */
    var billImage: ArrayList<String>? = null

    /**
     * 创建日期
     */
    var createTime: Long = 0

    /**
     * 修改日期
     */
    var updateTime: Long = 0

    override fun equals(obj: Any?): Boolean {
        return if (obj == null) {
            false
        } else {
            if (obj is BillBean) {
                val bean = obj
                if (bean.objectId == null) {
                    false
                } else bean.objectId == objectId && bean.billStatus == billStatus
            } else {
                false
            }
        }
    }
}
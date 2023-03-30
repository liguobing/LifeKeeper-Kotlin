package com.lixyz.lifekeeperforkotlin.bean.billaccount

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


/**
 * 账单账户原型
 *
 * @author LGB
 */
@SuppressLint("ParcelCreator")
@Parcelize
class BillAccount() : Parcelable {
    /**
     * ObjectId
     */
    @IgnoredOnParcel
    var objectId: String? = null

    /**
     * 账户ID
     */
    @IgnoredOnParcel
    var accountId: String? = null

    /**
     * 账户用户
     */
    @IgnoredOnParcel
    var accountUser: String? = null

    /**
     * 账户名称
     */
    @IgnoredOnParcel
    var accountName: String? = null

    /**
     * 账户状态
     * 1:正常账户   -1：非正常账户
     */
    @IgnoredOnParcel
    var accountStatus = 0

    /**
     * 账户类别
     * 0：正常  1：已删除  2：已修改
     */
    @IgnoredOnParcel
    var accountType = 0

    /**
     * 创建时间
     */
    @IgnoredOnParcel
    var createTime: Long = 0

    /**
     * 更新时间
     */
    @IgnoredOnParcel
    var updateTime: Long = 0

    /**
     * 排序下标
     */
    @IgnoredOnParcel
    var orderIndex = 0

    fun billAccount() {}

    constructor(objectId: String?, orderIndex: Int) : this() {
        this.objectId = objectId
        this.orderIndex = orderIndex
    }

    constructor(
        objectId: String?,
        accountId: String?,
        accountUser: String?,
        accountName: String?,
        accountStatus: Int,
        accountType: Int,
        createTime: Long,
        updateTime: Long,
        orderIndex: Int
    ) : this() {
        this.objectId = objectId
        this.accountId = accountId
        this.accountUser = accountUser
        this.accountName = accountName
        this.accountStatus = accountStatus
        this.accountType = accountType
        this.createTime = createTime
        this.updateTime = updateTime
        this.orderIndex = orderIndex
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null) {
            false
        } else {
            if (other is BillAccount) {
                val bean: BillAccount = other
                if (bean.objectId == null) {
                    false
                } else bean.objectId
                    .equals(objectId) && bean.accountStatus == accountStatus
            } else {
                false
            }
        }
    }
}
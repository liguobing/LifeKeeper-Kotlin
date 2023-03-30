package com.lixyz.lifekeeperforkotlin.bean.billcategory

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


/**
 * 账单分类原型
 *
 * @author LGB
 */
@SuppressLint("ParcelCreator")
@Parcelize
class BillCategory() : Parcelable {
    /**
     * ObjectId 唯一标识
     */
    @IgnoredOnParcel
    var objectId: String? = null

    /**
     * 分类 ID
     */
    @IgnoredOnParcel
    var categoryId: String? = null

    /**
     * 分类用户
     */
    @IgnoredOnParcel
    var categoryUser: String? = null

    /**
     * 分类名称
     */
    @IgnoredOnParcel
    var categoryName: String? = null

    /**
     * 收入/支出
     */
    @IgnoredOnParcel
    var isIncome = 0

    /**
     * 分类状态
     * 1:正常账单   -1：非正常账单
     */
    @IgnoredOnParcel
    var categoryStatus = 0

    /**
     * 分类类别
     * 0：正常  1：已删除  2：已修改
     */
    @IgnoredOnParcel
    var categoryType = 0

    /**
     * 创建时间
     */
    @IgnoredOnParcel
    var createTime: Long = 0

    /**
     * 修改时间
     */
    @IgnoredOnParcel
    var updateTime: Long = 0

    /**
     * 排序下标
     */
    @IgnoredOnParcel
    var orderIndex = 0

    override fun equals(other: Any?): Boolean {
        return if (other == null) {
            false
        } else {
            if (other is BillCategory) {
                val bean: BillCategory = other
                if (bean.objectId == null) {
                    false
                } else bean.objectId == objectId && bean.categoryStatus == categoryStatus
            } else {
                false
            }
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }


    constructor(objectId: String?, orderIndex: Int) : this() {
        this.objectId = objectId
        this.orderIndex = orderIndex
    }

    constructor(
        objectId: String?,
        categoryId: String?,
        categoryUser: String?,
        categoryName: String?,
        isIncome: Int,
        categoryStatus: Int,
        categoryType: Int,
        createTime: Long,
        updateTime: Long,
        orderIndex: Int
    ) : this() {
        this.objectId = objectId
        this.categoryId = categoryId
        this.categoryUser = categoryUser
        this.categoryName = categoryName
        this.isIncome = isIncome
        this.categoryStatus = categoryStatus
        this.categoryType = categoryType
        this.createTime = createTime
        this.updateTime = updateTime
        this.orderIndex = orderIndex
    }
}

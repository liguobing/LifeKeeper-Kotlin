package com.lixyz.lifekeeperforkotlin.bean.billshop

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * 账单商家原型
 *
 * @author LGB
 */
@SuppressLint("ParcelCreator")
@Parcelize
class BillShop : Parcelable {
    /**
     * ObjectId
     */
    @IgnoredOnParcel
    var objectId: String? = null

    /**
     * 商家 ID
     */
    @IgnoredOnParcel
    var shopId: String? = null

    /**
     * 商家名称
     */
    @IgnoredOnParcel
    var shopName: String? = null

    /**
     * 商家图标
     */
    @IgnoredOnParcel
    var shopIcon: String? = null

    /**
     * 商家用户
     */
    @IgnoredOnParcel
    var shopUser: String? = null

    /**
     * 商家状态
     * 1:正常商家   -1：非正常商家
     */
    @IgnoredOnParcel
    var shopStatus = 0

    /**
     * 商家类型
     * 0：正常  1：已删除  2：已修改
     */
    @IgnoredOnParcel
    var shopType = 0

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

    override fun equals(other: Any?): Boolean {
        return if (other == null) {
            false
        } else {
            if (other is BillShop) {
                val bean: BillShop = other
                if (bean.objectId == null) {
                    false
                } else bean.objectId == objectId && bean.shopStatus == shopStatus
            } else {
                false
            }
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
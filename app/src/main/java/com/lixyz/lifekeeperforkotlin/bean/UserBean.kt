package com.lixyz.lifekeeperforkotlin.bean

import java.io.Serializable

/**
 * 用户对象原型
 *
 * @author LGB
 */
class UserBean: Serializable {


    /**
     * 用户 ObjectId （唯一标识）
     */
    var objectId: String? = null

    /**
     * 用户 ID
     */
    var userId: String? = null

    /**
     * 用户电话
     */
    var userPhone: String? = null

    /**
     * 用户名
     */
    var userName: String? = null

    /**
     * 用户绑定微博
     */
    var userBindWeibo: String? = null

    /**
     * 用户绑定 QQ Token 过期时间
     */
    var userBindQQExpiresTime: String? = null

    /**
     * 用户绑定 QQ Token
     */
    var userBindQQAccessToken: String? = null

    /**
     * 用户绑定 QQ 头像
     */
    var userBindQQIcon: String? = null

    /**
     * 用户绑定微博 Token
     */
    var userBindWeiboAccessToken: String? = null

    /**
     * 用户密码
     */
    var userPassword: String? = null

    /**
     * 用户绑定微博头像
     */
    var userBindWeiboIcon: String? = null

    /**
     * 用户绑定微博 Token 过期时间
     */
    var userBindWeiboExpiresTime: String? = null

    /**
     * 用户绑定微博 ID
     */
    var userBindWeiboId: String? = null

    /**
     * 用户绑定 QQ
     */
    var userBindQQ: String? = null

    /**
     * 用户头像链接
     */
    var userIconUrl: String? = null

    /**
     * 用户绑定 QQ ID
     */
    var userBindQQOpenId: String? = null

    /**
     * 用户状态：
     * 1：正常用户
     * -1：非正常用户
     */
    var userStatus = 0

    /**
     * 用户类型
     * 0：正常用过户
     * 1:已删除
     * 2：已修改
     */
    var userType = 0

    /**
     * 创建时间
     */
    var createTime: Long = 0

    /**
     * 更新时间
     */
    var updateTime: Long = 0


    constructor() {}
    constructor(userName: String?, userIconUrl: String?) {
        this.userName = userName
        this.userIconUrl = userIconUrl
    }

    constructor(
        objectId: String?,
        userId: String?,
        userPhone: String?,
        userName: String?,
        userBindWeibo: String?,
        userBindQQExpiresTime: String?,
        userBindQQAccessToken: String?,
        userBindQQIcon: String?,
        userBindWeiboAccessToken: String?,
        userPassword: String?,
        userBindWeiboIcon: String?,
        userBindWeiboExpiresTime: String?,
        userBindWeiboId: String?,
        userBindQQ: String?,
        userIconUrl: String?,
        userBindQQOpenId: String?,
        userStatus: Int,
        userType: Int,
        createTime: Long,
        updateTime: Long
    ) {
        this.objectId = objectId
        this.userId = userId
        this.userPhone = userPhone
        this.userName = userName
        this.userBindWeibo = userBindWeibo
        this.userBindQQExpiresTime = userBindQQExpiresTime
        this.userBindQQAccessToken = userBindQQAccessToken
        this.userBindQQIcon = userBindQQIcon
        this.userBindWeiboAccessToken = userBindWeiboAccessToken
        this.userPassword = userPassword
        this.userBindWeiboIcon = userBindWeiboIcon
        this.userBindWeiboExpiresTime = userBindWeiboExpiresTime
        this.userBindWeiboId = userBindWeiboId
        this.userBindQQ = userBindQQ
        this.userIconUrl = userIconUrl
        this.userBindQQOpenId = userBindQQOpenId
        this.userStatus = userStatus
        this.userType = userType
        this.createTime = createTime
        this.updateTime = updateTime
    }

    override fun toString(): String {
        return "UserBean(objectId=$objectId, userId=$userId, userPhone=$userPhone, userName=$userName, userBindWeibo=$userBindWeibo, userBindQQExpiresTime=$userBindQQExpiresTime, userBindQQAccessToken=$userBindQQAccessToken, userBindQQIcon=$userBindQQIcon, userBindWeiboAccessToken=$userBindWeiboAccessToken, userPassword=$userPassword, userBindWeiboIcon=$userBindWeiboIcon, userBindWeiboExpiresTime=$userBindWeiboExpiresTime, userBindWeiboId=$userBindWeiboId, userBindQQ=$userBindQQ, userIconUrl=$userIconUrl, userBindQQOpenId=$userBindQQOpenId, userStatus=$userStatus, userType=$userType, createTime=$createTime, updateTime=$updateTime)"
    }


}
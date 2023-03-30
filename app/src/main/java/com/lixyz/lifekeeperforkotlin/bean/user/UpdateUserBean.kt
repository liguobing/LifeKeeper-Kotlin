package com.lixyz.lifekeeperforkotlin.bean.user

import com.lixyz.lifekeeperforkotlin.bean.UserBean
import java.io.Serializable

/**
 * 更新用户对象原型
 *
 * @author LGB
 */
class UpdateUserBean : Serializable {
    /**
     * 需要更新的用户的 ObjectId
     */
    var oldUserObjectId: String? = null

    /**
     * 更新时间
     */
    var oldUserUpdateTime: Long = 0

    /**
     * 更新后的用户对象
     */
    var newUser: UserBean? = null
}

package com.lixyz.lifekeeperforkotlin.bean.user

import com.lixyz.lifekeeperforkotlin.bean.UserBean
import java.io.Serializable


class UserResponseBean : Serializable {
    var responseCode = 0
    var responseList: ArrayList<UserBean>? = null

}

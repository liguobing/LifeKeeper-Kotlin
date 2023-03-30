package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.database.sqlite.SQLiteException
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.model.LoginModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.activity.ILoginView
import java.io.IOException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


/**
 * @author LGB
 * Login Presenter
 */
class LoginPresenter(
    view: ILoginView
) {

    /**
     * View
     */
    private val mView: ILoginView = view

    /**
     * model
     */
    private val model: LoginModel = LoginModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("注册/登录线程池"))

    /**
     * 手机号/密码登录
     */
    fun loginForPhoneAndPassword(
        context: Context,
        phone: EditText,
        password: EditText,
        btLogin: Button
    ) {
        if (TextUtils.isEmpty(phone.text) || !StringUtil.isPhoneNumber(
                phone.text.toString().trim()
            )
        ) {
            mView.showSnakeBar("手机号不合法")
            mView.startErrorAnimation(phone)
            return
        }

        if (TextUtils.isEmpty(password.text)) {
            mView.showSnakeBar("密码不能为空")
            mView.startErrorAnimation(password)
            return
        }

        mView.showWaitDialog()
        threadPool.execute {
            try {
                val overview =
                    model.selectUserByPassword(
                        context,
                        phone.text.toString().trim(),
                        password.text.toString().trim()
                    )
                if (overview != null) {
                    val saveSuccess = model.saveUserToLocal(context, overview)
                    if (saveSuccess) {
                        mView.loginSuccess()
                    } else {
                        mView.hideWaitDialog()
                        mView.startErrorAnimation(btLogin)
                        mView.showSnakeBar("登录失败，请稍后重试")
                    }
                } else {
                    mView.hideWaitDialog()
                    mView.startErrorAnimation(btLogin)
                    mView.showSnakeBar("帐号/密码不匹配")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                mView.hideWaitDialog()
                mView.startErrorAnimation(btLogin)
                mView.showSnakeBar("登录失败，请稍后重试")
            }
        }
    }


    /**
     * QQ登录
     */
    fun loginForQQ(context: Context, userBean: UserBean, imgQQ: ImageView) {
        threadPool.execute {
            try {
                //先判断有没有账号绑定过这个QQ
                val result = model.selectUserByQQId(context, userBean.userBindQQOpenId!!)
                //如果绑定过，直接将数据保存到本地
                if (result != null) {
                    val saveResult = model.saveUserToLocal(context, result)
                    //用户信息保存到本地成功，尝试保存数据
                    if (saveResult) {
                        mView.loginSuccess()
                    } else {
                        mView.hideWaitDialog()
                        mView.showSnakeBar("QQ 登录失败，请稍后重试")
                        mView.startErrorAnimation(imgQQ)
                    }
                } else {
                    userBean.objectId = StringUtil.getRandomString()
                    userBean.userId = StringUtil.getRandomString()
                    userBean.userName = userBean.userBindQQ
                    userBean.userIconUrl = userBean.userBindQQIcon
                    userBean.userStatus = 1
                    userBean.userType = 0
                    userBean.createTime = System.currentTimeMillis()
                    userBean.updateTime = 0
                    mView.qqLoginBindPhone(userBean)
                }
            } catch (e: SQLiteException) {
                e.printStackTrace()
                mView.hideWaitDialog()
                mView.showSnakeBar("QQ 登录失败，请稍后重试")
                mView.startErrorAnimation(imgQQ)
            } catch (e: IOException) {
                e.printStackTrace()
                mView.hideWaitDialog()
                mView.showSnakeBar("QQ 登录失败，请稍后重试")
                mView.startErrorAnimation(imgQQ)
            }
        }
    }

    fun loginForWeibo(context: Context, userBean: UserBean, imgWeibo: ImageView) {
        try {
            //先判断有没有账号绑定过这个微博
            val override = model.selectUserByWeiboId(context, userBean.userBindWeiboId!!)
            //如果绑定过，直接将数据保存到本地
            if (override != null) {
                val saveResult = model.saveUserToLocal(context, override)
                //用户信息保存到本地成功，尝试保存数据
                if (saveResult) {
                    mView.loginSuccess()
                } else {
                    mView.hideWaitDialog()
                    mView.showSnakeBar("2微博登录失败，请稍后重试")
                    mView.startErrorAnimation(imgWeibo)
                }
            } else {
                userBean.objectId = StringUtil.getRandomString()
                userBean.userId = StringUtil.getRandomString()
                userBean.userName = userBean.userBindWeibo
                userBean.userIconUrl = userBean.userBindWeiboIcon
                userBean.userStatus = 1
                userBean.userType = 0
                userBean.createTime = System.currentTimeMillis()
                userBean.updateTime = 0
                mView.weiboLoginBindPhone(userBean)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            mView.hideWaitDialog()
            mView.showSnakeBar("4微博登录失败，请稍后重试")
            mView.startErrorAnimation(imgWeibo)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            mView.hideWaitDialog()
            mView.showSnakeBar("5微博登录失败，请稍后重试")
            mView.startErrorAnimation(imgWeibo)
        }
    }

    fun shutdownThreadPool() {
        if (!threadPool.isShutdown) {
            threadPool.shutdown()
        }
    }

}

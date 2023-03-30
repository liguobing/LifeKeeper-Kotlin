package com.lixyz.lifekeeperforkotlin.presenter

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.ActivityInfo
import android.database.sqlite.SQLiteException
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.model.UserCenterModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.activity.GifSizeFilter
import com.lixyz.lifekeeperforkotlin.view.activity.Glide4Engine
import com.lixyz.lifekeeperforkotlin.view.activity.IUserCenterView
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import java.io.IOException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


/**
 * @author LGB
 * 个人中心 Presenter
 */
class UserCenterPresenter(
    context: Context,
    view: IUserCenterView
) {

    private val mView: IUserCenterView = view

    /**
     * 模型
     */
    private val model: UserCenterModel = UserCenterModel(context)


    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("个人中心线程池"))

    /**
     * View 进入 onResume 方法，更新信息
     */
    fun viewOnResume(context: Context) {
        mView.updateUserInfo(model.getUserInfo(context))
    }

    /**
     * 选择用户头像
     */
    fun selectUserIcon(activity: Activity?, context: Context) {
        Matisse.from(activity)
            .choose(MimeType.ofImage(), false)
            .countable(true)
            .capture(true)
            .captureStrategy(
                CaptureStrategy(true, "com.lixyz.lifekeeperforkotlin.fileprovider", "test")
            )
            .maxSelectable(1)
            .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
            .gridExpectedSize(
                context.resources.getDimensionPixelSize(R.dimen.grid_expected_size)
            )
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .thumbnailScale(0.85f)
            .imageEngine(Glide4Engine())
            .originalEnable(true)
            .maxOriginalSize(10)
            .autoHideToolbarOnSingleTap(true)
            .forResult(REQUEST_CODE_CHOOSE)
    }


    fun logout(context: Context) {
        mView.showWaitDialog()
        threadPool.execute {
            try {
                val logoutResult = model.logout(context)
                if (logoutResult) {
                    mView.hideWaitDialog()
                    mView.logout()
                } else {
                    mView.hideWaitDialog()
                    mView.showSnakeBar("出错啦")
                }
            } catch (e: SQLiteException) {
                e.printStackTrace()
                mView.hideWaitDialog()
                mView.showSnakeBar("出错啦")
            }
        }
    }


    companion object {
        /**
         * 选择图片 request code
         */
        private const val REQUEST_CODE_CHOOSE = 90000
    }


    /**
     * 检查微博绑定情况
     * 如果没有绑定微博，则绑定微博
     * 如果微博已经绑定，并且已经绑定了手机号或者QQ号，则可以取消绑定微博
     * 如果只绑定了微博，则提示无法取消绑定
     */
    fun checkWeiboBind(context: Context) {
        val sharedPreferences = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val phone = sharedPreferences.getString("UserPhone", null)
        val qq = sharedPreferences.getString("UserBindQQ", null)
        val weibo = sharedPreferences.getString("UserBindWeibo", null)

        if (weibo == null) {
            mView.bindWeibo()
        } else {
            if (phone != null || qq != null) {
                mView.unBindWeibo()
            } else {
                mView.showSnakeBar("您只绑定了微博，取消绑定会导致无法登录")
            }
        }
    }


    /**
     * 解除绑定微博
     */
    fun unBindWeibo(context: Context, imgWeibo: ImageView) {
        threadPool.execute {
            val result: Boolean = model.unBindWeibo(context)
            if (result) {
                mView.hideWaitDialog()
                mView.updateWeiboBindStatus(false)
                mView.showSnakeBar("微博解除绑定成功")
            } else {
                mView.hideWaitDialog()
                mView.startErrorAnimation(imgWeibo)
                mView.showSnakeBar("微博解除绑定失败")
            }
        }
    }

    /**
     * 检查QQ绑定情况
     * 如果没有绑定QQ，则绑定QQ
     * 如果QQ已经绑定，并且已经绑定了手机号或者微博，则可以取消绑定QQ
     * 如果只绑定了QQ，则提示无法取消绑定
     */
    fun checkQQBind(context: Context) {
        val sharedPreferences = context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
        val phone = sharedPreferences.getString("UserPhone", null)
        val qq = sharedPreferences.getString("UserBindQQ", null)
        val weibo = sharedPreferences.getString("UserBindWeibo", null)

        if (qq == null) {
            mView.bindQQ()
        } else {
            if (phone != null || weibo != null) {
                mView.unBindQQ()
            } else {
                mView.showSnakeBar("您只绑定了 QQ，取消绑定会导致无法登录")
            }
        }
    }

    fun bindQQ(
        context: Context,
        userBindQQOpenId: String,
        userBindQQAccessToken: String,
        userBindQQExpiresTime: String,
        userBindQQ: String,
        userBindQQIcon: String,
        imgQQ: ImageView
    ) {
        threadPool.execute {
            val isBind: Boolean = model.checkQQIsBind(userBindQQOpenId)
            if (isBind) {
                mView.hideWaitDialog()
                mView.showSnakeBar("绑定失败，该 QQ 已经绑定过其他账户")
            } else {
                val result = model.bindQQ(
                    context,
                    userBindQQOpenId,
                    userBindQQAccessToken,
                    userBindQQExpiresTime,
                    userBindQQ,
                    userBindQQIcon
                )
                if (result) {
                    mView.hideWaitDialog()
                    mView.showSnakeBar("QQ 绑定成功")
                    mView.updateQqBindStatus(true)
                } else {
                    mView.hideWaitDialog()
                    mView.startErrorAnimation(imgQQ)
                    mView.showSnakeBar("QQ 绑定失败")
                }
            }
        }
    }

    /**
     * 解除绑定微博
     */
    fun unBindQQ(context: Context, imgQQ: ImageView) {
        threadPool.execute {
            val result: Boolean = model.unBindQQ(context)
            if (result) {
                mView.hideWaitDialog()
                mView.updateQqBindStatus(false)
                mView.showSnakeBar("QQ 解除绑定成功")
            } else {
                mView.hideWaitDialog()
                mView.startErrorAnimation(imgQQ)
                mView.showSnakeBar("QQ 解除绑定失败")
            }
        }
    }

    fun changePassword(
        context: Context,
        etPassword: EditText,
        etRepeatPassword: EditText,
        layoutChangePassword: LinearLayout?
    ) {
        val result: Boolean = model.isOnlyThirdPartyLogin(context)
        if (result) {
            mView.showSnakeBar("您没有绑定手机，无需修改密码")
            mView.startErrorAnimation(layoutChangePassword)
        } else {
            if (TextUtils.isEmpty(etPassword.text) || TextUtils.isEmpty(etRepeatPassword.text)) {
                mView.showSnakeBar("密码不能为空")
                mView.startErrorAnimation(layoutChangePassword)
                return
            }

            if (etPassword.text.toString().trim() != etRepeatPassword.text.toString()
                    .trim()
            ) {
                mView.showSnakeBar("两次输入不同")
                mView.startErrorAnimation(layoutChangePassword)
                return
            }

            val passwordMinLength = 6
            if (etPassword.text.toString().trim().length < passwordMinLength) {
                mView.showSnakeBar("密码必须 6 位以上")
                mView.startErrorAnimation(layoutChangePassword)
                return
            }
            mView.showWaitDialog()
            threadPool.execute {
                val changeResult: Boolean =
                    model.changePassword(context, etPassword.text.toString().trim())
                if (changeResult) {
                    mView.hideWaitDialog()
                    mView.showSnakeBar("密码修改成功")
                } else {
                    mView.hideWaitDialog()
                    mView.showSnakeBar("密码修改出错，请稍后重试")
                    mView.startErrorAnimation(layoutChangePassword)
                }
            }
        }
    }

    fun changeUserName(context: Context, userName: EditText, layout: LinearLayout?) {
        if (TextUtils.isEmpty(userName.text)) {
            mView.showSnakeBar("名称不能为空")
            mView.startErrorAnimation(layout)
            return
        }

        val userNameMinLength = 1
        val userNameMaxLength = 10
        if (userName.text.toString().trim().length < userNameMinLength || userName.text.toString()
                .trim().length > userNameMaxLength
        ) {
            mView.showSnakeBar("用户名长度为 1-10")
            mView.startErrorAnimation(layout)
            return
        }

        mView.showWaitDialog()
        threadPool.execute {
            val changeResult: Boolean =
                model.changeUserName(context, userName.text.toString().trim())
            if (changeResult) {
                mView.hideWaitDialog()
                mView.showSnakeBar("名称修改成功")
                mView.updateUsername(userName.text.toString())
            } else {
                mView.hideWaitDialog()
                mView.showSnakeBar("名称修改出错，请稍后重试")
                mView.startErrorAnimation(layout)
            }
        }
    }

    fun bindPhoneRequestSMSCode(etPhone: EditText, btGetSMSCode: Button) {
        if (TextUtils.isEmpty(etPhone.text)) {
            mView.startErrorAnimation(btGetSMSCode)
            mView.showToast("手机号不能为空")
            return
        }

        if (!StringUtil.isPhoneNumber(etPhone.text.toString().trim())) {
            mView.startErrorAnimation(btGetSMSCode)
            mView.showToast("手机号不合法")
            return
        }

        mView.showWaitDialog()
        threadPool.execute {
            val isRegistered = model.phoneIsRegistered(etPhone.text.toString().trim())
            if (isRegistered) {
                mView.hideWaitDialog()
                mView.startErrorAnimation(btGetSMSCode)
                mView.showToast("手机号已经注册过了，无法绑定")
            } else {
//                BmobSMS.requestSMSCode(
//                    etPhone.text.toString().trim(),
//                    "",
//                    object : QueryListener<Int>() {
//                        override fun done(smsId: Int?, ex: BmobException?) {
//                            threadPool.execute {
//                                if (ex == null) {
//                                    mView.hideWaitDialog()
//                                    mView.showToast(
//                                        "验证码已经发送到 " + etPhone.text.toString()
//                                            .trim() + " 手机上"
//                                    )
//                                    mView.timeCountDown()
//                                } else {
//                                    mView.hideWaitDialog()
//                                    mView.startErrorAnimation(btGetSMSCode)
//                                    mView.showToast("获取验证码出错，请稍候重试")
//                                }
//                            }
//                        }
//                    })
            }
        }
    }

    /**
     * 绑定手机
     */
    fun bindPhone(
        context: Context,
        etPhone: EditText,
        etCode: EditText,
        etPassword: EditText,
        btBindPhone: Button
    ) {
        if (TextUtils.isEmpty(etPhone.text) || !StringUtil.isPhoneNumber(
                etPhone.text.toString().trim()
            )
        ) {
            mView.startErrorAnimation(etPhone)
            mView.showToast("手机号不合法")
            return
        }

        if (TextUtils.isEmpty(etCode.text)) {
            mView.showToast("验证码没有填写")
            mView.startErrorAnimation(etCode)
            return
        }

        if (TextUtils.isEmpty(etPassword.text)) {
            mView.showToast("密码不能为空")
            mView.startErrorAnimation(etPassword)
            return
        }

        mView.showWaitDialog()
        threadPool.execute {
//            BmobSMS.verifySmsCode(
//                etPhone.text.toString().trim(),
//                etCode.text.toString().trim(),
//                object : UpdateListener() {
//                    override fun done(ex: BmobException?) {
//                        threadPool.execute {
//                            if (ex == null) {
//                                val result = model.bindPhone(
//                                    context,
//                                    etPhone.text.toString().trim(),
//                                    etPassword.text.toString().trim()
//                                )
//                                if (result) {
//                                    mView.hideWaitDialog()
//                                    mView.hideBindPhoneDialog()
//                                    mView.updatePhone(etPhone.text.toString().trim())
//                                    mView.showSnakeBar("手机绑定成功")
//                                } else {
//                                    mView.hideWaitDialog()
//                                    mView.showToast("手机绑定出错，请稍后重试")
//                                    mView.startErrorAnimation(btBindPhone)
//                                }
//                            } else {
//                                mView.hideWaitDialog()
//                                mView.showToast("验证码错误，请检查后重试")
//                                mView.startErrorAnimation(btBindPhone)
//                            }
//                        }
//                    }
//                })
        }
    }
}
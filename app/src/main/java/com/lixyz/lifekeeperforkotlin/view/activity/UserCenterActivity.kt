package com.lixyz.lifekeeperforkotlin.view.activity

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.igexin.sdk.PushManager
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.net.GlideApp
import com.lixyz.lifekeeperforkotlin.presenter.UserCenterPresenter
import com.lixyz.lifekeeperforkotlin.service.GuardService
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.tencent.connect.UserInfo
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.zhihu.matisse.Matisse
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity___user_center.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*


/**
 * @author LGB
 * 个人中心 Activity
 */
class UserCenterActivity : BaseActivity(), View.OnClickListener,
    IUserCenterView {
    /**
     * Presenter
     */
    private var presenter: UserCenterPresenter? = null

    /**
     * 等待   Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * 返回按钮
     */
    private var imgBack: ImageView? = null

    /**
     * 用户头像
     */
    private var imgUserIcon: CircleImageView? = null

    /**
     * 用户名
     */
    private var tvUserName: TextView? = null

    /**
     * 手机号
     */
    private var tvPhone: TextView? = null

    /**
     * 绑定微信
     */
    private var imgBindWeixin: ImageView? = null

    /**
     * QQ登录
     */
    var mTencent: Tencent? = null

    /**
     * 绑定微博
     */
    private var imgBindWeibo: ImageView? = null

    /**
     * 绑定 QQ
     */
    private var imgBindQq: ImageView? = null

    /**
     * 修改用户名
     */
    private var layoutChangeUserName: LinearLayout? = null

    /**
     * 修改密码
     */
    private var layoutChangePassword: LinearLayout? = null

    /**
     * 绑定手机号
     */
    private var layoutBindPhone: LinearLayout? = null

    /**
     * 绑定手机文字
     */
    private var tvSetUserPhone: TextView? = null

    /**
     * 退出登录
     */
    private var layoutLogout: LinearLayout? = null


    /**
     * Handler
     */
    private val handler: MyHandler = MyHandler(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view: View = window.decorView
        val option: Int = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        view.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity___user_center)
        initWidget()
    }

    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        presenter!!.viewOnResume(this)
    }

    override fun updateUserInfo(user: UserBean) {
        if (user.userId != null) {
            //用户名称
            tvUserName!!.text = user.userName
            //用户头像
            if (user.userIconUrl!!.startsWith("http://") || user.userIconUrl!!.startsWith(
                    "https://"
                )
            ) {
                Glide.with(imgUserIcon!!)
                    .load(user.userIconUrl).error(
                        Glide.with(imgUserIcon!!)
                            .load(R.drawable.login___viewpager___login___user_icon)
                    ).into(imgUserIcon!!)
            } else {
                GlideApp.with(imgUserIcon!!)
                    .load(Constant.ADDRESS + "/LifeKeeper/resource/LifeKeeperUserIcon/" + user.userIconUrl)
                    .apply(
                        RequestOptions()
                            .transform(CenterCrop(), RoundedCorners(20))
                    )
                    .into(imgUserIcon!!)
            }
            //用户电话
            if (user.userPhone != null) {
                tvPhone!!.visibility = View.VISIBLE
                tvPhone!!.text = StringUtil.phoneToStar(user.userPhone!!)
                layoutChangePassword!!.visibility = View.VISIBLE
                tvSetUserPhone!!.text = "修改手机"
            } else {
                tvPhone!!.visibility = View.INVISIBLE
                layoutChangePassword!!.visibility = View.GONE
                tvSetUserPhone!!.text = "绑定手机"
            }
            //微博
            if (user.userBindWeiboId != null) {
                imgBindWeibo!!.setImageResource(R.drawable.personal_information___weibo_icon)
            } else {
                imgBindWeibo!!.setImageResource(R.drawable.personal_information___unbind_weibo_icon)
            }
            //QQ
            if (user.userBindQQOpenId != null) {
                imgBindQq!!.setImageResource(R.drawable.personal_information___qq_icon)
            } else {
                imgBindQq!!.setImageResource(R.drawable.personal_information___unbind_qq_icon)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            //返回
            R.id.img_back -> {
                finish()
            }
            //设置用户头像
            R.id.img_user_icon -> {
                presenter!!.selectUserIcon(this, this)
            }
            //绑定微信
            R.id.img_bind_weixin -> {
                Snackbar.make(
                    imgBindWeixin!!,
                    "暂时无法绑定微信",
                    Snackbar.LENGTH_SHORT
                ).show()
                startErrorAnimation(imgBindWeixin)
            }
            //绑定微博
            R.id.img_bind_weibo -> {
//                presenter!!.checkWeiboBind(this)
            }
            //绑定 QQ
            R.id.img_bind_qq -> {
//                presenter!!.checkQQBind(this)
            }
            //修改密码
            R.id.layout_change_password -> {
//                showChangePasswordDialog()
            }
            //修改名称
            R.id.layout_change_username -> {
//                showChangeUsernameDialog()
            }
            //修改手机号
            R.id.layout_change_phone -> {
//                showChangePhoneDialog()
            }
            //退出登录
            R.id.layout_logout -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage("确定退出？")
                builder.setPositiveButton(
                    "exit"
                ) { _, _ -> presenter!!.logout(this) }
                val dialog: AlertDialog = builder.create()
                dialog.show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            }
            else -> {
            }
        }
    }

    override fun initWidget() {
//        mSsoHandler = SsoHandler(this)
        waitDialog = CustomDialog(this, "请稍候")
        imgBack = findViewById(R.id.img_back)
        imgUserIcon = findViewById(R.id.img_user_icon)
        tvUserName = findViewById(R.id.tv_username)
        tvPhone = findViewById(R.id.tv_phone)
        imgBindWeixin = findViewById(R.id.img_bind_weixin)
        imgBindWeibo = findViewById(R.id.img_bind_weibo)
        imgBindQq = findViewById(R.id.img_bind_qq)
        layoutChangeUserName = findViewById(R.id.layout_change_username)
        layoutChangePassword = findViewById(R.id.layout_change_password)
        layoutBindPhone = findViewById(R.id.layout_change_phone)
        tvSetUserPhone = findViewById(R.id.tv_user_phone)
        layoutLogout = findViewById(R.id.layout_logout)

        presenter = UserCenterPresenter(this, this)
    }

    override fun initListener() {
        imgBack!!.setOnClickListener(this)
        imgUserIcon!!.setOnClickListener(this)
        tvUserName!!.setOnClickListener(this)
        imgBindWeixin!!.setOnClickListener(this)
        imgBindWeibo!!.setOnClickListener(this)
        imgBindQq!!.setOnClickListener(this)
        layoutChangeUserName!!.setOnClickListener(this)
        layoutChangePassword!!.setOnClickListener(this)
        layoutBindPhone!!.setOnClickListener(this)
        layoutLogout!!.setOnClickListener(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width: Int = size.x
            val userIconWidth = width / 5
            val userIconHeight = width / 5
            val params = imgUserIcon!!.layoutParams as RelativeLayout.LayoutParams
            params.width = userIconWidth
            params.height = userIconHeight
            params.topMargin = 0 - userIconWidth / 2
            imgUserIcon!!.layoutParams = params
        }
    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun hideWaitDialog() {
        handler.sendEmptyMessage(DISMISS_DIALOG)
    }

    override fun showSnakeBar(message: String?) {
        Snackbar.make(layoutLogout!!, message!!, Snackbar.LENGTH_SHORT).show()
    }

    override fun showToast(message: String?) {
        val msg = Message.obtain()
        msg.what = SHOW_TOAST
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun updateWarning(message: String) {
        val msg = Message()
        msg.obj = message
        msg.what = UPDATE_DIALOG_WARNING
        handler.sendMessage(msg)
    }

    override fun dismissBindPhoneDialog() {
        handler.sendEmptyMessage(DISMISS_BIND_PHONE_DIALOG)
    }

    override fun showBindPhoneDialogWarning(message: String) {
        val msg = Message()
        msg.what = SHOW_BIND_PHONE_DIALOG_WARNING
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun hideBindPhoneDialogWarning() {
        if (bindPhoneDialog!!.isShowing) {
            tvWarnings!!.visibility = View.GONE
        }
    }

    override fun logout() {
        handler.sendEmptyMessage(LOGOUT)
    }

    var bindPhoneDialog: AlertDialog? = null
    var btGetSMSCode: Button? = null
    private var etCode: EditText? = null
    private var etPhone: EditText? = null
    private var etPassword: EditText? = null
    private var btBindPhone: Button? = null
    private var tvWarnings: TextView? = null

    private fun showChangePhoneDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.view___bind_phone, LinearLayout(this), false)
        val close: ImageView = view.findViewById(R.id.img_close)
        etPhone = view.findViewById(R.id.et_bind_phone)
        etCode = view.findViewById(R.id.et_bind_code)
        btGetSMSCode = view.findViewById(R.id.bt_bind_get_code)
//        etPassword = view.findViewById(R.id.et_password)
        btBindPhone = view.findViewById(R.id.bt_bind)
        tvWarnings = view.findViewById(R.id.tv_warning)
        builder.setCancelable(false)
        builder.setView(view)
        bindPhoneDialog = builder.create()
        bindPhoneDialog!!.setCanceledOnTouchOutside(false)
        bindPhoneDialog!!.show()
        close.setOnClickListener {
            bindPhoneDialog!!.dismiss()
        }
        btGetSMSCode!!.setOnClickListener {
            hideBindPhoneDialogWarning()
            presenter!!.bindPhoneRequestSMSCode(etPhone!!, btGetSMSCode!!)
        }
        btBindPhone!!.setOnClickListener {
            hideBindPhoneDialogWarning()
            presenter!!.bindPhone(
                this@UserCenterActivity,
                etPhone!!,
                etCode!!,
                etPassword!!,
                btBindPhone!!
            )
        }
    }

    override fun hideBindPhoneDialog() {
        handler.sendEmptyMessage(HIDE_BIND_PHONE_DIALOG)
    }

    /**
     * 显示修改用户名的 Dialog
     */
    private fun showChangeUsernameDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.view___change_username, LinearLayout(this), false)
        val userName: EditText = view.findViewById(R.id.et_username)
        builder.setView(view)
        builder.setNegativeButton("确定") { dialog, _ ->
            dialog.dismiss()
            presenter!!.changeUserName(this, userName, layoutChangeUserName)
        }
        builder.show()
    }


    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.view___change_password, LinearLayout(this), false)
        val etPassword: EditText = view.findViewById(R.id.et_password)
        val etRepeatPassword: EditText = view.findViewById(R.id.et_repeat_password)
        builder.setView(view)
        builder.setNegativeButton("确定") { dialog, _ ->
            dialog.dismiss()
            presenter!!.changePassword(this, etPassword, etRepeatPassword, layoutChangePassword)
        }
        builder.show()
    }

    override fun updateWeiboBindStatus(isBind: Boolean) {
        val msg: Message = Message.obtain()
        msg.what = UPDATE_WEIBO_BIND_STATUS
        msg.obj = isBind
        handler.sendMessage(msg)
    }

    override fun updateQqBindStatus(isBind: Boolean) {
        val msg: Message = Message.obtain()
        msg.what = UPDATE_QQ_BIND_STATUS
        msg.obj = isBind
        handler.sendMessage(msg)
    }

    override fun updateUsername(username: String?) {
        val message: Message = Message.obtain()
        message.what = UPDATE_USERNAME_MESSAGE
        message.obj = username
        handler.sendMessage(message)
    }

    override fun updatePhone(phone: String?) {
        val message = Message.obtain()
        if (phone == null) {
            handler.sendEmptyMessage(HIDE_USER_PHONE)
        } else {
            message.obj = StringUtil.phoneToStar(phone)
            message.what = UPDATE_USER_PHONE
            handler.sendMessage(message)
        }
    }

    override fun updateUserIcon(userIconUrl: String?) {
        Glide.with(this).load(userIconUrl)
            .error(Glide.with(this).load(R.drawable.login___viewpager___login___user_icon)).into(
                imgUserIcon!!
            )
    }

    override fun updateLoginStatus(isLogin: Boolean) {
        if (!isLogin) {
            imgUserIcon!!.isClickable = false
            imgBindWeixin!!.isClickable = false
            imgBindWeibo!!.isClickable = false
            imgBindQq!!.isClickable = false
            layoutChangePassword!!.isClickable = false
            layoutChangeUserName!!.isClickable = false
            layoutLogout!!.isClickable = false
        }
    }

    override fun bindWeibo() {
        showWaitDialog()
//        val wbAuthListener = SelfWbAuthListener()
//        mSsoHandler!!.authorize(wbAuthListener)
    }

    override fun unBindWeibo() {
        showWaitDialog()
        presenter!!.unBindWeibo(this, imgBindWeibo!!)
    }

    override fun bindQQ() {
        showWaitDialog()
        if (mTencent == null) {
            mTencent = Tencent.createInstance(
                "101901025",
                this,
                "com.lixyz.lifekeeperforkotlin.qq.fileprovider"
            )
        }
        mTencent!!.login(this, "all", loginListener)
    }

    override fun unBindQQ() {
        showWaitDialog()
        presenter!!.unBindQQ(this, imgBindQq!!)
    }

    override fun timeCountDown() {
        try {
            val count = 5
            for (i in count downTo 0) {
                val message = Message.obtain()
                if (i == 0) {
                    message.what = BIND_PHONE_TIME_COUNT_DOWN_END
                } else {
                    message.what = BIND_PHONE_TIME_COUNT_DOWN
                    message.arg1 = i
                }
                handler.sendMessage(message)
                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            handler.sendEmptyMessage(BIND_PHONE_TIME_COUNT_DOWN_END)
        }
    }

    override fun startErrorAnimation(view: View?) {
        val msg = Message.obtain()
        msg.what = ERROR_ANIMATION
        msg.obj = view
        handler.sendMessage(msg)
    }

    fun startAnimator(view: View?, animResource: Int) {
        val animator: Animator =
            AnimatorInflater.loadAnimator(this@UserCenterActivity, animResource)
        animator.setTarget(view)
        animator.start()
    }

    /**
     * 绑定微博
     */
//    inner class SelfWbAuthListener : com.sina.weibo.sdk.auth.WbAuthListener {
//        override fun onSuccess(token: Oauth2AccessToken) {
//            val client = OkHttpClient.Builder()
//                .connectTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
//                .readTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
//                .writeTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
//                .retryOnConnectionFailure(true)
//                .build()
//            val request: Request = Request.Builder().url(
//                "https://api.weibo.com/2/users/show.json?access_token=" + token.token
//                    .toString() + "&uid=" + token.uid
//            ).build()
//            client.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    hideWaitDialog()
//                    startErrorAnimation(imgBindWeibo)
//                    showSnakeBar("出错了，请稍后重试")
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    val body = response.body
//                    if (body != null) {
//                        try {
//                            val weiboJson = JSONObject(body.string())
//                            val userBindWeibo: String = weiboJson.getString("name")
//                            val userBindWeiboExpiresTime =
//                                token.expiresTime.toString()
//                            val userBindWeiboAccessToken = token.token
//                            val userBindWeiboIcon: String = weiboJson.getString("avatar_large")
//                            val userBindWeiboId: String = weiboJson.getString("idstr")
//                            presenter!!.bindWeibo(
//                                this@UserCenterActivity,
//                                userBindWeibo,
//                                userBindWeiboExpiresTime,
//                                userBindWeiboAccessToken,
//                                userBindWeiboIcon,
//                                userBindWeiboId,
//                                imgBindWeibo!!
//                            )
//                        } catch (e: JSONException) {
//                            e.printStackTrace()
//                            hideWaitDialog()
//                            startErrorAnimation(imgBindWeibo)
//                            showSnakeBar("微博登录失败，请稍后重试")
//                        }
//                    }
//                }
//            })
//        }
//
//        override fun cancel() {
//            hideWaitDialog()
//            startErrorAnimation(imgBindWeibo)
//            showSnakeBar("绑定微博已取消")
//        }
//
//        override fun onFailure(errorMessage: WbConnectErrorMessage?) {
//            hideWaitDialog()
//            startErrorAnimation(imgBindWeibo)
//            showSnakeBar("绑定微博失败，请检查后重试")
//        }
//    }

    /**
     * 绑定 QQ
     */
    private var loginListener: IUiListener = object : IUiListener {
        override fun onComplete(response: Any?) {
            val `object`: JSONObject = response as JSONObject
            val userBindQqOpenId: String = `object`.getString("openid")
            val userBindQqAccessToken: String = `object`.getString("access_token")
            val userBindQqExpiresTime: String = `object`.getString("expires_time")
            mTencent!!.openId = userBindQqOpenId
            mTencent!!.setAccessToken(userBindQqAccessToken, userBindQqExpiresTime)
            val userInfo = UserInfo(this@UserCenterActivity, mTencent!!.qqToken)
            userInfo.getUserInfo(object : IUiListener {
                override fun onComplete(o: Any) {
                    try {
                        val userInfoJson: JSONObject = o as JSONObject
                        val userBindQq: String = userInfoJson.getString("nickname")
                        val userBindQqIcon: String =
                            userInfoJson.getString("figureurl_qq_2").replace("\\", "")
                        presenter!!.bindQQ(
                            this@UserCenterActivity,
                            userBindQqOpenId,
                            userBindQqAccessToken,
                            userBindQqExpiresTime,
                            userBindQq,
                            userBindQqIcon,
                            imgBindQq!!
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        hideWaitDialog()
                        startErrorAnimation(imgBindQq)
                        showSnakeBar("QQ 登录失败，请检查后重试")
                    }
                }

                override fun onError(uiError: UiError) {
                    hideWaitDialog()
                    showSnakeBar("QQ 登录失败，请检查后重试")
                }

                override fun onCancel() {
                    hideWaitDialog()
                    showSnakeBar("QQ 登录失败，请检查后重试")
                }

                override fun onWarning(p0: Int) {
                    hideWaitDialog()
                    showSnakeBar("QQ 登录失败，请检查后重试")
                }
            })
        }

        override fun onError(p0: UiError?) {
            hideWaitDialog()
            startErrorAnimation(imgBindQq)
            showSnakeBar("QQ 登录失败，请检查后重试")
        }

        override fun onCancel() {
            hideWaitDialog()
            startErrorAnimation(imgBindQq)
            showSnakeBar("取消 QQ 登录")
        }

        override fun onWarning(p0: Int) {
            hideWaitDialog()
            startErrorAnimation(imgBindQq)
            showSnakeBar("QQ 登录失败，请检查后重试")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //QQ登录result
        if (requestCode == BIND_QQ_REQUEST_CODE && resultCode == RESULT_OK) {
            Tencent.onActivityResultData(requestCode, resultCode, data, object : IUiListener {
                override fun onComplete(o: Any) {}
                override fun onError(uiError: UiError) {}
                override fun onCancel() {}
                override fun onWarning(p0: Int) {
                }
            })
        }
//        if (requestCode == WEIBO_LOGIN_REQUEST_CODE && resultCode == RESULT_OK && mSsoHandler != null) {
//            mSsoHandler!!.authorizeCallBack(requestCode, resultCode, data)
//        }
        //选择头像图片
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            val imagePath = Matisse.obtainPathResult(data)[0]
            val intent = Intent(this, SetUserIconActivity::class.java)
            intent.putExtra("imageUri", imagePath)
            startActivityForResult(intent, REQUEST_CODE_CLIP_USER_ICON)
        }

        //裁剪过头像后
        if (requestCode == REQUEST_CODE_CLIP_USER_ICON && resultCode == RESULT_OK) {
            val iconUri = data!!.getStringExtra("UserIconUrl")
            Glide.with(this).load(iconUri)
                .error(Glide.with(this).load(R.drawable.login___viewpager___login___user_icon))
                .into(
                    imgUserIcon!!
                )
        }
    }

    private class MyHandler(activity: UserCenterActivity) : Handler() {
        private val mActivity: WeakReference<UserCenterActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            val activity: UserCenterActivity = mActivity.get()!!
            when (msg.what) {
                DISMISS_DIALOG -> {
                    if (activity.waitDialog!!.isShowing) {
                        activity.waitDialog!!.dismiss()
                    }
                }
                UPDATE_USERNAME_MESSAGE -> {
                    activity.tvUserName!!.text =
                        msg.obj as String
                }
                UPDATE_WEIBO_BIND_STATUS -> {
                    if (msg.obj as Boolean) {
                        activity.imgBindWeibo!!.setImageResource(R.drawable.personal_information___weibo_icon)
                    } else {
                        activity.imgBindWeibo!!.setImageResource(R.drawable.personal_information___unbind_weibo_icon)
                    }
                }
                UPDATE_QQ_BIND_STATUS -> {
                    if (msg.obj as Boolean) {
                        activity.imgBindQq!!.setImageResource(R.drawable.personal_information___qq_icon)
                    } else {
                        activity.imgBindQq!!.setImageResource(R.drawable.personal_information___unbind_qq_icon)
                    }
                }
                ERROR_ANIMATION -> {
                    val view = msg.obj as View
                    activity.startAnimator(view, R.anim.error_warning)
                }
                BIND_PHONE_TIME_COUNT_DOWN -> {
                    if (activity.waitDialog!!.isShowing) {
                        activity.waitDialog!!.dismiss()
                    }
                    if (activity.bindPhoneDialog!!.isShowing) {
                        activity.btGetSMSCode!!.isClickable = false
                        activity.btGetSMSCode!!.text = java.lang.String.format(
                            Locale.CHINA,
                            "请等待 %d",
                            msg.arg1
                        )
                    }
                }
                BIND_PHONE_TIME_COUNT_DOWN_END -> {
                    activity.btGetSMSCode!!.isClickable = true
                    activity.btGetSMSCode!!.text = "获取验证码"
                }
                DISMISS_BIND_PHONE_DIALOG -> {
                    if (activity.bindPhoneDialog!!.isShowing) {
                        activity.bindPhoneDialog!!.dismiss()
                    }
                }
                SHOW_BIND_PHONE_DIALOG_WARNING -> {
                    if (activity.bindPhoneDialog!!.isShowing) {
                        activity.tvWarnings!!.visibility = View.VISIBLE
                        activity.tvWarnings!!.text = msg.obj as String
                    }
                }
                LOGOUT -> {
                    val guardService = Intent(activity, GuardService::class.java)
                    activity.stopService(guardService)

                    PushManager.getInstance().turnOffPush(activity)

                    val intent = Intent(activity, IndexActivity::class.java)
                    activity.startActivity(intent)
                }
                HIDE_USER_PHONE -> {
                    activity.tvPhone!!.visibility = View.INVISIBLE
                }
                UPDATE_USER_PHONE -> {
                    activity.tvPhone!!.visibility = View.VISIBLE
                    activity.tvPhone!!.text = msg.obj as String
                    activity.layout_change_phone.visibility = View.VISIBLE
                }
                SHOW_TOAST -> {
                    Toast.makeText(activity, msg.obj as String, Toast.LENGTH_SHORT).show()
                }
                HIDE_BIND_PHONE_DIALOG -> {
                    if (activity.bindPhoneDialog!!.isShowing) {
                        activity.bindPhoneDialog!!.dismiss()
                    }
                }
                else -> {
                }
            }
        }

    }

    companion object {
        /**
         * 微博登录 RequestCode
         */
        private const val WEIBO_LOGIN_REQUEST_CODE = 32973

        /**
         * 绑定 QQ request code
         */
        const val BIND_QQ_REQUEST_CODE = 11101

        /**
         * 选择图片 RequestCode
         */
        private const val REQUEST_CODE_CHOOSE = 90000

        /**
         * 裁剪图片 RequestCode
         */
        private const val REQUEST_CODE_CLIP_USER_ICON = 100000

        /**
         * 隐藏 Dialog 的消息
         */
        private const val DISMISS_DIALOG = 100

        /**
         * 修改用户名的消息
         */
        private const val UPDATE_USERNAME_MESSAGE = 300

        /**
         * 修改微博绑定的消息
         */
        private const val UPDATE_WEIBO_BIND_STATUS = 500

        /**
         * 修改QQ绑定的消息
         */
        private const val UPDATE_QQ_BIND_STATUS = 600

        /**
         * 执行错误提示动画的 Handler 消息
         */
        private const val ERROR_ANIMATION = 700

        private const val BIND_PHONE_TIME_COUNT_DOWN = 800

        private const val BIND_PHONE_TIME_COUNT_DOWN_END = 900

        private const val UPDATE_DIALOG_WARNING = 1000

        private const val DISMISS_BIND_PHONE_DIALOG = 1100

        private const val SHOW_BIND_PHONE_DIALOG_WARNING = 1200

        private const val LOGOUT = 1300

        private const val HIDE_USER_PHONE = 1400

        private const val UPDATE_USER_PHONE = 1500

        private const val SHOW_TOAST = 1600

        private const val HIDE_BIND_PHONE_DIALOG = 1700
    }
}
package com.lixyz.lifekeeperforkotlin.view.activity

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.presenter.LoginPresenter
import com.lixyz.lifekeeperforkotlin.utils.StatusBarUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.openapi.WBAPIFactory
import com.tencent.connect.UserInfo
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class LoginActivity : BaseActivity(), ILoginView, View.OnClickListener {

    companion object {
        /**
         * 微博登录 request code
         */
        private const val WEIBO_LOGIN_REQUEST_CODE = 32973

        /**
         * QQ 登录 request code
         */
        private const val BIND_QQ_REQUEST_CODE = 11101
    }


    /**
     * 用户协议
     */
    private var tvUserAgreement: TextView? = null

    /**
     * 手机号输入框
     */
    private var etPhone: EditText? = null

    /**
     * 密码输入框
     */
    private var etPassword: EditText? = null

    /**
     * 登录按钮
     */
    private var btLogin: Button? = null

    /**
     *  QQ
     */
    private var imgQQ: ImageView? = null

    /**
     *  微博
     */
    private var imgWeibo: ImageView? = null

    /**
     *  微信
     */
    private var imgWeixin: ImageView? = null

    /**
     * 手机号验证码登录
     */
    private var tvLoginForVerifyCode: TextView? = null

    /**
     * 找回密码
     */
    private var tvFindPassword: TextView? = null

    /**
     * Presenter
     */
    private var presenter: LoginPresenter? = null

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * 登录成功表示
     */
    private var loginSuccessful = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarTransparent(this)
        setContentView(R.layout.activity___login)
        initWidget()
        initSdk()
    }

    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter!!.shutdownThreadPool()
    }

    override fun initWidget() {
        presenter = LoginPresenter(this)
        waitDialog = CustomDialog(this, this, "请稍候")

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = StatusBarUtil.getStatusBarHeight(this).toInt() * 2
        layoutParams.marginStart = StatusBarUtil.getStatusBarHeight(this).toInt()

        etPhone = findViewById(R.id.et_phone)
        etPassword = findViewById(R.id.et_password)
        btLogin = findViewById(R.id.bt_login)
        imgWeixin = findViewById(R.id.img_weixin)
        imgQQ = findViewById(R.id.img_qq)
        imgWeibo = findViewById(R.id.img_weibo)
        tvLoginForVerifyCode = findViewById(R.id.tv_login_for_verify_code)
        tvFindPassword = findViewById(R.id.tv_find_password)
        tvUserAgreement = findViewById(R.id.tv_user_agreement)
        tvUserAgreement!!.text = getClickableSpan()
        tvUserAgreement!!.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun initListener() {
        btLogin!!.setOnClickListener(this)
        imgQQ!!.setOnClickListener(this)
        imgWeibo!!.setOnClickListener(this)
        imgWeixin!!.setOnClickListener(this)
        tvLoginForVerifyCode!!.setOnClickListener(this)
        tvFindPassword!!.setOnClickListener(this)

        waitDialog!!.setOnDismissListener {
            if (loginSuccessful) {
                finish()
            }
        }
    }

    private fun getClickableSpan(): SpannableString {
        val spannableString = SpannableString("注册/使用即表示您同意该软件的使用条款和隐私协议")
        //设置下划线文字
        spannableString.setSpan(UnderlineSpan(), 15, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        //设置文字的单击事件
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, UserAgreementActivity::class.java)
                startActivity(intent)
            }
        }, 15, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        //设置文字的前景色
        spannableString.setSpan(
            ForegroundColorSpan(Color.RED),
            15,
            24,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }


    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun updateWaitDialog(msg: String?) {
        runOnUiThread {
            if (waitDialog!!.isShowing) {
                waitDialog!!.setMessage(msg!!)
            }
        }
    }

    override fun hideWaitDialog() {
        runOnUiThread {
            if (waitDialog!!.isShowing) {
                waitDialog!!.dismiss()
            }
        }
    }

    override fun showSnakeBar(msg: String?) {
        runOnUiThread {
            Snackbar.make(btLogin!!, msg!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun updateViewPageCurrentPage(index: Int) {

    }

    override fun closeResetPasswordDialog() {

    }

    override fun startErrorAnimation(view: View?) {
        runOnUiThread {
            val animator: Animator =
                AnimatorInflater.loadAnimator(this@LoginActivity, R.animator.error_warning)
            animator.setTarget(view)
            animator.start()
        }
    }

    override fun timeCountDown(type: Int) {

    }

    override fun loginSuccess() {
        runOnUiThread {
            loginSuccessful = true
            waitDialog!!.setSuccessful()
        }
    }

    override fun hideSoftInput(view: View?) {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    override fun bindPhoneIsRegistered() {

    }

    override fun showBindPhoneDialog(user: UserBean) {

    }

    override fun closeBindPhoneDialog() {

    }

    override fun qqLoginBindPhone(user: UserBean) {
        runOnUiThread {
            hideWaitDialog()
            val intent = Intent(this, VerifyCodeActivity::class.java)
            intent.putExtra("PageType", 1)
            intent.putExtra("UserBean", user)
            startActivity(intent)
        }
    }

    override fun weiboLoginBindPhone(user: UserBean) {
        runOnUiThread {
            hideWaitDialog()
            val intent = Intent(this, VerifyCodeActivity::class.java)
            intent.putExtra("UserBean", user)
            intent.putExtra("PageType", 1)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //QQ登录result
        if (requestCode == BIND_QQ_REQUEST_CODE) {
            if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
                Tencent.onActivityResultData(
                    requestCode,
                    resultCode,
                    data,
                    loginListener
                )
            }
        }

//        微博登录 result
        if (requestCode == WEIBO_LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: 1")
            mWBAPI!!.authorizeCallback(this, requestCode, resultCode, data)
        }
        if (requestCode == WEIBO_LOGIN_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            hideWaitDialog()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bt_login -> {
                hideSoftInput(btLogin)
                presenter!!.loginForPhoneAndPassword(this, etPhone!!, etPassword!!, btLogin!!)
            }
            R.id.img_weixin -> {
                Snackbar.make(btLogin!!, "暂不提供微信登录", Snackbar.LENGTH_SHORT).show()
            }
            R.id.img_qq -> {
                showWaitDialog()
                if (mTencent == null) {
                    mTencent = Tencent.createInstance(
                        "101901025",
                        this,
                        "com.lixyz.lifekeeperforkotlin.qq.fileprovider"
                    )
                }
                onQQClickLogin()
            }
            R.id.img_weibo -> {
                showWaitDialog()
                onWeiboClickLogin()
            }
            R.id.tv_login_for_verify_code -> {
                val intent = Intent(this, VerifyCodeActivity::class.java)
                intent.putExtra("PageType", 2)
                startActivity(intent)
            }
            R.id.tv_find_password -> {
                val intent = Intent(this, VerifyCodeActivity::class.java)
                intent.putExtra("PageType", 3)
                startActivity(intent)
            }
        }
    }

    /**
     * 新浪微博
     */
    private var mWBAPI: IWBAPI? = null
    private val WBAPPKEY = "3900325669"
    private val REDIRECTURL = "https://api.weibo.com/oauth2/default.html"
    private val SINAWEIBOSCOP = ("email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write")

    //init sdk
    private fun initSdk() {
        val authInfo = AuthInfo(this, WBAPPKEY, REDIRECTURL, SINAWEIBOSCOP)
        mWBAPI = WBAPIFactory.createWBAPI(this)
        mWBAPI!!.registerApp(this, authInfo)
    }

    /**
     * 绑定微博
     */
    private fun onWeiboClickLogin() {
        //auth
        mWBAPI!!.authorizeClient(this, object : WbAuthListener {
            override fun onComplete(token: Oauth2AccessToken) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
                        .readTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
                        .writeTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
                        .retryOnConnectionFailure(true)
                        .build()
                    val request: Request = Request.Builder().url(
                        "https://api.weibo.com/2/users/show.json?access_token=${token.accessToken}&uid=${token.uid}"
                    ).build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            hideWaitDialog()
                            startErrorAnimation(imgWeibo)
                            showSnakeBar("微博登录失败，请稍后重试")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val body = response.body
                            if (body != null) {
                                try {
                                    val weiboJson = JSONObject(body.string())
                                    val userBindWeibo: String = weiboJson.getString("name")
                                    val userBindWeiboExpiresTime =
                                        java.lang.String.valueOf(token.expiresTime)
                                    val userBindWeiboAccessToken = token.accessToken
                                    val userBindWeiboIcon: String =
                                        weiboJson.getString("avatar_large")
                                    val userBindWeiboId: String = weiboJson.getString("idstr")
                                    val userBean = UserBean()
                                    userBean.userBindWeibo = userBindWeibo
                                    userBean.userBindWeiboAccessToken = userBindWeiboAccessToken
                                    userBean.userBindWeiboExpiresTime = userBindWeiboExpiresTime
                                    userBean.userBindWeiboIcon = userBindWeiboIcon
                                    userBean.userBindWeiboId = userBindWeiboId
                                    presenter!!.loginForWeibo(
                                        this@LoginActivity,
                                        userBean,
                                        imgWeibo!!
                                    )
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    hideWaitDialog()
                                    startErrorAnimation(imgWeibo)
                                    showSnakeBar("微博登录失败，请稍后重试")
                                }
                            }
                        }
                    })
                } catch (e: IOException) {
                    e.printStackTrace()
                    hideWaitDialog()
                    startErrorAnimation(imgWeibo)
                    showSnakeBar("微博登录失败，请稍后重试")
                }


                Toast.makeText(this@LoginActivity, "微博授权成功", Toast.LENGTH_SHORT).show()
            }

            override fun onError(p0: com.sina.weibo.sdk.common.UiError?) {
                Log.d(TAG, "onError: $p0")
                Toast.makeText(this@LoginActivity, "微博授权出错", Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "微博授权取消", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /**
     * QQ登录
     */
    var mTencent: Tencent? = null

    /**
     * 绑定 QQ
     */
    private fun onQQClickLogin() {
        mTencent!!.login(this, "all", loginListener)
    }

    /**
     * 绑定 QQ
     */
    private var loginListener: IUiListener = object : IUiListener {
        override fun onComplete(response: Any?) {
            val `object`: JSONObject = response as JSONObject
            val userBindQqOpenId: String = `object`.getString("openid")
            val userBindQqAccessToken: String = `object`.getString("access_token")
            val userBindQqExpiresTime: String = `object`.getString("expires_time")
            Log.d(TAG, "onComplete: $userBindQqOpenId")
            mTencent!!.openId = userBindQqOpenId
            mTencent!!.setAccessToken(userBindQqAccessToken, userBindQqExpiresTime)
            val userInfo = UserInfo(this@LoginActivity, mTencent!!.qqToken)
            userInfo.getUserInfo(object : IUiListener {
                override fun onComplete(o: Any) {
                    Log.d(TAG, "onComplete: $o")
                    try {
                        val userInfoJson: JSONObject = o as JSONObject
                        val userBindQq: String = userInfoJson.getString("nickname")
                        val userBindQqIcon: String =
                            userInfoJson.getString("figureurl_qq_2").replace("\\", "")
                        val userBean = UserBean()
                        userBean.userBindQQOpenId = userBindQqOpenId
                        userBean.userBindQQAccessToken = userBindQqAccessToken
                        userBean.userBindQQExpiresTime = userBindQqExpiresTime
                        userBean.userBindQQ = userBindQq
                        userBean.userBindQQIcon = userBindQqIcon
                        presenter!!.loginForQQ(this@LoginActivity, userBean, imgQQ!!)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        hideWaitDialog()
                        startErrorAnimation(imgQQ)
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
            startErrorAnimation(imgQQ)
            showSnakeBar("QQ 登录失败，请检查后重试")
        }

        override fun onCancel() {
            hideWaitDialog()
            startErrorAnimation(imgQQ)
            showSnakeBar("取消 QQ 登录")
        }

        override fun onWarning(p0: Int) {
            hideWaitDialog()
            startErrorAnimation(imgQQ)
            showSnakeBar("QQ 登录失败，请检查后重试")
        }

    }
}

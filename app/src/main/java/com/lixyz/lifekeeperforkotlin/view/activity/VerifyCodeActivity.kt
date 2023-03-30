package com.lixyz.lifekeeperforkotlin.view.activity

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.VerifyCodeViewPagerAdapter
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.UserBean
import com.lixyz.lifekeeperforkotlin.presenter.VerifyCodePresenter
import com.lixyz.lifekeeperforkotlin.utils.StatusBarUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.SeparatedEditText
import com.lixyz.lifekeeperforkotlin.view.customview.ViewPagerSlide

class VerifyCodeActivity : BaseActivity(), View.OnClickListener, IVerifyCodeView {

    private var viewPager: ViewPagerSlide? = null
    private var viewPagerAdapter: VerifyCodeViewPagerAdapter? = null

    private var etPhone: EditText? = null
    private var btGetVerifyCode: Button? = null
    private var tvPhoneTitle: TextView? = null
    private var seVerifyCode: SeparatedEditText? = null
    private var btVerify: Button? = null
    private var etPassword: EditText? = null
    private var btResetPassword: Button? = null


    private var presenter: VerifyCodePresenter? = null
    private var waitDialog: CustomDialog? = null

    private var userBean: UserBean? = null

    private var loginSuccessful: Boolean = false

    /**
     * 页面功能类型
     * 1：第三方登录时，如果该账户没有注册过，绑定手机，成为新用户
     * 2：手机号/验证码登录
     * 3：重置密码
     */
    private var pageType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarTransparent(this)
        setContentView(R.layout.activity___verify_code)

        pageType = intent.getIntExtra("PageType", 0)
        if (pageType == 1) {
            userBean = intent.getSerializableExtra("UserBean") as UserBean
        }
        initWidget()

    }

    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun initWidget() {
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = StatusBarUtil.getStatusBarHeight(this).toInt() * 2
        layoutParams.marginStart = StatusBarUtil.getStatusBarHeight(this).toInt()

        viewPager = findViewById(R.id.viewPager)
        val phoneView =
            layoutInflater.inflate(
                R.layout.view___verify_code___viewpager___phone,
                RelativeLayout(this),
                false
            )
        val verifyCodeView = layoutInflater.inflate(
            R.layout.view___verify_code___viewpager___verify_code,
            RelativeLayout(this),
            false
        )
        val resetPasswordView = layoutInflater.inflate(
            R.layout.view___verify_code___viewpager___reset_password,
            RelativeLayout(this),
            false
        )
        val viewList = ArrayList<View>()
        viewList.add(phoneView)
        viewList.add(verifyCodeView)
        viewList.add(resetPasswordView)

        viewPagerAdapter = VerifyCodeViewPagerAdapter(viewList, this)
        viewPager!!.adapter = viewPagerAdapter

        val inputPhoneTitle: TextView = phoneView.findViewById(R.id.tv_input_phone_title)
        val inputPhoneTitleLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        inputPhoneTitleLayoutParams.topMargin =
            StatusBarUtil.getStatusBarHeight(this).toInt() * 2 + dip2px(this)
        inputPhoneTitleLayoutParams.marginStart = StatusBarUtil.getStatusBarHeight(this).toInt()
        inputPhoneTitle.layoutParams = inputPhoneTitleLayoutParams

        etPhone = phoneView.findViewById(R.id.et_phone)
        btGetVerifyCode = phoneView.findViewById(R.id.bt_get_verify_code)
        tvPhoneTitle = verifyCodeView.findViewById(R.id.tv_phone)
        seVerifyCode = verifyCodeView.findViewById(R.id.se_verify_code)
        btVerify = verifyCodeView.findViewById(R.id.bt_verify)

        val tvNewPasswordTitle: TextView =
            resetPasswordView.findViewById(R.id.tv_new_password_title)
        val tvNewPasswordTitleParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        tvNewPasswordTitleParams.topMargin =
            StatusBarUtil.getStatusBarHeight(this).toInt() * 2 + dip2px(this)
        tvNewPasswordTitleParams.marginStart = StatusBarUtil.getStatusBarHeight(this).toInt()
        tvNewPasswordTitle.layoutParams = tvNewPasswordTitleParams

        etPassword = resetPasswordView.findViewById(R.id.et_password)
        btResetPassword = resetPasswordView.findViewById(R.id.bt_reset_password)

        presenter = VerifyCodePresenter(this)
        waitDialog = CustomDialog(this, this, "请稍候...")

    }

    override fun initListener() {
        btGetVerifyCode!!.setOnClickListener(this)
        btVerify!!.setOnClickListener(this)
        btResetPassword!!.setOnClickListener(this)

        waitDialog!!.setOnDismissListener {
            if (loginSuccessful) {
                finish()
            }
        }
    }

    private fun dip2px(context: Context): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (50 * scale + 0.5f).toInt()
    }

    override fun startErrorAnimation(view: View) {
        runOnUiThread {
            val animator: Animator =
                AnimatorInflater.loadAnimator(this@VerifyCodeActivity, R.animator.error_warning)
            animator.setTarget(view)
            animator.start()
        }
    }

    override fun showSnakeBar(msg: String) {
        runOnUiThread {
            Snackbar.make(viewPager!!, msg, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun hideWaitDialog() {
        runOnUiThread {
            if (waitDialog!!.isShowing) {
                waitDialog!!.dismiss()
            }
        }
    }

    override fun requestVerifyCodeSuccess() {
        runOnUiThread {
            viewPager!!.currentItem = 1
            tvPhoneTitle!!.text =
                String.format(
                    resources.getString(R.string.VerifyCodeActivityPhoneTitle),
                    "验证码已经发送到${StringUtil.phoneToStar(etPhone!!.text.toString().trim())}"
                )
        }
    }

    override fun requestVerifyCodeFailured() {
        runOnUiThread {
            showSnakeBar("请求出错，请稍候重试...")
        }
    }

    override fun loginSuccess() {
        runOnUiThread {
            hideWaitDialog()
            val intent = Intent(this, IndexActivity::class.java)
            startActivity(intent)
        }
    }

    override fun setViewPagerCurrentPage(index: Int) {
        runOnUiThread {
            viewPager!!.currentItem = index
        }
    }

    override fun resetPasswordSuccess() {
        runOnUiThread {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private var verifyCodeStamp: String? = null

    override fun setCodeStamp(stamp: String) {
        this.verifyCodeStamp = stamp
    }

    override fun getPhone(): String {
        return etPhone!!.text.toString().trim()
    }


    override fun showSnackBar(message: String) {
        runOnUiThread {
            Snackbar.make(btGetVerifyCode!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun initYoTestCaptchaVerify() {
        runOnUiThread {
            presenter!!.initYoTestCaptchaVerify(this,pageType)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_back -> {
                finish()
            }
            R.id.bt_get_verify_code -> {
                when (pageType) {
                    1 -> {
                        presenter!!.thirdPartyLoginRequestSMSCode(this, etPhone!!)
                    }
                    2 -> {
                        presenter!!.verifyCodeLoginRequestSMSCode(this, etPhone!!)
                    }
                    3 -> {
                        presenter!!.resetPasswordRequestSMSCode(this, etPhone!!)
                    }
                }
            }
            R.id.bt_verify -> {
                when (pageType) {
                    1 -> {
                        presenter!!.thirdPartyLoginCheckVerifyCode(
                            this,
                            userBean!!,
                            etPhone,
                            seVerifyCode,
                            btVerify!!,
                            verifyCodeStamp!!
                        )
                    }
                    2 -> {
                        presenter!!.verifyCodeLoginCheckVerifyCode(
                            this,
                            etPhone,
                            seVerifyCode,
                            btVerify!!,
                            verifyCodeStamp!!
                        )
                    }
                    3 -> {
                        presenter!!.resetPasswordCheckVerifyCode(
                            this,
                            etPhone,
                            seVerifyCode,
                            btVerify!!,
                            verifyCodeStamp!!
                        )
                    }
                }
            }
            R.id.bt_reset_password -> {
                presenter!!.resetPassword(
                    this,
                    etPhone,
                    etPassword,
                    btResetPassword
                )
            }
        }
    }
}
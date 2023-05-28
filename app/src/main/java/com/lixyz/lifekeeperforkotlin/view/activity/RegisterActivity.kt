package com.lixyz.lifekeeperforkotlin.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.RegisterViewPagerAdapter
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.presenter.RegisterPresenter
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.SeparatedEditText
import com.lixyz.lifekeeperforkotlin.view.customview.ViewPagerSlide


class RegisterActivity : BaseActivity(), View.OnClickListener, IRegisterView {

    private var imgBack: ImageView? = null
    private var viewPager: ViewPagerSlide? = null
    private var viewPagerAdapter: RegisterViewPagerAdapter? = null

    private var etUserName: EditText? = null
    private var etPhone: EditText? = null
    private var btGetVerifyCode: Button? = null
    private var tvPhoneTitle: TextView? = null
    private var seVerifyCode: SeparatedEditText? = null
    private var btVerify: Button? = null
    private var etPassword: EditText? = null
    private var btRegister: Button? = null

    private var waitDialog: CustomDialog? = null
    private var presenter: RegisterPresenter? = null

    private var verifyCodeStamp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        view.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity___register)
        initWidget()
    }

    override fun onStart() {
        super.onStart()
        initListener()


    }

    override fun initWidget() {
        imgBack = findViewById(R.id.img_back)
        viewPager = findViewById(R.id.vp_viewpager)
        val phoneView = layoutInflater.inflate(
            R.layout.view___register___viewpager___phone,
            RelativeLayout(this),
            false
        )
        val verifyCodeView = layoutInflater.inflate(
            R.layout.view___register___viewpager___verify_code,
            RelativeLayout(this),
            false
        )
        val passwordView = layoutInflater.inflate(
            R.layout.view___register___viewpager___password,
            RelativeLayout(this),
            false
        )
        val viewList = ArrayList<View>()
        viewList.add(phoneView)
        viewList.add(verifyCodeView)
        viewList.add(passwordView)
        viewPagerAdapter = RegisterViewPagerAdapter(viewList, this)
        viewPager!!.adapter = viewPagerAdapter

        etUserName = phoneView.findViewById(R.id.et_username)
        etPhone = phoneView.findViewById(R.id.et_phone)
        btGetVerifyCode = phoneView.findViewById(R.id.bt_get_verify_code)
        tvPhoneTitle = verifyCodeView.findViewById(R.id.tv_phone)
        seVerifyCode = verifyCodeView.findViewById(R.id.se_verify_code)
        btVerify = verifyCodeView.findViewById(R.id.bt_verify)
        etPassword = passwordView.findViewById(R.id.et_password)
        btRegister = passwordView.findViewById(R.id.bt_register)

        waitDialog = CustomDialog(this, this, "请稍候...")
        presenter = RegisterPresenter(this)
    }

    override fun initListener() {
        btGetVerifyCode!!.setOnClickListener(this)
        btVerify!!.setOnClickListener(this)
        btRegister!!.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.bt_get_verify_code -> {
                presenter!!.requestSMSCode(this, this, etUserName, etPhone)
            }
            R.id.bt_verify -> {
                presenter!!.checkVerifyCode(etPhone!!, seVerifyCode!!, verifyCodeStamp!!)
            }
            R.id.bt_register -> {
                presenter!!.register(this, etUserName, etPhone, etPassword)
            }
        }
    }

    override fun showWaitDialog() {
        runOnUiThread {
            if (!waitDialog!!.isShowing) {
                waitDialog!!.show()
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

    override fun showSnackBar(msg: String) {
        runOnUiThread {
            Snackbar.make(imgBack!!, msg, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun setCodeStamp(stamp: String) {
        this.verifyCodeStamp = stamp
    }

    override fun getPhone(): String {
        return etPhone!!.text.toString().trim()
    }

    override fun setViewPagerCurrentItem(index: Int) {
        runOnUiThread {
            viewPager!!.currentItem = index
        }
    }

    override fun requestVerifyCodeSuccess() {
        runOnUiThread {
            setViewPagerCurrentItem(1)
            tvPhoneTitle!!.text = String.format(
                resources.getString(R.string.RegisterActivityPhoneTitle),
                "验证码已经发送到${StringUtil.phoneToStar(etPhone!!.text.toString().trim())}"
            )
        }
    }

    override fun requestVerifyCodeFailured() {

    }

    override fun checkVerifyCodeSuccess() {
        runOnUiThread {
            setViewPagerCurrentItem(2)
        }
    }

    override fun registerSuccess() {
        val intent = Intent(this, IndexActivity::class.java)
        startActivity(intent)
    }
}
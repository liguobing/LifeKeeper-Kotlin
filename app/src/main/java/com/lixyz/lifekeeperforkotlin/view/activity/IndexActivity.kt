package com.lixyz.lifekeeperforkotlin.view.activity

import android.Manifest
import android.animation.ValueAnimator.RESTART
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
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
import com.lixyz.lifekeeperforkotlin.presenter.IndexPresenter
import com.lixyz.lifekeeperforkotlin.service.GuardService
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.ServiceRunManager
import com.lixyz.lifekeeperforkotlin.utils.StatusBarUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.CustomWeatherView
import com.lixyz.lifekeeperforkotlin.worker.WakeWorker
import com.lixyz.moudletest.MoudleMainActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class IndexActivity : BaseActivity(), View.OnClickListener, IIndexView, AMapLocationListener {

    /**
     * 权限请求码
     */
    private val permissionRequestCode = 1000

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * Presenter
     */
    private var presenter: IndexPresenter? = null

    /**
     * 背景天气
     */
    var background: CustomWeatherView? = null

    /**
     * 计划布局
     */
    private var layoutPlan: RelativeLayout? = null

    /**
     * 账本布局
     */
    private var layoutAccount: RelativeLayout? = null

    /**
     * 今日计划总数
     */
    private var tvDailyPlanCount: TextView? = null

    /**
     * 当月计划总数
     */
    private var tvMonthlyPlanCount: TextView? = null


    /**
     * 当月收入总额
     */
    private var tvIncomeMoneyCount: TextView? = null

    /**
     * 当月支出总额
     */
    private var tvExpendMoneyCount: TextView? = null

    /**
     * 网盘布局
     */
    private var layoutNetDisk: RelativeLayout? = null

    /**
     * 网盘文件数
     */
    private var tvNetDiskFileCount: TextView? = null

    /**
     * 用户头像
     */
    private var imgUserIcon: CircleImageView? = null

    /**
     * 用户名
     */
    private var tvUserName: TextView? = null

    /**
     * 登录链接
     */
    private var tvLoginLink: TextView? = null

    /**
     * 注册链接
     */
    private var tvRegisterLink: TextView? = null

    private var isLogin: Boolean = false

    private var locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarTransparent(this)
        setContentView(R.layout.activity___index)
        initWidget()

        checkLocationPermission()
    }

    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        presenter!!.checkLogin(this)

        presenter!!.getPlanOverview(this)
        presenter!!.getAccountOverview(this)
        presenter!!.getNetDiskOverview(this)


        //注册推送前台服务
        val userId = getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
        if (userId != null) {
            val instance = ServiceRunManager.getInstance()
            val running = instance.isServiceRunning(this)
            if (!running) {
                val guardService = Intent(this, GuardService::class.java)
                startForegroundService(guardService)
                PushManager.getInstance().turnOnPush(this)
            }
        } else {
            PushManager.getInstance().turnOffPush(this)
            val guardService = Intent(this, GuardService::class.java)
            stopService(guardService)
        }
    }

    private fun checkLocationPermission() {
        val notGetPermissionList = ArrayList<String>()
        for (permission in locationPermissions)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                notGetPermissionList.add(permission)
            }
        if (notGetPermissionList.size > 0) {
            val bottomDialog = Dialog(this, R.style.BottomDialog)
            val contentView: View =
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.view___index___request_permission,
                        RelativeLayout(this),
                        false
                    )
            val cancel = contentView.findViewById<Button>(R.id.bt_cancel_request)
            val startRequest = contentView.findViewById<Button>(R.id.bt_start_request)
            cancel.setOnClickListener {
                bottomDialog.dismiss()
            }
            startRequest.setOnClickListener {
                bottomDialog.dismiss()
                val arr =
                    arrayOfNulls<String>(notGetPermissionList.size)
                for (i in notGetPermissionList.indices) {
                    arr[i] = notGetPermissionList[i]
                }
                requestPermissions(arr, permissionRequestCode)
            }
            bottomDialog.setContentView(contentView)
            val layoutParams = contentView.layoutParams
            layoutParams.width = resources.displayMetrics.widthPixels
            contentView.layoutParams = layoutParams
            bottomDialog.window!!.setGravity(Gravity.BOTTOM)
            bottomDialog.window!!.setWindowAnimations(R.style.BottomDialog_Animation)
            bottomDialog.show()
        }else{
            startLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var result = true
        for (element in grantResults) {
            result = result and (element == PackageManager.PERMISSION_GRANTED)
        }
        if (result) {
            startLocation()
            Snackbar.make(tvDailyPlanCount!!, "正在获取当地天气状况，稍候会在背景显示", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(tvDailyPlanCount!!, "没有位置权限，无法获取天气", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter!!.shutDownThreadPool()
        background!!.stop()
        background!!.destory()

        val wakeWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<WakeWorker>()
                .build()
        WorkManager
            .getInstance(this)
            .enqueue(wakeWorkRequest)
    }

    private var planContentWrapper: LinearLayout? = null
    private var planLoadingWrapper: LinearLayout? = null
    private var imgPlanLoading: ImageView? = null
    private var planLoadingAnimation: RotateAnimation? = null
    private var accountContentWrapper: LinearLayout? = null
    private var accountLoadingWrapper: LinearLayout? = null
    private var imgAccountLoading: ImageView? = null
    private var accountLoadingAnimation: RotateAnimation? = null
    private var netDiskContentWrapper: LinearLayout? = null
    private var netDiskLoadingWrapper: LinearLayout? = null
    private var imgNetDiskLoading: ImageView? = null
    private var netDiskLoadingAnimation: RotateAnimation? = null

    override fun initWidget() {
        waitDialog = CustomDialog(this, this, "数据载入中...")
        presenter = IndexPresenter(this)

        background = findViewById(R.id.weather_bg)

        layoutPlan = findViewById(R.id.plan_layout)
        planLoadingWrapper = findViewById(R.id.ll_plan_loading_wrapper)
        planContentWrapper = findViewById(R.id.ll_plan_content_wrapper)
        imgPlanLoading = findViewById(R.id.img_plan_loading)
        planLoadingAnimation =
            AnimationUtils.loadAnimation(this, R.anim.index___loading_icon_anim) as RotateAnimation?
        planLoadingAnimation!!.duration = 1000
        planLoadingAnimation!!.repeatMode = RESTART
        imgPlanLoading!!.startAnimation(planLoadingAnimation)
        tvDailyPlanCount = findViewById(R.id.tv_daily_plan)
        tvMonthlyPlanCount = findViewById(R.id.tv_monthly_plan)

        layoutAccount = findViewById(R.id.account_layout)
        accountLoadingWrapper = findViewById(R.id.ll_account_loading_wrapper)
        accountContentWrapper = findViewById(R.id.ll_account_content_wrapper)
        imgAccountLoading = findViewById(R.id.img_account_loading)
        accountLoadingAnimation =
            AnimationUtils.loadAnimation(this, R.anim.index___loading_icon_anim) as RotateAnimation?
        accountLoadingAnimation!!.duration = 2000
        accountLoadingAnimation!!.repeatMode = RESTART
        imgAccountLoading!!.startAnimation(accountLoadingAnimation)
        tvIncomeMoneyCount = findViewById(R.id.tv_income_money)
        tvExpendMoneyCount = findViewById(R.id.tv_expend_money)

        layoutNetDisk = findViewById(R.id.net_disk_layout)
        netDiskLoadingWrapper = findViewById(R.id.ll_net_disk_loading_wrapper)
        netDiskContentWrapper = findViewById(R.id.ll_net_disk_content_wrapper)
        imgNetDiskLoading = findViewById(R.id.img_net_disk_loading)
        netDiskLoadingAnimation =
            AnimationUtils.loadAnimation(this, R.anim.index___loading_icon_anim) as RotateAnimation?
        netDiskLoadingAnimation!!.duration = 3000
        netDiskLoadingAnimation!!.repeatMode = RESTART
        imgNetDiskLoading!!.startAnimation(netDiskLoadingAnimation)
        tvNetDiskFileCount = findViewById(R.id.tv_net_disk_file_count)

        imgUserIcon = findViewById(R.id.image_user_icon)
        tvUserName = findViewById(R.id.tv_user_name)
        tvLoginLink = findViewById(R.id.tv_login_link)
        tvRegisterLink = findViewById(R.id.tv_register_link)
    }

    override fun initListener() {
        layoutPlan!!.setOnClickListener(this)
        layoutAccount!!.setOnClickListener(this)
        layoutNetDisk!!.setOnClickListener(this)
        tvLoginLink!!.setOnClickListener(this)
        tvRegisterLink!!.setOnClickListener(this)
        tvUserName!!.setOnClickListener(this)
        imgUserIcon!!.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            //计划 Card
            R.id.plan_layout, R.id.tv_daily_plan, R.id.tv_monthly_plan -> {
                if (isLogin) {
                    val planIntent = Intent(this, PlanWebActivity::class.java)
                    startActivityForResult(planIntent, 1)
                    planLoadingWrapper!!.visibility = View.VISIBLE
                    planContentWrapper!!.visibility = View.GONE
                } else {
                    val registerIntent = Intent(this, LoginActivity::class.java)
                    startActivity(registerIntent)
                }
            }
            //账本 Card
            R.id.account_layout, R.id.tv_income_money, R.id.tv_expend_money -> {
                if (isLogin) {
                    val billIntent = Intent(this, BillListActivity::class.java)
                    startActivityForResult(billIntent, 2)
                    accountLoadingWrapper!!.visibility = View.VISIBLE
                    accountContentWrapper!!.visibility = View.GONE
                } else {
                    val registerIntent = Intent(this, LoginActivity::class.java)
                    startActivity(registerIntent)
                }
            }
            //网盘 Card
            R.id.net_disk_layout, R.id.tv_net_disk_file_count -> {
                if (isLogin) {
                    val netDiskIntent = Intent(this, NetDiskActivity::class.java)
                    startActivityForResult(netDiskIntent, 3)
                    netDiskLoadingWrapper!!.visibility = View.VISIBLE
                    netDiskContentWrapper!!.visibility = View.GONE
                } else {
                    val registerIntent = Intent(this, LoginActivity::class.java)
                    startActivity(registerIntent)
                }
            }
            //用户头像和用户名
            R.id.image_user_icon, R.id.tv_user_name -> {
//                val userIntent = Intent(this, UserCenterActivity::class.java)
//                val options = ActivityOptionsCompat
//                    .makeSceneTransitionAnimation(
//                        this,
//                        imgUserIcon!!, "UserIcon"
//                    )
//                startActivity(userIntent, options.toBundle())
                val intent = Intent(this, MoudleMainActivity::class.java)
                startActivity(intent)
            }
            //注册链接
            R.id.tv_register_link -> {
                val registerIntent = Intent(this, RegisterActivity::class.java)
                startActivity(registerIntent)
            }
            R.id.tv_login_link -> {
                val registerIntent = Intent(this, LoginActivity::class.java)
                startActivity(registerIntent)
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                presenter!!.getPlanOverview(this)
            }
            2 -> {
                presenter!!.getAccountOverview(this)
            }
            3 -> {
                presenter!!.getNetDiskOverview(this)
            }
            else -> {

            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val params = layoutNetDisk!!.layoutParams
            params.height = layoutAccount!!.measuredHeight
            layoutNetDisk!!.layoutParams = params
        }
    }


    override fun showSnackBar(message: String?) {
        runOnUiThread {
            Snackbar.make(tvDailyPlanCount!!, message!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun updateLogin(isLogin: Boolean, userBean: UserBean?) {
        this.isLogin = isLogin
        if (isLogin) {
            tvLoginLink!!.visibility = View.GONE
            tvRegisterLink!!.visibility = View.GONE
            tvUserName!!.visibility = View.VISIBLE
            imgUserIcon!!.visibility = View.VISIBLE
            tvUserName!!.text = userBean!!.userName
            if (userBean.userIconUrl!!.startsWith("http://") || userBean.userIconUrl!!.startsWith(
                    "https://"
                )
            ) {
                Glide.with(imgUserIcon!!)
                    .load(userBean.userIconUrl).error(
                        Glide.with(imgUserIcon!!)
                            .load(R.drawable.login___viewpager___login___user_icon)
                    ).into(imgUserIcon!!)
            } else {
                GlideApp.with(imgUserIcon!!)
                    .load(Constant.ADDRESS + "/LifeKeeper/resource/LifeKeeperUserIcon/" + userBean.userIconUrl)
                    .apply(
                        RequestOptions()
                            .transform(CenterCrop(), RoundedCorners(20))
                    )
                    .into(imgUserIcon!!)
            }
        } else {
            tvLoginLink!!.visibility = View.VISIBLE
            tvRegisterLink!!.visibility = View.VISIBLE
            tvUserName!!.visibility = View.GONE
            imgUserIcon!!.visibility = View.GONE
        }
    }

    override fun updateWeather(weatherCode: Int) {
        runOnUiThread {
            background!!.weatherCode = weatherCode
            background!!.start()
        }
    }

    override fun resetPlanContent(planCountOfDay: Int, planCountOfMonth: Int) {
        runOnUiThread {
            planLoadingWrapper!!.visibility = View.GONE
            planContentWrapper!!.visibility = View.VISIBLE
            tvDailyPlanCount!!.text = java.lang.String.format(
                Locale.CHINA,
                "今天有 %d 项计划",
                planCountOfDay
            )
            tvMonthlyPlanCount!!.text = java.lang.String.format(
                Locale.CHINA,
                "本月有 %d 项计划",
                planCountOfMonth
            )
        }
    }

    override fun resetAccountContent(income: Double, expend: Double) {
        runOnUiThread {
            accountLoadingWrapper!!.visibility = View.GONE
            accountContentWrapper!!.visibility = View.VISIBLE
            tvIncomeMoneyCount!!.text =
                getString(
                    R.string.cardViewAccountTitleIncome,
                    income
                )

            tvExpendMoneyCount!!.text =
                getString(
                    R.string.cardViewAccountTitleExpend,
                    expend
                )
        }
    }

    override fun resetNetDiskContent(fileCount: Int) {
        runOnUiThread {
            netDiskLoadingWrapper!!.visibility = View.GONE
            netDiskContentWrapper!!.visibility = View.VISIBLE
            tvNetDiskFileCount!!.text = String.format(
                resources.getString(R.string.CardViewNetFileCount),
                fileCount
            )
        }
    }

    private var mLocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null


    /**
     * 开始定位
     */
    private fun startLocation() {
        try {
            Thread {
                AMapLocationClient.updatePrivacyShow(this, true, true)
                AMapLocationClient.updatePrivacyAgree(this, true)
                mLocationClient = AMapLocationClient(this)
                mLocationOption = AMapLocationClientOption()
                mLocationClient!!.setLocationListener(this)
                //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
                mLocationOption!!.interval = 2000
                mLocationOption!!.locationPurpose =
                    AMapLocationClientOption.AMapLocationPurpose.SignIn
                mLocationOption!!.locationMode =
                    AMapLocationClientOption.AMapLocationMode.Battery_Saving
                if (null != mLocationClient) {
                    mLocationClient!!.setLocationOption(mLocationOption)
                    mLocationClient!!.stopLocation()
                    mLocationClient!!.startLocation()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                presenter!!.updateWeather( aMapLocation.latitude, aMapLocation.longitude)
            } else {
                runOnUiThread {
                    Snackbar.make(
                        tvDailyPlanCount!!,
                        "定位出错(ErrorCode = ${aMapLocation.errorCode})，获取天气失败，请稍候重试",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            runOnUiThread {
                Snackbar.make(
                    tvDailyPlanCount!!,
                    "定位出错，获取天气失败，请稍候重试",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}
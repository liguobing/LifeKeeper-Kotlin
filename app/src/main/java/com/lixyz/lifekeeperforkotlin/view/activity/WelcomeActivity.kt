package com.lixyz.lifekeeperforkotlin.view.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.viewpager2.widget.ViewPager2
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationListener
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.WelcomeViewPager2Adapter
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.presenter.WelcomePresenter


class WelcomeActivity : BaseActivity(), View.OnClickListener,
    IWelcomeView, AMapLocationListener {

    /**
     * 拒绝了权限的时候，显示的提示
     */
    private lateinit var rlPermissionWarning: RelativeLayout

    /**
     * 所需权限
     */
    private var allPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.FOREGROUND_SERVICE
    )

    /**
     * 权限请求码
     */
    private val permissionRequestCode = 1000

    /**
     * 请求权限的 Dialog
     */
    private var requestPermissionDialog: AlertDialog? = null

    /**
     * 设置页面的请求码
     */
    private val requestSetting = 2000

    /**
     * 未获取的权限列表
     */
    private val notGetPermissionList: ArrayList<String> = ArrayList()

    /**
     * Presenter
     */
    private var presenter: WelcomePresenter? = null

    /**
     * 倒数按钮
     */
    private var btTimeDown: Button? = null

    /**
     * 倒计时是否进行
     */
    private var timeDowning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 通过 SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 将布局设置为全屏显示
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        decorView.systemUiVisibility = option
        // StatusBar 设置为透明
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity___welcome)
        initWidget()
        com.igexin.sdk.PushManager.getInstance().initialize(this)
    }

    override fun onStart() {
        super.onStart()
        //检查权限
        notGetPermissionList.clear()
//        for (permission in allPermissions) {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    permission
//                ) == PackageManager.PERMISSION_DENIED
//            ) {
//                notGetPermissionList.add(permission)
//            }
//        }
        //如果存在未获取的权限，则弹出提示
        if (notGetPermissionList.size > 0) {
            val requestPermissionView: View = layoutInflater.inflate(
                R.layout.view___welcome___request_permission,
                LinearLayout(this@WelcomeActivity)
            )
            val closeAlertDialog =
                requestPermissionView.findViewById<ImageView>(R.id.img_close_alert_dialog)
            val startRequestPermission =
                requestPermissionView.findViewById<Button>(R.id.bt_start_request_permission)
            closeAlertDialog.setOnClickListener(this)
            closeAlertDialog.setOnClickListener(this)
            startRequestPermission.setOnClickListener(this)
            requestPermissionDialog =
                AlertDialog.Builder(this).setView(requestPermissionView).setCancelable(false)
                    .show()
        }
    }

    private var isResume = false

    override fun onResume() {
        super.onResume()
        initListener()
        presenter!!.startTimeDown()
        isResume = true
    }

    override fun onPause() {
        super.onPause()
        isResume = false
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter!!.activityDestroy()
    }

    override fun initWidget() {
        presenter = WelcomePresenter(this)
        val viewpager: ViewPager2 = findViewById(R.id.vp)
        val adapter = WelcomeViewPager2Adapter()
        viewpager.adapter = adapter
        btTimeDown = findViewById(R.id.bt_time_down)
        rlPermissionWarning = findViewById(R.id.rl_permission_warning)
        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (timeDowning) {
                    presenter!!.stopTimeDown()
                    timeDowning = false
                }
            }
        })
    }

    override fun initListener() {
        btTimeDown!!.setOnClickListener(this)
        rlPermissionWarning.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bt_time_down -> {
                val intent = Intent(this@WelcomeActivity, IndexActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.img_close_alert_dialog -> {
                requestPermissionDialog!!.dismiss()
                if (rlPermissionWarning.visibility == View.GONE) {
                    rlPermissionWarning.visibility = View.VISIBLE
                }
            }
            R.id.bt_start_request_permission -> {
                requestPermissionDialog!!.dismiss()
                presenter!!.stopTimeDown()
                startRequestPermission()
            }
            R.id.rl_permission_warning -> {
                val intent = Intent()
                intent.data = Uri.fromParts("package", this.packageName, null)
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                startActivityForResult(intent, requestSetting)
            }
        }
    }

    private fun startRequestPermission() {
        val arr =
            arrayOfNulls<String>(notGetPermissionList.size)
        for (i in notGetPermissionList.indices) {
            arr[i] = notGetPermissionList[i]
        }
        requestPermissions(arr, permissionRequestCode)
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
            if (rlPermissionWarning.visibility == View.VISIBLE) {
                rlPermissionWarning.visibility = View.GONE
            }
        } else {
            if (rlPermissionWarning.visibility == View.GONE) {
                rlPermissionWarning.visibility = View.VISIBLE
            }
        }
    }

    override fun timeDown(number: Int) {
        runOnUiThread {
            btTimeDown!!.text = String.format(
                resources.getString(R.string.WelcomeActivityTimeDownButton),
                number
            )
        }
    }

    override fun startIndexActivity() {
        if (isResume) {
            runOnUiThread {
                val intent = Intent(this, IndexActivity::class.java)
                startActivity(intent)
            }
        }

    }

    override fun resetButton() {
        runOnUiThread {
            btTimeDown!!.text = "跳过"
        }
    }


    private var latitude = 0.0

    private var longitude = 0.0

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                latitude = aMapLocation.latitude
                longitude = aMapLocation.longitude
            }
        }
    }
}
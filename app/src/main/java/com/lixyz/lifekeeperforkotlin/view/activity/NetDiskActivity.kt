package com.lixyz.lifekeeperforkotlin.view.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.presenter.NetDiskPresenter
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.CustomUploadDialog
import kotlinx.coroutines.*
import java.lang.ref.WeakReference


class NetDiskActivity : BaseActivity(), View.OnClickListener, INetDiskView {

    /**
     * 权限请求码
     */
    private val permissionRequestCode = 1000

    /**
     * Presenter
     */
    private var presenter: NetDiskPresenter? = null

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * 上传 Dialog
     */
    private var uploadDialog: CustomUploadDialog? = null

    /**
     * Handler
     */
    private val handler: MyHandler = MyHandler(this)

    companion object {
        /**
         * 隐藏等待 Dialog
         */
        private const val HIDE_WAIT_DIALOG = 1000

        /**
         * 更新等待 Dialog
         */
        private const val UPDATE_WAIT_DIALOG = 2000

        /**
         * 显示 SnackBar
         */
        private const val SHOW_SNACK_BAR = 3000

        /**
         * 修改上传按钮状态
         */
        private const val UPDATE_UPLOAD_BUTTON_STATUS = 4000

        /**
         * 更新GridView
         */
        private const val UPDATE_GRID_VIEW = 5000

        /**
         * 隐藏上传 Dialog
         */
        private const val HIDE_UPLOAD_DIALOG = 6000

        /**
         * 更新上传 Dialog
         */
        private const val UPDATE_UPLOAD_DIALOG = 7000

        private const val IMAGE_REQUEST_CODE = 100001

        private const val PHONE_RECORD_REQUEST_CODE = 100002

        private const val VIDEO_REQUEST_CODE = 100003

        private const val ALL_FILE_PERMISSION_REQUEST_CODE = 100004
    }

    private var needRequestData = true

    private var externalPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // StatusBar 设置为透明
            window.statusBarColor = Color.parseColor("#4CE19F")
            setContentView(R.layout.activity___net_disk)
            initWidget()
            initListener()

            checkExternalPermission()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (needRequestData) {
            MainScope().launch(Dispatchers.IO) {
                val data = presenter!!.getNetDiskData(this@NetDiskActivity)
                withContext(Dispatchers.Main) {
                    if (data == null) {
                        Snackbar.make(tvImageCount!!, "出错啦，请稍候重试", Snackbar.LENGTH_SHORT).show()
                    } else {
                        tvImageCount!!.text = data.imageCount.toString()
                        tvImageCount!!.visibility = View.VISIBLE
                        imgImageLoading!!.clearAnimation()
                        imgImageLoading!!.visibility = View.GONE

                        tvVideoCount!!.text = data.videoCount.toString()
                        tvVideoCount!!.visibility = View.VISIBLE
                        imgVideoLoading!!.clearAnimation()
                        imgVideoLoading!!.visibility = View.GONE

                        tvRecordCount!!.text = data.recordCount.toString()
                        tvRecordCount!!.visibility = View.VISIBLE
                        imgRecordLoading!!.clearAnimation()
                        imgRecordLoading!!.visibility = View.GONE

                        needRequestData = false
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            presenter!!.deleteCacheFile(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkExternalPermission() {
        val notGetPermissionList = ArrayList<String>()
        for (permission in externalPermissions) if (ActivityCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            notGetPermissionList.add(permission)
        }
        if (notGetPermissionList.size > 0) {
            val bottomDialog = Dialog(this, R.style.BottomDialog)
            val contentView: View = LayoutInflater.from(this).inflate(
                R.layout.view___netdisk___request_permission, RelativeLayout(this), false
            )
            val cancel = contentView.findViewById<Button>(R.id.bt_cancel_request)
            val startRequest = contentView.findViewById<Button>(R.id.bt_start_request)
            cancel.setOnClickListener {
                bottomDialog.dismiss()
            }
            startRequest.setOnClickListener {
                bottomDialog.dismiss()
                val arr = arrayOfNulls<String>(notGetPermissionList.size)
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
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var result = true
        for (element in grantResults) {
            result = result and (element == PackageManager.PERMISSION_GRANTED)
        }
        if (result) {
            val isHasStoragePermission = Environment.isExternalStorageManager()
            if (!isHasStoragePermission) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, ALL_FILE_PERMISSION_REQUEST_CODE)
            }
        } else {
            Snackbar.make(tvImageCount!!, "您拒绝授予存储权限，无法备份文件", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            MainScope().launch(Dispatchers.IO) {
                val count = presenter!!.loadImageData(this@NetDiskActivity)
                withContext(Dispatchers.Main) {
                    tvImageCount!!.text = count.toString()
                    tvImageCount!!.visibility = View.VISIBLE
                    imgImageLoading!!.clearAnimation()
                    imgImageLoading!!.visibility = View.GONE
                }
            }
        }
        if (requestCode == PHONE_RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            MainScope().launch(Dispatchers.IO) {
                val count = presenter!!.loadPhoneRecordData(this@NetDiskActivity)
                withContext(Dispatchers.Main) {
                    tvRecordCount!!.text = count.toString()
                    tvRecordCount!!.visibility = View.VISIBLE
                    imgRecordLoading!!.clearAnimation()
                    imgRecordLoading!!.visibility = View.GONE
                }
            }
        }
        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            MainScope().launch(Dispatchers.IO) {
                val count = presenter!!.loadVideoData(this@NetDiskActivity)
                withContext(Dispatchers.Main) {
                    tvVideoCount!!.text = count.toString()
                    tvVideoCount!!.visibility = View.VISIBLE
                    imgVideoLoading!!.clearAnimation()
                    imgVideoLoading!!.visibility = View.GONE
                }
            }
        }

        if (requestCode == ALL_FILE_PERMISSION_REQUEST_CODE) {
            val isHasStoragePermission = Environment.isExternalStorageManager()
            if (!isHasStoragePermission) {
                showSnackBar("没有授予全部文件权限，无法享受完整的备份功能")
            }
        }
    }

    private var toolbar: Toolbar? = null
    private var animation: Animation? = null
    private var rlImageWrapper: RelativeLayout? = null
    private var imgImageLoading: ImageView? = null
    private var tvImageCount: TextView? = null
    private var rlRecordWrapper: RelativeLayout? = null
    private var imgRecordLoading: ImageView? = null
    private var tvRecordCount: TextView? = null
    private var rlVideoWrapper: RelativeLayout? = null
    private var imgVideoLoading: ImageView? = null
    private var tvVideoCount: TextView? = null


    override fun initWidget() {
        presenter = NetDiskPresenter(this)
        waitDialog = CustomDialog(this, this, "载入中")
        uploadDialog = CustomUploadDialog(this, this, "上传中")

        animation = AnimationUtils.loadAnimation(this, R.anim.net_disk___loading_icon_anim)

        toolbar = findViewById(R.id.toolbar)

        rlImageWrapper = findViewById(R.id.rl_image_wrapper)
        imgImageLoading = findViewById(R.id.img_image_loading)
        imgImageLoading!!.startAnimation(animation)
        tvImageCount = findViewById(R.id.tv_image_count)
        rlRecordWrapper = findViewById(R.id.rl_record_wrapper)
        imgRecordLoading = findViewById(R.id.img_record_loading)
        imgRecordLoading!!.startAnimation(animation)
        tvRecordCount = findViewById(R.id.tv_record_count)
        rlVideoWrapper = findViewById(R.id.rl_video_wrapper)
        imgVideoLoading = findViewById(R.id.img_video_loading)
        imgVideoLoading!!.startAnimation(animation)
        tvVideoCount = findViewById(R.id.tv_video_count)
    }

    override fun initListener() {
        rlImageWrapper!!.setOnClickListener(this)
        rlRecordWrapper!!.setOnClickListener(this)
        rlVideoWrapper!!.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rl_image_wrapper -> {
                val imageIntent = Intent(this, ImageCategoryActivity::class.java)
                startActivityForResult(imageIntent, IMAGE_REQUEST_CODE)
                imgImageLoading!!.visibility = View.VISIBLE
                imgImageLoading!!.startAnimation(animation)
                tvImageCount!!.visibility = View.GONE
            }
            R.id.rl_record_wrapper -> {
                val imageIntent = Intent(this, RecordActivity::class.java)
                startActivityForResult(imageIntent, PHONE_RECORD_REQUEST_CODE)
                imgRecordLoading!!.visibility = View.VISIBLE
                imgRecordLoading!!.startAnimation(animation)
                tvRecordCount!!.visibility = View.GONE
            }
            R.id.rl_video_wrapper -> {
                val imageIntent = Intent(this, VideoCategoryActivity::class.java)
                startActivityForResult(imageIntent, VIDEO_REQUEST_CODE)
                imgVideoLoading!!.visibility = View.VISIBLE
                imgVideoLoading!!.startAnimation(animation)
                tvVideoCount!!.visibility = View.GONE
            }
        }
    }

    override fun showWaitDialog() {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            if (!waitDialog!!.isShowing) {
                waitDialog!!.show()
            }
        } else {
            runOnUiThread {
                if (!waitDialog!!.isShowing) {
                    waitDialog!!.show()
                }
            }
        }
    }

    override fun hideWaitDialog() {
        handler.sendEmptyMessage(HIDE_WAIT_DIALOG)
    }

    override fun updateWaitDialog(message: String) {
        val msg = Message.obtain()
        msg.what = UPDATE_WAIT_DIALOG
        msg.obj = message
        handler.sendMessage(msg)
    }


    override fun showSnackBar(message: String) {
        runOnUiThread {
            Snackbar.make(toolbar!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun updateUploadButtonStatus(status: Int) {
        val msg = Message.obtain()
        msg.what = UPDATE_UPLOAD_BUTTON_STATUS
        msg.arg1 = status
        handler.sendMessage(msg)
    }

    override fun updateImageCardData(imageCount: Int) {
        runOnUiThread {
            tvImageCount!!.text = imageCount.toString()
            tvImageCount!!.visibility = View.VISIBLE
            imgImageLoading!!.clearAnimation()
            imgImageLoading!!.visibility = View.GONE
        }
    }

    override fun updatePhoneRecordCardData(phoneRecordCount: Int) {
        runOnUiThread {
            tvRecordCount!!.text = phoneRecordCount.toString()
            tvRecordCount!!.visibility = View.VISIBLE
            imgRecordLoading!!.clearAnimation()
            imgRecordLoading!!.visibility = View.GONE
        }
    }

    override fun updateVideoCardData(videoCount: Int) {
        runOnUiThread {
            tvVideoCount!!.text = videoCount.toString()
            tvVideoCount!!.visibility = View.VISIBLE
            imgVideoLoading!!.clearAnimation()
            imgVideoLoading!!.visibility = View.GONE

        }
    }


    class MyHandler(activity: NetDiskActivity) : Handler(Looper.getMainLooper()) {
        private val mActivity: WeakReference<NetDiskActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity: NetDiskActivity? = mActivity.get()
            when (msg.what) {
                HIDE_WAIT_DIALOG -> {
                    if (activity!!.waitDialog!!.isShowing) {
                        activity.waitDialog!!.dismiss()
                    }
                }
                UPDATE_WAIT_DIALOG -> {
                    if (activity!!.waitDialog!!.isShowing) {
                        activity.waitDialog!!.setMessage(msg.obj as String)
                    }
                }
                SHOW_SNACK_BAR -> {
                    Snackbar.make(
                        activity!!.toolbar!!, msg.obj as String, Snackbar.LENGTH_SHORT
                    ).show()
                }
                UPDATE_GRID_VIEW -> {

                }
                HIDE_UPLOAD_DIALOG -> {
                    if (activity!!.uploadDialog!!.isShowing) {
                        activity.uploadDialog!!.dismiss()
                    }
                }
                UPDATE_UPLOAD_DIALOG -> {
                    if (activity!!.uploadDialog!!.isShowing) {
                        activity.uploadDialog!!.updateDialog(msg.obj as String, msg.arg1)
                    }
                }
            }
        }
    }
}
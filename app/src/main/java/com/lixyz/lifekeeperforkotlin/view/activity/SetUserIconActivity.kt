package com.lixyz.lifekeeperforkotlin.view.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.presenter.SetUserIconPresenter
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.DragScaleClipImageView
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.ref.WeakReference


/**
 * @author LGB
 * 设置用户头像
 */
class SetUserIconActivity : BaseActivity(), View.OnClickListener, ISetUserIconView {
    /**
     * Toolbar
     */
    private var toolbar: Toolbar? = null

    /**
     * 保存头像按钮
     */
    private var saveUserIcon: ImageView? = null

    /**
     * 图像
     */
    private var image: DragScaleClipImageView? = null

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * presenter
     */
    private var presenter: SetUserIconPresenter? = null

    /**
     * Handler
     */
    private val handler: MyHandler = MyHandler(this)

    /**
     * 保存成功
     */
    private var saveBillSuccessful = false

    private var userIconUrl: String? = null

    companion object {
        /**
         * 保存成功 Handler 消息
         */
        private const val SAVE_SUCCESSFUL = 100

        /**
         * 隐藏等待 Dialog Handler 消息
         */
        private const val HIDE_WAIT_DIALOG = 200

        /**
         * 显示 SnackBar Handler 消息
         */
        private const val SHOW_SNACK_BAR = 300
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //弱化StatusBar
        val decorView: View = window.decorView
        val uiOptions: Int = View.SYSTEM_UI_FLAG_LOW_PROFILE
        decorView.systemUiVisibility = uiOptions
        window.statusBarColor = Color.parseColor("#1B82D1")
        setContentView(R.layout.activity___set_user_icon)
        initWidget()
        initListener()
        try {
            val bitmap =
                BitmapFactory.decodeStream(FileInputStream(File(intent.getStringExtra("imageUri")!!)))
            image!!.setBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun initWidget() {
        presenter = SetUserIconPresenter(this)
        waitDialog = CustomDialog(this, "请稍后...")
        toolbar = findViewById(R.id.toolbar)
        saveUserIcon = findViewById(R.id.img_save_user_icon)
        image = findViewById(R.id.image)
    }

    override fun initListener() {
        saveUserIcon!!.setOnClickListener(this)
        toolbar!!.setNavigationOnClickListener { finish() }
        waitDialog!!.setOnDismissListener { dialogInterface ->
            dialogInterface.dismiss()
            if (saveBillSuccessful) {
                val intent = Intent()
                intent.putExtra("UserIconUrl", userIconUrl)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.img_save_user_icon) {
            val imagePath: String = image!!.saveUserIcon()
            presenter!!.uploadUserIcon(this, imagePath)
        }
    }


    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun hideWaitDialog() {
        handler.sendEmptyMessage(HIDE_WAIT_DIALOG)
    }

    override fun showSnackBar(message: String) {
        val msg = Message()
        msg.what = SHOW_SNACK_BAR
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun saveSuccessful(userIconUrl: String) {
        this.userIconUrl = userIconUrl
        handler.sendEmptyMessage(SAVE_SUCCESSFUL)
    }

    class MyHandler(activity: SetUserIconActivity) : Handler() {
        var mActivity: WeakReference<SetUserIconActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val setUserIconActivity = mActivity.get()
            when (msg.what) {
                SAVE_SUCCESSFUL -> {
                    setUserIconActivity!!.saveBillSuccessful = true
                    setUserIconActivity.waitDialog!!.setSuccessful()
                }
                HIDE_WAIT_DIALOG -> {
                    if (setUserIconActivity!!.waitDialog!!.isShowing) {
                        setUserIconActivity.waitDialog!!.dismiss()
                    }
                }
                SHOW_SNACK_BAR -> {
                    Snackbar.make(
                        setUserIconActivity!!.saveUserIcon!!,
                        msg.obj as String,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
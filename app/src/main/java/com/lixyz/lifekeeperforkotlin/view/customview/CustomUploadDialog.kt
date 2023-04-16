package com.lixyz.lifekeeperforkotlin.view.customview

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.daimajia.numberprogressbar.NumberProgressBar
import com.lixyz.lifekeeperforkotlin.R


/**
 * @author LGB
 * 自定义 Dialog
 */
class CustomUploadDialog : Dialog {
    /**
     * 显示时候的图片
     */
    private var imgPhone: ImageView? = null
    private var imgFile: ImageView? = null
    private var imgCloud: ImageView? = null

    /**
     * 进度
     */
    private var progress: NumberProgressBar? = null

    /**
     * 展示问题提示信息
     */
    private var tvMessage: TextView? = null

    /**
     * 提示文字信息
     */
    private var message: String

    /**
     * Context
     */
    private var mContext: Context

    /**
     * 动画资源
     */
    private var animator: Animator? = null

    /**
     * 是否可以通过取消按钮隐藏
     */
    private var canCancelable = false

    /**
     * 是否可以通过点击外围区域隐藏
     */
    private var cancel = false
    private var activity: Activity? = null

    constructor(context: Context, message: String) : super(context) {
        this.message = message
        this.mContext = context
    }

    constructor(context: Context, activity: Activity?, message: String) : super(context) {
        this.mContext = context
        this.activity = activity
        this.message = message
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view___custom_upload_dialog)
        tvMessage = findViewById(R.id.tv_message)
        imgPhone = findViewById(R.id.img_phone)
        imgFile = findViewById(R.id.img_file)
        imgCloud = findViewById(R.id.img_cloud)
        progress = findViewById(R.id.progress)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val animator = ValueAnimator.ofInt(imgPhone!!.left, imgCloud!!.right)
            animator.duration = 1000
            animator.repeatCount = -1
            animator.repeatMode = ValueAnimator.RESTART
            animator.addUpdateListener {
                imgFile!!.left = (it.animatedValue as Int)
            }
            animator.start()
        }
    }


    fun setMessage(message: String) {
        this.message = message
        activity!!.runOnUiThread {
            tvMessage!!.text = message
            val window: Window? = this.window
            val attributes: WindowManager.LayoutParams = window!!.attributes
            attributes.width = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = attributes
        }
    }


    fun updateDialog(message: String, progress: Int) {
        this.message = message
        this.progress!!.progress = progress
        activity!!.runOnUiThread {
            tvMessage!!.text = message
            val window: Window? = this.window
            val attributes: WindowManager.LayoutParams = window!!.attributes
            attributes.width = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = attributes
        }
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
        this.cancel = cancel
    }

    override fun setCancelable(flag: Boolean) {
        super.setCancelable(flag)
        canCancelable = flag
    }

    override fun show() {
        super.show()
        setCancelable(canCancelable)
        setCanceledOnTouchOutside(cancel)
        val window: Window? = this.window
        window!!.setBackgroundDrawableResource(R.drawable.custom_dialog___background)
    }

    override fun dismiss() {
        super.dismiss()
        if (animator != null && animator!!.isRunning) {
            animator!!.cancel()
        }
    }
}
package com.lixyz.lifekeeperforkotlin.view.customview

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.lixyz.lifekeeperforkotlin.R


/**
 * @author LGB
 * 自定义 Dialog
 */
class CustomDialog : Dialog {
    /**
     * 显示时候的图片
     */
    private var imgShowing: ImageView? = null

    /**
     * 完成时下面的图片，通过移动上面的图片来造成动画效果
     */
    private var imgSuccessfulBottom: ImageView? = null

    /**
     * 完成时上面的图片，通过移动该图片来造成动画效果
     */
    private var imgSuccessfulTop: ImageView? = null

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
        setContentView(R.layout.view___custom_dialog)
        tvMessage = findViewById(R.id.tv_message)
        imgShowing = findViewById(R.id.img_showing)
        imgSuccessfulBottom = findViewById(R.id.image_successful_bottom)
        imgSuccessfulTop = findViewById(R.id.image_successful_top)
        tvMessage!!.text = message
        animator =
            AnimatorInflater.loadAnimator(context, R.animator.progress_dialog_animation_default)
        animator!!.setTarget(imgShowing)
    }

    /**
     * 设置为已完成
     */
    fun setSuccessful() {
        imgShowing!!.clearAnimation()
        tvMessage!!.text = "已完成"
        imgShowing!!.visibility = View.INVISIBLE
        imgSuccessfulBottom!!.visibility = VISIBLE
        imgSuccessfulTop!!.visibility = VISIBLE
        val animator = ValueAnimator.ofFloat(0F, imgShowing!!.width.toFloat())
        animator.duration = 1500
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = 0
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue
            imgSuccessfulTop!!.translationX = value as Float
            if (value == imgShowing!!.width.toFloat()) {
                dismiss()
            }
        }
        animator.start()
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
        animator!!.start()
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
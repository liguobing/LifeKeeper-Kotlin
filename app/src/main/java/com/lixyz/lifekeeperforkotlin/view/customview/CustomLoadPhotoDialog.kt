package com.lixyz.lifekeeperforkotlin.view.customview

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.lixyz.lifekeeperforkotlin.R

class CustomLoadPhotoDialog : Dialog {

    private var progressView: CircleProgress? = null

    /**
     * Context
     */
    private var mContext: Context


    /**
     * 是否可以通过取消按钮隐藏
     */
    private var canCancelable = false

    /**
     * 是否可以通过点击外围区域隐藏
     */
    private var cancel = false
    private var activity: Activity? = null

    constructor(context: Context) : super(context) {
        this.mContext = context
    }

    constructor(context: Context, activity: Activity?) : super(context) {
        this.mContext = context
        this.activity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view___custom_load_photo_dialog)
        progressView = findViewById(R.id.progress)

    }


    fun setProgress(progress: Float) {
        progressView!!.progress = progress
        progressView!!.invalidate()
        activity!!.runOnUiThread {
            this.progressView!!.progress = progress
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
}
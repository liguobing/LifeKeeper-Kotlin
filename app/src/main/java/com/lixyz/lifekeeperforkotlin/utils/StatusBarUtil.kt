package com.lixyz.lifekeeperforkotlin.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View

class StatusBarUtil {
    companion object {
        fun getStatusBarHeight(context: Context): Float {
            return context.resources.getDimension(
                context.resources.getIdentifier(
                    "status_bar_height",
                    "dimen",
                    "android"
                )
            )
        }

        fun setStatusBarTransparent(activity: Activity){
            val decorView = activity.window.decorView
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            decorView.systemUiVisibility = option
            // StatusBar 设置为透明
            activity.window.statusBarColor = Color.TRANSPARENT
        }
    }
}
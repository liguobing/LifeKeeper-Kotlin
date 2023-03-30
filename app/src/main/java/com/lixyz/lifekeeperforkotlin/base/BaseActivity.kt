package com.lixyz.lifekeeperforkotlin.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent

abstract class BaseActivity : Activity() {
    private val showClassName = true
    private val showLifeCycle = false

    companion object {
        const val TAG = "TTT"
    }


    //初始化组件
    abstract fun initWidget()

    //为组件设置监听器
    abstract fun initListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        PushAgent.getInstance(this).onAppStart();
        if (showClassName) {
            Log.d(
                TAG,
                "===============" + javaClass.simpleName + "==============="
            )
        }
        if (showLifeCycle) {
            Log.d(TAG, "【" + javaClass.simpleName + "】〖onCreate - 创建〗")
        }
    }

    override fun onStart() {
        super.onStart()
        if (showLifeCycle) {
            Log.d(TAG, "【" + javaClass.simpleName + "】〖onStart〗")
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (showLifeCycle) {
            Log.d(TAG, "【" + javaClass.simpleName + "】〖onRestart〗")
        }
    }

    override fun onResume() {
        super.onResume()
        if (showLifeCycle) {
            Log.d(TAG, "【" + javaClass.simpleName + "】〖onResume〗")
        }
    }

    override fun onPause() {
        super.onPause()
        if (showLifeCycle) {
            Log.d(TAG, "【" + javaClass.simpleName + "】〖onPause〗")
        }
    }

    override fun onStop() {
        super.onStop()
        if (showLifeCycle) {
            Log.d(TAG, "【" + javaClass.simpleName + "】〖onStop〗")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (showLifeCycle) {
            Log.d(TAG, "【" + javaClass.simpleName + "】〖onDestroy〗")
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (showLifeCycle) {
            Log.d(TAG, "【" + javaClass.simpleName + "】〖onActivityResult〗")
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }
}
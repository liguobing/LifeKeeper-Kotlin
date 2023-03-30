package com.lixyz.lifekeeperforkotlin

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class ApplicationLifecycleObserver(private var context: Context) : LifecycleObserver {


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        Toast.makeText(context, "温馨提示:LifeKeeper 此时处于后台运行中", Toast.LENGTH_SHORT).show()
    }
}
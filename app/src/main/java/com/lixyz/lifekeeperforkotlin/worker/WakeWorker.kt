package com.lixyz.lifekeeperforkotlin.worker

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lixyz.lifekeeperforkotlin.service.GuardService
import com.lixyz.lifekeeperforkotlin.utils.ServiceRunManager

class WakeWorker (appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val instance = ServiceRunManager.getInstance()
        val running = instance.isServiceRunning(applicationContext)
        if (!running) {
            val guardService = Intent(applicationContext, GuardService::class.java)
            applicationContext.startForegroundService(guardService)
        }
        return Result.success()
    }
}
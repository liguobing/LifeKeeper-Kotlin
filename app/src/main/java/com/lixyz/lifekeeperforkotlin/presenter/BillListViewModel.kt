package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.BillListActivityOverview
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.model.BillListModel
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class BillListViewModel : ViewModel() {

    private val model = BillListModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("账单列表线程池"))

    var billOverviewLiveData: MutableLiveData<BillListActivityOverview>? = null
    var waitDialogLiveData: MutableLiveData<Boolean>? = null
    var userIdLiveData: MutableLiveData<String>? = null
    var deleteLiveData: MutableLiveData<Boolean>? = null
    var snackBarLiveData: MutableLiveData<String>? = null

    init {
        billOverviewLiveData = MutableLiveData()
        waitDialogLiveData = MutableLiveData()
        userIdLiveData = MutableLiveData()
        deleteLiveData = MutableLiveData()
        snackBarLiveData = MutableLiveData()
    }


    fun getBillOverviewData(context: Context, year: Int, month: Int) {
        waitDialogLiveData!!.value = true
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
                    .getString("UserId", null)
            val billData = model.getBillData(userId!!, year, month)
            billOverviewLiveData!!.postValue(billData)
            waitDialogLiveData!!.postValue(false)
            userIdLiveData!!.postValue(userId)
        }
    }

    fun deleteBill(
        billList: ArrayList<BillBean>,
        position: Int,
        userId: String,
        year: Int,
        month: Int
    ) {
        waitDialogLiveData!!.value = true
        threadPool.execute {
            val result = model.deleteBill(billList[position], userId)
            if (result) {
                val billData = model.getBillData(userId, year, month)
                billOverviewLiveData!!.postValue(billData)
                waitDialogLiveData!!.postValue(false)
                snackBarLiveData!!.postValue("删除成功")
            } else {
                deleteLiveData!!.postValue(false)
                snackBarLiveData!!.postValue("删除失败，请检查后重试")
            }
        }
    }

    /**
     * 关闭线程池
     */
    fun shutdownThreadPool() {
        if (!threadPool.isShutdown) {
            threadPool.shutdown()
        }
    }
}
package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.PieEntry
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.bean.billchart.BillChartLineDataBean
import com.lixyz.lifekeeperforkotlin.bean.billchart.BillChartOverViewBean
import com.lixyz.lifekeeperforkotlin.model.BillChartModel
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class BillChartViewModel : ViewModel() {

    var waitDialogLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var snackBarLiveData: MutableLiveData<String> = MutableLiveData()
    var overviewLiveData: MutableLiveData<BillChartOverViewBean> = MutableLiveData()
    var pieChartLiveData: MutableLiveData<ArrayList<PieEntry>> = MutableLiveData()
    var pieChartBillListLiveData: MutableLiveData<ArrayList<BillBean>> = MutableLiveData()
    var lineChartLiveData: MutableLiveData<BillChartLineDataBean> = MutableLiveData()

    private val model = BillChartModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("账单图表线程池"))

    private var incomePieChartEntryList: ArrayList<PieEntry>? = null
    private var expendPieChartEntryList: ArrayList<PieEntry>? = null
    private var incomeLineChartEntryList: ArrayList<Float>? = null
    private var expendLineChartEntryList: ArrayList<Float>? = null

    fun getChartData(context: Context, year: Int, month: Int) {
        waitDialogLiveData.value = true
        threadPool.execute {
            val billChartData = model.getBillChartData(context, year, month)
            val overview = BillChartOverViewBean()
            overview.income = billChartData!!.income
            overview.expend = billChartData.expend
            overviewLiveData.postValue(overview)
            val incomeCategoryData = billChartData.incomeCategoryData
            incomePieChartEntryList = ArrayList()
            incomeCategoryData!!.forEachIndexed { _, billMoneyCountForCategoryGroup ->
                val entry = PieEntry(
                    billMoneyCountForCategoryGroup.moneyCount,
                    billMoneyCountForCategoryGroup.billCategory
                )
                incomePieChartEntryList!!.add(entry)
            }

            val expendCategoryData = billChartData.expendCategoryData
            expendPieChartEntryList = ArrayList()
            expendCategoryData!!.forEachIndexed { _, billMoneyCountForCategoryGroup ->
                val entry = PieEntry(
                    billMoneyCountForCategoryGroup!!.moneyCount,
                    billMoneyCountForCategoryGroup.billCategory
                )
                expendPieChartEntryList!!.add(entry)
            }

            pieChartLiveData.postValue(incomePieChartEntryList)


            incomeLineChartEntryList = ArrayList()
            expendLineChartEntryList = ArrayList()
            val lineChartBean = billChartData.billMoneyCountForDays
            lineChartBean!!.forEachIndexed { _, bean ->
                incomeLineChartEntryList!!.add(bean.income.toFloat())
                expendLineChartEntryList!!.add(bean.expend.toFloat())
            }
            val bean = BillChartLineDataBean()
            bean.type = 1
            bean.incomeList = incomeLineChartEntryList
            bean.expendList = expendLineChartEntryList
            lineChartLiveData.postValue(bean)

            waitDialogLiveData.postValue(false)
        }
    }

    fun getPieItemList(context: Context, label: String, billProperty: Int, year: Int, month: Int) {
        waitDialogLiveData.value = true
        threadPool.execute {
            val billList = model.getBillsByCategory(context, label, billProperty, year, month)
            pieChartBillListLiveData.postValue(billList)
            waitDialogLiveData.postValue(false)
        }
    }

    fun getIncomePieEntryList() {
        pieChartLiveData.value = incomePieChartEntryList
    }

    fun getExpendPieEntryList() {
        pieChartLiveData.value = expendPieChartEntryList
    }

    fun getIncomeLineChartDataList() {
        val bean = BillChartLineDataBean()
        bean.type = 1
        bean.incomeList = incomeLineChartEntryList
        bean.expendList = expendLineChartEntryList
        lineChartLiveData.value = bean
    }

    fun getExpendLineChartDataList() {
        val bean = BillChartLineDataBean()
        bean.type = 2
        bean.incomeList = incomeLineChartEntryList
        bean.expendList = expendLineChartEntryList
        lineChartLiveData.value = bean
    }

    fun getAllLineChartDataList() {
        val bean = BillChartLineDataBean()
        bean.type = 3
        bean.incomeList = incomeLineChartEntryList
        bean.expendList = expendLineChartEntryList
        lineChartLiveData.value = bean
    }

    fun activityOnDestroy() {
        if (!threadPool.isShutdown) {
            threadPool.shutdown()
        }
    }
}
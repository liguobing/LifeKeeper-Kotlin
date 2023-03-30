package com.lixyz.lifekeeperforkotlin.view.activity

import ando.widget.pickerview.builder.TimePickerBuilder
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount
import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory
import com.lixyz.lifekeeperforkotlin.model.AddBillModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class AddBillViewModel : ViewModel() {
    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("添加账单线程池"))

    var bill: BillBean? = null

    private val model = AddBillModel()

    var billCategoryLiveData = MutableLiveData<ArrayList<BillCategory>>()
    var billAccountLiveData = MutableLiveData<ArrayList<BillAccount>>()
    var waitDialogLiveData = MutableLiveData<Boolean>()
    var snackBarLiveData = MutableLiveData<String>()
    var billImageStatusLiveData = MutableLiveData<String?>()
    var addAccountAndCategoryDialogLiveData = MutableLiveData<Boolean>()
    var dateLiveData = MutableLiveData<String>()
    var saveDialogLiveData = MutableLiveData<Boolean>()

    fun getBillCategoryAndAccount(context: Context, property: Int) {
        waitDialogLiveData.value = true
        threadPool.execute {
            val bean = model.getBillCategoryAndAccount(context)
            if (bean == null) {
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("出错啦，请稍候重试...")
            } else {
                billAccountLiveData.postValue(bean.billAccounts)
                if (property > 0) {
                    billCategoryLiveData.postValue(bean.incomeBillCategories)
                } else {
                    billCategoryLiveData.postValue(bean.expendBillCategories)
                }
                waitDialogLiveData.postValue(false)
            }
        }
    }

    fun createBill(billProperty: Int) {
        bill = BillBean()
        bill!!.objectId = StringUtil.getRandomString()
        bill!!.billId = StringUtil.getRandomString()
        bill!!.billProperty = billProperty
    }

    fun uploadBillImage(imagePath: String, context: Context) {
        threadPool.execute {
            val uploadResult = model.uploadBillImage(imagePath, bill!!.billId!!, context)
            if (uploadResult) {
                billImageStatusLiveData.postValue(imagePath)
            } else {
                billImageStatusLiveData.postValue(null)
            }
        }
    }

    fun activityOnDestroy() {
        if (!threadPool.isShutdown) {
            threadPool.shutdown()
        }
    }

    fun setBillDate(context: Context) {
        val datePicker = TimePickerBuilder(
            context
        ) { date, _ ->
            val calendar: Calendar = Calendar.getInstance()
            calendar.time = date
            val year: Int = calendar.get(Calendar.YEAR)
            val month: Int = calendar.get(Calendar.MONTH)
            val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            val billDate = StringUtil.milliToString(calendar.timeInMillis, true)!!
            bill!!.billDate = calendar.timeInMillis
            dateLiveData.postValue(billDate)
        }.setCancelColor(Color.BLACK).setSubmitColor(Color.BLACK).setContentTextSize(20)
            .setType(booleanArrayOf(true, true, true, false, false, false)).build()
        datePicker.show()
    }

    fun saveListViewItem(context: Context, text: Editable, listViewFlag: Int, billProperty: Int) {
        if (TextUtils.isEmpty(text)) {
            addAccountAndCategoryDialogLiveData.value = false
            snackBarLiveData.value = "内容不能为空"
            return
        }
        waitDialogLiveData.value = true
        threadPool.execute {
            try {
                if (listViewFlag > 0) {
                    val addResult = model.addCategory(
                        context,
                        text.toString().trim(),
                        billProperty
                    )
                    if (addResult.result) {
                        val bean = model.getBillCategoryAndAccount(context)
                        if (bean == null) {
                            waitDialogLiveData.postValue(false)
                            snackBarLiveData.postValue("出错啦，请稍候重试...")
                        } else {
                            billAccountLiveData.postValue(bean.billAccounts)
                            if (billProperty > 0) {
                                billCategoryLiveData.postValue(bean.incomeBillCategories)
                            } else {
                                billCategoryLiveData.postValue(bean.expendBillCategories)
                            }
                            waitDialogLiveData.postValue(false)
                        }
                        addAccountAndCategoryDialogLiveData.postValue(false)
                        waitDialogLiveData.postValue(false)
                        snackBarLiveData.postValue("添加成功")
                    } else {
                        addAccountAndCategoryDialogLiveData.postValue(false)
                        waitDialogLiveData.postValue(false)
                        snackBarLiveData.postValue(addResult.exceptionMessage)
                    }
                } else {
                    val addResult =
                        model.addAccount(context, text.toString().trim())
                    if (addResult.result) {
                        val bean = model.getBillCategoryAndAccount(context)
                        if (bean == null) {
                            waitDialogLiveData.postValue(false)
                            snackBarLiveData.postValue("出错啦，请稍候重试...")
                        } else {
                            billAccountLiveData.postValue(bean.billAccounts)
                            if (billProperty > 0) {
                                billCategoryLiveData.postValue(bean.incomeBillCategories)
                            } else {
                                billCategoryLiveData.postValue(bean.expendBillCategories)
                            }
                            waitDialogLiveData.postValue(false)
                        }
                        addAccountAndCategoryDialogLiveData.postValue(false)
                        waitDialogLiveData.postValue(false)
                        snackBarLiveData.postValue("添加成功")
                    } else {
                        addAccountAndCategoryDialogLiveData.postValue(false)
                        waitDialogLiveData.postValue(false)
                        snackBarLiveData.postValue(addResult.exceptionMessage)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                addAccountAndCategoryDialogLiveData.postValue(false)
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("出错啦，请检查后重试")
            }
        }
    }

    fun saveBill(billMoney: Editable, billRemark: Editable, context: Context) {
        if (TextUtils.isEmpty(billMoney)) {
            snackBarLiveData.value = "金额不能为空"
            return
        }
        if ("." == billMoney.toString().trim()) {
            snackBarLiveData.value = "金额不合法"
            return
        }
        if (billMoney.toString().trim().toDouble() == 0.0) {
            snackBarLiveData.value = "金额不能为 0"
            return
        }
        if (bill!!.billCategory == null) {
            snackBarLiveData.value = "还没选择账单分类呢"
            return
        }
        if (bill!!.billAccount == null) {
            snackBarLiveData.value = "还没选择账单账户呢"
            return
        }
        if (!TextUtils.isEmpty(billRemark)) {
            bill!!.billRemark = billRemark.toString().trim()
        }
        bill!!.billMoney = billMoney.toString().trim().toDouble()
        threadPool.execute {
            bill!!.objectId = StringUtil.getRandomString()
            bill!!.billId = StringUtil.getRandomString()
            bill!!.billUser = model.getUserId(context)
            bill!!.billStatus = 1
            bill!!.billType = 0
            bill!!.updateTime = 0
            bill!!.createTime = System.currentTimeMillis()
            val saveResult = model.saveBill(bill!!, context)
            if (saveResult) {
                saveDialogLiveData.postValue(true)
            } else {
                saveDialogLiveData.postValue(false)
            }
        }
    }
}
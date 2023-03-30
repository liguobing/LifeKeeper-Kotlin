package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount
import com.lixyz.lifekeeperforkotlin.model.BillAccountModel
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class BillAccountViewModel : ViewModel() {

    var waitDialogLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var accountListLiveData: MutableLiveData<ArrayList<BillAccount>> = MutableLiveData()
    var snackBarLiveData: MutableLiveData<String> = MutableLiveData()

    private var model = BillAccountModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("账单账户列表线程池"))


    fun getBillAccount(context: Context) {
        waitDialogLiveData.value = true
        threadPool.execute {
            val accounts = model.getAccounts(context)
            accountListLiveData.postValue(accounts)
            waitDialogLiveData.postValue(false)
        }
    }

    fun addAccount(context: Context, name: Editable?) {
        if (TextUtils.isEmpty(name)) {
            snackBarLiveData.value = "账户名称不能为空"
        } else {
            waitDialogLiveData.value = true
            threadPool.execute {
                val result = model.addAccount(context, name.toString().trim())
                if (result.result) {
                    val categoryAndAccount = model.getAccounts(context)
                    accountListLiveData.postValue(categoryAndAccount)
                    waitDialogLiveData.postValue(false)
                    snackBarLiveData.postValue("添加成功")
                } else {
                    waitDialogLiveData.postValue(false)
                    snackBarLiveData.postValue(result.exceptionMessage.toString())
                }
            }
        }
    }

    fun deleteBillAccount(
        context: Context,
        accounts: ArrayList<BillAccount>,
        clickItemIndex: Int
    ) {
        waitDialogLiveData.value = true
        threadPool.execute {
            val objectIdList = ArrayList<String>()
            objectIdList.add(accounts[clickItemIndex].objectId!!)
            val result = model.deleteBillAccount(context, objectIdList)
            if (result) {
                val account = model.getAccounts(context)
                accountListLiveData.postValue(account)
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("删除成功")
            } else {
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("删除失败，请稍候重试")
            }
        }
    }

    fun updateAccount(context: Context, billAccount: BillAccount, newName: Editable?) {
        waitDialogLiveData.value = true
        threadPool.execute {
            if (TextUtils.isEmpty(newName)) {
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("新名称不能为空")
            } else {
                billAccount.accountName = newName.toString().trim()
                val result = model.updateBillAccount(context, billAccount)
                if (result.result) {
                    val categoryAndAccount = model.getAccounts(context)
                    accountListLiveData.postValue(categoryAndAccount)
                    waitDialogLiveData.postValue(false)
                    snackBarLiveData.postValue("修改成功")
                } else {
                    waitDialogLiveData.postValue(false)
                    snackBarLiveData.postValue(result.exceptionMessage.toString())
                }
            }
        }
    }

    fun updateAccountOrder(context: Context, data: ArrayList<BillAccount>) {
        waitDialogLiveData.value = true
        threadPool.execute {
            val result = model.updateBillAccountOrder(context, data)
            if (result) {
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("修改成功")
            } else {
                val accounts = model.getAccounts(context)
                accountListLiveData.postValue(accounts)
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("修改失败，请稍候重试")
            }
        }
    }
}
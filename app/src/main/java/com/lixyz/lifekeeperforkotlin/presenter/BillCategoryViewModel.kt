package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.text.Editable
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory
import com.lixyz.lifekeeperforkotlin.model.BillCategoryModel
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class BillCategoryViewModel : ViewModel() {

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("账单分类列表线程池"))

    var waitDialogLiveData: MutableLiveData<Boolean>? = null
    var incomeCategoryLiveData: MutableLiveData<ArrayList<BillCategory>>? = null
    var expendCategoryLiveData: MutableLiveData<ArrayList<BillCategory>>? = null
    var snackBarLiveData: MutableLiveData<String>? = null

    private val model: BillCategoryModel = BillCategoryModel()

    init {
        waitDialogLiveData = MutableLiveData()
        incomeCategoryLiveData = MutableLiveData()
        expendCategoryLiveData = MutableLiveData()
        snackBarLiveData = MutableLiveData()
    }

    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
    }

    fun getBillCategory(context: Context) {
        waitDialogLiveData!!.value = true
        threadPool.execute {
            val categoryAndAccount = model.getBillCategories(context)
            if (categoryAndAccount != null) {
                incomeCategoryLiveData!!.postValue(categoryAndAccount.incomeBillCategories)
                expendCategoryLiveData!!.postValue(categoryAndAccount.expendBillCategories)
                waitDialogLiveData!!.postValue(false)
            }
        }
    }

    fun deleteBillCategory(
        context: Context,
        list: ArrayList<BillCategory>,
        index: Int
    ) {
        waitDialogLiveData!!.value = true
        threadPool.execute {
            val objectIdList = ArrayList<String>()
            objectIdList.add(list[index].objectId!!)
            val result = model.deleteBillCategory(context, objectIdList)
            if (result) {
                val categoryAndAccount = model.getBillCategories(context)
                if (categoryAndAccount != null) {
                    incomeCategoryLiveData!!.postValue(categoryAndAccount.incomeBillCategories)
                    expendCategoryLiveData!!.postValue(categoryAndAccount.expendBillCategories)
                }
                waitDialogLiveData!!.postValue(false)
                snackBarLiveData!!.postValue("删除成功")
            } else {
                waitDialogLiveData!!.postValue(false)
                snackBarLiveData!!.postValue("删除失败，请稍候重试")
            }
        }
    }

    fun addBillCategory(context: Context, isIncome: Int, text: Editable?) {
        waitDialogLiveData!!.value = true
        threadPool.execute {
            if (TextUtils.isEmpty(text)) {
                waitDialogLiveData!!.postValue(false)
                snackBarLiveData!!.postValue("分类名称不能为空")
            } else {
                val result = model.addCategory(context, isIncome, text.toString().trim())
                if (result.result) {
                    val categoryAndAccount = model.getBillCategories(context)
                    if (categoryAndAccount != null) {
                        incomeCategoryLiveData!!.postValue(categoryAndAccount.incomeBillCategories)
                        expendCategoryLiveData!!.postValue(categoryAndAccount.expendBillCategories)
                    }
                    waitDialogLiveData!!.postValue(false)
                    snackBarLiveData!!.postValue("添加成功")
                } else {
                    waitDialogLiveData!!.postValue(false)
                    snackBarLiveData!!.postValue(result.exceptionMessage.toString())
                }
            }
        }
    }

    fun updateCategory(
        context: Context,
        billCategory: BillCategory,
        newName: Editable?
    ) {
        waitDialogLiveData!!.value = true
        threadPool.execute {
            if (TextUtils.isEmpty(newName)) {
                waitDialogLiveData!!.postValue(false)
                snackBarLiveData!!.postValue("新名称不能为空")
            } else {
                billCategory.categoryName = newName.toString().trim()
                val result = model.updateBillCategory(context, billCategory)
                if (result.result) {
                    val categoryAndAccount = model.getBillCategories(context)
                    if (categoryAndAccount != null) {
                        incomeCategoryLiveData!!.postValue(categoryAndAccount.incomeBillCategories)
                        expendCategoryLiveData!!.postValue(categoryAndAccount.expendBillCategories)
                    }
                    waitDialogLiveData!!.postValue(false)
                    snackBarLiveData!!.postValue("修改成功")
                } else {
                    waitDialogLiveData!!.postValue(false)
                    snackBarLiveData!!.postValue(result.exceptionMessage.toString())
                }
            }
        }
    }

    fun updateCategoryOrder(context: Context, data: ArrayList<BillCategory>) {
        waitDialogLiveData!!.value = true
        threadPool.execute {
            val result = model.updateBillCategoryOrder(context, data)
            if (result) {
                waitDialogLiveData!!.postValue(false)
                snackBarLiveData!!.postValue("修改成功")
            } else {
                val categoryAndAccount = model.getBillCategories(context)
                if (categoryAndAccount != null) {
                    incomeCategoryLiveData!!.postValue(categoryAndAccount.incomeBillCategories)
                    expendCategoryLiveData!!.postValue(categoryAndAccount.expendBillCategories)
                }
                waitDialogLiveData!!.postValue(false)
                snackBarLiveData!!.postValue("修改失败，请稍候重试")
            }
        }
    }
}
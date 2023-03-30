package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.services.core.AMapException
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.billshop.BillShopCategory
import com.lixyz.lifekeeperforkotlin.model.BillShopModel
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class BillShopViewModel : ViewModel() {
    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("账单商家线程池"))
    val billShopCategoryLiveData = MutableLiveData<ArrayList<BillShopCategory>>()
    val billShopNamesLiveData = MutableLiveData<ArrayList<String>>()
    val waitDialogLiveData = MutableLiveData<Boolean>()
    val snackBarLiveData = MutableLiveData<String>()
    val pageNumberLiveData = MutableLiveData<Int>()
    val pageCountLiveData = MutableLiveData<Int>()
    val searchKeyword = MutableLiveData<String>()
    val model = BillShopModel()
    val toolbarStatusLiveData = MutableLiveData<Boolean>()

    fun getOftenUseShop(context: Context, pageNumber: Int, pageItemCount: Int) {
        toolbarStatusLiveData.value = false
        waitDialogLiveData.value = true
        threadPool.execute {
            val list = getBillShopCategory()
            list.forEachIndexed { index, billShopCategory ->
                if (index == 0) {
                    billShopCategory.itemColor = Color.parseColor("#1B82D1")
                } else {
                    billShopCategory.itemColor = Color.parseColor("#F5F5F5")
                }
            }
            billShopCategoryLiveData.postValue(list)
            val shopResult = model.getOftenUseBillShop(context, pageNumber, pageItemCount)
            if (shopResult != null) {
                val count = shopResult.shopCount
                val shops = shopResult.shopNames
                val offset = shopResult.offset
                pageNumberLiveData.postValue(offset)
                if (count > pageItemCount) {
                    val pageCount = count / pageItemCount
                    if (pageCount * pageItemCount < count) {
                        pageNumberLiveData.postValue(pageNumber + 1)
                        pageCountLiveData.postValue(pageCount + 1)
                    } else {
                        pageNumberLiveData.postValue(pageNumber + 1)
                        pageCountLiveData.postValue(pageCount)
                    }
                } else {
                    pageNumberLiveData.postValue(1)
                    pageCountLiveData.postValue(1)
                }
                billShopNamesLiveData.postValue(shops)
                waitDialogLiveData.postValue(false)
            } else {
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("查询出错，请稍后重试...")
            }
        }
    }

    private fun getBillShopCategory(): ArrayList<BillShopCategory> {
        val categoryList = ArrayList<BillShopCategory>()
        val category1 = BillShopCategory()
        category1.shopName = "常用商家"
        category1.itemColor = Color.parseColor("#F5F5F5")
        categoryList.add(category1)
        val category2 = BillShopCategory()
        category2.shopName = "所有商家"
        category2.itemColor = Color.parseColor("#F5F5F5")
        categoryList.add(category2)
        val category3 = BillShopCategory()
        category3.shopName = "附近商家"
        category3.itemColor = Color.parseColor("#F5F5F5")
        categoryList.add(category3)
        return categoryList
    }

    fun getAllShop(context: Context, pageNumber: Int, pageItemCount: Int) {
        toolbarStatusLiveData.value = false
        waitDialogLiveData.value = true
        threadPool.execute {
            val list = getBillShopCategory()
            list.forEachIndexed { index, billShopCategory ->
                if (index == 1) {
                    billShopCategory.itemColor = Color.parseColor("#1B82D1")
                } else {
                    billShopCategory.itemColor = Color.parseColor("#F5F5F5")
                }
            }
            billShopCategoryLiveData.postValue(list)
            val shopResult = model.getAllCustomShop(context, pageNumber, 15)
            if (shopResult != null) {
                val count = shopResult.shopCount
                val shops = shopResult.shopNames
                val offset = shopResult.offset

                pageNumberLiveData.postValue(offset)
                if (count > pageItemCount) {
                    val pageCount = count / pageItemCount
                    if (pageCount * pageItemCount < count) {
                        pageCountLiveData.postValue(pageCount + 1)
                        pageNumberLiveData.postValue(pageNumber + 1)
                    } else {
                        pageNumberLiveData.postValue(pageNumber + 1)
                        pageCountLiveData.postValue(pageCount)
                    }
                } else {
                    pageCountLiveData.postValue(1)
                    pageNumberLiveData.postValue(1)
                }
                billShopNamesLiveData.postValue(shops)
                waitDialogLiveData.postValue(false)
            } else {
                waitDialogLiveData.postValue(false)
                snackBarLiveData.postValue("查询出错，请稍后重试...")
            }
        }
    }

    fun getNearShopCategory() {
        toolbarStatusLiveData.value = false
        waitDialogLiveData.value = true
        val list = getBillShopCategory()
        list.forEachIndexed { index, billShopCategory ->
            if (index == 2) {
                billShopCategory.itemColor = Color.parseColor("#1B82D1")
            } else {
                billShopCategory.itemColor = Color.parseColor("#F5F5F5")
            }
        }
        pageNumberLiveData.value = 0
        pageCountLiveData.value = 0
        billShopCategoryLiveData.value = list
        billShopNamesLiveData.value = model.nearPoiCategoryList
        waitDialogLiveData.value = false
    }

    fun getNearShopByCategory(
        context: Context?,
        currentPage: Int,
        categoryName: String?,
        cityCode: String?,
        latitude: Double,
        longitude: Double,
        pageItemCount: Int
    ) {
        toolbarStatusLiveData.value = false
        val list = getBillShopCategory()
        val newCategory = BillShopCategory()
        newCategory.shopName = categoryName
        list.add(newCategory)
        list.forEachIndexed { index, billShopCategory ->
            if (index == 3) {
                billShopCategory.itemColor = Color.parseColor("#1B82D1")
            } else {
                billShopCategory.itemColor = Color.parseColor("#F5F5F5")
            }
        }
        billShopCategoryLiveData.value = list
        waitDialogLiveData.value = true
        threadPool.execute {
            val array = model.searchShopWithCategoryName(
                context,
                currentPage,
                pageItemCount,
                categoryName,
                cityCode,
                latitude,
                longitude
            )
            val pageCount = array.keyAt(0)
            val shopList = array[pageCount]
            pageNumberLiveData.postValue(currentPage + 1)
            pageCountLiveData.postValue(pageCount)
            if (currentPage + 1 > pageCount) {
                pageNumberLiveData.postValue(currentPage + 1)
                pageCountLiveData.postValue(currentPage + 1)
            }
            billShopNamesLiveData.postValue(shopList)
            waitDialogLiveData.postValue(false)
        }
    }

    fun searchShop(
        context: Context,
        pageNumber: Int,
        pageItemCount: Int,
        keyword: String?,
        cityCode: String?
    ) {
        if (TextUtils.isEmpty(keyword)) {
            snackBarLiveData.value = "商家 / 商圈不能为空"
        } else {
            waitDialogLiveData.value = true
            val list = getBillShopCategory()
            val shopCategory = BillShopCategory()
            shopCategory.shopName = keyword.toString()
            list.add(shopCategory)
            list.forEachIndexed { index, billShopCategory ->
                if (index == 3) {
                    billShopCategory.itemColor = Color.parseColor("#1B82D1")
                } else {
                    billShopCategory.itemColor = Color.parseColor("#F5F5F5")
                }
            }
            billShopCategoryLiveData.value = list
            threadPool.execute {
                try {
                    val result = model.keyWordSearch(
                        context,
                        pageNumber,
                        pageItemCount,
                        keyword.toString().trim(),
                        cityCode
                    )
                    val pageCount = result.keyAt(0)
                    val shops = result[pageCount]
                    pageNumberLiveData.postValue(pageNumber + 1)
                    pageCountLiveData.postValue(pageCount)
                    if (pageNumber + 1 > pageCount) {
                        pageNumberLiveData.postValue(pageNumber + 1)
                        pageCountLiveData.postValue(pageNumber + 1)
                    }
                    billShopNamesLiveData.postValue(shops)
                    searchKeyword.postValue(keyword.toString().trim())
                    waitDialogLiveData.postValue(false)
                } catch (e: AMapException) {
                    e.printStackTrace()
                    waitDialogLiveData.postValue(false)
                    snackBarLiveData.postValue("搜索出错，请稍后重试")
                }
            }
        }
    }

    /**
     * Activity 销毁，关闭线程池
     */
    fun activityOnDestroy() {
        if (!threadPool.isShutdown) {
            threadPool.shutdown()
        }
    }

    fun addNewShop(context: Context, text: Editable?) {
        if (TextUtils.isEmpty(text)) {
            snackBarLiveData.value = "商家名称不能为空"
        } else {
            waitDialogLiveData.value = true
            threadPool.execute {
                val result = model.addShop(context, text.toString())
                if (result.result) {
                    val list = getBillShopCategory()
                    list.forEachIndexed { index, billShopCategory ->
                        if (index == 1) {
                            billShopCategory.itemColor = Color.parseColor("#1B82D1")
                        } else {
                            billShopCategory.itemColor = Color.parseColor("#F5F5F5")
                        }
                    }
                    billShopCategoryLiveData.postValue(list)
                    val shopResult = model.getAllCustomShop(context, 0, 15)
                    if (shopResult != null) {
                        val count = shopResult.shopCount
                        val shops = shopResult.shopNames
                        val offset = shopResult.offset

                        pageNumberLiveData.postValue(offset)
                        if (count > 15) {
                            val pageCount = count / 15
                            if (pageCount * 15 < count) {
                                pageCountLiveData.postValue(pageCount + 1)
                                pageNumberLiveData.postValue(0 + 1)
                            } else {
                                pageNumberLiveData.postValue(0 + 1)
                                pageCountLiveData.postValue(pageCount)
                            }
                        } else {
                            pageCountLiveData.postValue(1)
                            pageNumberLiveData.postValue(1)
                        }
                        billShopNamesLiveData.postValue(shops)
                        waitDialogLiveData.postValue(false)
                    } else {
                        waitDialogLiveData.postValue(false)
                        snackBarLiveData.postValue("查询出错，请稍后重试...")
                    }
                } else {
                    waitDialogLiveData.postValue(false)
                    snackBarLiveData.postValue(result.exceptionMessage)
                }
            }
        }
    }
}
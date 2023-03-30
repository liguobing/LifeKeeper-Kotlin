package com.lixyz.lifekeeperforkotlin.model

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import android.util.SparseArray
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.billshop.BillShop
import com.lixyz.lifekeeperforkotlin.bean.billshop.ShopResult
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * 账单商家模型
 *
 * @author LGB
 */
class BillShopModel {
    /**
     * 获取用户 ID
     *
     * @param context Context
     * @return 用户 ID
     */
    private fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
            .getString("UserId", null)
    }

    /**
     * 获取经常使用的商家（账单表中的 BillShop 字段）
     *
     * @param context Context
     * @return 商家列表
     */
    fun getOftenUseBillShop(
        context: Context,
        currentPage: Int,
        pageItemSize: Int
    ): ShopResult? {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/GetOftenUseBillShop?offset=$currentPage&rows=$pageItemSize"
            )
            .addHeader("Token", getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val str = response.body!!.string()
        val gson = Gson()
        val result = gson.fromJson(str, NewResult::class.java)
        return if (result.result) {
            gson.fromJson(result.resultObject.toString(), ShopResult::class.java)
        } else {
            null
        }
    }

    /**
     * 获取所有自定义商家
     *
     * @param context Context
     * @return 自定义商家列表
     */
    fun getAllCustomShop(context: Context, currentPage: Int, pageItemSize: Int): ShopResult? {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/GetAllCustomShops?offset=$currentPage&rows=$pageItemSize"
            )
            .addHeader("Token", getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val gson = Gson()
        val result = gson.fromJson(response.body!!.string(), NewResult::class.java)
        return if (result.result) {
            gson.fromJson(result.resultObject.toString(), ShopResult::class.java)
        } else {
            null
        }
    }

    /**
     * 获取附近 POI 分类
     *
     * @return POI 分类列表
     */
    val nearPoiCategoryList: ArrayList<String>
        get() {
            val nearPoiCategoryList: ArrayList<String> = ArrayList()
            nearPoiCategoryList.add("汽车服务")
            nearPoiCategoryList.add("汽车销售")
            nearPoiCategoryList.add("汽车维修")
            nearPoiCategoryList.add("摩托车服务")
            nearPoiCategoryList.add("餐饮服务")
            nearPoiCategoryList.add("购物服务")
            nearPoiCategoryList.add("生活服务")
            nearPoiCategoryList.add("体育休闲服务")
            nearPoiCategoryList.add("医疗保健服务")
            nearPoiCategoryList.add("住宿服务")
            nearPoiCategoryList.add("风景名胜")
            nearPoiCategoryList.add("商务住宅")
            nearPoiCategoryList.add("政府机构及社会团体")
            nearPoiCategoryList.add("科教文化服务")
            nearPoiCategoryList.add("交通设施服务")
            nearPoiCategoryList.add("金融保险服务")
            nearPoiCategoryList.add("公司企业")
            nearPoiCategoryList.add("道路附属设施")
            nearPoiCategoryList.add("地名地址信息")
            nearPoiCategoryList.add("公共设施")
            nearPoiCategoryList.add("事件活动")
            nearPoiCategoryList.add("室内设施")
            nearPoiCategoryList.add("通行设施")
            return nearPoiCategoryList
        }

    /**
     * 根据分类名称搜索商家
     *
     * @param context       Context
     * @param pageNumber    搜索页码
     * @param pageItemCount 每页显示条数
     * @param categoryName  分类名称
     * @param cityCode      城市代码
     * @param latitude      维度
     * @param longitude     精度
     * @return 搜索结果
     * @throws AMapException AMapException
     */
    @Throws(AMapException::class)
    fun searchShopWithCategoryName(
        context: Context?,
        pageNumber: Int,
        pageItemCount: Int,
        categoryName: String?,
        cityCode: String?,
        latitude: Double,
        longitude: Double
    ): SparseArray<ArrayList<String>> {
        val sparse = SparseArray<ArrayList<String>>()
        val shopList: ArrayList<String> = ArrayList()
        val query: PoiSearch.Query = PoiSearch.Query("", categoryName, cityCode)
        query.pageSize = pageItemCount
        query.pageNum = pageNumber
        val poiSearch = PoiSearch(context, query)
        poiSearch.bound = PoiSearch.SearchBound(LatLonPoint(latitude, longitude), 1000)
        val poiResult: PoiResult = poiSearch.searchPOI()
        val pageCount: Int = poiResult.pageCount
        val list: ArrayList<PoiItem> = poiResult.pois
        if (list.size > 0) {
            for (item in list) {
                shopList.add(item.title)
            }
        } else {
            Log.d("TTT", "searchShopWithCategoryName: 没有")
        }
        sparse.put(pageCount, shopList)
        return sparse
    }

    /**
     * 关键字搜索
     *
     * @param context       Context
     * @param pageNumber    搜索页码
     * @param pageItemCount 每页显示 Item 数量
     * @param keyWord       关键字
     * @param cityCode      城市代码
     * @return 搜索结果
     */
    @Throws(AMapException::class)
    fun keyWordSearch(
        context: Context?,
        pageNumber: Int,
        pageItemCount: Int,
        keyWord: String?,
        cityCode: String?
    ): SparseArray<ArrayList<String>> {
        val poiNameList: ArrayList<String> = ArrayList()
        val sparse = SparseArray<ArrayList<String>>()
        val query: PoiSearch.Query = PoiSearch.Query(keyWord, null, cityCode)
        query.pageSize = pageItemCount
        query.pageNum = pageNumber
        val poiSearch = PoiSearch(context, query)
        val poiResult: PoiResult = poiSearch.searchPOI()
        val pageCount: Int = poiResult.pageCount
        val list: ArrayList<PoiItem> = poiResult.pois
        if (list.size > 0) {
            for (item in list) {
                poiNameList.add(item.title)
            }
        }
        sparse.put(pageCount, poiNameList)
        return sparse
    }

    /**
     * 添加新商家
     */
    @Throws(IOException::class, SQLiteException::class)
    fun addShop(context: Context, shopName: String): NewResult {
        val billShop = BillShop()
        billShop.objectId = StringUtil.getRandomString()
        billShop.shopId = StringUtil.getRandomString()
        billShop.shopName = shopName
        billShop.shopUser = getUserId(context)
        billShop.shopStatus = 1
        billShop.shopType = 0
        billShop.createTime = System.currentTimeMillis()
        billShop.updateTime = 0
        val requestBody =
            Gson().toJson(billShop).toRequestBody("application/json; charset=UTF-8".toMediaType())
        val request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/AddBillShop")
            .post(requestBody)
            .addHeader("Token", getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        return Gson().fromJson(response.body!!.string(), NewResult::class.java)
    }
}
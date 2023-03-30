package com.lixyz.lifekeeperforkotlin.view.activity

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.BillShopCategoryAdapter
import com.lixyz.lifekeeperforkotlin.adapter.BillShopNameAdapter
import com.lixyz.lifekeeperforkotlin.bean.billshop.BillShopCategory
import com.lixyz.lifekeeperforkotlin.presenter.BillShopViewModel
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog


class BillShopActivity : AppCompatActivity(), AMapLocationListener,
    View.OnClickListener, TextView.OnEditorActionListener {

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * Toolbar
     */
    private var toolbar: Toolbar? = null

    /**
     * Toolbar 标题
     */
    private var tvToolbarTitle: TextView? = null

    /**
     * Toolbar 搜索按钮
     */
    private var imgToolBarSearchButton: ImageView? = null

    /**
     * Toolbar 搜索输入框
     */
    private var etToolbarSearchView: EditText? = null

    /**
     * Toolbar 搜索框 LayoutParams
     */
    private var editLayoutParams: ViewGroup.LayoutParams? = null


    /**
     * 每页 Item 个数
     */
    private val pageItemCount = 15

    /**
     * 纬度
     */
    private var latitude = 0.0

    /**
     * 精度
     */
    private var longitude = 0.0

    /**
     * 城市代码
     */
    private var cityCode: String? = null


    /**
     * 添加新商家的 Dialog
     */
    private var addNewShopDialog: AlertDialog? = null

    private var shopCategoryList = ArrayList<BillShopCategory>()
    private var shopNameList = ArrayList<String>()
    private var shopCategoryIndex = 0
    private var pageNumber = 0
    private var pageCount = 0
    private var footer: TextView? = null
    private var nearShopCategory: String? = null
    private var shopNameAdapter: BillShopNameAdapter? = null
    private var viewModel: BillShopViewModel? = null
    private var btAddShop: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = resources.getColor(R.color.BillShopActivity_ToolBarColor, null)
        setContentView(R.layout.activity___bill_shop)
        initWidget()

        val lvCategory: ListView = findViewById(R.id.lv_shop_category)
        val categoryAdapter = BillShopCategoryAdapter(this, shopCategoryList)
        lvCategory.adapter = categoryAdapter
        val lvShop: ListView = findViewById(R.id.lv_shop_name)
        shopNameAdapter = BillShopNameAdapter(this, shopNameList)
        lvShop.adapter = shopNameAdapter
        val view = LayoutInflater.from(this)
            .inflate(R.layout.view___bill_shop___list_view_item, RelativeLayout(this), false)
        footer = view.findViewById(R.id.text)
        footer!!.text = "- 加载更多 -"
        lvShop.addFooterView(view)

        btAddShop = findViewById(R.id.bt_add_shop)

        val provider = ViewModelProvider(this)
        viewModel = provider[BillShopViewModel::class.java]
        viewModel!!.billShopCategoryLiveData.observe(this) {
            shopCategoryList.clear()
            shopCategoryList.addAll(it)
            categoryAdapter.notifyDataSetChanged()
        }
        viewModel!!.billShopNamesLiveData.observe(this) {
            if (it.size > 0) {
                shopNameList.addAll(it)
                shopNameAdapter!!.notifyDataSetChanged()
            }
        }
        viewModel!!.snackBarLiveData.observe(this) {
            Snackbar.make(lvCategory, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel!!.waitDialogLiveData.observe(this) {
            if (it) {
                if (!waitDialog!!.isShowing) {
                    waitDialog!!.show()
                }
            } else {
                if (waitDialog!!.isShowing) {
                    waitDialog!!.dismiss()
                }
            }
        }
        viewModel!!.pageNumberLiveData.observe(this) {
            pageNumber = it
        }
        viewModel!!.pageCountLiveData.observe(this) {
            pageCount = it
        }
        viewModel!!.searchKeyword.observe(this) {
            searchKeyword = it
        }
        viewModel!!.getOftenUseShop(this, pageNumber, 15)
        lvShop.setOnItemClickListener { _, _, position, _ ->
            when (shopCategoryIndex) {
                0 -> {
                    if (position != shopNameList.size) {
                        val intent = Intent()
                        intent.putExtra("SelectShop", shopNameList[position])
                        setResult(RESULT_OK,intent)
                        finish()
                    } else {
                        if (pageNumber != pageCount) {
                            viewModel!!.getOftenUseShop(this, pageNumber, 15)
                        } else {
                            footer!!.text = "没有啦"
                        }
                    }
                }
                1 -> {
                    if (position != shopNameList.size) {
                        val intent = Intent()
                        intent.putExtra("SelectShop", shopNameList[position])
                        setResult(RESULT_OK,intent)
                        finish()
                    } else {
                        if (pageNumber != pageCount) {
                            viewModel!!.getAllShop(this, pageNumber, 15)
                        } else {
                            footer!!.text = "没有啦"
                        }
                    }
                }
                2 -> {
                    footer!!.text = "- 加载更多 -"
                    if (position != shopNameList.size) {
                        shopCategoryIndex = 3
                        nearShopCategory = shopNameList[position]
                        shopNameList.clear()
                        shopNameAdapter!!.notifyDataSetChanged()
                        viewModel!!.getNearShopByCategory(
                            this,
                            pageNumber,
                            nearShopCategory,
                            cityCode,
                            latitude,
                            longitude,
                            15
                        )
                    } else {
                        if (pageNumber != pageCount) {
                            val intent = Intent()
                            intent.putExtra("SelectShop", shopNameList[position])
                            setResult(RESULT_OK,intent)
                            finish()
                        } else {
                            footer!!.text = "没有啦"
                        }
                    }
                }
                3 -> {
                    if (position != shopNameList.size) {
                        val intent = Intent()
                        intent.putExtra("SelectShop", shopNameList[position])
                        setResult(RESULT_OK,intent)
                        finish()
                    } else {
                        if (pageNumber != pageCount) {
                            viewModel!!.getNearShopByCategory(
                                this,
                                pageNumber,
                                nearShopCategory,
                                cityCode,
                                latitude,
                                longitude,
                                15
                            )
                        } else {
                            footer!!.text = "没有啦"
                        }
                    }
                }
                4 -> {
                    if (position != shopNameList.size) {
                        val intent = Intent()
                        intent.putExtra("SelectShop", shopNameList[position])
                        setResult(RESULT_OK,intent)
                        finish()
                    } else {
                        if (pageNumber != pageCount) {
                            viewModel!!.searchShop(
                                this,
                                pageNumber,
                                pageItemCount,
                                searchKeyword,
                                cityCode
                            )
                        } else {
                            footer!!.text = "没有啦"
                        }
                    }
                }
            }
        }
        lvCategory.setOnItemClickListener { _, _, position, _ ->
            footer!!.text = "- 加载更多 -"
            pageNumber = 0
            when (position) {
                0 -> {
                    shopCategoryIndex = 0
                    shopNameList.clear()
                    shopNameAdapter!!.notifyDataSetChanged()
                    viewModel!!.getOftenUseShop(this, pageNumber, pageItemCount)
                }
                1 -> {
                    shopCategoryIndex = 1
                    shopNameList.clear()
                    shopNameAdapter!!.notifyDataSetChanged()
                    viewModel!!.getAllShop(this, pageNumber, pageItemCount)
                }
                2 -> {
                    shopCategoryIndex = 2
                    shopNameList.clear()
                    shopNameAdapter!!.notifyDataSetChanged()
                    viewModel!!.getNearShopCategory()
                }
                3 -> {
                    shopCategoryIndex = 3
                    shopNameList.clear()
                    pageNumber = 0
                    pageCount = 0
                    shopNameAdapter!!.notifyDataSetChanged()
                    viewModel!!.getNearShopByCategory(
                        this,
                        pageNumber,
                        nearShopCategory,
                        cityCode,
                        latitude,
                        longitude,
                        pageItemCount
                    )
                }
            }
        }

        viewModel!!.toolbarStatusLiveData.observe(this) {
            if (it) {
                if (etToolbarSearchView!!.visibility == View.INVISIBLE) {
                    etToolbarSearchView!!.text = null
                    tvToolbarTitle!!.visibility = View.GONE
                    editLayoutParams = etToolbarSearchView!!.layoutParams
                    editLayoutParams!!.width = 0
                    etToolbarSearchView!!.layoutParams = editLayoutParams
                    etToolbarSearchView!!.visibility = View.VISIBLE
                    val animator = ValueAnimator.ofInt(
                        0,
                        toolbar!!.measuredWidth - imgToolBarSearchButton!!.measuredWidth
                    )
                    animator.interpolator = LinearInterpolator()
                    animator.duration = 500
                    animator.repeatCount = 0
                    animator.addUpdateListener { animation ->
                        editLayoutParams!!.width = animation.animatedValue as Int
                        etToolbarSearchView!!.layoutParams = editLayoutParams
                    }
                    animator.start()
                }
            } else {
                if (etToolbarSearchView!!.visibility == View.VISIBLE) {
                    val animator = ValueAnimator.ofInt(
                        toolbar!!.measuredWidth - imgToolBarSearchButton!!.measuredWidth,
                        0
                    )
                    animator.interpolator = LinearInterpolator()
                    animator.duration = 500
                    animator.repeatCount = 0
                    animator.addUpdateListener { animation ->
                        val width = animation.animatedValue as Int
                        editLayoutParams!!.width = width
                        etToolbarSearchView!!.layoutParams = editLayoutParams
                        if (width == 0) {
                            tvToolbarTitle!!.visibility = View.VISIBLE
                            etToolbarSearchView!!.visibility = View.INVISIBLE
                        }
                    }
                    hideSoftInput(imgToolBarSearchButton!!)
                    animator.start()
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        startLocation()
    }

    override fun onResume() {
        super.onResume()
        initListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.activityOnDestroy()
    }


    fun initWidget() {
        waitDialog = CustomDialog(this, this, "请稍后")
        toolbar = findViewById(R.id.toolbar)
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title)
        etToolbarSearchView = findViewById(R.id.et_toolbar_search_content)
        imgToolBarSearchButton = findViewById(R.id.img_toolbar_search_button)
    }

    fun initListener() {
        imgToolBarSearchButton!!.setOnClickListener(this)
        etToolbarSearchView!!.setOnEditorActionListener(this)
        btAddShop!!.setOnClickListener(this)
    }


    /**
     * 显示添加新商家 Dialog
     */
    private fun showAddNewShopDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(
            R.layout.view___search_shop___dialog___add_shop_item,
            LinearLayout(this)
        )
        val etNewShopName = view.findViewById<EditText>(R.id.et_new_shop_name)
        val btSubmitNewShop: Button = view.findViewById(R.id.bt_submit_new_shop)
        btSubmitNewShop.setOnClickListener {
            if (addNewShopDialog != null && addNewShopDialog!!.isShowing) {
                addNewShopDialog!!.dismiss()
                viewModel!!.addNewShop(this@BillShopActivity, etNewShopName.text)
            }
        }
        builder.setView(view)
        addNewShopDialog = builder.create()
        addNewShopDialog!!.show()
    }

    private fun setToolbarSearchView() {
        if (etToolbarSearchView!!.visibility == View.INVISIBLE) {
            etToolbarSearchView!!.text = null
            tvToolbarTitle!!.visibility = View.GONE
            editLayoutParams = etToolbarSearchView!!.layoutParams
            editLayoutParams!!.width = 0
            etToolbarSearchView!!.layoutParams = editLayoutParams
            etToolbarSearchView!!.visibility = View.VISIBLE
            val animator = ValueAnimator.ofInt(
                0,
                toolbar!!.measuredWidth - imgToolBarSearchButton!!.measuredWidth
            )
            animator.interpolator = LinearInterpolator()
            animator.duration = 500
            animator.repeatCount = 0
            animator.addUpdateListener { animation ->
                editLayoutParams!!.width = animation.animatedValue as Int
                etToolbarSearchView!!.layoutParams = editLayoutParams
            }
            animator.start()
        } else if (etToolbarSearchView!!.visibility == View.VISIBLE) {
            val animator = ValueAnimator.ofInt(
                toolbar!!.measuredWidth - imgToolBarSearchButton!!.measuredWidth,
                0
            )
            animator.interpolator = LinearInterpolator()
            animator.duration = 500
            animator.repeatCount = 0
            animator.addUpdateListener { animation ->
                val width = animation.animatedValue as Int
                editLayoutParams!!.width = width
                etToolbarSearchView!!.layoutParams = editLayoutParams
                if (width == 0) {
                    tvToolbarTitle!!.visibility = View.VISIBLE
                    etToolbarSearchView!!.visibility = View.INVISIBLE
                }
            }
            hideSoftInput(imgToolBarSearchButton!!)
            animator.start()
        }
    }


    /**
     * 开始定位
     */
    private fun startLocation() {
        val mLocationClient = AMapLocationClient(this)
        val mLocationOption = AMapLocationClientOption()
        mLocationOption.isOnceLocation = true
        mLocationClient.setLocationListener(this)
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        mLocationClient.setLocationOption(mLocationOption)
        mLocationClient.startLocation()
    }

    /**
     * 隐藏软键盘
     */
    private fun hideSoftInput(view: View) {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                latitude = aMapLocation.latitude
                longitude = aMapLocation.longitude
                cityCode = aMapLocation.cityCode
            }
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_toolbar_search_button -> {
                setToolbarSearchView()
            }
            R.id.bt_add_shop -> {
                showAddNewShopDialog()
            }
            else -> {
            }
        }
    }

    private var searchKeyword: String? = null

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            shopCategoryIndex = 4
            shopNameList.clear()
            shopNameAdapter!!.notifyDataSetChanged()
            pageNumber = 0
            pageCount = 0
            viewModel!!.searchShop(
                this,
                pageNumber,
                pageItemCount,
                etToolbarSearchView!!.text.toString(),
                cityCode
            )
        }
        return false
    }
}
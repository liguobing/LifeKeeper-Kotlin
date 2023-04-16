package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.BillListRecyclerViewAdapter
import com.lixyz.lifekeeperforkotlin.adapter.BillListRecyclerViewDecoration
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.presenter.BillListViewModel
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CircleWaveProgressSurfaceView
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import java.text.DecimalFormat
import java.util.*


class BillListActivity : AppCompatActivity(), View.OnClickListener,
    BillListRecyclerViewAdapter.OnItemClickListener {

    /**
     * 主界面根布局
     */
    private var mainView: RelativeLayout? = null

    /**
     * 抽屉界面根布局
     */
    private var drawerView: LinearLayout? = null

    /**
     * 控件 - 圆形波浪进度条
     */
    private lateinit var waveProgress: CircleWaveProgressSurfaceView

    /**
     * 控件 - 上一月
     */
    private var imgLastMonth: ImageView? = null

    /**
     * 控件 - 下一月
     */
    private var imgNextMonth: ImageView? = null


    /**
     * 控件 - 收入月份
     */
    private var tvIncomeMonth: TextView? = null

    /**
     * 控件 - 支出月份
     */
    private var tvExpendMonth: TextView? = null

    /**
     * 控件 - 当月收入金额
     */
    private var tvIncomeMoney: TextView? = null

    /**
     * 控件 - 当前支出金额
     */
    private var tvExpendMoney: TextView? = null

    /**
     * 控件 - 圆形按钮
     */
    private lateinit var menuButtonLayout: FloatingActionMenu

    /**
     * 控件 - 添加收入账单按钮
     */
    private var menuAddIncomeBillButton: FloatingActionButton? = null

    /**
     * 控件 - 添加支出账单按钮
     */
    private var menuAddExpendBillButton: FloatingActionButton? = null

    /**
     * 控件 - 抽屉菜单
     */
    private lateinit var menuDrawerLayout: DrawerLayout

    /**
     * 控件 - 呼出抽屉菜单按钮
     */
    private var imgOpenDrawerMenu: ImageView? = null


    /**
     * 控件 - 抽屉菜单Item - 账单种类
     */
    private var tvMenuItemBillCategory: TextView? = null

    /**
     * 控件 - 抽屉菜单Item - 账单账户
     */
    private var tvMenuItemBillAccount: TextView? = null

    /**
     * 控件 - 抽屉菜单Item - 图标
     */
    private var tvMenuItemChart: TextView? = null

    /**
     * 控件 - 账单 RecyclerView
     */
    private var rvBills: RecyclerView? = null

    /**
     * 抽屉菜单的宽度
     */
    private var drawerWidth = 0

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * 当前年份
     */
    private var currentYear = 0

    /**
     * 展示数据的年份
     */
    private var displayYear = 0

    /**
     * 展示数据的月份
     */
    private var displayMonth = 0

    /**
     * 账单列表数据 List
     */
    private var billList: ArrayList<BillBean>? = null

    /**
     * 账单列表 RecyclerView Adapter
     */
    private lateinit var adapter: BillListRecyclerViewAdapter

    /**
     * 账单详情 Dialog
     */
    private lateinit var billDetailDialog: Dialog

    /**
     * 控件 - 账单详情 - 删除账单
     */
    private var tvDeleteBill: TextView? = null

    /**
     * 控件 - 账单详情 - 账单金额
     */
    private var tvBillMoney: TextView? = null

    /**
     * 控件 - 账单详情 - 账单分类
     */
    private var tvBillCategory: TextView? = null

    /**
     * 控件 - 账单详情 - 账单图片
     */
    private var imgBillImage: ImageView? = null

    /**
     * 控件 - 账单详情 - 账单账户
     */
    private var tvBillAccount: TextView? = null

    /**
     * 控件 - 账单详情 - 账单日期
     */
    private var tvBillDate: TextView? = null

    /**
     * 控件 - 账单详情 - 账单商家
     */
    private var tvBillShop: TextView? = null

    /**
     * 控件 - 账单详情 - 账单备注
     */
    private var tvBillRemark: TextView? = null

    /**
     * 显示详情的账单 Index（用于编辑/删除按钮）
     */
    private var showBillDetailIndex = 0

    private var viewModel: BillListViewModel? = null

    private var userId = ""

    private var progress: CircleWaveProgressSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //没有 TitleBar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        //设置状态栏颜色
        window.statusBarColor = Color.parseColor("#1B82D1")
        setContentView(R.layout.activity___bill_list)
        initWidget()

        val incomeMonth = findViewById<TextView>(R.id.tv_income_month)
        val expendMonth = findViewById<TextView>(R.id.tv_expend_month)
        val incomeMoneyCount = findViewById<TextView>(R.id.tv_income_money_count)
        val expendMoneyCount = findViewById<TextView>(R.id.tv_expend_money_count)
        progress = findViewById(R.id.wave_progress)

        val viewModelProvider = ViewModelProvider(this)
        viewModel = viewModelProvider[BillListViewModel::class.java]
        viewModel!!.billOverviewLiveData!!.observe(this) {
            incomeMonth.text =
                String.format(getString(R.string.BillListActivityIncomeMonth), it.month)
            expendMonth.text =
                String.format(getString(R.string.BillListActivityExpendMonth), it.month)
            val df1 = DecimalFormat("##,##0.00")
            incomeMoneyCount.text = df1.format(it.incomeCount)
            expendMoneyCount.text = df1.format(it.expendCount)
            progress!!.setFinalProgressPercent((it.expendCount!! / it.incomeCount!!).toFloat() * 100)
            progress!!.start()
            billList!!.clear()
            billList!!.addAll(it.bills!!)
            adapter.notifyDataSetChanged()
        }
        viewModel!!.waitDialogLiveData!!.observe(this) {
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
        viewModel!!.userIdLiveData!!.observe(this) {
            userId = it
        }
        viewModel!!.snackBarLiveData!!.observe(this) {
            Snackbar.make(tvExpendMonth!!, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel!!.deleteLiveData!!.observe(this) {
            if (it) {
                adapter.notifyDataSetChanged()
            }
        }
        viewModel!!.getBillOverviewData(this, displayYear, displayMonth)
    }

    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        progress!!.notifyWave()
    }

    override fun onPause() {
        super.onPause()
        progress!!.waveWait()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.shutdownThreadPool()
//        waveProgress.visibility = View.GONE
        progress!!.stop()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_open_drawer_menu -> {
                progress!!.waveWait()
                menuDrawerLayout.openDrawer(GravityCompat.START)
            }
            R.id.img_next_month -> {
                displayMonth += 1
                if (displayMonth > 12) {
                    displayYear += 1
                    displayMonth = 1
                }
                viewModel!!.getBillOverviewData(this, displayYear, displayMonth)
            }
            R.id.img_last_month -> {
                displayMonth -= 1
                if (displayMonth < 1) {
                    displayYear -= 1
                    displayMonth = 12
                }
                viewModel!!.getBillOverviewData(this, displayYear, displayMonth)
            }
            R.id.menu_add_income_bill_button -> {
                menuButtonLayout.close(true)
                val incomeIntent = Intent(this, AddBillActivity::class.java)
                incomeIntent.putExtra("BillProperty", 1)
                startActivityForResult(incomeIntent,1)
            }
            R.id.menu_add_expend_bill_button -> {
                menuButtonLayout.close(true)
                val expendIntent = Intent(this, AddBillActivity::class.java)
                expendIntent.putExtra("BillProperty", -1)
                startActivityForResult(expendIntent,1)
            }
            R.id.tv_delete_bill -> {
                billDetailDialog.dismiss()
                val deleteBuilder: AlertDialog.Builder = AlertDialog.Builder(this@BillListActivity)
                deleteBuilder.setMessage("确定删除该账单吗？")
                deleteBuilder.setPositiveButton(
                    "删除"
                ) { dialog, _ ->
                    billDetailDialog.dismiss()
                    dialog.dismiss()
                    viewModel!!.deleteBill(
                        billList!!,
                        showBillDetailIndex,
                        userId,
                        displayYear,
                        displayMonth
                    )
                }
                val deleteDialog: AlertDialog = deleteBuilder.create()
                deleteDialog.show()
                deleteDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            }
            R.id.tv_menu_item_bill_category -> {
                menuDrawerLayout.closeDrawer(GravityCompat.START)
                val categoryIntent = Intent(this, BillCategoryActivity::class.java)
                startActivity(categoryIntent)
            }
            R.id.tv_menu_item_bill_account -> {
                menuDrawerLayout.closeDrawer(GravityCompat.START)
                val accountIntent = Intent(this, BillAccountActivity::class.java)
                startActivityForResult(accountIntent, 1)
            }
            R.id.tv_menu_item_chart -> {
                menuDrawerLayout.closeDrawer(GravityCompat.START)
                progress!!.waveWait()
                val chartIntent = Intent(this, BillChartActivity::class.java)
                startActivityForResult(chartIntent, 1)
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel!!.getBillOverviewData(this, displayYear, displayMonth)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val display: Display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width: Int = size.x
            drawerWidth = width * 3 / 4
            val layoutParams: ViewGroup.LayoutParams = drawerView!!.layoutParams
            layoutParams.width = width * 3 / 4
            drawerView!!.layoutParams = layoutParams
        }
    }

    fun initWidget() {
        drawerView = findViewById(R.id.drawer_view)
        mainView = findViewById(R.id.view)
        imgLastMonth = findViewById(R.id.img_last_month)
        imgNextMonth = findViewById(R.id.img_next_month)
        tvIncomeMonth = findViewById(R.id.tv_income_month)
        tvExpendMonth = findViewById(R.id.tv_expend_month)
        tvIncomeMoney = findViewById(R.id.tv_income_money_count)
        tvExpendMoney = findViewById(R.id.tv_expend_money_count)
        rvBills = findViewById(R.id.rv_bills)
        menuButtonLayout = findViewById(R.id.menu_button_layout)
        menuButtonLayout.setClosedOnTouchOutside(true)
        menuAddIncomeBillButton = findViewById(R.id.menu_add_income_bill_button)
        menuAddExpendBillButton = findViewById(R.id.menu_add_expend_bill_button)
        menuDrawerLayout = findViewById(R.id.menu_drawer_layout)
        menuDrawerLayout.setScrimColor(Color.TRANSPARENT)
        imgOpenDrawerMenu = findViewById(R.id.img_open_drawer_menu)
        tvMenuItemBillCategory = findViewById(R.id.tv_menu_item_bill_category)
        tvMenuItemBillAccount = findViewById(R.id.tv_menu_item_bill_account)
        tvMenuItemChart = findViewById(R.id.tv_menu_item_chart)

        waitDialog = CustomDialog(this, this, "请稍候...")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONDAY] + 1
        displayYear = currentYear
        displayMonth = currentMonth

        billList = ArrayList()
        adapter = BillListRecyclerViewAdapter(billList!!)
        rvBills!!.adapter = adapter
        rvBills!!.layoutManager = LinearLayoutManager(this)
        rvBills!!.addItemDecoration(BillListRecyclerViewDecoration(this))

        val dialogView: View = LayoutInflater.from(this)
            .inflate(R.layout.view___bill_list___popup_window, LinearLayout(this))
        tvDeleteBill = dialogView.findViewById(R.id.tv_delete_bill)
        tvBillMoney = dialogView.findViewById(R.id.tv_bill_money)
        tvBillCategory = dialogView.findViewById(R.id.tv_bill_category)
        imgBillImage = dialogView.findViewById(R.id.img_bill_image)
        tvBillAccount = dialogView.findViewById(R.id.tv_bill_account)
        tvBillDate = dialogView.findViewById(R.id.tv_bill_date)
        tvBillShop = dialogView.findViewById(R.id.tv_bill_shop)
        tvBillRemark = dialogView.findViewById(R.id.tv_bill_remark)

        billDetailDialog = Dialog(this)
        billDetailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        billDetailDialog.setContentView(dialogView)
        val window = billDetailDialog.window
        if (window != null) {
            val lp: WindowManager.LayoutParams = window.attributes
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = lp
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun initListener() {
        //上半部分控件点击事件
        imgLastMonth!!.setOnClickListener(this)
        imgNextMonth!!.setOnClickListener(this)
        imgOpenDrawerMenu!!.setOnClickListener(this)
        //RecyclerView Item Click 事件
        adapter.setOnItemClickListener(this)
        //抽屉菜单 Item 点击事件
        tvMenuItemBillCategory!!.setOnClickListener(this)
        tvMenuItemBillAccount!!.setOnClickListener(this)
        tvMenuItemChart!!.setOnClickListener(this)
        //圆形按钮点击事件
        menuAddIncomeBillButton!!.setOnClickListener(this)
        menuAddExpendBillButton!!.setOnClickListener(this)
        //抽屉菜单开关事件
        menuDrawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(v: View, slideOffset: Float) {
                val scrollWidth = slideOffset * drawerWidth
                //setScroll中的参数，正数表示向左移动，负数向右
                mainView!!.scrollX = (-1 * scrollWidth).toInt()
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
//                progress!!.start()
                progress!!.notifyWave()
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
        //RecyclerView 滚动事件，滚动时隐藏右下角按钮
        rvBills!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> menuButtonLayout.animate()
                        .alpha(1f)
                    AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL, AbsListView.OnScrollListener.SCROLL_STATE_FLING -> menuButtonLayout.animate()
                        .alpha(0f)
                    else -> {
                    }
                }
            }
        })
        //删除账单按钮
        tvDeleteBill!!.setOnClickListener(this)
    }


    override fun onItemClick(position: Int) {
        if (menuButtonLayout.isOpened) {
            menuButtonLayout.close(true)
        } else {
            showBillDetailIndex = position
            val billBean = billList!![position]
            if (billBean.billProperty > 0) {
                tvBillMoney!!.text = java.lang.String.format("+%s", billBean.billMoney)
                tvBillMoney!!.setTextColor(Color.GREEN)
            } else {
                tvBillMoney!!.text = java.lang.String.format("-%s", billBean.billMoney)
                tvBillMoney!!.setTextColor(Color.RED)
            }
            tvBillCategory!!.text = billBean.billCategory
            tvBillAccount!!.text = billBean.billAccount
            tvBillDate!!.text = StringUtil.milliToString(billBean.billDate, false)
            tvBillShop!!.text = billBean.billShop
            tvBillRemark!!.text = billBean.billRemark
            if (billBean.billImage != null && billBean.billImage!!.size != 0) {
                Glide.with(this)
                    .load(Constant.CLOUD_THUMBNAIL_PIC_ADDRESS + userId + "/thumbnail/" + billBean.billImage!![0])
                    .error(Glide.with(this).load(R.drawable.bill_detail___bill_image))
                    .into(imgBillImage!!)
            }
            billDetailDialog.show()
        }
    }
}
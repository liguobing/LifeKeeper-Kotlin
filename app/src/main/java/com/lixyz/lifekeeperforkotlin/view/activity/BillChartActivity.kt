package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.BillCategoryRecyclerViewAdapter
import com.lixyz.lifekeeperforkotlin.adapter.BillChartMonthMenuListViewAdapter
import com.lixyz.lifekeeperforkotlin.adapter.BillChartMonthMenuYearRecyclerViewAdapter
import com.lixyz.lifekeeperforkotlin.adapter.BillChartPieCharListViewAdapter
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.bean.billchart.BillChartItemBean
import com.lixyz.lifekeeperforkotlin.presenter.BillChartViewModel
import com.lixyz.lifekeeperforkotlin.utils.ColorUtil
import com.lixyz.lifekeeperforkotlin.utils.DisplayUtil
import com.lixyz.lifekeeperforkotlin.utils.MyXFormatter
import com.lixyz.lifekeeperforkotlin.utils.TimeUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import java.text.NumberFormat
import java.util.*


/**
 * 账单图表 Activity
 *
 * @author LGB
 */
class BillChartActivity : AppCompatActivity(), View.OnClickListener,
    OnChartValueSelectedListener {
    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * 返回按钮
     */
    private var imgBack: ImageView? = null

    /**
     * 修改月份按钮
     */
    private var imgMonthMenu: ImageView? = null

    /**
     * Toolbar 日期
     */
    private var tvToolbarDate: TextView? = null

    /**
     * 收入金额
     */
    private var tvIncome: TextView? = null

    /**
     * 结余奖金
     */
    private var tvBalance: TextView? = null

    /**
     * 支出金额
     */
    private var tvExpend: TextView? = null

    /**
     * 日平均
     */
    private var tvEvaluate: TextView? = null

    /**
     * 表单年份
     */
    private var year = 0

    /**
     * 表单月份
     */
    private var month = 0

    /**
     * 圆形图表
     */
    private var pieChart: PieChart? = null

    /**
     * 圆形图表，收入按钮
     */
    private var tvIncomePie: TextView? = null

    /**
     * 圆形图表，支出按钮
     */
    private var tvExpendPie: TextView? = null

    /**
     * 圆形图表，账单属性
     */
    private var pieBillProperty = 1

    /**
     * 每个分类下的账单
     */
    private var lvBillsByCategory: ListView? = null

    /**
     * 分类账单列表
     */
    private val billsByCategory: ArrayList<BillBean> = ArrayList()

    /**
     * 分类账单 ListView Adapter
     */
    private var billsByCategoryAdapter: BillChartPieCharListViewAdapter? = null


    /**
     * 折线图
     */
    private var lineChart: LineChart? = null

    /**
     * 折线图 - 收入属性按钮
     */
    private var tvLineChartIncome: TextView? = null

    /**
     * 折线图 - 支出属性按钮
     */
    private var tvLineChartExpend: TextView? = null

    /**
     * 折线图 - 全部属性按钮
     */
    private var tvLineChartAll: TextView? = null

    /**
     * 屏幕宽度
     */
    private var screenWidth = 0

    /**
     * 选择表单日期的 Dialog
     */
    private var changeDateDialog: AlertDialog? = null

    private val currency: NumberFormat = NumberFormat.getCurrencyInstance()


    private var viewModel: BillChartViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = resources.getColor(R.color.BillChartActivity_ToolBarColor, null)
        setContentView(R.layout.activity___chart)
        initWidget()
    }

    override fun onStart() {
        super.onStart()
        val provider = ViewModelProvider(this)
        viewModel = provider[BillChartViewModel::class.java]
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
        viewModel!!.snackBarLiveData.observe(this) {
            Snackbar.make(tvBalance!!, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel!!.overviewLiveData.observe(this) {
            tvIncome!!.text = currency.format(it.income)
            tvExpend!!.text = currency.format(it.expend)
            tvBalance!!.text = currency.format((it.income - it.expend))
            val daysCount = TimeUtil.getDaysOfMonth(year, month)
            tvEvaluate!!.text = currency.format(it.expend / daysCount)
        }
        viewModel!!.pieChartLiveData.observe(this) {
            if (it!!.isEmpty()) {
                val dataSet = PieDataSet(it, "")
                val data = PieData(dataSet)
                val centerText = SpannableString("无消费")
                centerText.setSpan(RelativeSizeSpan(1.7f), 0, 3, 0)
                centerText.setSpan(StyleSpan(Typeface.NORMAL), 0, 3, 0)
                pieChart!!.centerText = centerText
                pieChart!!.data = data
            } else {
                val dataSet = PieDataSet(it, "")
                dataSet.setDrawIcons(false)
                dataSet.sliceSpace = 3f
                dataSet.iconsOffset = MPPointF(0f, 40f)
                dataSet.selectionShift = 5f
                val colors: ArrayList<Int> = ColorUtil.getPieChartItemColor()
                colors.add(ColorTemplate.getHoloBlue())
                dataSet.colors = colors
                val data = PieData(dataSet)
                data.setValueFormatter(PercentFormatter(pieChart))
                data.setValueTextSize(11f)
                data.setValueTextColor(Color.BLACK)
                pieChart!!.data = data
            }
            pieChart!!.invalidate()
        }

        viewModel!!.pieChartBillListLiveData.observe(this) {
            Log.d("TTT", "onStart: ${it.size}")
            if (it.size == 0) {
                lvBillsByCategory!!.visibility = View.GONE
            } else {
                lvBillsByCategory!!.visibility = View.VISIBLE
                billsByCategory.clear()
                billsByCategory.addAll(it)
                billsByCategoryAdapter!!.notifyDataSetChanged()
                setListViewHeightBasedOnChildren(lvBillsByCategory)
            }
        }

        viewModel!!.lineChartLiveData.observe(this) {
            when (it.type) {
                1 -> {
                    val incomeLine = LineData(getIncomeLineDataSet(it.incomeList!!))
                    incomeLine.setValueTextColor(Color.WHITE)
                    incomeLine.setValueTextSize(9f)
                    lineChart!!.data = incomeLine
                }
                2 -> {
                    val expendLine = LineData(getExpendLineDataSet(it.expendList!!))
                    expendLine.setValueTextColor(Color.WHITE)
                    expendLine.setValueTextSize(9f)
                    lineChart!!.data = expendLine
                }
                3 -> {
                    val lineData =
                        LineData(
                            getIncomeLineDataSet(it.incomeList!!),
                            getExpendLineDataSet(it.expendList!!)
                        )
                    lineChart!!.data = lineData
                }
                else -> {
                }
            }
            lineChart!!.invalidate()
            val params: ViewGroup.LayoutParams = lineChart!!.layoutParams
            if (it.incomeList!!.size <= 5) {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                params.width = screenWidth / 5 * it.incomeList!!.size
            }
            lineChart!!.layoutParams = params
        }

        viewModel!!.getChartData(this, year, month)
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
        //等待 Dialog
        waitDialog = CustomDialog(this, this, "请稍后...")
        //屏幕数据
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        //初始日期
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        //toolbar 控件
        imgBack = findViewById(R.id.img_back)
        imgMonthMenu = findViewById(R.id.img_month_menu)
        tvToolbarDate = findViewById(R.id.tv_toolbar_date)
        if (month < 10) {
            tvToolbarDate!!.text = java.lang.String.format(Locale.CHINA, "%d年0%d月", year, month)
        } else {
            tvToolbarDate!!.text = java.lang.String.format(Locale.CHINA, "%d年%d月", year, month)
        }
        //概览控件
        tvIncome = findViewById(R.id.tv_income_money)
        tvBalance = findViewById(R.id.tv_balance_money)
        tvExpend = findViewById(R.id.tv_expend_money)
        tvEvaluate = findViewById(R.id.tv_evaluate_money)
        //饼状图
        initPieChart()
        //饼状图内容按钮：收入还是支出
        tvIncomePie = findViewById(R.id.tv_income)
        tvExpendPie = findViewById(R.id.tv_expend)
        //饼状图详细内容 ListView 相关
        lvBillsByCategory = findViewById(R.id.lv_bills_by_category)
        billsByCategoryAdapter = BillChartPieCharListViewAdapter(billsByCategory, this)
        lvBillsByCategory!!.adapter = billsByCategoryAdapter
        //饼状图详细内容 ListView Item 动画
        val set = AnimationSet(true)
        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 200
        set.addAnimation(animation)
        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        animation.setDuration(200)
        set.addAnimation(animation)
        val controller = LayoutAnimationController(
            set, 0.5f
        )
        lvBillsByCategory!!.layoutAnimation = controller
        //折线图
        initLineChart()
        tvLineChartIncome = findViewById(R.id.tv_line_chart_income)
        tvLineChartExpend = findViewById(R.id.tv_line_chart_expend)
        tvLineChartAll = findViewById(R.id.tv_line_chart_all)
    }

    /**
     * 初始化饼状图
     */
    private fun initPieChart() {
        pieChart = findViewById(R.id.pie_chart)
        //设置使用百分比
        pieChart!!.setUsePercentValues(true)
        //隐藏表单描述
        pieChart!!.description.isEnabled = false
        //设置表单编剧
        pieChart!!.setExtraOffsets(
            DisplayUtil.dip2px(this, 5f),
            0f,
            DisplayUtil.dip2px(this, 5f),
            0f
        )
        //设置摩擦系数（值越小摩擦系数越大）
        pieChart!!.dragDecelerationFrictionCoef = 0.95f
        //设置环中的文字
        val centerText = SpannableString("收入统计")
        centerText.setSpan(RelativeSizeSpan(1.7f), 0, 4, 0)
        centerText.setSpan(StyleSpan(Typeface.NORMAL), 0, 4, 0)
        pieChart!!.centerText = centerText
        //这个方法为 true 就是环形图，为 false 就是饼图
        pieChart!!.isDrawHoleEnabled = true
        //设置环形中间空白颜色是白色
        pieChart!!.setHoleColor(Color.WHITE)
        //设置半透明圆环的颜色
        pieChart!!.setTransparentCircleColor(Color.WHITE)
        //设置半透明圆环的透明度
        pieChart!!.setTransparentCircleAlpha(110)
        //以最大半径的百分比(最大半径 = 整个图表的半径)来设置饼图中心的孔半径,默认为50％
        pieChart!!.holeRadius = 58f
        //设置饼图中孔旁边的透明圆的半径,以最大半径的百分比表示(最大半径=整个图表的半径),默认为55％->默认情况下比中心孔大5％
        pieChart!!.transparentCircleRadius = 61f
        //饼状图中间可以添加文字
        pieChart!!.setDrawCenterText(true)
        // 初始旋转角度
        pieChart!!.rotationAngle = 0f
        // 可以手动旋转
        pieChart!!.isRotationEnabled = false
        //将此设置为 false 可以防止点击手势突出显示值。
        pieChart!!.isHighlightPerTapEnabled = true
        //设置 Item 图例
        val pieChartLegend: Legend = pieChart!!.legend
        pieChartLegend.isEnabled = false
        //设置 Item Label 文字颜色
        pieChart!!.setEntryLabelColor(Color.BLACK)
        //设置 Item Label 文字大小
        pieChart!!.setEntryLabelTextSize(12f)
    }

    /**
     * 初始化折线图
     */
    private fun initLineChart() {
        lineChart = findViewById(R.id.line_chart)
        lineChart!!.description.isEnabled = false
        //图表设置
        //是否展示网格线
        lineChart!!.setDrawGridBackground(false)
        //是否显示边界
        lineChart!!.setDrawBorders(true)
        //是否可以拖动
        lineChart!!.isDragEnabled = false
        //是否有触摸事件
        lineChart!!.setTouchEnabled(true)
        //设置XY轴动画效果
        lineChart!!.animateY(2500)
        lineChart!!.animateX(1500)
        //XY轴的设置
        val xAxis: XAxis = lineChart!!.xAxis
        val leftYAxis: YAxis = lineChart!!.axisLeft
        val rightYaxis: YAxis = lineChart!!.axisRight
        //X轴设置显示位置在底部
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f
        xAxis.valueFormatter = MyXFormatter()
        //保证Y轴从0开始，不然会上移一点
        leftYAxis.axisMinimum = 0f
        rightYaxis.axisMinimum = 0f
        //折线图例 标签 设置
        val legend: Legend = lineChart!!.legend
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.form = Legend.LegendForm.LINE
        legend.textSize = 12f
        //显示位置 左下方
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        //是否绘制在图表里面
        legend.setDrawInside(false)
    }

    fun initListener() {
        imgBack!!.setOnClickListener(this)
        imgMonthMenu!!.setOnClickListener(this)
        pieChart!!.setOnChartValueSelectedListener(this)
        tvIncomePie!!.setOnClickListener(this)
        tvExpendPie!!.setOnClickListener(this)
        tvLineChartIncome!!.setOnClickListener(this)
        tvLineChartExpend!!.setOnClickListener(this)
        tvLineChartAll!!.setOnClickListener(this)
    }

    private fun changePieChartBillPropertyButtonStatus(clickButton: Int) {
        if (clickButton == 1) {
            tvIncomePie!!.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.bill_chart___pie_chart___bill_property___check
                )

            tvIncomePie!!.setTextColor(
                resources.getColor(
                    R.color.BillChartActivity_PieChartBillPropertyTextCheckedColor,
                    null
                )
            )
            tvExpendPie!!.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.bill_chart___pie_chart___bill_property___uncheck
                )
            tvExpendPie!!.setTextColor(
                resources.getColor(
                    R.color.BillChartActivity_PieChartBillPropertyTextUnCheckedColor,
                    null
                )
            )
        } else {
            tvIncomePie!!.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.bill_chart___pie_chart___bill_property___uncheck
                )
            tvIncomePie!!.setTextColor(
                resources.getColor(
                    R.color.BillChartActivity_PieChartBillPropertyTextUnCheckedColor,
                    null
                )
            )
            tvExpendPie!!.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.bill_chart___pie_chart___bill_property___check
                )
            tvExpendPie!!.setTextColor(
                resources.getColor(
                    R.color.BillChartActivity_PieChartBillPropertyTextCheckedColor,
                    null
                )
            )
        }
    }

    private fun changeLineChartBillPropertyButtonStatus(clickButton: Int) {
        when (clickButton) {
            0 -> {
                tvLineChartAll!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___check
                    )
                tvLineChartAll!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextCheckedColor,
                        null
                    )
                )
                tvLineChartIncome!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___uncheck
                    )
                tvLineChartIncome!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextUnCheckedColor,
                        null
                    )
                )
                tvLineChartExpend!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___uncheck
                    )
                tvLineChartExpend!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextUnCheckedColor,
                        null
                    )
                )
            }
            1 -> {
                tvLineChartAll!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___uncheck
                    )
                tvLineChartAll!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextUnCheckedColor,
                        null
                    )
                )
                tvLineChartIncome!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___check
                    )
                tvLineChartIncome!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextCheckedColor,
                        null
                    )
                )
                tvLineChartExpend!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___uncheck
                    )
                tvLineChartExpend!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextUnCheckedColor,
                        null
                    )
                )
            }
            -1 -> {
                tvLineChartAll!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___uncheck
                    )
                tvLineChartAll!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextUnCheckedColor,
                        null
                    )
                )
                tvLineChartIncome!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___uncheck
                    )
                tvLineChartIncome!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextUnCheckedColor,
                        null
                    )
                )
                tvLineChartExpend!!.background =
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bill_chart___line_chart___bill_property___check
                    )
                tvLineChartExpend!!.setTextColor(
                    resources.getColor(
                        R.color.BillChartActivity_LineChartBillPropertyTextCheckedColor,
                        null
                    )
                )
            }
            else -> {
            }
        }
    }

    private fun getIncomeLineDataSet(data: ArrayList<Float>): LineDataSet {
        val incomeValue: ArrayList<Entry> = ArrayList()
        for (i in 0 until data.size) {
            incomeValue.add(
                Entry(
                    i.toFloat(),
                    data[i]
                )
            )
        }
        val incomeSet = LineDataSet(incomeValue, "收入")
        incomeSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        incomeSet.axisDependency = YAxis.AxisDependency.LEFT
        incomeSet.color = Color.GREEN
        incomeSet.setCircleColor(Color.GREEN)
        incomeSet.lineWidth = 2f
        incomeSet.circleRadius = 3f
        incomeSet.fillAlpha = 65
        incomeSet.fillColor = Color.GREEN
        incomeSet.highLightColor = Color.rgb(244, 117, 117)
        incomeSet.setDrawCircleHole(false)
        return incomeSet
    }

    private fun getExpendLineDataSet(data: ArrayList<Float>): LineDataSet {
        val expendValue: ArrayList<Entry> = ArrayList()
        for (i in 0 until data.size) {
            expendValue.add(
                Entry(
                    i.toFloat(),
                    data[i]
                )
            )
        }
        val expendSet = LineDataSet(expendValue, "支出")
        expendSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        expendSet.axisDependency = YAxis.AxisDependency.LEFT
        expendSet.color = Color.RED
        expendSet.setCircleColor(Color.RED)
        expendSet.lineWidth = 2f
        expendSet.circleRadius = 3f
        expendSet.fillAlpha = 65
        expendSet.fillColor = Color.RED
        expendSet.highLightColor = Color.rgb(244, 117, 117)
        expendSet.setDrawCircleHole(false)
        return expendSet
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (lvBillsByCategory!!.visibility == View.GONE) {
            lvBillsByCategory!!.visibility = View.VISIBLE
        }
        val entry = e as PieEntry
        viewModel!!.getPieItemList(this, entry.label, pieBillProperty, year, month)
    }

    override fun onNothingSelected() {
        if (lvBillsByCategory!!.visibility == View.VISIBLE) {
            lvBillsByCategory!!.visibility = View.GONE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val widgetSize: Int = size.x
            val params: ViewGroup.LayoutParams = pieChart!!.layoutParams
            params.width = widgetSize
            params.height = widgetSize
            pieChart!!.layoutParams = params
        }
    }

    /**
     * 动态设置ListView的高度
     *
     * @param listView 要修改高度的 ListView
     */
    private fun setListViewHeightBasedOnChildren(listView: ListView?) {
        if (listView == null) {
            return
        }
        val listAdapter: ListAdapter = listView.adapter ?: return
        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem: View = listAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        val params: ViewGroup.LayoutParams = listView.layoutParams
        params.height = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
        listView.layoutParams = params
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_back -> {
                finish()
            }
            R.id.img_month_menu -> {
                showMonthMenuDialog()
            }
            R.id.tv_income -> {
                changePieChartBillPropertyButtonStatus(1)
                pieBillProperty = 1
                viewModel!!.getIncomePieEntryList()
                lvBillsByCategory!!.visibility = View.GONE
            }
            R.id.tv_expend -> {
                changePieChartBillPropertyButtonStatus(-1)
                pieBillProperty = -1
                viewModel!!.getExpendPieEntryList()
                lvBillsByCategory!!.visibility = View.GONE
            }
            R.id.tv_line_chart_income -> {
                viewModel!!.getIncomeLineChartDataList()
                changeLineChartBillPropertyButtonStatus(1)
            }
            R.id.tv_line_chart_expend -> {
                viewModel!!.getExpendLineChartDataList()
                changeLineChartBillPropertyButtonStatus(-1)
            }
            R.id.tv_line_chart_all -> {
                viewModel!!.getAllLineChartDataList()
                changeLineChartBillPropertyButtonStatus(0)
            }
            else -> {
            }
        }
    }

    private fun showMonthMenuDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(
            R.layout.view___bill_chart___month_menu,
            LinearLayout(this),
            false
        )
        val yearList: ArrayList<BillChartItemBean> = ArrayList()
        val calendar: Calendar = Calendar.getInstance()
        val currentYear: Int = calendar.get(Calendar.YEAR)
        year = currentYear
        yearList.add(BillChartItemBean(currentYear, true))
        for (i in currentYear - 1 downTo currentYear - 10 + 1) {
            yearList.add(BillChartItemBean(i, false))
        }
        val rvYear: RecyclerView = view.findViewById(R.id.rv_year)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.HORIZONTAL
        rvYear.layoutManager = llm
        val yearAdapter = BillChartMonthMenuYearRecyclerViewAdapter(yearList)
        rvYear.adapter = yearAdapter
        yearAdapter.setOnItemClickListener(object :
            BillCategoryRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                for (i in 0 until yearList.size) {
                    if (i == position) {
                        yearList[i].isClick = true
                        year = yearList[i].itemName
                    } else {
                        yearList[i].isClick = false
                    }
                }
                yearAdapter.notifyDataSetChanged()
            }
        })
        val gvMonth: GridView = view.findViewById(R.id.gv_month)
        val monthList: ArrayList<String> = ArrayList()
        monthList.add("一月")
        monthList.add("二月")
        monthList.add("三月")
        monthList.add("四月")
        monthList.add("五月")
        monthList.add("六月")
        monthList.add("七月")
        monthList.add("八月")
        monthList.add("九月")
        monthList.add("十月")
        monthList.add("十一月")
        monthList.add("十二月")
        val monthAdapter = BillChartMonthMenuListViewAdapter(this, monthList)
        gvMonth.adapter = monthAdapter
        gvMonth.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                month = position + 1
                if (month < 10) {
                    tvToolbarDate!!.text = java.lang.String.format(
                        Locale.CHINA,
                        "%d年0%d月",
                        year,
                        month
                    )
                } else {
                    tvToolbarDate!!.text = java.lang.String.format(
                        Locale.CHINA,
                        "%d年%d月",
                        year,
                        month
                    )
                }
                viewModel!!.getChartData(this@BillChartActivity, year, month)
                changeDateDialog!!.dismiss()
            }
        builder.setView(view)
        changeDateDialog = builder.create()
        changeDateDialog!!.setCanceledOnTouchOutside(false)
        changeDateDialog!!.setCancelable(false)
        changeDateDialog!!.show()
    }
}
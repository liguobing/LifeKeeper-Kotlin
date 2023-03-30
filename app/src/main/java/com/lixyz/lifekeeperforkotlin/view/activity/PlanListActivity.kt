package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.*
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.billchart.BillChartItemBean
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanBean
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanListDateMenuItemBean
import com.lixyz.lifekeeperforkotlin.presenter.PlanListPresenter
import com.lixyz.lifekeeperforkotlin.utils.TimeUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import java.lang.ref.WeakReference
import java.util.*


/**
 * 计划列表 Activity
 *
 * @author LGB
 */
class PlanListActivity : BaseActivity(), View.OnClickListener, IPlanListView {
    /**
     * Presenter
     */
    private var presenter: PlanListPresenter? = null

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * Handler
     */
    private val handler = MyHandler(this)

    /**
     * 日期
     */
    private var tvDate: TextView? = null

    /**
     * 日期菜单
     */
    private var rvDateMenu: RecyclerView? = null

    /**
     * 日期菜单 RecyclerView Adapter
     */
    private var dateMenuAdapter: PlanListDateMenuRecyclerAdapter? = null

    /**
     * 日期菜单 RecyclerView 数据 list
     */
    private var dateMenuDataList: ArrayList<PlanListDateMenuItemBean>? = null

    /**
     * 计划 RecyclerView
     */
    private var rvPlans: RecyclerView? = null

    /**
     * 计划 RecyclerView Adapter
     */
    private var planListPlanRecyclerViewAdapter: PlanListPlanRecyclerViewAdapter? = null

    /**
     * 未完成计划 RecyclerView List
     */
    private var unFinishedPlanList: ArrayList<PlanBean>? = null

    /**
     * 已完成计划 RecyclerView List
     */
    private var finishedPlanList: ArrayList<PlanBean>? = null

    /**
     * 所有计划 RecyclerView List
     */
    private var allPlans: ArrayList<PlanBean>? = null

    /**
     * 年
     */
    private var year = 0

    /**
     * 月
     */
    private var month = 0

    /**
     * 日
     */
    private var day = 0

    /**
     * 选择表单日期的 Dialog
     */
    private var changeDateDialog: AlertDialog? = null

    /**
     * 今日按钮
     */
    private var tvToday: TextView? = null

    /**
     * 添加计划按钮
     */
    private var addPlan: FloatingActionButton? = null

    /**
     * 没有计划的图片文字提示
     */
    private var tvNoPlan: TextView? = null

    /**
     * 需要添加提醒的计划在列表中的位置
     */
    private var needAddAlarmPlanIndex = 0

    /**
     * 屏幕的宽度
     */
    private var screenWidth = 0

    /**
     * 今日按钮的距离 Window 顶部的位置
     */
    private var todayButtonTop = 0

    /**
     * 今日按钮的距离 Window 左边的位置
     */
    private var todayButtonLeft = 0

    /**
     * 今日按钮的宽度
     */
    private var todayButtonWidth = 0

    /**
     * 首次初始化参数的标识
     */
    private var initParams = true

    /**
     * 日期按钮的布局参数,用于修改"今日"按钮显示/隐藏时,日期按钮的 宽度
     */
    private var dateButtonParams: ViewGroup.LayoutParams? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = resources.getColor(R.color.PlanListActivityBackgroundColor, null)
        setContentView(R.layout.activity___plan_list)
        initWidget()
    }

    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        presenter!!.activityOnResume(this, year, month, day)
    }


    override fun initWidget() {
        presenter = PlanListPresenter(this)
        waitDialog = CustomDialog(this, this, "请稍后...")
        val calendar: Calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
        tvDate = findViewById(R.id.tv_date)
        tvDate!!.translationZ = 20f
        rvDateMenu = findViewById(R.id.rv_date_menu)
        dateMenuDataList = ArrayList()
        dateMenuAdapter = PlanListDateMenuRecyclerAdapter(this, dateMenuDataList!!)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        rvDateMenu!!.layoutManager = manager
        rvDateMenu!!.adapter = dateMenuAdapter
        rvPlans = findViewById(R.id.rv_plans)
        allPlans = ArrayList()
        unFinishedPlanList = ArrayList()
        finishedPlanList = ArrayList()
        planListPlanRecyclerViewAdapter = PlanListPlanRecyclerViewAdapter(allPlans!!, this)
        planListPlanRecyclerViewAdapter!!.setHasStableIds(true)
        rvPlans!!.adapter = planListPlanRecyclerViewAdapter
        rvPlans!!.layoutManager = LinearLayoutManager(this)
        rvPlans!!.itemAnimator = DefaultItemAnimator()
        tvToday = findViewById(R.id.tv_today)
        tvToday!!.translationZ = 20f
        addPlan = findViewById(R.id.fab_add_plan)
        tvNoPlan = findViewById(R.id.tv_oops)
    }

    override fun initListener() {
        //选择日期按钮
        tvDate!!.setOnClickListener(this)
        //“今日”按钮
        tvToday!!.setOnClickListener(this)
        //添加计划按钮
        addPlan!!.setOnClickListener(this)
        //计划 RecyclerView 列表滚动监听，滚动时，关闭侧滑菜单
        rvPlans!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                planListPlanRecyclerViewAdapter!!.closeOpenMenu()
            }
        })
        //日期 RecyclerView Item 点击监听器
        dateMenuAdapter!!.setOnItemClickListener(object :
            PlanListDateMenuRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, x: Int, y: Int) {
                dateMenuDataList!![position].isClick = true
                for (bean in dateMenuDataList!!) {
                    if (dateMenuDataList!!.indexOf(bean) != position) {
                        bean.isClick = false
                    }
                }
                dateMenuAdapter!!.notifyDataSetChanged()
                presenter!!.selectNewDate(this@PlanListActivity, year, month, position + 1)
                if (!TimeUtil.isToday(year, month, position + 1)) {
                    changeTodayButtonStatus(1, x, y)
                } else {
                    changeTodayButtonStatus(-1, x, y)
                }
            }
        })
        //完成计划按钮
        planListPlanRecyclerViewAdapter!!.setFinishListener(object :
            PlanListPlanRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@PlanListActivity)
                builder.setMessage("确定完成计划？")
                builder.setPositiveButton(
                    "完成"
                ) { dialog, _ ->
                    dialog.dismiss()
                    planListPlanRecyclerViewAdapter!!.closeOpenMenu()
                    presenter!!.setPlanFinish(this@PlanListActivity, allPlans!![position])
                }
                builder.show()
            }
        })
        //删除计划按钮
        planListPlanRecyclerViewAdapter!!.setDeleteListener(object :
            PlanListPlanRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@PlanListActivity)
                builder.setMessage("删除单个计划还是删除整组计划？")
                builder.setPositiveButton(
                    "删除单个"
                ) { dialog, _ ->
                    dialog.dismiss()
                    planListPlanRecyclerViewAdapter!!.closeOpenMenu()
                    presenter!!.deleteSinglePlan(this@PlanListActivity, allPlans!![position])
                }
                builder.setNegativeButton(
                    "删除整组"
                ) { dialog, _ ->
                    dialog.dismiss()
                    planListPlanRecyclerViewAdapter!!.closeOpenMenu()
                    presenter!!.deleteGroupPlan(this@PlanListActivity, allPlans!![position])
                }
                builder.show()
            }
        })
        //撤销完成计划
        planListPlanRecyclerViewAdapter!!.setUndoListener(object :
            PlanListPlanRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@PlanListActivity)
                builder.setMessage("撤销完成计划？")
                builder.setPositiveButton(
                    "撤销"
                ) { dialog, _ ->
                    dialog.dismiss()
                    planListPlanRecyclerViewAdapter!!.closeOpenMenu()
                    presenter!!.setPlanUnFinish(this@PlanListActivity, allPlans!![position])
                }
                builder.show()
            }
        })
        //提醒按钮
        planListPlanRecyclerViewAdapter!!.setAlarmListener(object :
            PlanListPlanRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                //有提醒时，取消提醒
                if (allPlans!![position].alarmTime >= 0) {
                    presenter!!.removeAlarm(this@PlanListActivity, allPlans!![position])
                } else {
                    needAddAlarmPlanIndex = position
                    //没有提醒时
                    val intent = Intent(this@PlanListActivity, AddPlanAlarmActivity::class.java)
                    startActivityForResult(intent, SET_ALARM_RESULT_CODE)
                }
            }
        })
    }

    /**
     * 更改"今日按钮"的状态
     *
     * @param status        按钮的状态:
     * 1:显示
     * -1:隐藏
     * @param clickItemLeft 点击日期 RecyclerView Item 的 X 坐标
     * @param clickItemTop  点击日期 RecyclerView Item 的 Y 坐标
     */
    private fun changeTodayButtonStatus(status: Int, clickItemLeft: Int, clickItemTop: Int) {
        //如果今日按钮已经显示了,则不进行任何操作,如果是隐藏状态,则显示按钮并执行动画
        if (status > 0) {
            if (tvToday!!.visibility != View.VISIBLE) {
                tvToday!!.visibility = View.VISIBLE
                dateButtonParams!!.width =
                    screenWidth - addPlan!!.measuredWidth - addPlan!!.measuredWidth - dip2px(
                        this,
                        80f
                    )
                tvDate!!.layoutParams = dateButtonParams
                if (clickItemLeft > todayButtonLeft) {
                    tvToday!!.animate()
                        .translationX(clickItemLeft - todayButtonLeft - todayButtonWidth.toFloat())
                        .translationY(-(todayButtonTop - clickItemTop).toFloat()).setDuration(0)
                        .start()
                } else {
                    tvToday!!.animate()
                        .translationX(0 - (todayButtonLeft - clickItemLeft) - todayButtonWidth.toFloat())
                        .translationY(-(todayButtonTop - clickItemTop).toFloat()).setDuration(0)
                        .start()
                }
                tvToday!!.animate().translationX(0f).translationY(0f).setDuration(300).start()
            }
        } else {
            tvToday!!.visibility = View.GONE
            dateButtonParams!!.width = screenWidth - addPlan!!.measuredWidth - dip2px(this, 60f)
            tvDate!!.layoutParams = dateButtonParams
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            //获取屏幕宽高
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
            val screenHeight: Int = size.y
            //获取 StatusBar 的高度
            val resources: Resources = resources
            val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
            val statusBarHeight: Int = resources.getDimensionPixelSize(resourceId)
            //获取添加计划按钮的宽高
            val addPlanButtonHeight: Int = addPlan!!.measuredHeight
            //获取标题高度
            val tvTitle = findViewById<TextView>(R.id.tv_title)
            val titleHeight = tvTitle.measuredHeight
            //获取日期列表高度
            val dateParams = rvDateMenu!!.layoutParams
            val dateListHeight = dateParams.height
            //设置计划列表的高度
            val params = rvPlans!!.layoutParams
            params.height = screenHeight - dip2px(
                this,
                30f
            ) - titleHeight - dateListHeight - statusBarHeight - addPlanButtonHeight
            rvPlans!!.layoutParams = params


            //设置日期按钮和今日按钮的高度
            dateButtonParams = tvDate!!.layoutParams
            dateButtonParams!!.height = addPlanButtonHeight
            tvDate!!.layoutParams = dateButtonParams
            val todayParams = tvToday!!.layoutParams
            todayParams.height = addPlanButtonHeight
            tvToday!!.layoutParams = todayParams
            if (tvToday!!.visibility == View.GONE) {
                dateButtonParams!!.width =
                    screenWidth - addPlan!!.measuredWidth - dip2px(this, 60f)
                tvDate!!.layoutParams = dateButtonParams
            } else {
                dateButtonParams!!.width =
                    screenWidth - tvToday!!.measuredWidth - addPlan!!.measuredWidth - dip2px(
                        this,
                        80f
                    )
                tvDate!!.layoutParams = dateButtonParams
            }
            if (initParams) {
                val arr = IntArray(2)
                tvToday!!.getLocationInWindow(arr)
                todayButtonLeft = arr[0]
                todayButtonTop = arr[1]
                todayButtonWidth = tvToday!!.measuredWidth
                tvToday!!.visibility = View.GONE
                dateButtonParams!!.width =
                    screenWidth - addPlan!!.measuredWidth - dip2px(this, 60f)
                tvDate!!.layoutParams = dateButtonParams
                initParams = false
            }
        }
    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun hideWaitDialog() {
        handler.sendEmptyMessage(HIDE_WAIT_DIALOG)
    }

    override fun updateWaitDialog(message: String?) {
        val msg: Message = Message.obtain()
        msg.what = UPDATE_WAIT_DIALOG
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun showSnakeBar(message: String?) {
        val msg: Message = Message.obtain()
        msg.what = SHOW_SNACK_BAR
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun updatePlanList(list: List<List<PlanBean>>?) {
        allPlans!!.clear()
        unFinishedPlanList!!.clear()
        unFinishedPlanList!!.addAll(list!![0])
        finishedPlanList!!.clear()
        finishedPlanList!!.addAll(list[1])
        allPlans!!.addAll(unFinishedPlanList!!)
        allPlans!!.addAll(finishedPlanList!!)
        handler.sendEmptyMessage(UPDATE_PLAN_LIST)
    }

    override fun updatePlanDate(date: String?) {
        val msg: Message = Message.obtain()
        msg.what = UPDATE_DATE
        msg.obj = date
        handler.sendMessage(msg)
    }

    override fun scrollDateList(index: Int) {
        val msg: Message = Message.obtain()
        msg.arg1 = index
        msg.what = SCROLL_DATE_LIST
        handler.sendMessage(msg)
    }

    override fun updatePlanDateMenu(list: ArrayList<PlanListDateMenuItemBean>?) {
        dateMenuDataList!!.clear()
        dateMenuDataList!!.addAll(list!!)
        handler.sendEmptyMessage(UPDATE_PLAN_DATE_MENU)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_date -> {
                showMonthMenuDialog()
            }
            R.id.tv_today -> {
                val calendar: Calendar = Calendar.getInstance()
                year = calendar.get(Calendar.YEAR)
                month = calendar.get(Calendar.MONTH) + 1
                day = calendar.get(Calendar.DAY_OF_MONTH)
                presenter!!.setPlanList(year, month, day)
                tvToday!!.visibility = View.GONE
                dateButtonParams!!.width =
                    screenWidth - addPlan!!.measuredWidth - dip2px(this, 60f)
                tvDate!!.layoutParams = dateButtonParams
            }
            R.id.fab_add_plan -> {
                val intent = Intent(this, AddPlanActivity::class.java)
                startActivity(intent)
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
        val weekArr =
            arrayOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月")
        val monthAdapter = BillPlanDateMenuMonthMenuGridViewAdapter(this, weekArr)
        gvMonth.adapter = monthAdapter
        gvMonth.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                month = position + 1
                day = 1
                presenter!!.setPlanList(year, month, day)
                changeDateDialog!!.dismiss()
                if (!TimeUtil.isToday(year, month, day)) {
                    tvToday!!.visibility = View.VISIBLE
                }
            }
        builder.setView(view)
        changeDateDialog = builder.create()
        changeDateDialog!!.setCanceledOnTouchOutside(false)
        changeDateDialog!!.setCancelable(false)
        changeDateDialog!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SET_ALARM_RESULT_CODE && resultCode == RESULT_OK) {
            val alarmTime = data!!.getIntExtra("AlarmTime", -1)
            val bean = allPlans!![needAddAlarmPlanIndex]
            if (alarmTime >= 0) {
                presenter!!.addAlarm(this, bean, alarmTime)
            }
        }
    }

    private class MyHandler(activity: PlanListActivity) : Handler() {
        private val mActivity: WeakReference<PlanListActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity: PlanListActivity = mActivity.get()!!
            when (msg.what) {
                HIDE_WAIT_DIALOG -> {
                    if (activity.waitDialog!!.isShowing) {
                        activity.waitDialog!!.dismiss()
                    }
                }
                UPDATE_WAIT_DIALOG -> {
                    if (activity.waitDialog!!.isShowing) {
                        activity.waitDialog!!.setMessage((msg.obj as String))
                    }
                }
                SHOW_SNACK_BAR -> {
                    Snackbar.make(
                        activity.rvDateMenu!!,
                        (msg.obj as String), Snackbar.LENGTH_SHORT
                    ).show()
                }
                UPDATE_DATE -> {
                    activity.tvDate!!.text = msg.obj as CharSequence?
                }
                UPDATE_PLAN_DATE_MENU -> {
                    activity.dateMenuAdapter!!.notifyDataSetChanged()
                }
                UPDATE_PLAN_LIST -> {
                    if (activity.allPlans!!.size == 0) {
                        activity.rvPlans!!.visibility = View.GONE
                        activity.tvNoPlan!!.visibility = View.VISIBLE
                    } else {
                        activity.rvPlans!!.visibility = View.VISIBLE
                        activity.tvNoPlan!!.visibility = View.GONE
                    }
                    activity.planListPlanRecyclerViewAdapter!!.notifyDataSetChanged()
                }
                SCROLL_DATE_LIST -> {
                    activity.rvDateMenu!!.scrollToPosition(msg.arg1)
                }
                else -> {
                }
            }
        }

    }

    companion object {
        /**
         * 隐藏等待 Dialog
         */
        private const val HIDE_WAIT_DIALOG = 1000

        /**
         * 更新等待 Dialog
         */
        private const val UPDATE_WAIT_DIALOG = 2000

        /**
         * 显示 SnackBar
         */
        private const val SHOW_SNACK_BAR = 3000

        /**
         * 更新日期
         */
        private const val UPDATE_DATE = 4000

        /**
         * 更新日期列表
         */
        private const val UPDATE_PLAN_DATE_MENU = 5000

        /**
         * 更新未完成计划列表
         */
        private const val UPDATE_PLAN_LIST = 6000

        /**
         * 滚动日期RecyclerView
         */
        private const val SCROLL_DATE_LIST = 7000

        private const val SET_ALARM_RESULT_CODE = 8000

        fun dip2px(context: Context, dipValue: Float): Int {
            val scale: Float = context.resources.displayMetrics.density
            return (dipValue * scale + 0.5f).toInt()
        }
    }
}
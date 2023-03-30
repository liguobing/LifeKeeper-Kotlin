package com.lixyz.lifekeeperforkotlin.view.activity

import ando.widget.pickerview.builder.TimePickerBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.presenter.AddPlanPresenter
import com.lixyz.lifekeeperforkotlin.utils.TimeUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


/**
 * 添加计划 Activity
 *
 * @author LGB
 */
class AddPlanActivity : BaseActivity(), View.OnClickListener, IAddPlanView {
    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * Handler
     */
    private val handler = MyHandler(this)

    /**
     * Presenter
     */
    private var presenter: AddPlanPresenter? = null

    /**
     * 取消按钮
     */
    private var imgCancel: ImageView? = null

    /**
     * 保存按钮
     */
    private var imgSavePlan: ImageView? = null

    /**
     * 计划标题
     */
    private var etPlanTitle: EditText? = null

    /**
     * 全天事件布局
     */
    private var llAllDayLayout: LinearLayout? = null

    /**
     * 全天事件开关
     */
    private var swAllDay: Switch? = null

    /**
     * 开始日期布局
     */
    private var llStartDateLayout: LinearLayout? = null

    /**
     * 开始日期
     */
    private var tvStartDate: TextView? = null

    /**
     * 开始时间布局
     */
    private var llStartTimeLayout: LinearLayout? = null

    /**
     * 开始时间
     */
    private var tvStartTime: TextView? = null

    /**
     * 重复布局
     */
    private var llRepeatLayout: LinearLayout? = null

    /**
     * 重复
     */
    private var tvRepeat: TextView? = null

    /**
     * 结束重复布局
     */
    private var llRepeatOverLayout: LinearLayout? = null

    /**
     * 结束重复
     */
    private var tvRepeatOver: TextView? = null

    /**
     * 闹钟布局
     */
    private var llAlarmLayout: LinearLayout? = null

    /**
     * 闹钟时间
     */
    private var tvAlarm: TextView? = null

    /**
     * 闹钟开关布局
     */
    private var llAlarmSwitchLayout: LinearLayout? = null

    /**
     * 闹钟开关
     */
    private var swAlarmSwitch: Switch? = null

    /**
     * 计划地点
     */
    private var etPlanLocation: EditText? = null

    /**
     * 计划描述
     */
    private var etPlanDescription: EditText? = null

    /**
     * 开始日期,默认当前日期
     */
    private var startDate: Long = 0

    /**
     * 开始时间，默认当前时间
     */
    private var startTime: Long = 0

    /**
     * 重复模式，默认为一次性
     */
    private var repeatType = 0

    /**
     * 结束重复模式，默认为次数
     */
    private var endRepeatType = 1

    /**
     * 结束重复的值，默认为 30 次
     */
    private var endRepeatValue: Long = 30

    /**
     * 闹钟时间
     */
    private var alarmTime = -1

    /**
     * 保存计划是否成功，用户隐藏 WaitDialog 关闭 Activity
     */
    private var savePlanSuccessful = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.view___add_plan___main)
        initWidget()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        tvStartDate!!.text = presenter!!.getStartDate(startDate + startTime)
        tvStartTime!!.text = presenter!!.getStartTime(startDate + startTime)
    }

    private var allDayWarningSnackBar: Snackbar? = null

    override fun onClick(v: View) {
        hideSoftInput(imgSavePlan!!)
        when (v.id) {
            R.id.all_day_layout -> {
                swAllDay!!.isChecked = !swAllDay!!.isChecked
                if (swAllDay!!.isChecked) {
                    tvStartDate!!.text = presenter!!.getStartDate(startDate)
                    tvStartTime!!.text = resources.getString(R.string.AddPlanActivity8ClockOnTheDay)
                    startTime = 3600000 * 8.toLong()
                    allDayWarningSnackBar =
                        Snackbar.make(swAllDay!!, "全天事件的提醒是每个小时提醒一次哦~", Snackbar.LENGTH_INDEFINITE)
                            .setAction(
                                "知道了"
                            ) { allDayWarningSnackBar!!.dismiss() }
                    allDayWarningSnackBar!!.show()
                    allDayWarningSnackBar!!.setActionTextColor(Color.WHITE)
                    //通过 getView() 方法获取 SnackBar 的父布局
                    val view: View = allDayWarningSnackBar!!.view
                    //设置背景色
                    view.setBackgroundColor(resources.getColor(R.color.colorAccent, null))
                    //获取 SnackBar 左侧提示信息的控件，修改字体颜色
                    (view.findViewById(R.id.snackbar_text) as TextView).setTextColor(Color.WHITE)
                } else {
                    tvStartDate!!.text = presenter!!.getStartDate(startDate)
                    tvStartTime!!.text = TimeUtil.longToStringTime(System.currentTimeMillis())
                    startTime = TimeUtil.longToLongTime(System.currentTimeMillis())
                    allDayWarningSnackBar!!.dismiss()
                }
            }
            R.id.start_date_layout -> {
                val datePicker = TimePickerBuilder(
                    this
                ) { date, _ ->
                    startDate = TimeUtil.dateToLongDate(date)
                    tvStartDate!!.text = TimeUtil.dateToStringDate(date)
                    updateRepeatView()
                }.setCancelColor(Color.BLACK).setSubmitColor(Color.BLACK).setContentTextSize(20)
                    .setType(booleanArrayOf(true, true, true, false, false, false)).build()
                datePicker.show()
            }
            R.id.start_time_layout -> {
                val timePicker = TimePickerBuilder(
                    this
                ) { date, _ ->
                    startTime = TimeUtil.dateToLongTime(date)
                    tvStartTime!!.text = TimeUtil.dateToStringTime(date)
                }.setCancelColor(Color.BLACK).setSubmitColor(Color.BLACK).setTitleText("开始时间")
                    .setContentTextSize(20)
                    .setType(booleanArrayOf(false, false, false, true, true, false)).build()
                timePicker.show()
            }
            R.id.repeat_layout -> {
                val repeatIntent = Intent(this, AddPlanRepeatActivity::class.java)
                repeatIntent.putExtra("StartTime", startDate + startTime)
                startActivityForResult(repeatIntent, 100)
            }
            R.id.repeat_over_layout -> {
                val repeatOverIntent = Intent(this, AddPlanRepeatOverActivity::class.java)
                startActivityForResult(repeatOverIntent, 200)
            }
            R.id.alarm_layout -> {
                val alarmIntent = Intent(this, AddPlanAlarmActivity::class.java)
                startActivityForResult(alarmIntent, 300)
            }
            R.id.alarm_switch_layout -> {
                swAlarmSwitch!!.isChecked = !swAlarmSwitch!!.isChecked
                if (swAlarmSwitch!!.isChecked) {
                    alarmTime = 0
                    tvAlarm!!.text = "计划开始时"
                } else {
                    alarmTime = -1
                    tvAlarm!!.text = "不提醒"
                }
            }
            R.id.main_img_cancel -> finish()
            R.id.main_img_save_plan -> presenter!!.savePlan(
                this,
                swAllDay!!.isChecked,
                etPlanTitle!!.text,
                etPlanDescription!!.text,
                etPlanLocation!!.text,
                startDate + startTime,
                repeatType,
                endRepeatType,
                endRepeatValue,
                alarmTime
            )
            else -> {
            }
        }
    }

    override fun initWidget() {
        waitDialog = CustomDialog(this, this, "请稍后...")
        presenter = AddPlanPresenter(this)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)
        startTime = hour * 3600000 + minute * 60000.toLong()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        startDate = calendar.timeInMillis
        imgCancel = findViewById(R.id.main_img_cancel)
        imgSavePlan = findViewById(R.id.main_img_save_plan)
        etPlanTitle = findViewById(R.id.et_plan_title)
        llAllDayLayout = findViewById(R.id.all_day_layout)
        swAllDay = findViewById(R.id.sw_all_day)
        llStartDateLayout = findViewById(R.id.start_date_layout)
        tvStartDate = findViewById(R.id.tv_start_date)
        llStartTimeLayout = findViewById(R.id.start_time_layout)
        tvStartTime = findViewById(R.id.tv_start_time)
        llRepeatLayout = findViewById(R.id.repeat_layout)
        tvRepeat = findViewById(R.id.tv_repeat)
        llRepeatOverLayout = findViewById(R.id.repeat_over_layout)
        tvRepeatOver = findViewById(R.id.tv_repeat_over)
        llAlarmLayout = findViewById(R.id.alarm_layout)
        tvAlarm = findViewById(R.id.tv_alarm)
        llAlarmSwitchLayout = findViewById(R.id.alarm_switch_layout)
        swAlarmSwitch = findViewById(R.id.sw_alarm_switch)
        etPlanLocation = findViewById(R.id.et_location)
        etPlanDescription = findViewById(R.id.et_description)
    }

    override fun initListener() {
        imgCancel!!.setOnClickListener(this)
        imgSavePlan!!.setOnClickListener(this)
        llAllDayLayout!!.setOnClickListener(this)
        llStartTimeLayout!!.setOnClickListener(this)
        llStartDateLayout!!.setOnClickListener(this)
        llRepeatLayout!!.setOnClickListener(this)
        llRepeatOverLayout!!.setOnClickListener(this)
        llAlarmLayout!!.setOnClickListener(this)
        llAlarmSwitchLayout!!.setOnClickListener(this)
        waitDialog!!.setOnDismissListener {
            if (savePlanSuccessful) {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            repeatType = data!!.getIntExtra("RepeatTypeIndex", -1)
            updateRepeatView()
            if (repeatType == 0) {
                llRepeatOverLayout!!.visibility = View.GONE
            } else {
                llRepeatOverLayout!!.visibility = View.VISIBLE
                endRepeatValue = 30
            }
        }
        if (requestCode == 200 && resultCode == RESULT_OK) {
            endRepeatType = data!!.getIntExtra("EndPlanType", -1)
            if (endRepeatType == 0) {
                val date: Date = data.getSerializableExtra("EndPlanTime") as Date
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                tvRepeatOver!!.text = java.lang.String.format(
                    Locale.CHINA,
                    "%s 结束",
                    format.format(date)
                )
                endRepeatValue = date.time
            } else if (endRepeatType == 1) {
                endRepeatValue = data.getIntExtra("ExecutePlanCount", -1).toLong()
                tvRepeatOver!!.text = java.lang.String.format(
                    Locale.CHINA,
                    "共执行 %d 次",
                    endRepeatValue
                )
            }
        }
        if (requestCode == 300 && resultCode == RESULT_OK) {
            tvAlarm!!.text = data!!.getStringExtra("AlarmText")
            alarmTime = data.getIntExtra("AlarmTime", -1)
            swAlarmSwitch!!.isChecked = alarmTime >= 0
        }
    }

    private fun updateRepeatView() {
        val weeks = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate + startTime
        when (repeatType) {
            0 -> tvRepeat!!.text = "一次性活动"
            1 -> tvRepeat!!.text = "每天"
            2 -> tvRepeat!!.text = java.lang.String.format(
                Locale.CHINA, "每周(每周的%s)",
                weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]
            )
            3 -> tvRepeat!!.text = java.lang.String.format(
                Locale.CHINA,
                "每月(每月的%d日)",
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            4 -> tvRepeat!!.text = java.lang.String.format(
                Locale.CHINA,
                "每年(%d月%d日)",
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            else -> {
            }
        }
    }

    override fun showSnackBar(message: String?) {
        val msg: Message = Message.obtain()
        msg.what = UPDATE_SNACK_BAR
        msg.obj = message
        handler.sendMessage(msg)
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
        msg.obj = message
        msg.what = UPDATE_WAIT_DIALOG
        handler.sendMessage(msg)
    }

    override fun saveSuccessful() {
        savePlanSuccessful = true
        handler.sendEmptyMessage(SAVE_PLAN_SUCCESSFUL)
    }

    /**
     * 隐藏软键盘
     */
    private fun hideSoftInput(view: View) {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private class MyHandler(activity: AddPlanActivity) : Handler() {
        private val mActivity: WeakReference<AddPlanActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity: AddPlanActivity = mActivity.get()!!
            when (msg.what) {
                UPDATE_SNACK_BAR -> {
                    Snackbar.make(
                        activity.imgSavePlan!!,
                        (msg.obj as String), Snackbar.LENGTH_SHORT
                    ).show()
                }
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
                SAVE_PLAN_SUCCESSFUL -> {
                    if (activity.waitDialog!!.isShowing) {
                        activity.waitDialog!!.setSuccessful()
                    }
                }
                else -> {
                }
            }
        }

    }

    companion object {
        /**
         * 显示 SnackBar
         */
        private const val UPDATE_SNACK_BAR = 10000

        /**
         * 隐藏 WaitDialog
         */
        private const val HIDE_WAIT_DIALOG = 20000

        /**
         * 更新 WaitDialog
         */
        private const val UPDATE_WAIT_DIALOG = 30000

        /**
         * 保存计划成功
         */
        private const val SAVE_PLAN_SUCCESSFUL = 40000
    }
}
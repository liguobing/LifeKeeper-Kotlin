package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanBean
import com.lixyz.lifekeeperforkotlin.model.AddPlanModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.activity.IAddPlanView
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


/**
 * 添加计划 Presenter
 *
 * @author LGB
 */
class AddPlanPresenter(
    /**
     * View
     */
    private val view: IAddPlanView
) {
    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("添加计划线程池"))

    /**
     * 星期选项
     */
    private val weeks = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    /**
     * Model
     */
    private val model: AddPlanModel = AddPlanModel()

    /**
     * 根据 long 值时间获取年月日
     *
     * @param startTime long 值时间
     * @return 年月日
     */
    fun getStartDate(startTime: Long): String {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val year: String = java.lang.String.valueOf(calendar.get(Calendar.YEAR))
        val month =
            if (calendar.get(Calendar.MONTH) + 1 > 9) java.lang.String.valueOf(calendar.get(Calendar.MONTH) + 1) else "0" + (calendar.get(
                Calendar.MONTH
            ) + 1)
        val day = if (calendar.get(Calendar.DAY_OF_MONTH) > 9) java.lang.String.valueOf(
            calendar.get(Calendar.DAY_OF_MONTH)
        ) else "0" + calendar.get(Calendar.DAY_OF_MONTH)
        val week = weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        return year + "年" + month + "月" + day + "日 " + week
    }

    /**
     * 根据 long 值获取时分
     *
     * @param startTime long 值时间
     * @return 时分
     */
    fun getStartTime(startTime: Long): String {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val hour = if (calendar.get(Calendar.HOUR_OF_DAY) > 9) java.lang.String.valueOf(
            calendar.get(Calendar.HOUR_OF_DAY)
        ) else "0" + calendar.get(Calendar.HOUR_OF_DAY)
        val minute =
            if (calendar.get(Calendar.MINUTE) > 9) java.lang.String.valueOf(calendar.get(Calendar.MINUTE)) else "0" + calendar.get(
                Calendar.MINUTE
            )
        return "$hour:$minute"
    }

    /**
     * 保存计划
     *
     * @param context         Context
     * @param isAllDay        是否是全天计划
     * @param planTitle       计划标题
     * @param planDescription 计划描述
     * @param planLocation    计划地点
     * @param startTime       开始时间
     * @param repeatType      重复模式：单次计划/每日计划/每周计划/每月计划/每年计划
     * @param endRepeatType   结束重复模式：时间/次数
     * @param endRepeatValue  结束重复值
     * @param alarmTime       提醒时间
     */
    fun savePlan(
        context: Context,
        isAllDay: Boolean,
        planTitle: Editable,
        planDescription: Editable,
        planLocation: Editable,
        startTime: Long,
        repeatType: Int,
        endRepeatType: Int,
        endRepeatValue: Long,
        alarmTime: Int
    ) {
        if (TextUtils.isEmpty(planTitle)) {
            view.showSnackBar("标题不能为空")
        } else {
            if (endRepeatType == 0 && endRepeatValue < startTime) {
                view.showSnackBar("结束时间不能大于开始时间")
            } else {
                view.showWaitDialog()
                threadPool.execute {
                    var list: List<PlanBean> = ArrayList()
                    when (repeatType) {
                        0 -> list = getOneTimePlanList(
                            context,
                            isAllDay,
                            planTitle.toString().trim(),
                            planDescription.toString().trim(),
                            planLocation.toString().trim(),
                            startTime,
                            alarmTime
                        )
                        1 -> list = getDailyPlanList(
                            context,
                            isAllDay,
                            planTitle.toString().trim { it <= ' ' },
                            planDescription.toString().trim { it <= ' ' },
                            planLocation.toString().trim { it <= ' ' },
                            startTime,
                            endRepeatType,
                            endRepeatValue,
                            alarmTime
                        )
                        2 -> list = getWeeklyPlanList(
                            context,
                            isAllDay,
                            planTitle.toString().trim { it <= ' ' },
                            planDescription.toString().trim { it <= ' ' },
                            planLocation.toString().trim { it <= ' ' },
                            startTime,
                            endRepeatType,
                            endRepeatValue,
                            alarmTime
                        )
                        3 -> list = getMonthlyPlanList(
                            context,
                            isAllDay,
                            planTitle.toString().trim { it <= ' ' },
                            planDescription.toString().trim { it <= ' ' },
                            planLocation.toString().trim { it <= ' ' },
                            startTime,
                            endRepeatType,
                            endRepeatValue,
                            alarmTime
                        )
                        4 -> list = getYearlyPlanList(
                            context,
                            isAllDay,
                            planTitle.toString().trim { it <= ' ' },
                            planDescription.toString().trim { it <= ' ' },
                            planLocation.toString().trim { it <= ' ' },
                            startTime,
                            endRepeatType,
                            endRepeatValue,
                            alarmTime
                        )
                    }
                    val addResult = model.addPlan( list)
                    if (addResult) {
                        view.saveSuccessful()
                    } else {
                        view.hideWaitDialog()
                        view.showSnackBar("保存出错，请稍后重试")
                    }
                }
            }
        }
    }

    /**
     * 获取一次性计划列表
     *
     * @param context         Context
     * @param isAllDay        是否是全天计划
     * @param planTitle       计划标题
     * @param planDescription 计划描述
     * @param planLocation    计划地点
     * @param startTime       开始时间
     * @param alarmTime       提醒时间
     * @return 计划列表
     */
    private fun getOneTimePlanList(
        context: Context,
        isAllDay: Boolean,
        planTitle: String,
        planDescription: String,
        planLocation: String,
        startTime: Long,
        alarmTime: Int
    ): List<PlanBean> {
        val planBean = PlanBean()
        planBean.objectId = StringUtil.getRandomString()
        planBean.planId = StringUtil.getRandomString()
        planBean.groupId = StringUtil.getRandomString()
        planBean.isAllDay = if (isAllDay) 1 else -1
        planBean.planName = planTitle
        planBean.planDescription = planDescription
        planBean.planLocation = planLocation
        planBean.planUser = model.getUserId(context)
        planBean.startTime = startTime
        planBean.repeatType = 0
        planBean.endRepeatType = -1
        planBean.endRepeatValue = -1
        planBean.alarmTime = alarmTime
        planBean.isFinished = -1
        planBean.planStatus = 1
        planBean.planType = 0
        planBean.createTime = System.currentTimeMillis()
        planBean.updateTime = 0
        planBean.finishTime = 0
        val list: MutableList<PlanBean> = ArrayList()
        list.add(planBean)
        return list
    }

    /**
     * 获取每日计划列表
     *
     * @param context         Context
     * @param isAllDay        全天计划
     * @param planTitle       计划标题
     * @param planDescription 计划描述
     * @param planLocation    计划地点
     * @param startTime       开始时间
     * @param endRepeatType   结束重复模式：时间/次数
     * @param endRepeatValue  结束重复值
     * @param alarmTime       提醒时间
     * @return 计划列表
     */
    private fun getDailyPlanList(
        context: Context,
        isAllDay: Boolean,
        planTitle: String,
        planDescription: String,
        planLocation: String,
        startTime: Long,
        endRepeatType: Int,
        endRepeatValue: Long,
        alarmTime: Int
    ): List<PlanBean> {
        val list: MutableList<PlanBean> = ArrayList()
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val groupId: String = StringUtil.getRandomString()
        if (endRepeatType == 0) {
            do {
                val planBean = PlanBean()
                planBean.objectId = StringUtil.getRandomString()
                planBean.planId = StringUtil.getRandomString()
                planBean.groupId = groupId
                planBean.isAllDay = if (isAllDay) 1 else -1
                planBean.planName = planTitle
                planBean.planDescription = planDescription
                planBean.planLocation = planLocation
                planBean.planUser = model.getUserId(context)
                planBean.startTime = calendar.timeInMillis
                planBean.alarmTime = alarmTime
                planBean.repeatType = 1
                planBean.endRepeatType = 0
                planBean.endRepeatValue = endRepeatValue
                planBean.isFinished = -1
                planBean.planStatus = 1
                planBean.planType = 0
                planBean.createTime = System.currentTimeMillis()
                planBean.updateTime = 0
                planBean.finishTime = 0
                list.add(planBean)
                calendar.add(Calendar.DATE, 1) //增加一天
            } while (calendar.timeInMillis <= endRepeatValue)
        } else {
            for (i in 0 until endRepeatValue) {
                val planBean = PlanBean()
                planBean.objectId = StringUtil.getRandomString()
                planBean.planId = StringUtil.getRandomString()
                planBean.groupId = groupId
                planBean.isAllDay = if (isAllDay) 1 else -1
                planBean.planName = planTitle
                planBean.planDescription = planDescription
                planBean.planLocation = planLocation
                planBean.planUser = model.getUserId(context)
                planBean.startTime = calendar.timeInMillis
                planBean.repeatType = 1
                planBean.endRepeatType = 1
                planBean.endRepeatValue = endRepeatValue
                planBean.alarmTime = alarmTime
                planBean.isFinished = -1
                planBean.planStatus = 1
                planBean.planType = 0
                planBean.createTime = System.currentTimeMillis()
                planBean.updateTime = 0
                planBean.finishTime = 0
                list.add(planBean)
                calendar.add(Calendar.DATE, 1) //增加一天
            }
        }
        return list
    }

    /**
     * 获取每周计划列表
     *
     * @param context         Context
     * @param isAllDay        全天计划
     * @param planTitle       计划标题
     * @param planDescription 计划描述
     * @param planLocation    计划地点
     * @param startTime       开始时间
     * @param endRepeatType   结束重复模式：时间/次数
     * @param endRepeatValue  结束重复值
     * @param alarmTime       提醒时间
     * @return 计划列表
     */
    private fun getWeeklyPlanList(
        context: Context,
        isAllDay: Boolean,
        planTitle: String,
        planDescription: String,
        planLocation: String,
        startTime: Long,
        endRepeatType: Int,
        endRepeatValue: Long,
        alarmTime: Int
    ): List<PlanBean> {
        val list: MutableList<PlanBean> = ArrayList()
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val groupId: String = StringUtil.getRandomString()
        if (endRepeatType == 0) {
            do {
                val planBean = PlanBean()
                planBean.objectId = StringUtil.getRandomString()
                planBean.planId = StringUtil.getRandomString()
                planBean.groupId = groupId
                planBean.isAllDay = if (isAllDay) 1 else -1
                planBean.planName = planTitle
                planBean.planDescription = planDescription
                planBean.planLocation = planLocation
                planBean.planUser = model.getUserId(context)
                planBean.startTime = calendar.timeInMillis
                planBean.repeatType = 2
                planBean.endRepeatType = 0
                planBean.endRepeatValue = endRepeatValue
                planBean.alarmTime = alarmTime
                planBean.isFinished = -1
                planBean.planStatus = 1
                planBean.planType = 0
                planBean.createTime = System.currentTimeMillis()
                planBean.updateTime = 0
                planBean.finishTime = 0
                list.add(planBean)
                calendar.add(Calendar.DATE, 7) //增加一周
            } while (calendar.timeInMillis <= endRepeatValue)
        } else {
            for (i in 0 until endRepeatValue) {
                val planBean = PlanBean()
                planBean.objectId = StringUtil.getRandomString()
                planBean.planId = StringUtil.getRandomString()
                planBean.groupId = groupId
                planBean.isAllDay = if (isAllDay) 1 else -1
                planBean.planName = planTitle
                planBean.planDescription = planDescription
                planBean.planLocation = planLocation
                planBean.planUser = model.getUserId(context)
                planBean.startTime = calendar.timeInMillis
                planBean.repeatType = 2
                planBean.endRepeatType = 1
                planBean.endRepeatValue = endRepeatValue
                planBean.alarmTime = alarmTime
                planBean.isFinished = -1
                planBean.planStatus = 1
                planBean.planType = 0
                planBean.createTime = System.currentTimeMillis()
                planBean.updateTime = 0
                planBean.finishTime = 0
                list.add(planBean)
                calendar.add(Calendar.DATE, 7) //增加一周
            }
        }
        return list
    }

    /**
     * 获取月度计划列表
     *
     * @param context         Context
     * @param isAllDay        全天计划
     * @param planTitle       计划标题
     * @param planDescription 计划描述
     * @param planLocation    计划地点
     * @param startTime       开始时间
     * @param endRepeatType   结束重复模式：时间/次数
     * @param endRepeatValue  结束重复值
     * @param alarmTime       提醒时间
     * @return 计划列表
     */
    private fun getMonthlyPlanList(
        context: Context,
        isAllDay: Boolean,
        planTitle: String,
        planDescription: String,
        planLocation: String,
        startTime: Long,
        endRepeatType: Int,
        endRepeatValue: Long,
        alarmTime: Int
    ): List<PlanBean> {
        val list: MutableList<PlanBean> = ArrayList()
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val groupId: String = StringUtil.getRandomString()
        if (endRepeatType == 0) {
            do {
                val planBean = PlanBean()
                planBean.objectId = StringUtil.getRandomString()
                planBean.planId = StringUtil.getRandomString()
                planBean.groupId = groupId
                planBean.isAllDay = if (isAllDay) 1 else -1
                planBean.planName = planTitle
                planBean.planDescription = planDescription
                planBean.planLocation = planLocation
                planBean.planUser = model.getUserId(context)
                planBean.startTime = calendar.timeInMillis
                planBean.repeatType = 3
                planBean.endRepeatType = 0
                planBean.endRepeatValue = endRepeatValue
                planBean.alarmTime = alarmTime
                planBean.isFinished = -1
                planBean.planStatus = 1
                planBean.planType = 0
                planBean.createTime = System.currentTimeMillis()
                planBean.updateTime = 0
                planBean.finishTime = 0
                list.add(planBean)
                calendar.add(Calendar.MONTH, 1) //增加一月
            } while (calendar.timeInMillis <= endRepeatValue)
        } else {
            for (i in 0 until endRepeatValue) {
                val planBean = PlanBean()
                planBean.objectId = StringUtil.getRandomString()
                planBean.planId = StringUtil.getRandomString()
                planBean.groupId = groupId
                planBean.isAllDay = if (isAllDay) 1 else -1
                planBean.planName = planTitle
                planBean.planDescription = planDescription
                planBean.planLocation = planLocation
                planBean.planUser = model.getUserId(context)
                planBean.startTime = calendar.timeInMillis
                planBean.repeatType = 3
                planBean.endRepeatType = 1
                planBean.endRepeatValue = endRepeatValue
                planBean.alarmTime = alarmTime
                planBean.isFinished = -1
                planBean.planStatus = 1
                planBean.planType = 0
                planBean.createTime = System.currentTimeMillis()
                planBean.updateTime = 0
                planBean.finishTime = 0
                list.add(planBean)
                calendar.add(Calendar.MONTH, 1) //增加一周
            }
        }
        return list
    }

    /**
     * 获取年度计划列表
     *
     * @param context         Context
     * @param isAllDay        全天计划
     * @param planTitle       计划标题
     * @param planDescription 计划描述
     * @param planLocation    计划地点
     * @param startTime       开始时间
     * @param endRepeatType   结束重复模式：时间/次数
     * @param endRepeatValue  结束重复值
     * @param alarmTime       提醒时间
     * @return 计划列表
     */
    private fun getYearlyPlanList(
        context: Context,
        isAllDay: Boolean,
        planTitle: String,
        planDescription: String,
        planLocation: String,
        startTime: Long,
        endRepeatType: Int,
        endRepeatValue: Long,
        alarmTime: Int
    ): List<PlanBean> {
        val list: MutableList<PlanBean> = ArrayList()
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val groupId: String = StringUtil.getRandomString()
        if (endRepeatType == 0) {
            do {
                val planBean = PlanBean()
                planBean.objectId = StringUtil.getRandomString()
                planBean.planId = StringUtil.getRandomString()
                planBean.groupId = groupId
                planBean.isAllDay = if (isAllDay) 1 else -1
                planBean.planName = planTitle
                planBean.planDescription = planDescription
                planBean.planLocation = planLocation
                planBean.planUser = model.getUserId(context)
                planBean.startTime = calendar.timeInMillis
                planBean.repeatType = 4
                planBean.endRepeatType = 0
                planBean.endRepeatValue = endRepeatValue
                planBean.alarmTime = alarmTime
                planBean.isFinished = -1
                planBean.planStatus = 1
                planBean.planType = 0
                planBean.createTime = System.currentTimeMillis()
                planBean.updateTime = 0
                planBean.finishTime = 0
                list.add(planBean)
                calendar.add(Calendar.YEAR, 1) //增加一月
            } while (calendar.timeInMillis <= endRepeatValue)
        } else {
            for (i in 0 until endRepeatValue) {
                val planBean = PlanBean()
                planBean.objectId = StringUtil.getRandomString()
                planBean.planId = StringUtil.getRandomString()
                planBean.groupId = groupId
                planBean.isAllDay = if (isAllDay) 1 else -1
                planBean.planName = planTitle
                planBean.planDescription = planDescription
                planBean.planLocation = planLocation
                planBean.planUser = model.getUserId(context)
                planBean.startTime = calendar.timeInMillis
                planBean.repeatType = 4
                planBean.endRepeatType = 1
                planBean.endRepeatValue = endRepeatValue
                planBean.alarmTime = alarmTime
                planBean.isFinished = -1
                planBean.planStatus = 1
                planBean.planType = 0
                planBean.createTime = System.currentTimeMillis()
                planBean.updateTime = 0
                planBean.finishTime = 0
                list.add(planBean)
                calendar.add(Calendar.YEAR, 1) //增加一周
            }
        }
        return list
    }

}
package com.lixyz.lifekeeperforkotlin.presenter

import android.content.Context
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanBean
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanListDateMenuItemBean
import com.lixyz.lifekeeperforkotlin.model.PlanListModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.utils.TimeUtil
import com.lixyz.lifekeeperforkotlin.view.activity.IPlanListView
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor


/**
 * PlanListActivity Presenter
 *
 * @author LGB
 */
class PlanListPresenter(
    /**
     * View
     */
    private val view: IPlanListView
) {
    /**
     * Model
     */
    private val model: PlanListModel = PlanListModel()

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("计划列表线程池"))

    /**
     * 星期英文选项
     */
    private val weeksEnglish = arrayOf("Sun.", "Mon.", "Tue.", "Wed.", "Thu.", "Fri.", "Sat.")

    /**
     * 星期中文选项
     */
    private val weeksChina = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")

    /**
     * 添加提醒的日期
     */
    private var addAlarmDay = 1

    private var currShowYear: Int? = null
    private var currShowMonth: Int? = null
    private var currShowDay: Int? = null
    private var dataMap: HashMap<Int, ArrayList<ArrayList<PlanBean>>>? = null

    /**
     * Activity 获取焦点
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    fun activityOnResume(context: Context, year: Int, month: Int, day: Int) {
        view.showWaitDialog()
        threadPool.execute {
            //更新日期 TextView
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val weekIndex: Int = calendar.get(Calendar.DAY_OF_WEEK)
            val monthStr = if (month < 10) "0$month" else month.toString() + ""
            val dayStr = if (day < 10) "0$day" else day.toString() + ""
            val date =
                year.toString() + "-" + monthStr + "-" + dayStr + " " + weeksChina[weekIndex - 1] + " "
            view.updatePlanDate(date)
            //更新日期菜单
            val dateMenuDataList: ArrayList<PlanListDateMenuItemBean> = ArrayList()
            val count = TimeUtil.getDaysOfMonth(year, month)
            for (i in 1..count) {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, i)
                val index: Int = calendar.get(Calendar.DAY_OF_WEEK)
                val bean = PlanListDateMenuItemBean()
                bean.day = i
                bean.week = weeksEnglish[index - 1]
                bean.isClick = i == day
                dateMenuDataList.add(bean)
            }
            view.updatePlanDateMenu(dateMenuDataList)
            view.scrollDateList(day - 1)
            //更新计划列表
            dataMap = model.getPlanByMonth(context, model.getUserId(context), year, month)
            currShowYear = year
            currShowMonth = month
            currShowDay = day
            val planList: ArrayList<ArrayList<PlanBean>> = dataMap!![day]!!
            view.updatePlanList(planList)
            view.hideWaitDialog()
        }
    }

    fun selectNewDate(context: Context, year: Int, month: Int, day: Int) {
        currShowYear = year
        currShowMonth = month
        currShowDay = day
        threadPool.execute {
            //更新日期 TextView
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val weekIndex: Int = calendar.get(Calendar.DAY_OF_WEEK)
            val monthStr = if (month < 10) "0$month" else month.toString() + ""
            val dayStr = if (day < 10) "0$day" else day.toString() + ""
            val date =
                year.toString() + "-" + monthStr + "-" + dayStr + " " + weeksChina[weekIndex - 1] + " "
            view.updatePlanDate(date)
            //更新日期菜单
            val dateMenuDataList: ArrayList<PlanListDateMenuItemBean> = ArrayList()
            val count = TimeUtil.getDaysOfMonth(year, month)
            for (i in 1..count) {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, i)
                val index: Int = calendar.get(Calendar.DAY_OF_WEEK)
                val bean = PlanListDateMenuItemBean()
                bean.day = i
                bean.week = weeksEnglish[index - 1]
                bean.isClick = i == day
                dateMenuDataList.add(bean)
            }
            view.updatePlanDateMenu(dateMenuDataList)
            view.scrollDateList(day)
            //更新计划列表
            //如果是当前年月，直接从已缓存 map 中读取当天数据
            //如果不是当前年月，从网络请求
            if (currShowYear == year && currShowMonth == month) {
                view.updatePlanList(dataMap!![day])
            } else {
                //更新计划列表
                dataMap = model.getPlanByMonth(context, model.getUserId(context), year, month)
                currShowYear = year
                currShowMonth = month
                currShowDay = day
                val planList: List<List<PlanBean>> = dataMap!![day]!!
                view.updatePlanList(planList)
            }
        }
    }

    fun setPlanFinish(context: Context, planBean: PlanBean) {
        view.showWaitDialog()
        threadPool.execute {
            val newObjectId = StringUtil.getRandomString()
            val updateTime = System.currentTimeMillis()
            val finishResult = model.finishPlan(context, planBean, newObjectId, updateTime)
            if (finishResult) {
                val list = dataMap!![currShowDay]
                val unFinishedPlans = list!![0]
                val finishedPlans = list[1]
                val newPlan = PlanBean()
                newPlan.objectId = newObjectId
                newPlan.planId = planBean.planId
                newPlan.groupId = planBean.groupId
                newPlan.isAllDay = planBean.isAllDay
                newPlan.planName = planBean.planName
                newPlan.planDescription = planBean.planDescription
                newPlan.planLocation = planBean.planLocation
                newPlan.planUser = planBean.planUser
                newPlan.startTime = planBean.startTime
                newPlan.repeatType = planBean.repeatType
                newPlan.endRepeatType = planBean.endRepeatType
                newPlan.endRepeatValue = planBean.endRepeatValue
                newPlan.alarmTime = planBean.alarmTime
                newPlan.isFinished = 1
                newPlan.planStatus = planBean.planStatus
                newPlan.planType = planBean.planType
                newPlan.createTime = updateTime
                newPlan.updateTime = 0
                newPlan.finishTime = 0
                finishedPlans.add(newPlan)
                unFinishedPlans.remove(planBean)
                val allList = ArrayList<ArrayList<PlanBean>>()
                allList.add(unFinishedPlans)
                allList.add(finishedPlans)
                view.updatePlanList(allList)
            } else {
                view.showSnakeBar("出错了，请稍后重试...")
            }
            view.hideWaitDialog()
        }
    }

    fun setPlanUnFinish(context: Context, planBean: PlanBean) {
        view.showWaitDialog()
        threadPool.execute {
            val newObjectId = StringUtil.getRandomString()
            val updateTime = System.currentTimeMillis()
            val unFinishResult = model.unFinishPlan(context, planBean, newObjectId, updateTime)
            if (unFinishResult) {
                val list = dataMap!![currShowDay]
                val unFinishedPlans = list!![0]
                val finishedPlans = list[1]
                val newPlan = PlanBean()
                newPlan.objectId = newObjectId
                newPlan.planId = planBean.planId
                newPlan.groupId = planBean.groupId
                newPlan.isAllDay = planBean.isAllDay
                newPlan.planName = planBean.planName
                newPlan.planDescription = planBean.planDescription
                newPlan.planLocation = planBean.planLocation
                newPlan.planUser = planBean.planUser
                newPlan.startTime = planBean.startTime
                newPlan.repeatType = planBean.repeatType
                newPlan.endRepeatType = planBean.endRepeatType
                newPlan.endRepeatValue = planBean.endRepeatValue
                newPlan.alarmTime = planBean.alarmTime
                newPlan.isFinished = -1
                newPlan.planStatus = planBean.planStatus
                newPlan.planType = planBean.planType
                newPlan.createTime = updateTime
                newPlan.updateTime = 0
                newPlan.finishTime = 0
                unFinishedPlans.add(newPlan)
                finishedPlans.remove(planBean)
                val allList = ArrayList<ArrayList<PlanBean>>()
                allList.add(unFinishedPlans)
                allList.add(finishedPlans)
                view.updatePlanList(allList)
            } else {
                view.showSnakeBar("出错了，请稍后重试...")
            }
            view.hideWaitDialog()
        }
    }

    fun deleteSinglePlan(context: Context, planBean: PlanBean) {
        view.showWaitDialog()
        threadPool.execute {
            val deleteResult: Boolean = model.deleteSinglePlan(context, planBean)
            if (deleteResult) {
                val list = dataMap!![currShowDay]
                val unFinishedPlans = list!![0]
                val finishedPlans = list[1]
                unFinishedPlans.remove(planBean)
                val allList = ArrayList<ArrayList<PlanBean>>()
                allList.add(unFinishedPlans)
                allList.add(finishedPlans)
                view.updatePlanList(allList)
                view.showSnakeBar("删除成功")
            } else {
                view.showSnakeBar("删除出错，请稍后重试...")
            }
            view.hideWaitDialog()
        }
    }

    fun deleteGroupPlan(context: Context, planBean: PlanBean) {
        view.showWaitDialog()
        threadPool.execute {
            val deleteResult: Boolean = model.deleteGroupPlan(context, planBean)
            if (deleteResult) {
                dataMap = model.getPlanByMonth(
                    context,
                    model.getUserId(context),
                    currShowYear!!,
                    currShowMonth!!
                )
                val planList: ArrayList<ArrayList<PlanBean>> = dataMap!![currShowDay]!!
                view.updatePlanList(planList)
                view.showSnakeBar("删除成功")
            } else {
                view.showSnakeBar("删除出错，请稍后重试...")
            }
            view.hideWaitDialog()
        }
    }

    /**
     * 取消已设置的提醒
     *
     * @param planBean 要取消的计划对象
     */
    fun removeAlarm(context: Context, planBean: PlanBean) {
        view.showWaitDialog()
        threadPool.execute {
            try {
                val newObjectId = StringUtil.getRandomString()
                val updateTime = System.currentTimeMillis()
                val result = model.removeAlarm(context, planBean, newObjectId, updateTime)
                if (result) {
                    val list = dataMap!![currShowDay]
                    val unFinishedPlans = list!![0]
                    val finishedPlans = list[1]
                    val newPlan = PlanBean()
                    newPlan.objectId = newObjectId
                    newPlan.planId = planBean.planId
                    newPlan.groupId = planBean.groupId
                    newPlan.isAllDay = planBean.isAllDay
                    newPlan.planName = planBean.planName
                    newPlan.planDescription = planBean.planDescription
                    newPlan.planLocation = planBean.planLocation
                    newPlan.planUser = planBean.planUser
                    newPlan.startTime = planBean.startTime
                    newPlan.repeatType = planBean.repeatType
                    newPlan.endRepeatType = planBean.endRepeatType
                    newPlan.endRepeatValue = planBean.endRepeatValue
                    newPlan.alarmTime = -1
                    newPlan.isFinished = planBean.isFinished
                    newPlan.planStatus = planBean.planStatus
                    newPlan.planType = planBean.planType
                    newPlan.createTime = updateTime
                    newPlan.updateTime = 0
                    newPlan.finishTime = 0
                    unFinishedPlans[unFinishedPlans.indexOf(planBean)] = newPlan
                    val allList = ArrayList<ArrayList<PlanBean>>()
                    allList.add(unFinishedPlans)
                    allList.add(finishedPlans)
                    view.updatePlanList(allList)
                } else {
                    view.showSnakeBar("出错了，请稍后重试...")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            view.hideWaitDialog()
        }
    }

    /**
     * 添加提醒
     *
     * @param planBean 要取消的计划对象
     */
    fun addAlarm(context: Context, planBean: PlanBean, alarm: Int) {
        view.showWaitDialog()
        threadPool.execute {
            try {
                addAlarmDay = currShowDay!!
                val newObjectId = StringUtil.getRandomString()
                val updateTime = System.currentTimeMillis()
                val result = model.addAlarm(context, planBean, newObjectId, alarm, updateTime)
                if (result) {
                    val list = dataMap!![addAlarmDay]
                    val unFinishedPlans = list!![0]
                    val finishedPlans = list[1]
                    val newPlan = PlanBean()
                    newPlan.objectId = newObjectId
                    newPlan.planId = planBean.planId
                    newPlan.groupId = planBean.groupId
                    newPlan.isAllDay = planBean.isAllDay
                    newPlan.planName = planBean.planName
                    newPlan.planDescription = planBean.planDescription
                    newPlan.planLocation = planBean.planLocation
                    newPlan.planUser = planBean.planUser
                    newPlan.startTime = planBean.startTime
                    newPlan.repeatType = planBean.repeatType
                    newPlan.endRepeatType = planBean.endRepeatType
                    newPlan.endRepeatValue = planBean.endRepeatValue
                    newPlan.alarmTime = alarm
                    newPlan.isFinished = planBean.isFinished
                    newPlan.planStatus = planBean.planStatus
                    newPlan.planType = planBean.planType
                    newPlan.createTime = updateTime
                    newPlan.updateTime = 0
                    newPlan.finishTime = 0
                    unFinishedPlans[unFinishedPlans.indexOf(planBean)] = newPlan
                    val allList = ArrayList<ArrayList<PlanBean>>()
                    allList.add(unFinishedPlans)
                    allList.add(finishedPlans)
                    view.updatePlanList(allList)
                } else {
                    view.showSnakeBar("出错了，请稍后重试...")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            view.hideWaitDialog()
        }
    }

    fun setPlanList(year: Int, month: Int, day: Int) {
        try {
            //更新日期 TextView
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val weekIndex: Int = calendar.get(Calendar.DAY_OF_WEEK)
            val monthStr = if (month < 10) "0$month" else month.toString() + ""
            val dayStr = if (day < 10) "0$day" else day.toString() + ""
            val date =
                year.toString() + "-" + monthStr + "-" + dayStr + " " + weeksChina[weekIndex - 1] + " "
            view.updatePlanDate(date)
            //更新日期菜单
            val dateMenuDataList: ArrayList<PlanListDateMenuItemBean> = ArrayList()
            val count = TimeUtil.getDaysOfMonth(year, month)
            for (i in 1..count) {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, i)
                val index: Int = calendar.get(Calendar.DAY_OF_WEEK)
                val bean = PlanListDateMenuItemBean()
                bean.day = i
                bean.week = weeksEnglish[index - 1]
                bean.isClick = i == day
                dateMenuDataList.add(bean)
            }
            view.updatePlanDateMenu(dateMenuDataList)
            view.scrollDateList(day - 1)
            //更新计划列表
            if (dataMap != null) {
                currShowYear = year
                currShowMonth = month
                currShowDay = day
                val planList: ArrayList<ArrayList<PlanBean>> = dataMap!![day]!!
                view.updatePlanList(planList)
            } else {
                view.showSnakeBar("出错了，请稍后重试...")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
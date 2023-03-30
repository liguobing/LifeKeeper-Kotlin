package com.lixyz.lifekeeperforkotlin.bean.plan

import java.io.Serializable

/**
 * 计划原型
 *
 * @author LGB
 */
class PlanBean: Serializable {
    /**
     * ObjectId 唯一标识
     */
    var objectId: String? = null

    /**
     * 计划 ID
     */
    var planId: String? = null

    /**
     * 分组 ID
     */
    var groupId: String? = null

    /**
     * 是否是全天计划，全天计划没有提醒
     * 1：全天计划
     * -1：非全天计划
     */
    var isAllDay = 0

    /**
     * 计划名称
     */
    var planName: String? = null

    /**
     * 计划描述
     */
    var planDescription: String? = null

    /**
     * 计划地点
     */
    var planLocation: String? = null

    /**
     * 计划用户
     */
    var planUser: String? = null

    /**
     * 开始时间
     */
    var startTime: Long = 0

    /**
     * 重复模式
     * 0：一次性活动（不重复）
     * 1：每日计划
     * 2：每周计划
     * 3：每月计划
     * 4：每年计划
     */
    var repeatType = 0

    /**
     * 结束重复模式
     * 0：时间
     * 1：次数
     */
    var endRepeatType = 0

    /**
     * 结束重复值
     * 当 endRepeatType 为 0 时，该值代表结束时间
     * 当 endRepeatType 为 1 时，该值代表执行次数
     */
    var endRepeatValue: Long = 0

    /**
     * 提醒时间
     * -1：为不提醒
     * N：为 N 分钟前提醒
     */
    var alarmTime = 0

    /**
     * 是否已经完成
     * 1：已完成
     * -1：未完成
     */
    var isFinished = 0

    /**
     * 计划状态
     * 1：正常；
     * -1：非正常；
     */
    var planStatus = 0

    /**
     * 计划类型
     * 0：正常
     * 1：已删除
     * 2：已修改
     */
    var planType = 0

    /**
     * 创建时间
     */
    var createTime: Long = 0

    /**
     * 更新时间
     */
    var updateTime: Long = 0

    /**
     * 完成时间
     */
    var finishTime: Long = 0

}
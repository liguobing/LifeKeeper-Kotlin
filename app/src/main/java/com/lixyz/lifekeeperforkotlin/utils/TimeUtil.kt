package com.lixyz.lifekeeperforkotlin.utils

import java.text.SimpleDateFormat
import java.util.*

class TimeUtil {
    companion object {

        fun millisTimeToShortFormatString(time: Long): String? {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            val date = Date(time)
            return format.format(date)
        }

        fun getMonthStart(year: Int, month: Int): Long {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = month - 1
            calendar[Calendar.DAY_OF_MONTH] = 1
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar.timeInMillis
        }

        fun getMonthEnd(year: Int, month: Int): Long {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = month - 1
            calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DATE)
            calendar[Calendar.HOUR_OF_DAY] = 23
            calendar[Calendar.MINUTE] = 59
            calendar[Calendar.SECOND] = 59
            calendar[Calendar.MILLISECOND] = 999
            return calendar.timeInMillis
        }

        /**
         * 获取某年某月有多少天
         *
         * @param year  年份
         * @param month 月份
         * @return 一共有多少天
         */
        fun getDaysOfMonth(year: Int, month: Int): Int {
            var days = 0
            if (month != 2) {
                when (month) {
                    1, 3, 5, 7, 8, 10, 12 -> days = 31
                    4, 6, 9, 11 -> days = 30
                }
            } else {
                // 闰年
                days = if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) 29 else 28
            }
            return days
        }


        /**
         * 是否是当天
         *
         * @param year  年
         * @param month 月
         * @param day   日
         * @return 是否是当天
         */
        fun isToday(year: Int, month: Int, day: Int): Boolean {
            val calendar = Calendar.getInstance()
            val currYear = calendar[Calendar.YEAR]
            val currMonth = calendar[Calendar.MONTH] + 1
            val currDay = calendar[Calendar.DAY_OF_MONTH]
            return currYear == year && currMonth == month && currDay == day
        }

        /**
         * 传入一个 long 值，返回这个 long 值标识的时间（不包含日期）
         *
         * @param time long 值时间
         * @return 时间字符串
         */
        fun longToStringTime(time: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val hourStr: String = if (hour < 10) {
                "0$hour"
            } else {
                hour.toString() + ""
            }
            val minuteStr: String = if (minute < 10) {
                "0$minute"
            } else {
                minute.toString() + ""
            }
            return "$hourStr:$minuteStr"
        }

        /**
         * 传入一个 long 值，返回这个 long 值标识的时间（不包含日期）字符串
         *
         * @param time long 值时间
         * @return 时间 long 值
         */
        fun longToLongTime(time: Long): Long {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val hourLong = hour * 3600000.toLong()
            val minuteLong = minute * 60000.toLong()
            return hourLong + minuteLong
        }

        /**
         * Date 对象转 long date 值（日期值，不包含时间）
         *
         * @param date Date 对象
         * @return long 值
         */
        fun dateToLongDate(date: Date): Long {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar.timeInMillis
        }

        /**
         * Date 对象转日期字符串（年月日，不包含时间）
         *
         * @param date Date 对象
         * @return 日期字符串
         */
        fun dateToStringDate(date: Date): String {
            val weeks = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
            val calendar = Calendar.getInstance()
            calendar.time = date
            val year = java.lang.String.valueOf(calendar[Calendar.YEAR])
            val month =
                if (calendar[Calendar.MONTH] + 1 > 9) java.lang.String.valueOf(calendar[Calendar.MONTH] + 1) else "0" + (calendar[Calendar.MONTH] + 1)
            val day =
                if (calendar[Calendar.DAY_OF_MONTH] > 9) java.lang.String.valueOf(calendar[Calendar.DAY_OF_MONTH]) else "0" + calendar[Calendar.DAY_OF_MONTH]
            val week = weeks[calendar[Calendar.DAY_OF_WEEK] - 1]
            return year + "年" + month + "月" + day + "日" + week
        }

        /**
         * Date 对象转 long time 值（仅时分）
         *
         * @param date Date 对象
         * @return long 值
         */
        fun dateToLongTime(date: Date): Long {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            return (hour * 3600000 + minute * 60000).toLong()
        }

        /**
         * Date 对象转日期字符串（年月日，不包含时间）
         *
         * @param date Date 对象
         * @return 日期字符串
         */
        fun dateToStringTime(date: Date): String {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val hour =
                if (calendar[Calendar.HOUR_OF_DAY] > 9) java.lang.String.valueOf(calendar[Calendar.HOUR_OF_DAY]) else "0" + calendar[Calendar.HOUR_OF_DAY]
            val minute =
                if (calendar[Calendar.MINUTE] > 9) java.lang.String.valueOf(calendar[Calendar.MINUTE]) else "0" + calendar[Calendar.MINUTE]
            return "$hour:$minute"
        }

    }
}
package com.lixyz.lifekeeperforkotlin.utils

import android.graphics.Color
import com.github.mikephil.charting.utils.ColorTemplate




class ColorUtil {

    //测试 tag   哈哈哈哈哈哈
    //v6.0


    companion object{
        //以val最大取值90为例
        fun getColor(`val`: Float): Int {
            val one = (255 + 255) / 240.toFloat() //（255+255）除以最大取值的三分之二
            var r = 0
            var g = 0
            val b = 0
            if (`val` < 120) //第一个三等分
            {
                r = (one * `val`).toInt()
                g = 255
            } else if (`val` >= 120 && `val` < 240) //第二个三等分
            {
                r = 255
                g = 255 - ((`val` - 120) * one).toInt() //val减最大取值的三分之一
            } else {
                r = 255
            } //最后一个三等分
            return Color.rgb(r, g, b)
        }

        fun getPieChartItemColor(): ArrayList<Int> {
            val colors: ArrayList<Int> = ArrayList()
            for (c in ColorTemplate.VORDIPLOM_COLORS) {
                colors.add(c)
            }
            for (c in ColorTemplate.JOYFUL_COLORS) {
                colors.add(c)
            }
            for (c in ColorTemplate.COLORFUL_COLORS) {
                colors.add(c)
            }
            for (c in ColorTemplate.LIBERTY_COLORS) {
                colors.add(c)
            }
            for (c in ColorTemplate.PASTEL_COLORS) {
                colors.add(c)
            }
            return colors
        }
    }
}
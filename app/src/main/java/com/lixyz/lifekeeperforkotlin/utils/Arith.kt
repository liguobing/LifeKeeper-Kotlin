package com.lixyz.lifekeeperforkotlin.utils

import java.math.BigDecimal

/**
 * 由于Java的简单类型不能够精确的对浮点数进行运算，这个工具类提供精确的浮点数运算，包括加減乘除和四捨五入。
 */
class Arith {
    companion object {
        //默认吃吃饭运算精度
        private const val DEF_DIV_SCALE = 10

        /**
         * 提供精确的加法运算
         *
         * @param v1 被加数
         * @param v2 加数
         * @return 两个参数的和
         */
        fun add(v1: Float, v2: Float): Float {
            val b1 = BigDecimal(v1.toString())
            val b2 = BigDecimal(v2.toString())
            return b1.add(b2).toFloat()
        }

        /**
         * 提供精确的减法运算
         *
         * @param v1 被減数
         * @param v2 減数
         * @return 两个参数的差
         */
        fun sub(v1: Float, v2: Float): Float {
            val b1 = BigDecimal(v1.toString())
            val b2 = BigDecimal(v2.toString())
            return b1.subtract(b2).toFloat()
        }

        /**
         * 提供精确的乘法运算
         *
         * @param v1 被乘数
         * @param v2 乘数
         * @return 两个参数的积
         */
        fun mul(v1: Float, v2: Float): Float {
            val b1 = BigDecimal(v1.toString())
            val b2 = BigDecimal(v2.toString())
            return b1.multiply(b2).toFloat()
        }

        /**
         * 提供（相对）精确的除非运算，当发生除不尽的情况时，精确到小数点以后10位，以后的数字四舍五入
         *
         * @param v1 被除數
         * @param v2 除數
         * @return 兩個參數的商
         */
        fun div(v1: Float, v2: Float): Float {
            return div(v1, v2, DEF_DIV_SCALE)
        }

        /**
         * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入
         *
         * @param v1    被除數
         * @param v2    除數
         * @param scale 表示表示需要精確到小數點以後位数。
         * @return 兩個參數的商
         */
        private fun div(v1: Float, v2: Float, scale: Int): Float {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b1 = BigDecimal(v1.toString())
            val b2 = BigDecimal(v2.toString())
            return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toFloat()
        }

        /**
         * 提供精確的小數位四捨五入處理。
         * 提供精确的小数位四舍五入处理
         *
         * @param v     需要四捨五入的數位
         * @param scale 小數點後保留幾位
         * @return 四捨五入後的結果
         */
        fun round(v: Float, scale: Int): Float {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b = BigDecimal(v.toString())
            val one = BigDecimal("1")
            return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).toFloat()
        }
    }
}
package com.lixyz.lifekeeperforkotlin.utils

import android.content.Context

class DisplayUtil {
    companion object{
        /**
         * 将dip或dp值转换为px值，保证尺寸大小不变
         *
         * @param dipValue
         * @return
         */
        fun dip2px(context: Context, dipValue: Float): Float {
            val scale: Float = context.resources.displayMetrics.density
            return (dipValue * scale + 0.5f)
        }
    }
}
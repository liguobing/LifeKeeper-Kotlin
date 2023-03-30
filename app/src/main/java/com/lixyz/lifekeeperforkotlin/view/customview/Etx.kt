package com.lixyz.lifekeeperforkotlin.view.customview

fun <T> Boolean?.matchValue(valueTrue: T, valueFalse: T): T {
    return if (this == true) valueTrue else valueFalse
}
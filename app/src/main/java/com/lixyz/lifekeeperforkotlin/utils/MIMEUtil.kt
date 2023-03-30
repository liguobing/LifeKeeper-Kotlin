package com.lixyz.lifekeeperforkotlin.utils

import android.graphics.Bitmap

class MIMEUtil {
    companion object {
        fun getMIMEType(format: String): String {
            return when (format) {
                "PNG", "png" -> {
                    "image/png"
                }
                "JPG", "jpg", "JEPG", "jepg" -> {
                    "image/jpg"
                }
                "GIF", "gif" -> {
                    "image/gif"
                }
                else -> {
                    "image/png"
                }
            }
        }

        fun getFileFormat(format: String): Bitmap.CompressFormat {
            return when (format) {
                "PNG", "png" -> {
                    Bitmap.CompressFormat.PNG
                }
                "JPG", "jpg", "JEPG", "jepg" -> {
                    Bitmap.CompressFormat.JPEG
                }
                "GIF", "gif" -> {
                    Bitmap.CompressFormat.PNG
                }
                else -> {
                    Bitmap.CompressFormat.PNG
                }
            }
        }
    }
}
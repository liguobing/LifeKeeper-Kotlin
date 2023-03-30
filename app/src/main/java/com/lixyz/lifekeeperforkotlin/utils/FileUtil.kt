package com.lixyz.lifekeeperforkotlin.utils

import org.apache.commons.io.FilenameUtils




class FileUtil {
    companion object{
        /**
         * 获取文件后缀名
         */
        fun getFileFormat(fileName: String): String {
            return FilenameUtils.getExtension(fileName)
        }
    }
}
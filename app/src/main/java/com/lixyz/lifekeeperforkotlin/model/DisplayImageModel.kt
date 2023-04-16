package com.lixyz.lifekeeperforkotlin.model

import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.photo.MoveImageBean
import com.lixyz.lifekeeperforkotlin.bean.photo.PageImageBean
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.FileUtil
import com.lixyz.lifekeeperforkotlin.utils.MIMEUtil
import com.lixyz.lifekeeperforkotlin.utils.OKHttpUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.InputStream
import java.lang.reflect.Type


class DisplayImageModel {

    private fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
    }

    fun downloadImage(
        context: Context,
        imageBean: ImageBean
    ): Boolean {
        try {
            val client = OKHttpUtil.getInstance
            val file = File(
                Environment.DIRECTORY_PICTURES,
                imageBean.originalFileName!!
            )
            val url = "${Constant.CLOUD_ADDRESS}/LifeKeeper/resource/LifeKeeperImage/${imageBean.imageUser}/source/${imageBean.sourceFileName}"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val inputStream: InputStream = response.body!!.byteStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val values = ContentValues()
            values.put(
                MediaStore.Images.Media.DESCRIPTION,
                "LifeKeeper Download Image"
            )
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageBean.originalFileName)
            values.put(
                MediaStore.Images.Media.MIME_TYPE,
                MIMEUtil.getMIMEType(FileUtil.getFileFormat(imageBean.originalFileName!!))
            )
            values.put(
                MediaStore.Images.Media.TITLE,
                imageBean.originalFileName
            )
//            values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/$categoryName")
            val insertUri: Uri? =
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            bitmap.compress(
                MIMEUtil.getFileFormat(FileUtil.getFileFormat(imageBean.originalFileName!!)),
                100,
                context.contentResolver.openOutputStream(insertUri!!)
            )
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/gif",
                    "image/ief",
                    "image/jpeg",
                    "image/tiff",
                    "image/x-cmu-raster",
                    "image/x-ms-bmp",
                    "image/x-portable-anymap",
                    "image/x-portable-bitmap",
                    "image/x-portable-graymap",
                    "image/x-portable-pixmap",
                    "image/x-rgb",
                    "image/x-xbitmap",
                    "image/x-xpixmap",
                    "image/x-xwindowdump"
                )
            ) { _, _ ->

            }
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }


    fun deleteFile(context: Context, list: ArrayList<String>): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/DeleteImage")
            .post(
                Gson().toJson(list).toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .addHeader("Token", getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }


    fun setImageCategory(
        context: Context,
        moveImageBean: MoveImageBean
    ): Boolean {
        val request: Request = Request.Builder()
            .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/MoveImages")
            .post(
                Gson().toJson(moveImageBean)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .addHeader("Token", getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val str = response.body!!.string()
        val result = Gson().fromJson(str, NewResult::class.java)
        return if (result.result) {
            result.resultObject as Boolean
        } else {
            false
        }
    }

    fun getOtherCategory(
        context: Context,
        categoryId: String
    ): ArrayList<ImageCategoryBean> {
        try {
            val request: Request = Request.Builder()
                .url(
                    "${Constant.CLOUD_ADDRESS}/LifeKeeper/GetOtherImageCategory"
                )
                .addHeader("Token", getUserId(context)!!)
                .addHeader("CategoryId", categoryId)
                .build()
            val client = OKHttpUtil.getInstance
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
            return if (result.result) {
                val listType: Type = object : TypeToken<ArrayList<ImageCategoryBean>>() {}.type
                val gson = Gson()
                gson.fromJson(gson.toJson(result.resultObject), listType)
            } else {
                ArrayList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
    }

    fun moveImage(context: Context, moveImageBean: MoveImageBean): Boolean {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/MoveImages"
            )
            .post(
                Gson().toJson(moveImageBean)
                    .toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .addHeader("Token",getUserId(context)!!)
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val str = response.body!!.string()
        val result = Gson().fromJson(str, NewResult::class.java)
        return if (result.result) {
            result.resultObject as Boolean
        } else {
            false
        }
    }

    fun addNewCategory(context: Context, category: ImageCategoryBean): Boolean {
        val request: Request = Request.Builder()
            .url(
                "${Constant.CLOUD_ADDRESS}/LifeKeeper/AddImageCategory"
            )
            .addHeader("Token",getUserId(context)!!)
            .post(Gson().toJson(category)
                .toRequestBody("application/json; charset=UTF-8".toMediaType()))
            .build()
        val client = OKHttpUtil.getInstance
        val response = client.newCall(request).execute()
        val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
        return result.result
    }

    fun getImages(
        context: Context,
        categoryId: String,
        password: String
    ): PageImageBean? {
        try {
            val client = OKHttpUtil.getInstance
            val request =
                Request.Builder()
                    .url("https://www.li-xyz.com/LifeKeeper/GetImages?cp=0&ps=-1")
                    .addHeader("Token", getUserId(context)!!)
                    .addHeader("Password", password)
                    .addHeader("CategoryId", categoryId)
                    .build()
            val response = client.newCall(request).execute()
            val result = Gson().fromJson(response.body!!.string(), NewResult::class.java)
            return if (result.result) {
                val jsonString = Gson().toJson(result.resultObject)
                Gson().fromJson(JsonParser.parseString(jsonString), PageImageBean::class.java)
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }
}
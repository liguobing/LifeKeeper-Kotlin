package com.lixyz.lifekeeperforkotlin.presenter

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.WeChatRecordBean
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.WeChatRecordContactBean
import com.lixyz.lifekeeperforkotlin.model.WeChatRecordModel
import com.lixyz.lifekeeperforkotlin.net.CountingRequestBody
import com.lixyz.lifekeeperforkotlin.sql.SQLiteHelper
import com.lixyz.lifekeeperforkotlin.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class WeChatRecordViewModel : ViewModel() {
    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("微信通话录音线程池"))

    private val gson = Gson()

    private val model = WeChatRecordModel()

    var needUploadLiveData: MutableLiveData<Boolean>? = null
    var uploadDialogStateLiveData: MutableLiveData<Boolean>? = null
    var uploadDialogFileNameLiveData: MutableLiveData<String>? = null
    var uploadDialogProgressLiveData: MutableLiveData<Float>? = null
    var contactLiveData: MutableLiveData<ArrayList<WeChatRecordContactBean>>? = null
    var recordLiveData: MutableLiveData<ArrayList<WeChatRecordBean>>? = null
    var localFileLiveData: MutableLiveData<ArrayList<String>>? = null
    var waitDialogStateLiveData: MutableLiveData<Boolean>? = null
    var editableStateLiveData: MutableLiveData<Boolean>? = null

    init {

        needUploadLiveData = MutableLiveData()
        uploadDialogStateLiveData = MutableLiveData()
        uploadDialogFileNameLiveData = MutableLiveData()
        uploadDialogProgressLiveData = MutableLiveData()
        contactLiveData = MutableLiveData()
        recordLiveData = MutableLiveData()
        localFileLiveData = MutableLiveData()
        waitDialogStateLiveData = MutableLiveData()
        editableStateLiveData = MutableLiveData()
    }

    fun fileToSQLite(context: Context) {
        threadPool.execute {
            val database = SQLiteHelper(context, Constant.DB_NAME, null, 1).writableDatabase
            val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
            val dir = File("$parentPath/MIUI/sound_recorder/app_rec/")
            val files = dir.listFiles { pathname ->
                pathname.isFile
            }
            if (files != null && files.isNotEmpty()) {
                files.forEachIndexed { _, file ->
                    val cursor = database.query(
                        "WeChatRecord",
                        null,
                        "FileName = ?",
                        arrayOf(file.name),
                        null,
                        null,
                        null
                    )
                    if (cursor.count == 0) {
                        val value = ContentValues()
                        value.put("FileName", file.name)
                        value.put("ContactName", file.name.split("_")[0])
                        database.insert("WeChatRecord", null, value)
                    }
                    cursor.close()
                }
            }
            database.close()
        }
    }

    fun getContactNames(context: Context) {
        waitDialogStateLiveData!!.postValue(true)
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
                    .getString("UserId", null)
            if (userId != null) {
                val respList: ArrayList<WeChatRecordContactBean>? = model.getContactNames(userId)
                contactLiveData!!.postValue(respList)
            }
            waitDialogStateLiveData!!.postValue(false)
        }
    }

    fun deleteContact(context: Context, checkList: MutableList<String>) {
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
                    .getString("UserId", null)
            if (userId != null) {
                val result = model.deleteContact(userId, checkList)
                if (result) {
                    getContactNames(context)
                }
                waitDialogStateLiveData!!.postValue(false)
                editableStateLiveData!!.postValue(false)
            }
        }
    }

    fun getRecords(context: Context, contactId: String) {
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
                    .getString("UserId", null)
            if (userId != null) {
                val respList: ArrayList<WeChatRecordBean>? = model.getRecords(userId, contactId)
                recordLiveData!!.postValue(respList)
            }
        }
    }

    @SuppressLint("Range")
    fun deleteRecordFile(context: Context, checkedList: ArrayList<String>) {
        val selection: String = "ContactName IN ('" + TextUtils.join("','", checkedList) + "')"
        val helper = SQLiteHelper(context, "LifeKeeper.db", null, 1)
        val database = helper.writableDatabase
        database.beginTransaction()
        val cursor =
            database.query("WeChatRecord", arrayOf("FileName"), selection, null, null, null, null)
        if (cursor.count > 0) {
            cursor.moveToFirst()
            do {
                database.delete(
                    "WeChatRecord",
                    "FileName = ?",
                    arrayOf(cursor.getString(cursor.getColumnIndex("FileName")))
                )
                val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
                val dir = File("$parentPath/MIUI/sound_recorder/app_rec/")
                val file =
                    File("${dir.absoluteFile}/${cursor.getString(cursor.getColumnIndex("FileName"))}")
                file.delete()
            } while (cursor.moveToNext())
        }
        database.setTransactionSuccessful()
        database.endTransaction()
        cursor.close()
        database.close()
        helper.close()
        waitDialogStateLiveData!!.postValue(false)
        checkNeedUpload()
    }

    fun checkNeedUpload() {
        try {
            threadPool.execute {
                val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
                val dir = File("$parentPath/MIUI/sound_recorder/app_rec/")
                val files = dir.listFiles { pathname ->
                    pathname.isFile
                }
                if (files == null) {
                    needUploadLiveData!!.postValue(false)
                } else {
                    needUploadLiveData!!.postValue(files.isNotEmpty())
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    val lock = Any()

    @SuppressLint("Range")
    @Throws(Exception::class)
    fun uploadRecordFile(context: Context, checkedList: java.util.ArrayList<String>) {
        threadPool.execute {
            try {
                val database = SQLiteHelper(context, "LifeKeeper.db", null, 1).writableDatabase
                val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
                val dir = File("$parentPath/MIUI/sound_recorder/app_rec/")
                val client: OkHttpClient = OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .connectTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                    .readTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                    .writeTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                    .addNetworkInterceptor(StethoInterceptor())
                    .build()
                var index = 0
                checkedList.forEachIndexed { _, s ->
                    val cursor = database.query(
                        "WeChatRecord",
                        arrayOf("FileName"),
                        "FileName = ?",
                        arrayOf("微信录音 $s"),
                        null,
                        null,
                        null
                    )
                    if (cursor != null) {
                        if (cursor.count > 0) {
                            cursor.moveToFirst()
                            do {
                                val fileName =
                                    cursor.getString(cursor.getColumnIndex("FileName"))
                                val file = File("${dir.absolutePath}/$fileName")
                                if (file.exists()) {
                                    val requestBody = CountingRequestBody(
                                        MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart(
                                                "file",
                                                file.name,
                                                file.asRequestBody("application/octet-stream".toMediaType())
                                            )
                                            .addFormDataPart(
                                                "fileName",
                                                fileName
                                            ).build()
                                    ) { max, value ->
                                        val progress = value.toFloat() / max.toFloat()
                                        uploadDialogFileNameLiveData!!.postValue(s)
                                        uploadDialogProgressLiveData!!.postValue(progress)
                                    }
                                    val request = Request.Builder()
                                        .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UploadWeChatRecord")
                                        .addHeader("Token", getUserId(context)!!)
                                        .post(requestBody)
                                        .build()
                                    val response = client.newCall(request).execute()
                                    val str = response.body!!.string()
                                    val result = gson.fromJson(str, NewResult::class.java)
                                    if (result.result) {//如果上传成功，删除文件和sqlite数据
                                        database.delete(
                                            "WeChatRecord",
                                            "FileName = ?",
                                            arrayOf("微信录音 $s")
                                        )
                                        file.delete()
                                        index++
                                    } else {
                                        index++
                                    }
                                    if (index == checkedList.size) {
                                        uploadDialogStateLiveData!!.postValue(false)
                                    }
                                    checkNeedUpload()
                                }
                            } while (cursor.moveToNext())
                        }
                        cursor.close()
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                uploadDialogStateLiveData!!.postValue(false)
                MainScope().launch(Dispatchers.Main) {
                    Toast.makeText(context, "上传出错", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getLocalFileList() {
        val fileNameList = ArrayList<String>()
        val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
        val dir = File("$parentPath/MIUI/sound_recorder/app_rec/")
        val files = dir.listFiles { pathname ->
            pathname.isFile
        }
        files?.forEachIndexed { _, file ->
            val fileName = file.name.replace("微信录音 ", "")
            fileNameList.add(fileName)
        }
        localFileLiveData!!.postValue(fileNameList)
    }

    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", Context.MODE_PRIVATE)
            .getString("UserId", null)
    }
}
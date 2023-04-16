package com.lixyz.lifekeeperforkotlin.presenter

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Environment
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.NewResult
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.ContactBean
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.RecordBean
import com.lixyz.lifekeeperforkotlin.model.RecordModel
import com.lixyz.lifekeeperforkotlin.net.CountingRequestBody
import com.lixyz.lifekeeperforkotlin.sql.SQLiteHelper
import com.lixyz.lifekeeperforkotlin.utils.Constant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern


class RecordViewModel : ViewModel() {

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("通话录音线程池"))

    private val gson = Gson()

    private val model = RecordModel()


    var needUploadLiveData: MutableLiveData<Boolean>? = null
    var uploadDialogStateLiveData: MutableLiveData<Boolean>? = null
    var uploadDialogFileNameLiveData: MutableLiveData<String>? = null
    var uploadDialogProgressLiveData: MutableLiveData<Float>? = null
    var contactLiveData: MutableLiveData<ArrayList<ContactBean>>? = null
    var recordLiveData: MutableLiveData<ArrayList<RecordBean>>? = null
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


    fun getUserId(context: Context): String? {
        return context.getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
    }

    fun getContactNames(context: Context) {
        waitDialogStateLiveData!!.postValue(true)
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
                    .getString("UserId", null)
            if (userId != null) {
                val respList: ArrayList<ContactBean>? = model.getContactNames(userId)
                contactLiveData!!.postValue(respList)
            }
            waitDialogStateLiveData!!.postValue(false)
        }
    }

    fun getRecords(context: Context, contactId: String) {
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
                    .getString("UserId", null)
            if (userId != null) {
                val respList: ArrayList<RecordBean>? = model.getRecords(userId, contactId)
                recordLiveData!!.postValue(respList)
            }
        }
    }

    fun checkNeedUpload() {
        try {
            threadPool.execute {
                val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
                val dir = File("$parentPath/MIUI/sound_recorder/call_rec/")
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

    fun getLocalFileList() {
        val fileNameList = ArrayList<String>()
        val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
        val dir = File("$parentPath/MIUI/sound_recorder/call_rec/")
        val files = dir.listFiles { pathname ->
            pathname.isFile
        }
        files?.forEachIndexed { _, file ->
            val pattern = ".*\\(\\d*\\)_\\d*.mp3"
            val r = Pattern.compile(pattern)
            val m: Matcher = r.matcher(file.name)
            if (m.matches()) {
                if (!fileNameList.contains(file.name.split("_")[0])) {
                    fileNameList.add(file.name.split("_")[0])
                }
            }
        }
        localFileLiveData!!.postValue(fileNameList)
    }

    @SuppressLint("Range")
    fun deleteRecordFile(context: Context, checkedList: ArrayList<String>) {
        val selection: String = "ContactName IN ('" + TextUtils.join("','", checkedList) + "')"
        val helper = SQLiteHelper(context, "LifeKeeper.db", null, 1)
        val database = helper.writableDatabase
        database.beginTransaction()
        val cursor =
            database.query("PhoneRecord", arrayOf("FileName"), selection, null, null, null, null)
        if (cursor.count > 0) {
            cursor.moveToFirst()
            do {
                database.delete(
                    "PhoneRecord",
                    "FileName = ?",
                    arrayOf(cursor.getString(cursor.getColumnIndex("FileName")))
                )
                val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
                val dir = File("$parentPath/MIUI/sound_recorder/call_rec/")
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

    fun fileToSQLite(context: Context) {
        threadPool.execute {
            val database = SQLiteHelper(context, Constant.DB_NAME, null, 1).writableDatabase
            val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
            val dir = File("$parentPath/MIUI/sound_recorder/call_rec/")
            val files = dir.listFiles { pathname ->
                pathname.isFile
            }
            if (files != null && files.isNotEmpty()) {
                files.forEachIndexed { _, file ->
                    val cursor = database.query(
                        "PhoneRecord",
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
                        database.insert("PhoneRecord", null, value)
                    }
                    cursor.close()
                }
            }
            database.close()
        }
    }

    @SuppressLint("Range")
    @Throws(Exception::class)
    fun uploadRecordFile(context: Context, checkedList: java.util.ArrayList<String>) {
        threadPool.execute {
            val database = SQLiteHelper(context, "LifeKeeper.db", null, 1).writableDatabase
            val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
            val dir = File("$parentPath/MIUI/sound_recorder/call_rec/")
            val client: OkHttpClient = OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                .writeTimeout(120 * 1000.toLong(), TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(StethoInterceptor())
                .build()
            checkedList.forEachIndexed { _, s ->
                val cursor = database.query(
                    "PhoneRecord",
                    arrayOf("FileName"),
                    "ContactName = ?",
                    arrayOf(s),
                    null,
                    null,
                    null
                )
                if (cursor != null) {
                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        var index = 0
                        do {
                            val fileName = cursor.getString(cursor.getColumnIndex("FileName"))
                            val file = File("${dir.absolutePath}/$fileName")
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
                                val progress = value / max
                                uploadDialogFileNameLiveData!!.postValue(fileName)
                                uploadDialogProgressLiveData!!.postValue(progress.toFloat())
                            }
                            val request = Request.Builder()
                                .url("${Constant.CLOUD_ADDRESS}/LifeKeeper/UploadRecord")
                                .addHeader("Token", getUserId(context)!!)
                                .post(requestBody)
                                .build()
                            val response = client.newCall(request).execute()
                            val str = response.body!!.string()
                            val result = gson.fromJson(str, NewResult::class.java)
                            if (result.result) {//如果上传成功，删除文件和sqlite数据
                                database.beginTransaction()
                                database.delete("PhoneRecord", "FileName = ?", arrayOf(fileName))
                                file.delete()
                                database.setTransactionSuccessful()
                                database.endTransaction()
                            }
                            index++
                            if (index == cursor.count) {
                                uploadDialogStateLiveData!!.postValue(false)
                                checkNeedUpload()
                            }
                        } while (cursor.moveToNext())
                    }
                    cursor.close()
                }
            }
        }
    }

    fun deleteContact(context: Context, checkList: MutableList<String>) {
        threadPool.execute {
            val userId =
                context.getSharedPreferences("LoginConfig", MODE_PRIVATE)
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
}
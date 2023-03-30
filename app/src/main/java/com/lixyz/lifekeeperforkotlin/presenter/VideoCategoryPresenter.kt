package com.lixyz.lifekeeperforkotlin.presenter

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoCategoryBean
import com.lixyz.lifekeeperforkotlin.model.VideoCategoryModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.utils.UriUtils
import com.lixyz.lifekeeperforkotlin.view.activity.IVideoCategoryView
import java.io.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class VideoCategoryPresenter(private var view: IVideoCategoryView) {
    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("视频分类线程池"))

    private val model = VideoCategoryModel()


    fun getCover(context: Context) {
        view.showWaitDialog()
        threadPool.execute {
            val videoCategoryCover = model.getVideoCategoryCover(context)
            if (videoCategoryCover == null) {
                view.showSnackBar("出错啦，请稍后重试")
            } else {
                view.updateView(videoCategoryCover)
            }
            view.hideWaitDialog()
        }
    }

    fun activityDestroy() {
        if (!threadPool.isShutdown) {
            threadPool.shutdownNow()
        }
    }

    fun verifyImageCategoryPassword(
        context: Context,
        category: VideoCategoryBean,
        password: String,
        type: Int
    ) {
        threadPool.execute {
            val result: Boolean =
                model.checkVideoCategoryPassword(context, category.categoryId!!, password)
            if (result) {
                when (type) {
                    0 -> {
                        view.startVideoActivity(
                            category.categoryId!!,
                            category.categoryName!!,
                            StringUtil.string2MD5("", password)!!
                        )
                    }
                    1 -> {
                        view.showDeleteVideoCategoryDialog(
                            category.categoryName!!,
                            category.categoryId!!,
                            password
                        )
                    }
                    2 -> {
                        view.showEditCategoryDialog(category)
                    }
                }
            } else {
                view.showSnackBar("密码出错")
            }
        }
    }

    fun deleteCategory(context: Context, categoryId: String) {
        view.showWaitDialog()
        threadPool.execute {
            val result: Boolean =
                model.deleteCategory(context, categoryId)
            if (result) {
                val categoryCover = model.getVideoCategoryCover(context)
                if (categoryCover == null) {
                    view.showSnackBar("出错啦，请稍后重试")
                } else {
                    view.updateView(categoryCover)
                }
                view.showSnackBar("删除成功")
                view.hideWaitDialog()
            } else {
                view.hideWaitDialog()
                view.showSnackBar("删除失败，请检查后重试")
            }
        }
    }

    fun updateVideoCategory(context: Context, bean: VideoCategoryBean) {
        view.showWaitDialog()
        threadPool.execute {
            val result = model.updateVideoCategory(context, bean)
            if (result) {
                view.hideWaitDialog()
                view.showSnackBar("修改成功")
                val categoryCover = model.getVideoCategoryCover(context)
                if (categoryCover == null) {
                    view.showSnackBar("出错啦，请稍后重试")
                } else {
                    view.updateView(categoryCover)
                }
            } else {
                view.hideWaitDialog()
                view.showSnackBar("修改失败，请稍候重试")
            }
        }
    }

    fun checkNeedUpload(
        context: Context,
        view: IVideoCategoryView
    ) {
        threadPool.execute {
            try {
                val except = context.getSharedPreferences("UploadExcept", Context.MODE_PRIVATE)
                val set = except.getStringSet("Video", HashSet<String>())
                val arr = arrayOfNulls<String>(set!!.size)
                var tmpStr = "("
                val videoCursor: Cursor?
                if (set.size > 0) {
                    set.forEachIndexed { index, s ->
                        arr[index] = s
                        tmpStr = "$tmpStr$s,"
                    }
                    tmpStr = tmpStr.subSequence(0, tmpStr.length - 1).toString()
                    tmpStr = "$tmpStr)"
                    videoCursor = context.contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null,
                        MediaStore.Video.Media._ID + " not in " + tmpStr,
                        null,
                        null
                    )
                } else {
                    videoCursor = context.contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                    )
                }
                if (videoCursor != null) {
                    if (videoCursor.count > 0) {
                        view.showUploadBottomButton()
                    } else {
                        view.hideUploadBottomButton()
                    }
                    videoCursor.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun upload(
        context: Context,
        view: IVideoCategoryView
    ) {
        view.showUploadDialog()
        view.updateUploadDialog("上传准备中...", 0)
        threadPool.execute {
            val list = ArrayList<Uri>()
            //搜索视频
            val videoCursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null, null
            )
            //如果视频数量大于0
            if (videoCursor!!.count > 0) {
                videoCursor.moveToFirst()
                var index = 1
                do {
                    val id =
                        videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val duration =
                        videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Video.Media.DURATION)) * 1000
                    val createTime =
                        videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)) * 1000
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id.toLong()
                    )
                    try {
                        val path = UriUtils.getPathFromUri(context, uri)
                        val name = if (path != null) {
                            File(path).name
                        } else {
                            val fileSuffix =
                                videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE))
                                    .split("/")[1]
                            val fileName =
                                videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                            "$fileName.$fileSuffix"
                        }
                        //如果文件不存在，直接删除 Uri
                        if (!fileIsExists(context, uri)) {
                            deleteDataByUri(context, uri)
                            continue
                        }
                        //复制缓存文件
                        val cacheVideoFile =
                            File(context.cacheDir.absolutePath + "/" + name)
                        val out = FileOutputStream(cacheVideoFile)
                        val bos = BufferedOutputStream(out)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bis = BufferedInputStream(inputStream)
                        var length: Int
                        val arr = ByteArray(1024)
                        while (bis.read(arr).also { length = it } != -1) {
                            bos.write(arr, 0, length)
                            bos.flush()
                        }
                        //上传文件
                        val uploadResult = model.uploadLocalVideoToCloud(
                            context,
                            cacheVideoFile,
                            index++,
                            videoCursor.count,
                            duration,
                            createTime,
                            view
                        )
                        //不管上传是否成功，都删除缓存文件
                        cacheVideoFile.delete()
                        if (uploadResult) {
                            deleteDataByUri(context, uri)
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        list.add(uri)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } while (videoCursor.moveToNext())
            }
            videoCursor.close()
            deleteDataByUriList(context, list)
            view.hideUploadDialog()
            view.uploadDone()
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    private fun deleteDataByUriList(context: Context, list: ArrayList<Uri>) {
        list.forEachIndexed { _, uri ->
            context.contentResolver.delete(uri, null, null)
        }
    }

    @kotlin.jvm.Throws(Exception::class)
    private fun deleteDataByUri(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }

    @kotlin.jvm.Throws(Exception::class)
    private fun fileIsExists(context: Context, uri: Uri): Boolean {
        val cr = context.contentResolver
        return try {
            val afd = cr.openAssetFileDescriptor(uri, "r")
            val result = afd != null
            afd?.close()
            result
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}
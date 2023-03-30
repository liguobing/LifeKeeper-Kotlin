package com.lixyz.lifekeeperforkotlin.presenter

import android.content.ContentUris
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.lixyz.lifekeeperforkotlin.base.BaseThreadFactory
import com.lixyz.lifekeeperforkotlin.bean.SelectFileBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.model.ImageCategoryModel
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.activity.IImageCategoryView
import java.io.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class ImageCategoryPresenter(
    private var view: IImageCategoryView
) {

    /**
     * 线程池
     */
    private val threadPool: ScheduledExecutorService =
        ScheduledThreadPoolExecutor(3, BaseThreadFactory("图片分类线程池"))

    private val model = ImageCategoryModel()


    fun getCover(context: Context) {
        view.showWaitDialog()
        threadPool.execute {
            val categoryCover = model.getImageCategoryCover(context)
            if (categoryCover == null) {
                view.showSnackBar("出错啦，请稍后重试")
            } else {
                view.updateView(categoryCover)
            }
            view.hideWaitDialog()
        }
    }

    fun activityDestroy() {
        if (!threadPool.isShutdown) {
            threadPool.shutdownNow()
        }
    }

    /**
     * type:验证形式
     * 0：打开相册的验证
     * 1：删除相册的验证
     * 2：编辑相册的验证
     */
    fun verifyImageCategoryPassword(
        context: Context,
        category: ImageCategoryBean,
        password: String,
        type: Int
    ) {
        threadPool.execute {
            val result: Boolean =
                model.checkImageCategoryPassword(
                    context,
                    category.categoryId!!,
                    StringUtil.string2MD5("", password)!!
                )
            if (result) {
                when (type) {
                    0 -> {
                        view.startImageActivity(
                            category.categoryId!!,
                            category.categoryName!!,
                            password
                        )
                    }
                    1 -> {
                        view.showDeleteImageCategoryDialog(
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
                val categoryCover = model.getImageCategoryCover(context)
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

    fun updateImageCategory(context: Context, bean: ImageCategoryBean) {
        view.showWaitDialog()
        threadPool.execute {
            val result = model.updateImageCategory(context, bean)
            if (result) {
                view.hideWaitDialog()
                view.showSnackBar("修改成功")
                val categoryCover = model.getImageCategoryCover(context)
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

    //检查有没有需要上传的图片
    fun checkNeedUpload(context: Context, view: IImageCategoryView) {
        threadPool.execute {
            try {
                val except = context.getSharedPreferences("UploadExcept", MODE_PRIVATE)
                val set = except.getStringSet("Image", HashSet<String>())
                val arr = arrayOfNulls<String>(set!!.size)
                var tmpStr = "("
                val photoCursor: Cursor?
                if (set.size > 0) {
                    set.forEachIndexed { index, s ->
                        arr[index] = s
                        tmpStr = "$tmpStr$s,"
                    }
                    tmpStr = tmpStr.subSequence(0, tmpStr.length - 1).toString()
                    tmpStr = "$tmpStr)"
                    photoCursor = context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        MediaStore.Images.Media._ID + " not in " + tmpStr,
                        null,
                        null
                    )
                } else {
                    photoCursor = context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                    )
                }
                if (photoCursor != null) {
                    if (photoCursor.count > 0) {
                        view.showUpload()
                    } else {
                        view.hideUpload()
                    }
                    photoCursor.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getNeedUploadFile(context: Context, view: IImageCategoryView) {
        threadPool.execute {
            try {
                val except = context.getSharedPreferences("UploadExcept", MODE_PRIVATE)
                val set = except.getStringSet("Image", null)
                if (set == null) {
                    val photoCursor = context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                    )
                    if (photoCursor != null) {
                        if (photoCursor.count > 0) {
                            photoCursor.moveToFirst()
                            val list = ArrayList<SelectFileBean>()
                            do {
                                val id =
                                    photoCursor.getString(photoCursor.getColumnIndex(MediaStore.Images.Media._ID))
                                val name =
                                    photoCursor.getString(photoCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                                list.add(SelectFileBean(id, name, false))
                            } while (photoCursor.moveToNext())
                            view.showBottomUploadDialog(list)
                        }
                        photoCursor.close()
                    } else {
                        view.showSnackBar("没有需要上传的文件")
                    }
                } else {
                    val arr = arrayOfNulls<String>(set.size)
                    var tmpStr = "("
                    val photoCursor: Cursor?
                    if (set.size > 0) {
                        set.forEachIndexed { index, s ->
                            arr[index] = s
                            tmpStr = "$tmpStr$s,"
                        }
                        tmpStr = tmpStr.subSequence(0, tmpStr.length - 1).toString()
                        tmpStr = "$tmpStr)"
                        photoCursor = context.contentResolver.query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            null,
                            MediaStore.Images.Media._ID + " not in " + tmpStr,
                            null,
                            null
                        )
                    } else {
                        photoCursor = context.contentResolver.query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            null,
                            null,
                            null,
                            null
                        )
                    }
                    if (photoCursor != null) {
                        if (photoCursor.count > 0) {
                            photoCursor.moveToFirst()
                            val list = ArrayList<SelectFileBean>()
                            do {
                                val id =
                                    photoCursor.getString(photoCursor.getColumnIndex(MediaStore.Images.Media._ID))
                                val name =
                                    photoCursor.getString(photoCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                                list.add(SelectFileBean(id, name, false))
                            } while (photoCursor.moveToNext())
                            view.showBottomUploadDialog(list)
                        }
                        photoCursor.close()
                    } else {
                        view.showSnackBar("没有需要上传的文件")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                view.showSnackBar("出错了，请稍候重试")
            }
        }
    }

    fun addExceptImage(context: Context, view: IImageCategoryView, set: HashSet<String>) {
        val sharedPreferences = context.getSharedPreferences("UploadExcept", MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        val newSet = HashSet<String>()
        val imageSet = sharedPreferences.getStringSet("Image", HashSet<String>())
        set.forEachIndexed { _, s ->
            newSet.add(s)
        }
        imageSet!!.forEachIndexed { _, s ->
            newSet.add(s)
        }
        edit.putStringSet("Image", newSet)
        if (edit.commit()) {
            view.hideUploadBottomDialog()
            view.showSnackBar("添加排除成功")
            checkNeedUpload(context, view)
        } else {
            view.hideUploadBottomDialog()
            view.showSnackBar("添加排除出错，请检查后重试")
            checkNeedUpload(context, view)
        }
    }

    fun uploadImage(context: Context, selectFileList: ArrayList<SelectFileBean>) {
        view.showUploadDialog()
        threadPool.execute {
            selectFileList.forEachIndexed { index, s ->
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    s.id.toLong()
                )
                try {
                    //如果该文件不存在，则直接删除 Uri
                    if (!fileIsExists(context, uri)) {
                        deleteDataByUri(context, uri)
                    } else {
                        //复制文件到缓存目录
                        val cacheFile =
                            File(context.cacheDir.absolutePath + "/${StringUtil.getRandomString()}_" + s.fileName)
                        val out = FileOutputStream(cacheFile)
                        val bos = BufferedOutputStream(out)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bis = BufferedInputStream(inputStream)
                        var length: Int
                        val arr = ByteArray(1024)
                        while (bis.read(arr).also { length = it } != -1) {
                            bos.write(arr, 0, length)
                            bos.flush()
                        }
                        //上传缓存文件
                        val uploadResult = model.uploadLocalImageFileToCloud(
                            context,
                            cacheFile,
                            index,
                            selectFileList.size,
                            System.currentTimeMillis(),
                            view
                        )
                        //上传完成之后，不管上传成功与否，都将缓存文件删除
                        cacheFile.delete()
                        //如果上传成功，通过URI删除文件，并且更新UI Card
                        if (uploadResult > 0) {
                            deleteDataByUri(context, uri)
                        }
                    }
                    view.uploadDone()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            view.hideUploadDialog()
        }
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

    @kotlin.jvm.Throws(Exception::class)
    private fun deleteDataByUri(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }
}

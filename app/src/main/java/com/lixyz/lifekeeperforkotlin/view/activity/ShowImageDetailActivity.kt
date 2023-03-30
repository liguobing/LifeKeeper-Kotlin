package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.github.lzyzsd.circleprogress.DonutProgress
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.ShowImageDetailMoveImageCategoryListAdapter
import com.lixyz.lifekeeperforkotlin.adapter.ShowImageViewPagerAdapter
import com.lixyz.lifekeeperforkotlin.bean.ShowPhotoItemBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.pass.ImageThumbnailToShowPhotoDetail
import com.lixyz.lifekeeperforkotlin.presenter.ShowImageDetailPresenter
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.CustomLoadPhotoDialog
import java.io.File
import java.lang.ref.WeakReference


class ShowImageDetailActivity : FragmentActivity(), View.OnClickListener, IShowImageDetailView {

    private var presenter: ShowImageDetailPresenter? = null

    /**
     * 顶部 Toolbar
     */
    private var toolbarTop: Toolbar? = null

    /**
     * 返回按钮
     */
    private var imgBack: ImageView? = null

    /**
     * 旋转按钮
     */
    private var imgRotate: ImageView? = null

    /**
     * viewpager
     */
    private var viewPager: ViewPager2? = null

    /**
     * ViewPager Adapter
     */
    private var viewPagerAdapter: ShowImageViewPagerAdapter? = null

    /**
     * ViewPager 显示的 Fragment List
     */
    private val fragmentList = ArrayList<ImageDetailFragment>()

    /**
     * Fragment id List
     */
    private val itemIdList = ArrayList<Long>()

    /**
     * 底部 Toolbar
     */
    private var toolbarBottom: Toolbar? = null

    /**
     * 删除按钮
     */
    private var imgDelete: ImageView? = null

    /**
     * 修改分类
     */
    private var imgChangeImageCategory: ImageView? = null

    /**
     * 高清显示
     */
    private var imgHd: ImageView? = null

    /**
     * 下载按钮
     */
    private var imgDownload: ImageView? = null

    /**
     * 图片加载进度条
     */
    private var mProgress: DonutProgress? = null

    /**
     * 要显示的数据列表
     */
    private var dataList: ArrayList<ImageBean>? = null

    /**
     * 显示的页面下标
     */
    private var showPosition: Int = 0

    /**
     * 载入图片 Dialog
     */
    private var photoLoadingDialog: CustomLoadPhotoDialog? = null

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * handler
     */
    private val handler: MyHandler = MyHandler(this)

    /**
     * PhotoView 是否是全屏的，用于处理点击图片时显示/隐藏 Toolbar
     */
    private var photoViewFullScreen = false

    /**
     * 所有图片分类，供修改图片分类使用
     */
    private val categoryList = ArrayList<ImageCategoryBean>()

    companion object {

        /**
         * 显示 SnackBar
         */
        private const val SHOW_SNACK_BAR = 1000

        /**
         * 更新载入图片 Dialog
         */
        private const val UPDATE_PHOTO_LOADING_DIALOG = 2000

        /**
         * 隐藏载入图片 Dialog
         */
        private const val HIDE_PHOTO_LOADING_DIALOG = 3000

        /**
         * 更新 ViewPager
         */
        private const val UPDATE_VIEW_PAGER_IMAGE = 4000

        /**
         * 更新等待 Dialog
         */
        private const val UPDATE_WAIT_DIALOG = 5000

        /**
         * 隐藏等待 Dialog
         */
        private const val HIDE_WAIT_DIALOG = 6000
    }

    private var sourceDataList: ArrayList<ShowPhotoItemBean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val view = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        view.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity___show_image_detail)

        if (intent != null) {
//            val data =
//                intent.getSerializableExtra("Param") as ImageThumbnailToShowPhotoDetail
//            showPosition = data.position!!
//            sourceDataList = data.dataList
//            dataList = ArrayList()
//            sourceDataList!!.forEachIndexed { _, showPhotoItemBean ->
//                if (dataList != null) {
//                    dataList!!.add(showPhotoItemBean.image!!)
//                }
//            }
            categoryId = intent.getStringExtra("CategoryId")
            password = intent.getStringExtra("Password")
            position = intent.getIntExtra("Position", 0)
        }
    }

    private var categoryId: String? = null
    private var password: String? = null
    private var position = 0

    override fun onStart() {
        super.onStart()
        initWidget()
        presenter!!.getOtherImageCategory(this, categoryId!!)
        presenter!!.getImages(this, categoryId!!, password!!)
    }

    override fun onResume() {
        super.onResume()
        initListener()
    }


    fun initWidget() {
        presenter = ShowImageDetailPresenter(this)

        toolbarTop = findViewById(R.id.toolbar_top)
        imgBack = findViewById(R.id.img_back)
        imgRotate = findViewById(R.id.img_rotate)

        viewPager = findViewById(R.id.viewPager)
        viewPager!!.offscreenPageLimit = 1

        toolbarBottom = findViewById(R.id.toolbar_bottom)
        imgDelete = findViewById(R.id.img_delete)
        imgChangeImageCategory = findViewById(R.id.img_change_image_category)
        imgHd = findViewById(R.id.img_hd)
        imgDownload = findViewById(R.id.img_download)
        photoLoadingDialog = CustomLoadPhotoDialog(this, this)
        waitDialog = CustomDialog(this, this, "请稍后...")
        dataList!!.forEach {
            val fragment = ImageDetailFragment(it, categoryId!!, this@ShowImageDetailActivity)
            fragmentList.add(fragment)
            itemIdList.add(fragment.hashCode().toLong())
        }
        viewPagerAdapter = ShowImageViewPagerAdapter(this, fragmentList, itemIdList)
        viewPager!!.adapter = viewPagerAdapter
        viewPager!!.setCurrentItem(showPosition, false)
    }

    fun initListener() {
        imgBack!!.setOnClickListener(this)
        imgRotate!!.setOnClickListener(this)
        imgDelete!!.setOnClickListener(this)
        imgChangeImageCategory!!.setOnClickListener(this)
        imgHd!!.setOnClickListener(this)
        imgDownload!!.setOnClickListener(this)

        viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                showPosition = position
            }
        })
    }

    fun photoViewClick() {
        photoViewFullScreen = !photoViewFullScreen
        if (photoViewFullScreen) {
            toolbarTop!!.visibility = View.GONE
            toolbarBottom!!.visibility = View.GONE
        } else {
            toolbarTop!!.visibility = View.VISIBLE
            toolbarBottom!!.visibility = View.VISIBLE
        }
    }

    override fun updateImages(images: ArrayList<ImageBean>) {
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            //设置 Toolbar padding
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            toolbarTop!!.setPadding(0, resources.getDimensionPixelSize(resourceId), 0, 0)
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        val bean = ImageThumbnailToShowPhotoDetail()
        bean.dataList = sourceDataList
        intent.putExtra("Result", bean)
        setResult(1, intent)
        finish()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_back -> {
                val intent = Intent()
                val bean = ImageThumbnailToShowPhotoDetail()
                bean.dataList = sourceDataList
                intent.putExtra("Result", bean)
                setResult(1, intent)
                finish()
            }
            R.id.img_rotate -> {
                fragmentList[viewPager!!.currentItem].rotateImage()
            }
            R.id.img_delete -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("确定删除吗？")
                builder.setPositiveButton("删除") { dialog, _ ->
                    dialog.dismiss()
                    val bean = dataList!![viewPager!!.currentItem]
                    presenter!!.deleteFile(this, bean)
                }
                builder.show()
            }
            R.id.img_change_image_category -> {
                showBottomDialog()
            }
            R.id.img_hd -> {
                fragmentList[showPosition].showHDImage()
                viewPagerAdapter!!.notifyDataSetChanged()
            }
            R.id.img_download -> {
                val bean = dataList!![viewPager!!.currentItem]
                presenter!!.downloadImage(this, bean)
            }
        }
    }

    private fun showBottomDialog() {
        val bottomDialog = Dialog(this, R.style.BottomDialog)
        val contentView: View =
            LayoutInflater.from(this)
                .inflate(
                    R.layout.view___show_image_change_category_bottom_dialog,
                    RelativeLayout(this),
                    false
                )
        val listView: ListView = contentView.findViewById(R.id.lv_category)
        listView.adapter = ShowImageDetailMoveImageCategoryListAdapter(this, categoryList)
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                bottomDialog.dismiss()
                presenter!!.moveImage(
                    this,
                    categoryList[position],
                    dataList!![showPosition]
                )
            }
        bottomDialog.setContentView(contentView)
        val layoutParams = contentView.layoutParams
        layoutParams.width = resources.displayMetrics.widthPixels
        contentView.layoutParams = layoutParams
        bottomDialog.window!!.setGravity(Gravity.BOTTOM)
        bottomDialog.window!!.setWindowAnimations(R.style.BottomDialog_Animation)
        bottomDialog.show()
    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun updateWaitDialog(message: String) {
        val msg = Message.obtain()
        msg.what = UPDATE_WAIT_DIALOG
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun hideWaitDialog() {
        handler.sendEmptyMessage(HIDE_WAIT_DIALOG)
    }

    override fun showSnackBar(message: String) {
        val msg = Message.obtain()
        msg.what = SHOW_SNACK_BAR
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun showPhotoLoadingDialog() {
        if (!photoLoadingDialog!!.isShowing) {
            photoLoadingDialog!!.show()
        }
    }

    override fun updatePhotoLoadingDialog(progress: Float) {
        val msg = Message.obtain()
        msg.what = UPDATE_PHOTO_LOADING_DIALOG
        msg.obj = progress
        handler.sendMessage(msg)
    }

    override fun hidePhotoLoadingDialog() {
        handler.sendEmptyMessage(HIDE_PHOTO_LOADING_DIALOG)
    }

    override fun updateViewPager(file: File) {
//        val itemBean = dataList!![viewPager!!.currentItem]
//        val parentFile = file.parentFile
//        if (itemBean.fileCategory == parentFile!!.name && itemBean.coverFileName == file.name) {
//            val msg = Message.obtain()
//            msg.what = UPDATE_VIEW_PAGER_IMAGE
//            msg.obj = file
//            handler.sendMessage(msg)
//        }
    }

    override fun deleteFileSuccessUpdateViewPager(bean: ImageBean) {
        fragmentList.removeAt(showPosition)
        itemIdList.removeAt(showPosition)
        sourceDataList!!.removeAt(showPosition)
        dataList!!.removeAt(showPosition)
        runOnUiThread {
            viewPagerAdapter!!.notifyDataSetChanged()
            showPosition = viewPager!!.currentItem
        }
    }

    override fun updateImageCategoryList(categoryList: ArrayList<ImageCategoryBean>) {
        if (categoryList.size > 0) {
            this.categoryList.clear()
        }
        this.categoryList.addAll(categoryList)
    }

    override fun moveImageSuccess(bean: ImageBean) {
        val index = dataList!!.indexOf(bean)
        dataList!!.removeAt(index)
        fragmentList.removeAt(index)
        itemIdList.removeAt(index)
        sourceDataList!!.removeAt(index)
        runOnUiThread {
            viewPagerAdapter!!.notifyDataSetChanged()
        }
    }

    private class MyHandler(activity: ShowImageDetailActivity) : Handler() {
        private val mActivity: WeakReference<ShowImageDetailActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity = mActivity.get()
            when (msg.what) {
                SHOW_SNACK_BAR -> {
                    Snackbar.make(activity!!.viewPager!!, msg.obj as String, Snackbar.LENGTH_SHORT)
                        .show()
                }
                UPDATE_PHOTO_LOADING_DIALOG -> {
                    if (activity!!.photoLoadingDialog!!.isShowing) {
                        activity.photoLoadingDialog!!.setProgress(msg.obj as Float)
                    }
                }
                HIDE_PHOTO_LOADING_DIALOG -> {
                    if (activity!!.photoLoadingDialog!!.isShowing) {
                        activity.photoLoadingDialog!!.dismiss()
                    }
                }
                UPDATE_WAIT_DIALOG -> {
                    if (activity!!.waitDialog!!.isShowing) {
                        activity.waitDialog!!.setMessage(msg.obj as String)
                    }
                }
                HIDE_WAIT_DIALOG -> {
                    if (activity!!.waitDialog!!.isShowing) {
                        activity.waitDialog!!.dismiss()
                    }
                }
            }
        }
    }
}
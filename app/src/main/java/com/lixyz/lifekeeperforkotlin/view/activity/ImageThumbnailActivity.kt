package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.ImageThumbnailMoveImageBottomDialogAdapter
import com.lixyz.lifekeeperforkotlin.adapter.ShowPhotoRecyclerViewAdapter
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.ShowPhotoItemBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImagesForCategoryBean
import com.lixyz.lifekeeperforkotlin.presenter.ImageThumbnailPresenter
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.CustomLoadPhotoDialog
import java.lang.ref.WeakReference


class ImageThumbnailActivity : BaseActivity(), IImageThumbnailView, View.OnClickListener {

    /**
     * Presenter
     */
    private var thumbnailPresenter: ImageThumbnailPresenter? = null

    /**
     * 分类列表（用于移动图片到其他分类）
     */
    private var otherCategoryList: ArrayList<ImageCategoryBean>? = ArrayList()

    /**
     * 返回按钮
     */
    private var imgBack: ImageView? = null


    /**
     * 分类标题
     */
    private var tvCategory: TextView? = null

    /**
     * 删除/取消按钮布局
     */
    private var llDeleteCancelLayout: LinearLayout? = null

    /**
     * 删除按钮
     */
    private var tvDelete: TextView? = null

    /**
     * 移动图片
     */
    private var tvMoveImage: TextView? = null

    /**
     * 取消按钮按钮
     */
    private var tvCancel: TextView? = null

    /**
     * RecyclerView 刷新 Layout
     */
    private var refreshLayout: TwinklingRefreshLayout? = null

    /**
     * 缩略图 RecyclerView
     */
    private var rvPhotoThumbnails: RecyclerView? = null

    /**
     * 缩略图 RecyclerView adapter
     */
    private var thumbnailRecyclerViewAdapter: ShowPhotoRecyclerViewAdapter? = null

    /**
     * 缩略图 RecyclerView 数据 List
     */
    private var thumbnailRecyclerViewDataList: ArrayList<ShowPhotoItemBean>? = ArrayList()

    /**
     * Handler
     */
    private val handler = MyHandler(this)

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * 载入照片 Dialog
     */
    private var loadPhotoDialog: CustomLoadPhotoDialog? = null

    /**
     * 要删除的图片列表
     */
    private val deleteList = ArrayList<ShowPhotoItemBean>()

    /**
     * 当前分类 Id
     */
    private var currentCategoryId: String = ""

    /**
     * 当前分类密码
     */
    private var currentCategoryPassword: String = ""

    /**
     * 当前分页
     */
    private var currentPage = 1

    companion object {
        /**
         * 更新缩略图 RecyclerView
         */
        private const val UPDATE_PHOTO_RECYCLER_VIEW = 1000

        /**
         * 隐藏等待 Dialog
         */
        private const val HIDE_WAIT_DIALOG = 2000

        /**
         * 更新等待 Dialog
         */
        private const val UPDATE_WAIT_DIALOG = 3000

        /**
         * 显示 SnackBar
         */
        private const val SHOW_SNACK_BAR = 4000

        /**
         * 隐藏载入图片 Dialog
         */
        private const val HIDE_LOAD_PHOTO_DIALOG = 5000

        /**
         * 更新载入图片 Dialog
         */
        private const val UPDATE_LOAD_PHOTO_DIALOG = 6000

        /**
         * 删除图片结果
         */
        private const val UPDATE_DELETE_FILE_RESULT = 7000

        private const val SHOW_DELETE_LAYOUT = 8000

        private const val HIDE_DELETE_LAYOUT = 9000
    }

    /**
     * 是否是编辑状态
     * 编辑状态的话，
     * 1.图片的选中按钮显示
     * 2.toolbar 的删除按钮显示
     * 3.单击按钮，选中图片，而不是打开图片
     */
    private var isEdit: Boolean = false

    private var imageData: ImagesForCategoryBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // StatusBar 设置为透明
        window.statusBarColor = Color.parseColor("#4CE19F")
        setContentView(R.layout.activity___image_thumbnail)
        initWidget()

        currentCategoryId = intent.getStringExtra("CategoryId")!!
        currentCategoryPassword = intent.getStringExtra("Password")!!
        thumbnailRecyclerViewDataList!!.clear()
        thumbnailPresenter!!.loadMoreImage(this, currentCategoryId, currentCategoryPassword, 1, 51)
    }


    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        if (otherCategoryList?.size == 0) {
            thumbnailPresenter!!.getOtherCategory(this, currentCategoryId)
        }
    }

    override fun onPause() {
        super.onPause()
        thumbnailRecyclerViewDataList!!.forEach {
            it.checkViewIsShow = false
            it.checked = false
        }
        thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
        llDeleteCancelLayout!!.visibility = View.GONE
        deleteList.clear()
        isEdit = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.thumbnailRecyclerViewDataList!!.clear()
        this.currentPage = 0
        thumbnailPresenter!!.loadMoreImage(this, currentCategoryId, currentCategoryPassword, 1, 51)
        handler.sendEmptyMessage(UPDATE_PHOTO_RECYCLER_VIEW)

//        val resultData = data!!.getSerializableExtra("Result") as ImageThumbnailToShowPhotoDetail
//        thumbnailRecyclerViewDataList!!.clear()
//        thumbnailRecyclerViewDataList!!.addAll(resultData.dataList!!)
//        thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
    }

    override fun initWidget() {
        waitDialog = CustomDialog(this, this, "请稍后...")
        loadPhotoDialog = CustomLoadPhotoDialog(this, this)
        thumbnailPresenter = ImageThumbnailPresenter(this)
        thumbnailRecyclerViewAdapter = ShowPhotoRecyclerViewAdapter(
            thumbnailRecyclerViewDataList!!,
            this
        )
        imgBack = findViewById(R.id.img_back)
        tvCategory = findViewById(R.id.tv_category)
        llDeleteCancelLayout = findViewById(R.id.ll_delete_cancel_layout)
        tvDelete = findViewById(R.id.tv_delete)
        tvMoveImage = findViewById(R.id.tv_move_image)
        tvCancel = findViewById(R.id.tv_cancel)
        imgBack = findViewById(R.id.img_back)
        rvPhotoThumbnails = findViewById(R.id.rv_photos)
        rvPhotoThumbnails!!.layoutManager = GridLayoutManager(this, 3)
        rvPhotoThumbnails!!.adapter = thumbnailRecyclerViewAdapter!!

        refreshLayout = findViewById(R.id.refreshLayout)
        refreshLayout!!.setEnableRefresh(false)
    }

    override fun initListener() {
        imgBack!!.setOnClickListener(this)
        tvDelete!!.setOnClickListener(this)
        tvMoveImage!!.setOnClickListener(this)
        tvCancel!!.setOnClickListener(this)
        thumbnailRecyclerViewAdapter!!.setOnItemClickListener(object :
            ShowPhotoRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (isEdit) {
                    thumbnailRecyclerViewDataList!![position].checked =
                        !thumbnailRecyclerViewDataList!![position].checked!!
                    if (thumbnailRecyclerViewDataList!![position].checked!!) {
                        deleteList.add(thumbnailRecyclerViewDataList!![position])
                    } else {
                        deleteList.remove(thumbnailRecyclerViewDataList!![position])
                    }
                    tvDelete!!.text = resources.getString(R.string.delete_count, deleteList.size)
                    thumbnailRecyclerViewAdapter!!.notifyItemChanged(position)
                } else {
                    val intent =
                        Intent(this@ImageThumbnailActivity, DisplayImageActivity::class.java)
                    intent.putExtra("Password", currentCategoryPassword)
                    intent.putExtra("Position", position)
                    intent.putExtra(
                        "CategoryId",
                        thumbnailRecyclerViewDataList!![0].image!!.fileCategory
                    )
                    startActivityForResult(intent, 1)
                }
            }
        })
        thumbnailRecyclerViewAdapter!!.setOnItemLongClickListener(object :
            ShowPhotoRecyclerViewAdapter.OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {
                if (isEdit) {
                    llDeleteCancelLayout!!.visibility = View.GONE
                    thumbnailRecyclerViewDataList!!.forEach {
                        it.checked = false
                        it.checkViewIsShow = false
                    }
                    thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
                } else {
                    tvDelete!!.text = resources.getString(R.string.delete_count, 0)
                    llDeleteCancelLayout!!.visibility = View.VISIBLE
                    thumbnailRecyclerViewDataList!!.forEach {
                        it.checked = false
                        it.checkViewIsShow = true
                    }
                    thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
                }
                isEdit = !isEdit
            }
        })

        //照片列表加载更多监听
        refreshLayout!!.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout) {
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout) {
                thumbnailPresenter!!.loadMoreImage(
                    this@ImageThumbnailActivity,
                    currentCategoryId,
                    currentCategoryPassword,
                    currentPage,
                    51
                )
                refreshLayout.finishLoadmore()
            }
        })
    }

    override fun onBackPressed() {
        if (isEdit) {
            llDeleteCancelLayout!!.visibility = View.GONE
            thumbnailRecyclerViewDataList!!.forEach {
                it.checked = false
                it.checkViewIsShow = false
            }
            thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
            deleteList.clear()
            isEdit = !isEdit
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_back -> {
                finish()
            }
            R.id.tv_delete -> {
                if (deleteList.size > 0) {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("确定删除吗？")
                    builder.setPositiveButton("删除") { dialog, _ ->
                        dialog.dismiss()
                        thumbnailPresenter!!.deleteFile(deleteList, this)
                    }
                    builder.show()
                } else {
                    Snackbar.make(tvCategory!!, "还没选择要删除的图片呢", Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.tv_move_image -> {
                if (deleteList.size > 0) {
                    showBottomDialog()
                } else {
                    Snackbar.make(tvCategory!!, "还没选择要移动的图片呢", Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.tv_cancel -> {
                isEdit = false
                deleteList.clear()
                llDeleteCancelLayout!!.visibility = View.GONE
                thumbnailRecyclerViewDataList!!.forEach {
                    it.checked = false
                    it.checkViewIsShow = false
                }
                thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
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
        listView.adapter = ImageThumbnailMoveImageBottomDialogAdapter(this, otherCategoryList!!)
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                bottomDialog.dismiss()
                if (otherCategoryList!![position].categoryName == "创建新相册" && otherCategoryList!![position].categoryId == "add_new_category_object_id" && otherCategoryList!![position].objectId == "add_new_category_category_id") {
                    showCreateNewCategoryDialog()
                } else {
                    thumbnailPresenter!!.moveImage(
                        this,
                        otherCategoryList!![position],
                        deleteList
                    )
                }
            }
        bottomDialog.setContentView(contentView)
        val layoutParams = contentView.layoutParams
        layoutParams.width = resources.displayMetrics.widthPixels
        contentView.layoutParams = layoutParams
        bottomDialog.window!!.setGravity(Gravity.BOTTOM)
        bottomDialog.window!!.setWindowAnimations(R.style.BottomDialog_Animation)
        bottomDialog.show()
    }

    private fun showCreateNewCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(
            R.layout.view___show_image_detail_add_new_category,
            RelativeLayout(this),
            false
        )
        val edit: EditText = view.findViewById(R.id.et_new_category_name)
        builder.setView(view)
        builder.setPositiveButton(
            "确定"
        ) { dialog, _ ->
            dialog.dismiss()
            if (TextUtils.isEmpty(edit.text)) {
                showSnackBar("分类名称不能为空")
            } else {
                otherCategoryList!!.forEach {
                    if (it.categoryName == edit.text.toString()) {
                        showSnackBar("分类名称已存在，无需重复添加")
                        return@setPositiveButton
                    }
                }
                val newCategory = ImageCategoryBean()
                newCategory.objectId = StringUtil.getRandomString()
                newCategory.categoryId = StringUtil.getRandomString()
                newCategory.categoryName = edit.text.toString()
                newCategory.categoryUser = otherCategoryList!![0].categoryUser
                newCategory.isPrivate = -1
                newCategory.password = ""
                newCategory.categoryStatus = 1
                newCategory.categoryType = 0
                newCategory.createTime = System.currentTimeMillis()
                newCategory.updateTime = 0
                otherCategoryList!!.add(0, newCategory)
                Log.d("TTT", Gson().toJson(newCategory))
                thumbnailPresenter!!.moveImageToNewCategory(this, newCategory, deleteList)
            }
        }
        builder.show()
    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun hideWaitDialog() {
        handler.sendEmptyMessage(HIDE_WAIT_DIALOG)
    }

    override fun updateWaitDialog(message: String) {
        val msg = Message.obtain()
        msg.obj = message
        msg.what = UPDATE_WAIT_DIALOG
        handler.sendMessage(msg)
    }

    override fun showSnackBar(message: String) {
        val msg = Message.obtain()
        msg.obj = message
        msg.what = SHOW_SNACK_BAR
        handler.sendMessage(msg)
    }

    override fun updateRecyclerView(list: ArrayList<ShowPhotoItemBean>) {
        this.thumbnailRecyclerViewDataList!!.addAll(list)
        currentPage++
        handler.sendEmptyMessage(UPDATE_PHOTO_RECYCLER_VIEW)
    }

    override fun updateOtherCategoryList(list: ArrayList<ImageCategoryBean>) {
        otherCategoryList = list
        val bean = ImageCategoryBean()
        bean.categoryName = "创建新相册"
        bean.categoryId = "add_new_category_object_id"
        bean.objectId = "add_new_category_category_id"
        otherCategoryList!!.add(bean)
    }

    override fun hideLoadPhotoDialog() {
        handler.sendEmptyMessage(HIDE_LOAD_PHOTO_DIALOG)
    }

    override fun updateLoadPhotoDialog(progress: Float) {
        val msg = Message.obtain()
        msg.what = UPDATE_LOAD_PHOTO_DIALOG
        msg.obj = progress
        handler.sendMessage(msg)
    }

    override fun updateDeleteFileResult(list: ArrayList<ShowPhotoItemBean>) {
        thumbnailRecyclerViewDataList!!.removeAll(list.toSet())
        thumbnailRecyclerViewDataList!!.forEachIndexed { _, showPhotoItemBean ->
            showPhotoItemBean.checkViewIsShow = false
            showPhotoItemBean.checked = false
        }
        isEdit = false
        handler.sendEmptyMessage(UPDATE_DELETE_FILE_RESULT)
    }

    override fun updateMoveFileResult(list: ArrayList<ShowPhotoItemBean>) {
        thumbnailRecyclerViewDataList!!.removeAll(list.toSet())
        thumbnailRecyclerViewDataList!!.forEachIndexed { _, showPhotoItemBean ->
            showPhotoItemBean.checkViewIsShow = false
            showPhotoItemBean.checked = false
        }
        isEdit = false
        handler.sendEmptyMessage(UPDATE_DELETE_FILE_RESULT)
    }

    override fun showOrHideDeleteLayout(isShow: Boolean) {
        if (isShow) {
            handler.sendEmptyMessage(SHOW_DELETE_LAYOUT)
        } else {
            handler.sendEmptyMessage(HIDE_DELETE_LAYOUT)
        }
    }

    override fun updateCategoryName(categoryName: String) {
        runOnUiThread {
            tvCategory!!.text = categoryName
        }
    }

    class MyHandler(thumbnailActivity: ImageThumbnailActivity) : Handler() {
        private val mThumbnailActivity: WeakReference<ImageThumbnailActivity> =
            WeakReference(thumbnailActivity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val thumbnailActivity: ImageThumbnailActivity? = mThumbnailActivity.get()
            when (msg.what) {
                UPDATE_PHOTO_RECYCLER_VIEW -> {
                    thumbnailActivity!!.thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
                }
                HIDE_WAIT_DIALOG -> {
                    if (thumbnailActivity!!.waitDialog!!.isShowing) {
                        thumbnailActivity.waitDialog!!.dismiss()
                    }
                }
                UPDATE_WAIT_DIALOG -> {
                    if (thumbnailActivity!!.waitDialog!!.isShowing) {
                        thumbnailActivity.waitDialog!!.setMessage(msg.obj as String)
                    }
                }
                SHOW_SNACK_BAR -> {
                    Snackbar.make(
                        thumbnailActivity!!.rvPhotoThumbnails!!,
                        msg.obj as String,
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
                HIDE_LOAD_PHOTO_DIALOG -> {
                    if (thumbnailActivity!!.loadPhotoDialog!!.isShowing) {
                        thumbnailActivity.loadPhotoDialog!!.dismiss()
                    }
                }
                UPDATE_LOAD_PHOTO_DIALOG -> {
                    if (thumbnailActivity!!.loadPhotoDialog!!.isShowing) {
                        thumbnailActivity.loadPhotoDialog!!.setProgress(msg.obj as Float)
                    }
                }
                UPDATE_DELETE_FILE_RESULT -> {
                    thumbnailActivity!!.thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
                    thumbnailActivity.deleteList.clear()
                }
                SHOW_DELETE_LAYOUT -> {
                    thumbnailActivity!!.llDeleteCancelLayout!!.visibility = View.VISIBLE
                }
                HIDE_DELETE_LAYOUT -> {
                    thumbnailActivity!!.llDeleteCancelLayout!!.visibility = View.GONE
                }
            }
        }
    }
}
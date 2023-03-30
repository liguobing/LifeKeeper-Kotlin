package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.ShowVideoRecyclerViewAdapter
import com.lixyz.lifekeeperforkotlin.adapter.VideoThumbnailMoveVideoBottomDialogAdapter
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.ShowVideoItemBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideosForCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.pass.VideoThumbnailToShowVideoDetail
import com.lixyz.lifekeeperforkotlin.presenter.VideoThumbnailListPresenter
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.CustomLoadVideoDialog


class VideoThumbnailListActivity : BaseActivity(), IVideoThumbnailView, View.OnClickListener {

    /**
     * Presenter
     */
    private var presenter: VideoThumbnailListPresenter? = null

    /**
     * 分类列表（用于移动视频到其他分类）
     */
    private var otherCategoryList: ArrayList<VideoCategoryBean>? = ArrayList()

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
    private var tvMoveVideo: TextView? = null

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
    private var rvVideoThumbnails: RecyclerView? = null

    /**
     * 缩略图 RecyclerView adapter
     */
    private var thumbnailRecyclerViewAdapter: ShowVideoRecyclerViewAdapter? = null

    /**
     * 缩略图 RecyclerView 数据 List
     */
    private var thumbnailRecyclerViewDataList: ArrayList<ShowVideoItemBean>? = ArrayList()

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * 载入照片 Dialog
     */
    private var loadPhotoDialog: CustomLoadVideoDialog? = null

    /**
     * 要删除的图片列表
     */
    private val deleteList = ArrayList<ShowVideoItemBean>()

    /**
     * 是否是编辑状态
     * 编辑状态的话，
     * 1.图片的选中按钮显示
     * 2.toolbar 的删除按钮显示
     * 3.单击按钮，选中图片，而不是打开图片
     */
    private var isEdit: Boolean = false

    private var videoData: VideosForCategoryBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // StatusBar 设置为透明
        window.statusBarColor = Color.parseColor("#4CE19F")
        setContentView(R.layout.activity___video_thumbnail)
        currentCategoryId = intent.getStringExtra("CategoryId")!!
        currentCategoryPassword = intent.getStringExtra("Password")!!
        currentCategoryName = intent.getStringExtra("CategoryName")!!
        initWidget()
    }

    override fun onStart() {
        super.onStart()
        initListener()
    }

    private var currentCategoryId: String? = null
    private var currentCategoryPassword: String? = null
    private var currentCategoryName: String? = null

    override fun onResume() {
        super.onResume()
        thumbnailRecyclerViewDataList!!.clear()
        presenter!!.loadMoreVideo(
            this,
            currentCategoryId!!,
            currentCategoryPassword!!,
            1,
            51
        )
        if (otherCategoryList?.size == 0) {
            presenter!!.getOtherCategory(this, currentCategoryId)
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
        if (data != null) {
            val resultData =
                data!!.getSerializableExtra("Result") as VideoThumbnailToShowVideoDetail
            thumbnailRecyclerViewDataList!!.clear()
            thumbnailRecyclerViewDataList!!.addAll(resultData.dataList!!)
            thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
        }
    }


    override fun initWidget() {
        waitDialog = CustomDialog(this, this, "请稍后...")
        loadPhotoDialog = CustomLoadVideoDialog(this, this)
        presenter = VideoThumbnailListPresenter(this)
        thumbnailRecyclerViewAdapter = ShowVideoRecyclerViewAdapter(
            thumbnailRecyclerViewDataList!!,
            this
        )
        imgBack = findViewById(R.id.img_back)
        tvCategory = findViewById(R.id.tv_category)
        tvCategory!!.text = currentCategoryName
        llDeleteCancelLayout = findViewById(R.id.ll_delete_cancel_layout)
        tvDelete = findViewById(R.id.tv_delete)
        tvMoveVideo = findViewById(R.id.tv_move_video)
        tvCancel = findViewById(R.id.tv_cancel)
        imgBack = findViewById(R.id.img_back)
        rvVideoThumbnails = findViewById(R.id.rv_videos)
        rvVideoThumbnails!!.layoutManager = GridLayoutManager(this, 3)
        rvVideoThumbnails!!.adapter = thumbnailRecyclerViewAdapter!!

        refreshLayout = findViewById(R.id.refreshLayout)
        refreshLayout!!.setEnableRefresh(false)
    }

    /**
     * 当前分页
     */
    private var currentPage = 1

    override fun initListener() {
        imgBack!!.setOnClickListener(this)
        tvDelete!!.setOnClickListener(this)
        tvMoveVideo!!.setOnClickListener(this)
        tvCancel!!.setOnClickListener(this)
        thumbnailRecyclerViewAdapter!!.setOnItemClickListener(object :
            ShowVideoRecyclerViewAdapter.OnItemClickListener {
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
                        Intent(this@VideoThumbnailListActivity, TikTokActivity::class.java)
                    intent.putExtra("Position", position)
                    intent.putExtra(
                        "CategoryId",
                        thumbnailRecyclerViewDataList!![0].video!!.fileCategory
                    )
                    intent.putExtra("Password", currentCategoryPassword)
                    startActivityForResult(intent, 1)
                }
            }
        })
        thumbnailRecyclerViewAdapter!!.setOnItemLongClickListener(object :
            ShowVideoRecyclerViewAdapter.OnItemLongClickListener {
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
                presenter!!.loadMoreVideo(
                    this@VideoThumbnailListActivity,
                    currentCategoryId!!,
                    currentCategoryPassword!!,
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
                        presenter!!.deleteFile(deleteList, this)
                    }
                    builder.show()
                } else {
                    Snackbar.make(tvCategory!!, "还没选择要删除的视频呢", Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.tv_move_video -> {
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
                    R.layout.view___show_video_change_category_bottom_dialog,
                    RelativeLayout(this),
                    false
                )
        val listView: ListView = contentView.findViewById(R.id.lv_category)
        listView.adapter = VideoThumbnailMoveVideoBottomDialogAdapter(this, otherCategoryList!!)
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                bottomDialog.dismiss()
                if (otherCategoryList!![position].categoryName == "创建新相册" && otherCategoryList!![position].categoryId == "add_new_category" && otherCategoryList!![position].objectId == "add_new_category") {
                    showCreateNewCategoryDialog()
                } else {
                    presenter!!.changeVideoCategory(
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
                        return@forEach
                    }
                }
                val newCategory = VideoCategoryBean()
                newCategory.objectId = StringUtil.getRandomString()
                newCategory.categoryId = StringUtil.getRandomString()
                newCategory.categoryName = edit.text.toString()
                newCategory.categoryUser = otherCategoryList!![0].categoryUser
                newCategory.isPrivate = -1
                newCategory.categoryStatus = 1
                newCategory.createTime = System.currentTimeMillis()
                otherCategoryList!!.add(0, newCategory)
                presenter!!.changeVideoCategory(this, newCategory, deleteList)
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
        runOnUiThread {
            if (waitDialog!!.isShowing) {
                waitDialog!!.dismiss()
            }
        }
    }

    override fun updateWaitDialog(message: String) {
        runOnUiThread {
            if (waitDialog!!.isShowing) {
                waitDialog!!.setMessage(message)
            }
        }
    }

    override fun showSnackBar(message: String) {
        runOnUiThread {
            Snackbar.make(
                rvVideoThumbnails!!,
                message,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun updateRecyclerView(list: ArrayList<ShowVideoItemBean>) {
        runOnUiThread {
            this.thumbnailRecyclerViewDataList!!.addAll(list)
            currentPage++
            thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
        }
    }

    override fun updateOtherCategoryList(list: ArrayList<VideoCategoryBean>) {
        list.forEachIndexed { _, videoCategoryBean ->
            otherCategoryList!!.add(videoCategoryBean)
        }
        otherCategoryList = list
        val bean = VideoCategoryBean()
        bean.categoryName = "创建新分类"
        bean.categoryId = "add_new_category_object_id"
        bean.objectId = "add_new_category_category_id"
        otherCategoryList!!.add(bean)
    }

    override fun hideLoadVideoDialog() {
        runOnUiThread {
            if (loadPhotoDialog!!.isShowing) {
                loadPhotoDialog!!.dismiss()
            }
        }
    }

    override fun updateLoadVideoDialog(progress: Float) {
        runOnUiThread {
            if (loadPhotoDialog!!.isShowing) {
                loadPhotoDialog!!.setProgress(progress)
            }
        }
    }

    override fun updateDeleteFileResult(list: ArrayList<ShowVideoItemBean>) {
        thumbnailRecyclerViewDataList!!.removeAll(list)
        thumbnailRecyclerViewDataList!!.forEachIndexed { _, showPhotoItemBean ->
            showPhotoItemBean.checkViewIsShow = false
            showPhotoItemBean.checked = false
        }
        isEdit = false
        runOnUiThread {
            thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
            deleteList.clear()
        }
    }

    override fun updateMoveFileResult(list: ArrayList<ShowVideoItemBean>) {
        thumbnailRecyclerViewDataList!!.removeAll(list)
        thumbnailRecyclerViewDataList!!.forEachIndexed { _, showPhotoItemBean ->
            showPhotoItemBean.checkViewIsShow = false
            showPhotoItemBean.checked = false
        }
        isEdit = false
        runOnUiThread {
            thumbnailRecyclerViewAdapter!!.notifyDataSetChanged()
            deleteList.clear()
        }
    }

    override fun showOrHideDeleteLayout(isShow: Boolean) {
        if (isShow) {
            runOnUiThread {
                llDeleteCancelLayout!!.visibility = View.VISIBLE
            }
        } else {
            runOnUiThread {
                llDeleteCancelLayout!!.visibility = View.GONE
            }
        }
    }
}
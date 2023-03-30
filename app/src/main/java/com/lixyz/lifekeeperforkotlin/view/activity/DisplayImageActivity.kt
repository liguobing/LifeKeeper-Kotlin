package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.WindowInsetsController
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.DisplayImageRecyclerViewAdapter
import com.lixyz.lifekeeperforkotlin.adapter.ImageThumbnailMoveImageBottomDialogAdapter
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.presenter.DisplayImagePresenter
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.utils.RecyclerItemClickListener
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import interfaces.heweather.com.interfacesmodule.view.HeContext.context
import java.io.File


class DisplayImageActivity : BaseActivity(), IDisplayImageView, View.OnClickListener {

    private var categoryId: String? = null
    private var password: String? = null
    private var position = 0
    private var waitDialog: CustomDialog? = null

    private var rvImages: RecyclerView? = null
    private var imageList: ArrayList<ImageBean>? = null
    private var adapter: DisplayImageRecyclerViewAdapter? = null
    private var layoutManager: LinearLayoutManager? = null

    private var presenter: DisplayImagePresenter? = null

    private var toolbarIsShow = false

    private var tbToolBarTop: Toolbar? = null
    private var imgBack: ImageView? = null
    private var imgRotate: ImageView? = null

    private var tbToolBarBottom: Toolbar? = null
    private var imgDelete: ImageView? = null
    private var imgMoveImage: ImageView? = null
    private var imgHD: ImageView? = null
    private var imgDownload: ImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        val view = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        view.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT


        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity___display_image)

        if (intent != null) {
            categoryId = intent.getStringExtra("CategoryId")
            password = intent.getStringExtra("Password")
            position = intent.getIntExtra("Position", 0)
        }

        initWidget()

    }

    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
//            设置 Toolbar padding
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
           tbToolBarTop!!.setPadding(0, resources.getDimensionPixelSize(resourceId), 0, 0)
        }
    }

    override fun initWidget() {
        waitDialog = CustomDialog(this, this, "Loading...")
        rvImages = findViewById(R.id.rv_images)
        imageList = ArrayList()
        adapter = DisplayImageRecyclerViewAdapter(this, imageList!!)
        rvImages!!.adapter = adapter
        layoutManager = LinearLayoutManager(this)
        rvImages!!.layoutManager = layoutManager
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvImages)


        presenter = DisplayImagePresenter()
        presenter!!.getImages(this, this, categoryId!!, password!!)

        tbToolBarTop = findViewById(R.id.toolbar_top)
        imgBack = findViewById(R.id.img_back)
        imgRotate = findViewById(R.id.img_rotate)

        tbToolBarBottom = findViewById(R.id.toolbar_bottom)
        imgDelete = findViewById(R.id.img_delete)
        imgMoveImage = findViewById(R.id.img_move_image)
        imgHD = findViewById(R.id.img_hd)
        imgDownload = findViewById(R.id.img_download)
    }

    override fun initListener() {
        imgBack!!.setOnClickListener(this)
        imgRotate!!.setOnClickListener(this)
        imgDelete!!.setOnClickListener(this)
        imgMoveImage!!.setOnClickListener(this)
        imgHD!!.setOnClickListener(this)
        imgDownload!!.setOnClickListener(this)

        rvImages!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                tbToolBarTop!!.visibility = View.GONE
                tbToolBarBottom!!.visibility = View.GONE
            }
        })

        rvImages!!.addOnItemTouchListener(
            RecyclerItemClickListener(
                context,
                rvImages,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        toolbarIsShow = tbToolBarBottom!!.visibility == View.GONE
                        if (toolbarIsShow) {
                            tbToolBarBottom!!.visibility = View.VISIBLE
                            tbToolBarTop!!.visibility = View.VISIBLE
                        } else {
                            tbToolBarBottom!!.visibility = View.GONE
                            tbToolBarTop!!.visibility = View.GONE
                        }
                        toolbarIsShow = !toolbarIsShow
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                    }
                })
        )
    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun updateWaitDialog(message: String) {

    }

    override fun hideWaitDialog() {
        if (waitDialog!!.isShowing) {
            runOnUiThread {
                waitDialog!!.dismiss()
            }
        }
    }

    override fun showSnackBar(message: String) {
        runOnUiThread {
            Snackbar.make(rvImages!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showPhotoLoadingDialog() {
    }

    override fun updatePhotoLoadingDialog(progress: Float) {
    }

    override fun hidePhotoLoadingDialog() {
    }

    override fun updateViewPager(file: File) {
    }

    override fun deleteFileSuccessUpdateRecyclerView(position: Int) {
        runOnUiThread {
            imageList!!.removeAt(position)
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun updateImageCategoryList(categoryList: ArrayList<ImageCategoryBean>) {
    }

    override fun moveImageSuccess(bean: ImageBean) {
    }

    override fun updateImages(images: ArrayList<ImageBean>) {
        this.imageList!!.clear()
        this.imageList!!.addAll(images)
        runOnUiThread {
            adapter!!.notifyDataSetChanged()
            rvImages!!.scrollToPosition(position)
        }
    }

    override fun showMoveImageBottomDialog(categoryList: ArrayList<ImageCategoryBean>) {
        runOnUiThread {
            val bottomDialog = Dialog(this, R.style.BottomDialog)
            val contentView: View =
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.view___show_image_change_category_bottom_dialog,
                        RelativeLayout(this),
                        false
                    )
            val listView: ListView = contentView.findViewById(R.id.lv_category)
            listView.adapter = ImageThumbnailMoveImageBottomDialogAdapter(this, categoryList)
            listView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    bottomDialog.dismiss()

                    if (categoryList[position].categoryName == "创建新相册" && categoryList[position].categoryId == "add_new_category_object_id" && categoryList[position].objectId == "add_new_category_category_id") {
                        showCreateNewCategoryDialog(
                            categoryList,
                            layoutManager!!.findFirstCompletelyVisibleItemPosition()
                        )
                    } else {
                        presenter!!.moveImage(
                            this,
                            this,
                            categoryList[position],
                            imageList!![layoutManager!!.findFirstVisibleItemPosition()],
                            layoutManager!!.findFirstVisibleItemPosition()
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
    }

    private fun showCreateNewCategoryDialog(
        categoryList: ArrayList<ImageCategoryBean>,
        position: Int
    ) {
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
                categoryList.forEach {
                    if (it.categoryName == edit.text.toString()) {
                        showSnackBar("分类名称已存在，无需重复添加")
                        return@setPositiveButton
                    }
                }
                val newCategory = ImageCategoryBean()
                newCategory.objectId = StringUtil.getRandomString()
                newCategory.categoryId = StringUtil.getRandomString()
                newCategory.categoryName = edit.text.toString()
                newCategory.categoryUser = categoryList[0].categoryUser
                newCategory.isPrivate = -1
                newCategory.password = ""
                newCategory.categoryStatus = 1
                newCategory.categoryType = 0
                newCategory.createTime = System.currentTimeMillis()
                newCategory.updateTime = 0
                categoryList.add(0, newCategory)
                presenter!!.moveImageToNewCategory(
                    this,
                    this,
                    newCategory,
                    imageList!![position],
                    position
                )
            }
        }
        builder.show()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_back -> {
                finish()
            }
            R.id.img_rotate -> {
                val view =
                    layoutManager!!.findViewByPosition(layoutManager!!.findFirstVisibleItemPosition())
                var degree = view!!.rotation
                degree += 90
                view.rotation = degree
            }
            R.id.img_delete -> {
                val position = layoutManager!!.findFirstVisibleItemPosition()
                val deleteDialog = AlertDialog.Builder(this)
                deleteDialog.setMessage("确定要删除吗？")
                deleteDialog.setPositiveButton(
                    "删除"
                ) { dialog, _ ->
                    dialog!!.dismiss()
                    val deleteList = ArrayList<String>()
                    deleteList.add(imageList!![position].objectId!!)
                    presenter!!.deleteImage(
                        this@DisplayImageActivity,
                        this@DisplayImageActivity,
                        position,
                        deleteList
                    )

                }
                deleteDialog.show()
            }
            R.id.img_move_image -> {
                val position = layoutManager!!.findFirstVisibleItemPosition()
                presenter!!.getOtherCategory(
                    this@DisplayImageActivity,
                    this@DisplayImageActivity,
                    imageList!![position]
                )
            }
            R.id.img_hd -> {
                val position = layoutManager!!.findFirstCompletelyVisibleItemPosition()
                val view = layoutManager!!.findViewByPosition(position)
                val image: ImageView = view!!.findViewById(R.id.img_image)
                val progress: ProgressBar = view.findViewById(R.id.progressBar)
                val sourceUrl =
                    Constant.CLOUD_ADDRESS + "/LifeKeeper/resource/LifeKeeperImage/" + imageList!![position].imageUser + "/source/" + imageList!![position].sourceFileName

                val coverUrl =
                    Constant.CLOUD_ADDRESS + "/LifeKeeper/resource/LifeKeeperImage/" + imageList!![position].imageUser + "/cover/" + imageList!![position].coverFileName

                Glide.with(this).load(sourceUrl).error(Glide.with(this).load(coverUrl)).into(
                    object : ImageViewTarget<Drawable>(image) {
                        override fun setResource(resource: Drawable?) {
                        }

                        override fun onLoadStarted(placeholder: Drawable?) {
                            super.onLoadStarted(placeholder)
                            progress.visibility = View.VISIBLE
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            progress.visibility = View.GONE
                            image.setImageResource(R.drawable.display_image_error)
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            super.onResourceReady(resource, transition)
                            progress.visibility = View.GONE
                            image.setImageDrawable(resource)
                        }
                    }
                )
            }
            R.id.img_download -> {
                val position = layoutManager!!.findFirstVisibleItemPosition()
                presenter!!.downloadImage(
                    this@DisplayImageActivity,
                    this@DisplayImageActivity,
                    imageList!![position]
                )
            }
        }
    }
}
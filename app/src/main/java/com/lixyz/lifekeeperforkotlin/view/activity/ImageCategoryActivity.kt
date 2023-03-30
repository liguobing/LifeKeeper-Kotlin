package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.ImageCategoryListViewAdapter
import com.lixyz.lifekeeperforkotlin.adapter.ImageCategorySelectFileBottomDialogAdapter
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.SelectFileBean
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.photo.ImageCategoryCover
import com.lixyz.lifekeeperforkotlin.presenter.ImageCategoryPresenter
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.CustomUploadDialog
import java.lang.ref.WeakReference


class ImageCategoryActivity : BaseActivity(), IImageCategoryView, AdapterView.OnItemClickListener,
    View.OnClickListener, AdapterView.OnItemLongClickListener {

    /**
     * presenter
     */
    private var presenter: ImageCategoryPresenter? = null

    /**
     * 分类 ListView
     */
    private var lvImageCategories: ListView? = null

    /**
     * ListView Adapter
     */
    private var adapter: ImageCategoryListViewAdapter? = null

    /**
     * 数据 List
     */
    private var dataList = ArrayList<ImageCategoryCover>()

    /**
     * Handler
     */
    private var handler = MyHandler(this)

    private var tvUpload: TextView? = null

    /**
     * 上传 Dialog
     */
    private var uploadDialog: CustomUploadDialog? = null

    companion object {
        private const val UPDATE_LIST_VIEW = 1000
        private const val HIDE_WAIT_DIALOG = 2000
        private const val SHOW_SNACK_BAR = 3000
    }

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null


    override fun onStart() {
        super.onStart()
        initListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity___image_category)
        initWidget()
        presenter!!.checkNeedUpload(this, this)
    }

    override fun onResume() {
        super.onResume()
        presenter!!.getCover(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter!!.activityDestroy()
    }

    override fun initWidget() {
        tvUpload = findViewById(R.id.tv_upload)
        presenter = ImageCategoryPresenter(this)
        lvImageCategories = findViewById(R.id.lv_image_categories)
        adapter = ImageCategoryListViewAdapter(this, dataList)
        lvImageCategories!!.adapter = adapter

        waitDialog = CustomDialog(this, this, "请稍后")
        uploadDialog = CustomUploadDialog(this, this, "上传中")
    }

    override fun initListener() {
        lvImageCategories!!.onItemClickListener = this
        lvImageCategories!!.onItemLongClickListener = this
        tvUpload!!.setOnClickListener(this)
    }

    override fun showUpload() {
        runOnUiThread { tvUpload!!.visibility = View.VISIBLE }
    }

    override fun hideUpload() {
        runOnUiThread { tvUpload!!.visibility = View.GONE }
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ): Boolean {
        val editDialog = Dialog(this, R.style.BottomDialogStyle)
        //填充对话框的布局
        val v: View =
            LayoutInflater.from(this).inflate(
                R.layout.view___edit_image_category, RelativeLayout(
                    this
                ), false
            )
        //初始化控件
        val tvEditCategory = v.findViewById<View>(R.id.tv_edit_category) as TextView
        val tvDeleteCategory = v.findViewById<View>(R.id.tv_delete_category) as TextView
        tvDeleteCategory.setOnClickListener {
            editDialog.dismiss()
            showVerifyCategoryPasswordDialog(position, 1)
        }

        tvEditCategory.setOnClickListener {
            editDialog.dismiss()
            showVerifyCategoryPasswordDialog(position, 2)
        }
        //将布局设置给Dialog
        editDialog.setContentView(v)
        //获取当前Activity所在的窗体
        val dialogWindow: Window? = editDialog.window
        //设置Dialog从窗体底部弹出
        dialogWindow!!.setGravity(Gravity.BOTTOM)
        //获得窗体的属性
        val lp: WindowManager.LayoutParams = dialogWindow.attributes
        lp.width = (windowManager.currentWindowMetrics.bounds.width() * 0.95).toInt()
        lp.y = 20 //设置Dialog距离底部的距离
        dialogWindow.attributes = lp //将属性设置给窗体
        editDialog.show() //显示对话框
        return true
    }

    override fun showDeleteImageCategoryDialog(
        categoryName: String,
        categoryId: String,
        password: String
    ) {
        runOnUiThread {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage("删除相册照片也会随之一起删除，确定要删除 $categoryName 吗？")
            alertDialog.setNegativeButton("删除") { dialog, _ ->
                dialog.dismiss()
                presenter!!.deleteCategory(this, categoryId)
            }
            alertDialog.setPositiveButton("") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
        }
    }

    override fun showEditCategoryDialog(category: ImageCategoryBean) {
        runOnUiThread {
            val editDialog = Dialog(this, R.style.BottomDialogStyle)
            //填充对话框的布局
            val v: View =
                LayoutInflater.from(this).inflate(
                    R.layout.view___image_category___edit, RelativeLayout(
                        this
                    ), false
                )
            //初始化控件
            val tvCategoryName = v.findViewById<View>(R.id.tv_category_name) as TextView
            val etNewName = v.findViewById<View>(R.id.et_new_name) as EditText
            val cbPrivate = v.findViewById<View>(R.id.cb_private) as CheckBox
            val etPassword = v.findViewById<View>(R.id.et_password) as EditText
            val btSubmit = v.findViewById<View>(R.id.bt_submit_edit) as Button

            tvCategoryName.text = category.categoryName
            etPassword.visibility = View.GONE

            cbPrivate.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    etPassword.visibility = View.VISIBLE
                } else {
                    etPassword.visibility = View.GONE
                }
            }

            btSubmit.setOnClickListener {
                if (TextUtils.isEmpty(etNewName.text)) {
                    showSnackBar("相册名不能为空")
                    return@setOnClickListener
                }

                if (cbPrivate.isChecked) {
                    if (TextUtils.isEmpty(etPassword.text)) {
                        showSnackBar("加密相册需要设置密码")
                        return@setOnClickListener
                    }
                }

                category.categoryName = etNewName.text.trim().toString()
                if (cbPrivate.isChecked) {
                    category.isPrivate = 1
                    category.password = etPassword.text.trim().toString()
                } else {
                    category.isPrivate = -1
                    category.password = ""
                }
                category.updateTime = System.currentTimeMillis()
                presenter!!.updateImageCategory(this@ImageCategoryActivity, category)
                editDialog.dismiss()
            }
            //将布局设置给Dialog
            editDialog.setContentView(v)
            //获取当前Activity所在的窗体
            val dialogWindow: Window? = editDialog.window
            //设置Dialog从窗体底部弹出
            dialogWindow!!.setGravity(Gravity.BOTTOM)
            //获得窗体的属性
            val lp: WindowManager.LayoutParams = dialogWindow.attributes
            lp.width = (windowManager.currentWindowMetrics.bounds.width() * 0.95).toInt()
            lp.y = 20 //设置Dialog距离底部的距离
            dialogWindow.attributes = lp //将属性设置给窗体
            editDialog.show() //显示对话框
        }
    }


    private fun showVerifyCategoryPasswordDialog(position: Int, type: Int) {
        val passwordDialog = Dialog(this, R.style.BottomDialogStyle)
        //填充对话框的布局
        val v: View =
            LayoutInflater.from(this).inflate(
                R.layout.view___image_category___verify_image_password, RelativeLayout(
                    this
                ), false
            )
        //初始化控件
        val tvCategoryName = v.findViewById<View>(R.id.tv_category_name) as TextView
        val tvIsPrivate = v.findViewById<View>(R.id.tv_category_private) as TextView
        val tvPasswordType = v.findViewById<View>(R.id.tv_password_type) as TextView
        val etPassword = v.findViewById<View>(R.id.et_password) as EditText
        val btSubmitVerify = v.findViewById<View>(R.id.bt_submit_verify) as Button
        tvCategoryName.text = dataList[position].category!!.categoryName
        if (dataList[position].category!!.isPrivate > 0) {
            tvIsPrivate.text = "加密账户"
            tvPasswordType.text = "相册密码"
        } else {
            tvIsPrivate.text = "普通账户"
            tvPasswordType.text = "账户密码"
        }

        btSubmitVerify.setOnClickListener {
            passwordDialog.dismiss()
            if (TextUtils.isEmpty(etPassword.text)) {
                showSnackBar("密码不能为空")
                return@setOnClickListener
            }
            presenter!!.verifyImageCategoryPassword(
                this@ImageCategoryActivity,
                dataList[position].category!!,
                etPassword.text.trim().toString(),
                type
            )
        }
        //将布局设置给Dialog
        passwordDialog.setContentView(v)
        //获取当前Activity所在的窗体
        val dialogWindow: Window? = passwordDialog.window
        //设置Dialog从窗体底部弹出
        dialogWindow!!.setGravity(Gravity.BOTTOM)
        //获得窗体的属性
        val lp: WindowManager.LayoutParams = dialogWindow.attributes
        lp.width = (windowManager.currentWindowMetrics.bounds.width() * 0.95).toInt()
        lp.y = 20 //设置Dialog距离底部的距离
        dialogWindow.attributes = lp //将属性设置给窗体
        passwordDialog.show() //显示对话框
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
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


    override fun updateView(list: ArrayList<ImageCategoryCover>) {
        dataList.clear()
        dataList.addAll(list)
        handler.sendEmptyMessage(UPDATE_LIST_VIEW)
    }

    class MyHandler(activity: ImageCategoryActivity) : Handler(Looper.getMainLooper()) {
        private val mActivity: WeakReference<ImageCategoryActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity: ImageCategoryActivity? = mActivity.get()
            when (msg.what) {
                UPDATE_LIST_VIEW -> {
                    activity!!.adapter!!.notifyDataSetChanged()
                }
                HIDE_WAIT_DIALOG -> {
                    if (activity!!.waitDialog!!.isShowing) {
                        activity.waitDialog!!.dismiss()
                    }
                }
                SHOW_SNACK_BAR -> {
                    Snackbar.make(
                        activity!!.lvImageCategories!!,
                        msg.obj as String,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        clickCategoryId = dataList[position].category!!.categoryId!!
        if (dataList[position].category!!.isPrivate > 0) {
            showVerifyCategoryPasswordDialog(position, 0)
        } else {
            val intent = Intent(this, ImageThumbnailActivity::class.java)
            intent.putExtra("CategoryId", clickCategoryId)
            intent.putExtra("Password", StringUtil.string2MD5("", ""))
            intent.putExtra("CategoryName", dataList[position].category!!.categoryName)
            startActivity(intent)
        }
    }

    private var clickCategoryId: String = ""

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_upload -> {
                val isHasStoragePermission = Environment.isExternalStorageManager()
                if (!isHasStoragePermission) {
                    val builder =
                        AlertDialog.Builder(this).setMessage("备份照片需要授予所有文件的管理权限")
                            .setNegativeButton(
                                "去授予"
                            ) { dialog, _ ->
                                dialog.dismiss()
                                val intent = Intent()
                                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                                startActivity(intent)
                            }
                    builder.show()
                } else {
                    presenter!!.getNeedUploadFile(this, this)
                }
            }
        }
    }

    private var bottomDialog: Dialog? = null

    override fun uploadDone(){
        runOnUiThread {
            presenter!!.getCover(this)
        }
    }


    override fun showBottomUploadDialog(list: ArrayList<SelectFileBean>) {
        runOnUiThread {
            var selectCount = 0
            bottomDialog = Dialog(this, R.style.BottomDialog)
            val contentView: View =
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.view___image_category_select_file_bottom_dialog,
                        RelativeLayout(this),
                        false
                    )
            val gridView: GridView = contentView.findViewById(R.id.gv_images)
            val btStartUpload: Button = contentView.findViewById(R.id.bt_start_upload)
            val btAddExcept: Button = contentView.findViewById(R.id.bt_add_except)
            val adapter = ImageCategorySelectFileBottomDialogAdapter(this, list)
            gridView.adapter = adapter
            gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                list[position].checked = !list[position].checked
                if (list[position].checked) {
                    selectCount++
                } else {
                    selectCount--
                }
                adapter.notifyDataSetChanged()
            }

            btStartUpload.setOnClickListener {
                bottomDialog!!.dismiss()
                val selectNameList = ArrayList<SelectFileBean>()
                if (selectCount > 0) {
                    list.forEachIndexed { _, selectFileBean ->
                        if (selectFileBean.checked) {
                            selectNameList.add(selectFileBean)
                        }
                    }
                    presenter!!.uploadImage(this@ImageCategoryActivity, selectNameList)
                } else {
                    showSnackBar("没选中")
                }
            }
            btAddExcept.setOnClickListener {
                val selectFileSet = HashSet<String>()
                if (selectCount > 0) {
                    list.forEachIndexed { _, selectFileBean ->
                        if (selectFileBean.checked) {
                            selectFileSet.add(selectFileBean.id)
                        }
                    }
                    presenter!!.addExceptImage(
                        this@ImageCategoryActivity,
                        this@ImageCategoryActivity,
                        selectFileSet
                    )
                }
            }
            bottomDialog!!.setContentView(contentView)
            val layoutParams = contentView.layoutParams
            layoutParams.width = resources.displayMetrics.widthPixels
            layoutParams.height = 2000
            contentView.layoutParams = layoutParams
            bottomDialog!!.window!!.setGravity(Gravity.BOTTOM)
            bottomDialog!!.window!!.setWindowAnimations(R.style.BottomDialog_Animation)
            bottomDialog!!.show()
        }
    }

    override fun hideUploadBottomDialog() {
        runOnUiThread {
            bottomDialog!!.dismiss()
        }
    }

    override fun showUploadDialog() {
        runOnUiThread {
            if (!uploadDialog!!.isShowing) {
                uploadDialog!!.show()
            }
        }
    }

    override fun hideUploadDialog() {
        runOnUiThread {
            if (uploadDialog!!.isShowing) {
                uploadDialog!!.dismiss()
            }
            presenter!!.checkNeedUpload(this, this)
        }
    }

    override fun updateUploadDialog(message: String, progress: Int) {
        runOnUiThread {
            if (uploadDialog!!.isShowing) {
                uploadDialog!!.updateDialog(message, progress)
            }
        }
    }

    override fun startImageActivity(categoryId: String, categoryName: String, password: String) {
        runOnUiThread {
            val intent = Intent(this, ImageThumbnailActivity::class.java)
            intent.putExtra("CategoryId", categoryId)
            intent.putExtra("Password", StringUtil.string2MD5("",password))
            intent.putExtra("CategoryName", categoryName)
            startActivity(intent)
        }
    }
}
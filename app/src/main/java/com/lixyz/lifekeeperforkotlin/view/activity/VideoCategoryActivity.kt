package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.VideoCategoryListViewAdapter
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.VideoCategoryBean
import com.lixyz.lifekeeperforkotlin.bean.video.VideoCategoryCover
import com.lixyz.lifekeeperforkotlin.presenter.VideoCategoryPresenter
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.CustomUploadDialog

class VideoCategoryActivity : BaseActivity(), IVideoCategoryView, AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener, View.OnClickListener {

    private var presenter: VideoCategoryPresenter? = null

    private var lvVideoCategories: ListView? = null

    private var adapter: VideoCategoryListViewAdapter? = null

    private var waitDialog: CustomDialog? = null

    /**
     * 数据 List
     */
    private var dataList = ArrayList<VideoCategoryCover>()

    private var tvNeedUpload: TextView? = null

    /**
     * 上传 Dialog
     */
    private var uploadDialog: CustomUploadDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity___video_category)
        initWidget()
        presenter!!.checkNeedUpload(this, this)
    }

    override fun onStart() {
        super.onStart()
        initListener()
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
        uploadDialog = CustomUploadDialog(this, this, "上传中")
        presenter = VideoCategoryPresenter(this)
        lvVideoCategories = findViewById(R.id.lv_video_categories)
        adapter = VideoCategoryListViewAdapter(this, dataList)
        lvVideoCategories!!.adapter = adapter

        tvNeedUpload = findViewById(R.id.tv_upload)

        waitDialog = CustomDialog(this, this, "请稍后")
    }

    override fun initListener() {
        tvNeedUpload!!.setOnClickListener(this)
        lvVideoCategories!!.onItemClickListener = this
        lvVideoCategories!!.onItemLongClickListener = this
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
        runOnUiThread {
            if (waitDialog!!.isShowing) {
                waitDialog!!.dismiss()
            }
        }
    }

    override fun showSnackBar(message: String) {
        runOnUiThread {
            Snackbar.make(
                lvVideoCategories!!,
                message,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun updateView(list: ArrayList<VideoCategoryCover>) {
        dataList.clear()
        dataList.addAll(list)
        runOnUiThread {
            adapter!!.notifyDataSetChanged()
        }
    }

    private var clickCategoryId: String = ""

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        clickCategoryId = dataList[position].category!!.categoryId!!
        if (dataList[position].category!!.isPrivate > 0) {
            showVerifyCategoryPasswordDialog(position, 0)
        } else {
            val intent = Intent(this, VideoThumbnailListActivity::class.java)
            intent.putExtra("CategoryId", clickCategoryId)
            intent.putExtra("Password", StringUtil.string2MD5("", ""))
            intent.putExtra("CategoryName", dataList[position].category!!.categoryName)
            startActivity(intent)
        }
    }

    override fun showUploadBottomButton() {
        runOnUiThread {
            tvNeedUpload!!.visibility = View.VISIBLE
        }
    }

    override fun hideUploadBottomButton() {
        runOnUiThread {
            tvNeedUpload!!.visibility = View.GONE
        }
    }

    override fun updateUploadDialog(message: String, progress: Int) {
        runOnUiThread {
            if (uploadDialog!!.isShowing) {
                uploadDialog!!.updateDialog(message, progress)
            }
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

    override fun uploadDone() {
        runOnUiThread {
            presenter!!.getCover(this)
        }
    }

    private fun showVerifyCategoryPasswordDialog(position: Int, type: Int) {
        val passwordDialog = Dialog(this, R.style.BottomDialogStyle)
        //填充对话框的布局
        val v: View =
            LayoutInflater.from(this).inflate(
                R.layout.view___video_category___verify_image_password, RelativeLayout(
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
            tvIsPrivate.text = "加密分类"
            tvPasswordType.text = "分类密码"
        } else {
            tvIsPrivate.text = "普通分类"
            tvPasswordType.text = "账户密码"
        }

        btSubmitVerify.setOnClickListener {
            passwordDialog.dismiss()
            if (TextUtils.isEmpty(etPassword.text)) {
                showSnackBar("密码不能为空")
                return@setOnClickListener
            }
            presenter!!.verifyImageCategoryPassword(
                this@VideoCategoryActivity,
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
//        //获得窗体的属性
        val lp: WindowManager.LayoutParams = dialogWindow.attributes
        lp.width = (windowManager.currentWindowMetrics.bounds.width() * 0.95).toInt()
        lp.y = 20 //设置Dialog距离底部的距离
        dialogWindow.attributes = lp //将属性设置给窗体
        passwordDialog.show() //显示对话框


    }

    override fun startVideoActivity(categoryId: String, categoryName: String, password: String) {
        runOnUiThread {
            val intent = Intent(this, VideoThumbnailListActivity::class.java)
            intent.putExtra("CategoryId", categoryId)
            intent.putExtra("Password", password)
            intent.putExtra("CategoryName", categoryName)
            startActivity(intent)
        }
    }

    override fun showDeleteVideoCategoryDialog(
        categoryName: String,
        categoryId: String,
        password: String
    ) {
        runOnUiThread {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage("删除分类视频也会随之一起删除，确定要删除 $categoryName 吗？")
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

    override fun showEditCategoryDialog(category: VideoCategoryBean) {
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
                presenter!!.updateVideoCategory(this@VideoCategoryActivity, category)
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
                R.layout.view___edit_video_category, RelativeLayout(
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_upload -> {
                val isHasStoragePermission = Environment.isExternalStorageManager()
                if (!isHasStoragePermission) {
                    val builder =
                        AlertDialog.Builder(this).setMessage("备份视频需要授予所有文件的管理权限")
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
                    presenter!!.upload(this, this)
                }
            }
        }
    }
}
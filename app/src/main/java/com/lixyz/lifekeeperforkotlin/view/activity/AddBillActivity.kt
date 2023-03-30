package com.lixyz.lifekeeperforkotlin.view.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.AddBillAccountListAdapter
import com.lixyz.lifekeeperforkotlin.adapter.AddBillCategoryListAdapter
import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount
import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDrawerLayout
import com.lixyz.lifekeeperforkotlin.view.customview.CustomEditText
import com.lixyz.lifekeeperforkotlin.view.customview.LoadingView
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import jp.wasabeef.glide.transformations.BlurTransformation


class AddBillActivity :
    AppCompatActivity(), View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * Toolbar
     */
    private var toolbar: Toolbar? = null

    /**
     * Toolbar 标题
     */
    private var tvToolBarTitle: TextView? = null

    /**
     * 抽屉菜单
     */
    private var drawer: CustomDrawerLayout? = null

    /**
     * 账单金额
     */
    private var etBillMoney: EditText? = null

    /**
     * 账单照片
     */
    private var imgBillImage: ImageView? = null

    /**
     * 上传图片进度条
     */
    private var pbUploadImageProgress: ProgressBar? = null

    /**
     * 账单分类
     */
    private var tvBillCategory: TextView? = null

    /**
     * 账单账户
     */
    private var tvBillAccount: TextView? = null

    /**
     * 账单日期
     */
    private var btBillDate: Button? = null

    /**
     * 账单商家
     */
    private var btBillShop: Button? = null

    /**
     * 账单备注
     */
    private var etBillRemark: CustomEditText? = null

    /**
     * 保存账单
     */
    private var btSave: Button? = null

    /**
     * 清空输入
     */
    private var btClear: Button? = null

    /**
     * 抽屉布局标题
     */
    private var tvDrawerLayoutTitle: TextView? = null

    /**
     * 抽屉布局菜单
     */
    private var lvDrawerLayoutMenu: ListView? = null

    /**
     * 抽屉布局添加菜单 Item
     */
    private var btDrawerLayoutAddMenuItem: Button? = null

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null


    companion object {
        /**
         * 设置图片 RequestCode
         */
        private const val BILL_IMAGE_REQUEST_CODE = 10086
    }

    /**
     * 账单分类 ListView Adapter
     */
    private var categoryListViewAdapter: AddBillCategoryListAdapter? = null

    /**
     * 账单分类数据列表
     */
    private var billCategories: ArrayList<BillCategory>? = null

    /**
     * 账单账户 ListView Adapter
     */
    private var accountListViewAdapter: AddBillAccountListAdapter? = null

    /**
     * 账单账户数据列表
     */
    private var billAccounts: ArrayList<BillAccount>? = null

    /**
     * 收入/支出：
     * 1 ： 收入；-1 ： 支出
     */
    private var billProperty = 0

    /**
     * ListView 展示的是分类还是账户
     * 1：分类；-1：账户
     */
    private var listViewFlag = 0

    /**
     * 账单详情图片 Dialog
     */
    private var imageDetailDialog: AlertDialog? = null

    /**
     * 屏幕宽高
     */
    private var screenWidth = 0

    /**
     * 屏幕宽高
     */
    private var screenHeight: Int = 0

    /**
     * ListView Menu 添加 Item Dialog
     */
    private var addMenuItemDialog: AlertDialog? = null

    /**
     * 保存/更新成功
     */
    private var saveBillSuccessful = false


    private var startShopActivityLaunch: ActivityResultLauncher<Intent>? = null
    private var viewModel: AddBillViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //没有 TitleBar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //设置状态栏颜色
        window.statusBarColor = resources.getColor(R.color.AddBillActivity_ToolBarColor, null)
        setContentView(R.layout.activity___add_bill)

        initWidget()

        val provider = ViewModelProvider(this)
        viewModel = provider[AddBillViewModel::class.java]
        viewModel!!.createBill(billProperty)
        viewModel!!.snackBarLiveData.observe(this) {
            Snackbar.make(tvBillAccount!!, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel!!.waitDialogLiveData.observe(this) {
            if (!waitDialog!!.isShowing) {
                waitDialog!!.show()
            } else {
                if (waitDialog!!.isShowing) {
                    waitDialog!!.dismiss()
                }
            }
        }
        viewModel!!.billAccountLiveData.observe(this) {
            billAccounts!!.clear()
            billAccounts!!.addAll(it)
            accountListViewAdapter!!.notifyDataSetChanged()
        }
        viewModel!!.billCategoryLiveData.observe(this) {
            billCategories!!.clear()
            billCategories!!.addAll(it)
            categoryListViewAdapter!!.notifyDataSetChanged()
        }
        viewModel!!.billImageStatusLiveData.observe(this) {
            if (it != null) {
                pbUploadImageProgress!!.visibility = View.GONE
                imgBillImage!!.isClickable = true
                Glide.with(this).load(it)
                    .into(imgBillImage!!)
            } else {
                pbUploadImageProgress!!.visibility = View.GONE
                imgBillImage!!.isClickable = true
                Glide.with(this@AddBillActivity).load(R.drawable.add_bill___load_img_error)
                    .into(imgBillImage!!)
            }
        }
        viewModel!!.addAccountAndCategoryDialogLiveData.observe(this) {
            if (!it) {
                addMenuItemDialog!!.dismiss()
            }
        }
        viewModel!!.dateLiveData.observe(this) {
            btBillDate!!.text = it
        }
        viewModel!!.saveDialogLiveData.observe(this) {
            saveBillResult = it
            if (it) {
                loadingView!!.success(true)
            } else {
                loadingView!!.success(false)
            }
        }

        viewModel!!.getBillCategoryAndAccount(this, billProperty)


        initListener()


        startShopActivityLaunch =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                //此处进行数据接收
                if (it.resultCode == Activity.RESULT_OK) {
                    val shopName: String = it.data!!.getStringExtra("SelectShop")!!
                    viewModel!!.bill!!.billShop = shopName
                    if (shopName.length > 3) {
                        val str = shopName.substring(0, 3)
                        btBillShop!!.text =
                            String.format(resources.getString(R.string.add_bill_shop_name), str)
                    } else {
                        btBillShop!!.text = shopName
                    }
                }
            }
    }

    private var saveBillResult: Boolean = false

    override fun onResume() {
        super.onResume()
        viewModel!!.bill!!.billProperty = billProperty
        viewModel!!.bill!!.billDate = System.currentTimeMillis()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.activityOnDestroy()
    }

    fun initWidget() {
        billProperty = intent.getIntExtra("BillProperty", 0)
        //Toolbar
        toolbar = findViewById(R.id.toolbar)
        tvToolBarTitle = findViewById(R.id.tv_toolbar_title)
        drawer = findViewById(R.id.drawer)
        etBillMoney = findViewById(R.id.et_bill_money)
        imgBillImage = findViewById(R.id.img_bill_image)
        pbUploadImageProgress = findViewById(R.id.pb_upload_image_progress)
        tvBillCategory = findViewById(R.id.tv_bill_category)
        tvBillAccount = findViewById(R.id.tv_bill_account)
        btBillDate = findViewById(R.id.bt_bill_date)
        val currentTime = System.currentTimeMillis()
        btBillDate!!.text = StringUtil.milliToString(currentTime, true)
        btBillShop = findViewById(R.id.bt_bill_shop)
        etBillRemark = findViewById(R.id.et_bill_remark)
        btSave = findViewById(R.id.bt_save)
        btClear = findViewById(R.id.bt_clear)
        tvDrawerLayoutTitle = findViewById(R.id.tv_list_view_title)
        if (billProperty > 0) {
            tvToolBarTitle!!.text = "收入"
            tvBillCategory!!.text = "收入分类"
            tvBillAccount!!.text = "收入账户"
        } else {
            tvToolBarTitle!!.text = "支出"
            tvBillCategory!!.text = "支出分类"
            tvBillAccount!!.text = "支出账户"
        }
        lvDrawerLayoutMenu = findViewById(R.id.lv_menu)
        btDrawerLayoutAddMenuItem = findViewById(R.id.add_menu)

        waitDialog = CustomDialog(this, this, "请稍后...")

        billCategories = ArrayList()
        billAccounts = ArrayList()
        categoryListViewAdapter =
            AddBillCategoryListAdapter(this, billCategories!!)
        accountListViewAdapter =
            AddBillAccountListAdapter(this, billAccounts!!)
    }

    fun initListener() {
        imgBillImage!!.setOnClickListener(this)
        tvBillCategory!!.setOnClickListener(this)
        tvBillAccount!!.setOnClickListener(this)
        lvDrawerLayoutMenu!!.onItemClickListener = this
        btBillDate!!.setOnClickListener(this)
        btBillShop!!.setOnClickListener(this)
        btClear!!.setOnClickListener(this)
        btSave!!.setOnClickListener(this)
        btDrawerLayoutAddMenuItem!!.setOnClickListener(this)
        toolbar!!.setNavigationOnClickListener { finish() }
        waitDialog!!.setOnDismissListener { dialogInterface ->
            dialogInterface.dismiss()
            if (saveBillSuccessful) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private var loadingView: LoadingView? = null

    private fun showBottomDialog() {
        val bottomDialog = Dialog(this, R.style.BottomDialog)
        val contentView: View =
            LayoutInflater.from(this)
                .inflate(
                    R.layout.view___add_bill_save_loading,
                    RelativeLayout(this),
                    false
                )
        loadingView = contentView.findViewById(R.id.loading_view)
        loadingView!!.setOnProgressFinishedListener(object :
            LoadingView.OnProgressFinishedListener {
            override fun onFinished() {
                if (saveBillResult) {
                    bottomDialog.dismiss()
                    finish()
                } else {
                    bottomDialog.dismiss()
                    Snackbar.make(tvBillAccount!!, "保存失败，请检查后重试", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
        bottomDialog.setCancelable(false)
        bottomDialog.setCanceledOnTouchOutside(false)
        bottomDialog.setContentView(contentView)
        val layoutParams = contentView.layoutParams
        layoutParams.width = resources.displayMetrics.widthPixels
        contentView.layoutParams = layoutParams
        bottomDialog.window!!.setGravity(Gravity.BOTTOM)
        bottomDialog.window!!.setWindowAnimations(R.style.BottomDialog_Animation)
        val attributes = bottomDialog.window!!.attributes
        attributes.height = dp2px(this, 230F)
        bottomDialog.window!!.attributes = attributes
        bottomDialog.show()
        loadingView!!.start()
    }

    /**
     * dp转换成px
     */
    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BILL_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Glide.with(this).load(Matisse.obtainPathResult(data)[0])
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(imgBillImage!!)
            imgBillImage!!.isClickable = false
            pbUploadImageProgress!!.visibility = View.VISIBLE
            val animation = RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            animation.interpolator = LinearInterpolator()
            animation.duration = 1000
            animation.repeatCount = -1
            pbUploadImageProgress!!.animation = animation
            animation.startNow()
            viewModel!!.uploadBillImage(Matisse.obtainPathResult(data)[0], this)
        }
    }

    private fun clearUI() {
        viewModel!!.createBill(billProperty)

        etBillMoney!!.text = null
        if (billProperty > 0) {
            tvBillCategory!!.text = "收入分类"
            tvBillAccount!!.text = "收入账户"
        } else {
            tvBillCategory!!.text = "支出分类"
            tvBillAccount!!.text = "支出账户"
        }
        val currentTime = System.currentTimeMillis()
        btBillDate!!.text = StringUtil.milliToString(currentTime, true)
        viewModel!!.bill!!.billDate = currentTime
        btBillShop!!.text = "商家"
        etBillRemark!!.text = null
        imgBillImage!!.setImageResource(R.drawable.add_bill___camera)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
            screenHeight = size.y
        }
    }

    private fun showAddItemDialog() {
        val addMenuItemView: View =
            layoutInflater.inflate(R.layout.view___add_bill___add_menu_dialog, LinearLayout(this))
        val inputContent = addMenuItemView.findViewById<EditText>(R.id.et_input_content)
        val submitMenuItem: Button = addMenuItemView.findViewById(R.id.bt_submit_add_menu)
        if (listViewFlag > 0) {
            inputContent.hint = "输入新的账单分类"
        } else {
            inputContent.hint = "输入新的账户名称"
        }
        submitMenuItem.setOnClickListener {
            hideSoftInput(submitMenuItem)
            viewModel!!.saveListViewItem(this, inputContent.text, listViewFlag, billProperty)
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(addMenuItemView)
        addMenuItemDialog = builder.create()
        val window = addMenuItemDialog!!.window
        window?.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources, R.drawable.add_bill___dialog___add_menu___background,
                null
            )
        )
        addMenuItemDialog!!.show()
    }

    override fun onClick(v: View?) {
        hideSoftInput(btClear!!)
        when (v!!.id) {
            //账单图片
            R.id.img_bill_image -> {
                if (viewModel!!.bill!!.billImage == null) {
                    addImage(this)
                } else {
                    val builder = AlertDialog.Builder(this)
                    val view: View = layoutInflater.inflate(
                        R.layout.view___add_bill___dialog___image_detail,
                        LinearLayout(this)
                    )
                    val image: ImageView = view.findViewById(R.id.img_bill_image_detail)
                    val deleteImage: ImageView = view.findViewById(R.id.img_delete_bill_image)
                    deleteImage.setOnClickListener(this)
                    Glide.with(this)
                        .load(viewModel!!.bill!!.billImage)
                        .error(Glide.with(this).load(R.drawable.add_bill___load_img_error))
                        .into(image)
                    builder.setView(view)
                    imageDetailDialog = builder.create()
                    imageDetailDialog!!.show()
                }
            }
            //账单日期
            R.id.bt_bill_date -> {
                viewModel!!.setBillDate(this)
            }
            //账单商家
            R.id.bt_bill_shop -> {
                val intent = Intent(this, BillShopActivity::class.java)
                startShopActivityLaunch!!.launch(intent)
            }
            //保存账单
            R.id.bt_save -> {
                showBottomDialog()
                viewModel!!.saveBill(etBillMoney!!.text, etBillRemark!!.text!!, this)
            }
            //清空输入
            R.id.bt_clear -> {
                clearUI()
            }
            //账单分类
            R.id.tv_bill_category -> {
                lvDrawerLayoutMenu!!.adapter = categoryListViewAdapter
                categoryListViewAdapter!!.notifyDataSetChanged()
                if (drawer!!.isDrawerOpen(GravityCompat.END)) {
                    drawer!!.closeDrawers()
                }
                drawer!!.openDrawer(GravityCompat.END)
                tvDrawerLayoutTitle!!.text = "分类"
                listViewFlag = 1
            }
            //账单账户
            R.id.tv_bill_account -> {
                lvDrawerLayoutMenu!!.adapter = accountListViewAdapter
                accountListViewAdapter!!.notifyDataSetChanged()
                if (drawer!!.isDrawerOpen(GravityCompat.END)) {
                    drawer!!.closeDrawers()
                }
                drawer!!.openDrawer(GravityCompat.END)
                tvDrawerLayoutTitle!!.text = "账户"
                listViewFlag = -1
            }
            //删除账单图片
            R.id.img_delete_bill_image -> {
                imgBillImage!!.setImageResource(R.drawable.add_bill___camera)
                imageDetailDialog!!.dismiss()
                viewModel!!.bill!!.billImage = null
            }
            //添加账单分类/账单账户
            R.id.add_menu -> {
                showAddItemDialog()
            }
            else -> {
            }
        }
    }

    private fun addImage(activity: Activity?) {
        if (hasExternalPermission()) {
            Matisse.from(activity)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .capture(true)
                .captureStrategy(
                    CaptureStrategy(
                        true,
                        "com.lixyz.lifekeeperforkotlin.matisse.fileprovider",
                        "test"
                    )
                )
                .maxSelectable(9)
                .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(
                    resources.getDimensionPixelSize(R.dimen.grid_expected_size)
                )
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(Glide4Engine())
                .showSingleMediaType(true)
                .originalEnable(true)
                .maxOriginalSize(10)
                .autoHideToolbarOnSingleTap(true)
                .setOnCheckedListener {
                }
                .forResult(10086)
        } else {
            Snackbar.make(tvBillAccount!!, "您需要授予存储权限，才能添加账单图片", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * 检查是否具有读取存储权限
     *
     * @return 是否有权限
     */
    private fun hasExternalPermission(): Boolean {
        val i =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return i == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 隐藏软键盘
     */
    private fun hideSoftInput(view: View) {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (listViewFlag > 0) {
            tvBillCategory!!.text = billCategories!![position].categoryName
            viewModel!!.bill!!.billCategory = billCategories!![position].categoryId
        } else {
            tvBillAccount!!.text = billAccounts!![position].accountName
            viewModel!!.bill!!.billAccount = billAccounts!![position].accountId
        }
        drawer!!.closeDrawers()
    }
}
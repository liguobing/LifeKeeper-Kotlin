package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.BillCategoryDragItemHelperCallBack
import com.lixyz.lifekeeperforkotlin.adapter.BillCategoryRecyclerViewAdapter
import com.lixyz.lifekeeperforkotlin.adapter.BillCategoryViewPagerAdapter
import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory
import com.lixyz.lifekeeperforkotlin.presenter.BillCategoryViewModel
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import java.lang.ref.WeakReference


/**
 *
 *
 * @author LGB
 */
class BillCategoryActivity : AppCompatActivity(),
    BillCategoryRecyclerViewAdapter.OnItemClickListener,
    IBillCategoryView, View.OnClickListener, OnPageChangeListener {

    /**
     * toolbar 收入 title
     */
    private var tvHeadIncome: TextView? = null

    /**
     * toolbar 支出 title
     */
    private var tvHeadExpend: TextView? = null

    /**
     * ViewPager
     */
    private var pager: ViewPager? = null

    /**
     * 添加收入分类
     */
    private var btAddIncomeCategory: Button? = null

    /**
     * 添加支出分类
     */
    private var btAddExpendCategory: Button? = null

    /**
     * Handler
     */
    private val handler = MyHandler(this)

    /**
     * 等待 Dialog
     */
    private var dialog: CustomDialog? = null

    /**
     * 单击 RecyclerView 的 Item 下标
     */
    private var clickItemIndex = 0

    /**
     * 收入分类数据 List
     */
    private val incomeDataList: ArrayList<BillCategory> = ArrayList()

    /**
     * 支出分类数据 List
     */
    private val expendDataList: ArrayList<BillCategory> = ArrayList()

    /**
     * 收入分类列表 adapter
     */
    private var incomeCategoryAdapter: BillCategoryRecyclerViewAdapter? = null

    /**
     * 支出分类列表 adapter
     */
    private var expendCategoryAdapter: BillCategoryRecyclerViewAdapter? = null

    /**
     * 单击 RecyclerView Item 显示的 AlertDialog
     */
    private var clickCategoryDialog: AlertDialog? = null

    /**
     * 单击 RecyclerView Item 显示的 AlertDialog 的标题
     */
    private var tvDialogTitle: TextView? = null

    /**
     * 单击 RecyclerView Item 显示的 AlertDialog 的删除按钮
     */
    private var tvDeleteCategory: TextView? = null

    /**
     * 单击 RecyclerView Item 显示的 AlertDialog 的编辑按钮
     */
    private var tvEditCategory: TextView? = null

    /**
     * 添加新分类 dialog
     */
    private var addCategoryDialog: AlertDialog? = null

    /**
     * 添加新分类按钮
     */
    private var btAddCategory: TextView? = null

    /**
     * 分类名称
     */
    private var etCategoryName: EditText? = null

    /**
     * 编辑分类 dialog
     */
    private var updateCategoryDialog: AlertDialog? = null

    /**
     * 旧分类名称
     */
    private var tvOldCategoryName: TextView? = null

    /**
     * 新分类名称
     */
    private var etNewCategoryName: EditText? = null

    /**
     * 更新分类名称按钮
     */
    private var btUpdateCategory: Button? = null

    /**
     * Activity 第一次创建
     */
    private var firstLaunch = true

    private var viewModel: BillCategoryViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#529AF8")
        setContentView(R.layout.activity___bill_category)
        val viewModelProvider = ViewModelProvider(this)
        viewModel = viewModelProvider[BillCategoryViewModel::class.java]
        initWidget()
        viewModel!!.incomeCategoryLiveData!!.observe(this) {
            incomeDataList.clear()
            incomeDataList.addAll(it)
            incomeCategoryAdapter!!.notifyDataSetChanged()
        }
        viewModel!!.expendCategoryLiveData!!.observe(this) {
            expendDataList.clear()
            expendDataList.addAll(it)
            expendCategoryAdapter!!.notifyDataSetChanged()
        }
        viewModel!!.waitDialogLiveData!!.observe(this) {
            if (it) {
                if (!dialog!!.isShowing) {
                    dialog!!.show()
                }
            } else {
                if (dialog!!.isShowing) {
                    dialog!!.dismiss()
                }
            }
        }
        viewModel!!.snackBarLiveData!!.observe(this) {
            Snackbar.make(pager!!, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel!!.getBillCategory(this)
    }

    override fun onStart() {
        super.onStart()
        if (firstLaunch) {
            initListener()
        }
    }

    override fun onResume() {
        super.onResume()
        if (firstLaunch) {
//            presenter!!.activityOnResume()
            firstLaunch = false
        }
    }

    fun initWidget() {
        pager = findViewById(R.id.vp_category)
        val viewList: ArrayList<View> = ArrayList()
        val incomeView: View = layoutInflater.inflate(
            R.layout.view___bill_category___viewpager___income,
            LinearLayout(this),
            false
        )
        val expendView: View = layoutInflater.inflate(
            R.layout.view___bill_category___viewpager___expend,
            LinearLayout(this),
            false
        )
        viewList.add(incomeView)
        viewList.add(expendView)
        val adapter = BillCategoryViewPagerAdapter(viewList, this)
        pager!!.adapter = adapter
        tvHeadIncome = findViewById(R.id.tv_income)
        tvHeadExpend = findViewById(R.id.tv_expend)
        val rvIncomeCategory: RecyclerView = incomeView.findViewById(R.id.rv_income_list)
        incomeCategoryAdapter = BillCategoryRecyclerViewAdapter(this, incomeDataList, viewModel!!,this)
        rvIncomeCategory.adapter = incomeCategoryAdapter
        rvIncomeCategory.layoutManager = GridLayoutManager(this, 5)
        val incomeItemTouchHelper = ItemTouchHelper(BillCategoryDragItemHelperCallBack())
        incomeItemTouchHelper.attachToRecyclerView(rvIncomeCategory)
        btAddIncomeCategory = incomeView.findViewById(R.id.bt_add_income)
        val rvExpendCategory: RecyclerView = expendView.findViewById(R.id.rv_expend_list)
        expendCategoryAdapter = BillCategoryRecyclerViewAdapter(this, expendDataList, viewModel!!,this)
        rvExpendCategory.adapter = expendCategoryAdapter
        rvExpendCategory.layoutManager = GridLayoutManager(this, 5)
        val expendItemTouchHelper = ItemTouchHelper(BillCategoryDragItemHelperCallBack())
        expendItemTouchHelper.attachToRecyclerView(rvExpendCategory)
        btAddExpendCategory = expendView.findViewById(R.id.bt_add_expend)
        dialog = CustomDialog(this, this, "请稍后...")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(
            R.layout.view___bill_category___alert_dialog,
            LinearLayout(this),
            false
        )
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title)
        tvDeleteCategory = view.findViewById(R.id.tv_delete_category)
        tvEditCategory = view.findViewById(R.id.tv_edit_category)
        builder.setView(view)
        clickCategoryDialog = builder.create()
        val window: Window = clickCategoryDialog!!.window!!
        window.setGravity(Gravity.BOTTOM)
        val addCategoryBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val addCategoryView: View = layoutInflater.inflate(
            R.layout.view___bill_category___dialog___add_category,
            LinearLayout(this),
            false
        )
        btAddCategory = addCategoryView.findViewById(R.id.bt_submit_add)
        etCategoryName = addCategoryView.findViewById(R.id.et_category_name)
        addCategoryBuilder.setView(addCategoryView)
        addCategoryDialog = addCategoryBuilder.create()
        val editCategoryBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val editCategoryView: View = layoutInflater.inflate(
            R.layout.view___bill_category___dialog___edit_category,
            LinearLayout(this),
            false
        )
        tvOldCategoryName = editCategoryView.findViewById(R.id.tv_old_category_name)
        etNewCategoryName = editCategoryView.findViewById(R.id.et_new_category_name)
        btUpdateCategory = editCategoryView.findViewById(R.id.bt_submit_edit)
        editCategoryBuilder.setView(editCategoryView)
        updateCategoryDialog = editCategoryBuilder.create()
    }

    fun initListener() {
        pager!!.addOnPageChangeListener(this)
        tvHeadIncome!!.setOnClickListener(this)
        tvHeadExpend!!.setOnClickListener(this)
        btAddIncomeCategory!!.setOnClickListener(this)
        btAddExpendCategory!!.setOnClickListener(this)
        incomeCategoryAdapter!!.setOnItemClickListener(this)
        expendCategoryAdapter!!.setOnItemClickListener(this)
        tvDeleteCategory!!.setOnClickListener(this)
        tvEditCategory!!.setOnClickListener(this)
        btAddCategory!!.setOnClickListener(this)
        btUpdateCategory!!.setOnClickListener(this)
    }

    override fun showSnackBar(message: String?) {
        val msg: Message = Message.obtain()
        msg.what = SHOW_SNACK_BAR
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun showWaitDialog() {
        if (!dialog!!.isShowing) {
            dialog!!.show()
        }
    }

    override fun hideWaitDialog() {
        handler.sendEmptyMessage(HIDE_WAIT_DIALOG)
    }

    override fun updateWaitDialog(message: String?) {
        val msg: Message = Message.obtain()
        msg.what = UPDATE_WAIT_DIALOG
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun updateIncomeList(list: ArrayList<BillCategory>?) {
        incomeDataList.clear()
        incomeDataList.addAll(list!!)
        handler.sendEmptyMessage(UPDATE_INCOME_CATEGORIES)
    }

    override fun updateExpendList(list: ArrayList<BillCategory>?) {
        expendDataList.clear()
        expendDataList.addAll(list!!)
        handler.sendEmptyMessage(UPDATE_EXPEND_CATEGORIES)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_income -> {
                pager!!.currentItem = 0
            }
            R.id.tv_expend -> {
                pager!!.currentItem = 1
            }
            R.id.bt_add_income, R.id.bt_add_expend -> {
                etCategoryName!!.text = null
                addCategoryDialog!!.show()
            }
            R.id.tv_edit_category -> {
                clickCategoryDialog!!.dismiss()
                if (pager!!.currentItem == 0) {
                    tvOldCategoryName!!.text = incomeDataList[clickItemIndex].categoryName
                    tvOldCategoryName!!.setTextColor(Color.parseColor("#36C37E"))
                } else {
                    tvOldCategoryName!!.text = expendDataList[clickItemIndex].categoryName
                    tvOldCategoryName!!.setTextColor(Color.parseColor("#FF524F"))
                }
                etNewCategoryName!!.text = null
                updateCategoryDialog!!.show()
            }
            R.id.tv_delete_category -> {
                clickCategoryDialog!!.dismiss()
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@BillCategoryActivity)
                val message: String = if (pager!!.currentItem == 0) {
                    incomeDataList[clickItemIndex].categoryName!!
                } else {
                    expendDataList[clickItemIndex].categoryName!!
                }
                builder.setMessage("确定删除 $message?")
                builder.setPositiveButton(
                    "删除"
                ) { _, _ ->
                    if (pager!!.currentItem == 0) {
                        viewModel!!.deleteBillCategory(
                            this@BillCategoryActivity,
                            incomeDataList,
                            clickItemIndex
                        )
                    } else {
                        viewModel!!.deleteBillCategory(
                            this@BillCategoryActivity,
                            expendDataList,
                            clickItemIndex
                        )
                    }
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            }
            R.id.bt_submit_add -> {
                addCategoryDialog!!.dismiss()
                viewModel!!.addBillCategory(
                    this@BillCategoryActivity,
                    pager!!.currentItem,
                    etCategoryName!!.text
                )
            }
            R.id.bt_submit_edit -> {
                updateCategoryDialog!!.dismiss()
                if (pager!!.currentItem == 0) {
                    viewModel!!.updateCategory(
                        this@BillCategoryActivity,
                        incomeDataList[clickItemIndex],
                        etNewCategoryName!!.text
                    )
                } else {
                    viewModel!!.updateCategory(
                        this@BillCategoryActivity,
                        expendDataList[clickItemIndex],
                        etNewCategoryName!!.text
                    )
                }
            }
            else -> {
            }
        }
    }

    override fun onPageScrolled(i: Int, v: Float, i1: Int) {

    }

    override fun onPageSelected(i: Int) {
        if (i == 0) {
            tvHeadIncome!!.textSize = 25f
            tvHeadIncome!!.setTextColor(Color.WHITE)
            tvHeadExpend!!.textSize = 20f
            tvHeadExpend!!.setTextColor(Color.parseColor("#E2DFDF"))
        } else {
            tvHeadExpend!!.textSize = 25f
            tvHeadExpend!!.setTextColor(Color.WHITE)
            tvHeadIncome!!.textSize = 20f
            tvHeadIncome!!.setTextColor(Color.parseColor("#E2DFDF"))
        }
    }

    override fun onPageScrollStateChanged(i: Int) {}
    override fun onItemClick(position: Int) {
        clickItemIndex = position
        if (pager!!.currentItem == 0) {
            tvDialogTitle!!.text =
                java.lang.String.format("操作：收入 - [%s]", incomeDataList[position].categoryName)
        } else {
            tvDialogTitle!!.text =
                java.lang.String.format("操作：支出 - [%s]", expendDataList[position].categoryName)
        }
        clickCategoryDialog!!.show()
    }

    private class MyHandler(activity: BillCategoryActivity) : Handler() {
        private val mActivity: WeakReference<BillCategoryActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity: BillCategoryActivity = mActivity.get()!!
            when (msg.what) {
                SHOW_SNACK_BAR -> {
                    Snackbar.make(
                        activity.tvHeadIncome!!,
                        (msg.obj as String), Snackbar.LENGTH_SHORT
                    ).show()
                }
                HIDE_WAIT_DIALOG -> {
                    if (activity.dialog!!.isShowing) {
                        activity.dialog!!.dismiss()
                    }
                }
                UPDATE_WAIT_DIALOG -> {
                    if (activity.dialog!!.isShowing) {
                        activity.dialog!!.setMessage((msg.obj as String))
                    }
                }
                UPDATE_INCOME_CATEGORIES -> {
                    activity.incomeCategoryAdapter!!.notifyDataSetChanged()
                }
                UPDATE_EXPEND_CATEGORIES -> {
                    activity.expendCategoryAdapter!!.notifyDataSetChanged()
                }
                else -> {
                }
            }
        }

    }

    companion object {
        /**
         * 显示SnackBar 消息
         */
        private const val SHOW_SNACK_BAR = 100

        /**
         * 隐藏等待 Dialog 消息
         */
        private const val HIDE_WAIT_DIALOG = 200

        /**
         * 更新等待 Dialog 消息
         */
        private const val UPDATE_WAIT_DIALOG = 300

        /**
         * 更新收入分类列表消息
         */
        private const val UPDATE_INCOME_CATEGORIES = 400

        /**
         * 更新支出分类列表消息
         */
        private const val UPDATE_EXPEND_CATEGORIES = 500
    }
}
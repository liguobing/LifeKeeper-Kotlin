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
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.BillAccountDragItemHelperCallBack
import com.lixyz.lifekeeperforkotlin.adapter.BillAccountRecyclerViewAdapter
import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount
import com.lixyz.lifekeeperforkotlin.presenter.BillAccountViewModel
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import java.lang.ref.WeakReference


/**
 * @author LGB
 */
class BillAccountActivity : AppCompatActivity(), BillAccountRecyclerViewAdapter.OnItemClickListener,
    IBillAccountView, View.OnClickListener {
    /**
     * 账户 RecyclerView
     */
    private var rvAccountList: RecyclerView? = null

    /**
     * 添加新账户按钮
     */
    private var btAddAccount: Button? = null

    /**
     * Adapter
     */
    private var adapter: BillAccountRecyclerViewAdapter? = null

    /**
     * 账户数据
     */
    private var accounts: ArrayList<BillAccount>? = null


    /**
     * Handler
     */
    private val handler = MyHandler(this)

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    /**
     * 添加新账户提交按钮
     */
    private var btSubmitAdd: Button? = null

    /**
     * 添加新账户名输入框
     */
    private var etAccountName: EditText? = null

    /**
     * 添加新账户 Dialog
     */
    private var addAccountDialog: AlertDialog? = null

    /**
     * 编辑/删除 Dialog 标题
     */
    private var tvDialogTitle: TextView? = null

    /**
     * 编辑/删除 Dialog 删除按钮
     */
    private var tvDeleteAccount: TextView? = null

    /**
     * 编辑/删除 Dialog 编辑按钮
     */
    private var tvEditAccount: TextView? = null

    /**
     * 编辑/删除 Dialog
     */
    private var clickAccountDialog: AlertDialog? = null

    /**
     * 单击 RecyclerView 的 Item 下标
     */
    private var clickItemIndex = 0

    /**
     * 修改账户名 Dialog - 旧账户名
     */
    private var tvOldAccountName: TextView? = null

    /**
     * 修改账户名  Dialog - 新账户名
     */
    private var etNewAccountName: EditText? = null

    /**
     * 修改账户名  Dialog - 提交修改按钮
     */
    private var btUpdateAccount: Button? = null

    /**
     * 修改账户名  Dialog
     */
    private var updateAccountDialog: AlertDialog? = null


    private var viewModel: BillAccountViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#529AF8")
        setContentView(R.layout.activity___bill_account)

        val viewModelProvider = ViewModelProvider(this)
        viewModel = viewModelProvider[BillAccountViewModel::class.java]
        initWidget()
        viewModel!!.waitDialogLiveData.observe(this) {
            if (it) {
                if (!waitDialog!!.isShowing) {
                    waitDialog!!.show()
                }
            } else {
                if (waitDialog!!.isShowing) {
                    waitDialog!!.dismiss()
                }
            }
        }
        viewModel!!.snackBarLiveData.observe(this) {
            Snackbar.make(rvAccountList!!, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel!!.accountListLiveData.observe(this) {
            accounts!!.clear()
            accounts!!.addAll(it)
            adapter!!.notifyDataSetChanged()
        }
        viewModel!!.getBillAccount(this)
    }


    override fun onStart() {
        super.onStart()
        initListener()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_add_account -> {
                etAccountName!!.text = null
                addAccountDialog!!.show()
            }
            R.id.bt_submit_add -> {
                addAccountDialog!!.dismiss()
                viewModel!!.addAccount(
                    this@BillAccountActivity,
                    etAccountName!!.text
                )
            }
            R.id.tv_delete_account -> {
                clickAccountDialog!!.dismiss()
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                val message: String = accounts!![clickItemIndex].accountName!!
                builder.setMessage("确定删除 $message?")
                builder.setPositiveButton(
                    "删除"
                ) { _, _ ->
                    viewModel!!.deleteBillAccount(
                        this@BillAccountActivity,
                        accounts!!,
                        clickItemIndex
                    )
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            }
            R.id.tv_edit_account -> {
                clickAccountDialog!!.dismiss()
                tvOldAccountName!!.text = accounts!![clickItemIndex].accountName
                tvOldAccountName!!.setTextColor(Color.parseColor("#36C37E"))
                etNewAccountName!!.text = null
                updateAccountDialog!!.show()
            }
            R.id.bt_submit_edit -> {
                updateAccountDialog!!.dismiss()
                viewModel!!.updateAccount(
                    this@BillAccountActivity,
                    accounts!![clickItemIndex],
                    etNewAccountName!!.text
                )
            }
            else -> {
            }
        }
    }

    fun initWidget() {
        waitDialog = CustomDialog(this, this, "请稍后...")
        rvAccountList = findViewById(R.id.rv_account_list)
        btAddAccount = findViewById(R.id.bt_add_account)
        accounts = ArrayList()
        adapter = BillAccountRecyclerViewAdapter(viewModel!!, accounts!!, this)
        rvAccountList!!.adapter = adapter
        rvAccountList!!.layoutManager = GridLayoutManager(this, 5)
        val itemTouchHelper = ItemTouchHelper(BillAccountDragItemHelperCallBack())
        itemTouchHelper.attachToRecyclerView(rvAccountList)
        val addAccountBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val addAccountView: View = layoutInflater.inflate(
            R.layout.view___bill_account___dialog___add_account,
            LinearLayout(this),
            false
        )
        btSubmitAdd = addAccountView.findViewById(R.id.bt_submit_add)
        etAccountName = addAccountView.findViewById(R.id.et_account_name)
        addAccountBuilder.setView(addAccountView)
        addAccountDialog = addAccountBuilder.create()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(
            R.layout.view___bill_account___alert_dialog,
            LinearLayout(this),
            false
        )
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title)
        tvDeleteAccount = view.findViewById(R.id.tv_delete_account)
        tvEditAccount = view.findViewById(R.id.tv_edit_account)
        builder.setView(view)
        clickAccountDialog = builder.create()
        val window: Window = clickAccountDialog!!.window!!
        window.setGravity(Gravity.BOTTOM)
        val editAccountBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val editAccountView: View = layoutInflater.inflate(
            R.layout.view___bill_account___dialog___edit_account,
            LinearLayout(this),
            false
        )
        tvOldAccountName = editAccountView.findViewById(R.id.tv_old_account_name)
        etNewAccountName = editAccountView.findViewById(R.id.et_new_account_name)
        btUpdateAccount = editAccountView.findViewById(R.id.bt_submit_edit)
        editAccountBuilder.setView(editAccountView)
        updateAccountDialog = editAccountBuilder.create()
    }

    fun initListener() {
        btAddAccount!!.setOnClickListener(this)
        btSubmitAdd!!.setOnClickListener(this)
        adapter!!.setOnItemClickListener(this)
        tvDeleteAccount!!.setOnClickListener(this)
        tvEditAccount!!.setOnClickListener(this)
        btUpdateAccount!!.setOnClickListener(this)
    }

    override fun showSnackBar(message: String?) {
        val msg: Message = Message.obtain()
        msg.what = SHOW_SNACK_BAR
        msg.obj = message
        handler.sendMessage(msg)
    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    override fun hideWaitDialog() {
        if (waitDialog!!.isShowing) {
            handler.sendEmptyMessage(HIDE_WAIT_DIALOG)
        }
    }

    override fun updateWaitDialog(message: String?) {
        if (waitDialog!!.isShowing) {
            val msg: Message = Message.obtain()
            msg.what = UPDATE_WAIT_DIALOG
            msg.obj = message
            handler.sendMessage(msg)
        }
    }

    override fun updateAccountList(list: ArrayList<BillAccount>?) {
        accounts!!.clear()
        accounts!!.addAll(list!!)
        handler.sendEmptyMessage(UPDATE_BILL_ACCOUNT_LIST)
    }

    override fun onItemClick(position: Int) {
        clickItemIndex = position
        tvDialogTitle!!.text =
            java.lang.String.format("操作：[%s]", accounts!![position].accountName)
        clickAccountDialog!!.show()
    }

    private class MyHandler(activity: BillAccountActivity) : Handler() {
        private val mActivity: WeakReference<BillAccountActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity: BillAccountActivity = mActivity.get()!!
            when (msg.what) {
                UPDATE_BILL_ACCOUNT_LIST -> {
                    activity.adapter!!.notifyDataSetChanged()
                }
                SHOW_SNACK_BAR -> {
                    Snackbar.make(
                        activity.btAddAccount!!,
                        (msg.obj as String), Snackbar.LENGTH_SHORT
                    ).show()
                }
                HIDE_WAIT_DIALOG -> {
                    activity.waitDialog!!.dismiss()
                }
                UPDATE_WAIT_DIALOG -> {
                    activity.waitDialog!!.setMessage(
                        (msg.obj as String)
                    )
                }
                else -> {
                }
            }
        }

    }

    companion object {
        /**
         * 更新 RecyclerView
         */
        private const val UPDATE_BILL_ACCOUNT_LIST = 100

        /**
         * 显示 SnackBar
         */
        private const val SHOW_SNACK_BAR = 200

        /**
         * 隐藏 WaitDialog
         */
        private const val HIDE_WAIT_DIALOG = 300

        /**
         * 更新 WaitDialog
         */
        private const val UPDATE_WAIT_DIALOG = 400
    }
}
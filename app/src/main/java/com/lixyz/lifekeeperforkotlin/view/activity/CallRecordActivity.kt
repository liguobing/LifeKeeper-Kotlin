package com.lixyz.lifekeeperforkotlin.view.activity

import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.adapter.CallRecordSelectFileBottomDialogAdapter
import com.lixyz.lifekeeperforkotlin.bean.SelectFileBean
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.RecordRecyclerViewItemBean
import com.lixyz.lifekeeperforkotlin.presenter.CallRecordPresenter
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import java.io.File

class CallRecordActivity : AppCompatActivity(), IPhoneRecordView, View.OnClickListener {

    /**
     * Presenter
     */
    private var presenter: CallRecordPresenter? = null

    /**
     * 等待 Dialog
     */
    private var waitDialog: CustomDialog? = null

    private var button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity___call_record)

        initWidget()
        initListener()

    }

    private fun initWidget() {
        waitDialog = CustomDialog(this, this, "请稍候...")
        presenter = CallRecordPresenter(this)
    }

    private fun initListener() {
        button = findViewById(R.id.button)
        button!!.setOnClickListener {
            showBottomDialog()
        }
    }


    private fun showBottomDialog() {
        val fileNameList = ArrayList<String>()
        val selectFileBean = ArrayList<SelectFileBean>()
        val fullNameList = ArrayList<String>()
        var selectCount = 0
        val parentPath: String = Environment.getExternalStorageDirectory().absolutePath
        val dir = File("$parentPath/MIUI/sound_recorder/call_rec/")
        val files = dir.listFiles { pathname ->
            pathname.isFile
        }
        files?.forEachIndexed { _, file ->
//            val pattern = "([a-zA-Z0-9\\u4e00-\\u9fa5\\s])*\\([0-9]*\\)_[0-9]*.mp3"
//            val r = Pattern.compile(pattern)
//            val m: Matcher = r.matcher(file.name)
//            if (m.matches()) {
//                if (!fileNameList.contains(file.name.split("_")[0])) {
//                    val bean = SelectFileBean(file.name.split("_")[0], false)
//                    selectFileBean.add(bean)
//                    fileNameList.add(file.name.split("_")[0])
//                }
//            }
//            fullNameList.add(file.name)
        }

        val bottomDialog = Dialog(this, R.style.BottomDialog)
        val contentView: View =
            LayoutInflater.from(this)
                .inflate(
                    R.layout.view___call_record_select_file_bottom_dialog,
                    RelativeLayout(this),
                    false
                )
        val listView: ListView = contentView.findViewById(R.id.lv_records)
        val btStartUpload: Button = contentView.findViewById(R.id.bt_start_upload)
        val adapter = CallRecordSelectFileBottomDialogAdapter(this, selectFileBean)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectFileBean[position].checked = !selectFileBean[position].checked
            if (selectFileBean[position].checked) {
                selectCount++
            } else {
                selectCount--
            }
            adapter.notifyDataSetChanged()
        }

        btStartUpload.setOnClickListener {
            bottomDialog.dismiss()
            val selectNameList = ArrayList<String>()
            val uploadFileNameList = ArrayList<String>()
            if (selectCount > 0) {
                selectFileBean.forEachIndexed { _, selectFileBean ->
                    if (selectFileBean.checked) {
                        selectNameList.add(selectFileBean.fileName)
                    }
                }
                fullNameList.forEachIndexed { _, s ->
                    if (selectNameList.contains(s.split("_")[0])) {
                        uploadFileNameList.add(s)
                    }
                }
                Log.d("TTT", "showBottomDialog: ${uploadFileNameList.size}")
                presenter!!.upload(this@CallRecordActivity, uploadFileNameList)
            } else {
                showSnackBar("没选中")
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

    override fun updatePhoneRecordRecyclerView(
        dataList: ArrayList<RecordRecyclerViewItemBean>,
        progressList: ArrayList<Float>
    ) {

    }

    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }


    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    override fun updateWaitDialog(message: String) {

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
            Snackbar.make(button!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun removeDeleteRecord(removeList: ArrayList<RecordRecyclerViewItemBean>) {

    }

    override fun updateRecyclerViewItem(position: Int) {

    }

    override fun onClick(v: View?) {

    }

}
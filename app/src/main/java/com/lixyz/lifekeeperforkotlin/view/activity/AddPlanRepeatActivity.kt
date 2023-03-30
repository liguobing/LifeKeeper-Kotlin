package com.lixyz.lifekeeperforkotlin.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseActivity
import java.util.*
import kotlin.collections.ArrayList


class AddPlanRepeatActivity : BaseActivity(), AdapterView.OnItemClickListener {
    /**
     * 重复模式 ListView
     */
    private var lvRepeat: ListView? = null

    /**
     * 开始时间
     */
    private var startTime: Long = 0

    /**
     * 星期选项
     */
    private val weeks = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity___add_plan_repeat)
        val intent = intent
        startTime = intent.getLongExtra("StartTime", System.currentTimeMillis())
    }

    override fun onStart() {
        super.onStart()
        initWidget()
    }

    override fun onResume() {
        super.onResume()
        lvRepeat!!.onItemClickListener = this
    }

    override fun initWidget() {
        lvRepeat = findViewById(R.id.lv_repeat)
        val list: ArrayList<String?> = ArrayList()
        list.add("一次性活动")
        list.add("每天")
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        list.add("每周(每周的" + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1] + ")")
        list.add("每月(每月的" + calendar.get(Calendar.DAY_OF_MONTH).toString() + "日)")
        list.add(
            "每年(" + (calendar.get(Calendar.MONTH) + 1).toString() + "月" + calendar.get(Calendar.DAY_OF_MONTH)
                .toString() + "日)"
        )
        val adapter: ArrayAdapter<String?> =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        lvRepeat!!.adapter = adapter
    }

    override fun initListener() {
        lvRepeat!!.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intent = Intent()
        intent.putExtra("RepeatTypeIndex", position)
        setResult(RESULT_OK, intent)
        finish()
    }
}
package com.lixyz.lifekeeperforkotlin.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseActivity


class AddPlanAlarmActivity : BaseActivity(), AdapterView.OnItemClickListener {
    /**
     * 返回按钮
     */
    private var imgBack: ImageView? = null

    /**
     * 闹钟时间 ListView
     */
    private var lvAlarmTime: ListView? = null

    /**
     * 闹钟列表
     */
    private var alarmTextList: ArrayList<String?> = ArrayList()

    /**
     * 闹钟时间列表
     */
    private val alarmTimeList: ArrayList<Int> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity___add_plan_alarm)
        initWidget()
    }

    override fun onResume() {
        super.onResume()
        initListener()
    }

    override fun initWidget() {
        imgBack = findViewById(R.id.alarm_img_cancel)
        lvAlarmTime = findViewById(R.id.lv_alarm_time)
        alarmTextList.add("不提醒")
        alarmTextList.add("活动开始时")
        alarmTextList.add("5 分钟前")
        alarmTextList.add("10 分钟前")
        alarmTextList.add("15 分钟前")
        alarmTextList.add("30 分钟前")
        alarmTextList.add("1 小时前")
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alarmTextList)
        lvAlarmTime!!.adapter = adapter
        alarmTimeList.add(-1)
        alarmTimeList.add(0)
        alarmTimeList.add(5)
        alarmTimeList.add(10)
        alarmTimeList.add(15)
        alarmTimeList.add(30)
        alarmTimeList.add(60)
    }

    override fun initListener() {
        imgBack!!.setOnClickListener { finish() }
        lvAlarmTime!!.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intent = Intent()
        intent.putExtra("AlarmText", alarmTextList[position])
        intent.putExtra("AlarmTime", alarmTimeList[position])
        setResult(RESULT_OK, intent)
        finish()
    }
}
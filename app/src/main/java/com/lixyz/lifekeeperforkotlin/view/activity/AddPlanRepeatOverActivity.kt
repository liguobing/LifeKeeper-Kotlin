package com.lixyz.lifekeeperforkotlin.view.activity

import ando.widget.pickerview.builder.OptionsPickerBuilder
import ando.widget.pickerview.builder.TimePickerBuilder
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.base.BaseActivity


/**
 * 创建日历，结束重复 Activity
 *
 * @author LGB
 */
class AddPlanRepeatOverActivity : BaseActivity(), View.OnClickListener {
    private var imgBack: ImageView? = null

    /**
     * 截止时间
     */
    private var tvTime: TextView? = null

    /**
     * 重复次数
     */
    private var tvCount: TextView? = null

    /**
     * 执行次数 Picker 数据源 List
     */
    private val list: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity___add_plan_repeat_over)
        initWidget()
        initListener()
    }

    override fun initWidget() {
        imgBack = findViewById(R.id.end_plan_img_cancel)
        tvTime = findViewById(R.id.end_plan_tv_time)
        tvCount = findViewById(R.id.end_plan_tv_count)
        for (i in 1..99) {
            list.add(i.toString())
        }
    }

    override fun initListener() {
        imgBack!!.setOnClickListener(this)
        tvTime!!.setOnClickListener(this)
        tvCount!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.end_plan_img_cancel -> {
                finish()
            }
            R.id.end_plan_tv_time -> {
                val datePicker = TimePickerBuilder(
                    this
                ) { date, _ ->
                    val intent = Intent()
                    intent.putExtra("EndPlanType", 0)
                    intent.putExtra("EndPlanTime", date)
                    setResult(RESULT_OK, intent)
                    finish()
                }.setCancelColor(Color.BLACK).setSubmitColor(Color.BLACK).setContentTextSize(20)
                    .setType(booleanArrayOf(true, true, true, false, false, false)).build()
                datePicker.show()
            }
            R.id.end_plan_tv_count -> {
                val countPicker = OptionsPickerBuilder(
                    this
                ) { options1, _, _, _ ->
                    val intent = Intent()
                    intent.putExtra("EndPlanType", 1)
                    intent.putExtra("ExecutePlanCount", Integer.valueOf(list[options1]))
                    setResult(RESULT_OK, intent)
                    finish()
                }.setCancelColor(Color.BLACK).setSubmitColor(Color.BLACK).setContentTextSize(20)
                    .build<String>()
                countPicker.setPicker(list)
                countPicker.show()
            }
        }
    }
}
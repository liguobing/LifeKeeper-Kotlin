package com.lixyz.lifekeeperforkotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.RecordRecyclerViewItemBean
import com.lixyz.lifekeeperforkotlin.utils.StringUtil
import com.lixyz.lifekeeperforkotlin.utils.TimeUtil
import com.lixyz.lifekeeperforkotlin.view.customview.CallRecordingView

class PhoneRecordRecyclerViewAdapter(
    private val recordList: ArrayList<RecordRecyclerViewItemBean>
) :
    RecyclerView.Adapter<PhoneRecordRecyclerViewAdapter.ViewHolder>() {


    /**
     * Item 点击监听器
     */
    private var mOnItemContentClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(
                R.layout.view___phone_record___recyclerview_item,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvContactName.text = recordList[position].contactName
        holder.tvCallTime.text = StringUtil.milliToString(recordList[position].callTime, false)
        if (recordList[position].checkBoxIsVisibility!!) {
            holder.cbCheckBox.visibility = View.VISIBLE
        } else {
            holder.cbCheckBox.visibility = View.GONE
        }

        holder.cbCheckBox.isChecked = recordList[position].checked!!

        if (recordList[position].progress > 0) {
            holder.recordPlayView.isPlaying = true
            holder.recordPlayView.setProgress(recordList[position].progress)
        } else {
            holder.recordPlayView.isPlaying = false
            holder.recordPlayView.setProgress(0f)
        }

        if (mOnItemContentClickListener != null) {
            holder.recordPlayView.setOnClickListener {
                val clickPosition = holder.layoutPosition
                mOnItemContentClickListener!!.onItemClick(clickPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemContentClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.mOnItemContentClickListener = mOnItemClickListener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvContactName: TextView = view.findViewById<View>(R.id.tv_contact_name) as TextView
        var tvCallTime: TextView = view.findViewById<View>(R.id.tv_call_time) as TextView
        var recordPlayView: CallRecordingView =
            view.findViewById(R.id.crv_player) as CallRecordingView
        var cbCheckBox: CheckBox = view.findViewById(R.id.cb_check)
    }

    /**
     * 判断position对应的Item是否是组的第一项
     */
    fun isItemHeader(position: Int, category: Int): Boolean {
        return if (position == 0) {
            true
        } else {
            val bean = recordList[position]
            val lastBean = recordList[position - 1]
            if (category == 0) {
                val billDate = bean.callTime
                val lastBillDate = lastBean.callTime
                !TimeUtil.millisTimeToShortFormatString(billDate)
                    .equals(TimeUtil.millisTimeToShortFormatString(lastBillDate))
            } else {
                bean.contactName != lastBean.contactName
            }
        }
    }

    fun getDate(position: Int): String {
        return TimeUtil.millisTimeToShortFormatString(recordList[position].callTime)!!
    }

    fun getName(position: Int): String {
        return recordList[position].contactName!!
    }
}
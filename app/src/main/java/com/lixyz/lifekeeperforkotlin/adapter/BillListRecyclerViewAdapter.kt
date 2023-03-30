package com.lixyz.lifekeeperforkotlin.adapter

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.bill.BillBean
import com.lixyz.lifekeeperforkotlin.utils.TimeUtil
import java.text.DecimalFormat


/**
 * BillListActivity RecyclerView Adapter
 *
 * @author LGB
 */
class BillListRecyclerViewAdapter(
    /**
     * 数据 List
     */
    private val mList: ArrayList<BillBean>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /**
     * Item 点击监听器
     */
    private var mOnItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view___account_book___listview_item_no_date, viewGroup, false)
        return NoHeadViewHolder(view)
    }

    class NoHeadViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        val position = viewHolder.adapterPosition
        val imgBillIcon: ImageView = viewHolder.itemView.findViewById(R.id.image_bill_type_icon)
        val tvBillCategory = viewHolder.itemView.findViewById<TextView>(R.id.text_bill_category)
        val tvBillRemark = viewHolder.itemView.findViewById<TextView>(R.id.text_bill_remark)
        val tvBillMoney = viewHolder.itemView.findViewById<TextView>(R.id.text_bill_money)
        val bean = mList[position]
        if (bean.billProperty > 0) {
            imgBillIcon.setImageResource(R.drawable.account_book___listview_item___income_icon)
        } else {
            imgBillIcon.setImageResource(R.drawable.account_book___listview_item___expend_icon)
        }
        tvBillCategory.text = bean.billCategory
        tvBillRemark.text = bean.billRemark
        if (bean.billRemark == null) {
            tvBillRemark.visibility = View.GONE
        } else {
            tvBillRemark.visibility = View.VISIBLE
        }
        val format = DecimalFormat("0.00")
        tvBillMoney.text = format.format(bean.billMoney)

        //判断是否设置了监听器
        if (mOnItemClickListener != null) {
            //为ItemView设置监听器
            viewHolder.itemView.setOnClickListener {
                val layoutPosition = viewHolder.layoutPosition // 1
                mOnItemClickListener!!.onItemClick(layoutPosition) // 2
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    /**
     * 判断position对应的Item是否是组的第一项
     *
     * @param position 坐标
     * @return 是否是第一项
     */
    fun isItemHeader(position: Int): Boolean {
        return if (position == 0) {
            true
        } else {
            val bean = mList[position]
            val lastBean = mList[position - 1]
            val billDate = bean.billDate
            val lastBillDate = lastBean.billDate
            !TimeUtil.millisTimeToShortFormatString(billDate)
                .equals(TimeUtil.millisTimeToShortFormatString(lastBillDate))
        }
    }

    /**
     * 获取 position 对应的 Item 的日期
     *
     * @param position 下标
     * @return 日期
     */
    fun getDate(position: Int): String {
        return TimeUtil.millisTimeToShortFormatString(
            mList[position].billDate
        )!!
    }

    /**
     * 获取每日收入/支出 金额
     *
     * @param position Item 下标
     * @return 收入/支出 金额
     */
    fun getMoneyCount(position: Int): SparseArray<Double> {
        val billDate: Long = mList[position].billDate
        val time: String = TimeUtil.millisTimeToShortFormatString(billDate)!!
        var income = 0.0
        var expend = 0.0
        for (bean in mList) {
            if (time == TimeUtil.millisTimeToShortFormatString(bean.billDate)) {
                if (bean.billProperty > 0) {
                    income += bean.billMoney
                } else {
                    expend += bean.billMoney
                }
            }
        }
        val data = SparseArray<Double>()
        data.put(1, income)
        data.put(-1, expend)
        return data
    }

    /**
     * 对外接口，设置 Item Click Listener
     */
    interface OnItemClickListener {
        /**
         * Item Click
         *
         * @param position 下标
         */
        fun onItemClick(position: Int)
    }

    /**
     * 设置 Item Click Listener
     *
     * @param mOnItemClickListener Listener
     */
    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mOnItemClickListener
    }
}
package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.billaccount.BillAccount
import com.lixyz.lifekeeperforkotlin.presenter.BillAccountViewModel


/**
 * BillAccountActivity RecyclerView Adapter
 *
 * @author LGB
 */
class BillAccountRecyclerViewAdapter(
    private val viewModel: BillAccountViewModel,
    private val mDataList: ArrayList<BillAccount>,
    private val mContext: Context
) :
    RecyclerView.Adapter<BillAccountRecyclerViewAdapter.MyViewHolder>() {
    /**
     * Item 是否移动过
     *
     * @return 是否移动过
     */
    /**
     * Item 是否移动过
     */
    private var isItemIsMoved = false

    /**
     * Item 点击监听器
     */
    private var mOnItemClickListener: OnItemClickListener? = null


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view___bill_account___recycler_view___item, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        myViewHolder.tvAccountName!!.text = mDataList[i].accountName
        myViewHolder.tvAccountName!!.setBackgroundColor(Color.parseColor("#36C37E"))

        //判断是否设置了点击监听器
        if (mOnItemClickListener != null) {
            //为ItemView设置监听器
            myViewHolder.itemView.setOnClickListener {
                val position = myViewHolder.layoutPosition // 1
                mOnItemClickListener!!.onItemClick(position) // 2
            }
        }
    }

    /**
     * 拖拽过程中的数据处理
     * 1.先记录原来位置的数据
     * 2.删除原来位置的数据
     * 3.在拖拽到的位置插入原来的数据
     */
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val bean = mDataList[fromPosition]
        mDataList.removeAt(fromPosition)
        mDataList.add(toPosition, bean)
        //通知数据发生移动
        notifyItemMoved(fromPosition, toPosition)
        isItemIsMoved = true
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

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * 旧的排列列表
         */
        private val oldOrder: ArrayList<Int>

        /**
         * 账户名称
         */
        var tvAccountName: TextView? = null

        fun onItemSelected() {
            tvAccountName!!.setBackgroundColor(Color.parseColor("#F0F0F0"))
        }

        fun onItemFinish() {
            tvAccountName!!.setBackgroundColor(Color.parseColor("#36C37E"))
            val newOrder: ArrayList<BillAccount> = ArrayList()
            for (bean in mDataList) {
                newOrder.add(BillAccount(bean.objectId, bean.orderIndex))
            }

            mDataList.forEachIndexed { index, billAccount ->
                billAccount.orderIndex = index
            }

            viewModel.updateAccountOrder(mContext, mDataList)
        }


        init {
            tvAccountName = itemView.findViewById(R.id.tv_account_name)
            oldOrder = ArrayList()
            for (bean in mDataList) {
                oldOrder.add(bean.orderIndex)
            }
        }
    }

}

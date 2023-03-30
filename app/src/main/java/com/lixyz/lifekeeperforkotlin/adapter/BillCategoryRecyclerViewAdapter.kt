package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.billcategory.BillCategory
import com.lixyz.lifekeeperforkotlin.presenter.BillCategoryViewModel


/**
 * BillCategoryActivity RecyclerView Adapter
 *
 * @author LGB
 */
class BillCategoryRecyclerViewAdapter(
    context: Context,
    data: ArrayList<BillCategory>,
    viewModel: BillCategoryViewModel,
    owner: LifecycleOwner
) :
    RecyclerView.Adapter<BillCategoryRecyclerViewAdapter.MyViewHolder>() {
    /**
     * Context
     */
    private val context: Context

    /**
     * 数据列表
     */
    private val data: ArrayList<BillCategory>

    /**
     * Item 点击监听器
     */
    private var mOnItemClickListener: OnItemClickListener? = null
    /**
     * Item 是否移动过
     *
     * @return 是否移动过
     */
    /**
     * Item 是否移动过
     */
    var isItemIsMoved = false
        private set

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view___bill_category___recycler_view___item, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, i: Int) {
        holder.tvCategoryName!!.text = data[i].categoryName
        if (data[i].isIncome > 0) {
            holder.tvCategoryName!!.setBackgroundColor(Color.parseColor("#36C37E"))
        } else {
            holder.tvCategoryName!!.setBackgroundColor(Color.parseColor("#FF524F"))
        }

        //判断是否设置了点击监听器
        if (mOnItemClickListener != null) {
            //为ItemView设置监听器
            holder.itemView.setOnClickListener {
                val position = holder.layoutPosition // 1
                mOnItemClickListener!!.onItemClick(position) // 2
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * 拖拽过程中的数据处理
     * 1.先记录原来位置的数据
     * 2.删除原来位置的数据
     * 3.在拖拽到的位置插入原来的数据
     */
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val bean = data[fromPosition]
        data.removeAt(fromPosition)
        data.add(toPosition, bean)
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

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * 收入旧的排列列表
         */
        private var incomeOldOrder: ArrayList<Int>

        /**
         * 支出旧的排列列表
         */
        private var expendOldOrder: ArrayList<Int>

        /**
         * 分类名称
         */
        var tvCategoryName: TextView? = null

        fun onItemSelected() {
            tvCategoryName!!.setBackgroundColor(Color.parseColor("#F0F0F0"))
        }

        fun onItemFinish() {
            val incomeNewOrder: ArrayList<BillCategory> = ArrayList()
            val expendNewOrder: ArrayList<BillCategory> = ArrayList()
            if (data[0].isIncome > 0) {
                tvCategoryName!!.setBackgroundColor(Color.parseColor("#36C37E"))
            } else {
                tvCategoryName!!.setBackgroundColor(Color.parseColor("#FF524F"))
            }
            for (bean in data) {
                if (bean.isIncome > 0) {
                    incomeNewOrder.add(BillCategory(bean.objectId, bean.orderIndex))
                } else {
                    expendNewOrder.add(BillCategory(bean.objectId, bean.orderIndex))
                }
            }

            data.forEachIndexed { index, billCategory ->
                billCategory.orderIndex = index
            }

            viewModel!!.updateCategoryOrder(context, data)
        }


        init {
            tvCategoryName = itemView.findViewById(R.id.tv_category_name)
            incomeOldOrder = ArrayList()
            for (bean in data) {
                if (bean.isIncome > 0) {
                    incomeOldOrder.add(bean.orderIndex)
                }
            }
            expendOldOrder = ArrayList()
            for (bean in data) {
                if (bean.isIncome < 0) {
                    expendOldOrder.add(bean.orderIndex)
                }
            }
        }
    }

    private var viewModel: BillCategoryViewModel? = null
    private var owner: LifecycleOwner? = null

    init {
        this.context = context
        this.data = data
        this.viewModel = viewModel
        this.owner = owner
    }
}
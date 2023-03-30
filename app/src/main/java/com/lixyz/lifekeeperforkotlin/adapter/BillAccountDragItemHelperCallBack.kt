package com.lixyz.lifekeeperforkotlin.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


/**
 * BillAccount Activity RecyclerView 拖拽回调
 *
 * @author LGB
 */
class BillAccountDragItemHelperCallBack : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = if (recyclerView.layoutManager is GridLayoutManager) {
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        } else {
            ItemTouchHelper.UP or ItemTouchHelper.DOWN
        }
        // 支持左右滑动(删除)操作, swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        //被按下拖拽时候的position
        val fromPosition = viewHolder.adapterPosition
        //当前拖拽到的item的position
        val toPosition = target.adapterPosition

        //回调到adapter 当中处理移动过程中,数据变更的逻辑,以及更新UI
        if (recyclerView.adapter is BillAccountRecyclerViewAdapter) {
            val listener = recyclerView.adapter as BillAccountRecyclerViewAdapter?
            listener!!.onItemMove(fromPosition, toPosition)
        }
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // 不在闲置状态
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is BillAccountRecyclerViewAdapter.MyViewHolder) {
                viewHolder.onItemSelected()
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        if (viewHolder is BillAccountRecyclerViewAdapter.MyViewHolder) {
            viewHolder.onItemFinish()
        }
        super.clearView(recyclerView, viewHolder)
    }
}
package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.plan.PlanBean
import com.lixyz.lifekeeperforkotlin.view.customview.SlidingMenu
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * 计划 RecyclerView 的 adapter
 *
 * @author LGB
 */
class PlanListPlanRecyclerViewAdapter(
    /**
     * 要显示的计划列表
     */
    private val planList: List<PlanBean>, context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /**
     * Context
     */
    private val mContext: Context = context

    /**
     * LayoutInflater
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType == FINISHED_PLAY_TYPE) {
            view = inflater.inflate(
                R.layout.view___plan_list___finished_recyclerview_item,
                viewGroup,
                false
            )
            FinishedPlanHolder(view)
        } else {
            view = inflater.inflate(
                R.layout.view___plan_list___unfinished_recyclerview_item,
                viewGroup,
                false
            )
            UnfinishedPlanHolder(view)
        }
    }

    private var undoListener: OnItemClickListener? = null
    private var finishListener: OnItemClickListener? = null
    private var alarmListener: OnItemClickListener? = null
    private var deleteListener: OnItemClickListener? = null
    fun setUndoListener(undoListener: OnItemClickListener?) {
        this.undoListener = undoListener
    }

    fun setFinishListener(finishListener: OnItemClickListener?) {
        this.finishListener = finishListener
    }

    fun setAlarmListener(alarmListener: OnItemClickListener?) {
        this.alarmListener = alarmListener
    }

    fun setDeleteListener(deleteListener: OnItemClickListener?) {
        this.deleteListener = deleteListener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        if (holder is FinishedPlanHolder) {
            holder.tvPlanTitle.text =
                planList[i].planName
            if (undoListener != null) {
                holder.tvUndo.setOnClickListener {
                    val position = holder.getLayoutPosition() // 1
                    undoListener!!.onItemClick(position) // 2
                }
            }
        }
        if (holder is UnfinishedPlanHolder) {
            holder.imgFinish.setImageDrawable(
                ResourcesCompat.getDrawable(
                    mContext.resources,
                    R.drawable.plan_list___unfinished_plan,
                    null
                )
            )
            holder.tvPlanTitle.text =
                planList[i].planName
            val format: DateFormat = SimpleDateFormat("hh:mm a", Locale.CHINA)
            val time: String = format.format(Date(planList[i].startTime))
            holder.tvPlanTime.text = time
            if (planList[i].alarmTime >= 0) {
                holder.imgAlarm.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        mContext.resources,
                        R.drawable.plan_list___alarm,
                        null
                    )
                )
            } else {
                holder.imgAlarm.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        mContext.resources,
                        R.drawable.plan_list___unalarm,
                        null
                    )
                )
            }
            if (finishListener != null) {
                holder.imgFinish.setOnClickListener {
                    val position = holder.getLayoutPosition() // 1
                    finishListener!!.onItemClick(position) // 2
                }
            }
            if (alarmListener != null) {
                holder.imgAlarm.setOnClickListener {
                    val position = holder.getLayoutPosition() // 1
                    alarmListener!!.onItemClick(position) // 2
                }
            }
            if (deleteListener != null) {
                holder.imgDelete.setOnClickListener {
                    val position = holder.getLayoutPosition() // 1
                    deleteListener!!.onItemClick(position) // 2
                }
            }
        }
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

    override fun getItemCount(): Int {
        return planList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (planList.isNotEmpty()) {
            if (planList[position].isFinished > 0) {
                FINISHED_PLAY_TYPE
            } else {
                UNFINISHED_PLAN_TYPE
            }
        } else {
            -1
        }
    }

    /**
     * 拖动菜单
     */
    private var mOpenMenu: SlidingMenu? = null
    fun holdOpenMenu(slidingMenu: SlidingMenu?) {
        mOpenMenu = slidingMenu
    }

    fun closeOpenMenu() {
        if (mOpenMenu != null && mOpenMenu!!.isOpen) {
            mOpenMenu!!.closeMenu()
        }
    }

    internal inner class FinishedPlanHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvPlanTitle: TextView = itemView.findViewById(R.id.tv_plan_title)
        var tvUndo: TextView = itemView.findViewById(R.id.tv_undo)

    }

    internal inner class UnfinishedPlanHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var imgDelete: ImageView = itemView.findViewById(R.id.img_delete)
        var imgFinish: ImageView = itemView.findViewById(R.id.img_finish)
        var tvPlanTitle: TextView = itemView.findViewById(R.id.tv_plan_title)
        var tvPlanTime: TextView = itemView.findViewById(R.id.tv_plan_time)
        var imgAlarm: ImageView = itemView.findViewById(R.id.img_plan_alarm)

    }

    companion object {
        private const val FINISHED_PLAY_TYPE = 1
        private const val UNFINISHED_PLAN_TYPE = 2
    }

}
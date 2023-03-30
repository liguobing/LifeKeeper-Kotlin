package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.netdisk.video.ShowVideoItemBean
import com.lixyz.lifekeeperforkotlin.utils.Constant


/**
 * 显示照片 RecyclerView Adapter
 */
class ShowVideoRecyclerViewAdapter(
    private val videoList: ArrayList<ShowVideoItemBean>,
    private val context: Context
) :
    RecyclerView.Adapter<ShowVideoRecyclerViewAdapter.ViewHolder>() {


    /**
     * Item 点击监听器
     */
    private var mOnItemClickListener: OnItemClickListener? = null

    /**
     * Item 长按监听器
     */
    private var mOnItemLongClickListener: OnItemLongClickListener? = null

    private var size = 0


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(
                R.layout.view___show_photo___recyclerview_item,
                parent,
                false
            )
        val layoutParams = view.layoutParams
        layoutParams.width = parent.width / 3
        layoutParams.height = parent.width / 3
        size = parent.width / 3
        view.layoutParams = layoutParams
        view.setPadding(5, 5, 5, 5)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemBean: ShowVideoItemBean = videoList[position]
        if (itemBean.checkViewIsShow!!) {
            holder.imgCheck.visibility = View.VISIBLE
            if (itemBean.checked!!) {
                Glide.with(context).load(R.drawable.show_photo_recyclerview_check).override(50)
                    .skipMemoryCache(true)
                    .into(holder.imgCheck)
            } else {
                Glide.with(context).load(R.drawable.show_photo_recyclerview_uncheck).override(50)
                    .skipMemoryCache(true)
                    .into(holder.imgCheck)
            }
        } else {
            holder.imgCheck.visibility = View.GONE
        }

        val webpUrl = "${Constant.CLOUD_ADDRESS}/LifeKeeper/resource/LifeKeeperPhoneVideo/${itemBean.video!!.videoUser}/thumbnail/${itemBean.video!!.thumbnailFileName}"
        val thumbnailUrl = "${Constant.CLOUD_ADDRESS}/LifeKeeper/resource/LifeKeeperPhoneVideo/${itemBean.video!!.videoUser}/thumbnail/${itemBean.video!!.thumbnailFileName}"
        Glide.with(context)
            .load(webpUrl)
            .error(Glide.with(context).load(thumbnailUrl))
            .placeholder(R.drawable.photo)
            .into(holder.imgVideo)

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener {
                val clickPosition = holder.layoutPosition // 1
                mOnItemClickListener!!.onItemClick(clickPosition) // 2
            }
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener {
                val clickPosition = holder.layoutPosition
                mOnItemLongClickListener!!.onItemLongClick(clickPosition)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    fun setOnItemLongClickListener(mOnItemLongClickListener: OnItemLongClickListener?) {
        this.mOnItemLongClickListener = mOnItemLongClickListener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imgVideo: ImageView = view.findViewById<View>(R.id.img_photo) as ImageView
        var imgCheck: ImageView = view.findViewById<View>(R.id.img_check) as ImageView
    }
}
package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.ShowPhotoItemBean
import com.lixyz.lifekeeperforkotlin.utils.Constant


/**
 * 显示照片 RecyclerView Adapter
 */
class ShowPhotoRecyclerViewAdapter(
    private val photoList: ArrayList<ShowPhotoItemBean>,
    private val context: Context
) :
    RecyclerView.Adapter<ShowPhotoRecyclerViewAdapter.ViewHolder>() {


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
        val itemBean: ShowPhotoItemBean = photoList[position]
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
        Glide.with(context)
            .load("${Constant.CLOUD_ADDRESS}/LifeKeeper/resource/LifeKeeperImage/${itemBean.image!!.imageUser}/thumbnail/${itemBean.image!!.thumbnailFileName}")
            .placeholder(R.drawable.photo)
            .into(holder.imgPhoto)

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
        return photoList.size
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
        var imgPhoto: ImageView = view.findViewById<View>(R.id.img_photo) as ImageView
        var imgCheck: ImageView = view.findViewById<View>(R.id.img_check) as ImageView
    }
}
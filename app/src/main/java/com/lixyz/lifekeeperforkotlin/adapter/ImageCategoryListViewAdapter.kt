package com.lixyz.lifekeeperforkotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.bumptech.glide.request.RequestOptions.overrideOf
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.photo.ImageCategoryCover
import com.lixyz.lifekeeperforkotlin.utils.Constant
import jp.wasabeef.glide.transformations.BlurTransformation

class ImageCategoryListViewAdapter(
    /**
     * Context
     */
    private val context: Context,
    /**
     * 数据 List
     */
    private val list: ArrayList<ImageCategoryCover>
) :
    BaseAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View? {
        val viewHolder: ViewHolder
        val itemView: View?

        if (view == null) {
            itemView = LayoutInflater.from(context)
                .inflate(R.layout.view___image_category_list_view_item, viewGroup, false)
            viewHolder = ViewHolder(itemView)
            itemView.tag = viewHolder
        } else {
            itemView = view
            viewHolder = itemView.tag as ViewHolder
        }
        val imageCoverUrl =
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/resource/LifeKeeperImage/${list[position].category!!.categoryUser}/cover/${list[position].image!!.coverFileName}"
        val imageBlurUrl =
            "${Constant.CLOUD_ADDRESS}/LifeKeeper/resource/LifeKeeperImage/${list[position].category!!.categoryUser}/blur/${list[position].image!!.blurFileName}"

        if (list[position].category!!.isPrivate > 0) {
            Glide.with(context).load(imageBlurUrl)
                .apply(overrideOf(viewGroup!!.width, dip2px(context)))
                .apply(
                    bitmapTransform(
                        MultiTransformation(
                            CenterCrop(),
                            BlurTransformation(25, 8)
                        )
                    )
                )
                .into(viewHolder.imgCover)
            viewHolder.imgPrivate.visibility = View.VISIBLE
        } else {
            Glide.with(context)
                .load(imageCoverUrl)
                .override(viewGroup!!.width, dip2px(context))
                .centerCrop()
                .error(Glide.with(context).load(R.drawable.add_bill___camera))
                .into(viewHolder.imgCover)
            viewHolder.imgPrivate.visibility = View.GONE
        }
        viewHolder.tvCategory.text = list[position].category!!.categoryName
        viewHolder.llCardTitleLayout.alpha = 0.7f
        return itemView
    }

    private fun dip2px(context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (300 * scale + 0.5f).toInt()
    }

    class ViewHolder(view: View?) {
        val imgCover = view?.findViewById(R.id.img_cover) as ImageView
        val tvCategory = view?.findViewById(R.id.tv_category) as TextView
        val llCardTitleLayout = view?.findViewById(R.id.card_title_layout) as RelativeLayout
        val imgPrivate = view?.findViewById(R.id.image_is_private) as ImageView
    }
}
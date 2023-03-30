package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.utils.Constant

class DisplayImageRecyclerViewAdapter(
    private var context: Context,
    private var images: ArrayList<ImageBean>
) : RecyclerView.Adapter<DisplayImageRecyclerViewViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DisplayImageRecyclerViewViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.view___display_image___recyclerview_item, parent, false)
        return DisplayImageRecyclerViewViewHolder(view)
    }

    override fun onBindViewHolder(holder: DisplayImageRecyclerViewViewHolder, position: Int) {
        val webpUrl =
            Constant.CLOUD_ADDRESS + "/LifeKeeper/resource/LifeKeeperImage/" + images[0].imageUser + "/cover/" + images[position].coverFileName
        val sourceUrl =
            Constant.CLOUD_ADDRESS + "//LifeKeeper/resource/LifeKeeperImage/" + images[0].imageUser + "/source/" + images[position].sourceFileName
        Glide.with(context).load(webpUrl).error(Glide.with(context).load(sourceUrl))
            .into(object : ImageViewTarget<Drawable>(holder.image) {
                override fun setResource(resource: Drawable?) {
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(placeholder)
                    holder.progress!!.visibility = View.VISIBLE
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    holder.progress!!.visibility = View.GONE
                    holder.image!!.setImageResource(R.drawable.display_image_error)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    super.onResourceReady(resource, transition)
                    holder.progress!!.visibility = View.GONE
                    holder.image!!.setImageDrawable(resource)
                }
            })

    }

    override fun getItemCount(): Int {
        return images.size
    }
}
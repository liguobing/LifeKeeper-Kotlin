package com.lixyz.lifekeeperforkotlin.adapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R

open class DisplayImageRecyclerViewViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var image: ImageView? = null
    var progress: ProgressBar? = null

    init {
        image = itemView.findViewById(R.id.img_image)
        progress = itemView.findViewById(R.id.progressBar)
    }
}
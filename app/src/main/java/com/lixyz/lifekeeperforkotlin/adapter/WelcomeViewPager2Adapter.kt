package com.lixyz.lifekeeperforkotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.lixyz.lifekeeperforkotlin.R


class WelcomeViewPager2Adapter :
    RecyclerView.Adapter<WelcomeViewPager2Adapter.ViewPagerViewHolder>() {

    private val imageArr = arrayOf(
        R.drawable.welcome___viewpager_plan,
        R.drawable.welcome___viewpager_account,
        R.drawable.welcome___viewpager_net_disk
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        return ViewPagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view___welcome_viewpager_item, parent,false))
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.image.setImageResource(imageArr[position])
    }

    override fun getItemCount(): Int {
        return imageArr.size
    }

    class ViewPagerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)
    }
}
package com.lixyz.lifekeeperforkotlin.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lixyz.lifekeeperforkotlin.view.activity.ImageDetailFragment

class ShowImageViewPagerAdapter(
    fa: FragmentActivity,
    private var fragmentList: ArrayList<ImageDetailFragment>,
    private var itemIdList: ArrayList<Long>
) :
    FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = fragmentList.size


    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemId(position: Int): Long {
        return itemIdList[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return itemIdList.contains(itemId)
    }
}
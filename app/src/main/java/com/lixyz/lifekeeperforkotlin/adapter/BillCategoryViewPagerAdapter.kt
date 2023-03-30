package com.lixyz.lifekeeperforkotlin.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup

import androidx.viewpager.widget.PagerAdapter


/**
 * BillCategoryActivity ViewPager Adapter
 *
 * @author LGB
 */
class BillCategoryViewPagerAdapter(list: ArrayList<View>, mContext: Context) :
    PagerAdapter() {
    private val list: ArrayList<View>
    private val mContext: Context
    override fun getCount(): Int {
        return list.size
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view === o
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = list[position]
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    init {
        this.list = list
        this.mContext = mContext
    }
}
package com.lixyz.lifekeeperforkotlin.adapter

import android.view.View
import android.view.ViewGroup

import androidx.viewpager.widget.PagerAdapter


/**
 * @author LGB
 * 登录界面 ViewPager Adapter
 */
class LoginActivityViewPagerAdapter(private val viewList: List<View>) :
    PagerAdapter() {
    override fun getCount(): Int {
        return viewList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = viewList[position]
        container.addView(view)
        return view
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(viewList[position])
    }

}

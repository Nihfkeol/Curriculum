package com.nihfkeol.curriculum.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    fragment: FragmentActivity,
    private val mFragmentList: MutableList<Fragment>
) : FragmentStateAdapter(fragment) {


    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }


}
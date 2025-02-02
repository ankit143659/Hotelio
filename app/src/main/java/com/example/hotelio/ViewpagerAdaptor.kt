package com.example.hotelio

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlin.jvm.Throws

class ViewpagerAdaptor(fm:FragmentManager)  : FragmentPagerAdapter(fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> Slide1()
            1-> Slide2()
            else -> throw IllegalArgumentException("Invalid Position")
        }
    }
}
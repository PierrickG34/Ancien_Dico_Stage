package com.antoine_charlotte_romain.dictionary.Controllers.Adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import com.antoine_charlotte_romain.dictionary.Controllers.*

/**
 * Created by dineen on 17/06/2016.
 */
//Menu selection to switch view
class PagerAdapterKot(fm: FragmentManager, icons: IntArray) : FragmentStatePagerAdapter(fm) {

    val baseId: Long = 0
    //Store the icons ids
    internal var icons = icons

    //This method return the fragment for the every position in the View Pager
    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return HomeFragmentKot()
            //return HomeFragment()
        }
        else if (position == 1) {
            return HistoryFragmentKot()
        } else {
            return SearchFragment()
        }
    }

    // This method return the Number of tabs for the tabs Strip
    override fun getCount(): Int {
        return icons.count()
    }

    // This method return the specific tab icon
    fun getDrawableId(position: Int): Int {
        return icons[position]
    }

    override fun getItemPosition(`object`: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }

}
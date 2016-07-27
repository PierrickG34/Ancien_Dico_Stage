package ie.csis.dicosaure.Controllers.Adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import ie.csis.dicosaure.Controllers.HistoryFragment
import ie.csis.dicosaure.Controllers.HomeFragment
import ie.csis.dicosaure.Controllers.SearchFragment

/**
 * Created by dineen on 17/06/2016.
 */
//Menu selection to switch view
class PagerAdapter(fm: FragmentManager, icons: IntArray) : FragmentStatePagerAdapter(fm) {

    val baseId: Long = 0
    //Store the icons ids
    internal var icons = icons

    /**
     * This method return the fragment for the every position in the View Pager
     * @param position the item position
     * @return the fragment
     */
    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return HomeFragment()
            //return HomeFragment()
        }
        else if (position == 1) {
            return HistoryFragment()
        } else {
            return SearchFragment()
        }
    }

    /**
     * This method return the number of tabs for the tabs Strip
     * @return the count
     */
    override fun getCount(): Int {
        return icons.count()
    }

    /**
     * This method return the specific tab icon
     * @return the tab icon
     */
    fun getDrawableId(position: Int): Int {
        return icons[position]
    }

    /**
     * This method return the position of the item specified
     * @param object the item
     * @return the item position
     */
    override fun getItemPosition(`object`: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }

}
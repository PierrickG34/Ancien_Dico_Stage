package com.antoine_charlotte_romain.dictionary.Controllers.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.DrawerAdapter
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.PagerAdapterKot
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.ViewPagerAdapter
import com.antoine_charlotte_romain.dictionary.Controllers.Lib.SlidingTabLayout
import com.antoine_charlotte_romain.dictionary.Controllers.activities.about.AboutActivityKot
import com.antoine_charlotte_romain.dictionary.Controllers.activities.language.SetLanguageKot
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelper
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot
import com.antoine_charlotte_romain.dictionary.R
import org.jetbrains.anko.ctx

/**
 * Created by dineen on 15/06/2016.
 */
class MainActivityKot : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var pager: ViewPager? = null
    private var adapterMenu: PagerAdapterKot? = null
    private var tabs: SlidingTabLayout? = null
    private val iconsPager = intArrayOf(
            R.drawable.home_tab_drawable,
            R.drawable.history_tab_drawable,
            R.drawable.search_tab_drawable
    )

    companion object {

        val HOME_FRAGMENT = 0
        val HISTORY_FRAGMENT = 1
        val ADVANCED_SEARCH_FRAGMENT = 2
        val EXTRA_DICTIONARY = "SelectedDictionary"
        val EXTRA_FRAGMENT = "fragment"
        val EXTRA_WORD = "selectedWord"
        val EXTRA_BEGIN_STRING = "begin"
        val EXTRA_MIDDLE_STRING = "middle"
        val EXTRA_END_STRING = "end"
        val EXTRA_SEARCH_DATA = "searchOption"
        val EXTRA_PART_OR_WHOLE = "partOrWhole"
        val EXTRA_NEW_DICO_NAME = "namedico"
        val EXTRA_RENAME = "rename"

        val WHOLE_WORD = "whole"
        val PART_WORD = "part"
        val HEADWORD_ONLY = "headword"
        val MEANING_ONLY = "meaning"
        val NOTES_ONLY = "notes"
        val ALL_DATA = "allData"
    }

    //var menuDrawerList: RecyclerView? = null
    //var menuDrawerLayout: DrawerLayout? = null
    private var myMenuDrawerToggle: ActionBarDrawerToggle? = null
    var menuAdapter: DrawerAdapter? = null

    var addButton: FloatingActionButton? = null
        private set

    var rootLayout: CoordinatorLayout? = null
        private set
    private var currentPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_main)
        var db = DataBaseHelper(this)
        var d = DataBaseHelperKot(this.ctx)
        d.insertTest(this.ctx)

        // Creating The Toolbar and setting it as the Toolbar for the activity
        this.toolbar = findViewById(R.id.tool_bar) as Toolbar?
        super.setSupportActionBar(this.toolbar)

        this.initMenu()

        // Retrieving the intent to know the fragment to show
        val fragment = this.intent.getStringExtra("fragment")

        // Creating The PagerAdapter and Passing Fragment Manager, and icons of the tables.
        this.adapterMenu = PagerAdapterKot(this.supportFragmentManager, this.iconsPager)

        // Assigning ViewPager View and setting the adapter
        pager = findViewById(R.id.pager) as ViewPager?
        pager!!.offscreenPageLimit = this.iconsPager.count()
        pager!!.adapter = this.adapterMenu

        // Assigning the Sliding Tab Layout View
        tabs = findViewById(R.id.tabs) as SlidingTabLayout?
        tabs!!.setDistributeEvenly(true)

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs!!.setCustomTabColorizer { resources.getColor(R.color.tabsScrollColor) }

        // Setting the ViewPager For the SlidingTabsLayout
        tabs!!.setViewPager(pager)

        if (fragment != null && fragment.equals("advancedSearch", ignoreCase = true)) {
            pager!!.currentItem = ADVANCED_SEARCH_FRAGMENT
            currentPage = ADVANCED_SEARCH_FRAGMENT
        }

        // Pager Listener
        pager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == HOME_FRAGMENT) {
                    addButton!!.visibility = View.VISIBLE
                    addButton!!.animate().translationY(0f)
                    currentPage = HOME_FRAGMENT
                } else {
                    addButton!!.animate().translationY(350f)
                    if (position == HISTORY_FRAGMENT) {
                        currentPage = HISTORY_FRAGMENT
                    } else {
                        currentPage = ADVANCED_SEARCH_FRAGMENT
                    }
                }
            }
        })


        //setupUI(findViewById(R.id.activity_main)!!)

        addButton = findViewById(R.id.add_button) as FloatingActionButton?
        rootLayout = findViewById(R.id.rootLayout) as CoordinatorLayout?
//    }
//
//    override fun onPostCreate(savedInstanceState: Bundle?) {
//        super.onPostCreate(savedInstanceState)
//        // Sync the toggle state after onRestoreInstanceState has occurred.
//        myMenuDrawerToggle!!.syncState()
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        myMenuDrawerToggle!!.onConfigurationChanged(newConfig)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (currentPage == HOME_FRAGMENT) {
//            addButton!!.visibility = View.VISIBLE
//        } else {
//            addButton!!.visibility = View.GONE
//        }
//        adapter!!.notifyDataSetChanged()
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        val id = item.itemId
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true
//        }
//
//        if (myMenuDrawerToggle!!.onOptionsItemSelected(item)) {
//            return true
//        }
//
//        return super.onOptionsItemSelected(item)
//    }
//
//    fun setupUI(view: View) {
//
//        //Set up touch listener for non-text box views to hide keyboard.
//        if (view !is EditText) {
//
//            view.setOnTouchListener { v, event ->
//                KeyboardUtility.hideSoftKeyboard(this@MainActivityKot)
//                false
//            }
//        }
//
//        //If a layout container, iterate over children and seed recursion.
//        if (view is ViewGroup) {
//
//            for (i in 0..view.childCount - 1) {
//
//                val innerView = view.getChildAt(i)
//
//                setupUI(innerView)
//            }
//        }
//    }
    }

    //Create menu settings
    fun initMenu() {
        val menuTitles = arrayOf(getString(R.string.language), getString(R.string.about))
        val menuIcons = intArrayOf(R.drawable.ic_language_white_24dp, R.drawable.ic_info_white_24dp)
        val menuDrawerLayout = findViewById(R.id.activity_main) as DrawerLayout?
        val menuDrawerList = findViewById(R.id.left_drawer) as RecyclerView?

        // Set the adapter for the recycler view of the menu settings
        this.menuAdapter = DrawerAdapter(menuTitles, menuIcons)
        menuDrawerList!!.adapter = this.menuAdapter
        val layoutManager = LinearLayoutManager(this)
        menuDrawerList!!.layoutManager = layoutManager

        //Add listener when the menu is open or close
        val menuDrawerToggle = object : ActionBarDrawerToggle(this, menuDrawerLayout, toolbar, R.string.open, R.string.close) {

            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(drawerView: View?) {
                super.onDrawerClosed(drawerView)
                invalidateOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu()
            }
        }

        menuDrawerLayout!!.addDrawerListener(menuDrawerToggle)
        menuDrawerToggle!!.syncState()

        // Set the onItemClickListener of the menu settings
        this.menuAdapter!!.SetOnItemClickListener { v, position ->
            // Languages position
            if (position == 1) {
                val languageIntent = Intent(applicationContext, SetLanguageKot::class.java)
                startActivity(languageIntent)
            } else if (position == 2) {
                val aboutIntent = Intent(applicationContext, AboutActivityKot::class.java)
                startActivity(aboutIntent)
            }// About position
        }
    }
}
package ie.csis.dicosaure.Controllers.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import ie.csis.dicosaure.Controllers.Adapter.PagerAdapter
import ie.csis.dicosaure.Controllers.Lib.SlidingTabLayout
import ie.csis.dicosaure.Controllers.R
import ie.csis.dicosaure.Controllers.activities.about.AboutActivityKot
import ie.csis.dicosaure.Controllers.activities.language.SetLanguageKot
import ie.csis.dicosaure.DataModel.DataBaseHelperKot
import org.jetbrains.anko.ctx

/**
 * Created by dineen on 15/06/2016.
 */
class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var pager: ViewPager? = null
    private var adapterMenu: PagerAdapter? = null
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

    var addButton: FloatingActionButton? = null
        private set

    var rootLayout: CoordinatorLayout? = null
        private set
    private var currentPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_main)
        var d = DataBaseHelperKot(this.ctx)

        // Creating The Toolbar and setting it as the Toolbar for the activity
        this.toolbar = findViewById(R.id.tool_bar) as Toolbar?
        super.setSupportActionBar(this.toolbar)

        this.initMenu()

        // Retrieving the intent to know the fragment to show
        val fragment = this.intent.getStringExtra("fragment")

        // Creating The PagerAdapter and Passing Fragment Manager, and icons of the tables.
        this.adapterMenu = PagerAdapter(this.supportFragmentManager, this.iconsPager)

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

        addButton = findViewById(R.id.add_button) as FloatingActionButton?
        rootLayout = findViewById(R.id.rootLayout) as CoordinatorLayout?
    }

    //Create menu settings
    fun initMenu() {
        val menuDrawerLayout = findViewById(R.id.activity_main) as DrawerLayout?

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
    }

    fun launchLanguage(v : View) {
        val languageIntent = Intent(applicationContext, SetLanguageKot::class.java)
        this.startActivity(languageIntent)
    }

    fun launchAbout(v : View) {
        val aboutIntent = Intent(applicationContext, AboutActivityKot::class.java)
        this.startActivity(aboutIntent)
    }

}
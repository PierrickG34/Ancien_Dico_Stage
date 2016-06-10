package com.antoine_charlotte_romain.dictionary.Controllers;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.DrawerAdapter;
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.ViewPagerAdapter;
import com.antoine_charlotte_romain.dictionary.Controllers.Lib.SlidingTabLayout;
import com.antoine_charlotte_romain.dictionary.R;
import com.antoine_charlotte_romain.dictionary.Utilities.KeyboardUtility;
import com.antoine_charlotte_romain.dictionary.Utilities.StringsUtility;

/**
 */
public class MainActivity extends AppCompatActivity {

    public final static int HOME_FRAGMENT = 0;
    public final static int HISTORY_FRAGMENT = 1;
    public final static int ADVANCED_SEARCH_FRAGMENT = 2;
    public final static String EXTRA_DICTIONARY = "SelectedDictionary";
    public final static String EXTRA_FRAGMENT = "fragment";
    public final static String EXTRA_WORD = "selectedWord";
    public static final String EXTRA_BEGIN_STRING = "begin";
    public static final String EXTRA_MIDDLE_STRING = "middle";
    public static final String EXTRA_END_STRING = "end";
    public static final String EXTRA_SEARCH_DATA = "searchOption";
    public static final String EXTRA_PART_OR_WHOLE = "partOrWhole";
    public static final String EXTRA_NEW_DICO_NAME = "namedico";
    public static final String EXTRA_RENAME = "rename";

    public static final String WHOLE_WORD = "whole";
    public static final String PART_WORD = "part";
    public static final String HEADWORD_ONLY = "headword";
    public static final String MEANING_ONLY = "meaning";
    public static final String NOTES_ONLY = "notes";
    public static final String ALL_DATA = "allData";

    private Toolbar toolbar;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private int numbOfTabs = 3;

    private RecyclerView myMenuDrawerList;
    private DrawerLayout myMenuDrawerLayout;
    private ActionBarDrawerToggle myMenuDrawerToggle;
    private DrawerAdapter myMenuAdapter;

    private FloatingActionButton addButton;

    private CoordinatorLayout rootLayout;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Creating the menu settings
        String[] myPlanetTitles = {getString(R.string.language), getString(R.string.about)};
        int[] icons = {R.drawable.ic_language_white_24dp, R.drawable.ic_info_white_24dp};
        myMenuDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        myMenuDrawerList = (RecyclerView) findViewById(R.id.left_drawer);

        // Set the adapter for the recycler view of the menu settings
        myMenuAdapter = new DrawerAdapter(myPlanetTitles, icons);
        myMenuDrawerList.setAdapter(myMenuAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        myMenuDrawerList.setLayoutManager(mLayoutManager);

        // Set the listener of the menu settings drawer
        myMenuDrawerToggle = new ActionBarDrawerToggle(this, myMenuDrawerLayout, toolbar, R.string.open, R.string.close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        myMenuDrawerLayout.setDrawerListener(myMenuDrawerToggle);
        myMenuDrawerToggle.syncState();

        // Set the onItemClickListener of the menu settings
        myMenuAdapter.SetOnItemClickListener(new DrawerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View v , int position) {
                // Languages position
                if(position == 1){
                    Intent languageIntent = new Intent(getApplicationContext(), LanguageActivity.class);
                    startActivity(languageIntent);
                }

                // About position
                else if(position == 2){
                    Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
                    startActivity(aboutIntent);
                }
            }
        });


        // Retrieving the intent to know the fragment to show
        Intent intent = getIntent();
        String fragment = intent.getStringExtra("fragment");

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), numbOfTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(numbOfTabs);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        if(fragment != null && fragment.equalsIgnoreCase("advancedSearch")){
            pager.setCurrentItem(ADVANCED_SEARCH_FRAGMENT);
            currentPage = ADVANCED_SEARCH_FRAGMENT;
        }

        // Pager Listener
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                if (position == HOME_FRAGMENT) {
                    addButton.setVisibility(View.VISIBLE);
                    addButton.animate().translationY(0);
                    currentPage = HOME_FRAGMENT;
                } else {
                    addButton.animate().translationY(350);
                    if (position == HISTORY_FRAGMENT) {
                        currentPage = HISTORY_FRAGMENT;
                    } else {
                        currentPage = ADVANCED_SEARCH_FRAGMENT;
                    }
                }
            }
        });


        setupUI(findViewById(R.id.activity_main));

        addButton = (FloatingActionButton) findViewById(R.id.add_button);
        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        myMenuDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        myMenuDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentPage == HOME_FRAGMENT){
            addButton.setVisibility(View.VISIBLE);
        }
        else {
            addButton.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (myMenuDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public FloatingActionButton getAddButton()
    {
        return addButton;
    }

    public CoordinatorLayout getRootLayout()
    {
        return rootLayout;
    }

    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    KeyboardUtility.hideSoftKeyboard(MainActivity.this);
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }
}

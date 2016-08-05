package ie.csis.dicosaure.views.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.*
import android.widget.Toast.makeText
import ie.csis.dicosaure.model.dictionary.DictionarySQLITE
import ie.csis.dicosaure.model.word.Word
import ie.csis.dicosaure.model.word.WordSQLITE
import ie.csis.dicosaure.views.R
import ie.csis.dicosaure.views.activities.MainActivity
import ie.csis.dicosaure.views.activities.WordViewActivity
import ie.csis.dicosaure.views.adapters.SearchDateAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dineen on 20/06/2016.
 * This class permit to search a word in the historic of the search by date
 */
class HistoryFragment(): Fragment() {


    /**
     * The label for search a word in the view
     */
    private var historySearch: EditText? = null

    /**
     * Is the area that returns search
     */
    private var gridViewHistory: GridView? = null

    /**
     * Button that allows you to search
     */
    private var advancedSearchButton: Button? = null

    /**
     * Button that allows you to reset search
     */
    private var resetButton: Button? = null

    /**
     * Lets find out if all the words of the database are already searching
     */
    private var allLoaded: Boolean = false

    /**
     * Define a limit for the search
     */
    private var historyLimit: Int = 0

    /**
     * Define a offset for the search
     */
    private var historyOffset: Int = 0

    /**
     * A dialog showing a progress indicator when the user scroll down to load more words
     */
    private var progressDialog: ProgressDialog? = null

    /**
     * Database for the word
     */
    private var sddm: WordSQLITE? = null

    /**
     * Result of requests in the database
     */
    private var mySearchDateList: MutableList<Word>? = null

    /**
     * Add the words in the mySearchDateList in the view
     */
    private var myAdapter: SearchDateAdapter? = null

    /**
     * Lets find out if there are words in the database
     */
    private var loadingMore: Boolean = false

    /**
     * Size of the list of Word to determine if there is still word in the database
     */
    private var actualListSize: Int = 0


    /**
     * Initialize the view with the name and the id of the components
     * @param inflater 	The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container  If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState  If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The view Initialize
     */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val thisView: View? = inflater!!.inflate(R.layout.fragment_history, container, false)

        this.historySearch = thisView!!.findViewById(R.id.historySearch) as EditText
        this.gridViewHistory = thisView.findViewById(R.id.gridViewHistory) as GridView
        this.advancedSearchButton = thisView.findViewById(R.id.buttonAdvancedSearch) as Button
        this.resetButton = thisView.findViewById(R.id.buttonReset) as Button

        this.sddm = WordSQLITE(context, null, null, null, null, "", null, null)
        initListView()

        setHasOptionsMenu(true)

        return thisView
    }

    override fun onResume() {
        super.onResume()
        initListView()
    }

    /**
     * Create the menu at the top right
     * @param menu the name of the menu
     * @param inflater Used to instantiate menu XML files into Menu objects
     */
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_history, menu)
    }

    /**
     * This function is call whenever an item in the menu (top right) is selected
     * @param item The menu
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_clear_history -> {
                clearHistory()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    /**
     * Function that load all the search history on the database and show it on the listView
     */
    private fun initListView() {
        // Initialize the limit of the research
        this.historyLimit = 10
        this.historyOffset = 0
        this.allLoaded = false

        // Initialize the progress dialog
        this.progressDialog = ProgressDialog(activity)
        this.progressDialog!!.setMessage(getString(R.string.loadingHistory))
        this.progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        this.progressDialog!!.setIndeterminate(true)
        this.progressDialog!!.setCancelable(false)
        this.progressDialog!!.window.setGravity(Gravity.BOTTOM)

        // Get word in the database
        this.mySearchDateList = this.sddm!!.selectAll(historyLimit, historyOffset)

        // Put this in the view
        myAdapter = SearchDateAdapter(R.layout.row_history, mySearchDateList, context, 0, null)

        this.gridViewHistory!!.setAdapter(myAdapter)
        this.gridViewHistory!!.setTextFilterEnabled(true)

        this.gridViewHistory!!.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id -> seeWord(position) })

        this.gridViewHistory!!.setOnScrollListener(object : AbsListView.OnScrollListener {

            internal var currentVisibleItemCount: Int = 0
            internal var currentFirstVisibleItem: Int = 0
            internal var currentScrollState: Int = 0

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                this.currentVisibleItemCount = visibleItemCount
                this.currentFirstVisibleItem = firstVisibleItem
            }

            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                this.currentScrollState = scrollState
                this.isScrollCompleted()
            }

            // Call the method that allows to load more word
            private fun isScrollCompleted() {
                // the "historySearch!!.text.length == 0" is for checking if we are not making research in history.
                if (this.currentVisibleItemCount > 0 && this.currentScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && historySearch!!.text.length == 0) {
                    val lastInScreen = currentFirstVisibleItem + currentVisibleItemCount
                    if (lastInScreen == mySearchDateList!!.size && !loadingMore && !allLoaded) {
                        progressDialog!!.show()
                        val thread = Thread(null, loadMoreHistory)
                        thread.start()
                    }
                }
            }
        })

        //Permit to search dynamically words with the search bar over the words
        this.historySearch!!.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(string: Editable?) {
                if (string != null) {
                    (mySearchDateList as MutableList<Word>).clear()
                    val tempList: ArrayList<Word>

                    if (string.length > 0) {
                        val search = string.toString()
                        tempList = (sddm as WordSQLITE).select(search) as ArrayList<Word>
                    } else {
                        historyOffset = 0
                        tempList = (sddm as WordSQLITE).selectAll(historyLimit, historyOffset) as ArrayList<Word>
                        allLoaded = false
                    }

                    for (i in tempList.indices) {
                        (mySearchDateList as MutableList<Word>).add(tempList[i])
                    }
                    (myAdapter as SearchDateAdapter).notifyDataSetChanged()
                }
            }
        })

        /*
            Load the advanced search view when the user click on the button.
            This research ask the user to enter a date before, a date after or both to search a word in the previous searches
        */
        this.advancedSearchButton!!.setOnClickListener(View.OnClickListener {
            val dialogBuilder = AlertDialog.Builder(activity)

            var dateBefore: String? = null
            var dateAfter: String? = null

            val inflater = activity.layoutInflater
            val dialogView = inflater.inflate(R.layout.history_advanced_search_dialog, null)
            dialogBuilder.setView(dialogView)

            val dateAfterEditText = dialogView.findViewById(R.id.editTextAfter) as EditText
            val dateBeforeEditText = dialogView.findViewById(R.id.editTextBefore) as EditText
            dateAfterEditText.inputType = InputType.TYPE_NULL
            dateBeforeEditText.inputType = InputType.TYPE_NULL

            val myCalendar = Calendar.getInstance()

            val dateAfterCalendar = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "yyyy-MM-dd"
                dateAfter = SimpleDateFormat(myFormat, Locale.US).format(myCalendar.time)

                dateAfterEditText.setText(dateAfter)
            }

            dateAfterEditText.setOnClickListener {
                DatePickerDialog(activity, dateAfterCalendar, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            val dateBeforeCalendar = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "yyyy-MM-dd"
                dateBefore = SimpleDateFormat(myFormat, Locale.US).format(myCalendar.time)


                dateBeforeEditText.setText(dateBefore)
            }

            dateBeforeEditText.setOnClickListener {
                DatePickerDialog(activity, dateBeforeCalendar, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            dialogBuilder.setTitle(R.string.advanced_search)

            // Get the dates of the user and search in the database
            dialogBuilder.setPositiveButton(R.string.search) { dialog, which ->
                var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                var dateBeforeFormat: Date
                var dateBeforeDATE: java.sql.Date
                var dateAfterFormat: Date
                var dateAfterDATE: java.sql.Date

                (mySearchDateList as MutableList<Word>).clear()
                var tempList: MutableList<Word>? = null

                // Only before date is selected
                if (dateAfter == null && dateBefore != null) {
                    dateBeforeFormat = formatter.parse(dateBefore)
                    dateBeforeDATE = java.sql.Date(dateBeforeFormat.time)
                    tempList = (sddm as WordSQLITE).selectBeforeDate(dateBeforeDATE)
                }
                // Only after date is selected
                else if (dateBefore == null && dateAfter != null) {
                    dateAfterFormat = formatter.parse(dateAfter)
                    dateAfterDATE = java.sql.Date(dateAfterFormat.time)
                    tempList = (sddm as WordSQLITE).selectAfterDate(dateAfterDATE)
                }
                // Both dates
                else if (dateBefore != null && dateAfter != null) {
                    dateBeforeFormat = formatter.parse(dateBefore)
                    dateBeforeDATE = java.sql.Date(dateBeforeFormat.time)
                    dateAfterFormat = formatter.parse(dateAfter)
                    dateAfterDATE = java.sql.Date(dateAfterFormat.time)
                    tempList = (sddm as WordSQLITE).selectBetweenDate(dateBeforeDATE, dateAfterDATE)
                }

                // If the answer is not null, put in the view
                if (tempList != null) {
                    for (i in (tempList as MutableList<Word>).indices) {
                        (mySearchDateList as MutableList<Word>).add((tempList as MutableList<Word>).get(i))
                    }
                }
                (myAdapter as SearchDateAdapter).notifyDataSetChanged()
                allLoaded = true
                dialog.cancel()
            }

            // Close the window if the user choose closed button
            dialogBuilder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }

            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        })

        // Call the initListView method when the user click on the reset button
        this.resetButton!!.setOnClickListener(View.OnClickListener {
            initListView()
            this.historySearch!!.setText("")
        })
    }

    /**
     * This function launches the view details of a word and allows to modify it
     * @param position the position in the listView of the word the user want to see more details or to modify
     */
    fun seeWord(position: Int) {
        val wordDetailIntent = Intent(activity, WordViewActivity::class.java)

        //add the word in the intent
        wordDetailIntent.putExtra(MainActivity.EXTRA_WORD, mySearchDateList!!.get(position))

        //add the dictionary in the intent
        var dictionaryModel: DictionarySQLITE? = DictionarySQLITE(this.context)
        wordDetailIntent.putExtra(MainActivity.EXTRA_DICTIONARY, dictionaryModel!!.select((mySearchDateList as MutableList<Word>).get(position).idDictionary!!))

        startActivity(wordDetailIntent)

        if (this.historySearch!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.historySearch!!.setText("")
        }
    }

    /**
     * This function is used to clear the search history of the app. Put all dates to 'null' value in the database.
     */
    private fun clearHistory() {
        val alert = AlertDialog.Builder(activity)
        alert.setMessage(getString(R.string.clearHistory) + " ?")
        alert.setPositiveButton(getString(R.string.clear)) { dialog, whichButton ->
            makeText(activity, getString(R.string.historyCleared), Toast.LENGTH_SHORT).show()
            sddm!!.deleteAllDates()
            initListView();
        }

        alert.setNegativeButton(R.string.cancel) { dialog, whichButton -> }

        alert.show()
    }

    /**
     * This thread is launch when the user scroll to the end of the list and it load more history
     */
    private val loadMoreHistory: Runnable
        get() = Runnable {
            loadingMore = true
            var tempList: MutableList<Word>? = null
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            historyOffset += 10
            this.sddm = WordSQLITE(context, null, null, null, null, "", null, null)
            if (historySearch!!.text.toString().length == 0) {
                tempList = (sddm as WordSQLITE).selectAll(historyLimit, historyOffset)
            }

            actualListSize = mySearchDateList!!.size
            for (i in tempList!!.indices) {
                (mySearchDateList as MutableList<Word>).add(tempList[i])
            }
            activity.runOnUiThread(returnRes)
        }

    /**
     * This thread tell the adapter that the more words were loaded
     */
    private val returnRes = Runnable {
        this.myAdapter!!.notifyDataSetChanged()
        this.loadingMore = false
        if (this.actualListSize == mySearchDateList!!.size) {
            allLoaded = true
        }
        progressDialog!!.dismiss()
    }


}
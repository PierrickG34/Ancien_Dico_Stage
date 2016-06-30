package com.antoine_charlotte_romain.dictionary.Controllers

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.*
import android.widget.Toast.makeText
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import java.text.SimpleDateFormat
import java.util.*
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.SearchDateAdapterKot
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE

/**
 * Created by dineen on 20/06/2016.
 * This class permit to search a word in the historic of the search
 */
class HistoryFragmentKot(): Fragment() {

    private var historySearch: EditText? = null
    private var gridViewHistory: GridView? = null
    private var advancedSearchButton: Button? = null
    private var resetButton: Button? = null

    private var allLoaded: Boolean = false
    private var historyLimit: Int = 0
    private var historyOffset: Int = 0
    private var progressDialog: ProgressDialog? = null

    private var sddm: WordSQLITE? = null

    private var mySearchDateList: MutableList<Word>? = null
    private var myAdapter: SearchDateAdapterKot? = null
    private var loadingMore: Boolean = false
    private var actualListSize: Int = 0


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
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_history, menu)
    }

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
        this.historyLimit = 10
        this.historyOffset = 0
        this.allLoaded = false

        this.progressDialog = ProgressDialog(activity)
        this.progressDialog!!.setMessage(getString(R.string.loadingHistory))
        this.progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        this.progressDialog!!.setIndeterminate(true)
        this.progressDialog!!.setCancelable(false)
        this.progressDialog!!.window.setGravity(Gravity.BOTTOM)

        this.mySearchDateList = this.sddm!!.selectAll(historyLimit, historyOffset)

        myAdapter = SearchDateAdapterKot(R.layout.row_history, mySearchDateList, context, 0, null)


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

            private fun isScrollCompleted() {
                if (this.currentVisibleItemCount > 0 && this.currentScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    val lastInScreen = currentFirstVisibleItem + currentVisibleItemCount
                    if (lastInScreen == mySearchDateList!!.size && !loadingMore && !allLoaded) {
                        progressDialog!!.show()
                        val thread = Thread(null, loadMoreHistory)
                        thread.start()
                    }
                }
            }
        })

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
                        println("if")
                        tempList = (sddm as WordSQLITE).select(search) as ArrayList<Word>
                    } else {
                        println("else")
                        historyOffset = 0
                        tempList = (sddm as WordSQLITE).selectAll(historyLimit, historyOffset) as ArrayList<Word>
                        allLoaded = false
                    }

                    for (i in tempList.indices) {
                        (mySearchDateList as MutableList<Word>).add(tempList[i])
                    }
                    (myAdapter as SearchDateAdapterKot).notifyDataSetChanged()
                }
            }
        })

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

            dialogBuilder.setPositiveButton(R.string.search) { dialog, which ->
                var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                var dateBeforeFormat: java.util.Date
                var dateBeforeDATE: java.sql.Date
                var dateAfterFormat: java.util.Date
                var dateAfterDATE: java.sql.Date

                (mySearchDateList as MutableList<Word>).clear()
                var tempList: MutableList<Word>? = null

                if (dateAfter == null) {
                    if (dateBefore != null) {
                        dateBeforeFormat = formatter.parse(dateBefore)
                        dateBeforeDATE = java.sql.Date(dateBeforeFormat.time)
                        tempList = (sddm as WordSQLITE).selectBeforeDate(dateBeforeDATE)
                    }
                }
                else if (dateBefore == null) {
                    if (dateAfter != null) {
                        dateAfterFormat = formatter.parse(dateAfter)
                        dateAfterDATE = java.sql.Date(dateAfterFormat.time)
                        tempList = (sddm as WordSQLITE).selectAfterDate(dateAfterDATE)

                    }
                }
                else {
                    dateBeforeFormat = formatter.parse(dateBefore)
                    dateBeforeDATE = java.sql.Date(dateBeforeFormat.time)
                    dateAfterFormat = formatter.parse(dateAfter)
                    dateAfterDATE = java.sql.Date(dateAfterFormat.time)
                    tempList = (sddm as WordSQLITE).selectBetweenDate(dateBeforeDATE, dateAfterDATE)
                }
                if (tempList != null) {
                    for (i in (tempList as MutableList<Word>).indices) {
                        (mySearchDateList as MutableList<Word>).add((tempList as MutableList<Word>).get(i))
                    }
                }
                (myAdapter as SearchDateAdapterKot).notifyDataSetChanged()
                allLoaded = true
                dialog.cancel()
            }

            dialogBuilder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }

            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        })

        this.resetButton!!.setOnClickListener(View.OnClickListener { initListView() })
    }

    /**
     * This function launches the view details of a word and allows to modify it
     * @param position the position in the listView of the word the user want to see more details or to modify
     */
    fun seeWord(position: Int) {
        val wordDetailIntent = Intent(activity, WordActivityKot::class.java)

        wordDetailIntent.putExtra(MainActivityKot.EXTRA_WORD, mySearchDateList!!.get(position))
        var dictionaryModel: DictionarySQLITE? = DictionarySQLITE(this.context)
        wordDetailIntent.putExtra(MainActivityKot.EXTRA_DICTIONARY, dictionaryModel!!.selectDictionary((mySearchDateList as MutableList<Word>).get(position).idDictionary!!))

        startActivity(wordDetailIntent)

        if (historySearch!!.getText().toString().trim { it <= ' ' }.length > 0) {
            historySearch!!.setText("")
        }
    }

        /**
         * This function is used to clear the search history of the app
         */
        private fun clearHistory() {
            val alert = AlertDialog.Builder(activity)
            alert.setMessage(getString(R.string.clearHistory) + " ?")
            alert.setPositiveButton(getString(R.string.clear)) { dialog, whichButton ->
                makeText(activity, getString(R.string.historyCleared), Toast.LENGTH_SHORT).show()
                sddm!!.deleteAll()
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
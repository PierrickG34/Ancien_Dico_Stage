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

    //    private var sddm: SearchDateDataModel? = null
    private var sddm: WordSQLITE? = null

    //    private var mySearchDateList: ArrayList<SearchDate>? = null
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

        println("HistoryFragmentKot.kt -- Test onCreateView")
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

        println("HistoryFragmentKot.kt -- Test initListView")

        this.mySearchDateList = this.sddm!!.selectAll(historyLimit, historyOffset)

        myAdapter = SearchDateAdapterKot(R.layout.row_history, mySearchDateList, context, 0, null)


        this.gridViewHistory!!.setAdapter(myAdapter)
        this.gridViewHistory!!.setTextFilterEnabled(true)

//        this.gridViewHistory!!.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id -> seeWord(position) })

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

            override fun afterTextChanged(s: Editable?) {
//                if (s != null) {
//                    (mySearchDateList as MutableList<Word>).clear()
//                    val tempList: ArrayList<Word>
//
//                    if (s.length > 0) {
//                        val search = s.toString()
//                        tempList = sddm.select(search)
//                        // TODO just need to implement the method.
//                    } else {
//                        historyOffset = 0
//                        tempList = (sddm as WordSQLITE).selectAll(historyLimit, historyOffset) as ArrayList<Word>
//                        allLoaded = false
//                    }
//
//                    for (i in tempList.indices) {
//                        (mySearchDateList as MutableList<Word>).add(tempList[i])
//                    }
//                    (myAdapter as SearchDateAdapterKot).notifyDataSetChanged()
//                }
            }
        })

        this.advancedSearchButton!!.setOnClickListener(View.OnClickListener {
            val dialogBuilder = AlertDialog.Builder(activity)

            val inflater = activity.layoutInflater
            val dialogView = inflater.inflate(R.layout.history_advanced_search_dialog, null)
            dialogBuilder.setView(dialogView)

            val dateBeforeEditText = dialogView.findViewById(R.id.editTextBefore) as EditText
            val dateAfterEditText = dialogView.findViewById(R.id.editTextAfter) as EditText
            dateBeforeEditText.inputType = InputType.TYPE_NULL
            dateAfterEditText.inputType = InputType.TYPE_NULL

            val myCalendar = Calendar.getInstance()

            val dateBeforeCalendar = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myFormat, Locale.US)

                dateBeforeEditText.setText(sdf.format(myCalendar.time))
            }

            dateBeforeEditText.setOnClickListener {
                DatePickerDialog(activity, dateBeforeCalendar, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            val dateAfterCalendar = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myFormat, Locale.US)


                dateAfterEditText.setText(sdf.format(myCalendar.time))
            }

            dateAfterEditText.setOnClickListener {
                DatePickerDialog(activity, dateAfterCalendar, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }



            dialogBuilder.setTitle(R.string.advanced_search)

            dialogBuilder.setPositiveButton(R.string.search) { dialog, which ->
                var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                var dateBeforeFormat: java.util.Date = formatter.parse(dateBeforeEditText.text.toString())
                var dateBefore: java.sql.Date = java.sql.Date(dateBeforeFormat.time)
                var dateAfterFormat: java.util.Date = formatter.parse(dateAfterEditText.text.toString())
                var dateAfter: java.sql.Date = java.sql.Date(dateAfterFormat.time)

                (mySearchDateList as MutableList<Word>).clear()
                val tempList = (sddm as WordSQLITE).selectBetweenDate(dateBefore, dateAfter)
                if (tempList != null) {
                    for (i in tempList.indices) {
                        (mySearchDateList as MutableList<Word>).add(tempList.get(i))
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

        wordDetailIntent.putExtra(MainActivityKot.EXTRA_WORD, mySearchDateList!!.get(position).getWord())
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
                //sddm.deleteAll()
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
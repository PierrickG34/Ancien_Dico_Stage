package com.antoine_charlotte_romain.dictionary.Controllers

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.SearchDateAdapter
import com.antoine_charlotte_romain.dictionary.DataModel.SearchDateDataModel
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import org.jetbrains.anko.db.BLOB
import org.jetbrains.anko.db.BlobParser
import org.jetbrains.anko.db.SqlType
import org.jetbrains.anko.db.rowParser
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.sql.Blob
import java.text.SimpleDateFormat
import java.util.*

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

    private var sddm: SearchDateDataModel? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val thisView: View? = inflater!!.inflate(R.layout.fragment_history, container, false)

        this.historySearch = thisView!!.findViewById(R.id.historySearch) as EditText
        this.gridViewHistory = thisView.findViewById(R.id.gridViewHistory) as GridView
        this.advancedSearchButton = thisView.findViewById(R.id.buttonAdvancedSearch) as Button
        this.resetButton = thisView.findViewById(R.id.buttonReset) as Button

        println("HistoryFragmentKot.kt -- Test onCreateView")

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
        when (item!!.getItemId()) {
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

    private fun initListView()
    {
        this.historyLimit = 10
        this.historyOffset = 0
        this.allLoaded = false

        this.progressDialog = ProgressDialog(activity)
        this.progressDialog!!.setMessage(getString(R.string.loadingHistory))
        this.progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        this.progressDialog!!.setIndeterminate(true)
        this.progressDialog!!.setCancelable(false)
        this.progressDialog!!.getWindow().setGravity(Gravity.BOTTOM)

        println("HistoryFragmentKot.kt -- Test initListView")

//        var img = BitmapFactory.decodeResource(resources, R.drawable.ic_action_create)
//        var bos: ByteArrayOutputStream? = ByteArrayOutputStream();
//        img.compress(Bitmap.CompressFormat.PNG, 100, bos);
//
//        val bArray : ByteArray = bos!!.toByteArray()
//        //val parser = BlobParser.parseRow(bArray)
//
//
//
//        var test: WordSQLITE? = WordSQLITE(context, "1", "note", bArray, bArray, "headword", null, "2")
//        test!!.save()
//        var resss: List<Word>? = null
//        resss = test!!.selectAll()
//        println(resss)
//        Toast.makeText(activity, getString(R.string.historyCleared), Toast.LENGTH_SHORT).show()

//        sddm = SearchDateDataModel(activity)
//        mySearchDateList = sddm.selectAll(historyLimit, historyOffset)

//        myAdapter = SearchDateAdapter(activity, R.layout.row_history, mySearchDateList)
//
//        this.gridViewHistory!!.setAdapter(myAdapter)
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
//                    if (lastInScreen == mySearchDateList.size && !loadingMore && !allLoaded) {
//                        progressDialog!!.show()
//                        val thread = Thread(null, loadMoreHistory)
//                        thread.start()
//                    }
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
//                    mySearchDateList.clear()
//                    val tempList: ArrayList<SearchDate>
//
//                    if (s.length > 0) {
//                        val search = s.toString()
//                        tempList = sddm.select(search)
//                    } else {
//                        historyOffset = 0
//                        tempList = sddm.selectAll(historyLimit, historyOffset)
//                        allLoaded = false
//                    }
//
//                    for (i in tempList.indices) {
//                        mySearchDateList.add(tempList[i])
//                    }
//                    myAdapter.notifyDataSetChanged()
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

//            dialogBuilder.setPositiveButton(R.string.search) { dialog, which ->
//                mySearchDateList.clear()
//                val tempList = sddm.select(dateBeforeEditText.text.toString(), dateAfterEditText.text.toString())
//                if (tempList != null) {
//                    for (i in tempList!!.indices) {
//                        mySearchDateList.add(tempList!!.get(i))
//                    }
//                }
//                myAdapter.notifyDataSetChanged()
//                allLoaded = true
//                dialog.cancel()
//            }

            dialogBuilder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }

            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        })

        this.resetButton!!.setOnClickListener(View.OnClickListener { initListView() })
    }

    /**
     * This function is used to clear the search history of the app
     */
    private fun clearHistory() {
        val alert = AlertDialog.Builder(activity)
        alert.setMessage(getString(R.string.clearHistory) + " ?")
        alert.setPositiveButton(getString(R.string.clear)) { dialog, whichButton ->
            Toast.makeText(activity, getString(R.string.historyCleared), Toast.LENGTH_SHORT).show()
            //sddm.deleteAll()
            //initListView();
        }

        alert.setNegativeButton(R.string.cancel) { dialog, whichButton -> }

        alert.show()
    }


}
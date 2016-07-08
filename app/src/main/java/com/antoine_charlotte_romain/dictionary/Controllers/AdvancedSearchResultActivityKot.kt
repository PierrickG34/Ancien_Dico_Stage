package com.antoine_charlotte_romain.dictionary.Controllers

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView


import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.AdvancedSearchResultsAdapter
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import com.dicosaure.Business.Translate.TranslateSQLITE

import java.util.ArrayList

class AdvancedSearchResultActivityKot : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var listResults: GridView? = null

    private var results: MutableList<Word>? = null
    private var wdm: WordSQLITE? = null
    private var myAdapter: AdvancedSearchResultsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_search_result)

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = findViewById(R.id.tool_bar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.title_activity_advanced_search_result)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Get data associated to the advanced search
        val intent = intent
        if (intent != null) {
            val begin = intent.getStringExtra(MainActivityKot.EXTRA_BEGIN_STRING)
            val middle = intent.getStringExtra(MainActivityKot.EXTRA_MIDDLE_STRING)
            val end = intent.getStringExtra(MainActivityKot.EXTRA_END_STRING)
            val searchOption = intent.getStringExtra(MainActivityKot.EXTRA_SEARCH_DATA)
            val dico = intent.getStringExtra(MainActivityKot.EXTRA_DICTIONARY)
            val partWhole = intent.getStringExtra(MainActivityKot.EXTRA_PART_OR_WHOLE)

            // find id of the dictionary
            val id: Long
            val ddm = DictionarySQLITE(this)
            if(!dico.equals("All")) {
                id = ddm.getIdByName(dico)
            }
            else{
                id = 0
            }

            // search
            wdm = WordSQLITE(this,headword = "test")
            var translat = TranslateSQLITE(this, wdm,wdm)
            if (partWhole == MainActivityKot.PART_WORD) {
               // results = wdm!!.selectHeadword(begin, middle, end, id)

                if (searchOption == MainActivityKot.HEADWORD_ONLY) {
                    if(id == 0L) {
                        results = wdm!!.selectHeadword(begin, middle,end)
                    }
                    else{
                        results = wdm!!.selectHeadwordByIdDico(begin, middle,end, id)
                    }
                }
                else if (searchOption == MainActivityKot.ALL_DATA) {
                    var words : MutableList<Word>
                    if(id == 0L) {
                        results = wdm!!.selectNoteOrHeadword(begin, middle,end)
                        words = wdm!!.selectHeadword(begin, middle, end)
                    }
                    else{
                        results = wdm!!.selectNoteOrHeadwordByIdDico(begin, middle,end, id)
                        words = wdm!!.selectHeadwordByIdDico(begin, middle, end, id)
                    }
                    var resultTrans: MutableList<Word>? = mutableListOf()
                    if (!words.isEmpty()) {
                        val idWord = (words.component1()).idWord
                        var resultsId = translat!!.selectWordToByWordFrom(idWord!!, id)
                        var it = resultsId!!.iterator()
                        var e : Word
                        while (it.hasNext()) {
                            if(id == 0L) {
                                e = wdm!!.getWordById(it.next())
                            }
                            else{
                                e = wdm!!.getWordByIdByIdDico(it.next(), id)
                            }
                            resultTrans = resultTrans!!.plus(e) as MutableList<Word>
                        }
                    }
                    var it = resultTrans?.iterator()
                    while(it!!.hasNext()) {
                        val el = it.next()
                        if (!results!!.contains(el)) {
                            results = results!!.plus(el) as MutableList<Word>
                        }
                    }
                    Log.d("allData",results.toString())
                }
                else if (searchOption == MainActivityKot.MEANING_ONLY) {
                    var words : MutableList<Word>
                    if(id == 0L) {
                        words = wdm!!.selectHeadword(begin, middle, end)
                    }
                    else{
                        words = wdm!!.selectHeadwordByIdDico(begin, middle, end, id)
                    }
                    if (!words.isEmpty()) {
                        val idWord = (words.component1()).idWord
                        var resultsId = translat!!.selectWordToByWordFrom(idWord!!, id)
                        var it = resultsId!!.iterator()
                        results = mutableListOf()
                        var e : Word
                        while(it.hasNext()) {
                            if(id == 0L) {
                                e = wdm!!.getWordById(it.next())
                            }
                            else{
                                e = wdm!!.getWordByIdByIdDico(it.next(), id)
                            }
                            results = results!!.plus(e) as MutableList<Word>
                        }
                    }
                } else if (searchOption == MainActivityKot.NOTES_ONLY) {
                    if(id == 0L) {
                        results = wdm!!.selectNote(begin, middle, end)
                    }
                    else{
                        results = wdm!!.selectNoteByIdDico(begin, middle, end, id)
                    }
                }

            }

            else {
                if (searchOption == MainActivityKot.HEADWORD_ONLY) {
                    if(id == 0L) {
                        results = wdm!!.selectWholeHeadword(end)
                    }
                    else{
                        results = wdm!!.selectWholeHeadwordByIdDico(end,id)
                    }

                }
                else if (searchOption == MainActivityKot.ALL_DATA) {
                    val words : MutableList<Word>
                    if(id == 0L) {
                        results = wdm!!.selectWholeNoteOrHeadword(end)
                        words = wdm!!.selectWholeHeadword(end)
                    }
                    else{
                        results = wdm!!.selectWholeNoteOrHeadwordByIdDico(end, id)
                        words = wdm!!.selectWholeHeadwordByIdDico(end,id)
                    }
                    var resultTrans: MutableList<Word>? = mutableListOf()
                    if (!words.isEmpty()) {
                        val idWord = (words.component1()).idWord
                        var resultsId = translat!!.selectWordToByWordFrom(idWord!!, id)
                        var it = resultsId!!.iterator()
                        var e : Word
                        while (it.hasNext()) {
                            if(id == 0L) {
                                e = wdm!!.getWordById(it.next())
                            }
                            else{
                                e = wdm!!.getWordByIdByIdDico(it.next(), id)
                            }
                            resultTrans = resultTrans!!.plus(e) as MutableList<Word>
                        }
                    }
                    var it = resultTrans?.iterator()
                    while(it!!.hasNext()) {
                        val el = it.next()
                        if (!results!!.contains(el)) {
                            results = results!!.plus(el) as MutableList<Word>
                        }
                    }
                        Log.d("allData", results.toString())

                }
                else if (searchOption == MainActivityKot.MEANING_ONLY) {
                    val words : MutableList<Word>
                    if(id == 0L) {
                        words = wdm!!.selectWholeHeadword(end)
                    }
                    else{
                        words = wdm!!.selectWholeHeadwordByIdDico(end,id)
                    }
                    if(!words.isEmpty())
                    {
                        val idWord = (words.component1()).idWord
                        var resultsId = translat!!.selectWordToByWordFrom(idWord!!, id)
                        var it = resultsId!!.iterator()
                        results = mutableListOf()
                        var e : Word
                        while(it.hasNext())
                        {
                            if(id == 0L) {
                                e = wdm!!.getWordById(it.next())
                            }
                            else{
                                e = wdm!!.getWordByIdByIdDico(it.next(), id)
                            }
                            results = results!!.plus(e) as MutableList<Word>
                        }
                    }

                }
                else if (searchOption == MainActivityKot.NOTES_ONLY) {
                    if(id == 0L) {
                        results = wdm!!.selectWholeNote(end)
                    }
                    else{
                        results = wdm!!.selectWholeNoteByIdDico(end, id)
                    }
                }
            }

        }

        // Display results
       /* listResults = findViewById(R.id.resultsList) as GridView?
        if (results!!.size > 0) {

            myAdapter = AdvancedSearchResultsAdapter(this, R.layout.row_advanced_search_result, results)

            listResults!!.setAdapter(myAdapter)

            listResults!!.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
                val wordDetailIntent = Intent(this@AdvancedSearchResultActivity, WordActivity::class.java)
                wordDetailIntent.putExtra(MainActivityKot.EXTRA_WORD, results!!.get(position))

                val ddm = DictionarySQLITE(applicationContext)
                wordDetailIntent.putExtra(MainActivityKot.EXTRA_DICTIONARY, ddm.select((results as ArrayList<Word>?)!!.get(position).idDictionary))

                startActivity(wordDetailIntent)
            })
        }
        else {
            val advancedSearchLayout = findViewById(R.id.advanced_search) as LinearLayout?

            advancedSearchLayout!!.removeView(listResults)

            val textResult = TextView(this)
            textResult.text = getString(R.string.no_result)
            textResult.gravity = Gravity.CENTER
            textResult.setPadding(0, 10, 0, 0)
            advancedSearchLayout.addView(textResult)
        }*/
    }
}

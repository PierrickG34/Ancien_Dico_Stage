package ie.csis.dicosaure.views.activities


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.widget.*
import ie.csis.dicosaure.views.adapters.AdvancedSearchResultsAdapter
import ie.csis.dicosaure.Controllers.R
import ie.csis.dicosaure.views.activities.WordViewActivity
import ie.csis.dicosaure.views.activities.MainActivity
import ie.csis.dicosaure.model.translate.TranslateSQLITE
import ie.csis.dicosaure.model.dictionary.DictionarySQLITE
import ie.csis.dicosaure.model.word.Word
import ie.csis.dicosaure.model.word.WordSQLITE

class AdvancedSearchResultActivity : AppCompatActivity() {

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
            val begin = (intent.getStringExtra(MainActivity.EXTRA_BEGIN_STRING)).trim()
            val middle = (intent.getStringExtra(MainActivity.EXTRA_MIDDLE_STRING)).trim()
            val end = (intent.getStringExtra(MainActivity.EXTRA_END_STRING)).trim()
            val searchOption = intent.getStringExtra(MainActivity.EXTRA_SEARCH_DATA)
            val dico = intent.getStringExtra(MainActivity.EXTRA_DICTIONARY)
            val partWhole = (intent.getStringExtra(MainActivity.EXTRA_PART_OR_WHOLE)).trim()

            // find id of the dictionary
            val id: Long
            val ddm = DictionarySQLITE(this)
            //if( ( (!dico.equals("All")).and(!dico.equals("Tous")) ) ) {
            if( !dico.equals(this.getResources().getString(R.string.allDico))) {
                // Search in a particular dictionary
                id = ddm.getIdByName(dico)
            }
            else{                           // Search in all the dictionaries
                id = 0
            }

            // search
            wdm = WordSQLITE(this, headword = "test")
            var translat = TranslateSQLITE(this, wdm, wdm)
            if (partWhole == MainActivity.PART_WORD) {   // Search word by "begin","content","end"
                if (searchOption == MainActivity.HEADWORD_ONLY) {    //Search by the headword
                    if(id == 0L) {
                        results = wdm!!.selectHeadword(begin, middle,end) // Search in all dictionaries
                    }
                    else{
                        results = wdm!!.selectHeadwordByIdDico(begin, middle,end, id) // Search in the dictionary which have the id "id"
                    }
                }
                else if (searchOption == MainActivity.ALL_DATA) {    // Search by headWord, translation and note
                    var words : MutableList<Word>
                    if(id == 0L) {
                        results = wdm!!.selectNoteOrHeadword(begin, middle,end) // Return all the words corresponding to the Note search and headword search
                    }
                    else{
                        results = wdm!!.selectNoteOrHeadwordByIdDico(begin, middle,end, id)
                    }
                    words = wdm!!.selectHeadword(begin, middle, end)
                    var resultTrans: MutableList<Word>? = mutableListOf()
                    if (!words.isEmpty()) {     // if the search by Headword doesn't return an empty array
                        var idIt = words!!.iterator()
                        while(idIt.hasNext())
                        {
                            var resultsId = translat!!.selectListWordsOutLangFromWordInLang(idIt.next().idWord!!, id) // return all the id of the word found by search translation
                            var it = resultsId!!.iterator() // an iterator to parse all the array resultsID (all the id word found by seearch translation)
                            var e : Word
                            while (it.hasNext()) { // until there is an other word to iterate
                                if(id == 0L) {
                                    e = wdm!!.getWordById(it.next()) // return the word corresponding to the id
                                }
                                else{
                                    e = wdm!!.getWordByIdByIdDico(it.next(), id)
                                }
                                resultTrans = resultTrans!!.plus(e) as MutableList<Word>
                            }
                        }
                    }
                    var it = resultTrans?.iterator() // an iterator to parse all the array resultsID (all the id word found by seearch translation)
                    while(it!!.hasNext()) { // this while enables that all research doesn't return the same word two time
                        val el = it.next()
                        if (!results!!.contains(el)) {
                            results = results!!.plus(el) as MutableList<Word>
                        }
                    }
                }
                else if (searchOption == MainActivity.MEANING_ONLY) { // Search by translation
                    var words : MutableList<Word>
                    words = wdm!!.selectHeadword(begin, middle, end) // return all the word wich contain middle or begin by begin or end by end in all
                    //Log.d("Part_Trans_word","Result 2 - ${words}")
                    if (!words.isEmpty()) { // if it found words
                        //val idWord = (words.component1()).idWord // get the id of the word found
                        var idIt = words!!.iterator()
                        results = mutableListOf()
                        while(idIt.hasNext())
                        {
                            var resultsId = translat!!.selectListWordsOutLangFromWordInLang((idIt.next()).idWord!!, id)
                            var it = resultsId!!.iterator()
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
                    }
                } else if (searchOption == MainActivity.NOTES_ONLY) { // Search by note
                    if(id == 0L) {
                        results = wdm!!.selectNote(begin, middle, end)
                    }
                    else{
                        results = wdm!!.selectNoteByIdDico(begin, middle, end, id)
                    }
                }

            }

            else {
                if (searchOption == MainActivity.HEADWORD_ONLY) { // Same thing that search by partWord except here it search by WholeWord
                    if(id == 0L) {
                        results = wdm!!.selectWholeHeadword(end)
                    }
                    else{
                        results = wdm!!.selectWholeHeadwordByIdDico(end,id)
                    }
                }
                else if (searchOption == MainActivity.ALL_DATA) {
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
                        var resultsId = translat!!.selectListWordsOutLangFromWordInLang(idWord!!, id)
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
                }
                else if (searchOption == MainActivity.MEANING_ONLY) {
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
                        var resultsId = translat!!.selectListWordsOutLangFromWordInLang(idWord!!, id)
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
                else if (searchOption == MainActivity.NOTES_ONLY) {
                    if(id == 0L) {
                        results = wdm!!.selectWholeNote(end)
                    }
                    else{
                        results = wdm!!.selectWholeNoteByIdDico(end, id)
                    }
                }
            }
            Log.d("AdvancedSearchResult","Result - $results")
        }

        // Display results
        listResults = findViewById(R.id.resultsList) as GridView?
        if (results!!.size > 0) {
            myAdapter = AdvancedSearchResultsAdapter(this, R.layout.row_advanced_search_result, results!!)
            listResults!!.setAdapter(myAdapter)

            listResults!!.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
                val wordDetailIntent = Intent(this@AdvancedSearchResultActivity, WordViewActivity::class.java)

                val wordClicked = results!!.get(position)
                wordDetailIntent.putExtra(MainActivity.EXTRA_WORD, wordClicked)

                val ddm = DictionarySQLITE(applicationContext)
                wordDetailIntent.putExtra(MainActivity.EXTRA_DICTIONARY, ddm.select(results!!.get(position).idDictionary!!))

                startActivity(wordDetailIntent)
            })

        } else {
            val advancedSearchLayout = findViewById(R.id.advanced_search) as LinearLayout?

            advancedSearchLayout!!.removeView(listResults)

            val textResult = TextView(this)
            textResult.text = getString(R.string.no_result)
            textResult.gravity = Gravity.CENTER
            textResult.setPadding(0, 10, 0, 0)
            advancedSearchLayout.addView(textResult)
        }
    }
}

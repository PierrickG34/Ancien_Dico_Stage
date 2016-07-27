package com.antoine_charlotte_romain.dictionary.Controllers


//import com.antoine_charlotte_romain.dictionary.Business.old.*
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE


/**
 * A simple [Fragment] subclass.
 */
class SearchFragmentKot : Fragment() {

    private var thisView: View? = null

    private var selectedDictionary: Dictionary? = null

    private var beginningText: EditText? = null
    private var containsText: EditText? = null
    private var endText: EditText? = null
    private var targetDictionary: EditText? = null
    private var searchIn: EditText? = null
    private var radioGroup: RadioGroup? = null
    private var partWord: RadioButton? = null
    private var wholeWord: RadioButton? = null
    private var searchTabButton: MenuItem? = null

    private var isReady: Boolean = false

    private var searchOptions: Array<String>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        thisView = inflater!!.inflate(R.layout.fragment_search, container, false)

        val intent = activity.intent
        // selectedDictionary = intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY) as Dictionary

        selectedDictionary = intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY) as? Dictionary

        beginningText = thisView!!.findViewById(R.id.beginString) as EditText
        containsText = thisView!!.findViewById(R.id.middleString) as EditText
        endText = thisView!!.findViewById(R.id.endString) as EditText
        targetDictionary = thisView!!.findViewById(R.id.targetDico) as EditText
        searchIn = thisView!!.findViewById(R.id.searchin) as EditText
        radioGroup = thisView!!.findViewById(R.id.boutonsradio) as RadioGroup
        partWord = thisView!!.findViewById(R.id.part) as RadioButton
        wholeWord = thisView!!.findViewById(R.id.whole) as RadioButton

        searchOptions =  arrayOf<String>(getString(R.string.headword_only), getString(R.string.translation_meaning_only), getString(R.string.notes_only), getString(R.string.all_data))

        setHasOptionsMenu(true)

        initSearchView()

        return thisView
    }

    override fun onResume() {
        super.onResume()

        isReady = false
        if (beginningText!!.text.toString().trim { it <= ' ' }.length > 0 || containsText!!.text.toString().trim { it <= ' ' }.length > 0 || endText!!.text.toString().trim { it <= ' ' }.length > 0) {
            isReady = true
        }

        if (radioGroup!!.checkedRadioButtonId == R.id.part) {
            // Set hint string of the end string EditText
            endText!!.hint = getString(R.string.ends_with)
            // Show all EditText (if they were gone)
            beginningText!!.visibility = View.VISIBLE
            containsText!!.visibility = View.VISIBLE
        } else {
            // Set hint string of the end string EditText
            endText!!.hint = getString(R.string.Word)
            // Hide contain and end EditText (if they were displayed)
            beginningText!!.visibility = View.GONE
            containsText!!.visibility = View.GONE
        }
    }

    /**
     * This function creates the buttons on the toolBar
     * @param menu
     * *
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu!!.clear()
        menuInflater!!.inflate(R.menu.menu_search_fragment, menu)
        searchTabButton = menu.findItem(R.id.action_search)

        searchTabButton!!.isVisible = isReady

        initTextFields()
    }

    /**
     * This function is called when the user click on a button of the toolBar
     * @param item
     * *
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_search -> {
                advancedSearch(thisView!!)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun initSearchView() {
        // set dictionary
        if (selectedDictionary == null) {
            targetDictionary!!.setText(getString(R.string.target_dico) + " : " + getString(R.string.allDico))
        } else {
            targetDictionary!!.setText(getString(R.string.target_dico) + " : " + selectedDictionary!!.getNameDictionary())
        }

        // set search option
        searchIn!!.setText(getString(R.string.search_in) + " : " + searchOptions!!.get(0))

        searchIn!!.setOnClickListener { v -> displaySearchOptions(v) }

        partWord!!.setOnClickListener {
            // Set hint string of the end string EditText
            endText!!.hint = getString(R.string.ends_with)
            // Show all EditText (if they were gone)
            beginningText!!.visibility = View.VISIBLE
            containsText!!.visibility = View.VISIBLE
        }

        wholeWord!!.setOnClickListener {
            // Set hint string of the end string EditText
            endText!!.hint = getString(R.string.Word)
            // Hide contain and end EditText (if they were displayed)
            beginningText!!.visibility = View.GONE
            containsText!!.visibility = View.GONE
        }
        /*val id : String = targetDictionary!!.getText().toString()
        Log.d("MontagAMoi", id)*/
        targetDictionary!!.setOnClickListener { v -> displayDictionaries(v) }
}

    private fun initTextFields() {
        beginningText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(arg0: Editable) {
                val isReady = beginningText!!.text.toString().trim { it <= ' ' }.length > 0
                searchTabButton!!.isVisible = isReady
            }

        })

        containsText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(arg0: Editable) {
                val isReady = containsText!!.text.toString().trim { it <= ' ' }.length > 0
                searchTabButton!!.isVisible = isReady
            }

        })

        endText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(arg0: Editable) {
                val isReady = endText!!.text.toString().trim { it <= ' ' }.length > 0
                searchTabButton!!.isVisible = isReady
            }

        })
    }

    /**
     * This function is called when the user click the search option text
     * @param v
     */
    fun displaySearchOptions(v: View) {
        val display = searchOptions!!.clone()

        val ad = AlertDialog.Builder(activity).setTitle(R.string.search_in).setItems(display) { dialog, which ->
            dialog.dismiss()
            searchIn!!.setText(getString(R.string.search_in) + " : " + searchOptions!![which])
        }.setNegativeButton(R.string.returnString) { dialog, which -> }
        val alert = ad.create()
        alert.show()
    }

    /**
     * This function is called when the user click the dictionary text
     * @param v
     */
    fun displayDictionaries(v: View) {
        val ddm = DictionarySQLITE(this.activity)
        val dico = ddm.selectAll()
        val nameDico = arrayOfNulls<String>(dico.size + 1)
        nameDico[0] = getString(R.string.allDico)
        for (i in dico.indices) {
            nameDico[i + 1] = dico[i].getNameDictionary()
        }
        val names = nameDico.clone()

        val ad = AlertDialog.Builder(activity).setTitle(R.string.choose_dictionary).setItems(names) { dialog, which ->
            dialog.dismiss()
            targetDictionary!!.setText(getString(R.string.target_dico) + " : " + names[which])
        }.setNegativeButton(R.string.returnString) { dialog, which -> }
        val alert = ad.create()
        alert.show()
    }

    /**
     * This function is called when the user click the search button
     * @param v
     */
    fun advancedSearch(v: View) {
        val intent = Intent(activity, AdvancedSearchResultActivityKot::class.java)

        intent.putExtra(MainActivityKot.EXTRA_BEGIN_STRING, beginningText!!.text.toString())
        intent.putExtra(MainActivityKot.EXTRA_MIDDLE_STRING, containsText!!.text.toString())
        intent.putExtra(MainActivityKot.EXTRA_END_STRING, endText!!.text.toString())

        // Let's see if the search has to be done on part or whole word
        when ((thisView!!.findViewById(R.id.boutonsradio) as RadioGroup).checkedRadioButtonId) {
            R.id.part -> intent.putExtra(MainActivityKot.EXTRA_PART_OR_WHOLE, MainActivityKot.PART_WORD)
            R.id.whole -> intent.putExtra(MainActivityKot.EXTRA_PART_OR_WHOLE, MainActivityKot.WHOLE_WORD)
        }

        // Let's get the chosen search option
        var tmp = searchIn!!.text.toString()
        val searchChoice = tmp.replace(getString(R.string.search_in) + " : ", "")

        when (getSearchChoiceRank(searchChoice)) {
            0 -> intent.putExtra(MainActivityKot.EXTRA_SEARCH_DATA, MainActivityKot.HEADWORD_ONLY)
            1 -> intent.putExtra(MainActivityKot.EXTRA_SEARCH_DATA, MainActivityKot.MEANING_ONLY)
            2 -> intent.putExtra(MainActivityKot.EXTRA_SEARCH_DATA, MainActivityKot.NOTES_ONLY)
            3 -> intent.putExtra(MainActivityKot.EXTRA_SEARCH_DATA, MainActivityKot.ALL_DATA)
        }

        // Let's get the targeted dictionary
        tmp = targetDictionary!!.text.toString()
        val dico = tmp.replace(getString(R.string.target_dico) + " : ", "")
        intent.putExtra(MainActivityKot.EXTRA_DICTIONARY, dico)

        startActivity(intent)

        beginningText!!.setText("")
        containsText!!.setText("")
        endText!!.setText("")
    }

    /**
     * This function finds which search option has been chosen
     * @param searchChoice
     * *          The string representing the search choice of the user
     * *
     * @return
     * *          The index number of the search choice in the searchChoice array
     */
    private fun getSearchChoiceRank(searchChoice: String): Int {
        for (i in searchOptions!!.indices) {
            if (searchChoice == searchOptions!![i]) {
                return i
            }
        }
        return -1
    }
}

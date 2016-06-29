package com.antoine_charlotte_romain.dictionary.Controllers

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.Utilities.KeyboardUtility
import com.antoine_charlotte_romain.dictionary.business.word.Word


class WordActivityKot : AppCompatActivity() {

    private var dictionaryText: EditText? = null
    private var headwordText: EditText? = null
    private var translationText: EditText? = null
    private var noteText: EditText? = null
    private var toolbar: Toolbar? = null
    private var saveButton: MenuItem? = null
    private val addButton: Button? = null

    private var layoutTranslations: RelativeLayout? = null
    private val addTranslationButton: FloatingActionButton? = null
    private var word_layout: RelativeLayout? = null

    private var wdm: WordSQLITE? = null
    private var selectedWord: Word? = null
    private var selectedDictionary: Dictionary? = null
    private var selectedWordSQLLite : WordSQLITE? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("JE PASSE DANS WORDACTIVITKOT")
        setContentView(R.layout.word)

        toolbar = findViewById(R.id.tool_bar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.details)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        selectedWord = intent.getSerializableExtra(MainActivityKot.EXTRA_WORD) as Word
        println("idWord : "+ (selectedWord as Word).idWord)
        println("note : "+ (selectedWord as Word).note)
        println("image : "+ (selectedWord as Word).image)
        println("sound : "+ (selectedWord as Word).sound)
        println("headword : "+ (selectedWord as Word).headword)
        println("dateView : "+ (selectedWord as Word).dateView)
        println("idDictionary : "+ (selectedWord as Word).idDictionary)

        selectedWordSQLLite = WordSQLITE(applicationContext, (selectedWord as Word).idWord, (selectedWord as Word).note, (selectedWord as Word).image, (selectedWord as Word).sound, (selectedWord as Word).headword, (selectedWord as Word).dateView, (selectedWord as Word).idDictionary)
        println("selectedDictionary "+ intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY))
        selectedDictionary = Dictionary(null,null,intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY).toString())
        //selectedDictionary = intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY) as Dictionary
        println("selectedDictionary "+selectedDictionary)
        dictionaryText = findViewById(R.id.editTextDictionary) as EditText?
        headwordText = findViewById(R.id.editTextHeadword) as EditText?
        translationText = findViewById(R.id.editTextTranslation1) as EditText?
        noteText = findViewById(R.id.editTextNote) as EditText?
        word_layout = findViewById(R.id.word_layout) as RelativeLayout?



        layoutTranslations = findViewById(R.id.layoutTranslations) as RelativeLayout?

        //    addTranslationButton = (FloatingActionButton) findViewById(R.id.add_button1);
        //    addTranslationButton.setOnClickListener(new OnClickListener() {
        //        /** This function is called when the user clicks on the add Button.
        //         *  It adds a new EditText unless the number of EditText is superior to 5.
        //         */
//        fun onClick(v: View) {
//            val enfants = layoutTranslations!!.childCount
//            println("enfants - " + enfants)
//            val lEditText = EditText(applicationContext)
//            val removeButton = findViewById(R.id.remove_button1)
//            if (enfants == 3) {
//                removeButton!!.visibility = View.INVISIBLE
//            }
//            val relativeParams = RelativeLayout.LayoutParams(headwordText!!.width, headwordText!!.height)
//            when (v.id) {
//                R.id.add_button1 -> {
//                    if (enfants < 6) {
//                        @IdRes val id = enfants + 1
//                        lEditText.id = id
//
//                        lEditText.setTextColor(Color.BLACK)
//                        lEditText.background.setColorFilter(Color.parseColor("#6d6d6d"), PorterDuff.Mode.SRC_ATOP)
//
//                        if (enfants == 2) {
//                            relativeParams.addRule(RelativeLayout.BELOW, R.id.editTextTranslation1)
//                            lEditText.setHintTextColor(Color.parseColor("#6d6d6d"))
//                            lEditText.hint = resources.getString(R.string.translation_children) + " " + enfants
//                            removeButton!!.visibility = View.INVISIBLE
//
//                        } else {
//                            relativeParams.addRule(RelativeLayout.BELOW, (layoutTranslations as RelativeLayout).getChildAt(enfants - 1).id)
//                            lEditText.setHintTextColor(Color.parseColor("#777777"))
//                            lEditText.hint = resources.getString(R.string.translation_children) + " " + enfants
//                        }
//                        layoutTranslations.addView(lEditText, relativeParams)
//                        removeButton!!.visibility = View.VISIBLE
//                    } else {
//                        Toast.makeText(applicationContext, R.string.maximum_translate, Toast.LENGTH_SHORT).show()
//                    }
//                }
//                R.id.remove_button1 -> {
//                    @IdRes val id = enfants
//                    if (enfants != 2) {
//                        (findViewById(id)!!.parent as ViewManager).removeView(findViewById(id))
//                        findViewById(id)!!.visibility = View.GONE
//                    }
//                }
//            }
//        }

        dictionaryText!!.isEnabled = false
        dictionaryText!!.setText((selectedDictionary as Dictionary).getNameDictionary())

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        setupUI(findViewById(R.id.word_layout)!!)
    }

    /**
     * This function is called when a child activity back to this view or finish
     */
    public override fun onResume() {
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (selectedWord != null) {
            menuInflater.inflate(R.menu.menu_word_details, menu)
            showDetails()
        } else {
            menuInflater.inflate(R.menu.menu_new_word, menu)
            saveButton = menu.findItem(R.id.action_add_word)
            newWord()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_word -> {
                addWord()
                return true
            }

            R.id.action_update_word -> {
                updateWord()
                return true
            }

            R.id.action_delete_word -> {
                deleteWord(findViewById(R.id.word_layout)!!)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This function show the details of a word and allow the user to update or delete it.
     * This function is called after the onCreate if a word was selected by the user.
     */
    private fun showDetails() {
        if (headwordText!!.text.toString().trim { it <= ' ' }.length <= 0) {
            headwordText!!.setText(selectedWord!!.headword)
        }
        headwordText!!.isEnabled = false
        if (translationText!!.text.toString().trim { it <= ' ' }.length <= 0) {
            translationText!!.setText(selectedWordSQLLite!!.getAllTranslationText())
        }
        if (noteText!!.text.toString().trim { it <= ' ' }.length <= 0) {
            noteText!!.setText(selectedWord!!.note)
        }

        supportActionBar!!.setTitle(getString(R.string.details) + " : " + selectedWord!!.headword)

        (selectedWordSQLLite as WordSQLITE).save()
    }

    /**
     * This function allow the user to create a new word.
     * This function is called after the onCreate if no word was selected by the user.
     */
    private fun newWord() {
        var isReady = false
        if (headwordText!!.text.toString().trim { it <= ' ' }.length <= 0) {
            headwordText!!.setText("")
        } else {
            isReady = true
        }
        headwordText!!.isFocusable = true
        if (translationText!!.text.toString().trim { it <= ' ' }.length <= 0) {
            translationText!!.setText("")
        }
        if (noteText!!.text.toString().trim { it <= ' ' }.length <= 0) {
            noteText!!.setText("")
        }

        supportActionBar!!.setTitle(R.string.new_word)

        saveButton!!.isVisible = isReady

        headwordText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(arg0: Editable) {
                val isReady = headwordText!!.text.toString().trim { it <= ' ' }.length > 0
                saveButton!!.isVisible = isReady
            }

        })
    }

    /**
     * This function is called on click on the saveButton, it update the selected word with the new values enter by the user.
     */
    private fun updateWord() {
        wdm = WordSQLITE(applicationContext, null, null, null, null, "", null, null)
//        selectedWord!!.setTranslation(translationText!!.text.toString())
        wdm!!.update(noteText!!.text.toString(), (selectedWord as Word).image, (selectedWord as Word).sound, (selectedWord as Word).headword!!, (selectedWord as Word).dateView, (selectedWord as Word).idDictionary)
        Toast.makeText(this, selectedWord!!.headword + getString(R.string.updated), Toast.LENGTH_LONG).show()
        finish()
    }

    /**
     * This function is called on click on the addWordButton, it insert a new word with the values enter by the user.
     */
    private fun addWord() {
        wdm = WordSQLITE(applicationContext, null, noteText!!.text.toString(), null, null, headwordText!!.text.toString(), null, selectedDictionary!!.idDictionary)


//        w.setTranslation(translationText!!.text.toString())


        val i = (wdm as WordSQLITE).save()

        when (i) {
            0 -> {
                Toast.makeText(this, (wdm as WordSQLITE).headword + getString(R.string.created), Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            1 -> Toast.makeText(this, getString(R.string.error) + " : " + (wdm as WordSQLITE).headword + " " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show()
            2 -> Toast.makeText(this, getString(R.string.error) + " " + getString(R.string.dico_not_exists), Toast.LENGTH_SHORT).show()
            3 -> Toast.makeText(this, getString(R.string.error) + " " + getString(R.string.no_selected_dico), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * This function is called on click on the deleteButton, it asks for a confirmation and then delete the selected word.
     * @param view the view which launched this function
     */
    fun deleteWord(view: View) {
        val alert = AlertDialog.Builder(this)
        alert.setMessage(getString(R.string.delete_word) + " ?")
        alert.setPositiveButton(getString(R.string.delete)) { dialog, whichButton ->
            Toast.makeText(applicationContext, selectedWord!!.headword + getString(R.string.deleted), Toast.LENGTH_SHORT).show()
            wdm = WordSQLITE(applicationContext, null, null, null, null, "", null, null)
            (wdm as WordSQLITE).delete((selectedWord as Word).idWord!!)
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> }

        alert.show()
    }

    /**
     * This function is used to hide the keyBoard on click outside an editText
     * @param view the view which launched this function
     */
    fun setupUI(view: View) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {

            view.setOnTouchListener { v, event ->
                KeyboardUtility.hideSoftKeyboard(this@WordActivityKot)
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {

            for (i in 0..view.childCount - 1) {

                val innerView = view.getChildAt(i)

                setupUI(innerView)
            }
        }
    }
}

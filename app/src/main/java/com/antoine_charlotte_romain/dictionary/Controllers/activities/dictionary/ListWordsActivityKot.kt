package com.antoine_charlotte_romain.dictionary.Controllers.activities.dictionary

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.*
import com.antoine_charlotte_romain.dictionary.Controllers.*
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.WordAdapterCallbackKot
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.WordAdapterKot
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.Utilities.KeyboardUtility
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import org.jetbrains.anko.ctx
import java.util.*

/**
 * Created by dineen on 24/06/2016.
 */
class ListWordsActivityKot() : AppCompatActivity(), AdapterView.OnItemClickListener, WordAdapterCallbackKot {

    companion object {
        val CONTEXT_MENU_MODIFY = 0
        val CONTEXT_MENU_DELETE = 1
        val NORMAL_STATE = 0
        val DELETE_STATE = 1
        val NEW_WORD = 1
        val DELETE_WORD = 2
        val SELECT_FILE = 0
    }

    var filterWords: EditText? = null
    var gridViewWords: GridView? = null
    var toolbar: Toolbar? = null
    var menuButton: FloatingActionButton? = null
    var addButton: FloatingActionButton? = null
    var addText: TextView? = null
    var importCsvButton: FloatingActionButton? = null
    var importText: TextView? = null
    var exportCsvButton: FloatingActionButton? = null
    var exportText: TextView? = null
    var inLangField: EditText? = null
    var outLangField: EditText? = null
    var menu: Menu? = null
    var backgroundMenuView: View? = null
    var progressDialog: ProgressDialog? = null

    var wdm: WordSQLITE? = null
    var ddm: DictionarySQLITE? = null
    var selectedDictionary: Dictionary? = null
    var myWordsList: ArrayList<Word> = ArrayList<Word>()
    var myWordsListInit: ArrayList<Word> = ArrayList<Word>()
    var myAdapter: WordAdapterKot? = null

    var isOpen: Boolean = false
    var undo: Boolean = false
    var loadingMore: Boolean = false
    var allLoaded: Boolean = false
    var hidden: Boolean = false
    var wordsLimit: Int = 0
    var wordsOffset: Int = 0
    var myLastFirstVisibleItem: Int = 0
    var actualListSize: Int = 0
    var stateMode: Int = 0

    /**
     * This function is called when the view is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_words)

        this.toolbar = findViewById(R.id.tool_bar) as Toolbar?
        setSupportActionBar(this.toolbar)
        this.supportActionBar!!.setTitle(R.string.list_words_activity)
        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val intent = this.intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY)

        if (intent != null) {
            this.selectedDictionary = intent as Dictionary
        }
        //this.selectedDictionary = intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY) as Dictionary

        this.filterWords = findViewById(R.id.filterWords) as EditText?
        this.gridViewWords = findViewById(R.id.gridViewWords) as GridView?
        this.menuButton = findViewById(R.id.floatingMenuButton) as FloatingActionButton?
        this.addButton = findViewById(R.id.addWordButton) as FloatingActionButton?
        this.addText = findViewById(R.id.textAddAWord) as TextView?
        this.importCsvButton = findViewById(R.id.importCsvButton) as FloatingActionButton?
        this.importText = findViewById(R.id.textImportACsv) as TextView?
        this.exportCsvButton = findViewById(R.id.exportCsvButton) as FloatingActionButton?
        this.exportText = findViewById(R.id.textExportACsv) as TextView?
        this.backgroundMenuView = findViewById(R.id.surfaceView)

        this.progressDialog = ProgressDialog(this)
        this.progressDialog!!.setMessage(getString(R.string.loadingWords))
        this.progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        this.progressDialog!!.setIndeterminate(true)
        this.progressDialog!!.setCancelable(false)
        this.progressDialog!!.getWindow().setGravity(Gravity.BOTTOM)

        this.gridViewWords!!.setOnItemClickListener(this)

        this.setupUI(findViewById(R.id.list_words_layout)!!)

        registerForContextMenu(this.gridViewWords)

        if (this.intent.getBooleanExtra(MainActivityKot.EXTRA_RENAME, false)) {
            getIntent().removeExtra(MainActivityKot.EXTRA_RENAME)
            this.renameDictionary()
        }


        this.menuButton!!.bringToFront()

        this.allLoaded = false
        this.wordsLimit = 10
        this.wordsOffset = 0
        this.myLastFirstVisibleItem = 0

        this.backgroundMenuView!!.setOnClickListener(View.OnClickListener { v -> showFloatingMenu(v) })

        this.initListView()
        this.initSearch()
        this.showToolbarMenu()

    }

    /**
     * This function is called when a child activity back to this view or finish
     */
    public override fun onResume() {
        super.onResume()

        this.loadingMore = false
        this.stateMode = NORMAL_STATE
        this.normalMode()

        this.backgroundMenuView!!.setVisibility(View.GONE)
        if (this.selectedDictionary != null) {
            this.isOpen = false
            this.menuButton!!.animate().translationY(0f)
            this.hidden = false
            this.exportCsvButton!!.setVisibility(View.VISIBLE)
            this.importCsvButton!!.setVisibility(View.VISIBLE)
            this.addButton!!.setVisibility(View.VISIBLE)
        }
        this.initListView()
        this.initSearch()
    }

    /**
     * This function is called when configurations have changed
     * @param newConfig the new configuration to set
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * Creating the options menu
     * @param menu
     * @return true if the menu is created, false else
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu
        if (this.selectedDictionary != null) {
            this.menuInflater.inflate(R.menu.menu_list_words, menu)
        }
        return true
    }

    /**
     * Calling the differents functions depending on the user choice
     * @param item the user choice
     * @return true if success, false else
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_export_csv -> {
                this.exportCsv(findViewById(R.id.list_words_layout)!!)
                return true
            }

            R.id.action_import_csv -> {
                this.importCsv(findViewById(R.id.list_words_layout)!!)
                return true
            }

            R.id.action_add_word -> {
                this.newWord(findViewById(R.id.list_words_layout)!!)
                return true
            }

            R.id.action_rename_dictionary -> {
                this.renameDictionary()
                return true
            }

            R.id.action_delete_dictionary -> {
                this.deleteDictionary()
                return true
            }

            R.id.action_delete_words -> {
                this.multipleDeleteMode()
                return true
            }

            R.id.action_cancel_list -> {
                this.normalMode()
                return true
            }

            R.id.action_delete_list -> {
                this.deleteSelectedWords()
                this.initListView()
                this.initSearch()
                this.myAdapter!!.notifyDataSetChanged()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Function that load all the words of a dictionary from the database and show them on the listView
     */
    private fun initListView() {

        Log.d("ListWordsActivity","InitListView")

        this.wdm = WordSQLITE(this.applicationContext)
        val select: Boolean

        if (this.selectedDictionary == null) { //All dictionaries
            this.myWordsList = ArrayList<Word>(this.wdm!!.selectAll())
            this.myWordsListInit = ArrayList<Word>(this.myWordsList)
            this.menuButton!!.setVisibility(View.GONE)
            this.addButton!!.setVisibility(View.GONE)
            this.addText!!.setVisibility(View.GONE)
            this.importCsvButton!!.setVisibility(View.GONE)
            this.importText!!.setVisibility(View.GONE)
            this.exportCsvButton!!.setVisibility(View.GONE)
            this.exportText!!.setVisibility(View.GONE)
            this.supportActionBar!!.setTitle(R.string.allDico)
            select = false
        }
        else {
            val ddm = DictionarySQLITE(this.applicationContext,this.selectedDictionary!!.inLang , this.selectedDictionary!!.outLang, this.selectedDictionary!!.idDictionary)
            this.myWordsList = ArrayList<Word>(ddm.selectAllWords())
            this.myWordsListInit = ArrayList<Word>(this.myWordsList)
            this.menuButton!!.setVisibility(View.VISIBLE)
            this.supportActionBar!!.setTitle(this.selectedDictionary!!.getNameDictionary())
            select = true
        }

        this.myAdapter = WordAdapterKot(this, R.layout.row_word, this.myWordsList, select)
        this.myAdapter!!.callback = this

        this.gridViewWords!!.setAdapter(this.myAdapter)
        this.gridViewWords!!.setTextFilterEnabled(true)

    }

    /**
     * This function is called when the user click on an item of the gridView
     * @param parent has to be there for override
     * @param view has to be there for override
     * @param position the position of the clicked item
     * @param id has to be there for override
     */
    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (!this.isOpen) {
            this.modify(position)
        }
    }

    /**
     * Function that init the search bar
     */
    fun initSearch() {
        //Creating the EditText for searching inside the word
        this.filterWords!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                myWordsList!!.clear()
                val search = s.toString()
                for (i in myWordsListInit!!.indices) {
                    if (myWordsListInit!!.get(i).headword!!.toLowerCase().contains(search.toLowerCase())) {
                        myWordsList!!.add(myWordsListInit!!.get(i))
                    }
                }
                myAdapter!!.notifyDataSetChanged()
            }
        })
    }

    /**
     * This function is called when the user click on the addWord button, it launch the view newWord
     * @param view the current view
     */
    fun newWord(view: View) {
        if (this.isOpen) {
            showFloatingMenu(view)
        }
        //val newWordIntent = Intent(this, WordViewKot::class.java)
        val newWordIntent = Intent(this, WordViewEditKot::class.java)

        newWordIntent.putExtra(MainActivityKot.EXTRA_DICTIONARY, this.selectedDictionary)

        startActivityForResult(newWordIntent, NEW_WORD)

        if (this.filterWords!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.filterWords!!.setText("")
        }
    }

    /**
     * This function is called when the user click on the exportCsv button, it launch the view exportACsv
     * @param view the current view
     */
    fun exportCsv(view: View) {
        if (this.isOpen) {
            showFloatingMenu(view)
        }
        val exportCSVintent = Intent(this, CSVExportKot::class.java)
        exportCSVintent.putExtra(MainActivityKot.EXTRA_DICTIONARY, this.selectedDictionary)

        startActivity(exportCSVintent)

        if (this.filterWords!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.filterWords!!.setText("")
        }
    }

    /**
     * This function is called when the user click on the importCsv button, it launch the view importACsv
     * @param view the current view
     */
    fun importCsv(view: View) {
        if (this.isOpen) {
            showFloatingMenu(view)
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/comma-separated-values"

        // special intent for Samsung file manager
        val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
        sIntent.putExtra("CONTENT_TYPE", "text/comma-separated-values")
        sIntent.addCategory(Intent.CATEGORY_DEFAULT)

        if (this.packageManager.resolveActivity(sIntent, 0) != null) {
            startActivityForResult(sIntent, SELECT_FILE)
        } else {
            startActivityForResult(intent, SELECT_FILE)
        }

        if (this.filterWords!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.filterWords!!.setText("")
        }
    }

    /**
     * This function launch the activity advancedSearch
     * @param view the current view
     */
    fun advancedSearch(view: View) {
        val advancedSearchIntent = Intent(this, MainActivityKot::class.java)

        advancedSearchIntent.putExtra(MainActivityKot.EXTRA_FRAGMENT, "advancedSearch")
        advancedSearchIntent.putExtra(MainActivityKot.EXTRA_DICTIONARY, this.selectedDictionary)

        startActivity(advancedSearchIntent)

        if (this.filterWords!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.filterWords!!.setText("")
        }
    }

    /**
     * This function is used to show or hide the buttons add a word and import a csv after the click on the floatingMenuButton (+)
     * @param view the current view
     */
    override fun showFloatingMenu(view: View) {
        if (this.isOpen) {
            this.backgroundMenuView!!.setVisibility(View.GONE)

            animationCloseMenu(this.exportCsvButton!!, 3)
            animationCloseMenu(this.exportText!!, 3)
            animationCloseMenu(this.importCsvButton!!, 2)
            animationCloseMenu(this.importText!!, 2)
            animationCloseMenu(this.addButton!!, 1)
            animationCloseMenu(this.addText!!, 1)

            this.menuButton!!.animate().rotation(0f)
            this.menuButton!!.bringToFront()

            this.isOpen = false
        } else {
            this.backgroundMenuView!!.setVisibility(View.VISIBLE)

            animationOpenMenu(this.exportCsvButton!!, 3)
            animationOpenMenu(this.exportText!!, 3)
            animationOpenMenu(this.importCsvButton!!, 2)
            animationOpenMenu(this.importText!!, 2)
            animationOpenMenu(this.addButton!!, 1)
            animationOpenMenu(this.addText!!, 1)

            this.menuButton!!.animate().rotation(45f)

            this.isOpen = true
        }
    }

    /**
     * This function creates the contextMenu when an item of the listView is long pressed
     * @param menu the current contextMenu
     * @param v the current view
     * @param menuInfo aditionnal informations on the menu
     */
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        if (!this.isOpen) {
            super.onCreateContextMenu(menu, v, menuInfo)

            val title = this.myWordsList[(menuInfo as AdapterView.AdapterContextMenuInfo).position].headword
            menu.setHeaderTitle(title)

            menu.add(Menu.NONE, CONTEXT_MENU_MODIFY, Menu.NONE, R.string.modify)
            menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.delete)
        }
    }

    /**
     * This function creates the items of the contextMenu
     * @param item the item to be created
     * @return true if success, false else
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            CONTEXT_MENU_DELETE -> {
                this.delete((item.menuInfo as AdapterView.AdapterContextMenuInfo).position)
                return true
            }

            CONTEXT_MENU_MODIFY -> {
                this.modify((item.menuInfo as AdapterView.AdapterContextMenuInfo).position)
                return true
            }

            else -> return super.onContextItemSelected(item)
        }
    }

    /**
     * This function deletes the word at the selected position in the listView
     * @param position the position in the listView of the word the user want to delete
     */
    private fun delete(position: Int) {
        this.undo = false
        val pos = position
        val w = this.myWordsList[position]

        Snackbar.make(findViewById(R.id.list_words_layout)!!, w.headword + getString(R.string.deleted), Snackbar.LENGTH_LONG).setAction(R.string.undo, View.OnClickListener {
            this.myWordsList.add(pos, w)
            this.undo = true
        }).show()

        this.wdm = WordSQLITE(applicationContext)

        if (!undo) {
            wdm!!.delete(w.idWord!!)
            initListView()
            initSearch()
            myAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * This function launches the view details of a word and allows to modify it
     * @param position the position in the listView of the word the user want to see more details or to modify
     */
    private fun modify(position: Int) {
        //TODO
        //val wordDetailIntent = Intent(this, WordViewKot::class.java)
        val wordDetailIntent = Intent(this, WordViewEditKot::class.java)

        wordDetailIntent.putExtra(MainActivityKot.EXTRA_WORD, this.myWordsList[position])
        if (this.selectedDictionary != null) {
            wordDetailIntent.putExtra(MainActivityKot.EXTRA_DICTIONARY, selectedDictionary)
        } else {
            this.ddm = DictionarySQLITE(ctx = applicationContext, id = myWordsList[position].idDictionary)
            this.ddm!!.read()
            wordDetailIntent.putExtra(MainActivityKot.EXTRA_DICTIONARY, this.ddm!! as Dictionary)
        }

        startActivityForResult(wordDetailIntent, DELETE_WORD)

        if (this.filterWords!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.filterWords!!.setText("")
        }
    }

    /**
     * This function allow to rename the current dictionary
     */
    fun renameDictionary() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 30, 60, 0)

        //Set in lang field
        this.inLangField = EditText(this)
        this.inLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.inLangField!!.setText(this.selectedDictionary!!.inLang)
        this.inLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.inLangField!!.selectAll()
        layout.addView(inLangField)

        //Set out lang field
        this.outLangField = EditText(this)
        this.outLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.outLangField!!.setText(this.selectedDictionary!!.outLang)
        this.outLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.outLangField!!.selectAll()
        layout.addView(outLangField)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.rename_dictionary)
        builder.setView(layout)

        builder.setPositiveButton(R.string.modify) { dialog, which ->
            val ddm = DictionarySQLITE(this.applicationContext, null, null, this.selectedDictionary!!.idDictionary)
            if(!this.inLangField!!.getText().toString().isEmpty() && !this.outLangField!!.getText().toString().isEmpty()) {
                if (ddm!!.update(this.inLangField!!.getText().toString(), this.outLangField!!.getText().toString()) > 0) {

                    //Set dictionary object
                    this.selectedDictionary!!.inLang = this.inLangField!!.getText().toString()
                    this.selectedDictionary!!.outLang = this.outLangField!!.getText().toString()

                    Snackbar.make(findViewById(R.id.list_words_layout)!!, R.string.dictionary_renamed, Snackbar.LENGTH_LONG)
                            .setAction(R.string.close_button) { }.show()

                    this.supportActionBar!!.setTitle(this.selectedDictionary!!.getNameDictionary())

                    this.myAdapter!!.notifyDataSetChanged()
                } else {
                    Snackbar.make(findViewById(R.id.list_words_layout)!!, R.string.dictionary_not_renamed, Snackbar.LENGTH_LONG).setAction(R.string.close_button) { }.show()
                }
            }
            else {
                Snackbar.make(findViewById(R.id.list_words_layout)!!, R.string.dictionary_not_renamed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.close_button) { }
                        .show()
            }
            dialog.cancel()
        }

        builder.setNegativeButton(R.string.cancel
        ) { dialog, which -> dialog.cancel() }

        //Creating the dialog and opening the keyboard
        val alertDialog = builder.create()
        alertDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        //Listening the keyboard to handle a "Done" action
        this.inLangField!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            //Simulating a positive button click. The positive action is executed.
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            true
        })
        this.outLangField!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            //Simulating a positive button click. The positive action is executed.
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            true
        })
        alertDialog.show()
    }

    /**
     * This function delete the current dictionary after a confirmation
     */
    fun deleteDictionary() {
        val alert = AlertDialog.Builder(this)
        alert.setMessage(getString(R.string.delete_dictionary) + " ?")
        alert.setPositiveButton(getString(R.string.delete)) { dialog, whichButton ->
            Toast.makeText(this.applicationContext, this.selectedDictionary!!.getNameDictionary() + getString(R.string.deleted), Toast.LENGTH_SHORT).show()
            this.ddm = DictionarySQLITE(applicationContext)
            this.ddm!!.delete(this.selectedDictionary!!.idDictionary!!)
            finish()
        }

        alert.setNegativeButton(R.string.cancel) { dialog, whichButton -> }

        alert.show()
    }

    /**
     * This function is used to modify and show the toolbar menu when the stateMode changes
     */
    private fun showToolbarMenu() {
        if (this.menu != null) {
            this.menu!!.clear()
            if (this.stateMode == NORMAL_STATE) {
                if (this.selectedDictionary != null) {
                    this.menuInflater.inflate(R.menu.menu_list_words, menu)
                    this.supportActionBar!!.setTitle(this.selectedDictionary!!.getNameDictionary())
                } else {
                    this.supportActionBar!!.setTitle(R.string.allDico)
                }

            } else if (this.stateMode == DELETE_STATE) {
                this.menuInflater.inflate(R.menu.menu_word_delete, this.menu)
                this.supportActionBar!!.setTitle("0 " + getString(R.string.item))
                this.menu!!.findItem(R.id.action_delete_list).isVisible = false
            }
        }
    }

    /**
     * This function leave the deletion mode
     */
    private fun normalMode() {
        this.stateMode = NORMAL_STATE
        this.myAdapter = WordAdapterKot(this, R.layout.row_word, this.myWordsList, true)
        this.myAdapter!!.callback = this
        this.gridViewWords!!.setAdapter(this.myAdapter)
        this.showToolbarMenu()
    }

    /**
     * This function allows the user to delete multiple words at the same time
     */
    private fun multipleDeleteMode() {
        this.stateMode = DELETE_STATE

        this.myAdapter = WordAdapterKot(this, R.layout.row_delete_word, this.myWordsList, true)
        this.myAdapter!!.callback = this
        this.gridViewWords!!.setAdapter(this.myAdapter)
        showToolbarMenu()
    }

    /**
     * This function deletes all the words selected by the user.
     */
    private fun deleteSelectedWords() {
        val alert = AlertDialog.Builder(this)
        val deleteWords = this.myAdapter!!.getDeleteList()
        println(deleteWords)
        if (deleteWords.size == 1) {
            alert.setMessage(getString(R.string.delete) + " " + deleteWords.size + " " + getString(R.string.word) + " ?")
        } else {
            alert.setMessage(getString(R.string.delete) + " " + deleteWords.size + " " + getString(R.string.words) + " ?")
        }
        alert.setPositiveButton(getString(R.string.delete)) { dialog, whichButton ->
            for (word in deleteWords) {
                this.myWordsList.remove(word)
                this.wdm!!.delete(word.idWord!!)
        }
            this.myAdapter!!.notifyDataSetChanged()
            normalMode()
        }

        alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> }

        alert.show()
    }

    /**
     * This function is called when the user click on the option delete from the row menu of a word and it deletes it
     * @param position the position of the item to delete in the list
     */
    override fun deletePressed(position: Int) {
        this.delete(position)
    }

    /**
     * This function is called when the user on the option modify from the row menu of a word and it launch the view details of this word
     * @param position the position of the item to modify in the list
     */
    override fun modifyPressed(position: Int) {
        this.modify(position)
    }

    /**
     * This function return the value of the boolean open
     * @return true if succes, false else
     */
    override fun getOpen(): Boolean {
        return this.isOpen
    }

    /**
     * Method that notify the view that the deleteList has changed
     */
    override fun notifyDeleteListChanged() {
        val s = this.myAdapter!!.getDeleteList().size
        this.supportActionBar!!.setTitle("""${s} ${getString(R.string.item)}""")
        this.menu!!.findItem(R.id.action_delete_list).isVisible = s > 0
    }

    /**
     * Method called when returning to this activity
     * @param requestCode the int saying if you return from CSV import or not
     * @param resultCode the int saying if all ended up fine
     * @param data the CSV informations
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //If we are importing a file
        if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK) {
            val c = this

            //Handling the end of the import
            val import = ImportCSVKot()
            var importExec = import.importCSV(DictionarySQLITE(this.ctx, this.selectedDictionary!!.inLang, this.selectedDictionary!!.outLang, this.selectedDictionary!!.idDictionary), data!!.data, c)
        }
        else if ((requestCode == NEW_WORD || requestCode == DELETE_WORD) && resultCode == Activity.RESULT_OK) {
            this.myWordsList.clear()
            val tempList = ArrayList<Word>()

            this.wordsOffset = 0
            tempList.addAll(this.wdm!!.selectAll())

            this.allLoaded = false

            for (i in tempList.indices) {
                this.myWordsList.add(tempList[i])
            }
            this.myAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * This thread tell the adapter that the more words were loaded
     */
    private val returnRes = Runnable {
        this.myAdapter!!.notifyDataSetChanged()
        this.loadingMore = false
        if (this.actualListSize == this.myWordsList.size) {
            this.allLoaded = true
        }
        this.progressDialog!!.dismiss()
    }

    /**
     * This animation is used to make the floating menu appear
     * @param v the view to make appear
     * @param i an int to put a little delay between each animation
     */
    private fun animationOpenMenu(v: View, i: Int) {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, (-(size.y * applicationContext.resources.getInteger(R.integer.floating_menu_translation) / 100) * i) as Float)
        val pvhsX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)
        val pvhsY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)

        val animation = ObjectAnimator.ofPropertyValuesHolder(v, pvhY, pvhsX, pvhsY)
        animation.duration = 500
        animation.interpolator = OvershootInterpolator(0.9f)

        // Put a slight lag between each of the menu items to make it asymmetric
        animation.startDelay = (i * 20).toLong()
        animation.start()

        v.isEnabled = true
    }

    /**
     * This animation is used to make the floating menu disappear
     * @param v the view to make disappear
     * @param i an int to put a little delay between each animation
     */
    private fun animationCloseMenu(v: View, i: Int) {
        val pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f)
        val pvhsX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)
        val pvhsY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f)

        val animation = ObjectAnimator.ofPropertyValuesHolder(v, pvhY, pvhsX, pvhsY)
        animation.duration = 500
        animation.interpolator = OvershootInterpolator(0.9f)

        // Put a slight lag between each of the menu items to make it asymmetric
        animation.startDelay = (i * 20).toLong()
        animation.start()

        v.isEnabled = false
    }

    /**
     * This function is used to hide the keyBoard on click outside an editText
     * @param view the current view
     */
    private fun setupUI(view: View) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {

            view.setOnTouchListener { v, event ->
                KeyboardUtility.hideSoftKeyboard(this@ListWordsActivityKot)
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
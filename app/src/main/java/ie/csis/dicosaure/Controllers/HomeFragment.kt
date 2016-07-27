package ie.csis.dicosaure.Controllers

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import ie.csis.dicosaure.business.dictionary.Dictionary
import ie.csis.dicosaure.Controllers.Adapter.DictionaryAdapter
import ie.csis.dicosaure.Controllers.Adapter.DictionaryAdapterCallback
import ie.csis.dicosaure.Controllers.Lib.HeaderGridView
import ie.csis.dicosaure.Controllers.activities.MainActivity
import ie.csis.dicosaure.Controllers.activities.dictionary.ListWordsActivity
import ie.csis.dicosaure.business.dictionary.DictionarySQLITE
import java.util.*

/**
 * Created by dineen on 17/06/2016.
 */
class HomeFragment : Fragment(), DictionaryAdapterCallback {

    /**
     * The view corresponding to this fragment.
     * @see MainActivity
     */
    var v: View? = null

    /**
     * Initial dictionary list. Contains all the dictionary.
     */
    var dictionaries: ArrayList<Dictionary> = ArrayList<Dictionary>()

    /**
     * List of displayed dictionaries according to the research performed
     */
    var dictionariesDisplay: ArrayList<Dictionary> = ArrayList<Dictionary>()

    /**
     * Allow to display a list of Objects.
     */
    var gridView: HeaderGridView? = null

    /**
     * Button on the right corner of the screen to add dictionaries
     */
    var addButton: FloatingActionButton? = null

    /**
     * Custom ArrayAdapter to manage the different rows of the grid
     */
    var adapter: DictionaryAdapter? = null


    /**
     * Used to communicating with the database
     */
    var dictionaryModel: DictionarySQLITE? = null

    /**
     * Used to handle a undo action after deleting a dictionary
     */
    var undo: Boolean = false

    /**
     * Header of the gridView
     */
    var header: View? = null

    var headerButton: Button? = null

    var state: Int = 0

    /**
     * Toolbar menu
     */
    var menu: Menu? = null

    var searchBox: EditText? = null
    var inLangField: EditText? = null
    var outLangField: EditText? = null

    var myLastFirstVisibleItem: Int = 0
    var hidden: Boolean = false

    companion object {
        val CONTEXT_MENU_READ = 0
        val CONTEXT_MENU_UPDATE = 1
        val CONTEXT_MENU_DELETE = 2
        val CONTEXT_MENU_EXPORT = 3
        val NORMAL_STATE = 0
        val DELETE_STATE = 1
        val SELECT_FILE = 0
    }

    /**
     * Initialising the view
     */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.v = inflater!!.inflate(R.layout.fragment_home, container, false)
        return v
    }

    /**
     * Calling all the init functions
     */
    override fun onStart() {
        super.onStart()
        super.setHasOptionsMenu(true)
        this.state = NORMAL_STATE

        this.initData()
        this.initFloatingActionButton()
        this.initGridView()
        this.initEditText()
    }

    /**
     * Initialising the data model and selecting all the dictionaries
     */
    private fun initData() {
        this.dictionaryModel = DictionarySQLITE(this.context)
        this.dictionaries = ArrayList<Dictionary>(this.dictionaryModel!!.selectAll())
        println(dictionaries)
        this.dictionariesDisplay = ArrayList<Dictionary>(dictionaries)
    }

    /**
     * Creating the Floating Action Button to add a dictionary through a dialog window
     */
    private fun initFloatingActionButton() {
        this.addButton = (this.activity as MainActivity).addButton
        this.addButton!!.setOnClickListener(View.OnClickListener { create() })
    }

    /**
     * Initialising the GridView to display the dictionary list and making its clickables
     */
    private fun initGridView() {
        //Creating the GridView
        this.gridView = this.v!!.findViewById(R.id.dictionary_list) as HeaderGridView
        this.gridView!!.setDrawSelectorOnTop(true)

        if (this.state == NORMAL_STATE) {
            //Adding the GridView header
            this.gridView!!.removeHeaderView(header)
            this.header = this.activity.layoutInflater.inflate(R.layout.grid_view_header, null)
            this.gridView!!.addHeaderView(header)
            val b = this.header!!.findViewById(R.id.button_all) as Button
            b.setText(R.string.all_dictionaries)
            b.setOnClickListener { read(-1) }

            //Populating the GridView
            this.adapter = DictionaryAdapter(this.activity, R.layout.dictionary_row, this.dictionariesDisplay!!)
            this.adapter!!.setCallback(this)
            this.gridView!!.setAdapter(this.adapter)

            //Adding the context menu on each rows
            registerForContextMenu(this.gridView)
        }
        else if (state == DELETE_STATE) {
            //Adding the GridView header
            this.gridView!!.removeHeaderView(this.header)
            this.header = activity.layoutInflater.inflate(R.layout.grid_view_header, null)
            this.gridView!!.addHeaderView(this.header)
            this.headerButton = this.header!!.findViewById(R.id.button_all) as Button
            this.headerButton!!.setText(R.string.select_all)
            this.headerButton!!.setOnClickListener(View.OnClickListener { this.adapter!!.selectAll() })

            //Populating the GridView
            this.adapter = DictionaryAdapter(activity, R.layout.delete_dictionary_row, this.dictionariesDisplay!!)
            this.adapter!!.setCallback(this)
            this.gridView!!.setAdapter(this.adapter)
        }

        //Animating the gridView on Scroll
        this.myLastFirstVisibleItem = 0
        this.hidden = false
        this.addButton!!.animate().translationY(0f)
        this.gridView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                val currentFirstVisibleItem = gridView!!.getFirstVisiblePosition()
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    if (currentFirstVisibleItem > myLastFirstVisibleItem) {
                        if (!hidden) {
                            addButton!!.animate().translationY(350f)
                            hidden = true
                        }
                    } else if (currentFirstVisibleItem < myLastFirstVisibleItem) {
                        if (hidden) {
                            addButton!!.animate().translationY(0f)
                            hidden = false
                        }
                    }
                }
                myLastFirstVisibleItem = currentFirstVisibleItem
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                val lastInScreen = firstVisibleItem + visibleItemCount
                if (lastInScreen == totalItemCount) {
                    if (hidden!!) {
                        addButton!!.animate().translationY(0f)
                        hidden = false
                    }
                }
            }
        })

        //Animating the gridView on appear
        val anim = AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left)
        this.gridView!!.setAnimation(anim)
        anim.start()

    }

    /**
     * Initialising the search box to dynamically researching on the dictionary list
     */
    private fun initEditText() {
        //Creating the EditText for searching inside the dictionaries list
        this.searchBox = this.v!!.findViewById(R.id.search_field) as EditText
        this.searchBox!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                dictionariesDisplay!!.clear()
                val search = s.toString()
                for (i in dictionaries!!.indices) {
                    if (dictionaries!!.get(i).getNameDictionary().toLowerCase().contains(search.toLowerCase())) {
                        dictionariesDisplay!!.add(dictionaries!!.get(i))
                    }
                }
                adapter!!.notifyDataSetChanged()
            }
        })

        this.searchBox!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (!dictionariesDisplay!!.isEmpty())
                read(0)
            true
        })

    }

    /**
     * Method which allows user to create a dictionary with a unique name
     */
    fun create() {
        //Creating the dialog layout
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 30, 60, 0)

        //Creating the EditText

        //add inlang field
        this.inLangField = EditText(this.activity)
        this.inLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.inLangField!!.setHint(R.string.dictionary_inLang)
        this.inLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.inLangField!!.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)
        layout.addView(this.inLangField)

        //add outlang field
        this.outLangField = EditText(this.activity)
        this.outLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.outLangField!!.setHint(R.string.dictionary_outLang)
        this.outLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.outLangField!!.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)
        layout.addView(this.outLangField)

        //Creating the dialog builder
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.add_dictionary)

        //Adding the layout to the dialog
        builder.setView(layout)

        //Dialog positive action
        builder.setPositiveButton(R.string.add) { dialog, which ->
            if (!this.inLangField!!.getText().toString().isEmpty() && !this.outLangField!!.getText().toString().isEmpty()) {
                val d = DictionarySQLITE(this.context, this.inLangField!!.getText().toString(), this.outLangField!!.getText().toString())
                if (d.save() > 0) {
                    this.dictionariesDisplay!!.add(d)
                    this.dictionaries!!.add(d)
                    if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
                        this.searchBox!!.setText("")
                    }
                    this.read(this.dictionariesDisplay!!.indexOf(d))
                }
                else {
                    Snackbar.make((activity as MainActivity).rootLayout!!, R.string.dictionary_not_added, Snackbar.LENGTH_LONG)
                            .show()
                }
            }
            else {
                Snackbar.make((activity as MainActivity).rootLayout!!, R.string.dictionary_not_added_empty_string, Snackbar.LENGTH_LONG)
                        .show()
            }
            dialog.cancel()
        }

        //Dialog negative action
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
        builder.setNeutralButton(R.string.from_csv) { dialog, which ->
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/comma-separated-values"

            // special intent for Samsung file manager
            val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
            sIntent.putExtra("CONTENT_TYPE", "text/comma-separated-values")
            sIntent.addCategory(Intent.CATEGORY_DEFAULT)

            if (activity.packageManager.resolveActivity(sIntent, 0) != null) {
                startActivityForResult(sIntent, SELECT_FILE)
            } else {
                startActivityForResult(intent, SELECT_FILE)
            }
            if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
                this.searchBox!!.setText("")
            }
        }

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
     * Method which allows user to delete a dictionary with his position
     * @param position the position of the dictionary you want to delete
     */
    override fun delete(position: Int) {
        val dictionary = this.dictionariesDisplay!!.get(position) //Get dictionary
        this.dictionariesDisplay!!.remove(dictionary) //delete from array

        this.adapter!!.notifyDataSetChanged()
        this.undo = false

        val snack = Snackbar.make((activity as MainActivity).rootLayout!!, """${dictionary.getNameDictionary()} ${getString(R.string.deleted)}""", Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, View.OnClickListener {
                    this.undo = true
                })

        snack.getView().addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                //Once snackbar is closed, whatever the way : undo button clicked, change activity, an other snackbar, etc.
                if (!undo) {
                    dictionaries!!.remove(dictionary)

                    val progressDialog = ProgressDialog(activity)
                    progressDialog.setMessage(getString(R.string.delete_progress))
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    progressDialog.isIndeterminate = true
                    progressDialog.setCancelable(false)
                    progressDialog.window.setGravity(Gravity.BOTTOM)
                    progressDialog.show()

                    val handler = object : Handler() {
                        override fun handleMessage(msg: Message) {
                            progressDialog.dismiss()
                        }
                    }

                    val t = object : Thread() {
                        override fun run() {
                            dictionaryModel!!.delete(dictionary.idDictionary!!)
                            handler.sendEmptyMessage(0)
                        }
                    }
                    t.start()
                }
                else {
                    dictionariesDisplay!!.add(position, dictionary)
                    adapter!!.notifyDataSetChanged()
                }
            }
        })
        snack.show()
    }

    /**
     * Method which allows user to update a dictionary with his position
     * @param position the position of the dictionary you want to update
     */
    override fun update(position: Int) {
        val dictionary = this.dictionariesDisplay!!.get(position)//get dictionary

        //Set popup update dictionary
        val layout = LinearLayout(this.activity)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 30, 60, 0)

        //Set in lang field
        this.inLangField = EditText(this.activity)
        this.inLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.inLangField!!.setText(dictionary.inLang)
        this.inLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.inLangField!!.selectAll()
        layout.addView(inLangField)

        //Set out lang field
        this.outLangField = EditText(this.activity)
        this.outLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.outLangField!!.setText(dictionary.outLang)
        this.outLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.outLangField!!.selectAll()
        layout.addView(outLangField)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.update_dictionary)
        builder.setView(layout)

        builder.setPositiveButton(R.string.modify) { dialog, which ->
            val title = dictionary.getNameDictionary()
            this.dictionaryModel!!.idDictionary = dictionary.idDictionary
            if(!this.inLangField!!.getText().toString().isEmpty() && !this.outLangField!!.getText().toString().isEmpty()) {
                if (this.dictionaryModel!!.update(this.inLangField!!.getText().toString(), this.outLangField!!.getText().toString()) > 0) {
                    this.adapter!!.notifyDataSetChanged()

                    //Set dictionary object
                    dictionary.inLang = this.inLangField!!.getText().toString()
                    dictionary.outLang = this.outLangField!!.getText().toString()

                    //Set search box
                    if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
                        this.searchBox!!.setText("")
                    }

                    Snackbar.make((activity as MainActivity).rootLayout!!, R.string.dictionary_renamed, Snackbar.LENGTH_LONG)
                            .show()
                } else {
                    Snackbar.make((activity as MainActivity).rootLayout!!, R.string.dictionary_not_renamed, Snackbar.LENGTH_LONG)
                            .show()
                }
            }
            else {
                Snackbar.make((activity as MainActivity).rootLayout!!, R.string.dictionary_not_renamed, Snackbar.LENGTH_LONG)
                        .show()
            }
            dialog.cancel()
        }

        builder.setNegativeButton(R.string.cancel) {
            dialog, which -> dialog.cancel()
        }

        //Creating the dialog and opening the keyboard
        val alertDialog = builder.create()
        alertDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        //Listening the keyboard to handle a "Done" action
        this.outLangField!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            //Simulating a positive button click. The positive action is executed.
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            true
        })

        this.inLangField!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            //Simulating a positive button click. The positive action is executed.
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            true
        })

        alertDialog.show()
    }

    /**
     * Method which allows user to read a dictionary with his position
     * @param position the position of the dictionary you want to read
     */
    override fun read(position: Int) {
        //Set object on the cell
        val intent = Intent(this.getActivity(), ListWordsActivity::class.java)
        if (position != -1) {
            intent.putExtra(MainActivity.EXTRA_DICTIONARY, this.dictionariesDisplay!!.get(position))
        }
        startActivity(intent)

        //set searchBox into empty
        if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.searchBox!!.setText("")
        }
    }

    /**
     * Method which allows user to export a dictionary with his position
     * @param position the position of the dictionary you want to export
     */
    override fun export(position: Int) {
        val exportCSVintent = Intent(this.activity, CSVExportKot::class.java)
        exportCSVintent.putExtra(MainActivity.EXTRA_DICTIONARY, this.dictionariesDisplay[position])
        startActivity(exportCSVintent)
        if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.searchBox!!.setText("")
        }
    }

    /**
     * Changing the view to delete mode
     */
    override fun notifyDeleteListChanged() {
        //Popup message to delete multi dictionaries
        val s = this.adapter!!.deleteList.size
        this.menu!!.findItem(R.id.nb_items).setTitle("""${s} ${getString(R.string.item)}""")
        this.menu!!.findItem(R.id.action_delete_list).isVisible = s > 0
        if (this.adapter!!.all_selected)
            this.headerButton!!.setText(R.string.deselect_all)
        else
            this.headerButton!!.setText(R.string.select_all)
    }

    /**
     * Resuming the view
     */
    override fun onResume() {
        super.onResume()
    }

    /**
     * Creating the Context Menu at the top right of the view
     * @param menu the Context Menu
     * @param v the current view
     * @param menuInfo additional information regarding the creation of the Context Menu
     * @param menuInfo additional information regarding the creation of the Context Menu
     */
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val info = menuInfo as AdapterView.AdapterContextMenuInfo

        val title = this.adapter!!.getItem(info.position - 1).getNameDictionary()
        menu.setHeaderTitle(title)

        menu.add(Menu.NONE, CONTEXT_MENU_READ, Menu.NONE, R.string.open)
        menu.add(Menu.NONE, CONTEXT_MENU_UPDATE, Menu.NONE, R.string.rename)
        menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.delete)
        menu.add(Menu.NONE, CONTEXT_MENU_EXPORT, Menu.NONE, R.string.csvexport_export)
    }

    /**
     * Calling the different functions on dictionaries depending on the user choice
     * @param item the dictionary menu
     * @return true if success, false else
     */
    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val info = item!!.menuInfo as AdapterView.AdapterContextMenuInfo
        when (item.itemId) {
            CONTEXT_MENU_READ -> {
                read(info.position - 1)
                return true
            }
            CONTEXT_MENU_UPDATE -> {
                update(info.position - 1)
                return true
            }
            CONTEXT_MENU_DELETE -> {
                delete(info.position - 1)
                return true
            }
            CONTEXT_MENU_EXPORT -> {
                export(info.position - 1)
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    /**
     * This function is called when returning to this activity
     * @param requestCode the int saying if you return from CSV import or not
     * @param resultCode the int saying if all ended up fine
     * @param data the CSV informations
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //If we are importing a file
        if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK) {
            //Creating the file
            val fileUri = data!!.data
            val fileName = fileUri.lastPathSegment

            //Creating a dictionary named like the file (without the .csv)
            val d = DictionarySQLITE(this.context, "InLang", "OutLang")
            //try to detect a pattern for the name
            var dicoName = fileName.split(".")[0].split(" -> ")
            if (dicoName.size != 2) {
                dicoName = fileName.split(".")[0].split(" - ")
            }
            if (dicoName.size != 2) {
                dicoName = fileName.split(".")[0].split("-")
            }
            if (dicoName.size != 2) {
                dicoName = fileName.split(".")[0].split(" _ ")
            }
            if (dicoName.size != 2) {
                dicoName = fileName.split(".")[0].split("_")
            }
            if (dicoName.size != 2) {
                dicoName = fileName.split(".")[0].split(" ")
            }
            if (dicoName.size != 2) {
                dicoName = fileName.split(".")[0].split(".")
            }
            // set the name of the dictionary if he is detected from the file name
            if (dicoName.size == 2) {
                d.inLang = dicoName[0]
                d.outLang = dicoName[1]
            }

            val c = this.activity

            if (d.save() < 0) {
                d.readByInLangOutLang()
            }
            this.dictionariesDisplay.add(d)
            this.dictionaries.add(d)
            if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
                this.searchBox!!.setText("")
            }
            val import = ImportCSV()
            import.importCSV(d, data.data, c)

            //Handling the end of the import
            val handler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    val intent = Intent(c, ListWordsActivity::class.java)
                    intent.putExtra(MainActivity.EXTRA_DICTIONARY, d)
                    intent.putExtra(MainActivity.EXTRA_RENAME, true)
                    c.startActivity(intent)
                }
            }
            handler.sendEmptyMessage(0)
        }
    }

    /**
     * Creating the option menu
     * @param m the menu
     * @param inflater the menu options
     */
    override fun onCreateOptionsMenu(m: Menu?, inflater: MenuInflater?) {
        this.menu = m
        super.onCreateOptionsMenu(this.menu, inflater)
        showMenu()
    }

    /**
     * Displaying the menu
     */
    fun showMenu() {
        this.menu!!.clear()
        if (state == NORMAL_STATE) {
            this.activity.menuInflater.inflate(R.menu.menu_home, this.menu)
        }
        else if (state == DELETE_STATE) {
            this.activity.menuInflater.inflate(R.menu.menu_home_delete, this.menu)
            val s = this.adapter!!.deleteList.size
            this.menu!!.findItem(R.id.nb_items).setTitle("""${s} ${getString(R.string.item)}""")
            this.menu!!.findItem(R.id.action_delete_list).isVisible = s > 0
        }
    }

    /**
     * Calling the functions on the dictionary depending on the user choice
     * @param item the dictionary menu
     * @return true if success, false else
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_add_dictionary -> {
                this.create()
                return true
            }
            R.id.action_multiple_delete -> {
                this.state = DELETE_STATE
                this.initGridView()
                this.showMenu()
                return true
            }
            R.id.action_delete_list -> {
                val alert = AlertDialog.Builder(activity)
                val s = adapter!!.deleteList.size
                if (s == 1) {
                    alert.setMessage("""${getString(R.string.delete)} ${s} ${getString(R.string.dictionary)} ?""")
                } else {
                    alert.setMessage("""${getString(R.string.delete)} ${s} ${getString(R.string.dictionaries)} ?""")
                }
                alert.setPositiveButton(getString(R.string.delete)) { dialog, whichButton ->
                    val progressDialog = ProgressDialog(activity)
                    progressDialog.setMessage(getString(R.string.delete_progress))
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    progressDialog.isIndeterminate = true
                    progressDialog.setCancelable(false)
                    progressDialog.window.setGravity(Gravity.BOTTOM)
                    progressDialog.show()

                    val handler = object : Handler() {
                        override fun handleMessage(msg: Message) {
                            progressDialog.dismiss()
                            state = NORMAL_STATE
                            initGridView()
                            showMenu()
                        }
                    }
                    val t = object : Thread() {
                        override fun run() {

                            for (i in 0..s - 1) {
                                val d = adapter!!.deleteList.get(i)
                                dictionaries!!.remove(d)
                                dictionariesDisplay!!.remove(d)
                                dictionaryModel!!.delete(d.idDictionary!!)
                            }
                            handler.sendEmptyMessage(0)
                        }
                    }
                    t.start()
                }
                alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> }
                alert.show()
                return true
            }
            R.id.action_cancel -> {
                state = NORMAL_STATE
                initGridView()
                showMenu()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}